package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OffGridColorScheme = darkColorScheme(
    primary = EmergencyAmber,
    onPrimary = Color.Black,
    primaryContainer = TacticalDarkSurfaceVariant,
    onPrimaryContainer = EmergencyAmberLight,
    secondary = TacticalGreen,
    onSecondary = Color.Black,
    secondaryContainer = TacticalDarkSurfaceHighlight,
    onSecondaryContainer = TacticalGreenLight,
    tertiary = RadioCyan,
    onTertiary = Color.Black,
    background = TacticalDarkBackground,
    onBackground = TextPrimary,
    surface = TacticalDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = DistressRed,
    onError = Color.White,
    outline = BorderSubtle
)

@Composable
fun OffGridTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OffGridColorScheme,
        typography = Typography,
        content = content
    )
}
