package com.edugo.kmp.resources

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Implementacion Android de Strings usando recursos nativos (strings.xml).
 */
actual object Strings {
    private lateinit var appContext: Context

    /**
     * Debe ser inicializado desde Application.onCreate().
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getString(resId: Int): String {
        return if (::appContext.isInitialized) {
            appContext.getString(resId)
        } else {
            "String not initialized"
        }
    }

    // Splash
    actual val splash_title: String get() = getString(R.string.splash_title)
    actual val splash_subtitle: String get() = getString(R.string.splash_subtitle)
    actual val splash_loading: String get() = getString(R.string.splash_loading)

    // Login
    actual val login_title: String get() = getString(R.string.login_title)
    actual val login_email_label: String get() = getString(R.string.login_email_label)
    actual val login_password_label: String get() = getString(R.string.login_password_label)
    actual val login_button: String get() = getString(R.string.login_button)
    actual val login_error_empty_fields: String get() = getString(R.string.login_error_empty_fields)

    // Home
    actual val home_welcome: String get() = getString(R.string.home_welcome)
    actual val home_subtitle: String get() = getString(R.string.home_subtitle)
    actual val home_card_title: String get() = getString(R.string.home_card_title)
    actual val home_card_description: String get() = getString(R.string.home_card_description)
    actual val home_settings_button: String get() = getString(R.string.home_settings_button)
    actual val home_logout_button: String get() = getString(R.string.home_logout_button)

    // Settings
    actual val settings_title: String get() = getString(R.string.settings_title)
    actual val settings_theme_section: String get() = getString(R.string.settings_theme_section)
    actual val settings_theme_light: String get() = getString(R.string.settings_theme_light)
    actual val settings_theme_dark: String get() = getString(R.string.settings_theme_dark)
    actual val settings_theme_system: String get() = getString(R.string.settings_theme_system)
    actual val settings_reset_button: String get() = getString(R.string.settings_reset_button)
    actual val settings_logout_button: String get() = getString(R.string.settings_logout_button)

    // Messaging System
    actual val message_error_title: String get() = getString(R.string.message_error_title)
    actual val message_error_retry: String get() = getString(R.string.message_error_retry)
    actual val message_error_dismiss: String get() = getString(R.string.message_error_dismiss)
    actual val message_success_title: String get() = getString(R.string.message_success_title)
    actual val message_success_ok: String get() = getString(R.string.message_success_ok)
    actual val message_warning_title: String get() = getString(R.string.message_warning_title)
    actual val message_warning_understood: String get() = getString(R.string.message_warning_understood)
    actual val message_info_title: String get() = getString(R.string.message_info_title)
    actual val message_info_ok: String get() = getString(R.string.message_info_ok)

    // Common
    actual val app_name: String get() = getString(R.string.app_name)
    actual val back_button: String get() = getString(R.string.back_button)
    actual val error_unknown: String get() = getString(R.string.error_unknown)
}

@Composable
actual fun stringResource(key: String): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else key
}
