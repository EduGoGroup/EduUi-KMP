# Fase 1: MVP - Sistema Base Funcional

## Objetivo de la Fase

Implementar el núcleo funcional del sistema de autenticación con capacidades básicas de login, logout, almacenamiento de tokens y restauración de sesión. Al finalizar esta fase, tendrás un sistema de autenticación que funciona end-to-end.

**Entregable**: Sistema de autenticación funcional sin renovación automática.

---

## Tiempo Estimado

**6-8 horas** distribuidas así:

- Configuración e interfaces: 1.5 horas
- Repository e implementación: 2 horas
- AuthService y lógica de negocio: 2.5 horas
- Testing: 1-2 horas
- Validación manual: 1 hora

---

## Prerequisitos

- Módulo `auth` creado y configurado en `settings.gradle.kts`
- Dependencias configuradas en `modules/auth/build.gradle.kts`
- Módulos `storage`, `network`, `foundation` funcionando correctamente

---

## Tareas de Implementación

### Tarea 1: Crear Modelo de Storage de Auth

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/storage/AuthStorage.kt`

```kotlin
package com.edugo.kmp.auth.storage

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.storage.EduGoStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Capa de persistencia para datos de autenticación.
 * Abstrae el almacenamiento de tokens y user info.
 */
internal class AuthStorage(
    private val storage: EduGoStorage
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth.token"
        private const val KEY_USER_INFO = "auth.user_info"
        private const val KEY_REFRESH_TOKEN = "auth.refresh_token"
    }

    /**
     * Guarda el token de autenticación.
     */
    fun saveAuthToken(token: AuthToken) {
        val tokenJson = json.encodeToString(token)
        storage.putString(KEY_AUTH_TOKEN, tokenJson)
        
        // Guardamos refresh token por separado para acceso rápido
        token.refreshToken?.let {
            storage.putString(KEY_REFRESH_TOKEN, it)
        }
    }

    /**
     * Obtiene el token de autenticación guardado.
     */
    fun getAuthToken(): AuthToken? {
        val tokenJson = storage.getStringOrNull(KEY_AUTH_TOKEN) ?: return null
        return try {
            json.decodeFromString<AuthToken>(tokenJson)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Guarda la información del usuario.
     */
    fun saveUserInfo(userInfo: AuthUserInfo) {
        val userJson = json.encodeToString(userInfo)
        storage.putString(KEY_USER_INFO, userJson)
    }

    /**
     * Obtiene la información del usuario guardada.
     */
    fun getUserInfo(): AuthUserInfo? {
        val userJson = storage.getStringOrNull(KEY_USER_INFO) ?: return null
        return try {
            json.decodeFromString<AuthUserInfo>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene solo el refresh token.
     */
    fun getRefreshToken(): String? {
        return storage.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    /**
     * Limpia todos los datos de autenticación.
     */
    fun clear() {
        storage.remove(KEY_AUTH_TOKEN)
        storage.remove(KEY_USER_INFO)
        storage.remove(KEY_REFRESH_TOKEN)
    }

    /**
     * Verifica si hay datos de sesión guardados.
     */
    fun hasStoredSession(): Boolean {
        return storage.contains(KEY_AUTH_TOKEN) && storage.contains(KEY_USER_INFO)
    }
}
```

**Validación**:
- Compila sin errores
- Usa `EduGoStorage` existente
- Serializa correctamente con kotlinx.serialization

---

### Tarea 2: Implementar AuthRepository

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/repository/AuthRepository.kt`

El archivo ya existe. Verificar que tenga esta interfaz:

```kotlin
package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.foundation.result.Result

/**
 * Repositorio de autenticación - interfaz.
 */
interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<LoginResponse>
    
    suspend fun refreshToken(refreshToken: String): Result<RefreshResponse>
    
    suspend fun verifyToken(token: String): Result<TokenVerificationResponse>
    
    suspend fun logout(token: String): Result<Unit>
}
```

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/repository/AuthRepositoryImpl.kt`

Actualizar o crear con esta implementación completa:

```kotlin
package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.logger.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Implementación real de AuthRepository usando Ktor HttpClient.
 */
class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : AuthRepository {

    private val logger = Logger.tagged("AuthRepository")

    override suspend fun login(credentials: LoginCredentials): Result<LoginResponse> {
        return try {
            logger.debug("Attempting login for user: ${credentials.username}")
            
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(credentials)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val loginResponse = response.body<LoginResponse>()
                    logger.info("Login successful for user: ${credentials.username}")
                    success(loginResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    logger.warn("Login failed: Invalid credentials")
                    failure("Credenciales inválidas")
                }
                else -> {
                    logger.error("Login failed with status: ${response.status}")
                    failure("Error en el servidor: ${response.status}")
                }
            }
        } catch (e: Exception) {
            logger.error("Login failed with exception: ${e.message}")
            failure("Error de conexión: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<RefreshResponse> {
        return try {
            logger.debug("Attempting token refresh")
            
            val response = httpClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refresh_token" to refreshToken))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val refreshResponse = response.body<RefreshResponse>()
                    logger.info("Token refresh successful")
                    success(refreshResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    logger.warn("Token refresh failed: Invalid refresh token")
                    failure("Sesión expirada")
                }
                else -> {
                    logger.error("Token refresh failed with status: ${response.status}")
                    failure("Error al renovar token: ${response.status}")
                }
            }
        } catch (e: Exception) {
            logger.error("Token refresh failed with exception: ${e.message}")
            failure("Error de conexión: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun verifyToken(token: String): Result<TokenVerificationResponse> {
        return try {
            logger.debug("Verifying token")
            
            val response = httpClient.post("$baseUrl/auth/verify") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val verificationResponse = response.body<TokenVerificationResponse>()
                    logger.debug("Token verification successful")
                    success(verificationResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    logger.warn("Token verification failed: Invalid token")
                    failure("Token inválido")
                }
                else -> {
                    logger.error("Token verification failed with status: ${response.status}")
                    failure("Error al verificar token: ${response.status}")
                }
            }
        } catch (e: Exception) {
            logger.error("Token verification failed with exception: ${e.message}")
            failure("Error de conexión: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun logout(token: String): Result<Unit> {
        return try {
            logger.debug("Attempting logout")
            
            val response = httpClient.post("$baseUrl/auth/logout") {
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    logger.info("Logout successful")
                    success(Unit)
                }
                else -> {
                    logger.warn("Logout failed with status: ${response.status}")
                    // Logout siempre retorna success localmente
                    success(Unit)
                }
            }
        } catch (e: Exception) {
            logger.warn("Logout failed with exception: ${e.message}")
            // Logout siempre retorna success localmente
            success(Unit)
        }
    }
}
```

**Validación**:
- Usa Ktor HttpClient
- Maneja errores HTTP correctamente
- Logs informativos en cada operación

---

### Tarea 3: Implementar AuthService (Core)

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/service/AuthServiceImpl.kt`

Actualizar o crear con esta implementación:

```kotlin
package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.storage.AuthStorage
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.logger.Logger
import kotlinx.coroutines.flow.*

/**
 * Implementación de AuthService.
 * Coordina login, logout, storage y estado de autenticación.
 */
class AuthServiceImpl(
    private val repository: AuthRepository,
    private val storage: AuthStorage,
    override val tokenRefreshManager: TokenRefreshManager
) : AuthService {

    private val logger = Logger.tagged("AuthService")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _onSessionExpired = MutableSharedFlow<Unit>(replay = 0)
    override val onSessionExpired: Flow<Unit> = _onSessionExpired.asSharedFlow()

    private val _onLogout = MutableSharedFlow<LogoutResult>(replay = 0)
    override val onLogout: Flow<LogoutResult> = _onLogout.asSharedFlow()

    override suspend fun login(credentials: LoginCredentials): LoginResult {
        logger.info("Starting login for user: ${credentials.username}")
        
        // Actualizar estado a Loading
        _authState.value = AuthState.Loading

        // Llamar al repository
        val result = repository.login(credentials)

        return when (result) {
            is Result.Success -> {
                val response = result.data
                
                // Guardar en storage
                storage.saveAuthToken(response.token)
                storage.saveUserInfo(response.user)
                
                // Actualizar estado
                _authState.value = AuthState.Authenticated(
                    user = response.user,
                    token = response.token
                )
                
                logger.info("Login successful for user: ${response.user.email}")
                LoginResult.Success(response.user, response.token)
            }
            is Result.Failure -> {
                _authState.value = AuthState.Unauthenticated
                logger.warn("Login failed: ${result.error}")
                LoginResult.Failure(result.error)
            }
            is Result.Loading -> {
                LoginResult.Failure("Unexpected loading state")
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        logger.info("Starting logout")
        
        val currentState = _authState.value
        val token = (currentState as? AuthState.Authenticated)?.token?.token

        // Llamar al servidor si hay token
        if (token != null) {
            repository.logout(token)
        }

        // Limpiar storage
        storage.clear()

        // Actualizar estado
        _authState.value = AuthState.Unauthenticated

        logger.info("Logout completed")
        return success(Unit)
    }

    override suspend fun logoutWithDetails(forceLocal: Boolean): LogoutResult {
        logger.info("Starting detailed logout (forceLocal=$forceLocal)")
        
        val currentState = _authState.value
        val token = (currentState as? AuthState.Authenticated)?.token?.token

        val serverResult = if (!forceLocal && token != null) {
            repository.logout(token)
        } else {
            success(Unit)
        }

        // Limpiar storage
        storage.clear()

        // Actualizar estado
        _authState.value = AuthState.Unauthenticated

        val result = LogoutResult(
            success = true,
            clearedLocal = true,
            notifiedServer = serverResult is Result.Success
        )

        _onLogout.emit(result)
        logger.info("Detailed logout completed: $result")
        
        return result
    }

    override suspend fun refreshAuthToken(): Result<AuthToken> {
        logger.debug("Attempting to refresh token")
        
        val refreshToken = storage.getRefreshToken()
            ?: return failure("No refresh token available")

        val result = repository.refreshToken(refreshToken)

        return when (result) {
            is Result.Success -> {
                val newToken = result.data.token
                
                // Actualizar storage
                storage.saveAuthToken(newToken)
                
                // Actualizar estado si estamos autenticados
                val currentState = _authState.value
                if (currentState is AuthState.Authenticated) {
                    _authState.value = currentState.copy(token = newToken)
                }
                
                logger.info("Token refresh successful")
                success(newToken)
            }
            is Result.Failure -> {
                logger.warn("Token refresh failed: ${result.error}")
                
                // Emitir evento de sesión expirada
                _onSessionExpired.emit(Unit)
                
                // Limpiar sesión
                storage.clear()
                _authState.value = AuthState.Unauthenticated
                
                failure(result.error)
            }
            is Result.Loading -> {
                failure("Unexpected loading state")
            }
        }
    }

    override fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }

    override suspend fun getToken(): String? {
        val state = _authState.value
        return (state as? AuthState.Authenticated)?.token?.token
    }

    override suspend fun isTokenExpired(): Boolean {
        val state = _authState.value
        return (state as? AuthState.Authenticated)?.token?.isExpired() ?: true
    }

    override fun getCurrentUser(): AuthUserInfo? {
        val state = _authState.value
        return (state as? AuthState.Authenticated)?.user
    }

    override fun getCurrentAuthToken(): AuthToken? {
        val state = _authState.value
        return (state as? AuthState.Authenticated)?.token
    }

    override suspend fun restoreSession() {
        logger.info("Attempting to restore session from storage")
        
        if (!storage.hasStoredSession()) {
            logger.debug("No stored session found")
            _authState.value = AuthState.Unauthenticated
            return
        }

        val token = storage.getAuthToken()
        val userInfo = storage.getUserInfo()

        if (token == null || userInfo == null) {
            logger.warn("Incomplete session data, clearing storage")
            storage.clear()
            _authState.value = AuthState.Unauthenticated
            return
        }

        // Verificar si el token está expirado
        if (token.isExpired()) {
            logger.warn("Stored token is expired, attempting refresh")
            
            // Intentar renovar
            val refreshResult = refreshAuthToken()
            
            if (refreshResult is Result.Failure) {
                logger.warn("Token refresh failed during restore, clearing session")
                storage.clear()
                _authState.value = AuthState.Unauthenticated
                return
            }
        }

        // Restaurar estado
        _authState.value = AuthState.Authenticated(
            user = userInfo,
            token = token
        )
        
        logger.info("Session restored successfully for user: ${userInfo.email}")
    }
}
```

**Validación**:
- Coordina repository y storage correctamente
- Emite eventos apropiados
- Maneja estados de forma consistente

---

### Tarea 4: Crear Factory de AuthService

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/service/AuthServiceFactory.kt`

Actualizar o crear:

```kotlin
package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.AuthRepositoryImpl
import com.edugo.kmp.auth.storage.AuthStorage
import com.edugo.kmp.auth.token.TokenRefreshConfig
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.auth.token.TokenRefreshManagerImpl
import com.edugo.kmp.storage.EduGoStorage
import io.ktor.client.*

/**
 * Factory para crear instancias de AuthService con todas sus dependencias.
 */
object AuthServiceFactory {

    /**
     * Crea una instancia de AuthService con configuración por defecto.
     */
    fun create(
        httpClient: HttpClient,
        baseUrl: String,
        storage: EduGoStorage = EduGoStorage.create("auth")
    ): AuthService {
        // Crear componentes
        val authStorage = AuthStorage(storage)
        val repository = AuthRepositoryImpl(httpClient, baseUrl)
        val tokenRefreshManager = TokenRefreshManagerImpl(
            repository = repository,
            config = TokenRefreshConfig.default()
        )

        return AuthServiceImpl(
            repository = repository,
            storage = authStorage,
            tokenRefreshManager = tokenRefreshManager
        )
    }

    /**
     * Crea una instancia de AuthService con configuración personalizada.
     */
    fun create(
        httpClient: HttpClient,
        baseUrl: String,
        storage: EduGoStorage,
        refreshConfig: TokenRefreshConfig
    ): AuthService {
        val authStorage = AuthStorage(storage)
        val repository = AuthRepositoryImpl(httpClient, baseUrl)
        val tokenRefreshManager = TokenRefreshManagerImpl(
            repository = repository,
            config = refreshConfig
        )

        return AuthServiceImpl(
            repository = repository,
            storage = authStorage,
            tokenRefreshManager = tokenRefreshManager
        )
    }
}
```

**Validación**:
- Factory crea todas las dependencias correctamente
- Permite inyectar configuración personalizada

---

### Tarea 5: Implementar TokenRefreshManager (Stub Básico)

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/token/TokenRefreshManagerImpl.kt`

Por ahora, implementación básica (se expandirá en Fase 2):

```kotlin
package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.logger.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Implementación básica de TokenRefreshManager.
 * En Fase 1, solo provee estructura. Renovación automática en Fase 2.
 */
class TokenRefreshManagerImpl(
    private val repository: AuthRepository,
    private val config: TokenRefreshConfig
) : TokenRefreshManager {

    private val logger = Logger.tagged("TokenRefreshManager")

    private val _onRefreshSuccess = MutableSharedFlow<AuthToken>(replay = 0)
    override val onRefreshSuccess: Flow<AuthToken> = _onRefreshSuccess.asSharedFlow()

    private val _onRefreshFailure = MutableSharedFlow<RefreshFailureReason>(replay = 0)
    override val onRefreshFailure: Flow<RefreshFailureReason> = _onRefreshFailure.asSharedFlow()

    override suspend fun refreshToken(refreshToken: String): Result<AuthToken> {
        logger.info("Manual token refresh requested")

        val result = repository.refreshToken(refreshToken)

        return when (result) {
            is Result.Success -> {
                val newToken = result.data.token
                _onRefreshSuccess.emit(newToken)
                logger.info("Token refresh successful")
                Result.Success(newToken)
            }
            is Result.Failure -> {
                _onRefreshFailure.emit(RefreshFailureReason.NetworkError(result.error))
                logger.warn("Token refresh failed: ${result.error}")
                failure(result.error)
            }
            is Result.Loading -> {
                failure("Unexpected loading state")
            }
        }
    }

    override fun startAutomaticRefresh(token: AuthToken) {
        // En Fase 1, solo logging. Implementación completa en Fase 2
        logger.debug("startAutomaticRefresh called (not implemented in Fase 1)")
    }

    override fun stopAutomaticRefresh() {
        // En Fase 1, solo logging
        logger.debug("stopAutomaticRefresh called (not implemented in Fase 1)")
    }

    override fun scheduleRefresh(token: AuthToken) {
        // En Fase 1, solo logging
        logger.debug("scheduleRefresh called (not implemented in Fase 1)")
    }

    override fun cancelScheduledRefresh() {
        // En Fase 1, solo logging
        logger.debug("cancelScheduledRefresh called (not implemented in Fase 1)")
    }
}
```

**Validación**:
- Compila sin errores
- Provee estructura para Fase 2

---

### Tarea 6: Configurar Módulo de DI

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/di/src/commonMain/kotlin/com/edugo/kmp/di/AuthModule.kt`

```kotlin
package com.edugo.kmp.di

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthServiceFactory
import com.edugo.kmp.storage.EduGoStorage
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo de DI para autenticación.
 */
val authModule: Module = module {
    
    // AuthService como singleton
    single<AuthService> {
        val httpClient = get()
        val baseUrl = "https://api.edugo.com" // TODO: Obtener de config
        val storage = EduGoStorage.create("auth")
        
        AuthServiceFactory.create(
            httpClient = httpClient,
            baseUrl = baseUrl,
            storage = storage
        )
    }
}
```

**Validación**:
- AuthService se inyecta correctamente
- Usa dependencias existentes (HttpClient, Storage)

---

## Tests de Fase 1

### Test 1: AuthStorage Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/storage/AuthStorageTest.kt`

```kotlin
package com.edugo.kmp.auth.storage

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.storage.EduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class AuthStorageTest {

    private lateinit var storage: EduGoStorage
    private lateinit var authStorage: AuthStorage

    @BeforeTest
    fun setup() {
        storage = EduGoStorage.withSettings(MapSettings())
        authStorage = AuthStorage(storage)
    }

    @AfterTest
    fun teardown() {
        storage.clear()
    }

    @Test
    fun `saveAuthToken and getAuthToken works correctly`() {
        val token = AuthToken(
            token = "test_access_token",
            expiresAt = Clock.System.now() + 1.hours,
            refreshToken = "test_refresh_token"
        )

        authStorage.saveAuthToken(token)
        val retrieved = authStorage.getAuthToken()

        assertNotNull(retrieved)
        assertEquals(token.token, retrieved.token)
        assertEquals(token.refreshToken, retrieved.refreshToken)
    }

    @Test
    fun `saveUserInfo and getUserInfo works correctly`() {
        val userInfo = AuthUserInfo(
            id = "user123",
            email = "test@edugo.com",
            username = "testuser",
            role = "student"
        )

        authStorage.saveUserInfo(userInfo)
        val retrieved = authStorage.getUserInfo()

        assertNotNull(retrieved)
        assertEquals(userInfo.id, retrieved.id)
        assertEquals(userInfo.email, retrieved.email)
        assertEquals(userInfo.username, retrieved.username)
    }

    @Test
    fun `clear removes all auth data`() {
        val token = AuthToken.createTestToken()
        val userInfo = AuthUserInfo(
            id = "user123",
            email = "test@edugo.com",
            username = "testuser",
            role = "student"
        )

        authStorage.saveAuthToken(token)
        authStorage.saveUserInfo(userInfo)

        assertTrue(authStorage.hasStoredSession())

        authStorage.clear()

        assertFalse(authStorage.hasStoredSession())
        assertNull(authStorage.getAuthToken())
        assertNull(authStorage.getUserInfo())
        assertNull(authStorage.getRefreshToken())
    }

    @Test
    fun `hasStoredSession returns false when no data`() {
        assertFalse(authStorage.hasStoredSession())
    }

    @Test
    fun `hasStoredSession returns true when data exists`() {
        val token = AuthToken.createTestToken()
        val userInfo = AuthUserInfo(
            id = "user123",
            email = "test@edugo.com",
            username = "testuser",
            role = "student"
        )

        authStorage.saveAuthToken(token)
        authStorage.saveUserInfo(userInfo)

        assertTrue(authStorage.hasStoredSession())
    }

    @Test
    fun `getRefreshToken returns correct value`() {
        val token = AuthToken(
            token = "access",
            expiresAt = Clock.System.now() + 1.hours,
            refreshToken = "refresh_token_123"
        )

        authStorage.saveAuthToken(token)

        assertEquals("refresh_token_123", authStorage.getRefreshToken())
    }
}
```

---

### Test 2: AuthService Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/service/AuthServiceImplTest.kt`

```kotlin
package com.edugo.kmp.auth.service

import app.cash.turbine.test
import com.edugo.kmp.auth.model.*
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.RefreshResponse
import com.edugo.kmp.auth.storage.AuthStorage
import com.edugo.kmp.auth.token.TokenRefreshManagerImpl
import com.edugo.kmp.auth.token.TokenRefreshConfig
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.EduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class AuthServiceImplTest {

    private lateinit var mockRepository: MockAuthRepository
    private lateinit var storage: EduGoStorage
    private lateinit var authStorage: AuthStorage
    private lateinit var authService: AuthServiceImpl

    @BeforeTest
    fun setup() {
        mockRepository = MockAuthRepository()
        storage = EduGoStorage.withSettings(MapSettings())
        authStorage = AuthStorage(storage)
        val tokenRefreshManager = TokenRefreshManagerImpl(
            repository = mockRepository,
            config = TokenRefreshConfig.default()
        )
        
        authService = AuthServiceImpl(
            repository = mockRepository,
            storage = authStorage,
            tokenRefreshManager = tokenRefreshManager
        )
    }

    @AfterTest
    fun teardown() {
        storage.clear()
    }

    @Test
    fun `login success updates state and saves to storage`() = runTest {
        val credentials = LoginCredentials("test@edugo.com", "password123")
        val expectedUser = AuthUserInfo("user123", "test@edugo.com", "testuser", "student")
        val expectedToken = AuthToken.createTestToken()

        mockRepository.loginResult = Result.Success(
            LoginResponse(expectedUser, expectedToken)
        )

        val result = authService.login(credentials)

        assertTrue(result is LoginResult.Success)
        assertEquals(expectedUser.email, result.user.email)

        // Verificar estado
        authService.authState.test {
            val state = awaitItem()
            assertTrue(state is AuthState.Authenticated)
            assertEquals(expectedUser.email, state.user.email)
        }

        // Verificar storage
        assertNotNull(authStorage.getAuthToken())
        assertNotNull(authStorage.getUserInfo())
    }

    @Test
    fun `login failure keeps state unauthenticated`() = runTest {
        val credentials = LoginCredentials("test@edugo.com", "wrongpassword")
        mockRepository.loginResult = Result.Failure("Invalid credentials")

        val result = authService.login(credentials)

        assertTrue(result is LoginResult.Failure)

        authService.authState.test {
            val state = awaitItem()
            assertTrue(state is AuthState.Unauthenticated)
        }
    }

    @Test
    fun `logout clears state and storage`() = runTest {
        // Primero hacer login
        val credentials = LoginCredentials("test@edugo.com", "password123")
        mockRepository.loginResult = Result.Success(
            LoginResponse(
                AuthUserInfo("user123", "test@edugo.com", "testuser", "student"),
                AuthToken.createTestToken()
            )
        )
        authService.login(credentials)

        // Ahora logout
        mockRepository.logoutResult = Result.Success(Unit)
        authService.logout()

        authService.authState.test {
            val state = awaitItem()
            assertTrue(state is AuthState.Unauthenticated)
        }

        assertNull(authStorage.getAuthToken())
        assertNull(authStorage.getUserInfo())
    }

    @Test
    fun `restoreSession restores authenticated state`() = runTest {
        val token = AuthToken.createTestToken(durationSeconds = 3600)
        val userInfo = AuthUserInfo("user123", "test@edugo.com", "testuser", "student")

        authStorage.saveAuthToken(token)
        authStorage.saveUserInfo(userInfo)

        authService.restoreSession()

        authService.authState.test {
            val state = awaitItem()
            assertTrue(state is AuthState.Authenticated)
            assertEquals(userInfo.email, state.user.email)
        }
    }

    @Test
    fun `restoreSession with expired token attempts refresh`() = runTest {
        val expiredToken = AuthToken(
            token = "expired_token",
            expiresAt = Clock.System.now() - 1.hours,
            refreshToken = "refresh_token"
        )
        val userInfo = AuthUserInfo("user123", "test@edugo.com", "testuser", "student")

        authStorage.saveAuthToken(expiredToken)
        authStorage.saveUserInfo(userInfo)

        // Mock refresh exitoso
        mockRepository.refreshResult = Result.Success(
            RefreshResponse(AuthToken.createTestToken())
        )

        authService.restoreSession()

        authService.authState.test {
            val state = awaitItem()
            assertTrue(state is AuthState.Authenticated)
        }
    }

    @Test
    fun `isAuthenticated returns correct value`() = runTest {
        assertFalse(authService.isAuthenticated())

        val credentials = LoginCredentials("test@edugo.com", "password123")
        mockRepository.loginResult = Result.Success(
            LoginResponse(
                AuthUserInfo("user123", "test@edugo.com", "testuser", "student"),
                AuthToken.createTestToken()
            )
        )
        authService.login(credentials)

        assertTrue(authService.isAuthenticated())
    }
}

// Mock Repository
private class MockAuthRepository : AuthRepository {
    var loginResult: Result<LoginResponse> = Result.Failure("Not configured")
    var refreshResult: Result<RefreshResponse> = Result.Failure("Not configured")
    var logoutResult: Result<Unit> = Result.Success(Unit)

    override suspend fun login(credentials: LoginCredentials) = loginResult
    override suspend fun refreshToken(refreshToken: String) = refreshResult
    override suspend fun verifyToken(token: String) = Result.Success(
        com.edugo.kmp.auth.repository.TokenVerificationResponse(true)
    )
    override suspend fun logout(token: String) = logoutResult
}
```

---

## Criterios de Aceptación de Fase 1

### Funcionalidad
- [ ] Login exitoso guarda tokens en storage
- [ ] Login exitoso actualiza AuthState a Authenticated
- [ ] Login fallido mantiene AuthState como Unauthenticated
- [ ] Logout limpia storage completamente
- [ ] Logout actualiza AuthState a Unauthenticated
- [ ] restoreSession recupera sesión guardada correctamente
- [ ] restoreSession con token expirado intenta renovar
- [ ] AuthStorage serializa y deserializa correctamente

### Testing
- [ ] AuthStorageTest pasa todos los tests
- [ ] AuthServiceImplTest pasa todos los tests
- [ ] Cobertura de tests > 70%

### Compilación
- [ ] `./gradlew :modules:auth:build` exitoso
- [ ] Cero warnings de compilación
- [ ] Cero errores de lint

---

## Checklist de Validación

```bash
# 1. Compilar
./gradlew :modules:auth:build

# 2. Ejecutar tests
./gradlew :modules:auth:test

# 3. Verificar cobertura (opcional)
./gradlew :modules:auth:testDebugUnitTestCoverage

# 4. Lint
./gradlew :modules:auth:lintKotlin
```

**Todos deben pasar** antes de continuar a Fase 2.

---

## Cómo Probar Manualmente

### Setup Inicial

```kotlin
// En tu app main
val httpClient = HttpClientFactory.create()
val authService = AuthServiceFactory.create(
    httpClient = httpClient,
    baseUrl = "https://api.edugo.com"
)
```

### Test 1: Login

```kotlin
lifecycleScope.launch {
    val result = authService.login(
        LoginCredentials("demo@edugo.com", "password123")
    )
    
    when (result) {
        is LoginResult.Success -> {
            println("Login exitoso: ${result.user.email}")
        }
        is LoginResult.Failure -> {
            println("Login fallido: ${result.error}")
        }
    }
}
```

### Test 2: Observar Estado

```kotlin
lifecycleScope.launch {
    authService.authState.collect { state ->
        when (state) {
            is AuthState.Authenticated -> {
                println("Usuario autenticado: ${state.user.email}")
            }
            is AuthState.Unauthenticated -> {
                println("Usuario no autenticado")
            }
            is AuthState.Loading -> {
                println("Cargando...")
            }
        }
    }
}
```

### Test 3: Logout

```kotlin
lifecycleScope.launch {
    authService.logout()
    println("Sesión cerrada")
}
```

### Test 4: Restaurar Sesión

```kotlin
// Al iniciar la app
lifecycleScope.launch {
    authService.restoreSession()
    println("Sesión restaurada (si existía)")
}
```

---

## Problemas Comunes

### Error: "No refresh token available"
**Causa**: Token guardado no tiene refresh token  
**Solución**: Verificar que API retorna refresh token en login

### Error: Serialization exception
**Causa**: Modelo no coincide con JSON  
**Solución**: Verificar anotaciones `@SerialName` en modelos

### Test falla: "Storage is empty"
**Causa**: Storage no se está guardando correctamente  
**Solución**: Verificar que `saveAuthToken` y `saveUserInfo` se llaman después de login

---

## Siguiente Fase

Una vez completada Fase 1, continuar con:
**[FASE-2-REACTIVO.md](FASE-2-REACTIVO.md)**

---

**Fase**: 1 - MVP  
**Estado**: Listo para implementar  
**Prerequisito para**: Fase 2
