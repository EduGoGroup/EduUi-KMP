package com.edugo.kmp.logger

/**
 * WasmJs implementation of [Logger].
 *
 * This class provides the WasmJs-specific logging implementation,
 * using println for output. In wasmJs browser environments, println
 * maps to console.log automatically.
 *
 * ## Output:
 * - d(), i() -> println (maps to console.log in browser)
 * - w() -> println with WARN prefix
 * - e() -> println with ERROR prefix
 *
 * ## Usage:
 * ```kotlin
 * val logger: Logger = WasmJsLogger()
 * logger.d("MyTag", "Debug message")
 * ```
 *
 * Prefer using [createDefaultLogger] to obtain instances.
 *
 * @see Logger
 * @see LoggerFormatter
 * @see createDefaultLogger
 */
class WasmJsLogger : Logger {

    override fun d(tag: String, message: String) {
        println(LoggerFormatter.format("DEBUG", tag, message))
    }

    override fun d(tag: String, message: String, throwable: Throwable) {
        println(LoggerFormatter.format("DEBUG", tag, message))
        println(throwable.stackTraceToString())
    }

    override fun i(tag: String, message: String) {
        println(LoggerFormatter.format("INFO", tag, message))
    }

    override fun i(tag: String, message: String, throwable: Throwable) {
        println(LoggerFormatter.format("INFO", tag, message))
        println(throwable.stackTraceToString())
    }

    override fun w(tag: String, message: String) {
        println(LoggerFormatter.format("WARN", tag, message))
    }

    override fun w(tag: String, message: String, throwable: Throwable) {
        println(LoggerFormatter.format("WARN", tag, message))
        println(throwable.stackTraceToString())
    }

    override fun e(tag: String, message: String) {
        println(LoggerFormatter.format("ERROR", tag, message))
    }

    override fun e(tag: String, message: String, throwable: Throwable) {
        println(LoggerFormatter.format("ERROR", tag, message))
        println(throwable.stackTraceToString())
    }
}

/**
 * Creates the default WasmJs Logger implementation.
 *
 * @return A [WasmJsLogger] instance
 */
public actual fun createDefaultLogger(): Logger = WasmJsLogger()
