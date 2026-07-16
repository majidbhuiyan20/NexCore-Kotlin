package com.matox.nexcore.presentation.appmanager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Four-card summary row at the top of the App Manager list:
 *   Total Apps  |  User Apps  |  System Apps  |  Total Size
 *
 * Cards are forced to equal height via
 *   - `Modifier.height(IntrinsicSize.Min)` on the parent Row so each
 *     child measures the maximum intrinsic height of its siblings
 *   - `Modifier.fillMaxHeight()` on each card so they actually stretch
 *   - identical internal layout (icon chip row + bigValue + label) —
 *     no extra trailing-unit line that would otherwise unbalance the
 *     Total Size card
 */
@Composable
fun AppStatsRow(
    totalApps: Int,
    userApps: Int,
    systemApps: Int,
    totalSizeBytes: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = Icons.Outlined.Apps,
            accent = MetricAccent.PURPLE,
            label = "Total Apps",
            bigValue = totalApps.toString(),
        )
        StatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = Icons.Outlined.Person,
            accent = MetricAccent.GREEN,
            label = "User Apps",
            bigValue = userApps.toString(),
        )
        StatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = Icons.Outlined.Settings,
            accent = MetricAccent.BLUE,
            label = "System Apps",
            bigValue = systemApps.toString(),
        )
        StatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = Icons.Outlined.CloudDownload,
            accent = MetricAccent.ORANGE,
            label = "Total Size",
            bigValue = "${formatGb(totalSizeBytes)} GB",
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    accent: MetricAccent,
    label: String,
    bigValue: String,
) {
    val accentColor = accent.toColor()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131C2F))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconChip(
                icon = icon,
                accent = accentColor,
                size = 28.dp,
                iconSize = 16.dp,
                backgroundAlpha = 0.22f,
            )
        }
        Text(
            text = bigValue,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
            color = TextPrimary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
        )
    }
}

private fun formatGb(bytes: Long): String {
    val gb = bytes / (1024f * 1024f * 1024f)
    return ((gb * 10f).toInt() / 10f).toString()
}
