# Arquitectura del Sistema de Autenticación

## Visión General

El sistema de autenticación de EduGo KMP está diseñado en capas con responsabilidades bien definidas, estado reactivo, y componentes desacoplados. La arquitectura permite escalabilidad, testing, y mantenibilidad a largo plazo.

---

## Diagrama de Capas

```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                              │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐   │
│  │ LoginScreen   │  │ HomeScreen    │  │ SettingsView │   │
│  └───────┬───────┘  └───────┬───────┘  └──────┬───────┘   │
│          │                   │                  │            │
│          └───────────────────┼──────────────────┘            │
└────────────────────────────┼─────────────────────────────────┘
                              │ observes authState
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              AuthService (Coordinator)                │   │
│  │  - StateFlow<AuthState>                              │   │
│  │  - login() / logout() / restoreSession()             │   │
│  │  - Flow<SessionExpired>                              │   │
│  └────┬──────────────┬──────────────┬───────────────────┘   │
│       │              │              │                        │
└───────┼──────────────┼──────────────┼────────────────────────┘
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌─────────────┐ ┌──────────────────┐
│   Storage    │ │ Repository  │ │ TokenRefresh     │
│   Layer      │ │   Layer     │ │   Manager        │
└──────────────┘ └─────────────┘ └──────────────────┘
        │              │              │
        │              ▼              │
        │      ┌──────────────┐      │
        │      │ HTTP Client  │      │
        │      │ + Interceptor│      │
        │      └──────────────┘      │
        │              │              │
        ▼              ▼              ▼
┌─────────────────────────────────────────────┐
│           INFRASTRUCTURE LAYER               │
│  Storage | Network | Coroutines | Platform  │
└─────────────────────────────────────────────┘
```

---

## Diagrama de Flujo de Datos

### Login Flow

```
┌────────┐
│  User  │
└────┬───┘
     │ enters credentials
     ▼
┌──────────────┐
│ LoginScreen  │
└──────┬───────┘
       │ calls login()
       ▼
┌──────────────────────┐
│   AuthService        │
│ 1. Set Loading state │
└──────┬───────────────┘
       │ delegate to repository
       ▼
┌──────────────────┐
│ AuthRepository   │──────► HTTP POST /auth/login
└──────┬───────────┘               │
       │                           │
       │ ◄─────────────────────────┘
       │ LoginResponse{user, token}
       ▼
┌──────────────────────────┐
│   AuthService            │
│ 2. Save to AuthStorage   │
│ 3. Emit Authenticated    │
│ 4. Start token refresh   │
└──────┬───────────────────┘
       │
       ▼
┌────────────────────┐
│ UI observes state  │
│ Navigate to Home   │
└────────────────────┘
```

### Token Refresh Flow

```
┌──────────────────────┐
│ TokenRefreshManager  │
│ scheduleRefresh()    │
└──────┬───────────────┘
       │ delay until threshold
       ▼
┌──────────────────────┐
│ performRefresh()     │
└──────┬───────────────┘
       │ call repository.refreshToken()
       ▼
┌──────────────────┐
│ AuthRepository   │──────► HTTP POST /auth/refresh
└──────┬───────────┘               │
       │                           │
       │ ◄─────────────────────────┘
       │ RefreshResponse{newToken}
       ▼
┌──────────────────────────┐
│ TokenRefreshManager      │
│ emit onRefreshSuccess    │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────┐
│   AuthService            │
│ - Update storage         │
│ - Update state           │
│ - Schedule next refresh  │
└──────────────────────────┘
```

### HTTP Request with Auto Token Injection

```
┌─────────────┐
│ UI/ViewModel│
└─────┬───────┘
      │ makes HTTP request
      ▼
┌──────────────────┐
│   HttpClient     │
└──────┬───────────┘
       │ intercept request
       ▼
┌──────────────────────┐
│  AuthInterceptor     │
│ 1. Check excluded    │
│ 2. Get token         │
│ 3. Add Bearer header │
└──────┬───────────────┘
       │
       ▼
┌──────────────────┐
│  Server Request  │◄───── Authorization: Bearer <token>
└──────────────────┘
```

---

## Diagrama de Módulos

