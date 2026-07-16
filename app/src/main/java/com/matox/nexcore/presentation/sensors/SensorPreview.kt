package com.matox.nexcore.presentation.sensors

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.SensorLiveMotion
import com.matox.nexcore.domain.model.SensorReading
import com.matox.nexcore.domain.model.SensorSnapshot
import com.matox.nexcore.domain.model.SensorType
import com.matox.nexcore.presentation.sensors.state.SensorUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = SensorSnapshot(
    readings = listOf(
        SensorReading(
            sensorType = SensorType.ACCELEROMETER,
            name = "BMI160 Accelerometer",
            vendor = "Bosch",
            values = floatArrayOf(0.12f, 9.81f, -0.34f),
            unit = "m/s²",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.GYROSCOPE,
            name = "BMI160 Gyroscope",
            vendor = "Bosch",
            values = floatArrayOf(0.005f, -0.002f, 0.001f),
            unit = "rad/s",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.MAGNETOMETER,
            name = "AK0991X Magnetometer",
            vendor = "AKM",
            values = floatArrayOf(23.4f, -10.2f, 41.0f),
            unit = "µT",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.GRAVITY,
            name = "Gravity Sensor",
            vendor = "Google",
            values = floatArrayOf(0.05f, 9.78f, -0.20f),
            unit = "m/s²",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.LINEAR_ACCELERATION,
            name = "Linear Accel",
            vendor = "Google",
            values = floatArrayOf(0.07f, 0.03f, -0.14f),
            unit = "m/s²",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.ROTATION_VECTOR,
            name = "Rotation Vector",
            vendor = "Google",
            values = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f),
            unit = "",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.STEP_COUNTER,
            name = "Step Counter",
            vendor = "Google",
            values = floatArrayOf(8421f),
            unit = "steps",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.STEP_DETECTOR,
            name = "Step Detector",
            vendor = "Google",
            values = floatArrayOf(0f),
            unit = "",
            active = false,
        ),
        SensorReading(
            sensorType = SensorType.PROXIMITY,
            name = "TMD3725 Proximity",
            vendor = "AMS",
            values = floatArrayOf(5f),
            unit = "cm",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.LIGHT,
            name = "TMD3725 Light",
            vendor = "AMS",
            values = floatArrayOf(420f),
            unit = "lx",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.PRESSURE,
            name = "BMP380 Pressure",
            vendor = "Bosch",
            values = floatArrayOf(1013.2f),
            unit = "hPa",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.AMBIENT_TEMPERATURE,
            name = "Ambient Temperature",
            vendor = "Bosch",
            values = floatArrayOf(24.6f),
            unit = "°C",
            active = false,
        ),
        SensorReading(
            sensorType = SensorType.RELATIVE_HUMIDITY,
            name = "Humidity",
            vendor = "Sensirion",
            values = floatArrayOf(47f),
            unit = "%",
            active = false,
        ),
        SensorReading(
            sensorType = SensorType.GAME_ROTATION_VECTOR,
            name = "Game Rotation",
            vendor = "Google",
            values = floatArrayOf(0.0f, 0.0f, 0.0f),
            unit = "",
            active = true,
        ),
        SensorReading(
            sensorType = SensorType.SIGNIFICANT_MOTION,
            name = "Significant Motion",
            vendor = "Google",
            values = floatArrayOf(0f),
            unit = "",
            active = false,
        ),
        SensorReading(
            sensorType = SensorType.HEART_RATE,
            name = "Heart Rate",
            vendor = "—",
            values = floatArrayOf(0f),
            unit = "bpm",
            active = false,
        ),
    ),
    activeCount = 11,
    accelerometer = SensorReading(
        sensorType = SensorType.ACCELEROMETER,
        name = "BMI160 Accelerometer",
        vendor = "Bosch",
        values = floatArrayOf(0.12f, 9.81f, -0.34f),
        unit = "m/s²",
        active = true,
    ),
    gyroscope = SensorReading(
        sensorType = SensorType.GYROSCOPE,
        name = "BMI160 Gyroscope",
        vendor = "Bosch",
        values = floatArrayOf(0.005f, -0.002f, 0.001f),
        unit = "rad/s",
        active = true,
    ),
    hasAnyMotion = true,
)

private val PreviewMotion = SensorLiveMotion(
    accelerometerMagnitude = 9.83f,
    gyroscopeMagnitude = 0.04f,
    active = true,
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1600)
@Composable
fun SensorPreview() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        SensorContent(
            state = SensorUiState.Success(
                snapshot = PreviewSnapshot,
                liveMotion = PreviewMotion,
            ),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
            onRefresh = {},
            onDetailsClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 800)
@Composable
fun SensorPreviewLoading() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        SensorContent(
            state = SensorUiState.Loading,
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 800)
@Composable
fun SensorPreviewError() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        SensorContent(
            state = SensorUiState.Error("Permission denied"),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1200)
@Composable
fun SensorPreviewEmpty() {
    NexCoreTheme {
        val snackbar = remember { SnackbarHostState() }
        SensorContent(
            state = SensorUiState.Success(
                snapshot = SensorSnapshot(),
                liveMotion = SensorLiveMotion.Empty,
            ),
            onBack = {},
            onBottomNavClick = {},
            snackbarHostState = snackbar,
        )
    }
}
