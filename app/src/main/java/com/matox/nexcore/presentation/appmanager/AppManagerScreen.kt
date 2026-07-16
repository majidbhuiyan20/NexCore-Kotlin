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

/** Bottom padding the LazyColumn applies via `contentPadding` so the
 *  last row can scroll up under the floating dock instead of stopping
 *  mid-screen. ~110 dp = dock height + a comfortable visual gap. */
private val ListBottomPadding: Dp = 96.dp

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
 * Layout (top → bottom):
 *   1. Background gradient (full-bleed)
 *   2. AppManagerTopBar (fixed at top)
 *   3. Fixed header: stats row + filter tabs + search/sort bar
 *   4. LazyColumn (fills remaining space, weighted 1f) — the list
 *      runs all the way to the bottom of the screen; bottom
 *      `contentPadding` keeps the last row above the dock visually.
 *   5. DashboardBottomBar (fixed at bottom)
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

            when (state) {
                AppManagerUiState.Loading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    LoadingState()
                }
                is AppManagerUiState.Error -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    ErrorState(state.message)
                }
                is AppManagerUiState.Success -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    FixedHeader(
                        snapshot = state,
                        onSearchChange = onSearchChange,
                        onSortSelected = onSortSelected,
                        onTabSelected = onTabSelected,
                    )

                    // LazyColumn takes the rest of the screen height.
                    // `weight(1f)` + `fillMaxSize()` on its modifier lets
                    // it grow until the bottom dock; the bottom
                    // contentPadding keeps the last row visible above
                    // the dock even when fully scrolled.
                    AppLazyList(
                        snapshot = state,
                        onOpen = onOpen,
                        onInfo = onInfo,
                        onUninstall = onUninstall,
                        onDisable = onDisable,
                    )
                }
            }

            DashboardBottomBar(
                items = AppManagerBottomNav,
                onItemClick = onBottomNavClick,
            )
        }
    }
}

/**
 * Stats row + filter tabs + search/sort bar. Renders inline so the
 * LazyColumn sibling can take all remaining vertical space.
 */
@Composable
private fun FixedHeader(
    snapshot: AppManagerUiState.Success,
    onSearchChange: (String) -> Unit,
    onSortSelected: (com.matox.nexcore.domain.model.AppSort) -> Unit,
    onTabSelected: (com.matox.nexcore.domain.model.AppFilterTab) -> Unit,
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
}

/**
 * The actual app list. Renders the empty-state message inline when
 * `filtered` is empty so the LazyColumn itself isn't laid out
 * empty (which would leave a big black gap before the dock).
 */
@Composable
private fun AppLazyList(
    snapshot: AppManagerUiState.Success,
    onOpen: (AppInfo) -> Unit,
    onInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit,
    onDisable: (AppInfo) -> Unit,
) {
    if (snapshot.filtered.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = ListBottomPadding),
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
        // While the live snapshot is replacing the cached one, append
        // 3 skeleton rows so the user sees something animating.
        if (snapshot.isFromCache) {
            items(count = 3, key = { "skeleton-$it" }) {
                AppRowSkeleton()
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