```
┌──────────────────────────────────────────────────────┐
│                  modules/auth                        │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │              service/                       │   │
│  │  - AuthService                              │   │
│  │  - AuthServiceImpl                          │   │
│  │  - AuthServiceFactory                       │   │
│  │  - AuthState                                │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │           repository/                       │   │
│  │  - AuthRepository                           │   │
│  │  - AuthRepositoryImpl                       │   │
│  │  - StubAuthRepository                       │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │             storage/                        │   │
│  │  - AuthStorage                              │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │              token/                         │   │
│  │  - TokenRefreshManager                      │   │
│  │  - TokenRefreshManagerImpl                  │   │
│  │  - TokenRefreshConfig                       │   │
│  │  - RefreshFailureReason                     │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │           interceptor/                      │   │
│  │  - AuthInterceptor                          │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │              model/                         │   │
│  │  - AuthToken                                │   │
│  │  - AuthUserInfo                             │   │
│  │  - LoginCredentials                         │   │
│  │  - LoginResponse                            │   │
│  │  - LoginResult                              │   │
│  │  - LogoutResult                             │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │              retry/ (Fase 3)                │   │
│  │  - RetryPolicy                              │   │
│  │  - withRetry()                              │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │            circuit/ (Fase 3)                │   │
│  │  - CircuitBreaker                           │   │
│  │  - CircuitBreakerConfig                     │   │
│  │  - CircuitState                             │   │
│  └─────────────────────────────────────────────┘   │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │           throttle/ (Fase 3)                │   │
│  │  - RateLimiter                              │   │
│  └─────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
         │                │                │
         ▼                ▼                ▼
┌──────────────┐  ┌─────────────┐  ┌──────────────┐
│modules/storage│  │modules/network│ │modules/foundation│
└──────────────┘  └─────────────┘  └──────────────┘
```

---

## Responsabilidades de Componentes

### AuthService (Coordinator)

**Responsabilidad**: Orquestar todas las operaciones de autenticación y gestionar el estado global.

**Funciones**:
- Coordinar login/logout
- Mantener `StateFlow<AuthState>` reactivo
- Delegar a repository para operaciones HTTP
- Delegar a storage para persistencia
- Delegar a TokenRefreshManager para renovación
- Emitir eventos (`onSessionExpired`, `onLogout`)

**Dependencias**:
- `AuthRepository` - para llamadas HTTP
- `AuthStorage` - para persistencia
- `TokenRefreshManager` - para renovación automática

**No debe**:
- Hacer llamadas HTTP directamente
- Conocer detalles de serialización
- Conocer detalles de platform storage

---

### AuthRepository

**Responsabilidad**: Comunicación con API de autenticación.

**Funciones**:
- `login()` - POST /auth/login
- `refreshToken()` - POST /auth/refresh
- `verifyToken()` - POST /auth/verify
- `logout()` - POST /auth/logout

**Dependencias**:
- `HttpClient` - para requests HTTP
- `RetryPolicy` (Fase 3) - para reintentos
- `CircuitBreaker` (Fase 3) - para protección

**No debe**:
- Guardar datos localmente
- Gestionar estado de UI
- Conocer lógica de renovación automática

---

### AuthStorage

**Responsabilidad**: Persistencia local de tokens y user info.

**Funciones**:
- `saveAuthToken()` / `getAuthToken()`
- `saveUserInfo()` / `getUserInfo()`
- `getRefreshToken()`
- `clear()`
- `hasStoredSession()`

**Dependencias**:
- `EduGoStorage` - wrapper sobre platform storage
- `kotlinx.serialization` - para serialización JSON

**No debe**:
- Hacer llamadas HTTP
- Validar tokens
- Conocer lógica de negocio

---

### TokenRefreshManager

**Responsabilidad**: Renovación automática de tokens.

**Funciones**:
- `refreshToken()` - renovación manual
- `startAutomaticRefresh()` - iniciar renovación automática
- `stopAutomaticRefresh()` - detener renovación
- `scheduleRefresh()` - programar próxima renovación

**Eventos**:
- `onRefreshSuccess` - token renovado exitosamente
- `onRefreshFailure` - renovación falló

**Dependencias**:
- `AuthRepository` - para llamar API de refresh
- `CoroutineScope` - para programar delays

**No debe**:
- Guardar tokens (lo hace AuthService al recibir evento)
- Actualizar UI directamente
- Conocer detalles de storage

