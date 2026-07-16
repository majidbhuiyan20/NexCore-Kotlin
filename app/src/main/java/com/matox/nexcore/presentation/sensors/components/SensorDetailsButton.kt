package com.matox.nexcore.presentation.sensors.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.outlined.Settings
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
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricTeal
import com.matox.nexcore.ui.theme.TextPrimary

/**
 * Sticky bottom action button — "Sensor & Privacy Settings".
 *
 * Gradient pill with a teal → cyan accent. The host wires the
 * `onClick` callback to launch the system intent. The helper
 * [rememberSensorDetailsLauncher] returns a no-fail lambda
 * that tries the high-fidelity target first and falls back to
 * the app's details page if the user has restricted that
 * activity (very common on locked-down enterprise devices).
 */
@Composable
fun SensorDetailsButton(
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
                ambientColor = MetricTeal.copy(alpha = 0.30f),
                spotColor = MetricTeal.copy(alpha = 0.40f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MetricTeal, MetricCyan),
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
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sensor & Privacy Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = TextPrimary,
            )
        }
    }
}

/**
 * Compose helper that returns a click-handler resolving to one of:
 *  1. `Settings.ACTION_PRIVACY_SETTINGS` — the system privacy
 *     page, on Android 12+ (API 31+).
 *  2. `Settings.ACTION_SETTINGS` — the system settings root,
 *     on older releases.
 *  3. `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` for the
 *     current package — the universal fallback. This always
 *     resolves, even on AOSP-stripped devices.
 *
 * Wrapped in `runCatching` + per-step try/catch so a locked-down
 * device can never throw back to the host. The lambda returns
 * a [LaunchResult] so the screen can decide whether to show a
 * snackbar ("Opened privacy settings") or fall back to the
 * snackbar message ("Opened app details").
 */
@Composable
fun rememberSensorDetailsLauncher(): () -> LaunchResult {
    val context = LocalContext.current
    return {
        launchSensorDetails(context)
    }
}

/**
 * Result of a [rememberSensorDetailsLauncher] click. The screen
 * can show a contextual snackbar using [LaunchResult.message].
 */
enum class LaunchTarget {
    PRIVACY,
    SYSTEM,
    APP_DETAILS,
}

data class LaunchResult(
    val target: LaunchTarget,
    val launched: Boolean,
    val message: String,
)

/**
 * Pure-Kotlin helper used by both [rememberSensorDetailsLauncher]
 * and previews/tests. Order of preference:
 *  1. `ACTION_PRIVACY_SETTINGS` (API 31+)
 *  2. `ACTION_SETTINGS` (older devices)
 *  3. `ACTION_APPLICATION_DETAILS_SETTINGS` (always works)
 */
fun launchSensorDetails(context: Context): LaunchResult {
    val pkg = context.packageName

    // 1. Privacy settings — only on Android 12+.
    val privacy: Intent? = runCatching {
        Intent(Settings.ACTION_PRIVACY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.getOrNull()
    if (privacy != null && tryStart(context, privacy)) {
        return LaunchResult(
            target = LaunchTarget.PRIVACY,
            launched = true,
            message = "Opening system privacy settings…",
        )
    }

    // 2. Settings root — older Android releases, also useful when
    //    OEM ROMs strip privacy settings entirely.
    val system: Intent? = runCatching {
        Intent(Settings.ACTION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.getOrNull()
    if (system != null && tryStart(context, system)) {
        return LaunchResult(
            target = LaunchTarget.SYSTEM,
            launched = true,
            message = "Opening system settings…",
        )
    }

    // 3. App details — always works.
    val details: Intent? = runCatching {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", pkg, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.getOrNull()
    if (details != null && tryStart(context, details)) {
        return LaunchResult(
            target = LaunchTarget.APP_DETAILS,
            launched = true,
            message = "Opening app details…",
        )
    }

    // Shouldn't happen — `ACTION_APPLICATION_DETAILS_SETTINGS` is
    // a public system activity. We still return a non-launched
    // result so the host can show a graceful snackbar.
    return LaunchResult(
        target = LaunchTarget.APP_DETAILS,
        launched = false,
        message = "Couldn't open settings — try again from the launcher",
    )
}

/**
 * Try to start the given intent. Wraps the SecurityException /
 * ActivityNotFoundException paths so a single locked-down
 * activity doesn't abort the cascade.
 */
private fun tryStart(context: Context, intent: Intent): Boolean = try {
    context.startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}
