package com.example.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherConditionInfo(
    val title: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val accentColor: Color,
    val isRainy: Boolean = false,
    val isStormy: Boolean = false,
    val isSnowy: Boolean = false
)

object WeatherConditionHelper {
    fun getCondition(weatherCode: Int, isDay: Boolean = true): WeatherConditionInfo {
        return when (weatherCode) {
            0 -> WeatherConditionInfo(
                title = if (isDay) "Clear Sky" else "Clear Night",
                icon = Icons.Default.WbSunny,
                primaryColor = Color(0xFFF59E0B), // Amber 500
                accentColor = Color(0xFFFEF3C7)
            )
            1 -> WeatherConditionInfo(
                title = "Mainly Clear",
                icon = Icons.Default.WbSunny,
                primaryColor = Color(0xFF38BDF8), // Sky 400
                accentColor = Color(0xFFE0F2FE)
            )
            2 -> WeatherConditionInfo(
                title = "Partly Cloudy",
                icon = Icons.Default.CloudQueue,
                primaryColor = Color(0xFF0EA5E9), // Sky 500
                accentColor = Color(0xFFBAE6FD)
            )
            3 -> WeatherConditionInfo(
                title = "Overcast",
                icon = Icons.Default.Cloud,
                primaryColor = Color(0xFF64748B), // Slate 500
                accentColor = Color(0xFFCBD5E1)
            )
            45, 48 -> WeatherConditionInfo(
                title = "Foggy",
                icon = Icons.Default.Cloud,
                primaryColor = Color(0xFF94A3B8), // Slate 400
                accentColor = Color(0xFFE2E8F0)
            )
            51, 53, 55 -> WeatherConditionInfo(
                title = "Light Drizzle",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF0284C7), // Sky 600
                accentColor = Color(0xFFBAE6FD),
                isRainy = true
            )
            56, 57 -> WeatherConditionInfo(
                title = "Freezing Drizzle",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF0369A1), // Sky 700
                accentColor = Color(0xFFE0F2FE),
                isRainy = true
            )
            61 -> WeatherConditionInfo(
                title = "Slight Rain",
                icon = Icons.Default.WaterDrop,
                primaryColor = Color(0xFF0284C7),
                accentColor = Color(0xFFBAE6FD),
                isRainy = true
            )
            63 -> WeatherConditionInfo(
                title = "Moderate Rain",
                icon = Icons.Default.WaterDrop,
                primaryColor = Color(0xFF0369A1),
                accentColor = Color(0xFF7DD3FC),
                isRainy = true
            )
            65 -> WeatherConditionInfo(
                title = "Heavy Rain",
                icon = Icons.Default.WaterDrop,
                primaryColor = Color(0xFF1D4ED8),
                accentColor = Color(0xFF93C5FD),
                isRainy = true
            )
            66, 67 -> WeatherConditionInfo(
                title = "Freezing Rain",
                icon = Icons.Default.AcUnit,
                primaryColor = Color(0xFF2563EB),
                accentColor = Color(0xFFDBEAFE),
                isRainy = true,
                isSnowy = true
            )
            71, 73, 75 -> WeatherConditionInfo(
                title = "Snowfall",
                icon = Icons.Default.AcUnit,
                primaryColor = Color(0xFF60A5FA),
                accentColor = Color(0xFFEFF6FF),
                isSnowy = true
            )
            77 -> WeatherConditionInfo(
                title = "Snow Grains",
                icon = Icons.Default.AcUnit,
                primaryColor = Color(0xFF60A5FA),
                accentColor = Color(0xFFEFF6FF),
                isSnowy = true
            )
            80, 81, 82 -> WeatherConditionInfo(
                title = "Rain Showers",
                icon = Icons.Default.WaterDrop,
                primaryColor = Color(0xFF0284C7),
                accentColor = Color(0xFF38BDF8),
                isRainy = true
            )
            85, 86 -> WeatherConditionInfo(
                title = "Snow Showers",
                icon = Icons.Default.AcUnit,
                primaryColor = Color(0xFF3B82F6),
                accentColor = Color(0xFFDBEAFE),
                isSnowy = true
            )
            95 -> WeatherConditionInfo(
                title = "Thunderstorm",
                icon = Icons.Default.Thunderstorm,
                primaryColor = Color(0xFF7C3AED),
                accentColor = Color(0xFFDDD6FE),
                isStormy = true,
                isRainy = true
            )
            96, 99 -> WeatherConditionInfo(
                title = "Thunderstorm with Hail",
                icon = Icons.Default.Thunderstorm,
                primaryColor = Color(0xFF5B21B6),
                accentColor = Color(0xFFC4B5FD),
                isStormy = true,
                isRainy = true
            )
            else -> WeatherConditionInfo(
                title = "Variable Conditions",
                icon = Icons.Default.WbCloudy,
                primaryColor = Color(0xFF0284C7),
                accentColor = Color(0xFFE0F2FE)
            )
        }
    }
}
