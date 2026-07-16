package com.matox.nexcore.presentation.sensors.components

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
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PowerInput
import androidx.compose.material.icons.outlined.SocialDistance
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.WaterDrop
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
import com.matox.nexcore.domain.model.SensorIcon
import com.matox.nexcore.domain.model.SensorReading
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricPink
import com.matox.nexcore.ui.theme.MetricTeal
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Full sensor list card — every sensor the OS reports, one row
 * each. Each row carries:
 *  - Icon tile (left), tinted by the sensor's semantic accent.
 *  - Sensor name + vendor secondary line.
 *  - Current value (formatted against the sensor's unit).
 *  - "Live" dot / "Idle" pill on the right.
 *
 * Renders an empty-state hint when the OS reports no sensors
 * at all (rare in practice — emulators may do this).
 */
@Composable
fun SensorListCard(
    readings: List<SensorReading>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricBlue.copy(alpha = 0.14f),
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
        // Glass highlight strip.
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MetricBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Memory,
                        contentDescription = null,
                        tint = MetricBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "All Sensors",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "${readings.size} reported by OS",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No sensors reported by the OS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                return
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                readings.forEach { reading ->
                    SensorRow(reading = reading)
                }
            }
        }
    }
}

@Composable
private fun SensorRow(reading: SensorReading) {
    val accent = accentFor(reading.sensorType.iconKey)
    val icon = iconFor(reading.sensorType.iconKey)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon tile.
        Box(
            modifier = Modifier
                .size(36.dp)
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
        Spacer(modifier = Modifier.width(10.dp))

        // Name + vendor.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reading.sensorType.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            val sub = listOfNotNull(
                reading.name.takeIf { it.isNotBlank() && it != reading.sensorType.label },
                reading.vendor.takeIf { it.isNotBlank() && it != "—" },
            ).joinToString(" · ")
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Current value (channel-aware).
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatValue(reading),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = TextPrimary,
                maxLines = 1,
            )
            if (reading.unit.isNotBlank()) {
                Text(
                    text = reading.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Live / Idle pill.
        LivePill(active = reading.active, accent = accent)
    }
}

@Composable
private fun LivePill(active: Boolean, accent: Color) {
    val bg = if (active) accent.copy(alpha = 0.20f) else Color(0xFF22304A)
    val fg = if (active) accent else TextSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (active) accent else TextSecondary),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (active) "Live" else "Idle",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = fg,
        )
    }
}

private fun accentFor(key: SensorIcon): Color = when (key) {
    SensorIcon.ACCEL -> MetricTeal
    SensorIcon.GYRO -> MetricOrange
    SensorIcon.MAGNETO -> MetricPink
    SensorIcon.GRAVITY -> MetricViolet
    SensorIcon.LIN_ACCEL -> MetricCyan
    SensorIcon.ROTATION -> MetricViolet
    SensorIcon.STEPS -> NexCoreGreen
    SensorIcon.MOTION -> NexCoreGreen
    SensorIcon.PROXIMITY -> MetricBlue
    SensorIcon.LIGHT -> MetricOrange
    SensorIcon.PRESSURE -> MetricBlue
    SensorIcon.THERMO -> MetricOrange
    SensorIcon.HUMIDITY -> MetricCyan
    SensorIcon.HEART -> MetricPink
}

private fun iconFor(key: SensorIcon): ImageVector = when (key) {
    SensorIcon.ACCEL -> Icons.Outlined.SwapVert
    SensorIcon.GYRO -> Icons.Outlined.Explore
    SensorIcon.MAGNETO -> Icons.Outlined.Explore
    SensorIcon.GRAVITY -> Icons.Outlined.PowerInput
    SensorIcon.LIN_ACCEL -> Icons.Outlined.SwapVert
    SensorIcon.ROTATION -> Icons.AutoMirrored.Outlined.RotateRight
    SensorIcon.STEPS -> Icons.AutoMirrored.Outlined.DirectionsWalk
    SensorIcon.MOTION -> Icons.AutoMirrored.Outlined.DirectionsWalk
    SensorIcon.PROXIMITY -> Icons.Outlined.SocialDistance
    SensorIcon.LIGHT -> Icons.Outlined.Lightbulb
    SensorIcon.PRESSURE -> Icons.Outlined.Compress
    SensorIcon.THERMO -> Icons.Outlined.DeviceThermostat
    SensorIcon.HUMIDITY -> Icons.Outlined.WaterDrop
    SensorIcon.HEART -> Icons.Outlined.Favorite
}

// ---- Value formatting -------------------------------------------------

private fun formatValue(reading: SensorReading): String {
    val values = reading.values
    if (values.isEmpty()) return "—"
    return when (values.size) {
        1 -> formatOne(values[0])
        // 3-axis (and 4-axis rotation vector — drop w for display)
        else -> values.take(3).joinToString(", ") { "%.2f".format(it) }
    }
}

private fun formatOne(v: Float): String {
    return when {
        kotlin.math.abs(v) >= 1000f -> "%.0f".format(v)
        kotlin.math.abs(v) >= 100f -> "%.1f".format(v)
        else -> "%.2f".format(v)
    }
}
