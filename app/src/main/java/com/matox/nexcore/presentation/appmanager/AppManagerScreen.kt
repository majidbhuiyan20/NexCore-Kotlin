package com.matox.nexcore.presentation.appmanager

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.appmanager.components.AppFilterTabs
import com.matox.nexcore.presentation.appmanager.components.AppListItem
import com.matox.nexcore.presentation.appmanager.components.AppManagerTopBar
import com.matox.nexcore.presentation.appmanager.components.AppRowSkeleton
import com.matox.nexcore.presentation.appmanager.components.AppSearchSortBar
import com.matox.nexcore.presentation.appmanager.components.AppStatsRow
import com.matox.nexcore.presentation.appmanager.intent.AppActions
import com.matox.nexcore.presentation.appmanager.state.AppManagerUiState
import com.matox.nexcore.presentation.appmanager.viewmodel.AppManagerViewModel
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/** Bottom padding so the list doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 120.dp

/** Static bottom-nav used while on App Manager. */
private val AppManagerBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = false),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = true),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun AppManagerScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val viewModel: AppManagerViewModel = viewModel(
        factory = AppManagerViewModel.Factory(AppContainer.appManagerRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AppManagerContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onSearchChange = viewModel::setSearch,
        onSortSelected = viewModel::setSort,
        onTabSelected = viewModel::setTab,
        onOpen = { pkg -> AppActions.open(context, pkg.packageName) },
        onInfo = { pkg -> AppActions.info(context, pkg.packageName) },
        onUninstall = { pkg -> AppActions.uninstall(context, pkg.packageName) },
        onDisable = { pkg -> AppActions.disable(context, pkg.packageName) },
        onBottomNavClick = onBottomNavClick,
    )
}

/**
 * Render the App Manager with the standard fixed-top-bar +
 * scrollable-body + fixed-bottom-bar layout.
 *
 *  1. Background gradient (full-bleed)
 *  2. AppManagerTopBar (fixed at top)
 *  3. Stats row + filter tabs + search/sort bar + app list (scrollable)
 *  4. DashboardBottomBar (fixed at bottom)
 */
@Composable
internal fun AppManagerContent(
    state: AppManagerUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSearchChange: (String) -> Unit,
    onSortSelected: (com.matox.nexcore.domain.model.AppSort) -> Unit,
    onTabSelected: (com.matox.nexcore.domain.model.AppFilterTab) -> Unit,
    onOpen: (AppInfo) -> Unit,
    onInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit,
    onDisable: (AppInfo) -> Unit,
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
        Column(modifier = Modifier.fillMaxSize()) {
            AppManagerTopBar(onBackClick = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = contentPadding.calculateTopPadding())
                    .padding(bottom = BottomContentPadding),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                when (state) {
                    AppManagerUiState.Loading -> LoadingState()
                    is AppManagerUiState.Error -> ErrorState(state.message)
                    is AppManagerUiState.Success -> ReadyState(
                        snapshot = state,
                        onSearchChange = onSearchChange,
                        onSortSelected = onSortSelected,
                        onTabSelected = onTabSelected,
                        onOpen = onOpen,
                        onInfo = onInfo,
                        onUninstall = onUninstall,
                        onDisable = onDisable,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            DashboardBottomBar(
                items = AppManagerBottomNav,
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
            text = "App Manager unavailable",
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
    snapshot: AppManagerUiState.Success,
    onSearchChange: (String) -> Unit,
    onSortSelected: (com.matox.nexcore.domain.model.AppSort) -> Unit,
    onTabSelected: (com.matox.nexcore.domain.model.AppFilterTab) -> Unit,
    onOpen: (AppInfo) -> Unit,
    onInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit,
    onDisable: (AppInfo) -> Unit,
) {
    AppStatsRow(
        totalApps = snapshot.totalCount,
        userApps = snapshot.userCount,
        systemApps = snapshot.systemCount,
        totalSizeBytes = snapshot.totalSizeBytes,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )

    Spacer(modifier = Modifier.height(8.dp))

    AppFilterTabs(
        selected = snapshot.tab,
        onTabSelected = onTabSelected,
    )

    Spacer(modifier = Modifier.height(4.dp))

    AppSearchSortBar(
        query = snapshot.search,
        sort = snapshot.sort,
        onQueryChange = onSearchChange,
        onSortSelected = onSortSelected,
        onGridToggle = { /* grid view stubbed for now */ },
    )

    Spacer(modifier = Modifier.height(6.dp))

    // First paint = disk cache. Show skeleton + cached rows together
    // so the user sees real data immediately while PackageManager
    // refreshes silently in the background.
    if (snapshot.isFromCache && snapshot.allApps.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            items(
                items = snapshot.filtered,
                key = { it.packageName },
            ) { app ->
                AppListItem(
                    app = app,
                    onOpen = onOpen,
                    onInfo = onInfo,
                    onUninstall = onUninstall,
                    onDisable = onDisable,
                )
            }
            // 3 skeleton rows at the bottom to hint "more coming".
            items(count = 3, key = { "skeleton-$it" }) {
                AppRowSkeleton()
            }
        }
        return
    }

    // Live snapshot — render the list as before, but in a LazyColumn.
    if (snapshot.filtered.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No apps match",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try clearing the search or switching tabs.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            items(
                items = snapshot.filtered,
                key = { it.packageName },
            ) { app ->
                AppListItem(
                    app = app,
                    onOpen = onOpen,
                    onInfo = onInfo,
                    onUninstall = onUninstall,
                    onDisable = onDisable,
                )
            }
        }
    }
}
