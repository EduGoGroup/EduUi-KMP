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
         * Obtiene el ambiente desde un string.
         * Retorna null si el string no corresponde a ningún ambiente válido.
         *
         * @param value String del ambiente (case-insensitive)
         * @return Environment correspondiente o null si no se encuentra
         */
        public fun fromString(value: String?): Environment? {
            if (value == null) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        /**
         * Obtiene el ambiente desde un string con un fallback.
         *
         * @param value String del ambiente (case-insensitive)
         * @param default Ambiente a retornar si value no es válido (por defecto DEV)
         * @return Environment correspondiente o el default
         */
        public fun fromStringOrDefault(value: String?, default: Environment = DEV): Environment {
            return fromString(value) ?: default
        }
    }
}
