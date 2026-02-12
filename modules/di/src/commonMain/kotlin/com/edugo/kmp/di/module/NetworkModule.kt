package com.edugo.kmp.di.module

import com.edugo.kmp.network.EduGoHttpClient
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo network.
 *
 * Provee EduGoHttpClient como singleton y el builder como factory.
 */
public val networkModule = module {
    single { EduGoHttpClient.create() }
    factory { EduGoHttpClient.builder() }
}
