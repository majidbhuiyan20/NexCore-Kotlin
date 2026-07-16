package com.matox.nexcore.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.appmanager.AppManagerScreen
import com.matox.nexcore.presentation.dashboard.DashboardScreen
import com.matox.nexcore.presentation.files.FilesScreen
import com.matox.nexcore.presentation.phoneinfo.PhoneInfoScreen
import com.matox.nexcore.presentation.ram.RamScreen
import com.matox.nexcore.presentation.settings.SettingsScreen
import com.matox.nexcore.presentation.storageanalyzer.StorageAnalyzerScreen

/** Single navigation model for the app. Adding a new destination is
 *  just appending a sealed-class entry + a case in `AppShell`. */
sealed class Screen {
    data object Home : Screen()
    data object StorageAnalyzer : Screen()
    data object AppManager : Screen()
    data object PhoneInfo : Screen()
    data object RamDetail : Screen()
    data object Files : Screen()
    data object Settings : Screen()
}

private val ScreenSaver: Saver<Screen, String> = Saver(
    save = { value ->
        when (value) {
            is Screen.Home -> "home"
            is Screen.StorageAnalyzer -> "storage_analyzer"
            is Screen.AppManager -> "app_manager"
            is Screen.PhoneInfo -> "phone_info"
            is Screen.RamDetail -> "ram_detail"
            is Screen.Files -> "files"
            is Screen.Settings -> "settings"
        }
    },
    restore = { saved ->
        when (saved) {
            "storage_analyzer" -> Screen.StorageAnalyzer
            "app_manager" -> Screen.AppManager
            "phone_info" -> Screen.PhoneInfo
            "ram_detail" -> Screen.RamDetail
            "files" -> Screen.Files
            "settings" -> Screen.Settings
            else -> Screen.Home
        }
    },
)

/**
 * Routes a bottom-nav tap to its destination screen. Centralised here
 * so every screen wires the same mapping and the active-highlight
 * stays in sync across the app.
 */
private fun navigateForNav(
    item: BottomNavItem,
    onHome: () -> Unit,
    onFiles: () -> Unit,
    onApps: () -> Unit,
    onSettings: () -> Unit,
) {
    when (item.id) {
        "nav_home" -> onHome()
        "nav_files" -> onFiles()
        "nav_apps" -> onApps()
        "nav_settings" -> onSettings()
    }
}

/**
 * Root shell that owns a saveable screen state and switches between
 * the dashboard, storage analyzer, app manager, phone info, files,
 * and settings.
 *
 * No Compose Navigation dependency — the user opted for the
 * lightweight sealed-class approach.
 *
 * Bottom-nav wiring (every screen, every pill):
 *  - Home pill → Home.
 *  - Files pill → Files (folder-browse screen).
 *  - Apps pill → AppManager (center FAB).
 *  - Settings pill → Settings.
 *
 * Quick-action wiring (Home only):
 *  - qa_storage → StorageAnalyzer
 *  - qa_apps → AppManager
 *  - qa_phone → PhoneInfo
 *
 * Sub-screen back arrows pop to Home.
 */
@Composable
fun AppShell(
    modifier: Modifier = Modifier,
) {
    var screen by rememberSaveable(stateSaver = ScreenSaver) {
        mutableStateOf<Screen>(Screen.Home)
    }

    val homeClick: () -> Unit = { screen = Screen.Home }
    val filesClick: () -> Unit = { screen = Screen.Files }
    val appsClick: () -> Unit = { screen = Screen.AppManager }
    val settingsClick: () -> Unit = { screen = Screen.Settings }

    when (val current = screen) {
        is Screen.Home -> DashboardScreen(
            modifier = modifier,
            onNavigateToStorageAnalyzer = { screen = Screen.StorageAnalyzer },
            onNavigateToAppManager = { screen = Screen.AppManager },
            onNavigateToPhoneInfo = { screen = Screen.PhoneInfo },
            onNavigateToRamDetail = { screen = Screen.RamDetail },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
        is Screen.StorageAnalyzer -> StorageAnalyzerScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
        is Screen.AppManager -> AppManagerScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
        is Screen.PhoneInfo -> PhoneInfoScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
        is Screen.RamDetail -> RamScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
        is Screen.Files -> FilesScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
        is Screen.Settings -> SettingsScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item ->
                navigateForNav(
                    item = item,
                    onHome = homeClick,
                    onFiles = filesClick,
                    onApps = appsClick,
                    onSettings = settingsClick,
                )
            },
        )
    }
}