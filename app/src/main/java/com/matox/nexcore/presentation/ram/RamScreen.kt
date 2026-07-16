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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.ram.components.RamBreakdownCard
import com.matox.nexcore.presentation.ram.components.RamFreeUpCard
import com.matox.nexcore.presentation.ram.components.RamHeroCard
import com.matox.nexcore.presentation.ram.components.RamHistoryChart
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

    RamContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onFreeUpClick = {
            // Stub action — Google discourages killBackgroundProcesses
            // because of FGS restrictions on Android 8+. Show a
            // confirmation snackbar so the UX is intact.
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        onClearCacheClick = {
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
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
    onFreeUpClick: () -> Unit = {},
    onClearCacheClick: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
                is RamUiState.Success -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = contentPadding.calculateTopPadding())
                        .padding(bottom = BottomContentPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    RamHeroCard(
                        snapshot = state.snapshot,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    RamHistoryChart(
                        history = state.snapshot.historyPercent,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    RamBreakdownCard(
                        snapshot = state.snapshot,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    RamTopAppsCard(
                        apps = state.snapshot.topApps,
                        totalRamMb = (state.snapshot.totalGb * 1024f).toLong(),
                        appIcons = state.appIcons,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    RamFreeUpCard(
                        onFreeUpClick = onFreeUpClick,
                        onClearCacheClick = onClearCacheClick,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
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