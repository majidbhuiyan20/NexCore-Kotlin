package com.matox.nexcore.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.dashboard.DashboardScreen
import com.matox.nexcore.presentation.storageanalyzer.StorageAnalyzerScreen

/** Single navigation model for the app. Adding a new destination is
 *  just appending a sealed-class entry + a case in `AppShell`. */
sealed class Screen {
    data object Home : Screen()
    data object StorageAnalyzer : Screen()
}

private val ScreenSaver: Saver<Screen, String> = Saver(
    save = { value ->
        when (value) {
            is Screen.Home -> "home"
            is Screen.StorageAnalyzer -> "storage_analyzer"
        }
    },
    restore = { saved ->
        when (saved) {
            "storage_analyzer" -> Screen.StorageAnalyzer
            else -> Screen.Home
        }
    },
)

/**
 * Root shell that owns a saveable screen state and switches between
 * the dashboard and the storage analyzer. No Compose Navigation
 * dependency — the user opted for the lightweight sealed-class
 * approach.
 *
 * Wires:
 *  - Home quick action `qa_storage` → push StorageAnalyzer.
 *  - StorageAnalyzer back arrow / "Home" nav item → pop to Home.
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
        )
        is Screen.StorageAnalyzer -> StorageAnalyzerScreen(
            modifier = modifier,
            onBack = { screen = Screen.Home },
            // Hook for future deep links / bottom nav: pressing the
            // "Home" pill on the storage analyzer brings the user
            // back to the dashboard.
            onBottomNavClick = { item: BottomNavItem ->
                if (item.id == "nav_home") screen = Screen.Home
            },
        )
    }
}
