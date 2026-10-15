package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.WeatherDashboardData
import com.example.ui.TemperatureUnit
import com.example.ui.formatTemp
import java.util.Locale

object WeatherNotificationHelper {
    const val CHANNEL_ID = "weather_daily_summary"
    const val CHANNEL_NAME = "Daily Weather Summary"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily weather intelligence summary and activity planning recommendations"
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showDailySummaryNotification(
        context: Context,
        data: WeatherDashboardData,
        unit: TemperatureUnit = TemperatureUnit.CELSIUS
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val locationName = data.location.name
        val current = data.current
        val today = data.dailyForecasts.firstOrNull()
        val planning = data.planning

        val highLowStr = if (today != null) {
            "High: ${today.maxTempC.formatTemp(unit)} / Low: ${today.minTempC.formatTemp(unit)}"
        } else {
            "Temp: ${current.temperatureC.formatTemp(unit)}"
        }

        val precipStr = String.format(Locale.US, "Rain: %.1f mm", today?.precipitationSumMm ?: current.precipitationMm)

        val title = "Weather Intel • $locationName"
        val shortContent = "${current.temperatureC.formatTemp(unit)} • ${current.conditionDescription} ($highLowStr)"

        val bigText = buildString {
            append("${current.temperatureC.formatTemp(unit)} • ${current.conditionDescription}\n")
            append("$highLowStr • $precipStr\n")
            append("Activity: ${planning.outdoorSuitability.level.label} (${planning.outdoorSuitability.score}/100)\n")
            append(planning.umbrellaAdvice)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Handled when notification permission is denied
        }
    }
}
