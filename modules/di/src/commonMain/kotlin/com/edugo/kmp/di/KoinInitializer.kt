package com.edugo.kmp.di

import com.edugo.kmp.di.module.authModule
import com.edugo.kmp.di.module.configModule
import com.edugo.kmp.di.module.coreModule
import com.edugo.kmp.di.module.foundationModule
import com.edugo.kmp.di.module.loggerModule
import com.edugo.kmp.di.module.networkModule
import com.edugo.kmp.di.module.storageModule
import com.edugo.kmp.di.module.validationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Inicializador centralizado de Koin para el ecosistema EduGo KMP.
 *
 * Provee funciones para obtener los modulos por grupos y para
 * inicializar Koin con la configuracion completa.
 *
 * ## Uso basico
 * ```kotlin
 * // Inicializar con todos los modulos
 * KoinInitializer.initKoin()
 *
 * // Inicializar con configuracion adicional
 * KoinInitializer.initKoin {
 *     // Configuracion adicional de Koin
 * }
 *
 * // Solo modulos core (sin auth ni network)
 * KoinInitializer.initKoin(modules = KoinInitializer.coreModules())
 * ```
 */
public object KoinInitializer {

    /**
     * Modulos core: foundation + core + logger + validation.
     * Son las dependencias base sin efectos secundarios de I/O.
     */
    public fun coreModules(): List<Module> = listOf(
        foundationModule,
        coreModule,
        loggerModule,
        validationModule
    )

    /**
     * Modulos de infraestructura: storage + config + network.
     * Requieren acceso a plataforma (Settings, filesystem, HTTP engine).
     */
    public fun infrastructureModules(): List<Module> = listOf(
        storageModule,
        configModule,
        networkModule
    )

    /**
     * Modulos de dominio: auth.
     */
    public fun domainModules(): List<Module> = listOf(
        authModule
    )

    /**
     * Todos los modulos del ecosistema EduGo KMP.
     */
    public fun allModules(): List<Module> =
        coreModules() + infrastructureModules() + domainModules()

    /**
     * Inicializa Koin con todos los modulos.
     *
     * @param appDeclaration Configuracion adicional de Koin (opcional).
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
     * Inicializa Koin con un subconjunto de modulos.
     *
     * @param modules Lista de modulos a cargar.
     * @param appDeclaration Configuracion adicional de Koin (opcional).
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
