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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActivitySuitabilityLevel
import com.example.domain.model.PlanningIntelligence

@Composable
fun PlanningRecommendationsView(
    planning: PlanningIntelligence,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Activity & Travel Intelligence",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1. Outdoor Activity Index Hero Card
        val suitability = planning.outdoorSuitability
        val levelColor = when (suitability.level) {
            ActivitySuitabilityLevel.EXCELLENT -> Color(0xFF10B981)
            ActivitySuitabilityLevel.GOOD -> Color(0xFF0EA5E9)
            ActivitySuitabilityLevel.MODERATE -> Color(0xFFF59E0B)
            ActivitySuitabilityLevel.POOR -> Color(0xFFEF4444)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("outdoor_activity_card"),
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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(levelColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Park,
                                contentDescription = null,
                                tint = levelColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Outdoor Activity Score",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = suitability.level.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = levelColor
                            )
                        }
                    }

                    // Score Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = levelColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${suitability.score}/100",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { suitability.score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = levelColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = suitability.headline,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = suitability.rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Actionable Recommendation Cards Grid / List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Umbrella Recommendation
            RecommendationItemCard(
                title = if (planning.umbrellaNeeded) "Umbrella Required" else "No Umbrella Needed",
                description = planning.umbrellaAdvice,
                icon = Icons.Default.Umbrella,
                accentColor = if (planning.umbrellaNeeded) Color(0xFF0284C7) else Color(0xFF10B981),
                testTag = "recommendation_umbrella"
            )

            // Travel & Commute Advisory
            RecommendationItemCard(
                title = "Travel & Commute Advisory",
                description = planning.travelAdvisory,
                icon = Icons.Default.DirectionsCar,
                accentColor = Color(0xFF6366F1),
                testTag = "recommendation_travel"
            )

            // Wardrobe Recommendation
            RecommendationItemCard(
                title = "Wardrobe Recommendation",
                description = planning.clothingAdvisory,
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFFF59E0B),
                testTag = "recommendation_clothing"
            )

            // Optional Wind Advisory
            planning.windAdvisory?.let { windAdv ->
                RecommendationItemCard(
                    title = "Wind & Gust Alert",
                    description = windAdv,
                    icon = Icons.Default.Air,
                    accentColor = Color(0xFFEF4444),
                    testTag = "recommendation_wind"
                )
            }

            // Optional UV Advisory
            planning.uvAdvisory?.let { uvAdv ->
                RecommendationItemCard(
                    title = "Sun & UV Protection",
                    description = uvAdv,
                    icon = Icons.Default.WbSunny,
                    accentColor = Color(0xFFEC4899),
                    testTag = "recommendation_uv"
                )
            }
        }
    }
}

@Composable
fun RecommendationItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
