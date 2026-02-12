package com.edugo.kmp.config

import android.os.Debug

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check system property (set via gradle or CI)
    val envProperty = System.getProperty("app.environment")
    if (envProperty != null) {
        return Environment.fromString(envProperty) ?: Environment.PROD
    }

    // Strategy 2: Check if debugger is attached (reliable at runtime)
    // Wrapped in try-catch because android.os.Debug is unavailable in local unit tests
    try {
        if (Debug.isDebuggerConnected()) {
            return Environment.DEV
        }
    } catch (_: Throwable) {
        // In unit test environment, Android framework classes throw
        return Environment.DEV
    }

    // Strategy 3: Default to PROD for release builds
    return Environment.PROD
}
