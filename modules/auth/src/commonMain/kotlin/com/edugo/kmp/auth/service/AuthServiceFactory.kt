package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.StubAuthRepository
import com.edugo.kmp.auth.token.TokenRefreshConfig
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

/**
 * Factory para crear instancias de [AuthService].
 *
 * Los métodos de testing aceptan un [EduGoStorage] opcional para evitar
 * la llamada a Settings() de plataforma que falla en Android unit tests
 * (donde no hay Context disponible).
 * Pasar [EduGoStorage.withSettings(MapSettings())] desde commonTest.
 */
public object AuthServiceFactory {

    private fun createSafeStorage(storage: EduGoStorage? = null): SafeEduGoStorage {
        val effectiveStorage = storage ?: run {
            val uniqueName = "test_auth_storage_${Clock.System.now().toEpochMilliseconds()}_${(0..999999).random()}"
            EduGoStorage.create(uniqueName)
        }
        return SafeEduGoStorage.wrap(effectiveStorage)
    }

    public fun createForTesting(
        validEmail: String = "test@edugo.com",
        validPassword: String = "password123",
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        refreshConfig: TokenRefreshConfig = TokenRefreshConfig.DEFAULT,
        storage: EduGoStorage? = null
    ): AuthService {
        val stubRepository = StubAuthRepository().apply {
            this.validEmail = validEmail
            this.validPassword = validPassword
        }

        val safeStorage = createSafeStorage(storage)

        return AuthServiceImpl(
            repository = stubRepository,
            storage = safeStorage,
            scope = scope,
            refreshConfig = refreshConfig
        )
    }

    public fun createWithCustomComponents(
        repository: AuthRepository,
        storage: SafeEduGoStorage,
        json: Json = Json { ignoreUnknownKeys = true },
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        refreshConfig: TokenRefreshConfig = TokenRefreshConfig.DEFAULT
    ): AuthService {
        return AuthServiceImpl(repository, storage, json, scope, refreshConfig)
    }

    public fun createForTestingWithNetworkError(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        storage: EduGoStorage? = null
    ): AuthService {
        val stubRepository = StubAuthRepository.createWithNetworkError()
        val safeStorage = createSafeStorage(storage)

        return AuthServiceImpl(
            repository = stubRepository,
            storage = safeStorage,
            scope = scope
        )
    }

    public fun createForTestingWithDelay(
        delayMillis: Long,
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        storage: EduGoStorage? = null
    ): AuthService {
        val stubRepository = StubAuthRepository.createWithDelay(delayMillis)
        val safeStorage = createSafeStorage(storage)

        return AuthServiceImpl(
            repository = stubRepository,
            storage = safeStorage,
            scope = scope
        )
    }
}
