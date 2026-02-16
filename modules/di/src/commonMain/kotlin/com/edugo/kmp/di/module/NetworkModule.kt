package com.edugo.kmp.di.module

import com.edugo.kmp.auth.interceptor.AuthInterceptor
import com.edugo.kmp.network.EduGoHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo network.
 *
 * Provee dos instancias de EduGoHttpClient:
 * - "plainHttp" (named): sin interceptores, para operaciones de auth (login/refresh)
 * - default (sin qualifier): con AuthInterceptor, para llamadas autenticadas a la API
 */
public val networkModule = module {
    single(named("plainHttp")) { EduGoHttpClient.create() }

    single {
        EduGoHttpClient.builder()
            .interceptor(get<AuthInterceptor>())
            .build()
    }

    factory { EduGoHttpClient.builder() }
}
