package com.edugo.kmp.di.module

import com.edugo.kmp.auth.circuit.CircuitBreaker
import com.edugo.kmp.auth.config.AuthConfig
import com.edugo.kmp.auth.interceptor.AuthInterceptor
import com.edugo.kmp.auth.logging.AuthLogger
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.AuthRepositoryImpl
import com.edugo.kmp.auth.repository.MockAuthRepository
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthServiceImpl
import com.edugo.kmp.auth.throttle.RateLimiter
import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.logger.Logger
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
 * AuthService también se registra como TokenProvider para la integracion con network.
 */
public val authModule = module {
    single<CoroutineScope>(named("authScope")) {
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
    single {
        AuthConfig.forEnvironment(get<AppConfig>().environment)
    }
    single {
        val authConfig = get<AuthConfig>()
        CircuitBreaker(authConfig.circuitBreakerConfig)
    }
    single(named("loginRateLimiter")) {
        val authConfig = get<AuthConfig>()
        RateLimiter(
            maxRequests = authConfig.rateLimitMaxRequests,
            timeWindow = authConfig.rateLimitWindow
        )
    }
    single { AuthLogger(get<Logger>()) }
    single<AuthRepository> {
        val appConfig = get<AppConfig>()
        if (appConfig.mockMode) {
            MockAuthRepository()
        } else {
            val authConfig = get<AuthConfig>()
            AuthRepositoryImpl(
                httpClient = get<EduGoHttpClient>(named("plainHttp")),
                baseUrl = appConfig.adminApiBaseUrl,
                circuitBreaker = get(),
                retryPolicy = authConfig.retryPolicy
            )
        }
    }
    single<AuthService> {
        val authConfig = get<AuthConfig>()
        AuthServiceImpl(
            repository = get(),
            storage = get<SafeEduGoStorage>(),
            scope = get(named("authScope")),
            refreshConfig = authConfig.refreshConfig,
            loginRateLimiter = get(named("loginRateLimiter")),
            authLogger = get()
        )
    } bind TokenProvider::class
    single {
        AuthInterceptor(
            tokenProvider = get<AuthService>()
        )
    }
}
