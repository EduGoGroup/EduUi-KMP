# Guía de Solución de Problemas

## Visión General

Esta guía contiene soluciones a errores comunes, tips de debugging, y herramientas de diagnóstico para el sistema de autenticación.

---

## Errores Comunes y Soluciones

### 1. Login Falla con "Network Error"

#### Síntomas
```
LoginResult.Failure("Error de conexión: Unable to resolve host")
```

#### Causas Posibles
1. URL base incorrecta
2. Servidor no disponible
3. Sin conexión a internet
4. Timeout muy corto

#### Soluciones

**Verificar URL base:**
```kotlin
// En AuthModule
single<AuthService> {
    AuthServiceFactory.create(
        httpClient = get(),
        baseUrl = "https://api.edugo.com" // ← Verificar URL
    )
}
```

**Verificar conectividad:**
```kotlin
// Agregar logging
val logger = Logger.tagged("NetworkDebug")
logger.debug("Attempting connection to: $baseUrl")

// Probar con curl
curl -v https://api.edugo.com/auth/login
```

**Ajustar timeouts:**
```kotlin
HttpClientFactory.create(
    connectTimeoutMs = 30_000, // 30 segundos
    requestTimeoutMs = 60_000  // 60 segundos
)
```

**Verificar permisos (Android):**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

### 2. "No Refresh Token Available"

#### Síntomas
```
Result.Failure("No refresh token available")
```

#### Causas Posibles
1. Token guardado sin refresh token
2. API no retorna refresh token
3. Refresh token expiró

#### Soluciones

**Verificar response de API:**
```kotlin
// En AuthRepositoryImpl, agregar log
logger.debug("Login response: ${response.body<String>()}")
```

**Verificar modelo:**
```kotlin
@Serializable
data class LoginResponse(
    @SerialName("user")
    val user: AuthUserInfo,
    
    @SerialName("token")
    val token: AuthToken // ← Debe incluir refresh_token
)
```

**Verificar storage:**
```kotlin
val token = authStorage.getAuthToken()
logger.debug("Stored token has refresh: ${token?.hasRefreshToken()}")
```

---

### 3. Tests Fallan con "Timeout waiting for event"

#### Síntomas
```
Expected an event but no item was emitted during 3000ms
```

#### Causas Posibles
1. Flow no emite
2. Test no avanza tiempo virtual
3. Delay muy largo

#### Soluciones

**Usar TestScope correctamente:**
```kotlin
// ✅ Correcto
@Test
fun `test with delay`() = testScope.runTest {
    refreshManager.startAutomaticRefresh(token)
    advanceTimeBy(8.seconds) // Avanza tiempo virtual
    // ...
}

// ❌ Incorrecto
@Test
fun `test with delay`() = runTest {
    refreshManager.startAutomaticRefresh(token)
    delay(8000) // Espera real
    // ...
}
```

**Verificar que Flow emite:**
```kotlin
flow.test {
    // Agregar timeout más largo
    withTimeout(5000) {
        val item = awaitItem()
        assertNotNull(item)
    }
}
```

---

### 4. Sesión no se Restaura al Reiniciar App

#### Síntomas
- Usuario debe hacer login cada vez que abre la app
- `restoreSession()` no funciona

#### Causas Posibles
1. Storage no persiste datos
2. `restoreSession()` no se llama
3. Token expirado y refresh falla

#### Soluciones

**Verificar que se llama restoreSession:**
```kotlin
// En App.onCreate() o main()
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        lifecycleScope.launch {
            val authService = get<AuthService>()
            authService.restoreSession() // ← Debe llamarse
        }
    }
}
```

**Verificar persistencia de storage:**
```kotlin
// Test manual
lifecycleScope.launch {
    // 1. Guardar
    authStorage.saveAuthToken(token)
    
    // 2. Verificar inmediatamente
    val retrieved = authStorage.getAuthToken()
    logger.debug("Token persisted: ${retrieved != null}")
    
    // 3. Reiniciar app
    // 4. Verificar que sigue ahí
    val afterRestart = authStorage.getAuthToken()
    logger.debug("Token after restart: ${afterRestart != null}")
}
```

**Verificar logs:**
```kotlin
// AuthServiceImpl.restoreSession()
logger.info("Attempting to restore session from storage")
logger.debug("Has stored session: ${storage.hasStoredSession()}")
logger.debug("Token: ${storage.getAuthToken()?.toLogString()}")
logger.debug("User: ${storage.getUserInfo()?.email}")
```

---

### 5. Token Refresh Loop Infinito

#### Síntomas
- CPU al 100%
- Logs muestran refresh constante
- App se congela

