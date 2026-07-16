package com.matox.nexcore.presentation.phoneinfo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SocialDistance
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.SensorsInfo
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Sensors card — top row has 5 high-signal sensors as icon tiles,
 * "View All" button reveals every remaining sensor.
 *
 * Renders as a single vertical column. The 5 highlighted sensors are
 * laid out using a horizontal `Row` of icon tiles (each tile is itself
 * a small vertical column with icon + label). They form a "showcase"
 * row inside an otherwise full-width card — the screen itself never
 * becomes two columns.
 */
@Composable
fun SensorsCard(
    sensors: SensorsInfo,
    modifier: Modifier = Modifier,
) {
    val accent = MetricAccent.PINK
    val accentColor = accent.toColor()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                icon = Icons.Outlined.Memory,
                accent = accentColor,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sensors",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${sensors.totalSensorCount}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
            )
        }

        Spacer(modifier = Modifier.size(14.dp))

        // 5-up tile row for the most useful sensors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SensorTile(Icons.Outlined.SwapVert, "Accelero", accentColor)
            SensorTile(Icons.Outlined.Explore, "Gyroscope", accentColor)
            SensorTile(Icons.Outlined.SocialDistance, "Proximity", accentColor)
            SensorTile(Icons.Outlined.Lightbulb, "Light", accentColor)
            SensorTile(Icons.Outlined.Straighten, "Pressure", accentColor)
        }

        Spacer(modifier = Modifier.size(14.dp))

        // "View all" toggle button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.14f))
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (expanded) "Hide details" else "View all sensors",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = accentColor,
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.size(12.dp))
            SensorDetail("Accelerometer", sensors.accelerometer)
            SensorDetail("Gyroscope", sensors.gyroscope)
            SensorDetail("Magnetometer", sensors.magnetometer)
            SensorDetail("Proximity sensor", sensors.proximity)
            SensorDetail("Light sensor", sensors.lightSensor)
            SensorDetail("Barometer (pressure)", sensors.barometer)
            SensorDetail("Step counter", sensors.stepCounter)
            SensorDetail("Step detector", sensors.stepDetector)
            SensorDetail("Gravity sensor", sensors.gravity)
            SensorDetail("Rotation vector", sensors.rotationVector)
        }
    }
}

@Composable
private fun SensorTile(
    icon: ImageVector,
    label: String,
    accent: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun SensorDetail(label: String, value: String) {
    val isPresent = !value.equals("Not present", ignoreCase = true) && value != "—"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isPresent) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (isPresent) TextPrimary else TextSecondary,
            maxLines = 2,
        )
    }
}
