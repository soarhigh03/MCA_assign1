package com.vacorder.assignment_1.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VacorderColorScheme = lightColorScheme(
    primary = VacorderYellow,
    onPrimary = VacorderNavy,
    primaryContainer = VacorderYellow,
    onPrimaryContainer = VacorderNavy,
    secondary = VacorderNavy,
    onSecondary = Color.White,
    secondaryContainer = VacorderNavyLight,
    onSecondaryContainer = Color.White,
    background = VacorderBg,
    onBackground = VacorderNavy,
    surface = Color.White,
    onSurface = VacorderNavy,
    surfaceVariant = VacorderBg,
    onSurfaceVariant = VacorderNavy,
    outline = VacorderNavy.copy(alpha = 0.3f)
)

@Composable
fun VacorderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VacorderColorScheme,
        typography = Typography,
        content = content
    )
}
