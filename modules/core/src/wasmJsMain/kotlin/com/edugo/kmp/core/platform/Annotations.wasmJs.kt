package com.edugo.kmp.core.platform

/**
 * WasmJS implementation of PlatformVolatile.
 *
 * Simple annotation class since WebAssembly/JavaScript is single-threaded
 * and has no memory visibility issues between threads.
 */
@Target(AnnotationTarget.FIELD)
public actual annotation class PlatformVolatile()
