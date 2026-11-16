package com.usj.hikingplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ForestGreen = Color(0xFF2E6B4F)
private val PineDark = Color(0xFF0F2A1F)
private val Moss = Color(0xFFA8D5BA)
private val Sun = Color(0xFFF6C453)

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBD9),
    onPrimaryContainer = PineDark,
    secondary = Color(0xFF5B6F63),
    secondaryContainer = Color(0xFFDDE9E1),
    tertiary = Color(0xFF8A6A12),
    tertiaryContainer = Color(0xFFFFE6A8),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFF7FAF7),
    // Card, bottom bar and dialog backgrounds: soft greens instead of Material's default purple.
    surfaceVariant = Color(0xFFDCE5DE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F5F1),
    surfaceContainer = Color(0xFFEAF1EC),
    surfaceContainerHigh = Color(0xFFE4ECE6),
    surfaceContainerHighest = Color(0xFFDEE7E0),
    outlineVariant = Color(0xFFC0CCC3),
)

private val DarkColors = darkColorScheme(
    primary = Moss,
    onPrimary = PineDark,
    primaryContainer = Color(0xFF1E4D38),
    onPrimaryContainer = Color(0xFFCDEBD9),
    secondary = Color(0xFFBCCCC2),
    secondaryContainer = Color(0xFF3A4A41),
    tertiary = Sun,
    tertiaryContainer = Color(0xFF5E4800),
    background = Color(0xFF101512),
    surface = Color(0xFF101512),
    surfaceVariant = Color(0xFF3F4943),
    surfaceContainerLowest = Color(0xFF0B0F0D),
    surfaceContainerLow = Color(0xFF181D1A),
    surfaceContainer = Color(0xFF1C211E),
    surfaceContainerHigh = Color(0xFF262B28),
    surfaceContainerHighest = Color(0xFF313633),
    outlineVariant = Color(0xFF3F4943),
)

@Composable
fun HikingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
