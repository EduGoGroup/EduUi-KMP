# Fase 2: Sistema Reactivo

## Objetivo de la Fase

Agregar reactividad completa al sistema de autenticación, incluyendo renovación automática de tokens, interceptor HTTP para inyección de tokens, y eventos de sesión. Al finalizar esta fase, tendrás un sistema totalmente reactivo que maneja tokens de forma transparente.

**Entregable**: Sistema reactivo con renovación automática, interceptor HTTP y eventos.

---

## Tiempo Estimado

**4-6 horas** distribuidas así:

- Renovación automática de tokens: 2 horas
- Interceptor HTTP: 1 hora
- Eventos y observadores: 1 hora
- Testing: 1-2 horas

---

## Prerequisitos

**OBLIGATORIO**: Fase 1 completada y funcionando correctamente.

Verificar:
- [ ] Login funciona y guarda tokens
- [ ] Logout limpia datos correctamente
- [ ] restoreSession funciona
- [ ] Todos los tests de Fase 1 pasan

---

## Tareas de Implementación

### Tarea 1: Implementar Renovación Automática de Tokens

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/token/TokenRefreshManagerImpl.kt`

Actualizar implementación completa:

```kotlin
package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Implementación completa de TokenRefreshManager con renovación automática.
 */
class TokenRefreshManagerImpl(
    private val repository: AuthRepository,
    private val config: TokenRefreshConfig,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : TokenRefreshManager {

    private val logger = Logger.tagged("TokenRefreshManager")

    private var refreshJob: Job? = null
    private var currentToken: AuthToken? = null

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
                val reason = RefreshFailureReason.NetworkError(result.error)
                _onRefreshFailure.emit(reason)
                logger.warn("Token refresh failed: ${result.error}")
                failure(result.error)
            }
            is Result.Loading -> {
                failure("Unexpected loading state")
            }
        }
    }

    override fun startAutomaticRefresh(token: AuthToken) {
        logger.info("Starting automatic token refresh")
        
        currentToken = token
        stopAutomaticRefresh() // Cancelar job anterior si existe
        
        scheduleRefresh(token)
    }

    override fun stopAutomaticRefresh() {
        logger.debug("Stopping automatic token refresh")
        
        refreshJob?.cancel()
        refreshJob = null
        currentToken = null
    }

    override fun scheduleRefresh(token: AuthToken) {
        if (!token.hasRefreshToken()) {
            logger.warn("Cannot schedule refresh: no refresh token available")
            return
        }

        val timeUntilExpiration = token.timeUntilExpiration()
        
        if (timeUntilExpiration.isNegative()) {
            logger.warn("Token already expired, refreshing immediately")
            refreshJob = scope.launch {
                performRefresh(token)
            }
            return
        }

        // Calcular cuándo renovar (threshold antes de expiración)
        val refreshDelay = calculateRefreshDelay(timeUntilExpiration)
        
        logger.debug("Scheduling token refresh in ${refreshDelay.inWholeSeconds} seconds")

        refreshJob = scope.launch {
            delay(refreshDelay)
            performRefresh(token)
        }
    }

    override fun cancelScheduledRefresh() {
        logger.debug("Cancelling scheduled refresh")
        refreshJob?.cancel()
        refreshJob = null
    }

    private suspend fun performRefresh(token: AuthToken) {
        val refreshToken = token.refreshToken
        if (refreshToken == null) {
            logger.error("Refresh token is null, cannot perform refresh")
            _onRefreshFailure.emit(RefreshFailureReason.NoRefreshToken)
            return
        }

        logger.info("Performing automatic token refresh")

        val result = repository.refreshToken(refreshToken)

        when (result) {
            is Result.Success -> {
                val newToken = result.data.token
                currentToken = newToken
                _onRefreshSuccess.emit(newToken)
                logger.info("Automatic token refresh successful")
                
                // Programar siguiente renovación
                scheduleRefresh(newToken)
            }
            is Result.Failure -> {
                val reason = RefreshFailureReason.NetworkError(result.error)
                _onRefreshFailure.emit(reason)
                logger.error("Automatic token refresh failed: ${result.error}")
            }
            is Result.Loading -> {
                logger.warn("Unexpected loading state during refresh")
            }
        }
    }

    private fun calculateRefreshDelay(timeUntilExpiration: Duration): Duration {
        // Renovar cuando quede X% del tiempo total
        val thresholdDuration = timeUntilExpiration * config.refreshThresholdPercent / 100.0
        
        // Asegurar que no sea menor al mínimo
        val delay = maxOf(thresholdDuration, config.minRefreshInterval.milliseconds)
        
        return delay
    }
}
```

**Validación**:
- Calcula correctamente cuándo renovar
- Programa renovación automática
- Emite eventos correctamente

---

### Tarea 2: Integrar Renovación Automática en AuthService

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/service/AuthServiceImpl.kt`

