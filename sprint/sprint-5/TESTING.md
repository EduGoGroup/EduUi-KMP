# Estrategia de Testing

## Visión General

La estrategia de testing del sistema de autenticación cubre tres niveles: unitarios, integración, y end-to-end. El objetivo es alcanzar >80% de cobertura con tests rápidos, confiables y mantenibles.

---

## Pirámide de Testing

```
        ┌─────────────┐
        │   E2E (5%)  │  Manual testing en devices
        └─────────────┘
      ┌─────────────────┐
      │ Integration (15%)│  Auth flows completos
      └─────────────────┘
    ┌─────────────────────┐
    │   Unit Tests (80%)  │  Componentes individuales
    └─────────────────────┘
```

---

## Tests por Fase

### Fase 1: MVP

#### Unit Tests

**AuthStorageTest** (8 tests)
- `saveAuthToken and getAuthToken works correctly`
- `saveUserInfo and getUserInfo works correctly`
- `clear removes all auth data`
- `hasStoredSession returns false when no data`
- `hasStoredSession returns true when data exists`
- `getRefreshToken returns correct value`
- `handles corrupted JSON gracefully`
- `handles missing fields in JSON`

**AuthServiceImplTest** (10 tests)
- `login success updates state and saves to storage`
- `login failure keeps state unauthenticated`
- `logout clears state and storage`
- `restoreSession restores authenticated state`
- `restoreSession with expired token attempts refresh`
- `restoreSession with no session sets unauthenticated`
- `isAuthenticated returns correct value`
- `getCurrentUser returns correct user`
- `getCurrentAuthToken returns correct token`
- `getToken returns token string when authenticated`

**AuthRepositoryImplTest** (8 tests)
- `login success returns LoginResponse`
- `login with invalid credentials returns failure`
- `login with network error returns failure`
- `refreshToken success returns new token`
- `refreshToken with invalid token returns failure`
- `logout always succeeds locally`
- `verifyToken success returns valid`
- `verifyToken with invalid token returns failure`

**Total Fase 1: 26 tests**

---

### Fase 2: Reactivo

#### Unit Tests

**TokenRefreshManagerImplTest** (10 tests)
- `refreshToken success emits success event`
- `refreshToken failure emits failure event`
- `startAutomaticRefresh schedules refresh correctly`
- `stopAutomaticRefresh cancels scheduled refresh`
- `scheduleRefresh calculates delay correctly`
- `performRefresh updates token on success`
- `performRefresh emits failure on error`
- `automatic refresh loops on success`
- `expired token refreshes immediately`
- `no refresh token prevents scheduling`

**AuthInterceptorTest** (6 tests)
- `interceptor adds auth header when token available`
- `interceptor skips auth for excluded paths`
- `interceptor handles missing token gracefully`
- `interceptor handles multiple concurrent requests`
- `interceptor refreshes token on 401`
- `interceptor custom excluded paths work`

**AuthServiceReactiveTest** (8 tests)
- `login starts automatic token refresh`
- `logout stops automatic token refresh`
- `refresh success event updates state`
- `refresh failure event clears session`
- `onSessionExpired emits when token refresh fails`
- `onLogout emits with correct details`
- `restoreSession starts automatic refresh`
- `authState emits correctly on all operations`

**Total Fase 2: 24 tests adicionales = 50 tests acumulados**

---

### Fase 3: Robustez

#### Unit Tests

**RetryPolicyTest** (8 tests)
- `withRetry succeeds on first attempt`
- `withRetry retries on retryable error`
- `withRetry does not retry on non-retryable error`
- `withRetry respects max attempts`
- `exponential backoff increases delay correctly`
- `max delay is respected`
- `custom retry policy works`
- `retryable errors are case insensitive`

**CircuitBreakerTest** (10 tests)
- `circuit breaker starts in closed state`
- `circuit breaker opens after failure threshold`
- `circuit breaker rejects requests when open`
- `circuit breaker transitions to half-open after timeout`
- `circuit breaker closes after successful recovery`
- `circuit breaker reopens on half-open failure`
- `forceOpen works correctly`
- `forceClose works correctly`
- `getState returns correct state`
- `multiple failures increment count`

