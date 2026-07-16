package com.matox.nexcore.presentation.cpu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * "Per-Core Frequencies" card — one row per core with a horizontal
 * frequency bar scaled to the max reading across the visible cores.
 *
 * 0 MHz is rendered as "Off" so parked cores are visually distinct from
 * slow-running cores.
 */
@Composable
fun CpuCoresCard(
    perCoreFrequenciesMhz: List<Int>,
    modifier: Modifier = Modifier,
) {
    val maxFreq = perCoreFrequenciesMhz.maxOrNull()?.coerceAtLeast(1) ?: 1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricOrange.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.30f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.92f)),
                ),
            )
            .border(1.dp, CardStroke, RoundedCornerShape(22.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MetricOrange.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Memory,
                        contentDescription = null,
                        tint = MetricOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Per-Core Frequencies",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "${perCoreFrequenciesMhz.size} cores visible · live",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (perCoreFrequenciesMhz.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Per-core frequencies unavailable on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                return
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                perCoreFrequenciesMhz.forEachIndexed { idx, mhz ->
                    CoreRow(coreIndex = idx, freqMhz = mhz, maxFreqMhz = maxFreq)
                }
            }
        }
    }
}

@Composable
private fun CoreRow(coreIndex: Int, freqMhz: Int, maxFreqMhz: Int) {
    val isOff = freqMhz <= 0
    val frac = if (isOff) 0f else (freqMhz.toFloat() / maxFreqMhz.toFloat()).coerceIn(0f, 1f)
    val valueLabel = if (isOff) "Off" else "%.2f GHz".format(freqMhz / 1000f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface.copy(alpha = 0.5f))
            .border(1.dp, CardStroke, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MetricOrange.copy(alpha = 0.18f))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Core $coreIndex",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                ),
                color = MetricOrange,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(TrackGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(frac)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(MetricOrange.copy(alpha = 0.7f), MetricOrange),
                        ),
                    ),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (isOff) TextSecondary else TextPrimary,
                maxLines = 1,
            )
        }
    }
}
