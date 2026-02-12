package com.edugo.kmp.di.module

import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.config.ConfigLoader
import com.edugo.kmp.config.Environment
import com.edugo.kmp.config.EnvironmentDetector
import org.koin.dsl.module

/**
 * Módulo Koin para dependencias del módulo config.
 *
 * El ambiente se detecta automáticamente según la plataforma:
 * - Android: System property `app.environment` o debugger attached
 * - Desktop: System property o env var `APP_ENVIRONMENT` o debugger attached
 * - iOS: Info.plist `AppEnvironment` key, default DEV (conservador)
 * - WasmJS: hostname detection (localhost=DEV, staging=STAGING, otro=PROD)
 *
 * Para override manual en tests: EnvironmentDetector.forceEnvironment(Environment.STAGING)
 */
public val configModule = module {
    single<Environment> { EnvironmentDetector.detect() }
    single<AppConfig> { ConfigLoader.load(get()) }
}
