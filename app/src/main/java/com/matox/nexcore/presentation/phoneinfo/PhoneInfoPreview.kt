package com.matox.nexcore.presentation.phoneinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.matox.nexcore.domain.model.BasicInfo
import com.matox.nexcore.domain.model.BatteryInfo
import com.matox.nexcore.domain.model.HardwareInfo
import com.matox.nexcore.domain.model.NetworkInfo
import com.matox.nexcore.domain.model.OsInfo
import com.matox.nexcore.domain.model.PhoneInfoSnapshot
import com.matox.nexcore.domain.model.SensorsInfo
import com.matox.nexcore.presentation.phoneinfo.state.PhoneInfoUiState
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop

/**
 * Compose preview entry point for the Phone Info screen — renders
 * against a representative mock snapshot so designers can iterate
 * without a device.
 */
@Composable
fun PhoneInfoPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundGradientTop, BackgroundGradientBottom),
                ),
            ),
    ) {
        PhoneInfoContent(
            state = PhoneInfoUiState.Success(snapshot = mockSnapshot()),
            onBack = {},
            onRefresh = {},
        )
    }
}

private fun mockSnapshot(): PhoneInfoSnapshot = PhoneInfoSnapshot(
    deviceName = "Samsung Galaxy S24",
    basic = BasicInfo(
        manufacturer = "Samsung",
        brand = "Samsung",
        model = "SM-S921B",
        device = "o1q",
        product = "o1q",
        board = "o1q",
        hardware = "qcom",
        deviceCodeName = "o1q",
        fingerprint = "samsung/o1qxxx/o1q:14/UP1A.231005.007/S921BXXS2AXL4:user/release-keys",
        serial = "—",
        host = "android-build",
        user = "dpi",
    ),
    hardware = HardwareInfo(
        cpuAbi = "arm64-v8a",
        cpuCores = 8,
        supportedAbis = "arm64-v8a, armeabi-v7a, armeabi",
        processorName = "Qualcomm SM8650 Snapdragon 8 Gen 3",
        minSdk = 24,
        targetSdk = 34,
        sdkInt = 34,
        javaVm = "OpenJDK 17.0",
        screenResolution = "2340 × 1080",
        screenDensity = "420 dpi (2.625x)",
        refreshRateHz = "120.0 Hz",
        totalMemoryGb = "12.0 GB",
        internalStorageGb = "256.0 GB",
    ),
    battery = BatteryInfo(
        levelPercent = 78,
        status = "Discharging",
        plugged = "Battery",
        technology = "Li-ion",
        temperatureC = 32,
        voltageMv = 4012,
        health = "Good",
    ),
    os = OsInfo(
        androidVersion = "14",
        release = "14",
        securityPatch = "2024-11-01",
        buildId = "UP1A.231005.007",
        buildDisplay = "UP1A.231005.007.S921BXXS2AXL4",
        buildDate = "Wed Nov 06 11:12:34 UTC 2024",
        kernelVersion = "Linux 5.15.123-android14-11 (build-user@build-host)",
        javaVm = "OpenJDK 17.0",
        bootloader = "O1QXXU2AXL4",
    ),
    network = NetworkInfo(
        simOperator = "T-Mobile",
        simState = "Active",
        networkType = "5G NR",
        dataState = "Connected (Mobile)",
        wifiMac = "02:00:00:00:00:00",
        ipAddress = "192.168.1.42",
        wifiSsid = "Home WiFi",
        signalDbm = "—",
    ),
    sensors = SensorsInfo(
        accelerometer = "BMI320 Accelerometer",
        gyroscope = "BMI320 Gyroscope",
        magnetometer = "AK09918 Magnetometer",
        proximity = "TMD3725 Proximity Sensor",
        lightSensor = "TMD3725 Light Sensor",
        barometer = "BMP580 Pressure Sensor",
        stepCounter = "BMI320 Step Counter",
        stepDetector = "BMI320 Step Detector",
        gravity = "BMI320 Gravity",
        rotationVector = "Game Rotation Vector",
        totalSensorCount = 32,
    ),
    uptimeSeconds = 5 * 86_400L + 4 * 3_600L + 23 * 60L,
)