Actualizar método `login` para iniciar renovación automática:

```kotlin
// En AuthServiceImpl, actualizar el método login:

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
            
            // NUEVO: Iniciar renovación automática
            tokenRefreshManager.startAutomaticRefresh(response.token)
            
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
```

Actualizar método `logout` para detener renovación:

```kotlin
override suspend fun logout(): Result<Unit> {
    logger.info("Starting logout")
    
    val currentState = _authState.value
    val token = (currentState as? AuthState.Authenticated)?.token?.token

    // NUEVO: Detener renovación automática
    tokenRefreshManager.stopAutomaticRefresh()

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
```

Actualizar método `restoreSession` para reiniciar renovación:

```kotlin
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
        
        // Si refresh fue exitoso, obtener nuevo token
        val newToken = (refreshResult as Result.Success).data
        
        // Restaurar estado con nuevo token
        _authState.value = AuthState.Authenticated(
            user = userInfo,
            token = newToken
        )
        
        // NUEVO: Iniciar renovación automática con nuevo token
        tokenRefreshManager.startAutomaticRefresh(newToken)
    } else {
        // Token válido, restaurar estado
        _authState.value = AuthState.Authenticated(
            user = userInfo,
            token = token
        )
        
        // NUEVO: Iniciar renovación automática
        tokenRefreshManager.startAutomaticRefresh(token)
    }
    
    logger.info("Session restored successfully for user: ${userInfo.email}")
}
```

Agregar observador de eventos de renovación en el constructor:

```kotlin
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

    // NUEVO: Scope para observadores
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // NUEVO: Observar eventos de renovación exitosa
        serviceScope.launch {
            tokenRefreshManager.onRefreshSuccess.collect { newToken ->
                logger.info("Token refreshed automatically, updating state")
                
                // Actualizar storage
                storage.saveAuthToken(newToken)
                
                // Actualizar estado si estamos autenticados
                val currentState = _authState.value
                if (currentState is AuthState.Authenticated) {
                    _authState.value = currentState.copy(token = newToken)
                }
            }
        }

        // NUEVO: Observar eventos de renovación fallida
        serviceScope.launch {
            tokenRefreshManager.onRefreshFailure.collect { reason ->
                logger.error("Token refresh failed: $reason")
                
                // Emitir evento de sesión expirada
                _onSessionExpired.emit(Unit)
                
                // Limpiar sesión
                storage.clear()
                _authState.value = AuthState.Unauthenticated
                tokenRefreshManager.stopAutomaticRefresh()
            }
        }
    }

    // ... resto de métodos ...
}
```

**Validación**:
- Renovación se inicia automáticamente después de login
- Renovación se detiene en logout
- Eventos de renovación actualizan estado correctamente

---

### Tarea 3: Crear Interceptor HTTP para Inyección de Tokens

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/interceptor/AuthInterceptor.kt`

Actualizar o crear:

```kotlin
package com.edugo.kmp.auth.interceptor

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.logger.Logger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

/**
 * Interceptor que inyecta automáticamente el token de autenticación
 * en todas las peticiones HTTP.
 */
class AuthInterceptor(
    private val authService: AuthService,
    private val excludedPaths: Set<String> = setOf("/auth/login", "/auth/register")
) {

    private val logger = Logger.tagged("AuthInterceptor")

    companion object {
        val key = AttributeKey<AuthInterceptor>("AuthInterceptor")
    }

    /**
     * Instala el interceptor en el HttpClient.
     */
    fun install(client: HttpClientConfig<*>) {
        client.install(createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
            val excludedPaths = pluginConfig.excludedPaths

            onRequest { request, _ ->
                // Verificar si la ruta está excluida
                val path = request.url.encodedPath
                if (excludedPaths.any { path.contains(it) }) {
                    logger.debug("Skipping auth for excluded path: $path")
                    return@onRequest
                }

                // Obtener token
                val token = authService.getToken()
                
                if (token != null) {
                    logger.debug("Injecting auth token for: $path")
                    request.header(HttpHeaders.Authorization, "Bearer $token")
                } else {
                    logger.debug("No auth token available for: $path")
                }
            }
        })
    }
}

