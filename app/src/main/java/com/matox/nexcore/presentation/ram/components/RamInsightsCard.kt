package com.matox.nexcore.presentation.ram.components

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Memory Health Insights" card — generates a small list of
 * recommendation / observation cards from the current [RamSnapshot].
 *
 * Insight types (derived purely from snapshot fields — no extra state):
 *
 *  - **alert** (red): top app uses > 600 MB and is still running.
 *    "Chrome has been using high memory for N hours."
 *  - **warning** (orange): there are user apps in the top list
 *    whose aggregate footprint exceeds 1 GB.
 *    "Closing unused applications could free ~X GB."
 *  - **info** (cyan): number of user apps in top list.
 *    "N apps are running in the background."
 *  - **success** (green): system is in a healthy state.
 *    "Memory usage is currently stable."
 *
 * Always renders at least one card. Maximum four.
 */
@Composable
fun RamInsightsCard(
    snapshot: RamSnapshot,
    modifier: Modifier = Modifier,
) {
    val insights = remember(snapshot) { generateInsights(snapshot) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricViolet.copy(alpha = 0.15f),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(
                    icon = Icons.Outlined.AutoAwesome,
                    accent = MetricViolet,
                    size = 40.dp,
                    iconSize = 22.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Memory Health Insights",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "AI-style recommendations, generated live",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                insights.forEach { ins ->
                    InsightRow(
                        icon = ins.icon,
                        title = ins.title,
                        subtitle = ins.subtitle,
                        accent = ins.accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(
            icon = icon,
            accent = accent,
            size = 36.dp,
            iconSize = 18.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

// --- Insight generation (pure) ----------------------------------------

private data class Insight(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector,
)

private val IconsWarn = Icons.Outlined.WarningAmber
private val IconsLight = Icons.Outlined.Lightbulb
private val IconsCheck = Icons.Outlined.CheckCircle
private val IconsInfo = Icons.Outlined.AutoAwesome

private fun generateInsights(snap: RamSnapshot): List<Insight> {
    val out = mutableListOf<Insight>()

    // 1. Top app alert — if the heaviest app is using > 600 MB.
    val top = snap.topApps.firstOrNull()
    if (top != null && top.pssMb >= 600) {
        out += Insight(
            title = "${top.displayName} has been using high memory",
            subtitle = "${formatMb(top.pssMb)} right now — consider closing it if you're done.",
            accent = MetricRed,
            icon = IconsWarn,
        )
    }

    // 2. Reclaimable footprint warning — user apps summing > 1 GB.
    val userApps = snap.topApps.filter { !it.isSystem }
    val userMb = userApps.sumOf { it.pssMb }
    if (userMb >= 1024L) {
        out += Insight(
            title = "Closing unused applications could free ${formatMb(userMb)}",
            subtitle = "${userApps.size} user apps currently in the top list.",
            accent = MetricOrange,
            icon = IconsLight,
        )
    }

    // 3. Background apps info.
    if (userApps.isNotEmpty()) {
        out += Insight(
            title = "${userApps.size} ${if (userApps.size == 1) "app is" else "apps are"} running in the background",
            subtitle = "Tap any app below to inspect its memory in detail.",
            accent = MetricCyan,
            icon = IconsInfo,
        )
    }

    // 4. Stability success — always the last card so the user ends on a positive note.
    val healthy = snap.percent < 60 && !snap.lowMemory
    out += Insight(
        title = if (healthy) "Memory usage is currently stable" else "Memory pressure is elevated",
        subtitle = if (healthy)
            "You're using ${snap.percent}% of ${formatGb(snap.totalGb)} total RAM."
        else
            "Pressure is at ${snap.percent}% — consider freeing memory.",
        accent = if (healthy) NexCoreGreen else MetricBlue,
        icon = if (healthy) IconsCheck else IconsInfo,
    )

    return out
}

// Small composable helper so we don't accidentally capture snapshot by reference.

private fun formatMb(mb: Long): String {
    val v = mb.toFloat()
    return when {
        v >= 1024f -> String.format("%.1f GB", v / 1024f)
        v <= 0f -> "0 MB"
        else -> "${mb.toInt()} MB"
    }
}

private fun formatGb(v: Float): String {
    if (v <= 0f) return "0.0 GB"
    val rounded = (v * 10f).toInt() / 10f
    return "$rounded GB"
}