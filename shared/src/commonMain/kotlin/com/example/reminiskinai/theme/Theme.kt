package com.example.reminiskinai.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReminiDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = ReminiBlack,
    secondary = ReminiCrimson,
    onSecondary = Color.White,
    tertiary = ReminiGold,
    background = ReminiBlack,
    onBackground = ReminiTextPrimary,
    surface = ReminiSurface,
    onSurface = ReminiTextPrimary,
    surfaceVariant = ReminiCard,
    onSurfaceVariant = ReminiTextSecondary
)

@Composable
fun ReminiSkinAITheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ReminiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
