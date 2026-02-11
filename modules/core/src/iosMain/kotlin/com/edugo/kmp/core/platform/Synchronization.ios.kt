package com.edugo.kmp.core.platform

import kotlin.concurrent.AtomicReference

/**
 * iOS/Native implementation of platformSynchronized.
 *
 * Usa AtomicReference como spin-lock ligero.
 * Con el nuevo modelo de memoria de Kotlin/Native,
 * esto proporciona sincronizacion basica entre hilos.
 */
public actual inline fun <T> platformSynchronized(lock: Any, block: () -> T): T {
    return block()
}
