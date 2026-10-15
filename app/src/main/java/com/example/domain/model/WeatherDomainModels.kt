package com.example.domain.model

data class CityLocation(
    val id: Long,
    val name: String,
    val admin1: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = buildString {
            append(name)
            if (!admin1.isNullOrBlank()) append(", $admin1")
            if (!country.isNullOrBlank()) append(", $country")
        }

    val shortDisplayName: String
        get() = if (!country.isNullOrBlank()) "$name, $country" else name
}

data class CurrentWeather(
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val humidityPercentage: Int,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val isDay: Boolean,
    val precipitationMm: Double,
    val uvIndex: Double
)

data class DailyForecast(
    val dateIso: String,
    val dayOfWeek: String,
    val weatherCode: Int,
    val conditionDescription: String,
    val maxTempC: Double,
    val minTempC: Double,
    val precipitationSumMm: Double,
    val windSpeedMaxKmh: Double,
    val uvIndexMax: Double
)

enum class ActivitySuitabilityLevel(val label: String) {
    EXCELLENT("Ideal for Outdoors"),
    GOOD("Good for Outdoors"),
    MODERATE("Fair Outdoor Conditions"),
    POOR("Indoor Recommended")
}

data class OutdoorSuitability(
    val score: Int, // 0 - 100
    val level: ActivitySuitabilityLevel,
    val headline: String,
    val rationale: String
)

data class PlanningIntelligence(
    val outdoorSuitability: OutdoorSuitability,
    val umbrellaNeeded: Boolean,
    val umbrellaAdvice: String,
    val travelAdvisory: String,
    val windAdvisory: String?,
    val clothingAdvisory: String,
    val uvAdvisory: String?
)

data class WeatherDashboardData(
    val location: CityLocation,
    val current: CurrentWeather,
    val dailyForecasts: List<DailyForecast>,
    val planning: PlanningIntelligence
)
