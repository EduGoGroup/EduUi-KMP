package com.edugo.kmp.resources

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Implementacion Android de Strings usando recursos nativos (strings.xml).
 *
 * Requiere llamar [init] desde Application.onCreate().
 * En previews, si el contexto no esta disponible, usa fallbacks hardcoded.
 */
actual object Strings {
    private lateinit var appContext: Context

    /**
     * Debe ser inicializado desde Application.onCreate().
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Inicializa desde un contexto de Preview (no requiere applicationContext).
     */
    fun initFromPreview(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context
        }
    }

    private fun getString(resId: Int, fallback: String): String {
        if (!::appContext.isInitialized) return fallback
        return try {
            appContext.getString(resId)
        } catch (_: Resources.NotFoundException) {
            fallback
        }
    }

    // Splash
    actual val splash_title: String get() = getString(R.string.splash_title, "EduGo")
    actual val splash_subtitle: String get() = getString(R.string.splash_subtitle, "Plataforma Educativa")
    actual val splash_loading: String get() = getString(R.string.splash_loading, "Cargando…")

    // Login
    actual val login_title: String get() = getString(R.string.login_title, "Inicio de sesión")
    actual val login_email_label: String get() = getString(R.string.login_email_label, "Email")
    actual val login_password_label: String get() = getString(R.string.login_password_label, "Contraseña")
    actual val login_button: String get() = getString(R.string.login_button, "Iniciar sesión")
    actual val login_error_empty_fields: String get() = getString(R.string.login_error_empty_fields, "Email y contraseña son requeridos")

    // Home
    actual val home_welcome: String get() = getString(R.string.home_welcome, "¡Bienvenido!")
    actual val home_subtitle: String get() = getString(R.string.home_subtitle, "Has iniciado sesión exitosamente")
    actual val home_card_title: String get() = getString(R.string.home_card_title, "EduGo KMP")
    actual val home_card_description: String get() = getString(R.string.home_card_description, "Plataforma educativa multiplataforma con Kotlin")
    actual val home_settings_button: String get() = getString(R.string.home_settings_button, "Configuración")
    actual val home_logout_button: String get() = getString(R.string.home_logout_button, "Cerrar Sesión")

    // Settings
    actual val settings_title: String get() = getString(R.string.settings_title, "Configuración")
    actual val settings_theme_section: String get() = getString(R.string.settings_theme_section, "Tema")
    actual val settings_theme_light: String get() = getString(R.string.settings_theme_light, "Claro")
    actual val settings_theme_dark: String get() = getString(R.string.settings_theme_dark, "Oscuro")
    actual val settings_theme_system: String get() = getString(R.string.settings_theme_system, "Sistema")
    actual val settings_reset_button: String get() = getString(R.string.settings_reset_button, "Restablecer configuración")
    actual val settings_logout_button: String get() = getString(R.string.settings_logout_button, "Cerrar Sesión")

    // Messaging System
    actual val message_error_title: String get() = getString(R.string.message_error_title, "Error")
    actual val message_error_retry: String get() = getString(R.string.message_error_retry, "Reintentar")
    actual val message_error_dismiss: String get() = getString(R.string.message_error_dismiss, "Cerrar")
    actual val message_success_title: String get() = getString(R.string.message_success_title, "Éxito")
    actual val message_success_ok: String get() = getString(R.string.message_success_ok, "Aceptar")
    actual val message_warning_title: String get() = getString(R.string.message_warning_title, "Advertencia")
    actual val message_warning_understood: String get() = getString(R.string.message_warning_understood, "Entendido")
    actual val message_info_title: String get() = getString(R.string.message_info_title, "Información")
    actual val message_info_ok: String get() = getString(R.string.message_info_ok, "Aceptar")

    // Common
    actual val app_name: String get() = getString(R.string.app_name, "EduGo")
    actual val back_button: String get() = getString(R.string.back_button, "Volver")
    actual val error_unknown: String get() = getString(R.string.error_unknown, "Ocurrió un error inesperado")
}

@Composable
actual fun stringResource(key: String): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else key
}

@Composable
actual fun InitStringsForPreview() {
    Strings.initFromPreview(LocalContext.current)
}
