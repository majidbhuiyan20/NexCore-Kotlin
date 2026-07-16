package com.matox.nexcore.presentation.dashboard

import androidx.compose.foundation.background
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
import com.matox.nexcore.presentation.dashboard.components.GreetingSection
import com.matox.nexcore.presentation.dashboard.components.InfoCardsRow
import com.matox.nexcore.presentation.dashboard.components.MetricsRow
import com.matox.nexcore.presentation.dashboard.components.NexCoreScoreCard
import com.matox.nexcore.presentation.dashboard.components.QuickActionsSection
import com.matox.nexcore.presentation.dashboard.components.StorageUsageCard
import com.matox.nexcore.presentation.dashboard.components.BatteryCard
import com.matox.nexcore.presentation.dashboard.state.DashboardUiState
import com.matox.nexcore.presentation.dashboard.viewmodel.DashboardViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.TextPrimary

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
        onViewStorageDetails = {},
        onInfoCardClick = {},
        onBottomNavClick = {},
    )
}

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
    onViewStorageDetails: () -> Unit,
    onInfoCardClick: (InfoCardData) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGradientTop,
                        BackgroundGradientBottom,
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
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
                onViewStorageDetails = onViewStorageDetails,
                onInfoCardClick = onInfoCardClick,
                onBottomNavClick = onBottomNavClick,
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
    onViewStorageDetails: () -> Unit,
    onInfoCardClick: (InfoCardData) -> Unit,
    onBottomNavClick: (BottomNavItem) -> Unit,
) {
    val data = snapshot.snapshot

    // -- Greeting + score + metric cards ----------------------------------
    GreetingSection(
        greeting = data.greeting,
        modifier = Modifier.padding(top = 4.dp),
    )

    Spacer(modifier = Modifier.height(20.dp))

    NexCoreScoreCard(
        score = data.nexCoreScore,
        modifier = Modifier.padding(horizontal = 16.dp),
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

    // -- Storage + Battery row -------------------------------------------
    Spacer(modifier = Modifier.height(22.dp))
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        StorageUsageCard(
            usage = data.storageUsage,
            modifier = Modifier.weight(1f),
            onViewDetailsClick = onViewStorageDetails,
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        BatteryCard(
            battery = data.battery,
            modifier = Modifier.weight(1f),
        )
    }

    // -- Installed Apps / Data Usage / Notifications ---------------------
    Spacer(modifier = Modifier.height(16.dp))
    InfoCardsRow(
        installedApps = data.installedApps,
        dataUsage = data.dataUsage,
        notifications = data.notifications,
        onClick = onInfoCardClick,
    )

    Spacer(modifier = Modifier.height(20.dp))

    // -- Bottom navigation ------------------------------------------------
    DashboardBottomBar(
        items = data.bottomNav,
        onItemClick = onBottomNavClick,
    )

    Spacer(modifier = Modifier.height(16.dp))
}