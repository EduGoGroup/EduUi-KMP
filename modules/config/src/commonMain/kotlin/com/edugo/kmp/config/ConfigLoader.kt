package com.edugo.kmp.config

import kotlinx.serialization.json.Json

/**
 * Cargador de configuración desde archivos JSON.
 *
 * Lee archivos de resources/config/ según el ambiente especificado
 * y deserializa el contenido en un objeto AppConfig.
 *
 * Uso:
 * ```kotlin
 * val config = ConfigLoader.load(Environment.DEV)
 * println(config.apiUrl) // "http://localhost"
 * ```
 */
public object ConfigLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Carga la configuración para el ambiente especificado.
     *
     * @param environment Ambiente a cargar (DEV, STAGING, PROD)
     * @return AppConfig con la configuración cargada
     * @throws IllegalStateException si el archivo no existe o el JSON es inválido
     */
    public fun load(environment: Environment): AppConfig {
        val fileName = "config/${environment.fileName}.json"
        val jsonContent = loadResourceAsString(fileName)
            ?: error("No se encontró el archivo de configuración: $fileName")

        return try {
            json.decodeFromString<AppConfigImpl>(jsonContent)
        } catch (e: Exception) {
            error("Error al parsear configuración $fileName: ${e.message}")
        }
    }

    /**
     * Carga la configuración desde un string (útil para testing).
     *
     * @param jsonString JSON string con la configuración
     * @return AppConfig con la configuración parseada
     */
    public fun loadFromString(jsonString: String): AppConfig {
        return json.decodeFromString<AppConfigImpl>(jsonString)
    }
}

/**
 * Función expect para cargar recursos como string.
 * Cada plataforma implementa su versión específica.
 *
 * @param path Ruta relativa del recurso en resources/
 * @return Contenido del archivo como string, o null si no existe
 */
internal expect fun loadResourceAsString(path: String): String?
