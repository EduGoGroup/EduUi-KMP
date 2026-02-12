package com.edugo.kmp.config

/**
 * Android implementation: loads from assets, falls back to DefaultConfigs.
 */
internal actual fun loadResourceAsString(path: String): String? {
    val context = AndroidContextHolder.get()
    if (context != null) {
        try {
            return context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            // File not found in assets, fall through to fallback
        }
    }

    return DefaultConfigs.get(path)
}
