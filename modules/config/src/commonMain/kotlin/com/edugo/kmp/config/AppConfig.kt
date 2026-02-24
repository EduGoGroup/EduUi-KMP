package com.edugo.kmp.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuración global de la aplicación.
 *
 * Contiene todas las propiedades necesarias para configurar
 * el comportamiento de la app en diferentes ambientes.
 *
 * Se carga desde archivos JSON en resources/config/ según el ambiente.
 */
public interface AppConfig {
    /**
     * Ambiente actual de ejecución.
     */
    public val environment: Environment

    /**
     * URL base del API IAM Platform (puerto 8070 en local).
     * Maneja endpoints de auth, roles, permisos y menú.
     */
    public val iamApiBaseUrl: String

    /**
     * URL base completa del API de administración (puerto 8060 en local).
     * Maneja endpoints de administración del negocio (escuelas, unidades, etc.).
     */
    public val adminApiBaseUrl: String

    /**
     * URL base completa del API mobile (puerto 8065 en local).
     * Maneja endpoints de materials y progress.
     */
    public val mobileApiBaseUrl: String

    /**
     * Puerto para la aplicación web (Wasm).
     */
    public val webPort: Int

    /**
     * Timeout en milisegundos para peticiones HTTP.
     */
    public val timeout: Long

    /**
     * Indica si el modo debug está activo.
     */
    public val debugMode: Boolean

    /**
     * Indica si el modo mock está activo.
     *
     * Cuando es true, los repositorios de red se reemplazan por mocks
     * que devuelven datos coherentes sin necesidad de backend.
     * Siempre es false en PROD por seguridad.
     */
    public val mockMode: Boolean
}

/**
 * Implementación de AppConfig con serialización JSON.
 *
 * Esta clase se usa internamente por ConfigLoader para
 * deserializar los archivos JSON de configuración.
 */
@Serializable
public data class AppConfigImpl(
    private val environmentName: String,
    override val iamApiBaseUrl: String,
    override val adminApiBaseUrl: String,
    override val mobileApiBaseUrl: String,
    override val webPort: Int,
    override val timeout: Long,
    override val debugMode: Boolean,
    @SerialName("mockMode")
    private val mockModeValue: Boolean = false
) : AppConfig {
    override val environment: Environment
        get() = Environment.fromStringOrDefault(environmentName, Environment.DEV)

    override val mockMode: Boolean
        get() = if (environment == Environment.PROD) false else mockModeValue
}
