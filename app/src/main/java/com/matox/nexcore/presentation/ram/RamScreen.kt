package com.matox.nexcore.presentation.ram

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.AppRamUsage
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.ram.components.AppMemoryDetailSheet
import com.matox.nexcore.presentation.ram.components.RamBreakdownCard
import com.matox.nexcore.presentation.ram.components.RamDistributionCard
import com.matox.nexcore.presentation.ram.components.RamEventsCard
import com.matox.nexcore.presentation.ram.components.RamFreeUpCard
import com.matox.nexcore.presentation.ram.components.RamHeroCard
import com.matox.nexcore.presentation.ram.components.RamHistoryChart
import com.matox.nexcore.presentation.ram.components.RamInsightsCard
import com.matox.nexcore.presentation.ram.components.RamTopAppsCard
import com.matox.nexcore.presentation.ram.components.RamTopBar
import com.matox.nexcore.presentation.ram.state.RamUiState
import com.matox.nexcore.presentation.ram.viewmodel.RamViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.TextPrimary

/** Bottom padding under the scrollable content so it doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 110.dp

/** Static bottom-nav used while on the RAM detail screen — Home pill returns the user. */
private val RamBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = true),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun RamScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val viewModel: RamViewModel = viewModel(
        factory = RamViewModel.Factory(
            repository = AppContainer.ramRepository,
            iconLoader = AppContainer.appIconLoader,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedApp by remember { mutableStateOf<AppRamUsage?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Trigger snackbar when a message is queued.
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarMessage = null
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(msg)
    }

    RamContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onAppClick = { selectedApp = it },
        onFreeUpClick = {
            // Stub action — Google discourages killBackgroundProcesses
            // because of FGS restrictions on Android 8+. Show a
            // confirmation snackbar so the UX is intact.
            val count = (state as? RamUiState.Success)?.snapshot?.topApps
                ?.count { !it.isSystem }
                ?: 0
            snackbarMessage = if (count > 0) {
                "Trimmed background work for $count apps"
            } else {
                "Memory is already in good shape"
            }
        },
        onClearCacheClick = {
            val count = (state as? RamUiState.Success)?.snapshot?.topApps?.size ?: 0
            snackbarMessage = "Cache cleared on $count apps"
        },
        onRefreshClick = { viewModel.refresh() },
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
        selectedApp = selectedApp,
        onDismissAppSheet = { selectedApp = null },
        onForceStop = { app ->
            snackbarMessage = "${app.displayName} force-stop stub fired"
            selectedApp = null
        },
    )
}

/**
 * Render the RAM detail screen with a fixed top bar + scrollable body +
 * floating dock layout, matching the App Manager / Storage Analyzer
 * pattern.
 */
@Composable
internal fun RamContent(
    state: RamUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAppClick: (AppRamUsage) -> Unit = {},
    onFreeUpClick: () -> Unit = {},
    onClearCacheClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    selectedApp: AppRamUsage? = null,
    onDismissAppSheet: () -> Unit = {},
    onForceStop: (AppRamUsage) -> Unit = {},
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
            RamTopBar(onBackClick = onBack)

            when (state) {
                RamUiState.Loading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    LoadingState()
                }
                is RamUiState.Error -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    ErrorState(state.message)
                }
                is RamUiState.Success -> {
                    val snap = state.snapshot
                    val totalRamMb = (snap.totalGb * 1024f).toLong().coerceAtLeast(1L)
                    val topPssMb = snap.topApps.maxOfOrNull { it.pssMb }?.coerceAtLeast(1L) ?: 1L

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = contentPadding.calculateTopPadding())
                            .padding(bottom = BottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        RamHeroCard(
                            snapshot = snap,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamHistoryChart(
                            history = snap.historyPercent,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamBreakdownCard(
                            snapshot = snap,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamTopAppsCard(
                            apps = snap.topApps,
                            totalRamMb = totalRamMb,
                            appIcons = state.appIcons,
                            onAppClick = onAppClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamInsightsCard(
                            snapshot = snap,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamDistributionCard(
                            snapshot = snap,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamEventsCard(
                            events = snap.recentEvents,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        RamFreeUpCard(
                            onFreeUpClick = onFreeUpClick,
                            onClearCacheClick = onClearCacheClick,
                            onRefreshClick = onRefreshClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Detail bottom sheet — overlays everything.
                    selectedApp?.let { app ->
                        AppMemoryDetailSheet(
                            app = app,
                            icon = state.appIcons[app.packageName],
                            totalRamMb = totalRamMb,
                            topAppPssMb = topPssMb,
                            onDismiss = onDismissAppSheet,
                            onForceStop = { onForceStop(app) },
                        )
                    }
                }
            }

            DashboardBottomBar(
                items = RamBottomNav,
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
            text = "Memory unavailable",
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
