package com.example.ui

import com.example.domain.model.WeatherDashboardData
import kotlin.math.roundToInt

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F")
}

fun Double.formatTemp(unit: TemperatureUnit): String {
    return if (unit == TemperatureUnit.CELSIUS) {
        "${this.roundToInt()}°C"
    } else {
        val fahrenheit = (this * 9.0 / 5.0) + 32.0
        "${fahrenheit.roundToInt()}°F"
    }
}

fun Double.toUnitValue(unit: TemperatureUnit): Double {
    return if (unit == TemperatureUnit.CELSIUS) {
        this
    } else {
        (this * 9.0 / 5.0) + 32.0
    }
}

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(
        val data: WeatherDashboardData,
        val isRefreshing: Boolean = false
    ) : WeatherUiState
    data class CityNotFound(
        val searchedQuery: String
    ) : WeatherUiState
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : WeatherUiState
}
