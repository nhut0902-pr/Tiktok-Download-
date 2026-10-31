package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF001F24),
    primaryContainer = Color(0xFF00363F),
    onPrimaryContainer = ElectricCyan,
    secondary = NeonCoral,
    onSecondary = Color.White,
    secondaryContainer = NeonCoralSoft,
    onSecondaryContainer = Color(0xFFFFB2C1),
    tertiary = GoldBadge,
    onTertiary = Color(0xFF261900),
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder,
    error = ErrorCrimson,
    onError = Color.White
)

@Composable
fun TikStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
