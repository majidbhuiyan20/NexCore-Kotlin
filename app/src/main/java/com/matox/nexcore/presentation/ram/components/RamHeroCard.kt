package com.matox.nexcore.presentation.ram.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Warning

/**
 * Hero card with an animated arc ring showing used / total RAM,
 * the percentage number in the middle, and a small "Used / Total /
 * Available" stat strip below the ring.
 *
 * Layout:
 *
 *   ┌─────────────────────────────────────┐
 *   │ [icon] Memory         [pill: %]     │
 *   │                                     │
 *   │           ╭───────╮                 │
 *   │           │  67%  │   ring          │
 *   │           │ 4.2GB │                 │
 *   │           │ of 8GB│                 │
 *   │           ╰───────╯                 │
 *   │                                     │
 *   │  Used 4.2GB  ·  Free 3.8GB  ·  …   │
 *   └─────────────────────────────────────┘
 */
@Composable
fun RamHeroCard(
    snapshot: RamSnapshot,
    modifier: Modifier = Modifier,
) {
    val accent = MetricAccent.BLUE
    val accentColor = accent.toColor()
    val ringColor = if (snapshot.percent >= 85) MetricRed else accentColor

    // Animate the sweep angle so the ring glides between polls instead
    // of snapping. 600 ms feels right — long enough to be smooth,
    // short enough that the next poll arrives mid-flight.
    val animatedPct by animateFloatAsState(
        targetValue = snapshot.percent.toFloat(),
        animationSpec = tween(durationMillis = 600),
        label = "ram-pct",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        // Header row — icon chip + "Memory" title + percent pill.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconChip(
                icon = Icons.Outlined.Memory,
                accent = accentColor,
                size = 40.dp,
                iconSize = 22.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Memory in use",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = if (snapshot.lowMemory) "Low memory" else "Live device telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (snapshot.lowMemory) MetricRed else TextSecondary,
                )
            }
            // Pill showing the current percentage.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ringColor.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "${snapshot.percent}%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = ringColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The animated ring + center labels.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedRamRing(
                percent = animatedPct,
                color = ringColor,
                sizeDp = 220,
                strokeWidthDp = 16,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${snapshot.percent}%",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = "${formatGb(snapshot.usedGb)} / ${formatGb(snapshot.totalGb)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Three-tile stat strip.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HeroStat(
                label = "Used",
                value = formatGb(snapshot.usedGb),
                accent = accentColor,
                modifier = Modifier.weight(1f),
            )
            HeroStat(
                label = "Available",
                value = formatGb(snapshot.availableGb),
                accent = MetricCyan,
                modifier = Modifier.weight(1f),
            )
            HeroStat(
                label = if (snapshot.lowMemory) "Threshold" else "Threshold",
                value = if (snapshot.thresholdGb > 0f) formatGb(snapshot.thresholdGb) else "—",
                accent = if (snapshot.lowMemory) MetricRed else TextSecondary,
                modifier = Modifier.weight(1f),
            )
        }

        if (snapshot.lowMemory) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MetricRed.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconChip(
                    icon = Icons.Outlined.Warning,
                    accent = MetricRed,
                    size = 32.dp,
                    iconSize = 16.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "System is reporting low memory — consider closing apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetricRed,
                )
            }
        }
    }
}

@Composable
private fun AnimatedRamRing(
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

        // Track
        drawArc(
            color = Color(0xFF22304A),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        // Animated sweep — blue → cyan gradient on the brush.
        val sweep = (percent.coerceIn(0f, 100f) / 100f) * 360f
        if (sweep > 0f) {
            // Soft glow under the ring
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = 0.35f),
                        color.copy(alpha = 0.18f),
                        color.copy(alpha = 0.0f),
                    ),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx + 6f),
            )
            // Solid ring on top
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(MetricBlue, MetricCyan, MetricBlue),
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

@Composable
private fun HeroStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = accent,
        )
    }
}

private fun formatGb(v: Float): String {
    if (v <= 0f) return "0.0 GB"
    val rounded = (v * 10f).toInt() / 10f
    return "$rounded GB"
}