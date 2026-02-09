package com.edugo.kmp.core.platform

/**
 * WasmJS implementation of platformSynchronized.
 *
 * No-op implementation since WebAssembly/JavaScript is single-threaded.
 * Simply executes the block without synchronization overhead.
 */
public actual inline fun <T> platformSynchronized(lock: Any, block: () -> T): T = block()