**RateLimiterTest** (7 tests)
- `allows requests within limit`
- `blocks requests exceeding limit`
- `allows requests after time window expires`
- `remainingRequests returns correct value`
- `reset clears all timestamps`
- `cleanup removes old timestamps`
- `concurrent requests respect limit`

**AuthLoggerTest** (5 tests)
- `logLoginAttempt formats correctly`
- `logLoginSuccess includes userId`
- `logTokenRefresh includes success flag`
- `logSecurityEvent formats details correctly`
- `logs never include sensitive data`

**AuthConfigTest** (3 tests)
- `development config has correct values`
- `production config has correct values`
- `testing config has correct values`

**Total Fase 3: 33 tests adicionales = 83 tests acumulados**

---

#### Integration Tests

**AuthFlowIntegrationTest** (12 tests)
- `complete login flow updates all components`
- `login then logout clears all data`
- `login then refresh updates token`
- `restore session with valid token succeeds`
- `restore session with expired token refreshes`
- `restore session with invalid token clears session`
- `automatic refresh updates storage and state`
- `automatic refresh failure triggers session expired`
- `HTTP requests include auth header after login`
- `HTTP requests exclude auth header on excluded paths`
- `circuit breaker prevents login after failures`
- `rate limiter prevents excessive login attempts`

**Total Integration: 12 tests**

**Total Fase 3: 95 tests acumulados**

---

## Estructura de Carpetas de Tests

```
modules/auth/src/
├── commonTest/kotlin/com/edugo/kmp/auth/
│   ├── storage/
│   │   └── AuthStorageTest.kt
│   ├── repository/
│   │   ├── AuthRepositoryImplTest.kt
│   │   └── MockAuthRepository.kt
│   ├── service/
│   │   ├── AuthServiceImplTest.kt
│   │   └── AuthServiceReactiveTest.kt
│   ├── token/
│   │   ├── TokenRefreshManagerImplTest.kt
│   │   └── TokenRefreshConfigTest.kt
│   ├── interceptor/
│   │   └── AuthInterceptorTest.kt
│   ├── retry/
│   │   └── RetryPolicyTest.kt
│   ├── circuit/
│   │   └── CircuitBreakerTest.kt
│   ├── throttle/
│   │   └── RateLimiterTest.kt
│   ├── logging/
│   │   └── AuthLoggerTest.kt
│   ├── config/
│   │   └── AuthConfigTest.kt
│   ├── integration/
│   │   └── AuthFlowIntegrationTest.kt
│   └── helpers/
│       ├── MockHttpClient.kt
│       ├── TestData.kt
│       └── TestExtensions.kt
└── androidTest/kotlin/com/edugo/kmp/auth/
    └── e2e/
        └── AuthE2ETest.kt
```

---

## Ejemplos de Tests Unitarios

### Test Básico con Assertions

```kotlin
@Test
fun `login success updates state and saves to storage`() = runTest {
    // Arrange
    val credentials = LoginCredentials("test@edugo.com", "password123")
    val expectedUser = AuthUserInfo("user123", "test@edugo.com", "testuser", "student")
    val expectedToken = AuthToken.createTestToken()
    
    mockRepository.loginResult = Result.Success(
        LoginResponse(expectedUser, expectedToken)
    )

    // Act
    val result = authService.login(credentials)

    // Assert
    assertTrue(result is LoginResult.Success)
    assertEquals(expectedUser.email, result.user.email)
    
    // Verify state
    authService.authState.test {
        val state = awaitItem()
        assertTrue(state is AuthState.Authenticated)
        assertEquals(expectedUser.email, state.user.email)
    }
    
    // Verify storage
    assertNotNull(authStorage.getAuthToken())
    assertNotNull(authStorage.getUserInfo())
}
```

### Test con Flows usando Turbine

```kotlin
@Test
fun `authState emits correctly on login`() = runTest {
    val credentials = LoginCredentials("test@edugo.com", "password123")
    mockRepository.loginResult = Result.Success(
        LoginResponse(
            AuthUserInfo("user123", "test@edugo.com", "testuser", "student"),
            AuthToken.createTestToken()
        )
    )

    authService.authState.test {
        // Estado inicial
        assertTrue(awaitItem() is AuthState.Unauthenticated)
        
        // Hacer login
        authService.login(credentials)
        
        // Loading state
        assertTrue(awaitItem() is AuthState.Loading)
        
        // Authenticated state
        val authenticated = awaitItem()
        assertTrue(authenticated is AuthState.Authenticated)
        assertEquals("test@edugo.com", authenticated.user.email)
    }
}
```

