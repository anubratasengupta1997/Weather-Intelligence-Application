package com.example.domain

import com.example.data.local.RecentCityDao
import com.example.data.local.RecentCityEntity
import com.example.data.network.OpenMeteoApiService
import com.example.domain.model.CityLocation
import com.example.domain.model.CurrentWeather
import com.example.domain.model.DailyForecast
import com.example.domain.model.WeatherDashboardData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherRepository(
    private val apiService: OpenMeteoApiService,
    private val recentCityDao: RecentCityDao
) {
    val recentCities: Flow<List<RecentCityEntity>> = recentCityDao.getRecentCities()

    val popularCities = listOf(
        CityLocation(5128581L, "New York", "New York", "United States", "US", 40.7128, -74.0060),
        CityLocation(2643743L, "London", "England", "United Kingdom", "GB", 51.5074, -0.1278),
        CityLocation(1850147L, "Tokyo", "Tokyo", "Japan", "JP", 35.6762, 139.6503),
        CityLocation(2988507L, "Paris", "Île-de-France", "France", "FR", 48.8566, 2.3522),
        CityLocation(5391959L, "San Francisco", "California", "United States", "US", 37.7749, -122.4194),
        CityLocation(2147714L, "Sydney", "New South Wales", "Australia", "AU", -33.8688, 151.2093)
    )

    suspend fun searchCities(query: String): List<CityLocation> {
        if (query.trim().length < 2) return emptyList()
        val response = apiService.searchCity(cityName = query.trim())
        val results = response.results ?: return emptyList()
        return results.map { dto ->
            CityLocation(
                id = dto.id,
                name = dto.name,
                admin1 = dto.admin1,
                country = dto.country,
                countryCode = dto.countryCode,
                latitude = dto.latitude,
                longitude = dto.longitude
            )
        }
    }

    suspend fun fetchWeatherDashboard(city: CityLocation): WeatherDashboardData {
        val forecast = apiService.getForecast(
            latitude = city.latitude,
            longitude = city.longitude
        )

        // Parse Current Weather
        val current = forecast.current
        val legacyCurrent = forecast.currentWeather

        val tempC = current?.temperature2m ?: legacyCurrent?.temperature ?: 20.0
        val apparentTempC = current?.apparentTemperature ?: tempC
        val humidity = current?.relativeHumidity2m?.toInt() ?: 55
        val windKmh = current?.windSpeed10m ?: legacyCurrent?.windspeed ?: 10.0
        val windDir = legacyCurrent?.winddirection ?: 0.0
        val weatherCode = current?.weatherCode ?: legacyCurrent?.weathercode ?: 0
        val precipMm = current?.precipitation ?: 0.0
        val isDay = (legacyCurrent?.isDay ?: 1) == 1

        val condition = WeatherConditionHelper.getCondition(weatherCode, isDay)

        // Parse 7-Day Forecast
        val daily = forecast.daily
        val dailyList = mutableListOf<DailyForecast>()
        val times = daily?.time ?: emptyList()
        val codes = daily?.weatherCode ?: daily?.weathercode ?: emptyList()
        val maxTemps = daily?.temperature2mMax ?: emptyList()
        val minTemps = daily?.temperature2mMin ?: emptyList()
        val precipSums = daily?.precipitationSum ?: emptyList()
        val windMaxes = daily?.windSpeed10mMax ?: daily?.windspeed10mMax ?: emptyList()
        val uvMaxes = daily?.uvIndexMax ?: emptyList()

        val todayDate = try { LocalDate.now() } catch (e: Exception) { null }
        val outputFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

        for (i in times.indices) {
            val isoDate = times[i]
            val parsedDate = try { LocalDate.parse(isoDate) } catch (e: Exception) { null }
            val dayName = when {
                parsedDate != null && todayDate != null && parsedDate == todayDate -> "Today"
                parsedDate != null && todayDate != null && parsedDate == todayDate.plusDays(1) -> "Tomorrow"
                parsedDate != null -> parsedDate.format(outputFormatter)
                else -> isoDate
            }

            val code = codes.getOrNull(i) ?: weatherCode
            val dayCondition = WeatherConditionHelper.getCondition(code, isDay = true)

            dailyList.add(
                DailyForecast(
                    dateIso = isoDate,
                    dayOfWeek = dayName,
                    weatherCode = code,
                    conditionDescription = dayCondition.title,
                    maxTempC = maxTemps.getOrNull(i) ?: tempC,
                    minTempC = minTemps.getOrNull(i) ?: (tempC - 5.0),
                    precipitationSumMm = precipSums.getOrNull(i) ?: 0.0,
                    windSpeedMaxKmh = windMaxes.getOrNull(i) ?: windKmh,
                    uvIndexMax = uvMaxes.getOrNull(i) ?: 4.0
                )
            )
        }

        val domainCurrent = CurrentWeather(
            temperatureC = tempC,
            apparentTemperatureC = apparentTempC,
            humidityPercentage = humidity,
            windSpeedKmh = windKmh,
            windDirectionDegrees = windDir,
            weatherCode = weatherCode,
            conditionDescription = condition.title,
            isDay = isDay,
            precipitationMm = precipMm,
            uvIndex = dailyList.firstOrNull()?.uvIndexMax ?: 4.0
        )

        val planning = WeatherIntelligenceCalculator.computePlanningIntelligence(
            current = domainCurrent,
            dailyList = dailyList
        )

        // Save to Recent Searches in Room DB
        recentCityDao.insertOrUpdate(
            RecentCityEntity(
                id = city.id,
                name = city.name,
                admin1 = city.admin1,
                country = city.country,
                countryCode = city.countryCode,
                latitude = city.latitude,
                longitude = city.longitude,
                searchedAt = System.currentTimeMillis()
            )
        )

        return WeatherDashboardData(
            location = city,
            current = domainCurrent,
            dailyForecasts = dailyList,
            planning = planning
        )
    }

    suspend fun removeRecentCity(entity: RecentCityEntity) {
        recentCityDao.delete(entity)
    }

    suspend fun clearAllRecent() {
        recentCityDao.clearAll()
    }
}
