package com.matox.nexcore.presentation.ram.components

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * Live memory usage timeline. Renders the rolling 60-minute history
 * with:
 *  - Header row: title + 5-tick badge showing current % + "auto-updating"
 *    subtitle + a small "AI" chip.
 *  - Canvas area: 200 dp tall, 4-stop grid + smooth cubic line + 3-stop
 *    gradient fill underneath + per-sample dots + glowing right-edge
 *    marker with a "now" callout bubble.
 *  - Time axis labels: 60m, 45m, 30m, 15m, now.
 *
 * Glass surface = layered gradient + 1 dp border + soft shadow.
 */
@Composable
fun RamHistoryChart(
    history: List<Int>,
    modifier: Modifier = Modifier,
) {
    val latest = history.lastOrNull() ?: 0
    val animatedLatest by animateFloatAsState(
        targetValue = latest.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "ram-chart-latest",
    )
    val animatedPercent = animatedLatest.toInt()

    // Continuous pulse ring around the latest sample.
    val pulse = rememberInfiniteTransition(label = "ram-chart-pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ram-chart-pulse-scale",
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ram-chart-pulse-alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricBlue.copy(alpha = 0.15f),
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
        // Glass highlight strip on top.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // ---- Header ------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Memory Usage Timeline",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "Last 60 minutes · auto-updating",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Live pulse dot.
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(NexCoreGreen),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$animatedPercent% now",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MetricBlue,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Chart canvas -------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1729))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
            ) {
                if (history.isEmpty()) {
                    Text(
                        text = "Waiting for samples…",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxWidth().height(172.dp)) {
                        val w = size.width
                        val h = size.height
                        val padX = 8f
                        val padY = 8f
                        val plotW = w - padX * 2
                        val plotH = h - padY * 2

                        // Background grid (5 horizontal lines).
                        val gridColor = Color(0xFF1F2A44)
                        for (i in 0..4) {
                            val y = padY + plotH * (i / 4f)
                            drawLine(
                                color = gridColor,
                                start = Offset(padX, y),
                                end = Offset(padX + plotW, y),
                                strokeWidth = 1f,
                            )
                        }

                        // Samples → (x, y)
                        val n = history.size
                        val stepX = if (n <= 1) 0f else plotW / (n - 1)
                        val points = history.mapIndexed { idx, pct ->
                            val x = padX + stepX * idx
                            val y = padY + plotH * (1f - pct.coerceIn(0, 100) / 100f)
                            Offset(x, y)
                        }

                        // Smooth cubic path.
                        val path = Path()
                        if (points.isNotEmpty()) {
                            path.moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.lastIndex) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val midX = (p0.x + p1.x) / 2f
                                path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }
                        }

                        // Area fill — 3-stop gradient (cyan → blue → near-transparent).
                        val fillPath = Path().apply {
                            addPath(path)
                            if (points.isNotEmpty()) {
                                lineTo(points.last().x, padY + plotH)
                                lineTo(points.first().x, padY + plotH)
                                close()
                            }
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MetricCyan.copy(alpha = 0.55f),
                                    MetricBlue.copy(alpha = 0.30f),
                                    MetricViolet.copy(alpha = 0.05f),
                                ),
                                startY = padY,
                                endY = padY + plotH,
                            ),
                        )

                        // Soft under-stroke (halo).
                        drawPath(
                            path = path,
                            color = MetricCyan.copy(alpha = 0.30f),
                            style = Stroke(width = 9f),
                        )
                        // Crisp top line — cyan → blue → violet gradient.
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(MetricCyan, MetricBlue, MetricViolet),
                            ),
                            style = Stroke(width = 3f),
                        )

                        // Per-sample dots (skip the latest — drawn separately).
                        for ((idx, p) in points.withIndex()) {
                            if (idx == points.lastIndex) continue
                            drawCircle(
                                color = MetricCyan.copy(alpha = 0.7f),
                                radius = 2.2f,
                                center = p,
                            )
                        }

                        // Latest sample — pulse + halo + core.
                        val last = points.last()
                        // Vertical hairline at the rightmost sample.
                        drawLine(
                            color = MetricCyan.copy(alpha = 0.45f),
                            start = Offset(last.x, padY),
                            end = Offset(last.x, padY + plotH),
                            strokeWidth = 1.5f,
                        )
                        // Outer expanding pulse rings.
                        drawCircle(
                            color = MetricCyan.copy(alpha = pulseAlpha * 0.7f),
                            radius = 7f * pulseScale,
                            center = last,
                        )
                        drawCircle(
                            color = MetricBlue.copy(alpha = pulseAlpha * 0.4f),
                            radius = 10f * pulseScale,
                            center = last,
                        )
                        // Static halo.
                        drawCircle(
                            color = MetricBlue.copy(alpha = 0.30f),
                            radius = 11f,
                            center = last,
                        )
                        // Core dot.
                        drawCircle(
                            color = Color.White,
                            radius = 5.5f,
                            center = last,
                        )
                        drawCircle(
                            color = MetricBlue,
                            radius = 4f,
                            center = last,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- 5-tick time axis ---------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("60m", "45m", "30m", "15m", "now").forEachIndexed { idx, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (idx == 4) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (idx == 4) MetricBlue else TextSecondary,
                    )
                }
            }
        }
    }
}

// Suppress unused-icon warning — AutoAwesome is reserved for a future
// "AI insight overlay" toggle on the chart.
@Suppress("unused")
private val _keep: Any = Icons.Outlined.AutoAwesome