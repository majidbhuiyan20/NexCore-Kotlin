package com.matox.nexcore.presentation.battery

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.BatteryAppUsage
import com.matox.nexcore.domain.model.BatteryHealth
import com.matox.nexcore.domain.model.BatteryInsight
import com.matox.nexcore.domain.model.BatteryInsightIcon
import com.matox.nexcore.domain.model.BatteryReading
import com.matox.nexcore.domain.model.BatterySnapshot
import com.matox.nexcore.domain.model.BatteryStatus
import com.matox.nexcore.domain.model.ChargingInfo
import com.matox.nexcore.domain.model.ChargingPattern
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.PlugType
import com.matox.nexcore.presentation.battery.state.BatteryUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = BatterySnapshot(
    reading = BatteryReading(
        levelPercent = 82,
        status = BatteryStatus.CHARGING,
        plugType = PlugType.AC,
        isFastCharging = true,
        temperatureC = 33.6f,
        voltageMv = 4320,
        currentNowMa = 1250,
        technology = "Li-ion",
    ),
    historyPercent = listOf(
        68, 70, 71, 72, 73, 74, 75, 76, 76, 77,
        78, 78, 79, 80, 80, 81, 81, 82, 82, 82,
        81, 81, 82, 82, 83, 82, 82, 82, 82, 82,
    ),
    historyTempC = listOf(
        31.0f, 31.4f, 31.8f, 32.0f, 32.2f, 32.4f, 32.5f, 32.8f, 33.0f, 33.2f,
        33.4f, 33.5f, 33.6f, 33.7f, 33.6f, 33.5f, 33.4f, 33.3f, 33.4f, 33.5f,
        33.6f, 33.7f, 33.6f, 33.4f, 33.5f, 33.6f, 33.7f, 33.6f, 33.5f, 33.6f,
    ),
    charging = ChargingInfo(
        chargingStartedMs = System.currentTimeMillis() - 84L * 60_000L,
        lastFullChargeMs = System.currentTimeMillis() - 19L * 60 * 60_000L,
        estimatedTimeToFullMin = 47,
        durationSoFarMin = 84,
    ),
    health = BatteryHealth(
        cyclesEstimate = 284,
        wearPercent = 12,
        chargingPattern = ChargingPattern.OVERNIGHT,
        healthScore = 92,
        healthLabel = "Excellent",
    ),
    topApps = listOf(
        BatteryAppUsage("com.google.android.youtube", "YouTube", 18.4f, 920f),
        BatteryAppUsage("com.whatsapp", "WhatsApp", 12.2f, 610f),
        BatteryAppUsage("com.instagram.android", "Instagram", 9.7f, 485f),
        BatteryAppUsage("com.zhiliaoapp.musically", "TikTok", 8.1f, 405f),
        BatteryAppUsage("com.google.android.gms", "Google Play Services", 6.4f, 320f),
    ),
    insights = listOf(
        BatteryInsight(
            accent = MetricAccent.CYAN,
            title = "Fast charging detected",
            subtitle = "Charging at 1250 mA — device supports rapid top-ups.",
            iconKey = BatteryInsightIcon.FAST_CHARGE,
        ),
        BatteryInsight(
            accent = MetricAccent.ORANGE,
            title = "Close battery-heavy apps",
            subtitle = "YouTube and the next two are using 40% combined.",
            iconKey = BatteryInsightIcon.APPS,
        ),
        BatteryInsight(
            accent = MetricAccent.RED,
            title = "Reduce thermal load",
            subtitle = "Battery is 33°C — close heavy apps and let it cool.",
            iconKey = BatteryInsightIcon.COOL,
        ),
    ),
    batteryCapacityMah = 5000,
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun BatteryPreview() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        BatteryContent(
            state = BatteryUiState.Success(
                snapshot = PreviewSnapshot,
                appIcons = emptyMap(),
            ),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}
