package com.edugo.kmp.core.platform

/**
 * WasmJS implementation of Platform.
 *
 * Provides platform information for WebAssembly JavaScript environments.
 * Uses hardcoded values since WASM does not support dynamic JS interop.
 */
public actual object Platform {
    actual val name: String = "WasmJS"

    actual val osVersion: String = "1.0"

    actual val isDebug: Boolean = false
}
