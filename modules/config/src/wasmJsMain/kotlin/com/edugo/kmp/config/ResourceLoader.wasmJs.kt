package com.edugo.kmp.config

/**
 * WasmJS implementation: uses DefaultConfigs (embedded).
 *
 * In WasmJS, classpath resources are not available at runtime.
 * Config is loaded from the centralized DefaultConfigs fallback.
 */
internal actual fun loadResourceAsString(path: String): String? {
    return DefaultConfigs.get(path)
}
