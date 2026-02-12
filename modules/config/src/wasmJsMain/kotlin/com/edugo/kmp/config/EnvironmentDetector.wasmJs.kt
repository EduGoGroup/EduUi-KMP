package com.edugo.kmp.config

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { return window.location.hostname; } catch(e) { return ''; } }")
private external fun getHostname(): String

internal actual fun detectPlatformEnvironment(): Environment {
    val hostname = getHostname()

    if (hostname.isNotEmpty()) {
        return when {
            hostname == "localhost" || hostname == "127.0.0.1" -> Environment.DEV
            hostname.contains("staging", ignoreCase = true) -> Environment.STAGING
            else -> Environment.PROD
        }
    }

    // Non-browser context (e.g., Node.js test runner)
    return Environment.DEV
}
