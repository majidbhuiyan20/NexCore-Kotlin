package com.matox.nexcore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import com.matox.nexcore.presentation.AppShell
import com.matox.nexcore.ui.theme.NexCoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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