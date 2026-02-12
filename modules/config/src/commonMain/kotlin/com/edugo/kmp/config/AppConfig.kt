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
     * URL base del API backend.
     */
    public val apiUrl: String

    /**
     * Puerto del API backend.
     */
    public val apiPort: Int

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

    /**
     * Construye la URL completa del API.
     * @return URL completa en formato "http://localhost:8080"
     */
    public fun getFullApiUrl(): String = "$apiUrl:$apiPort"
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
    override val apiUrl: String,
    override val apiPort: Int,
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
