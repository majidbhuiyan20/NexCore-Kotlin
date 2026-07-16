package com.matox.nexcore.presentation.sensors.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.SensorLiveMotion
import com.matox.nexcore.domain.model.SensorSnapshot
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricTeal
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import kotlin.math.sqrt

/**
 * Hero card for the Sensor Monitor screen.
 *
 * Layout (top → bottom):
 *  ┌──────────────────────────────────────────────┐
 *  │ [icon] Motion      [Status pill: Live · cold]  │
 *  │ Sensor telemetry                              │
 *  │                                              │
 *  │           ╭───────╮                          │
 *  │           │ 9.81  │   animated ring          │
 *  │           │ m/s²  │                          │
 *  │           ╰───────╯                          │
 *  │                                              │
 *  │  Accel magnitude     Gyro magnitude           │
 *  │  9.81 m/s²           0.04 rad/s               │
 *  │  ────                ────                     │
 *  └──────────────────────────────────────────────┘
 *
 * Glass surface: 24 dp corners, 1 dp border, soft teal shadow.
 * The big number is the *current* accelerometer magnitude so the
 * ring animates every time the user moves the device.
 */
@Composable
fun SensorHeroCard(
    snapshot: SensorSnapshot,
    liveMotion: SensorLiveMotion,
    modifier: Modifier = Modifier,
) {
    val accel = snapshot.accelerometer
    val gyro = snapshot.gyroscope

    val accelMagnitude = liveMotion.accelerometerMagnitude
        .takeIf { it > 0f }
        ?: computeMagnitude(accel?.values)
    val gyroMagnitude = liveMotion.gyroscopeMagnitude
        .takeIf { it > 0f }
        ?: computeMagnitude(gyro?.values)

    // Approx expected range: gravity alone gives ~9.81 m/s²; any
    // motion produces 10..30 m/s². We chart a 0..30 m/s² range so
    // the ring has room to react when the user shakes the phone.
    val animatedAccel by animateFloatAsState(
        targetValue = accelMagnitude.coerceIn(0f, ACCEL_MAX),
        animationSpec = tween(durationMillis = 220, easing = LinearEasing),
        label = "sensor-hero-accel",
    )
    val animatedGyro by animateFloatAsState(
        targetValue = gyroMagnitude.coerceIn(0f, GYRO_MAX),
        animationSpec = tween(durationMillis = 220, easing = LinearEasing),
        label = "sensor-hero-gyro",
    )

    val (statusLabel, statusBg, statusFg) = statusPill(active = snapshot.activeCount > 0)
    val sweepColor = MetricTeal

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MetricTeal.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.92f)),
                ),
            )
            .border(1.dp, CardStroke, RoundedCornerShape(24.dp)),
    ) {
        // Glass highlight.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header row.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Motion in focus",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = subtitleFor(snapshot),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                PillBadge(
                    label = statusLabel,
                    bg = statusBg,
                    fg = statusFg,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ring + interior.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Soft outer pulse ring.
                val infinite = rememberInfiniteTransition(label = "sensor-hero-pulse")
                val pulseScale by infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "sensor-hero-pulse-scale",
                )
                val pulseAlpha by infinite.animateFloat(
                    initialValue = 0.18f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "sensor-hero-pulse-alpha",
                )
                Box(modifier = Modifier.size(248.dp * pulseScale)) {
                    Canvas(modifier = Modifier.size(248.dp)) {
                        val strokePx = 6f
                        drawCircle(
                            color = sweepColor.copy(alpha = pulseAlpha),
                            radius = (size.minDimension / 2f) - strokePx,
                        )
                    }
                }

                SensorRing(
                    percent = animatedAccel / ACCEL_MAX,
                    color = sweepColor,
                    sizeDp = 220,
                    strokeWidthDp = 18,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatAccel(accelMagnitude),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "m/s²",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Accel magnitude",
                        style = MaterialTheme.typography.labelSmall,
                        color = MetricTeal,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Accel/Gyro side-by-side mini stats.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MotionStatTile(
                    label = "Accel magnitude",
                    value = formatAccel(accelMagnitude),
                    unit = "m/s²",
                    accent = MetricTeal,
                    progress = (animatedAccel / ACCEL_MAX).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f),
                )
                MotionStatTile(
                    label = "Gyro magnitude",
                    value = formatGyro(gyroMagnitude),
                    unit = "rad/s",
                    accent = MetricOrange,
                    progress = (animatedGyro / GYRO_MAX).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PillBadge(label: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = fg,
        )
    }
}

@Composable
private fun SensorRing(
    percent: Float,
    color: Color,
    sizeDp: Int,
    strokeWidthDp: Int,
) {
    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val strokePx = strokeWidthDp.dp.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokePx)

        // Track.
        drawArc(
            color = Color(0xFF22304A),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )

        val sweep = (percent.coerceIn(0f, 1f)) * 360f
        if (sweep > 0f) {
            // Outer soft glow.
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = 0.30f),
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.0f),
                    ),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx + 8f),
            )
            // Solid arc.
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(color, MetricCyan, color),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
    }
}

@Composable
private fun MotionStatTile(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = accent,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF22304A)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accent.copy(alpha = 0.55f), accent),
                        ),
                    ),
            )
        }
    }
}

// --- formatting helpers ------------------------------------------------

private fun computeMagnitude(values: FloatArray?): Float {
    if (values == null || values.size < 3) return 0f
    return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
}

private fun formatAccel(v: Float): String {
    if (v <= 0f) return "0.00"
    return "%.2f".format(v)
}

private fun formatGyro(v: Float): String {
    if (v <= 0f) return "0.00"
    return "%.2f".format(v)
}

private fun subtitleFor(snapshot: SensorSnapshot): String {
    val count = snapshot.totalSensorCount
    val active = snapshot.activeCount
    return if (count == 0) {
        "OS reported no sensors"
    } else if (active == 0) {
        "$count sensors · waiting for events"
    } else {
        "$active / $count sensors streaming"
    }
}

private fun statusPill(active: Boolean): Triple<String, Color, Color> {
    return if (active) {
        Triple("Live", MetricTeal.copy(alpha = 0.18f), MetricTeal)
    } else {
        Triple("Idle", Color(0xFF22304A), TextSecondary)
    }
}

// --- constants ----------------------------------------------------------

private const val ACCEL_MAX: Float = 30f
private const val GYRO_MAX: Float = 6f
