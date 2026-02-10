package com.edugo.kmp.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EduGoLightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = Color(0xFFFFA726),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFFE65100),

    tertiary = Color(0xFF66BB6A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Color(0xFF2E7D32),

    error = Color(0xFFE53935),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFC62828),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
)

private val EduGoDarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFFE65100),
    secondaryContainer = Color(0xFFF57C00),
    onSecondaryContainer = Color(0xFFFFE0B2),

    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF388E3C),
    onTertiaryContainer = Color(0xFFC8E6C9),

    error = Color(0xFFEF5350),
    onError = Color(0xFFFFEBEE),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFCDD2),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
)

/**
 * Theme principal de EduGo.
 * Aplica Material 3 con colores personalizados y tipografía.
 * Soporta modo oscuro automático.
 */
@Composable
fun EduGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        EduGoDarkColors
    } else {
        EduGoLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduGoTypography,
        content = content
    )
}
