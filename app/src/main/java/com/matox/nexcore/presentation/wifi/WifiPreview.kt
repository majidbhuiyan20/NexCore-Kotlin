package com.matox.nexcore.presentation.wifi

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.AppTrafficRow
import com.matox.nexcore.domain.model.WifiConnection
import com.matox.nexcore.domain.model.WifiIpInfo
import com.matox.nexcore.domain.model.WifiSecurity
import com.matox.nexcore.domain.model.WifiSnapshot
import com.matox.nexcore.presentation.wifi.state.WifiUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = WifiSnapshot(
    connection = WifiConnection(
        ssid = "NexCore-HQ-5G",
        bssid = "AA:BB:CC:11:22:33",
        rssiDbm = -54,
        linkSpeedMbps = 866,
        frequencyMhz = 5220,
        channel = 44,
        security = WifiSecurity.WPA3_SAE,
        signalPercent = 78,
    ),
    ip = WifiIpInfo(
        localIp = "192.168.1.42",
        gateway = "192.168.1.1",
        dns1 = "1.1.1.1",
        dns2 = "1.0.0.1",
        dhcpServer = "192.168.1.1",
    ),
    publicIp = "203.0.113.42",
    appTraffic = listOf(
        AppTrafficRow(
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            rxBytes = 384_512_000L,
            txBytes = 6_840_000L,
        ),
        AppTrafficRow(
            packageName = "com.whatsapp",
            displayName = "WhatsApp",
            rxBytes = 92_160_000L,
            txBytes = 18_440_000L,
        ),
        AppTrafficRow(
            packageName = "com.spotify.music",
            displayName = "Spotify",
            rxBytes = 73_728_000L,
            txBytes = 1_440_000L,
        ),
        AppTrafficRow(
            packageName = "com.google.android.gms",
            displayName = "Google Play Services",
            rxBytes = 41_120_000L,
            txBytes = 22_880_000L,
        ),
    ),
    cellularType = "—",
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun WifiPreview() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        WifiContent(
            state = WifiUiState.Success(
                snapshot = PreviewSnapshot,
                appIcons = emptyMap(),
            ),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}