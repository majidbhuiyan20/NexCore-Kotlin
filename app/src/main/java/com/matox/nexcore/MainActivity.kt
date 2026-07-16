package com.matox.nexcore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import com.matox.nexcore.presentation.dashboard.DashboardScreen
import com.matox.nexcore.ui.theme.NexCoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexCoreTheme {
                // The dashboard renders its own gradient + fixed bottom
                // navigation bar, and pads itself for the system status
                // bar. No Scaffold needed.
                DashboardScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                )
            }
        }
    }
}