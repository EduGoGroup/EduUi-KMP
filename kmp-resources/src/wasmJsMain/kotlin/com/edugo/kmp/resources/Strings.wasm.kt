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

    // Toolbar
    actual val toolbar_back: String = "Volver"
    actual val toolbar_new: String = "Nuevo"
    actual val toolbar_save: String = "Guardar"
    actual val toolbar_edit: String = "Editar"

    // Menu
    actual val menu_switch_context: String = "Cambiar contexto"
    actual val menu_logout: String = "Cerrar sesión"

    // School selection
    actual val school_selection_title: String = "Seleccionar escuela"
    actual val school_selection_subtitle: String = "Selecciona una escuela para continuar"
    actual val school_selection_empty: String = "No hay escuelas disponibles"
    actual fun school_selection_error(error: String): String = "Error cargando escuelas: $error"

    // Connectivity
    actual val offline_banner: String = "Sin conexión - usando datos guardados"
    actual fun offline_syncing(current: Int, total: Int): String = "Sincronizando $current/$total..."
    actual fun offline_pending(count: Int): String = "Sincronizando $count cambios pendientes..."
    actual val stale_data_indicator: String = "Datos en caché"

    // School selection (extra)
    actual val school_selection_description: String = "Selecciona la escuela con la que deseas trabajar"
    actual val syncing_data: String = "Sincronizando datos..."

    actual val form_field_required: String = "Este campo es obligatorio"
    actual val form_fix_errors: String = "Corrige los campos marcados"
    actual val select_loading: String = "Cargando..."
    actual val select_load_error: String = "Error al cargar opciones"

    actual val app_name: String = "EduGo"
    actual val back_button: String = "Volver"
    actual val error_unknown: String = "Ocurrió un error inesperado"
}

@Composable
actual fun stringResource(key: String): String = key

@Composable
actual fun InitStringsForPreview() { /* No-op: strings hardcoded */ }
