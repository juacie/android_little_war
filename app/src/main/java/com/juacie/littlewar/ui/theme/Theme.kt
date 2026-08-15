package com.juacie.littlewar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = WarGold,
    onPrimary = WarNavy,
    primaryContainer = WarGoldDark,
    onPrimaryContainer = WarParchment,
    secondary = WarCrimson,
    background = WarNavy,
    surface = WarNavyLight,
    onBackground = WarParchment,
    onSurface = WarParchment,
    error = WarCrimson
)

private val LightColors = lightColorScheme(
    primary = WarGoldDark,
    onPrimary = WarParchment,
    primaryContainer = WarGold,
    onPrimaryContainer = WarNavy,
    secondary = WarCrimson,
    background = WarParchment,
    surface = Color(0xFFFFFFFF),
    onBackground = WarNavy,
    onSurface = WarNavy,
    error = WarCrimson
)

@Composable
fun LittleWarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LittleWarTypography,
        content = content
    )
}
