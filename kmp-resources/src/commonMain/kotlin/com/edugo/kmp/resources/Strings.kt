package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

/**
 * Interface para acceso a strings localizados multiplataforma.
 *
 * Implementacion:
 * - Android: usa strings.xml con recursos nativos
 * - iOS/Desktop/Wasm: usa strings hardcoded
 */
expect object Strings {
    // Splash
    val splash_title: String
    val splash_subtitle: String
    val splash_loading: String

    // Login
    val login_title: String
    val login_email_label: String
    val login_password_label: String
    val login_button: String
    val login_error_empty_fields: String

    // Home
    val home_welcome: String
    val home_subtitle: String
    val home_card_title: String
    val home_card_description: String
    val home_settings_button: String
    val home_logout_button: String

    // Settings
    val settings_title: String
    val settings_theme_section: String
    val settings_theme_light: String
    val settings_theme_dark: String
    val settings_theme_system: String
    val settings_reset_button: String
    val settings_logout_button: String

    // Messaging System
    val message_error_title: String
    val message_error_retry: String
    val message_error_dismiss: String
    val message_success_title: String
    val message_success_ok: String
    val message_warning_title: String
    val message_warning_understood: String
    val message_info_title: String
    val message_info_ok: String

    // Common
    val app_name: String
    val back_button: String
    val error_unknown: String
}

/**
 * Helper composable para obtener strings por clave.
 */
@Composable
expect fun stringResource(key: String): String
