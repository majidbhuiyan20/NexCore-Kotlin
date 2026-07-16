package com.matox.nexcore.presentation.ram

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.AppRamUsage
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.presentation.ram.state.RamUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = RamSnapshot(
    usedGb = 4.2f,
    totalGb = 8.0f,
    percent = 53,
    lowMemory = false,
    thresholdGb = 0.8f,
    cachedMb = 1450,
    buffersMb = 220,
    activeMb = 3100,
    inactiveMb = 1800,
    swapTotalMb = 4096,
    swapFreeMb = 3940,
    historyPercent = listOf(
        48, 50, 49, 51, 52, 54, 53, 55, 56, 54,
        52, 51, 50, 49, 50, 52, 54, 53, 55, 56,
        55, 54, 53, 52, 51, 50, 49, 50, 52, 53,
        54, 55, 56, 57, 56, 55, 54, 53, 52, 51,
        50, 51, 52, 53, 54, 55, 54, 53, 52, 53,
        52, 53, 54, 55, 54, 53, 52, 51, 52, 53,
    ),
    topApps = listOf(
        AppRamUsage(
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            pssMb = 412,
            privateDirtyMb = 312,
            isSystem = false,
        ),
        AppRamUsage(
            packageName = "com.whatsapp",
            displayName = "WhatsApp",
            pssMb = 286,
            privateDirtyMb = 198,
            isSystem = false,
        ),
        AppRamUsage(
            packageName = "com.instagram.android",
            displayName = "Instagram",
            pssMb = 244,
            privateDirtyMb = 170,
            isSystem = false,
        ),
        AppRamUsage(
            packageName = "com.android.systemui",
            displayName = "System UI",
            pssMb = 188,
            privateDirtyMb = 96,
            isSystem = true,
        ),
        AppRamUsage(
            packageName = "com.google.android.gms",
            displayName = "Google Play Services",
            pssMb = 165,
            privateDirtyMb = 78,
            isSystem = true,
        ),
        AppRamUsage(
            packageName = "com.zhiliaoapp.musically",
            displayName = "TikTok",
            pssMb = 142,
            privateDirtyMb = 102,
            isSystem = false,
        ),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun RamPreview() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        RamContent(
            state = RamUiState.Success(
                snapshot = PreviewSnapshot,
                appIcons = emptyMap(),
            ),
            onBack = {},
            onFreeUpClick = {},
            onClearCacheClick = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}