package com.matox.nexcore.presentation.appmanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.AppFilterTab
import com.matox.nexcore.domain.model.AppIconRef
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.AppSort
import com.matox.nexcore.presentation.appmanager.state.AppManagerUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewApps = listOf(
    AppInfo(
        packageName = "com.whatsapp",
        displayName = "WhatsApp",
        versionName = "2.24.10.78",
        categoryLabel = "Communication",
        sizeBytes = 122L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis() - 86_400_000L * 14,
        isSystem = false,
        hasLauncher = true,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.spotify.music",
        displayName = "Spotify",
        versionName = "8.9.42.575",
        categoryLabel = "Music & Audio",
        sizeBytes = 89L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis() - 86_400_000L * 14,
        isSystem = false,
        hasLauncher = true,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.google.android.youtube",
        displayName = "YouTube",
        versionName = "19.22.33",
        categoryLabel = "Video Players",
        sizeBytes = 147L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis(),
        isSystem = false,
        hasLauncher = true,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.android.chrome",
        displayName = "Chrome",
        versionName = "125.0.6422.113",
        categoryLabel = "Tools",
        sizeBytes = 79L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis(),
        isSystem = false,
        hasLauncher = true,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.google.android.apps.photos",
        displayName = "Google Photos",
        versionName = "6.85.0.634770540",
        categoryLabel = "Photography",
        sizeBytes = 215L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis() - 86_400_000L,
        isSystem = false,
        hasLauncher = true,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.google.android.webview",
        displayName = "Android System WebView",
        versionName = "125.0.6422.113",
        categoryLabel = "System",
        sizeBytes = 77L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis() - 86_400_000L * 7,
        isSystem = true,
        hasLauncher = false,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.android.calendar",
        displayName = "com.android.calendar",
        versionName = "12.5.7-5099068",
        categoryLabel = "System",
        sizeBytes = 46L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis() - 86_400_000L * 12,
        isSystem = true,
        hasLauncher = false,
        iconRef = AppIconRef.Pending,
    ),
    AppInfo(
        packageName = "com.android.systemui",
        displayName = "System UI",
        versionName = "12",
        categoryLabel = "System",
        sizeBytes = 35L * 1024 * 1024,
        lastUpdatedEpochMs = System.currentTimeMillis() - 86_400_000L * 12,
        isSystem = true,
        hasLauncher = false,
        iconRef = AppIconRef.Pending,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun AppManagerPreview() {
    NexCoreTheme {
        AppManagerContent(
            state = AppManagerUiState.Success(
                filtered = PreviewApps,
                allApps = PreviewApps,
                search = "",
                sort = AppSort.NAME_ASC,
                tab = AppFilterTab.ALL,
            ),
            onBack = {},
            onSearchChange = {},
            onSortSelected = {},
            onTabSelected = {},
            onOpen = {},
            onInfo = {},
            onUninstall = {},
            onDisable = {},
            onBottomNavClick = {},
        )
    }
}