---

### AuthInterceptor

**Responsabilidad**: Inyectar tokens automáticamente en requests HTTP.

**Funciones**:
- Interceptar requests HTTP
- Obtener token de AuthService
- Agregar header `Authorization: Bearer <token>`
- Excluir rutas (login, register)

**Dependencias**:
- `AuthService` - para obtener token actual
- `Ktor HttpClient` - para interceptar requests

**No debe**:
- Renovar tokens (lo hace TokenRefreshManager)
- Guardar tokens
- Conocer lógica de negocio

---

## Inyección de Dependencias

### Configuración con Koin

```kotlin
// modules/di/src/commonMain/kotlin/com/edugo/kmp/di/AuthModule.kt

val authModule = module {
    
    // Storage
    single {
        EduGoStorage.create("auth")
    }
    
    // AuthStorage
    single {
        AuthStorage(get())
    }
    
    // AuthRepository
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get(),
            baseUrl = "https://api.edugo.com",
            retryPolicy = RetryPolicy.default(),
            circuitBreaker = CircuitBreaker(
                config = CircuitBreakerConfig.default(),
                name = "AuthRepository"
            )
        )
    }
    
    // TokenRefreshManager
    single<TokenRefreshManager> {
        TokenRefreshManagerImpl(
            repository = get(),
            config = TokenRefreshConfig.default()
        )
    }
    
    // AuthService
    single<AuthService> {
        AuthServiceImpl(
            repository = get(),
            storage = get(),
            tokenRefreshManager = get()
        )
    }
}

val networkModule = module {
    
    // HttpClient con interceptor
    single {
        HttpClientFactory.create().config {
            authInterceptor(
                authService = get(),
                excludedPaths = setOf("/auth/login", "/auth/register", "/auth/refresh")
            )
        }
    }
}
```

**Orden de inicialización**:
1. `EduGoStorage`
2. `AuthStorage`
3. `AuthRepository`
4. `TokenRefreshManager`
5. `AuthService`
6. `HttpClient` (con interceptor que usa AuthService)

---

## Flujo de Autenticación Completo

### 1. Login

```
User enters credentials
    │
    ▼
LoginScreen.login(credentials)
    │
    ▼
AuthService.login(credentials)
    │
    ├──► Set AuthState.Loading
    │
    ├──► AuthRepository.login(credentials)
    │       │
    │       ├──► withRetry() (Fase 3)
    │       │       │
    │       │       └──► CircuitBreaker.execute() (Fase 3)
    │       │               │
    │       │               └──► HTTP POST /auth/login
    │       │                       │
    │       │                       ▼
    │       │                   LoginResponse{user, token}
    │       │
    │       └──► Result.Success(LoginResponse)
    │
    ├──► AuthStorage.saveAuthToken(token)
    ├──► AuthStorage.saveUserInfo(user)
    │
    ├──► Set AuthState.Authenticated(user, token)
    │
    ├──► TokenRefreshManager.startAutomaticRefresh(token)
    │       │
    │       └──► scheduleRefresh()
    │               │
    │               └──► delay(calculatedTime)
    │                       │
    │                       └──► performRefresh() (en background)
    │
    └──► Return LoginResult.Success
            │
            ▼
        UI navigates to Home
```

---

### 2. Restauración de Sesión

```
App starts
    │
    ▼
App.onCreate() / main()
    │
    ▼
AuthService.restoreSession()
    │
    ├──► AuthStorage.hasStoredSession() ?
    │       │
    │       ├─ No ──► Set AuthState.Unauthenticated
    │       │
    │       └─ Yes
    │           │
    │           ├──► AuthStorage.getAuthToken()
    │           ├──► AuthStorage.getUserInfo()
    │           │
    │           ├──► token.isExpired() ?
    │           │       │
    │           │       ├─ No ──► Set AuthState.Authenticated
    │           │       │           │
    │           │       │           └──► TokenRefreshManager.startAutomaticRefresh()
    │           │       │
    │           │       └─ Yes
    │           │           │
    │           │           └──► AuthService.refreshAuthToken()
    │           │                   │
    │           │                   ├──► AuthRepository.refreshToken()
    │           │                   │       │
    │           │                   │       └──► HTTP POST /auth/refresh
    │           │                   │
    │           │                   ├─ Success ──► Set AuthState.Authenticated
    │           │                   │               │
    │           │                   │               └──► startAutomaticRefresh()
    │           │                   │
    │           │                   └─ Failure ──► Clear session
    │           │                                   │
    │           │                                   └──► Set AuthState.Unauthenticated
    │           │
    │           └──► UI reacts to AuthState
```