### Test con Delays usando TestScope

```kotlin
@Test
fun `automatic refresh triggers after delay`() = testScope.runTest {
    val token = AuthToken.createTestToken(durationSeconds = 10)
    val newToken = AuthToken.createTestToken(durationSeconds = 20)
    
    mockRepository.refreshResult = Result.Success(RefreshResponse(newToken))

    refreshManager.onRefreshSuccess.test {
        refreshManager.startAutomaticRefresh(token)
        
        // Avanzar tiempo hasta threshold (80% de 10s = 8s)
        advanceTimeBy(8.seconds)
        
        // Verificar que se emitió el nuevo token
        assertEquals(newToken, awaitItem())
    }
}
```

### Test de Errores

```kotlin
@Test
fun `login with network error returns failure`() = runTest {
    val credentials = LoginCredentials("test@edugo.com", "password123")
    mockRepository.loginResult = Result.Failure("Network error")

    val result = authService.login(credentials)

    assertTrue(result is LoginResult.Failure)
    assertTrue(result.error.contains("Network error"))
    
    // Estado debe ser unauthenticated
    authService.authState.test {
        assertTrue(awaitItem() is AuthState.Unauthenticated)
    }
}
```

---

## Ejemplos de Tests de Integración

### Test de Flujo Completo

```kotlin
@Test
fun `complete auth flow - login, refresh, logout`() = runTest {
    // Setup
    val credentials = LoginCredentials("test@edugo.com", "password123")
    val user = AuthUserInfo("user123", "test@edugo.com", "testuser", "student")
    val initialToken = AuthToken.createTestToken(durationSeconds = 10)
    val refreshedToken = AuthToken.createTestToken(durationSeconds = 20)
    
    mockRepository.loginResult = Result.Success(LoginResponse(user, initialToken))
    mockRepository.refreshResult = Result.Success(RefreshResponse(refreshedToken))
    mockRepository.logoutResult = Result.Success(Unit)

    // 1. Login
    val loginResult = authService.login(credentials)
    assertTrue(loginResult is LoginResult.Success)
    assertTrue(authService.isAuthenticated())
    
    // 2. Esperar refresh automático
    authService.tokenRefreshManager.onRefreshSuccess.test {
        advanceTimeBy(8.seconds) // threshold
        val newToken = awaitItem()
        assertNotNull(newToken)
    }
    
    // 3. Verificar que storage se actualizó
    val storedToken = authStorage.getAuthToken()
    assertNotNull(storedToken)
    assertEquals(refreshedToken.token, storedToken.token)
    
    // 4. Logout
    authService.logout()
    assertFalse(authService.isAuthenticated())
    assertNull(authStorage.getAuthToken())
    assertNull(authStorage.getUserInfo())
}
```

### Test de Restauración de Sesión

```kotlin
@Test
fun `session restoration with expired token refreshes automatically`() = runTest {
    // Setup: Guardar sesión con token expirado
    val expiredToken = AuthToken(
        token = "expired_token",
        expiresAt = Clock.System.now() - 1.hours,
        refreshToken = "refresh_token"
    )
    val user = AuthUserInfo("user123", "test@edugo.com", "testuser", "student")
    val newToken = AuthToken.createTestToken()
    
    authStorage.saveAuthToken(expiredToken)
    authStorage.saveUserInfo(user)
    
    mockRepository.refreshResult = Result.Success(RefreshResponse(newToken))

    // Act: Restaurar sesión
    authService.restoreSession()

    // Assert: Debe haber renovado token y restaurado sesión
    authService.authState.test {
        val state = awaitItem()
        assertTrue(state is AuthState.Authenticated)
        assertEquals(user.email, state.user.email)
        assertFalse(state.token.isExpired())
    }
    
    // Verificar que se guardó nuevo token
    val storedToken = authStorage.getAuthToken()
    assertNotNull(storedToken)
    assertEquals(newToken.token, storedToken.token)
}
```

---

## Mocks y Fakes

### MockAuthRepository

