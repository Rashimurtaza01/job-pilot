package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryWhite,
    onPrimary = PureBlack,
    primaryContainer = Graphite,
    onPrimaryContainer = PrimaryWhite,
    secondary = NeutralGray,
    onSecondary = PrimaryWhite,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = PrimaryWhite,
    background = PureBlack,
    onBackground = PrimaryWhite,
    surface = DarkSurface,
    onSurface = PrimaryWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = MutedTextDark,
    outline = DarkBorder,
    outlineVariant = Graphite
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PrimaryWhite,
    primaryContainer = LightSurface,
    onPrimaryContainer = PureBlack,
    secondary = MutedText,
    onSecondary = PrimaryWhite,
    secondaryContainer = LightSurface,
    onSecondaryContainer = PureBlack,
    background = PrimaryWhite,
    onBackground = PureBlack,
    surface = LightSurface,
    onSurface = PureBlack,
    surfaceVariant = LightCard,
    onSurfaceVariant = MutedText,
    outline = LightBorder,
    outlineVariant = LightBorder
)

@Composable
fun JobPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
