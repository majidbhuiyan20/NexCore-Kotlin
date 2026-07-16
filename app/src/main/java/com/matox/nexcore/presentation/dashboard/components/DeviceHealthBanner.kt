package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.DeviceHealth
import com.matox.nexcore.ui.theme.HealthCardBackground
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.NexCoreGreenAccent
import com.matox.nexcore.ui.theme.NexCoreGreenDark
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

@Composable
fun DeviceHealthBanner(
    health: DeviceHealth,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HealthCardBackground)
            .border(1.dp, NexCoreGreen.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShieldIllustration(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = health.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = health.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NexCoreGreen,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Stylized shield-with-check illustration built entirely with Canvas so
 * no binary asset is needed.
 */
@Composable
private fun ShieldIllustration(modifier: Modifier = Modifier) {
    val shieldBrush = Brush.verticalGradient(
        colors = listOf(NexCoreGreen, NexCoreGreenAccent, NexCoreGreenDark),
    )
    val glow = NexCoreGreen.copy(alpha = 0.55f)
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Glow halo
            drawCircle(
                color = glow,
                radius = w * 0.42f,
                center = Offset(w / 2f, h / 2f),
            )

            // Shield silhouette
            val shieldPath = Path().apply {
                moveTo(w / 2f, h * 0.08f)
                lineTo(w * 0.92f, h * 0.22f)
                lineTo(w * 0.92f, h * 0.55f)
                cubicTo(
                    w * 0.92f, h * 0.85f,
                    w * 0.62f, h * 0.95f,
                    w / 2f, h * 0.98f,
                )
                cubicTo(
                    w * 0.38f, h * 0.95f,
                    w * 0.08f, h * 0.85f,
                    w * 0.08f, h * 0.55f,
                )
                lineTo(w * 0.08f, h * 0.22f)
                close()
            }
            drawPath(path = shieldPath, brush = shieldBrush)

            // Circuit traces
            val traceColor = Color(0xFF86EFAC)
            // left side
            drawLine(traceColor.copy(alpha = 0.6f), Offset(w * 0.02f, h * 0.35f), Offset(w * 0.18f, h * 0.35f), strokeWidth = 1.5f)
            drawLine(traceColor.copy(alpha = 0.6f), Offset(w * 0.18f, h * 0.35f), Offset(w * 0.18f, h * 0.55f), strokeWidth = 1.5f)
            drawCircle(traceColor.copy(alpha = 0.8f), 2f, Offset(w * 0.02f, h * 0.35f))
            drawCircle(traceColor.copy(alpha = 0.8f), 2f, Offset(w * 0.18f, h * 0.55f))
            // right side
            drawLine(traceColor.copy(alpha = 0.6f), Offset(w * 0.82f, h * 0.45f), Offset(w * 0.98f, h * 0.45f), strokeWidth = 1.5f)
            drawLine(traceColor.copy(alpha = 0.6f), Offset(w * 0.82f, h * 0.45f), Offset(w * 0.82f, h * 0.65f), strokeWidth = 1.5f)
            drawCircle(traceColor.copy(alpha = 0.8f), 2f, Offset(w * 0.98f, h * 0.45f))
            drawCircle(traceColor.copy(alpha = 0.8f), 2f, Offset(w * 0.82f, h * 0.65f))

            // Check mark
            val check = Path().apply {
                moveTo(w * 0.34f, h * 0.55f)
                lineTo(w * 0.46f, h * 0.66f)
                lineTo(w * 0.66f, h * 0.42f)
            }
            drawPath(
                path = check,
                color = Color.White,
                style = Stroke(width = 4f),
            )
        }
    }
}
