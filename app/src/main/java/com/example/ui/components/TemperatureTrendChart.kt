package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyForecast
import com.example.ui.TemperatureUnit
import com.example.ui.formatTemp
import com.example.ui.toUnitValue
import kotlin.math.roundToInt

@Composable
fun TemperatureTrendChart(
    forecasts: List<DailyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (forecasts.size < 2) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val highColor = Color(0xFFFF6B6B)
    val lowColor = Color(0xFF38BDF8)
    val guideColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurface.hashCode()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("temperature_trend_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            // Header & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Temperature Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "7-day high & low trend curve",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(highColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "High",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(lowColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Low",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Tooltip if day tapped
            selectedIndex?.let { idx ->
                val f = forecasts.getOrNull(idx)
                if (f != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${f.dayOfWeek}: ${f.conditionDescription}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "High: ${f.maxTempC.formatTemp(unit)}  •  Low: ${f.minTempC.formatTemp(unit)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .pointerInput(forecasts) {
                            detectTapGestures { offset ->
                                val count = forecasts.size
                                val stepX = size.width / count
                                val clickedIdx = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                selectedIndex = clickedIdx
                            }
                        }
                        .testTag("temperature_line_canvas")
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val topPadding = 32.dp.toPx()
                    val bottomPadding = 38.dp.toPx()
                    val chartHeight = canvasHeight - topPadding - bottomPadding

                    val highTemps = forecasts.map { it.maxTempC.toUnitValue(unit) }
                    val lowTemps = forecasts.map { it.minTempC.toUnitValue(unit) }

                    val allTemps = highTemps + lowTemps
                    val minT = (allTemps.minOrNull() ?: 0.0) - 2.0
                    val maxT = (allTemps.maxOrNull() ?: 30.0) + 2.0
                    val tempRange = (maxT - minT).coerceAtLeast(1.0)

                    val count = forecasts.size
                    val stepX = canvasWidth / count

                    fun getY(temp: Double): Float {
                        val fraction = ((temp - minT) / tempRange).toFloat()
                        return (canvasHeight - bottomPadding) - (fraction * chartHeight)
                    }

                    fun getX(index: Int): Float {
                        return (index * stepX) + (stepX / 2f)
                    }

                    // Draw Horizontal Grid Guide Lines
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val y = topPadding + (chartHeight * i / gridSteps)
                        drawLine(
                            color = guideColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Build High & Low Paths
                    val highPath = Path()
                    val highAreaPath = Path()
                    val lowPath = Path()
                    val lowAreaPath = Path()

                    val highPoints = mutableListOf<Offset>()
                    val lowPoints = mutableListOf<Offset>()

                    forecasts.indices.forEach { i ->
                        val x = getX(i)
                        val yHigh = getY(highTemps[i])
                        val yLow = getY(lowTemps[i])

                        highPoints.add(Offset(x, yHigh))
                        lowPoints.add(Offset(x, yLow))

                        if (i == 0) {
                            highPath.moveTo(x, yHigh)
                            highAreaPath.moveTo(x, canvasHeight - bottomPadding)
                            highAreaPath.lineTo(x, yHigh)

                            lowPath.moveTo(x, yLow)
                            lowAreaPath.moveTo(x, canvasHeight - bottomPadding)
                            lowAreaPath.lineTo(x, yLow)
                        } else {
                            val prevX = getX(i - 1)
                            val prevYHigh = getY(highTemps[i - 1])
                            val prevYLow = getY(lowTemps[i - 1])

                            val cX1 = prevX + (x - prevX) / 2f
                            val cY1High = prevYHigh
                            val cX2 = prevX + (x - prevX) / 2f
                            val cY2High = yHigh
                            highPath.cubicTo(cX1, cY1High, cX2, cY2High, x, yHigh)
                            highAreaPath.cubicTo(cX1, cY1High, cX2, cY2High, x, yHigh)

                            val cY1Low = prevYLow
                            val cY2Low = yLow
                            lowPath.cubicTo(cX1, cY1Low, cX2, cY2Low, x, yLow)
                            lowAreaPath.cubicTo(cX1, cY1Low, cX2, cY2Low, x, yLow)
                        }
                    }

                    val lastX = getX(forecasts.lastIndex)
                    highAreaPath.lineTo(lastX, canvasHeight - bottomPadding)
                    highAreaPath.close()

                    lowAreaPath.lineTo(lastX, canvasHeight - bottomPadding)
                    lowAreaPath.close()

                    // Draw High Area Gradient
                    drawPath(
                        path = highAreaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(highColor.copy(alpha = 0.2f), highColor.copy(alpha = 0.02f)),
                            startY = topPadding,
                            endY = canvasHeight - bottomPadding
                        )
                    )

                    // Draw Low Area Gradient
                    drawPath(
                        path = lowAreaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lowColor.copy(alpha = 0.15f), lowColor.copy(alpha = 0.01f)),
                            startY = topPadding,
                            endY = canvasHeight - bottomPadding
                        )
                    )

                    // Draw Curves
                    drawPath(
                        path = highPath,
                        color = highColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = lowPath,
                        color = lowColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Selected Column Guide
                    selectedIndex?.let { idx ->
                        val sx = getX(idx)
                        drawLine(
                            color = highColor.copy(alpha = 0.5f),
                            start = Offset(sx, topPadding),
                            end = Offset(sx, canvasHeight - bottomPadding),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // Draw Nodes and Labels
                    val textPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 28f
                        textAlign = Paint.Align.CENTER
                        color = textColor
                        isFakeBoldText = true
                    }

                    val subTextPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                        color = android.graphics.Color.GRAY
                    }

                    highPoints.forEachIndexed { i, pt ->
                        // Node circle
                        val isHighlighted = selectedIndex == i
                        drawCircle(
                            color = Color.White,
                            radius = if (isHighlighted) 7.dp.toPx() else 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = highColor,
                            radius = if (isHighlighted) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = pt
                        )

                        // High Temp Text Label
                        val highLabel = "${highTemps[i].roundToInt()}°"
                        drawContext.canvas.nativeCanvas.drawText(
                            highLabel,
                            pt.x,
                            pt.y - 12f,
                            textPaint
                        )
                    }

                    lowPoints.forEachIndexed { i, pt ->
                        val isHighlighted = selectedIndex == i
                        drawCircle(
                            color = Color.White,
                            radius = if (isHighlighted) 7.dp.toPx() else 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = lowColor,
                            radius = if (isHighlighted) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = pt
                        )

                        // Low Temp Text Label
                        val lowLabel = "${lowTemps[i].roundToInt()}°"
                        drawContext.canvas.nativeCanvas.drawText(
                            lowLabel,
                            pt.x,
                            pt.y + 28f,
                            subTextPaint
                        )

                        // Day of week bottom label
                        val dayLabel = forecasts[i].dayOfWeek.take(3)
                        drawContext.canvas.nativeCanvas.drawText(
                            dayLabel,
                            pt.x,
                            canvasHeight - 6.dp.toPx(),
                            subTextPaint
                        )
                    }
                }
            }
        }
    }
}
