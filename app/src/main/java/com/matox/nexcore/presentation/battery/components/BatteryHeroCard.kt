package com.matox.nexcore.presentation.battery.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.BatteryReading
import com.matox.nexcore.domain.model.BatteryStatus
import com.matox.nexcore.domain.model.PlugType
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricGreen
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.MetricSoftRed
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Hero card for the Battery Monitor screen.
 *
 *  - Big 240 dp animated ring with a sweep whose colour follows the
 *    level thresholds (≥85 red, 60–84 orange, 40–59 cyan, <40 green).
 *  - Inside the ring: huge level (40 sp Bold), charging-status pill
 *    (Fast Charging / Charging / Discharging / Full / Plugged in),
 *    and a battery-health pill (Excellent / Good / Fair / Poor).
 *  - Glass surface: 24 dp rounded corners, 1 dp border, 8 dp shadow
 *    with green ambient glow.
 */
@Composable
fun BatteryHeroCard(
    reading: BatteryReading,
    healthLabel: String,
    modifier: Modifier = Modifier,
) {
    val pct = reading.levelPercent.coerceIn(0, 100)
    val animatedPct by animateFloatAsState(
        targetValue = pct.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "battery-hero-pct",
    )
    val sweepColor = sweepColorFor(pct)

    val (statusLabel, statusBg, statusFg) = statusPill(reading)
    val (healthBg, healthFg) = healthPill(healthLabel)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = NexCoreGreen.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.92f)),
                ),
            )
            .border(1.dp, CardStroke, RoundedCornerShape(24.dp)),
    ) {
        // Glass highlight.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header row: status pill on the right.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery in use",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "${reading.technology} · ${plugLabel(reading.plugType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                PillBadge(
                    label = statusLabel,
                    bg = statusBg,
                    fg = statusFg,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ring + interior.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Soft outer pulse ring (larger than sweep).
                val infinite = rememberInfiniteTransition(label = "battery-hero-pulse")
                val pulseScale by infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "battery-hero-pulse-scale",
                )
                val pulseAlpha by infinite.animateFloat(
                    initialValue = 0.20f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "battery-hero-pulse-alpha",
                )
                Box(
                    modifier = Modifier
                        .size(248.dp * pulseScale),
                ) {
                    Canvas(modifier = Modifier.size(248.dp)) {
                        val strokePx = 6f
                        drawCircle(
                            color = sweepColor.copy(alpha = pulseAlpha),
                            radius = (size.minDimension / 2f) - strokePx,
                        )
                    }
                }

                BatteryRing(
                    percent = animatedPct,
                    color = sweepColor,
                    sizeDp = 240,
                    strokeWidthDp = 18,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 56.sp,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = ofLabel(reading),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PillBadge(
                        label = "$healthLabel health",
                        bg = healthBg,
                        fg = healthFg,
                    )
                }
            }
        }
    }
}

@Composable
private fun PillBadge(label: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = fg,
        )
    }
}

@Composable
private fun BatteryRing(
    percent: Float,
    color: Color,
    sizeDp: Int,
    strokeWidthDp: Int,
) {
    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val strokePx = strokeWidthDp.dp.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokePx)

        // Track.
        drawArc(
            color = Color(0xFF22304A),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )

        val sweep = (percent.coerceIn(0f, 100f) / 100f) * 360f
        if (sweep > 0f) {
            // Outer soft glow stroke.
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = 0.35f),
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.0f),
                    ),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx + 8f),
            )
            // Crisp sweep.
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(color, color.copy(alpha = 0.7f), color),
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

private fun sweepColorFor(pct: Int): Color = when {
    pct >= 85 -> MetricRed
    pct >= 60 -> MetricOrange
    pct >= 40 -> MetricCyan
    else -> NexCoreGreen
}

private fun statusPill(reading: BatteryReading): Triple<String, Color, Color> {
    val label: String
    val bg: Color
    val fg: Color
    when {
        reading.status == BatteryStatus.FULL -> {
            label = "Full"
            bg = NexCoreGreen.copy(alpha = 0.18f)
            fg = NexCoreGreen
        }
        reading.isFastCharging -> {
            label = "Fast Charging"
            bg = MetricCyan.copy(alpha = 0.20f)
            fg = MetricCyan
        }
        reading.status == BatteryStatus.CHARGING -> {
            label = "Charging"
            bg = MetricBlue.copy(alpha = 0.20f)
            fg = MetricBlue
        }
        reading.status == BatteryStatus.NOT_CHARGING && reading.plugType != PlugType.NONE -> {
            label = "Plugged in"
            bg = MetricBlue.copy(alpha = 0.18f)
            fg = MetricBlue
        }
        reading.status == BatteryStatus.DISCHARGING -> {
            label = "Discharging"
            bg = MetricOrange.copy(alpha = 0.18f)
            fg = MetricOrange
        }
        else -> {
            label = "Idle"
            bg = MetricBlue.copy(alpha = 0.16f)
            fg = MetricBlue
        }
    }
    return Triple(label, bg, fg)
}

private fun healthPill(label: String): Pair<Color, Color> = when (label) {
    "Excellent" -> NexCoreGreen.copy(alpha = 0.18f) to NexCoreGreen
    "Good" -> MetricGreen.copy(alpha = 0.18f) to MetricGreen
    "Fair" -> MetricOrange.copy(alpha = 0.18f) to MetricOrange
    "Poor" -> MetricSoftRed.copy(alpha = 0.18f) to MetricSoftRed
    else -> MetricBlue.copy(alpha = 0.18f) to MetricBlue
}

private fun plugLabel(plug: PlugType): String = when (plug) {
    PlugType.NONE -> "On battery"
    PlugType.AC -> "AC"
    PlugType.USB -> "USB"
    PlugType.WIRELESS -> "Wireless"
}

private fun ofLabel(reading: BatteryReading): String {
    val prefix = when (reading.status) {
        BatteryStatus.CHARGING -> if (reading.isFastCharging) "Fast charging · up to" else "Charging at"
        BatteryStatus.DISCHARGING -> "Drawing"
        BatteryStatus.FULL -> "Held at"
        BatteryStatus.NOT_CHARGING -> "Available at"
        BatteryStatus.UNKNOWN -> "At"
    }
    val current = reading.currentNowMa
    return if (current != 0) {
        val sign = if (current > 0) "+" else ""
        "$prefix ${sign}${current} mA"
    } else {
        "$prefix idle"
    }
}
