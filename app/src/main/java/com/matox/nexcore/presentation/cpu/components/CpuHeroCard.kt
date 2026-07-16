package com.matox.nexcore.presentation.cpu.components

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
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Hero card for the CPU Monitor screen — mirrors `BatteryHeroCard`'s
 * 240 dp ring + pulse + status-pill layout but reskinned with
 * MetricOrange ambient glow + load-based sweep colour.
 *
 * Sweep colour rules:
 *  - < 30 % → `NexCoreGreen` (idle)
 *  - 30..69 % → `MetricCyan` (light / normal)
 *  - 70..89 % → `MetricOrange` (busy)
 *  - ≥ 90 % → `MetricRed` (overloaded)
 */
@Composable
fun CpuHeroCard(
    overallPercent: Int,
    coreCount: Int,
    modifier: Modifier = Modifier,
) {
    val pct = overallPercent.coerceIn(0, 100)
    val animatedPct by animateFloatAsState(
        targetValue = pct.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "cpu-hero-pct",
    )
    val sweepColor = sweepColorFor(pct)
    val label = loadLabelFor(pct)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MetricOrange.copy(alpha = 0.18f),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CPU usage",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "$coreCount cores · polled every 1 second",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                PillBadge(label = label, bg = sweepColor.copy(alpha = 0.18f), fg = sweepColor)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                val infinite = rememberInfiniteTransition(label = "cpu-hero-pulse")
                val pulseScale by infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "cpu-hero-pulse-scale",
                )
                val pulseAlpha by infinite.animateFloat(
                    initialValue = 0.20f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "cpu-hero-pulse-alpha",
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

                CpuRing(
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
                        text = "device-wide load",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PillBadge(
                        label = "$coreCount-core SoC",
                        bg = MetricOrange.copy(alpha = 0.18f),
                        fg = MetricOrange,
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
private fun CpuRing(
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
    pct >= 90 -> MetricRed
    pct >= 70 -> MetricOrange
    pct >= 30 -> MetricCyan
    else -> NexCoreGreen
}

private fun loadLabelFor(pct: Int): String = when {
    pct >= 90 -> "Overloaded"
    pct >= 70 -> "Heavy load"
    pct >= 30 -> "Busy"
    else -> "Light load"
}
