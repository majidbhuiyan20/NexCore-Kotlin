package com.matox.nexcore.presentation.battery

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.matox.nexcore.domain.model.BatteryAppUsage
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.battery.components.BatteryDetailsButton
import com.matox.nexcore.presentation.battery.components.BatteryHealthCard
import com.matox.nexcore.presentation.battery.components.BatteryHeroCard
import com.matox.nexcore.presentation.battery.components.BatteryLevelChart
import com.matox.nexcore.presentation.battery.components.BatteryMetricsCard
import com.matox.nexcore.presentation.battery.components.BatteryRecommendationsCard
import com.matox.nexcore.presentation.battery.components.BatteryTempChart
import com.matox.nexcore.presentation.battery.components.BatteryTimeEstimatesCard
import com.matox.nexcore.presentation.battery.components.BatteryTopAppsCard
import com.matox.nexcore.presentation.battery.components.BatteryTopBar
import com.matox.nexcore.presentation.battery.components.ChargingInfoCard
import com.matox.nexcore.presentation.battery.state.BatteryUiState
import com.matox.nexcore.presentation.battery.viewmodel.BatteryViewModel
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.TextPrimary

/** Bottom padding under the scrollable content so it doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 110.dp

/** Static bottom-nav used while on the Battery Monitor screen — Home pill returns the user. */
private val BatteryBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = true),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun BatteryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val viewModel: BatteryViewModel = viewModel(
        factory = BatteryViewModel.Factory(
            repository = AppContainer.batteryRepository,
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

    BatteryContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
        onAppClick = { app ->
            snackbarMessage = "Battery profile for ${app.displayName} — coming soon"
        },
        onBatteryDetailsClick = {
            snackbarMessage = "Opening system battery details…"
            runCatching {
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        },
    )
}

/**
 * Render the Battery Monitor screen with a fixed top bar + scrollable
 * body + floating dock layout, matching the RAM / Storage / App Manager
 * pattern.
 */
@Composable
internal fun BatteryContent(
    state: BatteryUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAppClick: (BatteryAppUsage) -> Unit = {},
    onBatteryDetailsClick: () -> Unit = {},
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
            BatteryTopBar(onBackClick = onBack)

            when (state) {
                BatteryUiState.Loading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    LoadingState()
                }
                is BatteryUiState.Error -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    ErrorState(state.message)
                }
                is BatteryUiState.Success -> {
                    val snap = state.snapshot
                    val reading = snap.reading

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = contentPadding.calculateTopPadding())
                            .padding(bottom = BottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        BatteryHeroCard(
                            reading = reading,
                            healthLabel = snap.health.healthLabel,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryMetricsCard(
                            temperatureC = reading.temperatureC,
                            voltageMv = reading.voltageMv,
                            currentMa = reading.currentNowMa,
                            capacityMah = snap.batteryCapacityMah,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryLevelChart(
                            historyPercent = snap.historyPercent,
                            currentLevelPercent = reading.levelPercent,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryTempChart(
                            historyTempC = snap.historyTempC,
                            currentTempC = reading.temperatureC,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        ChargingInfoCard(
                            charging = snap.charging,
                            status = reading.status,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryTimeEstimatesCard(
                            capacityMah = snap.batteryCapacityMah,
                            currentLevelPercent = reading.levelPercent,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryTopAppsCard(
                            apps = snap.topApps,
                            appIcons = state.appIcons,
                            onAppClick = onAppClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryHealthCard(
                            health = snap.health,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryRecommendationsCard(
                            insights = snap.insights,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        BatteryDetailsButton(
                            onClick = onBatteryDetailsClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            DashboardBottomBar(
                items = BatteryBottomNav,
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
                    containerColor = NexCoreGreen,
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
            text = "Battery unavailable",
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
