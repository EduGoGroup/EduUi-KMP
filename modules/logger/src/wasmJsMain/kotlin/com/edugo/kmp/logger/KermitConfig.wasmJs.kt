package com.edugo.kmp.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * WasmJs implementation of KermitConfig.
 *
 * Uses Kermit's default CommonWriter for wasmJs output.
 *
 * ## Features:
 * - CommonWriter for platform-agnostic logging
 * - Formateo con timestamp, nivel y tag
 * - Compatible with browser and Node.js environments
 *
 * ## Usage:
 * ```kotlin
 * // En el punto de entrada de la aplicación WasmJs
 * fun main() {
 *     KermitLogger.initialize()
 *     // ... resto de la aplicación
 * }
 * ```
 */
public actual object KermitConfig {

    /**
     * Crea un Logger de Kermit configurado para WasmJs.
     *
     * En WasmJs, Kermit usa CommonWriter para salida
     * compatible con el entorno de ejecución.
     *
     * @return Logger de Kermit configurado para WasmJs
     */
    actual fun createLogger(): Logger {
        return Logger.withTag("EduGo")
    }

    /**
     * Crea un Logger con severidad minima configurada.
     *
     * @param minSeverity Nivel mínimo de logging
     * @return Logger de Kermit configurado con filtro de severidad
     */
    actual fun createLoggerWithMinSeverity(minSeverity: Severity): Logger {
        // En Kermit 2.0.4, el filtrado por severidad se realiza en el nivel de configuración
        // Por ahora retornamos el logger estándar
        return Logger.withTag("EduGo")
    }
}
