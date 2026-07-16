package com.matox.nexcore.presentation.cpu

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.CpuAppUsage
import com.matox.nexcore.domain.model.CpuSnapshot
import com.matox.nexcore.presentation.cpu.state.CpuUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = CpuSnapshot(
    overallPercent = 42,
    perCoreFrequenciesMhz = listOf(
        1800, 2200, 2400, 2600, 2800, 3000, 3200, 3400,
    ),
    coreCount = 8,
    socModel = "Snapdragon 8 Gen 2",
    uptimeMs = System.currentTimeMillis() - 12L * 3600 * 1000,
    historyPercent = listOf(
        15, 18, 22, 25, 28, 31, 35, 38, 41, 44,
        48, 50, 52, 49, 47, 44, 42, 41, 40, 42,
        45, 47, 49, 51, 48, 46, 44, 42, 41, 42,
    ),
    topApps = listOf(
        CpuAppUsage("com.google.android.youtube", "YouTube", 24.8f),
        CpuAppUsage("com.whatsapp", "WhatsApp", 16.5f),
        CpuAppUsage("com.instagram.android", "Instagram", 13.2f),
        CpuAppUsage("com.zhiliaoapp.musically", "TikTok", 11.0f),
        CpuAppUsage("com.google.android.gms", "Google Play Services", 8.4f),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun CpuPreview() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        CpuContent(
            state = CpuUiState.Success(
                snapshot = PreviewSnapshot,
                appIcons = emptyMap(),
            ),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}
