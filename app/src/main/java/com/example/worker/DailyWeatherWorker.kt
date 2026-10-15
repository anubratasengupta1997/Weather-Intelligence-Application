package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.WeatherDatabase
import com.example.data.network.OpenMeteoApiService
import com.example.domain.WeatherRepository
import com.example.domain.model.CityLocation
import com.example.location.LocationHelper
import com.example.notification.WeatherNotificationHelper
import kotlinx.coroutines.flow.firstOrNull

class DailyWeatherWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val database = WeatherDatabase.getInstance(context)
            val apiService = OpenMeteoApiService.create()
            val repository = WeatherRepository(apiService, database.recentCityDao())

            // 1. Try to get user's current physical location
            var targetCity: CityLocation? = LocationHelper.getCurrentCityLocation(context)

            // 2. Fallback to user's most recent searched city if location is not enabled/available
            if (targetCity == null) {
                val recentCities = database.recentCityDao().getRecentCities().firstOrNull()
                val topRecent = recentCities?.firstOrNull()
                if (topRecent != null) {
                    targetCity = CityLocation(
                        id = topRecent.id,
                        name = topRecent.name,
                        admin1 = topRecent.admin1,
                        country = topRecent.country,
                        countryCode = topRecent.countryCode,
                        latitude = topRecent.latitude,
                        longitude = topRecent.longitude
                    )
                }
            }

            // 3. Fallback to default popular city if no recent cities exist
            if (targetCity == null) {
                targetCity = repository.popularCities.first()
            }

            // 4. Fetch the latest live weather forecast
            val dashboardData = repository.fetchWeatherDashboard(targetCity)

            // 5. Post the rich daily summary notification
            WeatherNotificationHelper.showDailySummaryNotification(
                context = context,
                data = dashboardData
            )

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
