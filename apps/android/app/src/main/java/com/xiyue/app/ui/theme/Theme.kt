package com.xiyue.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val XiyueDarkColorScheme = darkColorScheme(
    primary = XiyueAccent,
    onPrimary = XiyueBackgroundDeep,
    primaryContainer = XiyueAccentSoft,
    onPrimaryContainer = XiyueAccentStrong,
    secondary = XiyueAccentStrong,
    onSecondary = XiyueBackgroundDeep,
    tertiary = XiyueGold,
    onTertiary = XiyueBackgroundDeep,
    tertiaryContainer = XiyueGoldSoft,
    onTertiaryContainer = XiyueGoldStrong,
    background = XiyueBackground,
    onBackground = XiyueOnSurface,
    surface = XiyueSurface,
    onSurface = XiyueOnSurface,
    surfaceVariant = XiyueSurfaceVariant,
    onSurfaceVariant = XiyueOnSurfaceMuted,
    outline = XiyueOutline,
)

private val XiyueLightColorScheme = lightColorScheme(
    primary = ColorPalette.Light.primary,
    onPrimary = ColorPalette.Light.onPrimary,
    primaryContainer = ColorPalette.Light.primaryContainer,
    onPrimaryContainer = ColorPalette.Light.onPrimaryContainer,
    secondary = ColorPalette.Light.secondary,
    onSecondary = ColorPalette.Light.onSecondary,
    secondaryContainer = ColorPalette.Light.secondaryContainer,
    onSecondaryContainer = ColorPalette.Light.onSecondaryContainer,
    tertiary = ColorPalette.Light.tertiary,
    onTertiary = ColorPalette.Light.onTertiary,
    tertiaryContainer = ColorPalette.Light.tertiaryContainer,
    onTertiaryContainer = ColorPalette.Light.onTertiaryContainer,
    error = ColorPalette.Light.error,
    onError = ColorPalette.Light.onError,
    errorContainer = ColorPalette.Light.errorContainer,
    onErrorContainer = ColorPalette.Light.onErrorContainer,
    background = ColorPalette.Light.background,
    onBackground = ColorPalette.Light.onBackground,
    surface = ColorPalette.Light.surface,
    onSurface = ColorPalette.Light.onSurface,
    surfaceVariant = ColorPalette.Light.surfaceVariant,
    onSurfaceVariant = ColorPalette.Light.onSurfaceVariant,
    outline = ColorPalette.Light.outline,
    outlineVariant = ColorPalette.Light.outlineVariant,
)

@Composable
fun XiyueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> XiyueDarkColorScheme
        else -> XiyueLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = XiyueTypography,
        content = content,
    )
}
