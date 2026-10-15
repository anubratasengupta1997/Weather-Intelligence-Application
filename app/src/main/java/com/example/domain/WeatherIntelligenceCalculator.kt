package com.example.domain

import com.example.domain.model.ActivitySuitabilityLevel
import com.example.domain.model.CurrentWeather
import com.example.domain.model.DailyForecast
import com.example.domain.model.OutdoorSuitability
import com.example.domain.model.PlanningIntelligence
import java.util.Locale

object WeatherIntelligenceCalculator {

    fun computePlanningIntelligence(
        current: CurrentWeather,
        dailyList: List<DailyForecast>
    ): PlanningIntelligence {
        val todayForecast = dailyList.firstOrNull()
        val precipToday = todayForecast?.precipitationSumMm ?: current.precipitationMm
        val windSpeed = current.windSpeedKmh
        val temp = current.temperatureC
        val uv = todayForecast?.uvIndexMax ?: current.uvIndex

        // 1. Umbrella Recommendation
        val umbrellaNeeded = precipToday > 0.5 || current.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)
        val umbrellaAdvice = when {
            precipToday > 5.0 -> String.format(Locale.US, "Heavy rain expected (%.1f mm). Carry a sturdy umbrella and wear waterproof gear.", precipToday)
            precipToday > 0.5 -> String.format(Locale.US, "Rain showers likely (%.1f mm). Keep an umbrella handy.", precipToday)
            current.weatherCode in listOf(51, 53, 55, 61) -> "Passing drizzle expected. A compact umbrella is recommended."
            else -> "Dry skies expected today. No umbrella needed."
        }

        // 2. Outdoor Suitability Score (0 - 100)
        var score = 100

        // Deduct for precipitation
        if (precipToday > 5.0) score -= 45
        else if (precipToday > 1.0) score -= 30
        else if (precipToday > 0.1) score -= 15

        // Deduct for extreme temperature
        if (temp < 0) score -= 40
        else if (temp < 10) score -= 20
        else if (temp > 35) score -= 35
        else if (temp > 30) score -= 15

        // Deduct for wind
        if (windSpeed > 40) score -= 30
        else if (windSpeed > 25) score -= 15

        // Deduct for severe storms
        if (current.weatherCode in listOf(95, 96, 99)) score -= 50

        score = score.coerceIn(10, 100)

        val suitabilityLevel = when {
            score >= 80 -> ActivitySuitabilityLevel.EXCELLENT
            score >= 65 -> ActivitySuitabilityLevel.GOOD
            score >= 45 -> ActivitySuitabilityLevel.MODERATE
            else -> ActivitySuitabilityLevel.POOR
        }

        val outdoorHeadline = when (suitabilityLevel) {
            ActivitySuitabilityLevel.EXCELLENT -> "Clear sky — ideal for outdoor activities"
            ActivitySuitabilityLevel.GOOD -> "Favorable conditions for walking & light sports"
            ActivitySuitabilityLevel.MODERATE -> "Moderate conditions — check forecasts before heading out"
            ActivitySuitabilityLevel.POOR -> "Unfavorable weather — indoor activities recommended"
        }

        val outdoorRationale = buildString {
            when {
                temp in 18.0..25.0 && precipToday == 0.0 && windSpeed < 20.0 ->
                    append("Pleasant mild temperatures with calm breeze. Perfect for jogging, cycling, or patio dining.")
                precipToday > 2.0 ->
                    append("Rainfall will make trails and roads slippery. Outdoor recreation may be disrupted.")
                temp < 5.0 ->
                    append("Brisk cold temperatures. If exercising outside, dress warmly in thermal layers.")
                temp > 30.0 ->
                    append("High heat index. Avoid peak afternoon exertion and stay well-hydrated.")
                windSpeed > 30.0 ->
                    append("Gusty winds may affect cycling and outdoor games.")
                else ->
                    append("General weather is stable for daily errands and casual recreation.")
            }
        }

        // 3. Travel & Commute Advisory
        val travelAdvisory = when {
            current.weatherCode in listOf(95, 96, 99) -> "Severe thunderstorm alert: allow extra transit time and drive cautiously."
            precipToday > 5.0 -> "Wet road conditions: expect potential transit delays and lower driving speeds."
            current.weatherCode in listOf(45, 48) -> "Dense fog: reduced visibility on roadways. Keep headlights on low beam."
            windSpeed > 35.0 -> "High crosswinds on highways and bridges. Grip the steering wheel firmly."
            else -> "Smooth travel conditions. Clear roads and standard transit schedules."
        }

        // 4. Wind Advisory
        val windAdvisory = when {
            windSpeed >= 40.0 -> String.format(Locale.US, "Gale warning (%.0f km/h). Secure outdoor furniture and loose objects.", windSpeed)
            windSpeed >= 25.0 -> String.format(Locale.US, "Brisk breeze (%.0f km/h). Noticeable wind resistance when cycling.", windSpeed)
            else -> null
        }

        // 5. Clothing Advisory
        val clothingAdvisory = when {
            temp < 0.0 -> "Heavy winter coat, thermal innerwear, gloves, and a warm hat."
            temp < 10.0 -> "Warm coat, fleece or sweater, and windbreaker."
            temp < 18.0 -> "Light jacket, denim jacket, or long-sleeve layered top."
            temp < 25.0 -> "Comfortable shirt, jeans, or casual activewear."
            else -> "Breathable cotton/linen clothes, shorts, and light footwear."
        }

        // 6. UV Advisory
        val uvAdvisory = when {
            uv >= 8.0 -> String.format(Locale.US, "Very High UV (%.1f). Sun protection required: SPF 50+, wide-brim hat, shade between 11 AM - 4 PM.", uv)
            uv >= 6.0 -> String.format(Locale.US, "High UV (%.1f). Apply SPF 30+ sunscreen and wear UV-blocking sunglasses.", uv)
            uv >= 3.0 -> String.format(Locale.US, "Moderate UV (%.1f). Wear sunglasses and sunscreen for prolonged sun exposure.", uv)
            else -> null
        }

        return PlanningIntelligence(
            outdoorSuitability = OutdoorSuitability(
                score = score,
                level = suitabilityLevel,
                headline = outdoorHeadline,
                rationale = outdoorRationale
            ),
            umbrellaNeeded = umbrellaNeeded,
            umbrellaAdvice = umbrellaAdvice,
            travelAdvisory = travelAdvisory,
            windAdvisory = windAdvisory,
            clothingAdvisory = clothingAdvisory,
            uvAdvisory = uvAdvisory
        )
    }
}