/**
 * Configuración del interceptor.
 */
class AuthInterceptorConfig {
    var excludedPaths: Set<String> = setOf("/auth/login", "/auth/register")
}

/**
 * Extension function para configurar fácilmente el interceptor.
 */
fun HttpClientConfig<*>.authInterceptor(
    authService: AuthService,
    excludedPaths: Set<String> = setOf("/auth/login", "/auth/register")
) {
    val interceptor = AuthInterceptor(authService, excludedPaths)
    interceptor.install(this)
}
```

**Validación**:
- Inyecta tokens automáticamente
- Excluye rutas de login/register
- Funciona con todas las peticiones HTTP

---

### Tarea 4: Actualizar HttpClient Factory para Usar Interceptor

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/di/src/commonMain/kotlin/com/edugo/kmp/di/NetworkModule.kt`

Crear o actualizar:

```kotlin
package com.edugo.kmp.di

import com.edugo.kmp.auth.interceptor.authInterceptor
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.network.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo de DI para networking con auth.
 */
val networkModule: Module = module {
    
    // HttpClient con interceptor de auth
    single<HttpClient> {
        val authService = get<AuthService>()
        
        HttpClientFactory.create(
            logLevel = LogLevel.INFO // Cambiar a NONE en producción
        ).config {
            // Instalar interceptor de auth
            authInterceptor(
                authService = authService,
                excludedPaths = setOf("/auth/login", "/auth/register", "/auth/refresh")
            )
        }
    }
}
```

**Validación**:
- HttpClient incluye interceptor de auth
- Todas las peticiones usan el mismo client

---

### Tarea 5: Crear Observadores de Eventos de Sesión

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/observers/SessionObserver.kt`

```kotlin
package com.edugo.kmp.auth.observers

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Observador de eventos de sesión.
 * Facilita la reacción a cambios de autenticación en la UI.
 */
class SessionObserver(
    private val authService: AuthService,
    private val scope: CoroutineScope
) {
    private val logger = Logger.tagged("SessionObserver")

    /**
     * Observa cambios de estado de autenticación.
     */
    fun observeAuthState(
        onAuthenticated: suspend (user: com.edugo.kmp.auth.model.AuthUserInfo) -> Unit = {},
        onUnauthenticated: suspend () -> Unit = {},
        onLoading: suspend () -> Unit = {}
    ) {
        scope.launch {
            authService.authState.collect { state ->
                when (state) {
                    is com.edugo.kmp.auth.service.AuthState.Authenticated -> {
                        logger.debug("State changed: Authenticated (${state.user.email})")
                        onAuthenticated(state.user)
                    }
                    is com.edugo.kmp.auth.service.AuthState.Unauthenticated -> {
                        logger.debug("State changed: Unauthenticated")
                        onUnauthenticated()
                    }
                    is com.edugo.kmp.auth.service.AuthState.Loading -> {
                        logger.debug("State changed: Loading")
                        onLoading()
                    }
                }
            }
        }
    }

    /**
     * Observa eventos de sesión expirada.
     */
    fun observeSessionExpired(
        onSessionExpired: suspend () -> Unit
    ) {
        scope.launch {
            authService.onSessionExpired.collect {
                logger.warn("Session expired event received")
                onSessionExpired()
            }
        }
    }

    /**
     * Observa eventos de logout.
     */
    fun observeLogout(
        onLogout: suspend (com.edugo.kmp.auth.model.LogoutResult) -> Unit
    ) {
        scope.launch {
            authService.onLogout.collect { result ->
                logger.info("Logout event received: $result")
                onLogout(result)
            }
        }
    }
}
```

**Validación**:
- Facilita observación de eventos desde UI
- Usa CoroutineScope provisto

---

## Tests de Fase 2

### Test 1: TokenRefreshManager Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/token/TokenRefreshManagerImplTest.kt`