---

### 3. Renovación Automática de Token

```
TokenRefreshManager.startAutomaticRefresh(token)
    │
    ├──► Calculate refresh delay
    │       │
    │       └──► timeUntilExpiration * threshold%
    │               │
    │               └──► e.g., 3600s * 80% = 2880s
    │
    ├──► Launch coroutine with delay
    │       │
    │       └──► delay(2880s)
    │               │
    │               ▼
    │           performRefresh()
    │               │
    │               ├──► AuthRepository.refreshToken(refreshToken)
    │               │       │
    │               │       └──► HTTP POST /auth/refresh
    │               │               │
    │               │               ▼
    │               │           RefreshResponse{newToken}
    │               │
    │               ├─ Success
    │               │   │
    │               │   ├──► Emit onRefreshSuccess(newToken)
    │               │   │       │
    │               │   │       └──► AuthService observes
    │               │   │               │
    │               │   │               ├──► Save to storage
    │               │   │               └──► Update AuthState
    │               │   │
    │               │   └──► scheduleRefresh(newToken) (loop)
    │               │
    │               └─ Failure
    │                   │
    │                   ├──► Emit onRefreshFailure(reason)
    │                   │       │
    │                   │       └──► AuthService observes
    │                   │               │
    │                   │               ├──► Emit onSessionExpired
    │                   │               ├──► Clear storage
    │                   │               └──► Set AuthState.Unauthenticated
    │                   │
    │                   └──► UI shows session expired dialog
```

---

### 4. Logout

```
User clicks logout
    │
    ▼
AuthService.logout()
    │
    ├──► TokenRefreshManager.stopAutomaticRefresh()
    │       │
    │       └──► Cancel scheduled job
    │
    ├──► Get current token
    │
    ├──► AuthRepository.logout(token)
    │       │
    │       └──► HTTP POST /auth/logout
    │           (best effort, always returns success locally)
    │
    ├──► AuthStorage.clear()
    │       │
    │       └──► Remove all auth data
    │
    ├──► Set AuthState.Unauthenticated
    │
    └──► Return Result.Success
            │
            ▼
        UI navigates to Login
```

---

## Estado Reactivo

### AuthState

```kotlin
sealed class AuthState {
    data class Authenticated(
        val user: AuthUserInfo,
        val token: AuthToken
    ) : AuthState()
    
    object Unauthenticated : AuthState()
    
    object Loading : AuthState()
}
```

### Observación en UI

```kotlin
// En ViewModel o Composable
LaunchedEffect(Unit) {
    authService.authState.collect { state ->
        when (state) {
            is AuthState.Authenticated -> {
                // User logged in
                navigateToHome()
            }
            is AuthState.Unauthenticated -> {
                // User not logged in
                navigateToLogin()
            }
            is AuthState.Loading -> {
                // Show loading
            }
        }
    }
}
```

### Eventos

```kotlin
// Observar sesión expirada
LaunchedEffect(Unit) {
    authService.onSessionExpired.collect {
        showDialog("Tu sesión ha expirado. Por favor inicia sesión nuevamente.")
        navigateToLogin()
    }
}

// Observar logout
LaunchedEffect(Unit) {
    authService.onLogout.collect { result ->
        if (result.success) {
            showSnackbar("Sesión cerrada correctamente")
        }
    }
}
```

---

## Configuración por Ambiente

### Development

```kotlin
AuthConfig.development()
// - Retry: 2 intentos
// - Circuit breaker: 10 fallos threshold
// - Token refresh: 50% threshold
// - Rate limit: 20 requests/min
// - Logs detallados: enabled
```

### Production

```kotlin
AuthConfig.production()
// - Retry: 3 intentos con backoff
// - Circuit breaker: 5 fallos threshold
// - Token refresh: 80% threshold
// - Rate limit: 5 requests/min
// - Logs detallados: disabled
```

