package com.edugo.kmp.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.edugo.kmp.design.tokens.ColorTokens
import com.edugo.kmp.design.tokens.ExtendedColorScheme
import com.edugo.kmp.design.tokens.LocalExtendedColorScheme

private val MD3LightColors = lightColorScheme(
    primary = ColorTokens.Light.primary,
    onPrimary = ColorTokens.Light.onPrimary,
    primaryContainer = ColorTokens.Light.primaryContainer,
    onPrimaryContainer = ColorTokens.Light.onPrimaryContainer,
    secondary = ColorTokens.Light.secondary,
    onSecondary = ColorTokens.Light.onSecondary,
    secondaryContainer = ColorTokens.Light.secondaryContainer,
    onSecondaryContainer = ColorTokens.Light.onSecondaryContainer,
    tertiary = ColorTokens.Light.tertiary,
    onTertiary = ColorTokens.Light.onTertiary,
    tertiaryContainer = ColorTokens.Light.tertiaryContainer,
    onTertiaryContainer = ColorTokens.Light.onTertiaryContainer,
    error = ColorTokens.Light.error,
    onError = ColorTokens.Light.onError,
    errorContainer = ColorTokens.Light.errorContainer,
    onErrorContainer = ColorTokens.Light.onErrorContainer,
    background = ColorTokens.Light.background,
    onBackground = ColorTokens.Light.onBackground,
    surface = ColorTokens.Light.surface,
    onSurface = ColorTokens.Light.onSurface,
    surfaceVariant = ColorTokens.Light.surfaceVariant,
    onSurfaceVariant = ColorTokens.Light.onSurfaceVariant,
    surfaceTint = ColorTokens.Light.surfaceTint,
    inverseSurface = ColorTokens.Light.inverseSurface,
    inverseOnSurface = ColorTokens.Light.inverseOnSurface,
    inversePrimary = ColorTokens.Light.inversePrimary,
    outline = ColorTokens.Light.outline,
    outlineVariant = ColorTokens.Light.outlineVariant,
    scrim = ColorTokens.Light.scrim,
    surfaceBright = ColorTokens.Light.surfaceBright,
    surfaceDim = ColorTokens.Light.surfaceDim,
    surfaceContainer = ColorTokens.Light.surfaceContainer,
    surfaceContainerHigh = ColorTokens.Light.surfaceContainerHigh,
    surfaceContainerHighest = ColorTokens.Light.surfaceContainerHighest,
    surfaceContainerLow = ColorTokens.Light.surfaceContainerLow,
    surfaceContainerLowest = ColorTokens.Light.surfaceContainerLowest,
)

private val MD3DarkColors = darkColorScheme(
    primary = ColorTokens.Dark.primary,
    onPrimary = ColorTokens.Dark.onPrimary,
    primaryContainer = ColorTokens.Dark.primaryContainer,
    onPrimaryContainer = ColorTokens.Dark.onPrimaryContainer,
    secondary = ColorTokens.Dark.secondary,
    onSecondary = ColorTokens.Dark.onSecondary,
    secondaryContainer = ColorTokens.Dark.secondaryContainer,
    onSecondaryContainer = ColorTokens.Dark.onSecondaryContainer,
    tertiary = ColorTokens.Dark.tertiary,
    onTertiary = ColorTokens.Dark.onTertiary,
    tertiaryContainer = ColorTokens.Dark.tertiaryContainer,
    onTertiaryContainer = ColorTokens.Dark.onTertiaryContainer,
    error = ColorTokens.Dark.error,
    onError = ColorTokens.Dark.onError,
    errorContainer = ColorTokens.Dark.errorContainer,
    onErrorContainer = ColorTokens.Dark.onErrorContainer,
    background = ColorTokens.Dark.background,
    onBackground = ColorTokens.Dark.onBackground,
    surface = ColorTokens.Dark.surface,
    onSurface = ColorTokens.Dark.onSurface,
    surfaceVariant = ColorTokens.Dark.surfaceVariant,
    onSurfaceVariant = ColorTokens.Dark.onSurfaceVariant,
    surfaceTint = ColorTokens.Dark.surfaceTint,
    inverseSurface = ColorTokens.Dark.inverseSurface,
    inverseOnSurface = ColorTokens.Dark.inverseOnSurface,
    inversePrimary = ColorTokens.Dark.inversePrimary,
    outline = ColorTokens.Dark.outline,
    outlineVariant = ColorTokens.Dark.outlineVariant,
    scrim = ColorTokens.Dark.scrim,
    surfaceBright = ColorTokens.Dark.surfaceBright,
    surfaceDim = ColorTokens.Dark.surfaceDim,
    surfaceContainer = ColorTokens.Dark.surfaceContainer,
    surfaceContainerHigh = ColorTokens.Dark.surfaceContainerHigh,
    surfaceContainerHighest = ColorTokens.Dark.surfaceContainerHighest,
    surfaceContainerLow = ColorTokens.Dark.surfaceContainerLow,
    surfaceContainerLowest = ColorTokens.Dark.surfaceContainerLowest,
)

/**
 * Theme principal de EduGo.
 * Aplica Material 3 con paleta MD3 por defecto y extended color scheme.
 * Soporta modo oscuro automático.
 */
@Composable
fun EduGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MD3DarkColors else MD3LightColors
    val extendedColors = if (darkTheme) ExtendedColorScheme.dark() else ExtendedColorScheme.light()

    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EduGoTypography,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                content = content
            )
        }
    }
}

/**
 * Acceso al extended color scheme desde cualquier composable dentro de EduGoTheme.
 */
object EduGoThemeExtensions {
    val extendedColors: ExtendedColorScheme
        @Composable
        get() = LocalExtendedColorScheme.current
}
