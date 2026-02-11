package com.edugo.kmp.core.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * iOS implementation of AppDispatchers.
 *
 * - Main: dispatch queue principal (UI-safe)
 * - IO: usa Default (Kotlin/Native maneja I/O eficientemente)
 * - Default: pool de hilos background
 */
public actual object AppDispatchers {
    actual val Main: CoroutineDispatcher = Dispatchers.Main

    actual val IO: CoroutineDispatcher = Dispatchers.Default

    actual val Default: CoroutineDispatcher = Dispatchers.Default
}
