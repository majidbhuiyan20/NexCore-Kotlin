package com.matox.nexcore.presentation.battery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Schedule
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
import com.matox.nexcore.domain.model.BatteryStatus
import com.matox.nexcore.domain.model.ChargingInfo
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * "Charging History" card — vertical timeline with four labelled rows.
 *
 *  ─ Charging Started  : Today, 14:32 (or "—")
 *  ─ Charging Duration : 1 h 24 min so far
 *  ─ Estimated to Full : 47 min remaining (hidden while not charging)
 *  ─ Last Full Charge  : Yesterday, 02:14 (or "—")
 *
 * Reuses the timeline look established by `RamEventsCard`. Each row
 * has a left-side hairline + dot, an icon chip in the middle, and the
 * value on the right.
 */
@Composable
fun ChargingInfoCard(
    charging: ChargingInfo,
    status: BatteryStatus,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricCyan.copy(alpha = 0.15f),
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
            Text(
                text = "Charging History",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Recent charging session telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            val rows = buildList {
                add(
                    Row(
                        Icons.Outlined.Bolt,
                        "Charging Started",
                        formatStart(charging.chargingStartedMs),
                        MetricCyan,
                    ),
                )
                add(
                    Row(
                        Icons.Outlined.Schedule,
                        "Charging Duration",
                        formatDuration(charging.durationSoFarMin),
                        MetricBlue,
                    ),
                )
                if (status == BatteryStatus.CHARGING && charging.estimatedTimeToFullMin != null) {
                    add(
                        Row(
                            Icons.Outlined.HourglassBottom,
                            "Estimated Time to Full",
                            "${charging.estimatedTimeToFullMin} min remaining",
                            MetricViolet,
                        ),
                    )
                }
                add(
                    Row(
                        Icons.Outlined.BatteryFull,
                        "Last Full Charge",
                        formatStart(charging.lastFullChargeMs),
                        NexCoreGreen,
                    ),
                )
            }

            Column {
                rows.forEachIndexed { idx, row ->
                    TimelineRow(
                        row = row,
                        isFirst = idx == 0,
                        isLast = idx == rows.lastIndex,
                    )
                }
            }
        }
    }
}

private data class Row(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val accent: Color,
)

@Composable
private fun TimelineRow(
    row: Row,
    isFirst: Boolean,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left column — line + dot.
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(8.dp)
                    .background(
                        color = if (isFirst) Color.Transparent else row.accent.copy(alpha = 0.35f),
                    ),
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(row.accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(row.accent),
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (isLast) Color.Transparent else row.accent.copy(alpha = 0.35f),
                    ),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Icon chip.
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(row.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = row.accent,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Label + value.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = row.value,
                style = MaterialTheme.typography.bodySmall,
                color = row.accent,
            )
        }
    }
}

private fun formatStart(ms: Long?): String {
    if (ms == null) return "—"
    val now = System.currentTimeMillis()
    val deltaMin = ((now - ms) / 60_000L).toInt()
    return when {
        deltaMin < 60 -> {
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            "Today, ${fmt.format(Date(ms))}"
        }
        deltaMin < 24 * 60 -> {
            val hrs = deltaMin / 60
            val mins = deltaMin % 60
            if (mins == 0) "${hrs}h ago" else "${hrs}h ${mins}m ago"
        }
        else -> {
            val fmt = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            fmt.format(Date(ms))
        }
    }
}

private fun formatDuration(minutes: Int): String {
    if (minutes <= 0) return "—"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m} min so far"
        m == 0 -> "${h} h so far"
        else -> "${h} h ${m} min so far"
    }
}

@Suppress("unused")
private val _keep: Arrangement.Horizontal = Arrangement.Start
