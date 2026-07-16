package com.matox.nexcore.presentation.wifi.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
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
import com.matox.nexcore.domain.model.WifiConnection
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.MetricSoftRed
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Hero card for the WiFi detail screen.
 *
 * Layout:
 *  - Header row: "WiFi Connection" title + SSID subtitle on the left,
 *    status pill on the right.
 *  - Large 240 dp animated ring whose colour follows the signal
 *    threshold (≥75 blue, 50–74 cyan, 30–49 orange, <30 red).
 *  - Inside the ring: huge signal percent, RSSI dBm label, and a
 *    band/channel pill (or "No link" when disconnected).
 *
 * Glass surface: 24 dp rounded corners, 1 dp border, 8 dp shadow
 * with blue ambient glow.
 */
@Composable
fun WifiHeroCard(
    connection: WifiConnection?,
    modifier: Modifier = Modifier,
) {
    val pct = connection?.signalPercent ?: 0
    val animatedPct by animateFloatAsState(
        targetValue = pct.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "wifi-hero-pct",
    )
    val ringColor = signalColor(pct, hasConnection = connection != null)
    val (statusLabel, statusBg, statusFg) = statusPill(connection)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MetricBlue.copy(alpha = 0.18f),
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
                        text = if (connection != null) "Connected" else "WiFi Connection",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = connection?.ssid?.takeIf { it.isNotBlank() }
                            ?: "Tap WiFi Details to connect",
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Soft outer pulse ring.
                val infinite = rememberInfiniteTransition(label = "wifi-hero-pulse")
                val pulseScale by infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "wifi-hero-pulse-scale",
                )
                val pulseAlpha by infinite.animateFloat(
                    initialValue = 0.20f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "wifi-hero-pulse-alpha",
                )
                Box(modifier = Modifier.size(248.dp * pulseScale)) {
                    Canvas(modifier = Modifier.size(248.dp)) {
                        val strokePx = 6f
                        drawCircle(
                            color = ringColor.copy(alpha = pulseAlpha),
                            radius = (size.minDimension / 2f) - strokePx,
                        )
                    }
                }

                WifiRing(
                    percent = animatedPct,
                    color = ringColor,
                    sizeDp = 240,
                    strokeWidthDp = 18,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ringColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (connection != null) Icons.Outlined.Wifi else Icons.Outlined.SignalWifiOff,
                            contentDescription = null,
                            tint = ringColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (connection != null) "$pct%" else "—",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 56.sp,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = signalSubLabel(connection),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PillBadge(
                        label = if (connection != null) {
                            "${bandLabel(connection.frequencyMhz)} · Ch ${connection.channel.coerceAtLeast(0)}"
                        } else {
                            "No link"
                        },
                        bg = ringColor.copy(alpha = 0.18f),
                        fg = ringColor,
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
private fun WifiRing(
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

private fun signalColor(percent: Int, hasConnection: Boolean): Color {
    if (!hasConnection) return MetricSoftRed
    return when {
        percent >= 75 -> MetricBlue
        percent >= 50 -> MetricCyan
        percent >= 30 -> MetricOrange
        else -> MetricRed
    }
}

private fun statusPill(
    connection: WifiConnection?,
): Triple<String, Color, Color> {
    if (connection == null) {
        return Triple("Searching", MetricOrange.copy(alpha = 0.18f), MetricOrange)
    }
    return Triple("Connected", MetricBlue.copy(alpha = 0.20f), MetricBlue)
}

private fun signalSubLabel(connection: WifiConnection?): String {
    if (connection == null) return "Tap WiFi Details to connect"
    return "${connection.rssiDbm} dBm"
}

private fun bandLabel(freqMhz: Int): String = when {
    freqMhz == 0 -> "WiFi"
    freqMhz in 2400..2500 -> "2.4 GHz"
    freqMhz in 5000..5900 -> "5 GHz"
    freqMhz in 5925..7125 -> "6 GHz"
    else -> "WiFi"
}