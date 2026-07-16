package com.matox.nexcore.presentation.datausage

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.AppDataUsage
import com.matox.nexcore.domain.model.DataUsageSnapshot
import com.matox.nexcore.presentation.datausage.state.DataUsageUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = DataUsageSnapshot(
    mobileRxBytes = 24L * 1024 * 1024,
    mobileTxBytes = 8L * 1024 * 1024,
    wifiRxBytes = 312L * 1024 * 1024,
    wifiTxBytes = 5L * 1024 * 1024,
    perApp = listOf(
        AppDataUsage(
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            mobileRxBytes = 18L * 1024 * 1024,
            mobileTxBytes = 1L * 1024 * 1024,
            wifiRxBytes = 220L * 1024 * 1024,
            wifiTxBytes = 1L * 1024 * 1024,
        ),
        AppDataUsage(
            packageName = "com.whatsapp",
            displayName = "WhatsApp",
            mobileRxBytes = 2L * 1024 * 1024,
            mobileTxBytes = 3L * 1024 * 1024,
            wifiRxBytes = 48L * 1024 * 1024,
            wifiTxBytes = 12L * 1024 * 1024,
        ),
        AppDataUsage(
            packageName = "com.instagram.android",
            displayName = "Instagram",
            mobileRxBytes = 4L * 1024 * 1024,
            mobileTxBytes = 2L * 1024 * 1024,
            wifiRxBytes = 32L * 1024 * 1024,
            wifiTxBytes = 8L * 1024 * 1024,
        ),
    ),
    hasPermission = true,
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun DataUsagePreview() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        DataUsageContent(
            state = DataUsageUiState.Success(
                snapshot = PreviewSnapshot,
                appIcons = emptyMap(),
            ),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}
