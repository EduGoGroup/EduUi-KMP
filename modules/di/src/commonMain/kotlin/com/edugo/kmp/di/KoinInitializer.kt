package com.edugo.kmp.di

import com.edugo.kmp.di.module.authModule
import com.edugo.kmp.di.module.configModule
import com.edugo.kmp.di.module.coreModule
import com.edugo.kmp.di.module.foundationModule
import com.edugo.kmp.di.module.loggerModule
import com.edugo.kmp.di.module.networkModule
import com.edugo.kmp.di.module.settingsModule
import com.edugo.kmp.di.module.storageModule
import com.edugo.kmp.di.module.dynamicUiModule
import com.edugo.kmp.di.module.validationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Inicializador centralizado de Koin para el ecosistema EduGo KMP.
 *
 * Provee funciones para obtener los módulos por grupos y para
 * inicializar Koin con la configuración completa.
 *
 * ## Uso básico
 * ```kotlin
 * // Inicializar con todos los módulos
 * KoinInitializer.initKoin()
 *
 * // Inicializar con configuración adicional
 * KoinInitializer.initKoin {
 *     // Configuración adicional de Koin
 * }
 *
 * // Solo módulos core (sin auth ni network)
 * KoinInitializer.initKoin(modules = KoinInitializer.coreModules())
 * ```
 */
public object KoinInitializer {

    /**
     * Módulos core: foundation + core + logger + validation.
     * Son las dependencias base sin efectos secundarios de I/O.
     */
    public fun coreModules(): List<Module> = listOf(
        foundationModule,
        coreModule,
        loggerModule,
        validationModule
    )

    /**
     * Módulos de infraestructura: storage + config + network.
     * Requieren acceso a plataforma (Settings, filesystem, HTTP engine).
     */
    public fun infrastructureModules(): List<Module> = listOf(
        storageModule,
        configModule,
        networkModule
    )

    /**
     * Módulos de dominio: auth + settings.
     */
    public fun domainModules(): List<Module> = listOf(
        authModule,
        settingsModule,
        dynamicUiModule
    )

    /**
     * Todos los módulos del ecosistema EduGo KMP.
     */
    public fun allModules(): List<Module> =
        coreModules() + infrastructureModules() + domainModules()

    /**
     * Inicializa Koin con todos los módulos.
     *
     * @param appDeclaration Configuración adicional de Koin (opcional).
     * @return KoinApplication configurada.
     */
    public fun initKoin(
        appDeclaration: KoinAppDeclaration = {}
    ): KoinApplication {
        return startKoin {
            appDeclaration()
            modules(allModules())
        }
    }

    /**
     * Inicializa Koin con un subconjunto de módulos.
     *
     * @param modules Lista de módulos a cargar.
     * @param appDeclaration Configuración adicional de Koin (opcional).
     * @return KoinApplication configurada.
     */
    public fun initKoin(
        modules: List<Module>,
        appDeclaration: KoinAppDeclaration = {}
    ): KoinApplication {
        return startKoin {
            appDeclaration()
            modules(modules)
        }
    }
}