#### Causas Posibles
1. Cálculo de delay incorrecto
2. Token nuevo tiene expiración incorrecta
3. Refresh no cancela job anterior

#### Soluciones

**Verificar cálculo de delay:**
```kotlin
// En TokenRefreshManagerImpl
private fun calculateRefreshDelay(timeUntilExpiration: Duration): Duration {
    val thresholdDuration = timeUntilExpiration * config.refreshThresholdPercent / 100.0
    val delay = maxOf(thresholdDuration, config.minRefreshInterval.milliseconds)
    
    // Agregar validación
    if (delay.isNegative() || delay < 1.seconds) {
        logger.error("Invalid delay calculated: $delay")
        return 60.seconds // Fallback seguro
    }
    
    logger.debug("Refresh delay: ${delay.inWholeSeconds}s")
    return delay
}
```

**Verificar que se cancela job anterior:**
```kotlin
override fun startAutomaticRefresh(token: AuthToken) {
    logger.info("Starting automatic token refresh")
    
    currentToken = token
    stopAutomaticRefresh() // ← CRÍTICO: Cancelar anterior
    
    scheduleRefresh(token)
}
```

**Agregar circuit breaker al refresh:**
```kotlin
// En TokenRefreshManagerImpl
private var failureCount = 0
private val maxFailures = 5

private suspend fun performRefresh(token: AuthToken) {
    if (failureCount >= maxFailures) {
        logger.error("Max refresh failures reached, stopping")
        return
    }
    
    // ... resto del código
}
```

---

### 6. AuthInterceptor no Inyecta Token

#### Síntomas
- Requests fallan con 401 Unauthorized
- Logs muestran que hay token pero no se envía

#### Causas Posibles
1. Interceptor no instalado
2. Path excluido por error
3. Token es null

#### Soluciones

**Verificar instalación del interceptor:**
```kotlin
// En NetworkModule
single<HttpClient> {
    val authService = get<AuthService>()
    
    HttpClientFactory.create().config {
        authInterceptor(authService) // ← Debe estar aquí
    }
}
```

**Verificar paths excluidos:**
```kotlin
authInterceptor(
    authService = authService,
    excludedPaths = setOf(
        "/auth/login",
        "/auth/register",
        "/auth/refresh" // No excluir otras rutas
    )
)
```

**Agregar logging al interceptor:**
```kotlin
onRequest { request, _ ->
    val path = request.url.encodedPath
    val token = authService.getToken()
    
    logger.debug("Request to: $path")
    logger.debug("Token available: ${token != null}")
    logger.debug("Is excluded: ${excludedPaths.any { path.contains(it) }}")
    
    if (token != null && !isExcluded) {
        request.header(HttpHeaders.Authorization, "Bearer $token")
        logger.debug("Token injected")
    } else {
        logger.warn("Token NOT injected - token: ${token != null}, excluded: $isExcluded")
    }
}
```

---

### 7. Circuit Breaker Abierto Permanentemente

#### Síntomas
```
Result.Failure("Service temporarily unavailable. Retry in 28s")
```

#### Causas Posibles
1. Servidor caído por mucho tiempo
2. Threshold muy bajo
3. No se resetea correctamente

#### Soluciones

**Force close del circuit breaker:**
```kotlin
// En desarrollo/debug
val repository = get<AuthRepositoryImpl>()
repository.circuitBreaker.forceClose()
```

**Ajustar configuración:**
```kotlin
CircuitBreakerConfig(
    failureThreshold = 10, // Más tolerante
    timeout = 10.seconds,  // Timeout más corto
    successThreshold = 2
)
```

**Monitorear estado:**
```kotlin
lifecycleScope.launch {
    while (true) {
        val state = repository.circuitBreaker.getState()
        logger.debug("Circuit breaker state: $state")
        delay(5000)
    }
}
```

---

### 8. Serialization Exception

#### Síntomas
```
kotlinx.serialization.SerializationException: 
Field 'expires_at' is required but not found
```

#### Causas Posibles
1. Modelo no coincide con JSON
2. Falta @SerialName
3. JSON malformado

#### Soluciones

**Verificar JSON de respuesta:**
```kotlin
// Agregar logging raw
val responseBody = response.body<String>()
logger.debug("Raw JSON: $responseBody")

val parsed = response.body<LoginResponse>()
```

**Verificar anotaciones:**
```kotlin
@Serializable
data class AuthToken(
    @SerialName("token")        // ← CRÍTICO
    val token: String,
    
    @SerialName("expires_at")   // ← CRÍTICO
    val expiresAt: Instant,
    
    @SerialName("refresh_token") // ← CRÍTICO
    val refreshToken: String? = null
)
```

