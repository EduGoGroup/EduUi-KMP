package com.edugo.kmp.config

/**
 * Representa los ambientes disponibles para la aplicación.
 *
 * Cada ambiente tiene su propia configuración en un archivo JSON separado:
 * - DEV: config/dev.json
 * - STAGING: config/staging.json
 * - PROD: config/prod.json
 */
public enum class Environment {
    /**
     * Ambiente de desarrollo local.
     * Típicamente usa localhost y tiene debug habilitado.
     */
    DEV,

    /**
     * Ambiente de staging/pruebas.
     * Usa servidores de staging para validación antes de producción.
     */
    STAGING,

    /**
     * Ambiente de producción.
     * Configuración optimizada para usuarios finales.
     */
    PROD;

    /**
     * Convierte el enum a lowercase string para nombrar archivos.
     * Ejemplo: Environment.DEV -> "dev"
     */
    public val fileName: String
        get() = name.lowercase()

    public companion object {
        /**
         * Obtiene el ambiente desde un string, con fallback a DEV.
         * @param value String del ambiente (case-insensitive)
         * @return Environment correspondiente o DEV si no se encuentra
         */
        public fun fromString(value: String?): Environment {
            return when (value?.uppercase()) {
                "DEV" -> DEV
                "STAGING" -> STAGING
                "PROD" -> PROD
                else -> DEV
            }
        }
    }
}
