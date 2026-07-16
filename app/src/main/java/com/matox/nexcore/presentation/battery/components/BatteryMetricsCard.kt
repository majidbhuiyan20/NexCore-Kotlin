package com.matox.nexcore.presentation.battery.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Battery5Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * Battery Metric Tiles — 2×2 grid of four premium cards.
 *
 *  ┌─────────────────────┬──────────────────────┐
 *  │ Temperature         │ Voltage              │
 *  │ 33.6°C              │ 4.32 V               │
 *  │ [thermometer bar]   │ [voltage bar]        │
 *  ├─────────────────────┼──────────────────────┤
 *  │ Current             │ Capacity             │
 *  │ +1250 mA            │ 5000 mAh             │
 *  └─────────────────────┴──────────────────────┘
 *
 * Each card: glass surface, accent icon chip top-left, big value,
 * unit, mini progress bar where it makes sense (temperature/voltage).
 */
@Composable
fun BatteryMetricsCard(
    temperatureC: Float,
    voltageMv: Int,
    currentMa: Int,
    capacityMah: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricOrange.copy(alpha = 0.15f),
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
        // Glass highlight.
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
                text = "Live Battery Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Temperature · Voltage · Current · Capacity",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCell(
                    label = "Temperature",
                    value = if (temperatureC > 0f) "%.1f".format(temperatureC) else "—",
                    unit = "°C",
                    accent = MetricOrange,
                    icon = Icons.Outlined.Thermostat,
                    progress = (temperatureC.coerceIn(0f, 50f) / 50f),
                    modifier = Modifier.weight(1f),
                )
                MetricCell(
                    label = "Voltage",
                    value = if (voltageMv > 0) "%.2f".format(voltageMv / 1000f) else "—",
                    unit = "V",
                    accent = MetricViolet,
                    icon = Icons.Outlined.Bolt,
                    progress = ((voltageMv / 1000f).coerceIn(3.0f, 4.5f) - 3.0f) / 1.5f,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCell(
                    label = "Current",
                    value = if (currentMa != 0) {
                        val sign = if (currentMa > 0) "+" else ""
                        "$sign$currentMa"
                    } else "0",
                    unit = "mA",
                    accent = MetricBlue,
                    icon = Icons.Outlined.Power,
                    progress = null,
                    modifier = Modifier.weight(1f),
                )
                MetricCell(
                    label = "Capacity",
                    value = if (capacityMah > 0) "$capacityMah" else "—",
                    unit = "mAh",
                    accent = MetricCyan,
                    icon = Icons.Outlined.Battery5Bar,
                    progress = null,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * One metric tile — icon chip top-left, value, unit, optional mini bar.
 */
@Composable
private fun MetricCell(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    icon: ImageVector,
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
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
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (progress != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TrackGray),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(accent.copy(alpha = 0.6f), accent),
                            ),
                        ),
                )
            }
        }
    }
}

// Keep the brand color referenced (NexCoreGreen is the umbrella accent
// the card sits under).
@Suppress("unused")
private val _keep: Color = NexCoreGreen
