package com.matox.nexcore.presentation.storageanalyzer

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
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.storageanalyzer.components.CategoryGridCard
import com.matox.nexcore.presentation.storageanalyzer.components.InternalStorageHero
import com.matox.nexcore.presentation.storageanalyzer.components.QuickInsightsCard
import com.matox.nexcore.presentation.storageanalyzer.components.SmartCleanBanner
import com.matox.nexcore.presentation.storageanalyzer.components.StorageAnalyzerTopBar
import com.matox.nexcore.presentation.storageanalyzer.components.TopLargeFilesCard
import com.matox.nexcore.presentation.storageanalyzer.state.StorageAnalyzerUiState
import com.matox.nexcore.presentation.storageanalyzer.viewmodel.StorageAnalyzerViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.TextPrimary

private val BottomContentPadding: Dp = 120.dp

/** Static bottom-nav used while on Storage Analyzer — keeps the
 *  destination context visible and gives the user a "back to home"
 *  affordance beyond the back arrow. */
private val AnalyzerBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = false),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun StorageAnalyzerScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val viewModel: StorageAnalyzerViewModel = viewModel(
        factory = StorageAnalyzerViewModel.Factory(AppContainer.storageAnalyzerRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StorageAnalyzerContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onCategoryClick = {},
        onInsightClick = {},
        onLargeFileClick = {},
        onSmartCleanClick = {},
        onBottomNavClick = onBottomNavClick,
    )
}

/**
 * Render the Storage Analyzer with the same fixed-top-bar +
 * scrollable-body + fixed-bottom-bar layout as the dashboard.
 *
 *  1. Background gradient (full-bleed)
 *  2. StorageAnalyzerTopBar (fixed at top)
 *  3. Scrollable content (weight = 1f)
 *  4. DashboardBottomBar (fixed at bottom)
 */
@Composable
internal fun StorageAnalyzerContent(
    state: StorageAnalyzerUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onCategoryClick: (com.matox.nexcore.domain.model.StorageCategory) -> Unit,
    onInsightClick: (com.matox.nexcore.presentation.storageanalyzer.components.InsightKind) -> Unit,
    onLargeFileClick: (com.matox.nexcore.domain.model.LargeFileEntry) -> Unit,
    onSmartCleanClick: () -> Unit,
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
            // Fixed top bar
            StorageAnalyzerTopBar(onBackClick = onBack)

            // Scrollable body (consumes all remaining space)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = contentPadding.calculateTopPadding())
                    .padding(bottom = BottomContentPadding),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                when (state) {
                    StorageAnalyzerUiState.Loading -> LoadingState()
                    is StorageAnalyzerUiState.Error -> ErrorState(state.message)
                    is StorageAnalyzerUiState.Success -> ReadyState(
                        snapshot = state,
                        onCategoryClick = onCategoryClick,
                        onInsightClick = onInsightClick,
                        onLargeFileClick = onLargeFileClick,
                        onSmartCleanClick = onSmartCleanClick,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Fixed bottom navigation dock
            DashboardBottomBar(
                items = AnalyzerBottomNav,
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
            text = "Storage analyzer unavailable",
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
    snapshot: StorageAnalyzerUiState.Success,
    onCategoryClick: (com.matox.nexcore.domain.model.StorageCategory) -> Unit,
    onInsightClick: (com.matox.nexcore.presentation.storageanalyzer.components.InsightKind) -> Unit,
    onLargeFileClick: (com.matox.nexcore.domain.model.LargeFileEntry) -> Unit,
    onSmartCleanClick: () -> Unit,
) {
    val data = snapshot.snapshot

    InternalStorageHero(
        breakdown = data,
        modifier = Modifier.padding(horizontal = 16.dp),
    )

    Spacer(modifier = Modifier.height(16.dp))

    CategoryGridCard(
        categories = data.categories,
        modifier = Modifier.padding(horizontal = 16.dp),
        onCategoryClick = onCategoryClick,
    )

    Spacer(modifier = Modifier.height(16.dp))

    QuickInsightsCard(
        insights = data.insights,
        modifier = Modifier.padding(horizontal = 16.dp),
        onInsightClick = onInsightClick,
    )

    Spacer(modifier = Modifier.height(16.dp))

    TopLargeFilesCard(
        files = data.largeFiles,
        modifier = Modifier.padding(horizontal = 16.dp),
        onFileClick = onLargeFileClick,
    )

    Spacer(modifier = Modifier.height(16.dp))

    SmartCleanBanner(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = onSmartCleanClick,
    )
}
