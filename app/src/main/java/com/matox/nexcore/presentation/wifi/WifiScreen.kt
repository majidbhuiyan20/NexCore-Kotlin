package com.matox.nexcore.presentation.wifi

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.wifi.components.WifiAppTrafficCard
import com.matox.nexcore.presentation.wifi.components.WifiConnectionCard
import com.matox.nexcore.presentation.wifi.components.WifiDetailsButton
import com.matox.nexcore.presentation.wifi.components.WifiHeroCard
import com.matox.nexcore.presentation.wifi.components.WifiNetworkInfoCard
import com.matox.nexcore.presentation.wifi.components.WifiPublicIpCard
import com.matox.nexcore.presentation.wifi.components.WifiTopBar
import com.matox.nexcore.presentation.wifi.state.WifiUiState
import com.matox.nexcore.presentation.wifi.viewmodel.WifiViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.TextPrimary

/** Bottom padding under the scrollable content so it doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 110.dp

/** Static bottom-nav used while on the WiFi detail screen — Home pill returns the user. */
private val WifiBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = true),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun WifiScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val viewModel: WifiViewModel = viewModel(
        factory = WifiViewModel.Factory(
            repository = AppContainer.wifiRepository,
            iconLoader = AppContainer.appIconLoader,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarMessage = null
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(msg)
    }

    WifiContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
        onRefreshClick = { viewModel.refresh() },
        onWifiDetailsClick = {
            snackbarMessage = "Opening system WiFi settings…"
            runCatching {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        },
    )
}

/**
 * Render the WiFi detail screen with a fixed top bar + scrollable
 * body + floating dock layout, matching the RAM / Battery / App
 * Manager pattern.
 */
@Composable
internal fun WifiContent(
    state: WifiUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onRefreshClick: () -> Unit = {},
    onWifiDetailsClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGradientTop,
                        BackgroundGradientBottom,
                    ),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WifiTopBar(
                onBackClick = onBack,
                onRefreshClick = onRefreshClick,
            )

            when (state) {
                WifiUiState.Loading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    LoadingState()
                }
                is WifiUiState.Error -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    ErrorState(state.message)
                }
                is WifiUiState.Success -> {
                    val snap = state.snapshot

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = contentPadding.calculateTopPadding())
                            .padding(bottom = BottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        WifiHeroCard(
                            connection = snap.connection,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        WifiConnectionCard(
                            connection = snap.connection,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        WifiNetworkInfoCard(
                            ipInfo = snap.ip,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        WifiPublicIpCard(
                            publicIp = snap.publicIp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        WifiAppTrafficCard(
                            apps = snap.appTraffic,
                            appIcons = state.appIcons,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        WifiDetailsButton(
                            onClick = onWifiDetailsClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            DashboardBottomBar(
                items = WifiBottomNav,
                onItemClick = onBottomNavClick,
            )
        }

        if (snackbarHostState != null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MetricBlue,
                    contentColor = TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "WiFi unavailable",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}