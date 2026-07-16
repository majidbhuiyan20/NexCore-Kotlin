package com.matox.nexcore.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.domain.model.InfoCardData
import com.matox.nexcore.domain.model.QuickAction
import com.matox.nexcore.domain.model.SystemMetric
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.dashboard.components.DashboardTopBar
import com.matox.nexcore.presentation.dashboard.components.DeviceHealthBanner
import com.matox.nexcore.presentation.dashboard.components.GreetingWithScoreSection
import com.matox.nexcore.presentation.dashboard.components.InfoCardsRow
import com.matox.nexcore.presentation.dashboard.components.MetricsRow
import com.matox.nexcore.presentation.dashboard.components.QuickActionsSection
import com.matox.nexcore.presentation.dashboard.state.DashboardUiState
import com.matox.nexcore.presentation.dashboard.viewmodel.DashboardViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.TextPrimary

/** Bottom padding under the scrollable content so it doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 120.dp

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(AppContainer.dashboardRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        state = state,
        modifier = modifier,
        onMenuClick = {},
        onSearchClick = {},
        onNotificationsClick = {},
        onInfoClick = {},
        onMetricClick = {},
        onHealthClick = {},
        onEditQuickActions = {},
        onQuickActionClick = {},
        onInfoCardClick = {},
        onBottomNavClick = {},
    )
}

/**
 * Render the dashboard with a fixed bottom navigation dock.
 *
 * Layout uses a Column at the root:
 *  1. Background gradient (drawn first so it extends edge-to-edge)
 *  2. Scrollable content (weighted to take all remaining space)
 *  3. Bottom navigation dock (fixed at the bottom)
 */
@Composable
internal fun DashboardContent(
    state: DashboardUiState,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onMetricClick: (SystemMetric) -> Unit,
    onHealthClick: () -> Unit,
    onEditQuickActions: () -> Unit,
    onQuickActionClick: (QuickAction) -> Unit,
    onInfoCardClick: (InfoCardData) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
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
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Scrollable content above the bar (weight = 1f → takes all space)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = contentPadding.calculateTopPadding())
                    .padding(bottom = BottomContentPadding),
            ) {
                DashboardTopBar(
                    onMenuClick = onMenuClick,
                    onSearchClick = onSearchClick,
                    onNotificationsClick = onNotificationsClick,
                )

                Spacer(modifier = Modifier.height(20.dp))

                when (state) {
                    DashboardUiState.Loading -> LoadingState()
                    is DashboardUiState.Error -> ErrorState(state.message)
                    is DashboardUiState.Success -> ReadyState(
                        snapshot = state,
                        onInfoClick = onInfoClick,
                        onMetricClick = onMetricClick,
                        onHealthClick = onHealthClick,
                        onEditQuickActions = onEditQuickActions,
                        onQuickActionClick = onQuickActionClick,
                        onInfoCardClick = onInfoCardClick,
                    )
                }
            }

            // Fixed bottom navigation dock (always at the bottom, never scrolls)
            DashboardBottomBar(
                items = (state as? DashboardUiState.Success)?.snapshot?.bottomNav.orEmpty(),
                onItemClick = onBottomNavClick,
            )
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
            text = "Something went wrong",
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

@Composable
private fun ReadyState(
    snapshot: DashboardUiState.Success,
    onInfoClick: () -> Unit,
    onMetricClick: (SystemMetric) -> Unit,
    onHealthClick: () -> Unit,
    onEditQuickActions: () -> Unit,
    onQuickActionClick: (QuickAction) -> Unit,
    onInfoCardClick: (InfoCardData) -> Unit,
) {
    val data = snapshot.snapshot

    // -- Greeting + NexCore Score (single row) ----------------------------
    GreetingWithScoreSection(
        greeting = data.greeting,
        score = data.nexCoreScore,
        modifier = Modifier.padding(top = 4.dp),
        onInfoClick = onInfoClick,
    )

    Spacer(modifier = Modifier.height(20.dp))

    MetricsRow(
        metrics = data.metrics,
        onMetricClick = onMetricClick,
    )

    // -- Device health banner --------------------------------------------
    Spacer(modifier = Modifier.height(20.dp))
    DeviceHealthBanner(
        health = data.health,
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = onHealthClick,
    )

    // -- Quick actions grid ----------------------------------------------
    Spacer(modifier = Modifier.height(22.dp))
    QuickActionsSection(
        actions = data.quickActions,
        onEditClick = onEditQuickActions,
        onActionClick = onQuickActionClick,
    )

    // -- Installed Apps / Data Usage / Notifications ---------------------
    Spacer(modifier = Modifier.height(22.dp))
    InfoCardsRow(
        installedApps = data.installedApps,
        dataUsage = data.dataUsage,
        notifications = data.notifications,
        onClick = onInfoCardClick,
    )

    Spacer(modifier = Modifier.height(12.dp))
}