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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.MetricSoftRed
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * Premium hero card for the RAM detail screen.
 *
 * Layout (top → bottom):
 *
 *  ┌──────────────────────────────────────────────┐
 *  │ [icon] Memory       [Status pill: Excellent] │
 *  │ Real-time device telemetry                    │
 *  │                                              │
 *  │           ╭───────╮                          │
 *  │           │  67%  │   animated ring          │
 *  │           │ 4.2GB │                          │
 *  │           │ of 8GB│                          │
 *  │           ╰───────╯                          │
 *  │                                              │
 *  │  Free RAM        │  Cached        │  Pressure │
 *  │  3.8 GB · 47 %   │  1.4 GB · 17 % │  Normal   │
 *  │  ▓▓▓▓░░░░░       │  ▓▓░░░░░░░     │  ────     │
 *  └──────────────────────────────────────────────┘
 *
 * Glass surface = layered gradient + 1 dp border + 8 dp shadow.
 */
@Composable
fun RamHeroCard(
    snapshot: RamSnapshot,
    modifier: Modifier = Modifier,
) {
    val accent = MetricAccent.BLUE
    val accentColor = accent.toColor()

    // Status badge derived from percent.
    val statusLabel: String
    val statusColor: Color
    val statusBg: Color
    when {
        snapshot.percent >= 80 -> {
            statusLabel = "High Usage"
            statusColor = MetricSoftRed
            statusBg = MetricRed.copy(alpha = 0.18f)
        }
        snapshot.percent >= 60 -> {
            statusLabel = "Normal"
            statusColor = MetricCyan
            statusBg = MetricCyan.copy(alpha = 0.18f)
        }
        else -> {
            statusLabel = "Excellent"
            statusColor = NexCoreGreen
            statusBg = NexCoreGreen.copy(alpha = 0.18f)
        }
    }

    // Animated ring sweep.
    val animatedPct by animateFloatAsState(
        targetValue = snapshot.percent.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "ram-hero-pct",
    )

    // Pull out the components we surface as labelled rows below the ring.
    val totalMb = (snapshot.totalGb * 1024f).toLong().coerceAtLeast(1L)
    val freeMb = (snapshot.availableGb * 1024f).toLong().coerceAtLeast(0L)
    val cachedMb = snapshot.cachedMb.coerceAtLeast(0L)

    // Memoized card brushes — recomputed only when the colors they
    // build from change; keeps the per-tick recomposition cheap.
    val surfaceBrush = remember {
        Brush.verticalGradient(colors = listOf(Surface, Surface.copy(alpha = 0.92f)))
    }
    val glassBrush = remember {
        Brush.verticalGradient(colors = listOf(GlassHighlight, Color.Transparent))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MetricCyan.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(brush = surfaceBrush)
            .border(1.dp, CardStroke, RoundedCornerShape(24.dp)),
    ) {
        // Glass highlight along the top edge.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(brush = glassBrush),
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // ---- Header row ------------------------------------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconChip(
                    icon = Icons.Outlined.Memory,
                    accent = accentColor,
                    size = 44.dp,
                    iconSize = 24.dp,
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
                        text = if (snapshot.lowMemory)
                            "Low memory — kernel threshold reached"
                        else
                            "Real-time device telemetry",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (snapshot.lowMemory) MetricSoftRed else TextSecondary,
                    )
                }
                // Status badge pill.
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusBg)
                        .border(
                            1.dp,
                            statusColor.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- Ring + interior -------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedRamRing(
                    percent = animatedPct,
                    color = if (snapshot.percent >= 85) MetricRed else accentColor,
                    sizeDp = 240,
                    strokeWidthDp = 18,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = formatGb(snapshot.usedGb),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "of ${formatGb(snapshot.totalGb)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${snapshot.percent}% used",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        ),
                        color = if (snapshot.percent >= 85) MetricRed else accentColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- Three labelled mini-bar rows ------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PressureCell(
                    label = "Free RAM",
                    valueMb = freeMb,
                    totalMb = totalMb,
                    accent = MetricCyan,
                    icon = Icons.Outlined.Memory,
                    modifier = Modifier.weight(1f),
                )
                PressureCell(
                    label = "Cached",
                    valueMb = cachedMb,
                    totalMb = totalMb,
                    accent = MetricViolet,
                    icon = Icons.Outlined.Cached,
                    modifier = Modifier.weight(1f),
                )
                PressureCell(
                    label = "Pressure",
                    valueMb = 0,
                    totalMb = 0,
                    accent = statusColor,
                    icon = Icons.Outlined.Bolt,
                    modifier = Modifier.weight(1f),
                    pressureLabel = statusLabel,
                )
            }
        }
    }
}

/**
 * Small labelled cell — used in the 3-column strip below the ring.
 *
 * For Free RAM and Cached, the mini bar shows the share of total RAM.
 * For Pressure, no bar is rendered; just the status label.
 */
@Composable
private fun PressureCell(
    label: String,
    valueMb: Long,
    totalMb: Long,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    pressureLabel: String? = null,
) {
    val pct = if (totalMb > 0L && valueMb > 0L) {
        ((valueMb.toFloat() / totalMb.toFloat()) * 100f).toInt().coerceIn(0, 100)
    } else null

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = pressureLabel ?: formatMb(valueMb),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = TextPrimary,
        )
        if (pct != null) {
            Text(
                text = "$pct% of total",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TrackGray),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct / 100f)
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
        // Animated sweep
        val sweep = (percent.coerceIn(0f, 100f) / 100f) * 360f
        if (sweep > 0f) {
            // Outer soft glow
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
                style = Stroke(width = strokePx + 8f),
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

private fun formatGb(v: Float): String {
    if (v <= 0f) return "0.0 GB"
    val rounded = (v * 10f).toInt() / 10f
    return "$rounded GB"
}

private fun formatMb(mb: Long): String {
    val v = mb.toFloat()
    return when {
        v >= 1024f -> String.format("%.1f GB", v / 1024f)
        v <= 0f -> "0 MB"
        else -> "${mb.toInt()} MB"
    }
}

// Suppress an unused-import warning on the unused colour, keeping it
// available for the high-usage path of the badge.
@Suppress("unused")
private val _keep: Color = MetricOrange