```kotlin
package com.edugo.kmp.auth.token

import app.cash.turbine.test
import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.RefreshResponse
import com.edugo.kmp.auth.repository.TokenVerificationResponse
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.*
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class TokenRefreshManagerImplTest {

    private lateinit var mockRepository: MockAuthRepository
    private lateinit var refreshManager: TokenRefreshManagerImpl
    private val testScope = TestScope()

    @BeforeTest
    fun setup() {
        mockRepository = MockAuthRepository()
        refreshManager = TokenRefreshManagerImpl(
            repository = mockRepository,
            config = TokenRefreshConfig.default(),
            scope = testScope
        )
    }

    @AfterTest
    fun teardown() {
        testScope.cancel()
    }

    @Test
    fun `refreshToken success emits success event`() = testScope.runTest {
        val newToken = AuthToken.createTestToken()
        mockRepository.refreshResult = Result.Success(RefreshResponse(newToken))

        refreshManager.onRefreshSuccess.test {
            val result = refreshManager.refreshToken("refresh_token")
            
            assertTrue(result is Result.Success)
            assertEquals(newToken, awaitItem())
        }
    }

    @Test
    fun `refreshToken failure emits failure event`() = testScope.runTest {
        mockRepository.refreshResult = Result.Failure("Invalid token")

        refreshManager.onRefreshFailure.test {
            val result = refreshManager.refreshToken("refresh_token")
            
            assertTrue(result is Result.Failure)
            val failure = awaitItem()
            assertTrue(failure is RefreshFailureReason.NetworkError)
        }
    }

    @Test
    fun `startAutomaticRefresh schedules refresh correctly`() = testScope.runTest {
        val token = AuthToken.createTestToken(durationSeconds = 10)
        val newToken = AuthToken.createTestToken(durationSeconds = 20)
        
        mockRepository.refreshResult = Result.Success(RefreshResponse(newToken))

        refreshManager.onRefreshSuccess.test {
            refreshManager.startAutomaticRefresh(token)
            
            // Avanzar tiempo hasta que se ejecute refresh
            advanceTimeBy(8.seconds) // threshold 80% de 10s = 8s
            
            assertEquals(newToken, awaitItem())
        }
    }

    @Test
    fun `stopAutomaticRefresh cancels scheduled refresh`() = testScope.runTest {
        val token = AuthToken.createTestToken(durationSeconds = 10)
        
        refreshManager.startAutomaticRefresh(token)
        refreshManager.stopAutomaticRefresh()
        
        refreshManager.onRefreshSuccess.test {
            advanceTimeBy(10.seconds)
            expectNoEvents()
        }
    }
}

private class MockAuthRepository : AuthRepository {
    var refreshResult: Result<RefreshResponse> = Result.Failure("Not configured")

    override suspend fun login(credentials: LoginCredentials) = 
        Result.Failure<LoginResponse>("Not implemented")
    
    override suspend fun refreshToken(refreshToken: String) = refreshResult
    
    override suspend fun verifyToken(token: String) = 
        Result.Success(TokenVerificationResponse(true))
    
    override suspend fun logout(token: String) = 
        Result.Success(Unit)
}
```

---

### Test 2: AuthInterceptor Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/interceptor/AuthInterceptorTest.kt`

```kotlin
package com.edugo.kmp.auth.interceptor

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthState
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.foundation.result.Result
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AuthInterceptorTest {

    @Test
    fun `interceptor adds auth header when token available`() = runTest {
        val mockAuthService = MockAuthService(hasToken = true)
        
        val client = HttpClient(MockEngine { request ->
            // Verificar que el header de autorización está presente
            val authHeader = request.headers[HttpHeaders.Authorization]
            assertNotNull(authHeader)
            assertTrue(authHeader.startsWith("Bearer "))
            
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }) {
            install(ContentNegotiation) {
                json()
            }
            authInterceptor(mockAuthService)
        }

        client.get("https://api.edugo.com/users/me")
    }

    @Test
    fun `interceptor skips auth for excluded paths`() = runTest {
        val mockAuthService = MockAuthService(hasToken = false)
        
        val client = HttpClient(MockEngine { request ->
            // Verificar que NO hay header de autorización
            val authHeader = request.headers[HttpHeaders.Authorization]
            assertNull(authHeader)
            
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }) {
            install(ContentNegotiation) {
                json()
            }
            authInterceptor(mockAuthService, setOf("/auth/login"))
        }

        client.post("https://api.edugo.com/auth/login")
    }

    @Test
    fun `interceptor handles missing token gracefully`() = runTest {
        val mockAuthService = MockAuthService(hasToken = false)
        
        val client = HttpClient(MockEngine { request ->
            val authHeader = request.headers[HttpHeaders.Authorization]
            assertNull(authHeader)
            
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }) {
            authInterceptor(mockAuthService)
        }

        // No debe fallar
        client.get("https://api.edugo.com/public/data")
    }
}

private class MockAuthService(
    private val hasToken: Boolean
) : AuthService {
    override val authState: StateFlow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)
    override val tokenRefreshManager: TokenRefreshManager get() = TODO()
    override val onSessionExpired: Flow<Unit> = emptyFlow()
    override val onLogout: Flow<LogoutResult> = emptyFlow()
    
    override suspend fun login(credentials: LoginCredentials): LoginResult = TODO()
    override suspend fun logout(): Result<Unit> = TODO()
    override suspend fun logoutWithDetails(forceLocal: Boolean): LogoutResult = TODO()
    override suspend fun refreshAuthToken(): Result<AuthToken> = TODO()
    override fun isAuthenticated(): Boolean = hasToken
    
    override suspend fun getToken(): String? = if (hasToken) "test_token_123" else null
    
    override suspend fun isTokenExpired(): Boolean = false
    override fun getCurrentUser(): AuthUserInfo? = null
    override fun getCurrentAuthToken(): AuthToken? = null
    override suspend fun restoreSession() {}
}
```

