package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

/**
 * Interface para acceso a strings localizados multiplataforma.
 *
 * Implementación:
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

    // Toolbar
    val toolbar_back: String
    val toolbar_new: String
    val toolbar_save: String
    val toolbar_edit: String

    // Menu
    val menu_switch_context: String
    val menu_logout: String

    // School selection
    val school_selection_title: String
    val school_selection_subtitle: String
    val school_selection_empty: String
    fun school_selection_error(error: String): String

    // Connectivity
    val offline_banner: String
    fun offline_syncing(current: Int, total: Int): String
    fun offline_pending(count: Int): String
    val stale_data_indicator: String

    // School selection (extra)
    val school_selection_description: String
    val syncing_data: String

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

/**
 * Inicializa Strings para previews de Android Studio.
 * En Android obtiene el contexto via LocalContext; en otras plataformas es no-op.
 */
@Composable
expect fun InitStringsForPreview()
