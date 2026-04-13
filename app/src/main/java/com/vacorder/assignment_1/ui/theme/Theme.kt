package com.vacorder.assignment_1.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val VacorderColorScheme = lightColorScheme(
    primary = VacorderBlack,
    onPrimary = VacorderWhite,
    primaryContainer = VacorderLavender,
    onPrimaryContainer = VacorderBlack,
    secondary = VacorderLavender,
    onSecondary = VacorderBlack,
    secondaryContainer = VacorderLavender,
    onSecondaryContainer = VacorderBlack,
    background = VacorderWhite,
    onBackground = VacorderBlack,
    surface = VacorderWhite,
    onSurface = VacorderBlack,
    surfaceVariant = VacorderLavender,
    onSurfaceVariant = VacorderBlack,
    outline = VacorderBlack.copy(alpha = 0.3f)
)

@Composable
fun VacorderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VacorderColorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(fontFamily = InterFamily),
            content = content
        )
    }
}
