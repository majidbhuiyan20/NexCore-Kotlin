package com.matox.nexcore.presentation.cpu

import android.content.Intent
import android.os.Build
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
import com.matox.nexcore.domain.model.CpuAppUsage
import com.matox.nexcore.presentation.cpu.components.CpuCoresCard
import com.matox.nexcore.presentation.cpu.components.CpuDetailsButton
import com.matox.nexcore.presentation.cpu.components.CpuHeroCard
import com.matox.nexcore.presentation.cpu.components.CpuHistoryChart
import com.matox.nexcore.presentation.cpu.components.CpuMetricsCard
import com.matox.nexcore.presentation.cpu.components.CpuTopAppsCard
import com.matox.nexcore.presentation.cpu.components.CpuTopBar
import com.matox.nexcore.presentation.cpu.state.CpuUiState
import com.matox.nexcore.presentation.cpu.viewmodel.CpuViewModel
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.TextPrimary

/** Bottom padding so scroll content doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 110.dp

/** Static bottom-nav used while on the CPU Monitor screen. */
private val CpuBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = true),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun CpuScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val viewModel: CpuViewModel = viewModel(
        factory = CpuViewModel.Factory(
            repository = AppContainer.cpuRepository,
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

    CpuContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
        onAppClick = { app ->
            snackbarMessage = "CPU profile for ${app.displayName} — coming soon"
        },
        onDeveloperOptionsClick = {
            snackbarMessage = "Opening developer options…"
            runCatching {
                // ACTION_APPLICATION_DEVELOPER_SETTINGS is in the SDK but
                // @hide on some build variants — fall back to the
                // public ACTION_DEVICE_INFO_SETTINGS (About phone, where
                // the user can tap Build Number 7 times to unlock
                // developer options if hidden).
                val action = runCatching {
                    Settings::class.java
                        .getField("ACTION_APPLICATION_DEVELOPER_SETTINGS")
                        .get(null) as? String
                }.getOrNull() ?: Settings.ACTION_DEVICE_INFO_SETTINGS
                val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        },
    )
}

/**
 * Render the CPU Monitor screen with a fixed top bar + scrollable
 * body + floating dock layout, matching the RAM / Battery / Storage
 * pattern.
 */
@Composable
internal fun CpuContent(
    state: CpuUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAppClick: (CpuAppUsage) -> Unit = {},
    onDeveloperOptionsClick: () -> Unit = {},
) {
    val architecture: String = remember { Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown" }
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
            CpuTopBar(onBackClick = onBack)

            when (state) {
                CpuUiState.Loading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    LoadingState()
                }
                is CpuUiState.Error -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    ErrorState(state.message)
                }
                is CpuUiState.Success -> {
                    val snap = state.snapshot

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = contentPadding.calculateTopPadding())
                            .padding(bottom = BottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        CpuHeroCard(
                            overallPercent = snap.overallPercent,
                            coreCount = snap.coreCount,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        CpuMetricsCard(
                            coreCount = snap.coreCount,
                            socModel = snap.socModel,
                            architecture = architecture,
                            uptimeMs = snap.uptimeMs,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        CpuCoresCard(
                            perCoreFrequenciesMhz = snap.perCoreFrequenciesMhz,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        CpuHistoryChart(
                            historyPercent = snap.historyPercent,
                            currentPercent = snap.overallPercent,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        CpuTopAppsCard(
                            apps = snap.topApps,
                            appIcons = state.appIcons,
                            onAppClick = onAppClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        CpuDetailsButton(
                            onClick = onDeveloperOptionsClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            DashboardBottomBar(
                items = CpuBottomNav,
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
                    containerColor = MetricOrange,
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
            text = "CPU unavailable",
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
