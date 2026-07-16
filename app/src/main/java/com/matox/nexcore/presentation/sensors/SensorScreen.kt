package com.matox.nexcore.presentation.sensors

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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.sensors.components.SensorAccelGyroCard
import com.matox.nexcore.presentation.sensors.components.SensorDetailsButton
import com.matox.nexcore.presentation.sensors.components.SensorHeroCard
import com.matox.nexcore.presentation.sensors.components.SensorListCard
import com.matox.nexcore.presentation.sensors.components.SensorTopBar
import com.matox.nexcore.presentation.sensors.components.rememberSensorDetailsLauncher
import com.matox.nexcore.presentation.sensors.state.SensorUiState
import com.matox.nexcore.presentation.sensors.viewmodel.SensorViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.TextPrimary

/** Bottom padding under the scrollable content so it doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 110.dp

/**
 * Static bottom-nav used while on the Sensor Monitor screen.
 * Mirrors the Battery / RAM screens — Home pill is active, the
 * rest are inactive placeholders.
 */
private val SensorBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = true),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun SensorScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorViewModel = viewModel(
        factory = SensorViewModel.Factory(
            repository = AppContainer.sensorRepository,
            provider = AppContainer.sensorProvider,
        )
    ),
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val launcher = rememberSensorDetailsLauncher()

    // Lifecycle wiring — register listeners when the screen
    // becomes visible, unregister when it leaves.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onStart()
                Lifecycle.Event.ON_STOP -> viewModel.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onStop()
        }
    }

    // Trigger snackbar when a message is queued.
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarMessage = null
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(msg)
    }

    SensorContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.refresh() },
        onDetailsClick = {
            val result = launcher()
            snackbarMessage = result.message
        },
    )
}

/**
 * Render the Sensor Monitor screen with a fixed top bar + scrollable
 * body + floating dock layout, matching the Battery / RAM pattern.
 *
 * Split out from [SensorScreen] so previews can render the
 * composed screen with a hand-crafted [SensorUiState.Success]
 * without spinning up a real `SensorViewModel`.
 */
@Composable
internal fun SensorContent(
    state: SensorUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onRefresh: () -> Unit = {},
    onDetailsClick: () -> Unit = {},
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
            SensorTopBar(
                onBackClick = onBack,
                onRefreshClick = onRefresh,
            )

            when (state) {
                SensorUiState.Loading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    LoadingState()
                }
                is SensorUiState.Error -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                ) {
                    ErrorState(state.message)
                }
                is SensorUiState.Success -> {
                    val snap = state.snapshot
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = contentPadding.calculateTopPadding())
                            .padding(bottom = BottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        SensorHeroCard(
                            snapshot = snap,
                            liveMotion = state.liveMotion,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        SensorAccelGyroCard(
                            accelerometer = snap.accelerometer,
                            gyroscope = snap.gyroscope,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        SensorListCard(
                            readings = snap.readings,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        SensorDetailsButton(
                            onClick = onDetailsClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            DashboardBottomBar(
                items = SensorBottomNav,
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
            text = "Sensors unavailable",
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