**Hacer campos opcionales temporalmente:**
```kotlin
// Para debugging
@Serializable
data class LoginResponse(
    @SerialName("user")
    val user: AuthUserInfo? = null, // Opcional
    
    @SerialName("token")
    val token: AuthToken? = null    // Opcional
)
```

---

## Debugging Tips

### 1. Habilitar Logs Detallados

```kotlin
// En desarrollo
Logger.setMinSeverity(Severity.DEBUG)

// Solo para auth
val authLogger = Logger.tagged("Auth")
authLogger.setMinSeverity(Severity.VERBOSE)
```

### 2. Inspeccionar Storage

#### Android
```bash
# Ver SharedPreferences
adb shell run-as com.edugo.app cat /data/data/com.edugo.app/shared_prefs/auth.xml

# O usar Device File Explorer en Android Studio
```

#### iOS
```swift
// En Xcode, breakpoint en:
UserDefaults.standard.dictionaryRepresentation()
```

#### Desktop
```bash
# Linux
cat ~/.config/edugo/auth.properties

# macOS
cat ~/Library/Preferences/com.edugo.auth.plist

# Windows
# Ver en Registry Editor: HKEY_CURRENT_USER\Software\EduGo\Auth
```

### 3. Capturar Tráfico HTTP

```bash
# Usar Charles Proxy o mitmproxy

# Android: Configurar proxy en WiFi
# iOS: Settings → WiFi → Configure Proxy

# Ver requests/responses
mitmproxy -p 8888
```

### 4. Verificar Estado de Auth en Runtime

```kotlin
// Agregar debug screen
@Composable
fun AuthDebugScreen(authService: AuthService) {
    val state by authService.authState.collectAsState()
    
    Column {
        Text("State: ${state::class.simpleName}")
        
        if (state is AuthState.Authenticated) {
            val auth = state as AuthState.Authenticated
            Text("User: ${auth.user.email}")
            Text("Token: ${auth.token.toLogString()}")
            Text("Expired: ${auth.token.isExpired()}")
            Text("Can refresh: ${auth.token.hasRefreshToken()}")
        }
        
        Button(onClick = { 
            lifecycleScope.launch {
                authService.restoreSession()
            }
        }) {
            Text("Restore Session")
        }
        
        Button(onClick = {
            val storage = AuthStorage(EduGoStorage.create("auth"))
            storage.clear()
        }) {
            Text("Clear Storage")
        }
    }
}
```

---

## Logs Importantes

### Login Success
```
[Auth] LOGIN_ATTEMPT | user=demo@edugo.com
[AuthRepository] Login attempt 1 for user: demo@edugo.com
[AuthRepository] Login successful for user: demo@edugo.com
[AuthService] Login successful for user: demo@edugo.com
[TokenRefreshManager] Starting automatic token refresh
[Auth] LOGIN_SUCCESS | user=demo@edugo.com | userId=user123
```

### Token Refresh Success
```
[TokenRefreshManager] Scheduling token refresh in 2880 seconds
[TokenRefreshManager] Performing automatic token refresh
[AuthRepository] Token refresh successful
[TokenRefreshManager] Automatic token refresh successful
[AuthService] Token refreshed automatically, updating state
[Auth] TOKEN_REFRESH_SUCCESS | userId=user123
```

### Session Expired
```
[TokenRefreshManager] Automatic token refresh failed: Sesión expirada
[AuthService] Token refresh failed: Sesión expirada
[AuthService] Session expired, clearing data
[Auth] SESSION_EXPIRED | userId=user123 | reason=refresh_failed
```

---

## Validación de Storage

### Script de Validación

```kotlin
suspend fun validateAuthStorage() {
    val storage = AuthStorage(EduGoStorage.create("auth"))
    val logger = Logger.tagged("StorageValidator")
    
    // 1. Verificar lectura/escritura
    logger.info("Testing storage read/write...")
    val testToken = AuthToken.createTestToken()
    storage.saveAuthToken(testToken)
    val retrieved = storage.getAuthToken()
    
    if (retrieved == null) {
        logger.error("❌ Storage not persisting tokens")
        return
    } else {
        logger.info("✅ Storage read/write OK")
    }
    
    // 2. Verificar serialización
    logger.info("Testing serialization...")
    if (retrieved.token != testToken.token) {
        logger.error("❌ Token serialization failed")
        return
    } else {
        logger.info("✅ Serialization OK")
    }
    
    // 3. Verificar clear
    logger.info("Testing clear...")
    storage.clear()
    val afterClear = storage.getAuthToken()
    
    if (afterClear != null) {
        logger.error("❌ Clear not working")
        return
    } else {
        logger.info("✅ Clear OK")
    }
    
    logger.info("✅ All storage validations passed")
}
```

