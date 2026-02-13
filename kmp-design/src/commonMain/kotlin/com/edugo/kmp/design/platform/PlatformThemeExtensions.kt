package com.edugo.kmp.design.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Platform-specific theme extensions.
 */
expect object PlatformThemeExtensions {
    /**
     * Apply platform-specific color adjustments.
     * On Android 12+, this may return Dynamic Colors.
     * On other platforms, returns the provided scheme unchanged.
     */
    @Composable
    fun applyPlatformColors(
        colorScheme: ColorScheme,
        darkTheme: Boolean
    ): ColorScheme

    /**
     * Whether the current platform supports dynamic theming (Material You).
     */
    val supportsDynamicColor: Boolean
}
