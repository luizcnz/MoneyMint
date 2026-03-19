package com.example.moneymint.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    tertiary = NeonPink,
    background = CyberBlack,
    surface = CyberDarkGray,
    onPrimary = Color.White,
    onSecondary = CyberBlack,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = NeonRed
)

@Composable
fun MoneyMintTheme(
    darkTheme: Boolean = true, // Siempre oscuro para el estilo Cyberpunk
    dynamicColor: Boolean = false, // Desactivamos el color dinámico para mantener nuestro estilo
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CyberBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
