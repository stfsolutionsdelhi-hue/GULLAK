package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryGreenDark,
    secondary = AccentGold,
    background = BgDark,
    surface = CardDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun GullakSocietyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
