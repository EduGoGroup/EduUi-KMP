package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

actual object Strings {
    actual val splash_title: String = "EduGo"
    actual val splash_subtitle: String = "Plataforma Educativa"
    actual val splash_loading: String = "Cargando…"

    actual val login_title: String = "Inicio de sesión"
    actual val login_email_label: String = "Email"
    actual val login_password_label: String = "Contraseña"
    actual val login_button: String = "Iniciar sesión"
    actual val login_error_empty_fields: String = "Email y contraseña son requeridos"

    actual val home_welcome: String = "¡Bienvenido!"
    actual val home_subtitle: String = "Has iniciado sesión exitosamente"
    actual val home_card_title: String = "EduGo KMP"
    actual val home_card_description: String = "Plataforma educativa multiplataforma con Kotlin"
    actual val home_settings_button: String = "Configuración"
    actual val home_logout_button: String = "Cerrar Sesión"

    actual val settings_title: String = "Configuración"
    actual val settings_theme_section: String = "Tema"
    actual val settings_theme_light: String = "Claro"
    actual val settings_theme_dark: String = "Oscuro"
    actual val settings_theme_system: String = "Sistema"
    actual val settings_reset_button: String = "Restablecer configuración"
    actual val settings_logout_button: String = "Cerrar Sesión"

    actual val message_error_title: String = "Error"
    actual val message_error_retry: String = "Reintentar"
    actual val message_error_dismiss: String = "Cerrar"
    actual val message_success_title: String = "Éxito"
    actual val message_success_ok: String = "Aceptar"
    actual val message_warning_title: String = "Advertencia"
    actual val message_warning_understood: String = "Entendido"
    actual val message_info_title: String = "Información"
    actual val message_info_ok: String = "Aceptar"

    actual val app_name: String = "EduGo"
    actual val back_button: String = "Volver"
    actual val error_unknown: String = "Ocurrió un error inesperado"
}

@Composable
actual fun stringResource(key: String): String = key

@Composable
actual fun InitStringsForPreview() { /* No-op: strings hardcoded */ }
