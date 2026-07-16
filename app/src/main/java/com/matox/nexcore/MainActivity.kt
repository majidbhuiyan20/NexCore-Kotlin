package com.matox.nexcore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.matox.nexcore.presentation.AppShell
import com.matox.nexcore.ui.theme.NexCoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw edge-to-edge so the system bars are transparent and our
        // dark gradient shows through.
        enableEdgeToEdge()
        // Status bar / navigation bar icons are usually rendered dark by
        // default on Android. We're a dark theme (#0B0F1A), so explicitly
        // mark them as "light icons on dark background" — otherwise the
        // network + battery glyphs at the top blend into the white
        // scaffold default and disappear.
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
        setContent {
            NexCoreTheme {
                // AppShell owns a saveable screen state and switches
                // between Dashboard and Storage Analyzer. Each screen
                // renders its own gradient + fixed top/bottom bars,
                // and pads itself for the system status bar.
                AppShell(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                )
            }
        }
    }
}