```kotlin
class MockAuthRepository : AuthRepository {
    var loginResult: Result<LoginResponse> = Result.Failure("Not configured")
    var refreshResult: Result<RefreshResponse> = Result.Failure("Not configured")
    var verifyResult: Result<TokenVerificationResponse> = Result.Success(
        TokenVerificationResponse(true)
    )
    var logoutResult: Result<Unit> = Result.Success(Unit)

    override suspend fun login(credentials: LoginCredentials) = loginResult
    override suspend fun refreshToken(refreshToken: String) = refreshResult
    override suspend fun verifyToken(token: String) = verifyResult
    override suspend fun logout(token: String) = logoutResult
}
```

### MockHttpClient

```kotlin
fun createMockHttpClient(
    responses: Map<String, HttpResponse> = emptyMap()
): HttpClient {
    return HttpClient(MockEngine { request ->
        val url = request.url.toString()
        val mockResponse = responses[url] ?: defaultResponse
        
        respond(
            content = mockResponse.content,
            status = mockResponse.status,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }) {
        install(ContentNegotiation) {
            json()
        }
    }
}

data class HttpResponse(
    val content: String,
    val status: HttpStatusCode
)

val defaultResponse = HttpResponse(
    content = """{"error": "Not found"}""",
    status = HttpStatusCode.NotFound
)
```

### Test Data Builders

```kotlin
object TestData {
    fun createUser(
        id: String = "user123",
        email: String = "test@edugo.com",
        username: String = "testuser",
        role: String = "student"
    ) = AuthUserInfo(id, email, username, role)
    
    fun createToken(
        token: String = "test_token_${Clock.System.now().toEpochMilliseconds()}",
        expiresInSeconds: Long = 3600,
        includeRefresh: Boolean = true
    ) = AuthToken(
        token = token,
        expiresAt = Clock.System.now() + expiresInSeconds.seconds,
        refreshToken = if (includeRefresh) "refresh_$token" else null
    )
    
    fun createLoginResponse(
        user: AuthUserInfo = createUser(),
        token: AuthToken = createToken()
    ) = LoginResponse(user, token)
}
```

---

## Cobertura Esperada

### Por Componente

| Componente | Cobertura Mínima | Cobertura Objetivo |
|------------|------------------|-------------------|
| AuthService | 85% | 95% |
| AuthRepository | 80% | 90% |
| AuthStorage | 90% | 100% |
| TokenRefreshManager | 80% | 90% |
| AuthInterceptor | 75% | 85% |
| RetryPolicy | 85% | 95% |
| CircuitBreaker | 85% | 95% |
| RateLimiter | 80% | 90% |

### Global

**Mínimo aceptable**: 80%  
**Objetivo**: 85%+

---

## Comandos para Ejecutar Tests

### Ejecutar Todos los Tests

```bash
./gradlew :modules:auth:test
```

### Ejecutar Tests Específicos

```bash
# Solo tests de storage
./gradlew :modules:auth:test --tests "*AuthStorageTest*"

# Solo tests de service
./gradlew :modules:auth:test --tests "*AuthServiceImplTest*"

# Solo tests de integración
./gradlew :modules:auth:test --tests "*IntegrationTest*"
```

### Ejecutar con Cobertura

```bash
# Android
./gradlew :modules:auth:testDebugUnitTestCoverage

# Generar reporte HTML
./gradlew :modules:auth:testDebugUnitTestCoverage
# Reporte en: modules/auth/build/reports/coverage/test/debug/index.html
```

### Ejecutar Tests Continuos (Watch Mode)

```bash
./gradlew :modules:auth:test --continuous
```

### Ejecutar Tests con Logs Detallados

```bash
./gradlew :modules:auth:test --info
```

---

## Configuración de Tests

### build.gradle.kts

```kotlin
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.ktor.client.mock)
                implementation(libs.multiplatform.settings.test)
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.robolectric)
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
```

---

## Testing Best Practices

### 1. Nombrado de Tests

```kotlin
// ✅ Bueno: Describe qué hace y qué se espera
@Test
fun `login with valid credentials updates state to authenticated`()

// ❌ Malo: No descriptivo
@Test
fun testLogin()
```

### 2. Arrange-Act-Assert

