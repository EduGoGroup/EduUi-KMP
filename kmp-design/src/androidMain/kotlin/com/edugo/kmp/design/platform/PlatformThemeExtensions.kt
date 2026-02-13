package com.edugo.kmp.design.platform

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual object PlatformThemeExtensions {
    @Composable
    actual fun applyPlatformColors(
        colorScheme: ColorScheme,
        darkTheme: Boolean
    ): ColorScheme {
        return if (supportsDynamicColor) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            colorScheme
        }
    }

    actual val supportsDynamicColor: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
