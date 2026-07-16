package com.matox.nexcore.presentation.storageanalyzer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.core.util.StoragePermissions
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
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

private val BottomContentPadding: Dp = 120.dp

/** Static bottom-nav used while on Storage Analyzer. */
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
    val context = LocalContext.current
    val viewModel: StorageAnalyzerViewModel = viewModel(
        factory = StorageAnalyzerViewModel.Factory(AppContainer.storageAnalyzerRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Track permission status so we can show the gate before content.
    var hasPermission by remember {
        mutableStateOf(StoragePermissions.areGranted(context))
    }
    // Refresh whenever the screen recomposes (e.g. user toggles in
    // system settings and comes back).
    LaunchedEffect(Unit) { hasPermission = StoragePermissions.areGranted(context) }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        hasPermission = StoragePermissions.areGranted(context)
        // Whether granted or denied, kick the analyzer to recompute
        // so any change in MediaStore coverage is reflected.
        viewModel.refresh()
    }

    StorageAnalyzerContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onCategoryClick = {},
        onInsightClick = {},
        onLargeFileClick = {},
        onSmartCleanClick = {},
        onBottomNavClick = onBottomNavClick,
        hasPermission = hasPermission,
        onRequestPermission = {
            permLauncher.launch(StoragePermissions.required().toTypedArray())
        },
    )
}

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
    hasPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
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
            StorageAnalyzerTopBar(onBackClick = onBack)

            if (!hasPermission) {
                // Permission gate — full-screen prompt. Renders the
                // top bar but nothing else until the user grants.
                PermissionGate(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = contentPadding.calculateTopPadding()),
                    onRequestPermission = onRequestPermission,
                )
            } else {
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
            }

            DashboardBottomBar(
                items = AnalyzerBottomNav,
                onItemClick = onBottomNavClick,
            )
        }
    }
}

@Composable
private fun PermissionGate(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(MetricBlue.copy(alpha = 0.16f), shape = androidx.compose.foundation.shape.CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MetricBlue,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Storage access required",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = StoragePermissions.rationale(),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = MetricBlue,
                contentColor = TextPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        ) {
            Text(
                text = "Allow access",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
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