package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLoggerImpl
import co.touchlab.kermit.Severity
import com.edugo.kmp.core.platform.PlatformVolatile

/**
 * Wrapper/alternativa al Logger expect/actual que usa Kermit 2.0.4 como backend.
 *
 * Este wrapper permite usar Kermit como sistema de logging mientras mantiene
 * compatibilidad con la interfaz Logger existente. Kermit proporciona
 * logging multiplataforma con configuraciones especificas por plataforma.
 *
 * ## Caracteristicas:
 * - Android: Usa Logcat para logging
 * - JVM: Console logger con colores ANSI
 * - JS: Console logger (console.log/warn/error)
 * - Formatters personalizables (timestamp, thread, class)
 * - Multiples loggers simultaneos
 *
 * ## Uso:
 * ```kotlin
 * // Inicializar Kermit con configuracion especifica por plataforma
 * KermitLogger.initialize()
 *
 * // Usar el wrapper para logging
 * KermitLogger.debug("NetworkClient", "Request sent")
 * KermitLogger.info("AuthManager", "User logged in")
 * KermitLogger.error("Repository", "Failed to save", exception)
 * ```
 *
 * @see Logger Interfaz original expect/actual (no modificada)
 * @see KermitConfig Configuracion especifica por plataforma
 */
public object KermitLogger {
    private var kermitInstance: KermitLoggerImpl = KermitLoggerImpl

    @PlatformVolatile
    private var isInitialized: Boolean = false

    /**
     * Inicializa Kermit con la configuracion especifica de la plataforma.
     *
     * Debe llamarse una vez al inicio de la aplicacion, preferiblemente
     * en el punto de entrada de cada plataforma (Application.onCreate en Android,
     * main() en JVM, etc.).
     *
     * La configuracion especifica se obtiene de KermitConfig expect/actual.
     *
     * Es idempotente: llamadas subsecuentes son ignoradas.
     */
    public fun initialize() {
        if (isInitialized) return
        kermitInstance = KermitConfig.createLogger()
        isInitialized = true
    }

    /**
     * Permite configurar un logger personalizado de Kermit.
     *
     * Util para testing o configuraciones avanzadas.
     *
     * @param logger Instancia personalizada de Kermit Logger
     */
    public fun setLogger(logger: KermitLoggerImpl) {
        kermitInstance = logger
    }

    /**
     * Obtiene la instancia actual de Kermit Logger.
     *
     * @return Instancia de Kermit Logger configurada
     */
    public fun getLogger(): KermitLoggerImpl = kermitInstance

    /**
     * Logs a debug message.
     *
     * Mapea al nivel DEBUG de Kermit.
     *
     * @param tag Identificador de la fuente del log (nombre de clase)
     * @param message Mensaje a registrar
     */
    public fun debug(tag: String, message: String) {
        kermitInstance.d(tag = tag) { message }
    }

    /**
     * Logs an informational message.
     *
     * Mapea al nivel INFO de Kermit.
     *
     * @param tag Identificador de la fuente del log (nombre de clase)
     * @param message Mensaje a registrar
     */
    public fun info(tag: String, message: String) {
        kermitInstance.i(tag = tag) { message }
    }

    /**
     * Logs a warning message.
     *
     * Mapea al nivel WARN de Kermit.
     *
     * @param tag Identificador de la fuente del log (nombre de clase)
     * @param message Mensaje de advertencia a registrar
     */
    public fun warn(tag: String, message: String) {
        kermitInstance.w(tag = tag) { message }
    }

    /**
     * Logs an error message with optional exception.
     *
     * Mapea al nivel ERROR de Kermit.
     *
     * @param tag Identificador de la fuente del log (nombre de clase)
     * @param message Mensaje de error a registrar
     * @param throwable Excepcion opcional a incluir con stack trace
     */
    public fun error(tag: String, message: String, throwable: Throwable? = null) {
        kermitInstance.e(tag = tag, throwable = throwable) { message }
    }

    /**
     * Configura el nivel minimo de severidad para logging.
     *
     * NOTA: Kermit 2.0.4 usa un sistema de configuration basado en LogWriter.
     * Para cambiar la severidad minima, se debe configurar cada LogWriter
     * especificamente. Esta funcion permite recrear el logger con configuracion
     * personalizada desde KermitConfig.
     *
     * @param severity Nivel minimo de Kermit (Verbose, Debug, Info, Warn, Error, Assert)
     */
    public fun setMinSeverity(severity: Severity) {
        // Kermit 2.0.4 requiere configurar los LogWriters para filtrar por severidad
        // Recrear el logger con configuracion actualizada desde la plataforma
        kermitInstance = KermitConfig.createLoggerWithMinSeverity(severity)
    }
}

/**
 * Configuracion de Kermit especifica por plataforma.
 *
 * Implementado mediante expect/actual para cada target:
 * - Android: LogcatWriter
 * - JVM: ConsoleWriter con ANSI colors
 * - WasmJs: CommonWriter
 *
 * @see KermitConfig.android.kt
 * @see KermitConfig.jvm.kt
 * @see KermitConfig.wasmJs.kt
 */
public expect object KermitConfig {
    /**
     * Crea y configura una instancia de Kermit Logger especifica para la plataforma.
     *
     * @return Logger de Kermit configurado con writers y formatters apropiados
     */
    public fun createLogger(): KermitLoggerImpl

    /**
     * Crea un logger con severidad minima configurada.
     *
     * @param minSeverity Nivel minimo de logging
     * @return Logger de Kermit configurado con filtro de severidad
     */
    public fun createLoggerWithMinSeverity(minSeverity: Severity): KermitLoggerImpl
}
