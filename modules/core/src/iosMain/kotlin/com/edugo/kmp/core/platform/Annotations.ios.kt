package com.edugo.kmp.core.platform

/**
 * iOS/Native implementation of PlatformVolatile.
 *
 * Maps to kotlin.concurrent.Volatile para visibilidad de memoria
 * entre hilos en Kotlin/Native.
 */
public actual typealias PlatformVolatile = kotlin.concurrent.Volatile
