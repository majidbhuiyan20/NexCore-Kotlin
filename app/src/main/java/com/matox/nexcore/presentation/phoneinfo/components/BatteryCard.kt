package com.matox.nexcore.presentation.phoneinfo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.DonutChart
import com.matox.nexcore.core.ui.components.DonutSegment
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.BatteryInfo
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Battery section card with a donut chart showing percentage and a
 * four-tile mini-grid below it: Level · Status · Temperature · Voltage.
 *
 * Layout stays strictly single-column — the donut + tiles live in one
 * vertical stack, no horizontal split.
 */
@Composable
fun BatteryCard(
    battery: BatteryInfo,
    modifier: Modifier = Modifier,
) {
    val accent = MetricAccent.GREEN
    val accentColor = accent.toColor()
    val pct = battery.levelPercent.coerceIn(0, 100)
    val ringColor = when {
        pct >= 60 -> NexCoreGreen
        pct >= 25 -> MetricOrange
        else -> MetricRed
    }

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
                icon = Icons.Outlined.Bolt,
                accent = accentColor,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Battery",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        // Donut centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            DonutChart(
                segments = listOf(
                    DonutSegment(value = pct.toFloat(), color = ringColor),
                    DonutSegment(value = (100 - pct).toFloat(), color = androidx.compose.ui.graphics.Color(0xFF22304A)),
                ),
                size = 132.dp,
                strokeWidth = 14.dp,
                trackColor = androidx.compose.ui.graphics.Color(0xFF22304A),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = battery.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        // Four mini-tiles in a vertical stack (one tile per row)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BatteryTile(label = "Technology", value = battery.technology)
            BatteryTile(label = "Temperature", value = "${battery.temperatureC}°C")
            BatteryTile(label = "Voltage", value = "${battery.voltageMv} mV")
            BatteryTile(label = "Health", value = battery.health)
            BatteryTile(label = "Power source", value = battery.plugged)
        }
    }
}

@Composable
private fun BatteryTile(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
        )
    }
}
