package com.edugo.kmp.config

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { return window.location.hostname; } catch(e) { return ''; } }")
private external fun getHostname(): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { return new URLSearchParams(window.location.search).get('env') || ''; } catch(e) { return ''; } }")
private external fun getEnvParam(): String

internal actual fun detectPlatformEnvironment(): Environment {
    val hostname = getHostname()
    val isLocalhost = hostname == "localhost" || hostname == "127.0.0.1"

    // Query param override only allowed on localhost (security: prevent env spoofing in prod)
    if (isLocalhost) {
        val envParam = getEnvParam()
        if (envParam.isNotEmpty()) {
            return when (envParam.uppercase()) {
                "STAGING" -> Environment.STAGING
                "PROD" -> Environment.PROD
                else -> Environment.DEV
            }
        }
        return Environment.DEV
    }

    if (hostname.isNotEmpty()) {
        return when {
            hostname.contains("staging", ignoreCase = true) -> Environment.STAGING
            else -> Environment.PROD
        }
    }

    // Non-browser context (e.g., Node.js test runner)
    return Environment.DEV
}
