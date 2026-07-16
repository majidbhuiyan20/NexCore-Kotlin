package com.matox.nexcore.presentation.phoneinfo

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.NetworkWifi
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
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.PhoneInfoSnapshot
import com.matox.nexcore.presentation.phoneinfo.components.BatteryCard
import com.matox.nexcore.presentation.phoneinfo.components.DeviceHeroCard
import com.matox.nexcore.presentation.phoneinfo.components.PhoneInfoTopBar
import com.matox.nexcore.presentation.phoneinfo.components.SectionCard
import com.matox.nexcore.presentation.phoneinfo.components.SensorsCard
import com.matox.nexcore.presentation.phoneinfo.state.PhoneInfoUiState
import com.matox.nexcore.presentation.phoneinfo.viewmodel.PhoneInfoViewModel
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.TextPrimary

private val BottomContentPadding: Dp = 24.dp

@Composable
fun PhoneInfoScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val viewModel: PhoneInfoViewModel = viewModel(
        factory = PhoneInfoViewModel.Factory(AppContainer.phoneInfoRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PhoneInfoContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onRefresh = viewModel::refresh,
    )
}

@Composable
internal fun PhoneInfoContent(
    state: PhoneInfoUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundGradientTop, BackgroundGradientBottom),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PhoneInfoTopBar(onBackClick = onBack, onRefreshClick = onRefresh)

            when (state) {
                PhoneInfoUiState.Loading -> LoadingState()
                is PhoneInfoUiState.Error -> ErrorState(state.message)
                is PhoneInfoUiState.Success -> ReadyState(snapshot = state.snapshot)
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
            text = "Phone info unavailable",
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
private fun ReadyState(snapshot: PhoneInfoSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, bottom = BottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DeviceHeroCard(
            deviceName = snapshot.deviceName,
            manufacturer = snapshot.basic.manufacturer,
            model = snapshot.basic.model,
            uptimeSeconds = snapshot.uptimeSeconds,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        SectionCard(
            title = "Basic Information",
            icon = Icons.Outlined.Info,
            accent = MetricAccent.BLUE,
            modifier = Modifier.padding(horizontal = 16.dp),
            rows = listOf(
                "Manufacturer" to snapshot.basic.manufacturer,
                "Brand" to snapshot.basic.brand,
                "Model" to snapshot.basic.model,
                "Device" to snapshot.basic.device,
                "Product" to snapshot.basic.product,
                "Board" to snapshot.basic.board,
                "Hardware" to snapshot.basic.hardware,
                "Device codename" to snapshot.basic.deviceCodeName,
                "Host" to snapshot.basic.host,
                "User" to snapshot.basic.user,
                "Serial" to snapshot.basic.serial,
            ),
        )

        SectionCard(
            title = "Hardware",
            icon = Icons.Outlined.Memory,
            accent = MetricAccent.PURPLE,
            modifier = Modifier.padding(horizontal = 16.dp),
            rows = listOf(
                "Processor" to snapshot.hardware.processorName,
                "CPU ABI" to snapshot.hardware.cpuAbi,
                "Supported ABIs" to snapshot.hardware.supportedAbis,
                "CPU cores" to snapshot.hardware.cpuCores.toString(),
                "Total memory" to snapshot.hardware.totalMemoryGb,
                "Internal storage" to snapshot.hardware.internalStorageGb,
                "Resolution" to snapshot.hardware.screenResolution,
                "Density" to snapshot.hardware.screenDensity,
                "Refresh rate" to snapshot.hardware.refreshRateHz,
                "Min SDK" to snapshot.hardware.minSdk.toString(),
                "Target SDK" to snapshot.hardware.targetSdk.toString(),
                "SDK (compile)" to snapshot.hardware.sdkInt.toString(),
                "Java VM" to snapshot.hardware.javaVm,
            ),
        )

        BatteryCard(
            battery = snapshot.battery,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        SectionCard(
            title = "OS Information",
            icon = Icons.Outlined.Devices,
            accent = MetricAccent.GREEN,
            modifier = Modifier.padding(horizontal = 16.dp),
            rows = listOf(
                "Android version" to snapshot.os.androidVersion,
                "Release" to snapshot.os.release,
                "Security patch" to snapshot.os.securityPatch,
                "Build ID" to snapshot.os.buildId,
                "Build display" to snapshot.os.buildDisplay,
                "Build date" to snapshot.os.buildDate,
                "Bootloader" to snapshot.os.bootloader,
                "Kernel" to snapshot.os.kernelVersion,
                "Java VM" to snapshot.os.javaVm,
            ),
        )

        SectionCard(
            title = "Network",
            icon = Icons.Outlined.NetworkWifi,
            accent = MetricAccent.CYAN,
            modifier = Modifier.padding(horizontal = 16.dp),
            rows = listOf(
                "SIM operator" to snapshot.network.simOperator,
                "SIM state" to snapshot.network.simState,
                "Network type" to snapshot.network.networkType,
                "Data state" to snapshot.network.dataState,
                "WiFi SSID" to snapshot.network.wifiSsid,
                "WiFi MAC" to snapshot.network.wifiMac,
                "IP address" to snapshot.network.ipAddress,
                "Signal" to snapshot.network.signalDbm,
            ),
        )

        SensorsCard(
            sensors = snapshot.sensors,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}
