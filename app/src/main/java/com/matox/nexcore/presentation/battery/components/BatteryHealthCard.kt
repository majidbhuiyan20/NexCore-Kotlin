package com.matox.nexcore.presentation.battery.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.BatteryHealth
import com.matox.nexcore.domain.model.ChargingPattern
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricGreen
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Battery Health Insights" card — 2×2 grid of health metrics plus a
 * circular health-score badge.
 *
 *  ┌─────────────────┬─────────────────┐
 *  │ Charge Cycles   │ Battery Wear    │
 *  │ ~284            │ 12%             │
 *  ├─────────────────┼─────────────────┤
 *  │ Charging Pattern│ Health Score    │
 *  │ Overnight       │ (ring) 92/100   │
 *  └─────────────────┴─────────────────┘
 *
 * The Health Score cell uses a small 64 dp canvas ring instead of a
 * plain number — mirrors the hero card's design language at half
 * scale so the section reads as a coherent "battery health" story.
 */
@Composable
fun BatteryHealthCard(
    health: BatteryHealth,
    modifier: Modifier = Modifier,
) {
    val scoreColor = scoreColorFor(health.healthScore)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = NexCoreGreen.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.30f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.92f)),
                ),
            )
            .border(1.dp, CardStroke, RoundedCornerShape(22.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Battery Health Insights",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Cycles · Wear · Pattern · Health Score",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HealthCell(
                    label = "Charge Cycles",
                    value = "~${health.cyclesEstimate}",
                    accent = MetricBlue,
                    icon = Icons.Outlined.Loop,
                    modifier = Modifier.weight(1f),
                )
                HealthCell(
                    label = "Battery Wear",
                    value = "${health.wearPercent}%",
                    accent = NexCoreGreen,
                    icon = Icons.Outlined.Build,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HealthCell(
                    label = "Charging Pattern",
                    value = patternLabel(health.chargingPattern),
                    accent = MetricViolet,
                    icon = Icons.Outlined.Bolt,
                    modifier = Modifier.weight(1f),
                )
                HealthScoreCell(
                    score = health.healthScore,
                    label = health.healthLabel,
                    color = scoreColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HealthCell(
    label: String,
    value: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
            color = TextPrimary,
        )
    }
}

@Composable
private fun HealthScoreCell(
    score: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            HealthRing(percent = score.coerceIn(0, 100), color = color, sizeDp = 72.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Health · $label",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = color,
        )
    }
}

@Composable
private fun HealthRing(
    percent: Int,
    color: Color,
    sizeDp: Dp,
) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val strokePx = 8.dp.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokePx)

        drawArc(
            color = Color(0xFF22304A),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )

        val sweep = (percent / 100f) * 360f
        if (sweep > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(color.copy(alpha = 0.7f), color, color.copy(alpha = 0.7f)),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
    }
}

private fun scoreColorFor(score: Int): Color = when {
    score >= 80 -> NexCoreGreen
    score >= 60 -> MetricOrange
    else -> MetricRed
}

private fun patternLabel(pattern: ChargingPattern): String = when (pattern) {
    ChargingPattern.OVERNIGHT -> "Overnight"
    ChargingPattern.TOP_UP -> "Top-up"
    ChargingPattern.FULL_CYCLE -> "Full cycle"
    ChargingPattern.IRREGULAR -> "Irregular"
}

// Suppress unused-color warning.
@Suppress("unused")
private val _keep: Color = MetricGreen
