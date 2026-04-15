package com.monigarr.streamkit.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandAccentPurple,
    secondary = BrandAccentGreen,
    tertiary = BrandAccentRed,
    background = BrandBackground,
    surface = BrandSurface,
    onBackground = BrandTextPrimary,
    onSurface = BrandTextPrimary,
    surfaceContainerHigh = BrandSurface,
)

@Composable
fun LVSPOCStreamKitTheme(
    darkTheme: Boolean = true, // Force dark theme for the brand look
    dynamicColor: Boolean = false, // Disable dynamic color to maintain brand identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
