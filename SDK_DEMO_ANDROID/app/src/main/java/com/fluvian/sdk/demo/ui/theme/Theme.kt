package com.fluvian.sdk.demo.ui.theme

/**
 * File: Theme.kt
 * Description: Material 3 theme wiring for the Fluvian demo application (dark brand baseline).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Wrap demo composables with [FluvianSdkTheme] to inherit typography and colors.
 *
 * Usage example:
 *   FluvianSdkTheme { Surface { ... } }
 */
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
fun FluvianSdkTheme(
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
