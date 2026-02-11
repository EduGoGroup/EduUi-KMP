package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLoggerImpl

/**
 * Adaptador que implementa [Logger] delegando a Kermit como backend.
 *
 * Cada llamada a los metodos de [Logger] se traduce a la API de Kermit,
 * aprovechando las implementaciones nativas por plataforma:
 * - Android: LogcatWriter (android.util.Log)
 * - JVM/Desktop: platformLogWriter (colores ANSI)
 * - iOS: NSLogWriter (NSLog)
 * - WasmJs: CommonWriter (console.log)
 *
 * Las variantes con lambda proporcionan evaluacion lazy real a traves de Kermit.
 *
 * @param kermit Instancia de Kermit Logger a la que se delegan las llamadas
 */
public class KermitDelegateLogger(
    private val kermit: KermitLoggerImpl,
) : Logger {

    override fun d(tag: String, message: String) {
        kermit.d(tag = tag) { message }
    }

    override fun d(tag: String, message: () -> String) {
        kermit.d(tag = tag, message = message)
    }

    override fun d(tag: String, message: String, throwable: Throwable) {
        kermit.d(throwable = throwable, tag = tag) { message }
    }

    override fun i(tag: String, message: String) {
        kermit.i(tag = tag) { message }
    }

    override fun i(tag: String, message: () -> String) {
        kermit.i(tag = tag, message = message)
    }

    override fun i(tag: String, message: String, throwable: Throwable) {
        kermit.i(throwable = throwable, tag = tag) { message }
    }

    override fun w(tag: String, message: String) {
        kermit.w(tag = tag) { message }
    }

    override fun w(tag: String, message: () -> String) {
        kermit.w(tag = tag, message = message)
    }

    override fun w(tag: String, message: String, throwable: Throwable) {
        kermit.w(throwable = throwable, tag = tag) { message }
    }

    override fun e(tag: String, message: String) {
        kermit.e(tag = tag) { message }
    }

    override fun e(tag: String, message: () -> String) {
        kermit.e(tag = tag, message = message)
    }

    override fun e(tag: String, message: String, throwable: Throwable) {
        kermit.e(throwable = throwable, tag = tag) { message }
    }
}
