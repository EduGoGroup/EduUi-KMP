package com.edugo.kmp.logger

/**
 * iOS/Native implementation of [Logger].
 *
 * Usa println que en Kotlin/Native mapea a la salida estandar.
 * Los logs tambien son visibles en la consola de Xcode.
 */
class IosLogger : Logger {

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
 * Creates the default iOS Logger implementation.
 */
public actual fun createDefaultLogger(): Logger = IosLogger()