---

## Criterios de Aceptación de Fase 2

### Funcionalidad
- [ ] Tokens se renuevan automáticamente antes de expirar
- [ ] Renovación automática se inicia después de login
- [ ] Renovación automática se detiene en logout
- [ ] Interceptor inyecta tokens en todas las peticiones HTTP
- [ ] Interceptor excluye rutas de login/register
- [ ] Eventos de renovación exitosa actualizan estado
- [ ] Eventos de renovación fallida limpian sesión

### Testing
- [ ] TokenRefreshManagerImplTest pasa todos los tests
- [ ] AuthInterceptorTest pasa todos los tests
- [ ] Tests de Fase 1 siguen pasando
- [ ] Cobertura de tests > 75%

### Compilación
- [ ] `./gradlew :modules:auth:build` exitoso
- [ ] Cero warnings
- [ ] Cero errores de lint

---

## Checklist de Validación

```bash
# 1. Compilar
./gradlew :modules:auth:build

# 2. Ejecutar tests
./gradlew :modules:auth:test

# 3. Verificar que tests de Fase 1 siguen pasando
./gradlew :modules:auth:test --tests "*AuthServiceImplTest*"

# 4. Lint
./gradlew :modules:auth:lintKotlin
```

**Todos deben pasar** antes de continuar a Fase 3.

---

## Cómo Probar Manualmente

### Test 1: Renovación Automática

```kotlin
// En tu app
lifecycleScope.launch {
    // Crear token que expira en 10 segundos
    val shortLivedToken = AuthToken(
        token = "test_token",
        expiresAt = Clock.System.now() + 10.seconds,
        refreshToken = "refresh_token"
    )
    
    // Observar eventos de renovación
    authService.tokenRefreshManager.onRefreshSuccess.collect { newToken ->
        println("Token renovado automáticamente: ${newToken.toLogString()}")
    }
}
```

### Test 2: Interceptor HTTP

```kotlin
lifecycleScope.launch {
    // Login primero
    authService.login(LoginCredentials("user@edugo.com", "password"))
    
    // Hacer petición HTTP - debe incluir token automáticamente
    val response = httpClient.get("https://api.edugo.com/users/me")
    println("Response: $response")
    
    // Verificar en logs que el token fue inyectado
}
```

### Test 3: Eventos de Sesión Expirada

```kotlin
val sessionObserver = SessionObserver(authService, lifecycleScope)

sessionObserver.observeSessionExpired {
    println("Sesión expirada - redirigir a login")
    navController.navigate("login")
}
```

---

## Problemas Comunes

### Error: "Token refresh loop"
**Causa**: Renovación se programa incorrectamente  
**Solución**: Verificar cálculo de `refreshDelay` en `calculateRefreshDelay`

### Error: Interceptor no inyecta token
**Causa**: HttpClient no incluye interceptor  
**Solución**: Verificar que `authInterceptor()` está llamado en configuración de client

### Test falla: "No events received"
**Causa**: TestScope no avanza tiempo  
**Solución**: Usar `advanceTimeBy()` en tests con delays

---

## Siguiente Fase

Una vez completada Fase 2, continuar con:
**[FASE-3-ROBUSTEZ.md](FASE-3-ROBUSTEZ.md)**

---

**Fase**: 2 - Reactivo  
**Estado**: Listo para implementar  
**Prerequisito**: Fase 1 completada  
**Prerequisito para**: Fase 3
