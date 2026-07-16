package com.matox.nexcore.presentation.ram.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Card containing a Compose-Canvas line + area chart of recent RAM
 * percentages. The chart draws:
 *  - a soft horizontal grid (4 lines, 25% / 50% / 75% / 100%)
 *  - a gradient fill under the line (accent → transparent)
 *  - a smooth cubic-to path through the samples
 *  - a glowing dot at the latest sample
 *  - "now / 1m ago" labels along the x-axis
 *
 * Renders gracefully with an empty history (still shows the grid +
 * "Waiting for samples…" caption).
 */
@Composable
fun RamHistoryChart(
    history: List<Int>,
    modifier: Modifier = Modifier,
) {
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MetricBlue.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "3 s tick",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MetricBlue,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
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
                Canvas(modifier = Modifier.fillMaxWidth().height(136.dp)) {
                    val w = size.width
                    val h = size.height
                    val padX = 6f
                    val padY = 8f
                    val plotW = w - padX * 2
                    val plotH = h - padY * 2

                    // --- Background grid (4 horizontal lines) ----------
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
                                MetricBlue.copy(alpha = 0.45f),
                                MetricBlue.copy(alpha = 0.05f),
                            ),
                            startY = padY,
                            endY = padY + plotH,
                        ),
                    )

                    // --- Glowing under-stroke for soft halo -----------
                    drawPath(
                        path = path,
                        color = MetricCyan.copy(alpha = 0.25f),
                        style = Stroke(width = 7f),
                    )
                    // --- Crisp top line ------------------------------
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(MetricCyan, MetricBlue),
                        ),
                        style = Stroke(width = 2.5f),
                    )

                    // --- Glowing dot at the latest sample -------------
                    val last = points.last()
                    // Halo
                    drawCircle(
                        color = MetricBlue.copy(alpha = 0.30f),
                        radius = 9f,
                        center = last,
                    )
                    // Core dot
                    drawCircle(
                        color = Color.White,
                        radius = 4.5f,
                        center = last,
                    )
                    drawCircle(
                        color = MetricBlue,
                        radius = 3f,
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