---

## Qué Hacer Si Algo Falla

### Checklist de Diagnóstico

1. **Verificar compilación**
   ```bash
   ./gradlew :modules:auth:build
   ```
   - ✅ Compila sin errores
   - ❌ Hay errores de compilación → Revisar dependencias y sintaxis

2. **Ejecutar tests**
   ```bash
   ./gradlew :modules:auth:test
   ```
   - ✅ Todos los tests pasan
   - ❌ Tests fallan → Ver logs y fix

3. **Verificar logs**
   - Habilitar `Logger.setMinSeverity(Severity.DEBUG)`
   - Buscar errores o warnings
   - Seguir flujo de ejecución

4. **Verificar storage**
   - Usar script de validación
   - Inspeccionar archivos de storage directamente

5. **Verificar red**
   - Usar curl para probar API
   - Capturar tráfico HTTP
   - Verificar timeouts

6. **Revisar configuración**
   - Verificar DI module
   - Verificar AuthConfig
   - Verificar URLs base

7. **Probar manualmente**
   - Login manual
   - Observar estado
   - Verificar storage después de cada operación

---

## Errores de Configuración

### Error: Koin no encuentra AuthService

```
org.koin.core.error.NoBeanDefFoundException: 
No definition found for class AuthService
```

**Solución:**
```kotlin
// En App.onCreate()
startKoin {
    modules(
        authModule,    // ← Falta agregar
        networkModule
    )
}
```

### Error: Circular dependency

```
org.koin.core.error.InstanceCreationException: 
Could not create instance for [AuthService]
```

**Solución:**
```kotlin
// HttpClient debe crearse DESPUÉS de AuthService
val networkModule = module {
    single {
        HttpClientFactory.create().config {
            // get<AuthService>() causa circular dependency
            // Solución: Inyectar después
        }
    }
}

// Mejor: Lazy initialization
val networkModule = module {
    single {
        val client = HttpClientFactory.create()
        client.config {
            // Interceptor se agrega después
        }
        client
    }
}
```

---

## Performance Issues

### Login muy lento (> 5 segundos)

**Causas:**
1. Timeout muy largo
2. Reintentos excesivos
3. Red lenta

**Soluciones:**
```kotlin
// Ajustar timeouts
HttpClientFactory.create(
    connectTimeoutMs = 10_000,  // 10s en vez de 30s
    requestTimeoutMs = 15_000   // 15s en vez de 60s
)

// Ajustar reintentos
RetryPolicy(
    maxAttempts = 2,            // 2 en vez de 3
    initialDelay = 200.milliseconds
)
```

### Token refresh causa lag en UI

**Causa:** Refresh se ejecuta en main thread

**Solución:**
```kotlin
// TokenRefreshManagerImpl debe usar Dispatchers.IO
private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
```

---

## Recursos Adicionales

### Documentación
- [ARQUITECTURA.md](ARQUITECTURA.md) - Diseño del sistema
- [TESTING.md](TESTING.md) - Estrategia de testing
- [FASE-1-MVP.md](FASE-1-MVP.md) - Implementación Fase 1
- [FASE-2-REACTIVO.md](FASE-2-REACTIVO.md) - Implementación Fase 2
- [FASE-3-ROBUSTEZ.md](FASE-3-ROBUSTEZ.md) - Implementación Fase 3

### Herramientas
- **Charles Proxy**: Inspección de tráfico HTTP
- **Android Studio Profiler**: Performance monitoring
- **Xcode Instruments**: iOS debugging
- **Logcat**: Android logging

### Comandos Útiles

```bash
# Ver logs en tiempo real (Android)
adb logcat -s "Auth:*" "AuthService:*" "AuthRepository:*"

# Limpiar datos de app (Android)
adb shell pm clear com.edugo.app

# Ver databases/preferences (Android)
adb shell run-as com.edugo.app ls /data/data/com.edugo.app/shared_prefs/

# Ejecutar tests específicos
./gradlew :modules:auth:test --tests "*AuthServiceImplTest*"

# Generar reporte de cobertura
./gradlew :modules:auth:testDebugUnitTestCoverage
```

---

## Contacto para Soporte

Si después de revisar esta guía el problema persiste:

1. Revisar issues existentes en el repositorio
2. Crear issue con:
   - Descripción del problema
   - Logs relevantes
   - Pasos para reproducir
   - Ambiente (Android/iOS/Desktop, versión)
   - Código relevante

---

**Documento**: Troubleshooting  
**Versión**: 1.0  
**Última actualización**: 2026-02-10