### Testing

```kotlin
AuthConfig.testing()
// - Retry: 1 intento
// - Circuit breaker: 3 fallos threshold
// - Token refresh: 90% threshold
// - Rate limit: 100 requests/min
// - Logs detallados: enabled
```

---

## Características de Seguridad

### 1. Tokens Nunca se Loguean Completos

```kotlin
// En AuthToken
fun toLogString(): String {
    val tokenPreview = if (token.length > 10) {
        "${token.take(4)}...${token.takeLast(2)}"
    } else {
        "***"
    }
    return "AuthToken(token=$tokenPreview, ...)"
}
```

### 2. Passwords Nunca se Almacenan

Solo se envían al servidor durante login, nunca se guardan localmente.

### 3. Storage Específico de Plataforma

- **Android**: EncryptedSharedPreferences
- **iOS**: Keychain
- **Desktop**: Encrypted preferences
- **Web**: Secure localStorage

### 4. HTTPS Obligatorio

Todas las comunicaciones usan HTTPS, tokens nunca se envían por HTTP plano.

### 5. Token Expiration

Tokens tienen tiempo de vida limitado y se renuevan automáticamente.

---

## Patrones Utilizados

### 1. Repository Pattern
Abstrae acceso a datos (API)

### 2. Observer Pattern
UI observa cambios de estado reactivamente

### 3. Circuit Breaker Pattern
Previene cascadas de fallos

### 4. Retry Pattern
Reintentos con exponential backoff

### 5. Coordinator Pattern
AuthService coordina múltiples componentes

### 6. Factory Pattern
AuthServiceFactory crea instancias configuradas

---

## Performance

### Operaciones Async

Todas las operaciones de red son `suspend fun`, no bloquean UI thread.

### Caching

- Tokens se cachean en memoria (StateFlow)
- User info se cachea en memoria
- Storage solo se accede en login/logout/restore

### Renovación Proactiva

Tokens se renuevan ANTES de expirar, evitando requests fallidos.

---

## Diagrama de Testing

```
┌──────────────────────────────────────────┐
│            Unit Tests                    │
│                                          │
│  AuthStorageTest                         │
│  AuthRepositoryImplTest                  │
│  AuthServiceImplTest                     │
│  TokenRefreshManagerImplTest             │
│  AuthInterceptorTest                     │
│  RetryPolicyTest (Fase 3)                │
│  CircuitBreakerTest (Fase 3)             │
│  RateLimiterTest (Fase 3)                │
└──────────────────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────┐
│        Integration Tests                 │
│                                          │
│  AuthFlowIntegrationTest                 │
│  - Login → Refresh → Logout              │
│  - Session restore with expired token    │
│  - Automatic refresh triggering          │
└──────────────────────────────────────────┘
```

---

## Extensibilidad Futura

### Autenticación Biométrica

```kotlin
interface BiometricAuthenticator {
    suspend fun authenticate(): Result<Unit>
}

// Usar antes de restoreSession()
```

### Multi-Factor Authentication (MFA)

```kotlin
data class MFAChallenge(
    val method: MFAMethod,
    val code: String
)

suspend fun AuthService.verifyMFA(challenge: MFAChallenge): Result<Unit>
```

### OAuth / Social Login

```kotlin
sealed class AuthProvider {
    object Google : AuthProvider()
    object Facebook : AuthProvider()
    object Apple : AuthProvider()
}

suspend fun AuthService.loginWithProvider(
    provider: AuthProvider
): LoginResult
```

---

## Resumen

La arquitectura del sistema de autenticación de EduGo KMP está diseñada para:

- **Separación de responsabilidades**: Cada componente tiene un propósito claro
- **Reactividad**: UI se actualiza automáticamente con cambios de estado
- **Robustez**: Reintentos, circuit breaker, rate limiting
- **Testabilidad**: Componentes desacoplados y mockables
- **Seguridad**: Tokens encriptados, nunca logueados, HTTPS obligatorio
- **Escalabilidad**: Fácil agregar nuevas features (MFA, OAuth, etc.)
- **Multiplatforma**: Funciona en Android, iOS, Desktop, Web

---

**Documento**: Arquitectura  
**Versión**: 1.0  
**Última actualización**: 2026-02-10
