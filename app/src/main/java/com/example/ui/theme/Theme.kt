package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = UzzapOrange,
    onPrimary = Color.White,
    primaryContainer = UzzapOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = UzzapCyan,
    onSecondary = Color.Black,
    secondaryContainer = UzzapNavyCard,
    onSecondaryContainer = Color.White,
    tertiary = UzzapOrangeLight,
    background = UzzapNavy,
    onBackground = Color.White,
    surface = UzzapNavyCard,
    onSurface = Color.White,
    surfaceVariant = UzzapNavySurface,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = UzzapNavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = UzzapOrange,
    onPrimary = Color.White,
    primaryContainer = UzzapOrangeContainer,
    onPrimaryContainer = UzzapOrangeDark,
    secondary = UzzapCyan,
    onSecondary = Color.White,
    secondaryContainer = UzzapCyanContainer,
    onSecondaryContainer = Color(0xFF00363A),
    tertiary = UzzapOrangeDark,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve iconic Uzzap branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

