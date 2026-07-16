package com.matox.nexcore.presentation.wifi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.SettingsRemote
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.provider.Settings
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.TextPrimary

/**
 * Sticky bottom action button — "WiFi Details".
 *
 * Renders a wide gradient pill (MetricBlue → MetricCyan horizontal)
 * with an icon chip on the left and the label centered. Tapping fires
 * the supplied [onClick] callback (the host provides the launch +
 * snackbar wiring so the composable stays UI-only).
 *
 * The button itself is drawn glass-like — soft shadow, 18 dp corners —
 * so it sits comfortably at the bottom of the scrollable column above
 * the dashboard bottom-nav dock.
 */
@Composable
fun WifiDetailsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MetricBlue.copy(alpha = 0.30f),
                spotColor = MetricBlue.copy(alpha = 0.40f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MetricBlue, MetricCyan),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f),
                    ),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable { onClick() }
            .height(56.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SettingsRemote,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "WiFi Details",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = TextPrimary,
            )
        }
    }
}

/**
 * Helper used by the host to launch the system WiFi settings page.
 * Wrapped in `runCatching` so a SecurityException on a locked-down
 * device silently no-ops.
 *
 * Call from a `LaunchedEffect` or `onClick` lambda — the caller is
 * expected to also surface a snackbar so the user sees feedback
 * even when the system activity is unavailable.
 */
@Composable
fun rememberWifiDetailsLauncher(): () -> Unit {
    val context = LocalContext.current
    return {
        runCatching {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}