package com.edugo.kmp.config

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check system property (set via Gradle -Dapp.environment=STAGING)
    val sysProp = System.getProperty("app.environment")
    if (sysProp != null) {
        return Environment.fromString(sysProp) ?: Environment.PROD
    }

    // Strategy 2: Check environment variable (set via export APP_ENVIRONMENT=PROD)
    val envVar = System.getenv("APP_ENVIRONMENT")
    if (envVar != null) {
        return Environment.fromString(envVar) ?: Environment.PROD
    }

    // Strategy 3: Check if debugger is attached (JVM JDWP agent)
    val isDebuggerAttached = java.lang.management.ManagementFactory
        .getRuntimeMXBean()
        .inputArguments
        .any { it.contains("-agentlib:jdwp") }

    if (isDebuggerAttached) {
        return Environment.DEV
    }

    // Strategy 4: Default to DEV for local development
    // TODO: Change to Environment.PROD for production builds
    return Environment.DEV
}
