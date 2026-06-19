package com.example.relatorios.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = darkColorScheme(
    primary          = AppBlue,
    onPrimary        = Color.White,
    primaryContainer = AppBlueDark,
    onPrimaryContainer = Color.White,
    secondary        = AppBlueLight,
    onSecondary      = Color.White,
    tertiary         = AppBlueSurface,
    onTertiary       = Color.White,
    background       = AppBlueDeep,
    onBackground     = Color.White,
    surface          = AppBlueDark,
    onSurface        = Color.White,
    error            = Color(0xFFCF6679),
    onError          = Color.White
)

@Composable
fun RelatoriosTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
