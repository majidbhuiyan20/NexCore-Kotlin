package com.matox.nexcore.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NexCoreDarkColorScheme = darkColorScheme(
    primary = NexCoreGreen,
    onPrimary = Background,
    primaryContainer = NexCoreGreenAccent,
    onPrimaryContainer = TextPrimary,
    secondary = MetricBlue,
    onSecondary = TextPrimary,
    tertiary = MetricPurple,
    onTertiary = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardStroke,
    error = MetricRed,
    onError = TextPrimary,
)

@Composable
fun NexCoreTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NexCoreDarkColorScheme,
        typography = NexCoreTypography,
        content = content,
    )
}
