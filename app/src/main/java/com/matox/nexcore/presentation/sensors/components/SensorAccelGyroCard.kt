package com.matox.nexcore.presentation.sensors.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.SensorReading
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricTeal
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Sensor Card — Accelerometer + Gyroscope channel readout.
 *
 * Side-by-side:
 *  ┌────────────────────────┬────────────────────────┐
 *  │ Accelerometer          │ Gyroscope              │
 *  │ X  +0.12 m/s²          │ X  +0.00 rad/s         │
 *  │ Y  +9.81 m/s²          │ Y  +0.01 rad/s         │
 *  │ Z  -0.34 m/s²          │ Z  +0.00 rad/s         │
 *  │ Vendor: bosch          │ Vendor: invensense     │
 *  └────────────────────────┴────────────────────────┘
 *
 * Each panel is a tinted glass block with three "channel" rows
 * (X, Y, Z) showing the signed value and a tiny vertical bar
 * giving relative magnitude vs. the channel's typical range.
 */
@Composable
fun SensorAccelGyroCard(
    accelerometer: SensorReading?,
    gyroscope: SensorReading?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricTeal.copy(alpha = 0.15f),
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
                        .background(MetricTeal.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "3D",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MetricTeal,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Motion Channels",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "X · Y · Z live values, signed",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChannelPanel(
                    label = "Accelerometer",
                    reading = accelerometer,
                    accent = MetricTeal,
                    unit = "m/s²",
                    valueRange = 20f,
                    modifier = Modifier.weight(1f),
                )
                ChannelPanel(
                    label = "Gyroscope",
                    reading = gyroscope,
                    accent = MetricOrange,
                    unit = "rad/s",
                    valueRange = 5f,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ChannelPanel(
    label: String,
    reading: SensorReading?,
    accent: Color,
    unit: String,
    valueRange: Float,
    modifier: Modifier = Modifier,
) {
    val channels = reading?.values?.toList().orEmpty()
    val x = channels.getOrNull(0) ?: 0f
    val y = channels.getOrNull(1) ?: 0f
    val z = channels.getOrNull(2) ?: 0f

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
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (reading?.active == true) accent else TextSecondary),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = accent,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ChannelAxisRow(axis = "X", value = x, unit = unit, accent = accent, range = valueRange)
        ChannelAxisRow(axis = "Y", value = y, unit = unit, accent = accent, range = valueRange)
        ChannelAxisRow(axis = "Z", value = z, unit = unit, accent = accent, range = valueRange)
        if (reading != null && reading.vendor.isNotBlank() && reading.vendor != "—") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vendor · ${reading.vendor}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        if (reading == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Not present",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun ChannelAxisRow(
    axis: String,
    value: Float,
    unit: String,
    accent: Color,
    range: Float,
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 180, easing = LinearEasing),
        label = "channel-$axis",
    )
    val pct = ((kotlin.math.abs(animatedValue) / range) * 100f).coerceIn(0f, 100f)
    val sign = when {
        animatedValue > 0.005f -> "+"
        animatedValue < -0.005f -> "−"
        else -> " "
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = axis,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            ),
            color = accent,
            modifier = Modifier.width(12.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF22304A)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(accent.copy(alpha = 0.55f), accent),
                        ),
                    ),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$sign${formatChannel(animatedValue)}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
            color = TextPrimary,
            modifier = Modifier.width(58.dp),
        )
    }
}

private fun formatChannel(v: Float): String {
    if (kotlin.math.abs(v) < 0.005f) return " 0.00 "
    return "%.2f".format(kotlin.math.abs(v))
}
