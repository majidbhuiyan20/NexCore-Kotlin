package com.matox.nexcore.presentation.wifi.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Router
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
import com.matox.nexcore.domain.model.WifiIpInfo
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Network Info" card — vertical timeline of local addressing rows.
 *
 *  ─ IPv4 Address  : 192.168.1.42
 *  ─ Gateway       : 192.168.1.1
 *  ─ DNS           : 1.1.1.1
 *  ─ MAC Address   : AA:BB:CC:DD:EE:FF
 *
 * Mirrors the charging-history timeline used by the Battery screen
 * so the WiFi page reads as a familiar sequence of "label → value"
 * rows.
 */
@Composable
fun WifiNetworkInfoCard(
    ipInfo: WifiIpInfo?,
    modifier: Modifier = Modifier,
) {
    val info = ipInfo
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
                text = "Local Network",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "IPv4 · Gateway · DNS · MAC",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            val rows = listOf(
                TimelineRow(
                    icon = Icons.Outlined.Numbers,
                    label = "IPv4 Address",
                    value = info?.localIp?.takeIf { it.isNotBlank() } ?: "—",
                    accent = MetricBlue,
                ),
                TimelineRow(
                    icon = Icons.Outlined.Router,
                    label = "Gateway",
                    value = info?.gateway?.takeIf { it.isNotBlank() } ?: "—",
                    accent = MetricCyan,
                ),
                TimelineRow(
                    icon = Icons.Outlined.Dns,
                    label = "Primary DNS",
                    value = info?.dns1?.takeIf { it.isNotBlank() } ?: "—",
                    accent = MetricViolet,
                ),
                TimelineRow(
                    icon = Icons.Outlined.Hub,
                    label = "DHCP Server",
                    value = info?.dhcpServer?.takeIf { it.isNotBlank() } ?: "—",
                    accent = MetricBlue,
                ),
                TimelineRow(
                    icon = Icons.Outlined.Hub,
                    label = "Secondary DNS",
                    value = info?.dns2?.takeIf { it.isNotBlank() } ?: "—",
                    accent = MetricCyan,
                ),
            )

            rows.forEachIndexed { idx, row ->
                TimelineRowView(
                    row = row,
                    isFirst = idx == 0,
                    isLast = idx == rows.lastIndex,
                )
            }
        }
    }
}

private data class TimelineRow(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val accent: Color,
)

@Composable
private fun TimelineRowView(
    row: TimelineRow,
    isFirst: Boolean,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left column — hairline + dot.
        Column(
            modifier = Modifier.width(28.dp),
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
                    .height(if (isLast) 0.dp else 24.dp)
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