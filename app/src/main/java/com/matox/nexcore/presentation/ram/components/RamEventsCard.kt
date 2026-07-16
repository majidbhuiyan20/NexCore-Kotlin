package com.matox.nexcore.presentation.ram.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MemoryEvent
import com.matox.nexcore.domain.model.MemoryEventType
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Recent Memory Events" card — vertical timeline showing the
 * synthesised events from [com.matox.nexcore.data.device.RamProvider].
 *
 * Layout:
 *  - Left column: vertical hairline + colour-coded dots.
 *  - Middle column: icon chip (small) + title + subtitle.
 *  - Right column: relative timestamp ("now", "2 m ago", etc).
 *
 * Empty state: "No notable events in the last hour."
 */
@Composable
fun RamEventsCard(
    events: List<MemoryEvent>,
    modifier: Modifier = Modifier,
) {
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
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(
                    icon = Icons.Outlined.Schedule,
                    accent = MetricBlue,
                    size = 40.dp,
                    iconSize = 22.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Recent Memory Events",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = if (events.isEmpty()) "Last hour · nothing notable yet"
                        else "Last hour · ${events.size} ${if (events.size == 1) "event" else "events"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No notable events in the last hour.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                return
            }

            // Timeline
            Column {
                events.forEachIndexed { idx, event ->
                    EventRow(
                        event = event,
                        isFirst = idx == 0,
                        isLast = idx == events.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: MemoryEvent,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val accentColor = event.accent.toColor()
    val now = System.currentTimeMillis()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Left column — vertical line + dot.
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Upper line (skip if first — nothing above)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(8.dp)
                    .background(
                        color = if (isFirst) Color.Transparent else accentColor.copy(alpha = 0.35f),
                    ),
            )
            // Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                )
            }
            // Lower line (skip if last)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(
                        color = if (isLast) Color.Transparent else accentColor.copy(alpha = 0.35f),
                    ),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Middle column — icon + title + subtitle.
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            IconChip(
                icon = iconFor(event.type),
                accent = accentColor,
                size = 32.dp,
                iconSize = 16.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right column — relative timestamp.
        Text(
            text = relativeTime(now - event.timestampMs),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private fun iconFor(type: MemoryEventType): ImageVector = when (type) {
    MemoryEventType.APP_OPENED -> Icons.Outlined.Memory
    MemoryEventType.LARGE_ALLOCATION -> Icons.Outlined.Bolt
    MemoryEventType.BACKGROUND_CLEANUP -> Icons.Outlined.CheckCircle
    MemoryEventType.MEMORY_PRESSURE_CHANGE -> Icons.Outlined.WarningAmber
    MemoryEventType.LOW_MEMORY_WARNING -> Icons.Outlined.WarningAmber
}

/** Format a delta in ms as a short relative string. */
private fun relativeTime(deltaMs: Long): String {
    val seconds = (deltaMs / 1000L).coerceAtLeast(0L)
    return when {
        seconds < 60L -> "now"
        seconds < 3600L -> "${seconds / 60L} m ago"
        seconds < 86_400L -> "${seconds / 3600L} h ago"
        else -> "${seconds / 86_400L} d ago"
    }
}

// Keep AutoAwesome in scope for a future "AI summary" overlay on the
// events list.
@Suppress("unused")
private val _keep: ImageVector = Icons.Outlined.AutoAwesome