```kotlin
@Test
fun `example test`() = runTest {
    // Arrange: Setup
    val credentials = LoginCredentials("user@edugo.com", "password")
    mockRepository.loginResult = Result.Success(...)
    
    // Act: Execute
    val result = authService.login(credentials)
    
    // Assert: Verify
    assertTrue(result is LoginResult.Success)
}
```

### 3. Un Assert por Test (cuando sea posible)

```kotlin
// ✅ Bueno
@Test
fun `login success returns correct user`() = runTest {
    val result = authService.login(credentials)
    assertEquals("test@edugo.com", result.user.email)
}

@Test
fun `login success updates state`() = runTest {
    authService.login(credentials)
    assertTrue(authService.authState.value is AuthState.Authenticated)
}

// ⚠️ Aceptable si están muy relacionados
@Test
fun `login success updates state and storage`() = runTest {
    authService.login(credentials)
    assertTrue(authService.isAuthenticated())
    assertNotNull(authStorage.getAuthToken())
}
```

### 4. Tests Independientes

```kotlin
// ✅ Bueno: Cada test limpia su estado
@BeforeTest
fun setup() {
    storage = EduGoStorage.withSettings(MapSettings())
    authStorage = AuthStorage(storage)
}

@AfterTest
fun teardown() {
    storage.clear()
}

// ❌ Malo: Tests dependen del orden
@Test
fun test1() {
    authService.login(...)
}

@Test
fun test2() {
    // Asume que test1 ya ejecutó login
    authService.logout()
}
```

### 5. Tests Rápidos

```kotlin
// ✅ Bueno: Usa TestScope para controlar tiempo
@Test
fun `automatic refresh after delay`() = testScope.runTest {
    refreshManager.startAutomaticRefresh(token)
    advanceTimeBy(8.seconds)
    // Test completa inmediatamente
}

// ❌ Malo: Delay real
@Test
fun `automatic refresh after delay`() = runTest {
    refreshManager.startAutomaticRefresh(token)
    delay(8000) // Espera 8 segundos reales
}
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run tests
        run: ./gradlew :modules:auth:test
      
      - name: Generate coverage report
        run: ./gradlew :modules:auth:testDebugUnitTestCoverage
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./modules/auth/build/reports/jacoco/test/jacocoTestReport.xml
          
      - name: Fail if coverage < 80%
        run: |
          COVERAGE=$(cat modules/auth/build/reports/jacoco/test/html/index.html | grep -oP 'Total.*?(\d+)%' | grep -oP '\d+')
          if [ $COVERAGE -lt 80 ]; then
            echo "Coverage $COVERAGE% is below 80%"
            exit 1
          fi
```

---

## Debugging Tests

### Ver Logs en Tests

```kotlin
@Test
fun `debug test`() = runTest {
    // Activar logs detallados
    Logger.setMinSeverity(Severity.DEBUG)
    
    authService.login(credentials)
    
    // Los logs aparecerán en output de test
}
```

### Inspeccionar Estado en Fallo

```kotlin
@Test
fun `debug failing test`() = runTest {
    authService.login(credentials)
    
    // Print estado para debugging
    println("AuthState: ${authService.authState.value}")
    println("Storage token: ${authStorage.getAuthToken()}")
    println("Storage user: ${authStorage.getUserInfo()}")
    
    // Assertions...
}
```

---

## Métricas de Calidad

### Tests deben ser:

1. **Fast**: < 100ms por test unitario
2. **Independent**: No depender de otros tests
3. **Repeatable**: Mismo resultado cada vez
4. **Self-validating**: Pass o fail automático
5. **Timely**: Escritos antes o durante desarrollo

### Indicadores de Éxito:

- [ ] Cobertura > 80%
- [ ] Todos los tests pasan
- [ ] Tiempo total de suite < 30 segundos
- [ ] Cero tests flaky (intermitentes)
- [ ] Cero tests ignorados (@Ignore)

---

## Próximos Pasos

Después de implementar todos los tests:

1. Ejecutar suite completa
2. Verificar cobertura
3. Corregir tests fallidos
4. Revisar cobertura de líneas críticas
5. Agregar tests para edge cases descubiertos
6. Integrar en CI/CD

---

**Documento**: Testing  
**Versión**: 1.0  
**Total de Tests**: 95 (83 unit + 12 integration)  
**Cobertura Objetivo**: 85%+
