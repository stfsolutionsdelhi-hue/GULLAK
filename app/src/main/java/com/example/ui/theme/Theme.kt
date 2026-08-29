package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GullakPrimaryLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = GullakGoldContainer,
    onPrimaryContainer = GullakGold,
    secondary = GullakGold,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = GullakClay,
    onSecondaryContainer = Color.White,
    tertiary = GullakSuccess,
    onTertiary = Color.White,
    tertiaryContainer = GullakSuccessContainer,
    onTertiaryContainer = GullakSuccessBright,
    background = GullakNavyDark,
    surface = GullakSurfaceDark,
    surfaceVariant = GullakSurfaceVariantDark,
    outline = GullakCardBorderDark,
    outlineVariant = GullakCardBorderDark.copy(alpha = 0.6f),
    onBackground = GullakTextPrimary,
    onSurface = GullakTextPrimary,
    onSurfaceVariant = GullakTextSecondary
)

private val LightColorScheme = DarkColorScheme // Always maintain dark luxury theme as default as requested in prompt

@Composable
fun GullakSocietyTheme(
    darkTheme: Boolean = true, // Force rich dark theme with gold & emerald
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}


