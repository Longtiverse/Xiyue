package com.xiyue.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val XiyueColorScheme = darkColorScheme(
    primary = XiyueMint,
    secondary = XiyueTeal,
    tertiary = XiyueSlate,
    background = XiyueBackground,
    surface = XiyueSurface,
    surfaceVariant = XiyueSurfaceVariant,
    onSurface = XiyueOnSurface,
    onBackground = XiyueOnSurface,
)

@Composable
fun XiyueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = XiyueColorScheme,
        typography = XiyueTypography,
        content = content,
    )
}
