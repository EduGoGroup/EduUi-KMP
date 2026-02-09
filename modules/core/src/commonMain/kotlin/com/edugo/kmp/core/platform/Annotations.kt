package com.edugo.kmp.core.platform

/**
 * Platform-specific volatile annotation.
 *
 * This annotation ensures memory visibility for fields across different platforms:
 * - JVM (Android/Desktop): Maps to @kotlin.jvm.Volatile for proper memory visibility between threads
 * - WasmJS: No-op since WebAssembly/JS is single-threaded and has no memory visibility issues
 *
 * Usage:
 * ```kotlin
 * @PlatformVolatile
 * private var instance: MyClass? = null
 * ```
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FIELD)
public expect annotation class PlatformVolatile()
