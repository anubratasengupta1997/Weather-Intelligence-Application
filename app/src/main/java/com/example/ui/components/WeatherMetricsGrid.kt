package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrentWeather
import com.example.domain.model.DailyForecast
import com.example.ui.TemperatureUnit
import com.example.ui.formatTemp
import java.util.Locale

@Composable
fun WeatherMetricsGrid(
    current: CurrentWeather,
    todayForecast: DailyForecast?,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Weather Highlights",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Row 1: Wind Speed & Precipitation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val windSpeedVal = String.format(Locale.US, "%.1f km/h", current.windSpeedKmh)
            val windDesc = when {
                current.windSpeedKmh < 10 -> "Light breeze"
                current.windSpeedKmh < 25 -> "Moderate wind"
                current.windSpeedKmh < 40 -> "Fresh breeze"
                else -> "Strong gale"
            }
            MetricHighlightCard(
                title = "Wind Speed",
                value = windSpeedVal,
                subtitle = windDesc,
                icon = Icons.Default.Air,
                accentColor = Color(0xFF0EA5E9),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_wind")
            )

            val precipVal = String.format(Locale.US, "%.1f mm", todayForecast?.precipitationSumMm ?: current.precipitationMm)
            val precipDesc = when {
                (todayForecast?.precipitationSumMm ?: current.precipitationMm) > 5.0 -> "Heavy precipitation"
                (todayForecast?.precipitationSumMm ?: current.precipitationMm) > 0.5 -> "Scattered rain"
                else -> "No rain expected"
            }
            MetricHighlightCard(
                title = "Precipitation",
                value = precipVal,
                subtitle = precipDesc,
                icon = Icons.Default.WaterDrop,
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_precip")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Humidity & High / Low Temp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val humidityVal = "${current.humidityPercentage}%"
            val humidityDesc = when {
                current.humidityPercentage < 30 -> "Dry air"
                current.humidityPercentage in 30..60 -> "Comfortable"
                else -> "High humidity"
            }
            MetricHighlightCard(
                title = "Humidity",
                value = humidityVal,
                subtitle = humidityDesc,
                icon = Icons.Default.Opacity,
                accentColor = Color(0xFF10B981),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_humidity")
            )

            val highLowVal = if (todayForecast != null) {
                "${todayForecast.maxTempC.formatTemp(unit)} / ${todayForecast.minTempC.formatTemp(unit)}"
            } else {
                "${current.temperatureC.formatTemp(unit)} / ${(current.temperatureC - 5).formatTemp(unit)}"
            }
            MetricHighlightCard(
                title = "High / Low",
                value = highLowVal,
                subtitle = "Today's range",
                icon = Icons.Default.DeviceThermostat,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_high_low")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: UV Index & Apparent Temp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val uvVal = String.format(Locale.US, "%.1f", current.uvIndex)
            val uvDesc = when {
                current.uvIndex >= 8 -> "Very High"
                current.uvIndex >= 6 -> "High risk"
                current.uvIndex >= 3 -> "Moderate"
                else -> "Low risk"
            }
            MetricHighlightCard(
                title = "UV Index",
                value = uvVal,
                subtitle = uvDesc,
                icon = Icons.Default.WbSunny,
                accentColor = Color(0xFFEC4899),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_uv")
            )

            val apparentVal = current.apparentTemperatureC.formatTemp(unit)
            val diff = current.apparentTemperatureC - current.temperatureC
            val diffDesc = when {
                diff > 1.5 -> "Warmer than actual"
                diff < -1.5 -> "Colder with windchill"
                else -> "Similar to actual"
            }
            MetricHighlightCard(
                title = "Feels Like",
                value = apparentVal,
                subtitle = diffDesc,
                icon = Icons.Default.DeviceThermostat,
                accentColor = Color(0xFF8B5CF6),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_card_feels_like")
            )
        }
    }
}

@Composable
fun MetricHighlightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
