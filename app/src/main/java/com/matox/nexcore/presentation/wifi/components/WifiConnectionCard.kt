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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PublicOff
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tag
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
import com.matox.nexcore.domain.model.WifiConnection
import com.matox.nexcore.domain.model.WifiSecurity
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Connection Details" card — 2×2 grid of link metadata.
 *
 *  ┌─────────────────────┬──────────────────────┐
 *  │ BSSID               │ Link Speed           │
 *  │ AA:BB:CC:DD:EE:FF   │ 866 Mbps             │
 *  ├─────────────────────┼──────────────────────┤
 *  │ Standard            │ Security             │
 *  │ Wi-Fi 5 (802.11ac)  │ WPA3-Personal        │
 *  └─────────────────────┴──────────────────────┘
 *
 * Each cell uses the same accent-tinted pill style as the Battery
 * metric cards. When the device is disconnected every value is
 * rendered as `—` and the icon chip is dimmed.
 */
@Composable
fun WifiConnectionCard(
    connection: WifiConnection?,
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
                text = "Connection Details",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "BSSID · Speed · Standard · Security",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailCell(
                    label = "BSSID",
                    value = connection?.bssid?.takeIf { it.isNotBlank() } ?: "—",
                    accent = MetricBlue,
                    icon = Icons.Outlined.Tag,
                    modifier = Modifier.weight(1f),
                )
                DetailCell(
                    label = "Link Speed",
                    value = connection?.linkSpeedMbps?.takeIf { it > 0 }?.let { "$it Mbps" } ?: "—",
                    accent = MetricCyan,
                    icon = Icons.Outlined.Speed,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailCell(
                    label = "Frequency",
                    value = connection?.frequencyMhz?.takeIf { it > 0 }?.let { "${it} MHz" } ?: "—",
                    accent = MetricViolet,
                    icon = Icons.Outlined.Router,
                    modifier = Modifier.weight(1f),
                )
                DetailCell(
                    label = "Security",
                    value = connection?.security?.name?.replace('_', '-') ?: "UNKNOWN",
                    accent = MetricOrange,
                    icon = if (connection?.security == WifiSecurity.OPEN) {
                        Icons.Outlined.PublicOff
                    } else {
                        Icons.Outlined.Lock
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DetailCell(
    label: String,
    value: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = TextPrimary,
        )
    }
}