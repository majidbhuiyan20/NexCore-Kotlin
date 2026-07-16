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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Card containing a Compose-Canvas line + area chart of recent RAM
 * percentages. Designed so the **live updates are obvious to the eye**:
 *
 *  - The chart's right edge has an animated pulse ring that re-fires
 *    on every new sample (infinite transition).
 *  - The latest sample dot animates smoothly between Y positions
 *    using `animateFloatAsState` so each 3 s tick glides into place.
 *  - A prominent "current %" badge on the top-right updates with the
 *    same animateFloatAsState, so the user sees a number counting
 *    rather than a frozen screenshot.
 *  - The line is drawn as a smooth cubic path with a wide gradient
 *    fill underneath, plus per-sample dots so individual ticks are
 *    visible.
 *
 * Renders gracefully with an empty history (still shows the grid +
 * "Waiting for samples…" caption).
 */
@Composable
fun RamHistoryChart(
    history: List<Int>,
    modifier: Modifier = Modifier,
) {
    // Latest value — animated so it counts visibly each tick.
    val latest = history.lastOrNull() ?: 0
    val animatedLatest by animateFloatAsState(
        targetValue = latest.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "ramLatest",
    )
    val animatedPercent = animatedLatest.toInt()

    // Pulse ring around the latest dot — a constantly running
    // infinite transition that re-fires every 3 s as new samples
    // arrive. Visible feedback that the chart is alive.
    val pulse = rememberInfiniteTransition(label = "ramPulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ramPulseScale",
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ramPulseAlpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Live history",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = "Last ~3 minutes · ${history.size} samples",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            // Big current-value badge — this is what makes the chart
            // feel "live" at a glance: the number visibly changes
            // every 3 s.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MetricBlue.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(NexCoreGreen),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$animatedPercent%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MetricBlue,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "now",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F1729))
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            if (history.isEmpty()) {
                Text(
                    text = "Waiting for samples…",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(156.dp)) {
                    val w = size.width
                    val h = size.height
                    val padX = 8f
                    val padY = 8f
                    val plotW = w - padX * 2
                    val plotH = h - padY * 2

                    // --- Background grid (5 horizontal lines) ----------
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

                    // --- Map samples → (x,y) ---------------------------
                    val n = history.size
                    val stepX = if (n <= 1) 0f else plotW / (n - 1)
                    val points = history.mapIndexed { idx, pct ->
                        val x = padX + stepX * idx
                        // Invert Y so 100% sits at the top.
                        val y = padY + plotH * (1f - pct.coerceIn(0, 100) / 100f)
                        Offset(x, y)
                    }

                    // --- Smooth path: cubicTo between consecutive points
                    val path = Path()
                    if (points.isNotEmpty()) {
                        path.moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.lastIndex) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            path.cubicTo(
                                midX, p0.y,
                                midX, p1.y,
                                p1.x, p1.y,
                            )
                        }
                    }

                    // --- Area fill under the path ---------------------
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
                                MetricBlue.copy(alpha = 0.55f),
                                MetricCyan.copy(alpha = 0.10f),
                                MetricBlue.copy(alpha = 0.02f),
                            ),
                            startY = padY,
                            endY = padY + plotH,
                        ),
                    )

                    // --- Glowing under-stroke for soft halo -----------
                    drawPath(
                        path = path,
                        color = MetricCyan.copy(alpha = 0.30f),
                        style = Stroke(width = 8f),
                    )
                    // --- Crisp top line ------------------------------
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(MetricCyan, MetricBlue),
                        ),
                        style = Stroke(width = 2.5f),
                    )

                    // --- Per-sample dots so each tick is visible -------
                    for ((idx, p) in points.withIndex()) {
                        // Skip the last — drawn separately with the pulse.
                        if (idx == points.lastIndex) continue
                        drawCircle(
                            color = MetricCyan.copy(alpha = 0.7f),
                            radius = 2.2f,
                            center = p,
                        )
                    }

                    // --- Animated latest sample dot -------------------
                    val last = points.last()
                    // Outer expanding pulse ring — re-fires every 3 s
                    // because the infinite transition runs continuously.
                    drawCircle(
                        color = MetricCyan.copy(alpha = pulseAlpha * 0.7f),
                        radius = 6f * pulseScale,
                        center = last,
                    )
                    drawCircle(
                        color = MetricBlue.copy(alpha = pulseAlpha * 0.4f),
                        radius = 9f * pulseScale,
                        center = last,
                    )
                    // Halo (static)
                    drawCircle(
                        color = MetricBlue.copy(alpha = 0.30f),
                        radius = 10f,
                        center = last,
                    )
                    // Core dot
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = last,
                    )
                    drawCircle(
                        color = MetricBlue,
                        radius = 3.5f,
                        center = last,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "1m ago",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Text(
                text = "now",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MetricBlue,
            )
        }
    }
}
