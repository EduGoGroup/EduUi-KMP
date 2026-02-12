package com.edugo.kmp.config

import kotlin.concurrent.Volatile

/**
 * Detects the current environment (DEV, STAGING, PROD) automatically
 * based on platform-specific heuristics.
 *
 * Detection strategies per platform:
 * - Android: Debugger attached or System property `app.environment`
 * - Desktop: Debugger attached or env var `APP_ENVIRONMENT`
 * - iOS: Conservative (DEV default), Phase 2: Info.plist
 * - WasmJS: Conservative (DEV default), Phase 2: hostname detection
 *
 * Supports manual override for testing via [forceEnvironment].
 */
public object EnvironmentDetector {
    @Volatile
    private var manualOverride: Environment? = null

    /**
     * Detects the current environment automatically.
     *
     * If [forceEnvironment] was called, returns the forced value.
     * Otherwise, delegates to platform-specific implementation.
     *
     * Thread-safe: can be called from any coroutine or thread.
     *
     * @return The detected or forced environment
     */
    public fun detect(): Environment {
        return manualOverride ?: detectPlatformEnvironment()
    }

    /**
     * Forces a specific environment, overriding automatic detection.
     * Useful for testing or manual configuration.
     *
     * Thread-safe: can be called from any coroutine or thread.
     *
     * @param environment The environment to force
     */
    public fun forceEnvironment(environment: Environment) {
        manualOverride = environment
    }

    /**
     * Clears the forced environment, restoring automatic detection.
     *
     * Thread-safe: can be called from any coroutine or thread.
     */
    public fun reset() {
        manualOverride = null
    }
}

/**
 * Platform-specific environment detection.
 *
 * Each platform implements its own detection strategy independently,
 * without relying on Platform.isDebug (which is unreliable on some targets).
 */
internal expect fun detectPlatformEnvironment(): Environment
