package com.matox.nexcore.presentation.wifi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
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
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Premium top bar for the WiFi detail screen.
 *
 * Mirrors the RAM / Battery top bars — large title + subtitle, glass
 * surface with shadow + border — but tinted with [MetricBlue] accents
 * so the screen reads as the "network / connectivity" destination.
 *
 * The trailing icons expose `Refresh` (fires the VM poll) and
 * `More options` so the screen feels close to a real system settings
 * page.
 */
@Composable
fun WifiTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = MetricBlue.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.30f),
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Surface, Surface.copy(alpha = 0.85f)),
                    ),
                )
                .border(1.dp, CardStroke, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MetricBlue.copy(alpha = 0.20f))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MetricBlue,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "WiFi",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = "Real-time network telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            GlassyIconButton(
                icon = Icons.Outlined.Refresh,
                contentDescription = "Refresh",
                onClick = onRefreshClick,
                accent = MetricCyan,
            )

            Spacer(modifier = Modifier.width(8.dp))

            GlassyIconButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "More options",
                onClick = onMoreClick,
                accent = MetricBlue,
            )
        }
    }
}

/** Small circular icon button with a glassy tint. */
@Composable
private fun GlassyIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    accent: Color,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.18f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
    }
}