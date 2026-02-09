package com.edugo.kmp.di.module

import com.edugo.kmp.auth.interceptor.AuthInterceptor
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.AuthRepositoryImpl
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthServiceImpl
import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.interceptor.TokenProvider
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo auth.
 *
 * Provee AuthRepository, AuthService, y AuthInterceptor.
 * AuthService tambien se registra como TokenProvider para la integracion con network.
 */
public val authModule = module {
    single<CoroutineScope>(named("authScope")) {
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get<EduGoHttpClient>(),
            baseUrl = get<AppConfig>().getFullApiUrl()
        )
    }
    single<AuthService> {
        AuthServiceImpl(
            repository = get(),
            storage = get<SafeEduGoStorage>(),
            scope = get(named("authScope"))
        )
    } bind TokenProvider::class
    single {
        AuthInterceptor(
            tokenProvider = get<AuthService>()
        )
    }
}
