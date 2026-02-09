package com.edugo.kmp.di.module

import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.config.ConfigLoader
import com.edugo.kmp.config.Environment
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo config.
 *
 * Por defecto carga configuracion para el ambiente DEV.
 * La app puede sobrescribir el Environment antes de inicializar Koin.
 */
public val configModule = module {
    single<Environment> { Environment.DEV }
    single<AppConfig> { ConfigLoader.load(get()) }
}
