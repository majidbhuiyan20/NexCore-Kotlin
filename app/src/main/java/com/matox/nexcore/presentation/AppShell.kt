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
import com.matox.nexcore.presentation.phoneinfo.PhoneInfoScreen
import com.matox.nexcore.presentation.storageanalyzer.StorageAnalyzerScreen

/** Single navigation model for the app. Adding a new destination is
 *  just appending a sealed-class entry + a case in `AppShell`. */
sealed class Screen {
    data object Home : Screen()
    data object StorageAnalyzer : Screen()
    data object AppManager : Screen()
    data object PhoneInfo : Screen()
}

private val ScreenSaver: Saver<Screen, String> = Saver(
    save = { value ->
        when (value) {
            is Screen.Home -> "home"
            is Screen.StorageAnalyzer -> "storage_analyzer"
            is Screen.AppManager -> "app_manager"
            is Screen.PhoneInfo -> "phone_info"
        }
    },
    restore = { saved ->
        when (saved) {
            "storage_analyzer" -> Screen.StorageAnalyzer
            "app_manager" -> Screen.AppManager
            "phone_info" -> Screen.PhoneInfo
            else -> Screen.Home
        }
    },
)

/**
 * Root shell that owns a saveable screen state and switches between
 * the dashboard, storage analyzer, app manager, and phone info.
 * No Compose Navigation dependency — the user opted for the
 * lightweight sealed-class approach.
 *
 * Wires:
 *  - Home quick action `qa_storage` → push StorageAnalyzer.
 *  - Home quick action `qa_apps` → push AppManager.
 *  - Home quick action `qa_phone` → push PhoneInfo.
 *  - Home bottom nav "Apps" pill (also the qa_apps centre FAB)
 *    → push AppManager.
 *  - Any sub-screen back arrow → pop to Home.
 *  - StorageAnalyzer / AppManager / PhoneInfo "Home" nav pill →
 *    pop to Home.
 */
@Composable
fun AppShell(
    modifier: Modifier = Modifier,
) {
    var screen by rememberSaveable(stateSaver = ScreenSaver) {
        mutableStateOf<Screen>(Screen.Home)
    }

    when (val current = screen) {
        is Screen.Home -> DashboardScreen(
            modifier = modifier,
            onNavigateToStorageAnalyzer = { screen = Screen.StorageAnalyzer },
            onNavigateToAppManager = { screen = Screen.AppManager },
            onNavigateToPhoneInfo = { screen = Screen.PhoneInfo },
            onBottomNavClick = { item: BottomNavItem ->
                if (item.id == "nav_apps") screen = Screen.AppManager
            },
        )
        is Screen.StorageAnalyzer -> StorageAnalyzerScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item: BottomNavItem ->
                when (item.id) {
                    "nav_home" -> screen = Screen.Home
                    "nav_apps" -> screen = Screen.AppManager
                }
            },
        )
        is Screen.AppManager -> AppManagerScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            onBottomNavClick = { item: BottomNavItem ->
                when (item.id) {
                    "nav_home" -> screen = Screen.Home
                }
            },
        )
        is Screen.PhoneInfo -> PhoneInfoScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
        )
    }
}
