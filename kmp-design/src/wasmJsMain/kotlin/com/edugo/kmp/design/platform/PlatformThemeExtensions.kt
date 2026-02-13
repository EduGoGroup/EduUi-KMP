package com.edugo.kmp.design.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

actual object PlatformThemeExtensions {
    @Composable
    actual fun applyPlatformColors(
        colorScheme: ColorScheme,
        darkTheme: Boolean
    ): ColorScheme = colorScheme

    actual val supportsDynamicColor: Boolean = false
}
