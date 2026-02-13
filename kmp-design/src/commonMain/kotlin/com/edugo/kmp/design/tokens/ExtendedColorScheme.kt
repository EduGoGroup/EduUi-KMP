package com.edugo.kmp.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color scheme for semantic colors not included in Material 3 ColorScheme.
 * Provides success, warning, and info color roles.
 */
@Immutable
data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
) {
    companion object {
        fun light() = ExtendedColorScheme(
            success = Color(0xFF386A20),
            onSuccess = Color(0xFFFFFFFF),
            successContainer = Color(0xFFB8F397),
            onSuccessContainer = Color(0xFF062100),
            warning = Color(0xFF7E5700),
            onWarning = Color(0xFFFFFFFF),
            warningContainer = Color(0xFFFFDEA6),
            onWarningContainer = Color(0xFF281900),
            info = Color(0xFF0061A4),
            onInfo = Color(0xFFFFFFFF),
            infoContainer = Color(0xFFD1E4FF),
            onInfoContainer = Color(0xFF001D36),
        )

        fun dark() = ExtendedColorScheme(
            success = Color(0xFF9DD67D),
            onSuccess = Color(0xFF113800),
            successContainer = Color(0xFF205107),
            onSuccessContainer = Color(0xFFB8F397),
            warning = Color(0xFFF5BF48),
            onWarning = Color(0xFF432C00),
            warningContainer = Color(0xFF604100),
            onWarningContainer = Color(0xFFFFDEA6),
            info = Color(0xFF9ECAFF),
            onInfo = Color(0xFF003258),
            infoContainer = Color(0xFF00497D),
            onInfoContainer = Color(0xFFD1E4FF),
        )
    }
}

val LocalExtendedColorScheme = staticCompositionLocalOf { ExtendedColorScheme.light() }
