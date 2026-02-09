package com.edugo.kmp.di.module

import com.edugo.kmp.foundation.serialization.JsonConfig
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo foundation.
 *
 * Provee las configuraciones Json predefinidas como singletons.
 */
public val foundationModule = module {
    single<Json> { JsonConfig.Default }
    single<Json>(named("pretty")) { JsonConfig.Pretty }
    single<Json>(named("strict")) { JsonConfig.Strict }
    single<Json>(named("lenient")) { JsonConfig.Lenient }
}
