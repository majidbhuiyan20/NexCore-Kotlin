package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.BatteryDetails
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricGreen
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.NexCoreGreenAccent
import com.matox.nexcore.ui.theme.NexCoreGreenDark
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

@Composable
fun BatteryCard(
    battery: BatteryDetails,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Battery",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            BatteryIllustration(
                percent = battery.percent,
                modifier = Modifier.size(width = 70.dp, height = 38.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${battery.percent}%",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                    )
                }
                if (battery.isCharging) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Charging",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = NexCoreGreen,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(text = "⚡", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MetricOrange.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Thermostat,
                    contentDescription = null,
                    tint = MetricOrange,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Temperature",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${battery.temperatureC}°C",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MetricOrange,
            )
        }
    }
}

/**
 * Canvas-drawn battery silhouette with a green fill that matches
 * [percent] (0..100).
 */
@Composable
private fun BatteryIllustration(percent: Int, modifier: Modifier = Modifier) {
    val fillBrush = Brush.verticalGradient(
        colors = listOf(MetricGreen, NexCoreGreenAccent, NexCoreGreenDark),
    )
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val bodyW = w * 0.86f
            val bodyH = h * 0.84f
            val tipW = w - bodyW - 2f
            val left = 0f
            val top = (h - bodyH) / 2f

            // Body outline
            drawRoundRect(
                color = Color(0xFF22304A),
                topLeft = Offset(left, top),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2f),
            )

            // Tip
            drawRoundRect(
                color = Color(0xFF22304A),
                topLeft = Offset(bodyW + 1f, top + bodyH * 0.30f),
                size = Size(tipW, bodyH * 0.40f),
                cornerRadius = CornerRadius(2f, 2f),
            )

            // Fill (clamped inside the body)
            val pct = (percent.coerceIn(0, 100)) / 100f
            val padding = 4f
            val innerLeft = left + padding
            val innerTop = top + padding
            val innerW = bodyW - 2 * padding
            val innerH = bodyH - 2 * padding
            val fillW = innerW * pct
            if (fillW > 0f) {
                drawRoundRect(
                    brush = fillBrush,
                    topLeft = Offset(innerLeft, innerTop),
                    size = Size(fillW, innerH),
                    cornerRadius = CornerRadius(3f, 3f),
                )
            }

            // Subtle highlight reflection
            val highlight = Path().apply {
                moveTo(innerLeft + 2f, innerTop + innerH * 0.2f)
                lineTo(innerLeft + fillW - 2f, innerTop + innerH * 0.2f)
                lineTo(innerLeft + fillW - 2f, innerTop + innerH * 0.35f)
                lineTo(innerLeft + 2f, innerTop + innerH * 0.35f)
                close()
            }
            drawPath(path = highlight, color = Color.White.copy(alpha = 0.18f))
        }
    }
}
