# Sprint 5 - Análisis Comparativo: Patrones de Persistencia de Sesión

## Resumen Ejecutivo

Este documento compara los patrones de arquitectura para persistencia de sesión encontrados en tres proyectos:
1. **Sprint 5 Planificado** - Lo que originalmente se propuso implementar
2. **Kmp-Common** - Proyecto de referencia interno de EduGo (completo y robusto)
3. **Template-Kmp-Clean** - Template básico de arquitectura limpia KMP

**Conclusión anticipada**: Se recomienda un **enfoque híbrido** que combine la robustez de Kmp-Common con la simplicidad inicial de Template-Kmp-Clean, siguiendo una implementación por fases.

---

## 1. Plan Original (Sprint 5 Documentación)

### Arquitectura Propuesta

```
UI Layer (kmp-screens)
    ↓ observa StateFlow<SessionState>
SessionManager (NUEVO módulo kmp-session)
    ↓ usa
AuthService (módulo auth existente)
    ↓ usa
Storage (módulo storage existente)
```

### Componentes Clave

#### SessionState (Sealed Class)
```kotlin
sealed class SessionState {
    data object Unknown : SessionState()
    data object Loading : SessionState()
    data class LoggedIn(val user: User) : SessionState()
    data object LoggedOut : SessionState()
    data object Expired : SessionState()
    data class Error(val error: AppError) : SessionState()
}
```

#### SessionManager (Interface)
```kotlin
interface SessionManager {
    val sessionState: StateFlow<SessionState>
    suspend fun checkSession(): Result<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): User?
}
```

#### AuthService (Extensión)
```kotlin
// Nuevo método a añadir
suspend fun restoreSession(): Result<User?> {
    // 1. Leer tokens de storage
    // 2. Validar access token
    // 3. Intentar refresh si expirado
    // 4. Retornar User o null
}
```

### Fortalezas

- **Separación clara de capas**: SessionManager coordina, AuthService tiene lógica de negocio
- **Estado reactivo**: StateFlow permite UI reactiva
- **Extensible**: Fácil añadir estados futuros (Biometric, SSO, etc.)
- **Documentación detallada**: 3 documentos completos con ejemplos

### Debilidades

- **Duplicación de responsabilidades**: SessionManager y AuthService tienen overlap
- **No hay persistencia automática de estado**: StateFlow se pierde al cerrar app
- **Sin validación de keys de storage**: Podría guardar datos con keys inválidas
- **Falta secure storage**: Solo mencionado como "Fase 5 opcional"
- **No hay mecanismo de eventos**: onSessionExpired está en AuthService, no en SessionManager

---

## 2. Kmp-Common (Proyecto de Referencia)

### Arquitectura Implementada

```
UI Layer
    ↓ observa StateFlow<AuthState>
AuthService (interface completa)
    ├─ authState: StateFlow<AuthState>
    ├─ tokenRefreshManager: TokenRefreshManager
    ├─ onSessionExpired: Flow<Unit>
    └─ onLogout: Flow<LogoutResult>
    ↓ usa
AuthRepository (abstracción de datos)
    ↓ usa
Storage (EduGoStorage + SafeEduGoStorage + StateFlowStorage)
```

### Componentes Clave

#### AuthState (Sealed Class)
```kotlin
sealed class AuthState {
    data class Authenticated(
        val user: AuthUserInfo,
        val token: AuthToken
    ) : AuthState() {
        fun isTokenExpired(): Boolean
        fun canRefresh(): Boolean
    }
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
}

// Extension properties poderosas
val AuthState.isAuthenticated: Boolean
val AuthState.currentUser: AuthUserInfo?
val AuthState.currentToken: AuthToken?

// Funciones de orden superior
inline fun <R> AuthState.fold(
    onAuthenticated: (AuthUserInfo, AuthToken) -> R,
    onUnauthenticated: () -> R,
    onLoading: () -> R
): R
```

#### AuthService (Interface Completa)
```kotlin
interface AuthService : TokenProvider {
    // Estado reactivo
    val authState: StateFlow<AuthState>
    
    // Eventos observables
    val onSessionExpired: Flow<Unit>
    val onLogout: Flow<LogoutResult>
    
    // Managers especializados
    val tokenRefreshManager: TokenRefreshManager
    
    // Operaciones principales
    suspend fun login(credentials: LoginCredentials): LoginResult
    suspend fun logout(): Result<Unit>
    suspend fun logoutWithDetails(forceLocal: Boolean = true): LogoutResult
    suspend fun refreshAuthToken(): Result<AuthToken>
    
    // Queries síncronas
    fun isAuthenticated(): Boolean
    fun getCurrentUser(): AuthUserInfo?
    fun getCurrentAuthToken(): AuthToken?
    
    // Persistencia
    suspend fun restoreSession()
    
    // TokenProvider implementation
    override suspend fun getToken(): String?
    override suspend fun isTokenExpired(): Boolean
}
```

#### Storage (3 capas)

**1. EduGoStorage (Base)**
```kotlin
// Operaciones síncronas básicas
class EduGoStorage {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun getInt/getLong/getBoolean/getFloat/getDouble(...)
    fun remove(key: String)
    fun clear()
    fun has(key: String): Boolean
}
```

**2. SafeEduGoStorage (Wrapper con validación)**
```kotlin
class SafeEduGoStorage(
    val storage: EduGoStorage,
    val validateKeys: Boolean = true,
    val logger: TaggedLogger
) {
    fun putStringSafe(key: String, value: String): Result<Unit>
    fun getStringSafe(key: String, default: String): String
    // Valida keys antes de operaciones
    // Nunca lanza excepciones
    // Logs de errores
}
```

**3. StateFlowStorage (Storage reactivo)**
```kotlin
class StateFlowStorage(
    val scope: CoroutineScope,
    val storage: EduGoStorage
) {
    fun stateFlowString(key: String, default: String): StateFlow<String>
    suspend fun putString(key: String, value: String)
    // Cache in-memory + StateFlow
    // Ideal para preferencias observables desde UI
}
```

### Fortalezas

- **Arquitectura madura y probada**: 171 tests en auth module
- **Eventos observables**: onSessionExpired, onLogout como Flows separados
- **Storage robusto**: 3 niveles (básico, seguro, reactivo) con 231 tests
- **TokenRefreshManager independiente**: Maneja refresh con Mutex, backoff, retry
- **Extension functions**: AuthState tiene helpers poderosos (fold, ifAuthenticated, etc.)
- **Documentación exhaustiva**: KDoc detallada con ejemplos completos
- **Separación Repository/Service**: Repository para datos, Service para lógica
- **logoutWithDetails**: Logout con información granular (Success, PartialSuccess, AlreadyLoggedOut)
- **Validación de keys**: SafeEduGoStorage previene keys inválidas

### Debilidades

- **Complejidad inicial**: Muchos componentes (AuthService, AuthRepository, TokenRefreshManager, etc.)
- **Requiere más setup**: 3 capas de storage pueden ser overkill para MVP
- **No tiene SessionManager separado**: AuthService lo hace todo
- **Curva de aprendizaje**: API muy completa puede intimidar a desarrolladores nuevos

### Patrones Únicos de Kmp-Common

1. **Flow de eventos en lugar de callbacks**: `onSessionExpired: Flow<Unit>` en vez de listener
2. **Extension properties sobre sealed class**: `authState.currentUser`, `authState.isAuthenticated`
3. **Pattern matching con fold**: `authState.fold(onAuthenticated = {...}, ...)`
4. **Logout granular**: `logoutWithDetails(forceLocal: Boolean)` con `LogoutResult` detallado
5. **Storage con validación**: `SafeEduGoStorage` valida keys y nunca lanza excepciones
6. **Storage reactivo**: `StateFlowStorage` para preferencias observables

---

## 3. Template-Kmp-Clean (Template Básico)

### Arquitectura Implementada

```
UI Layer (LoginViewModel)
    ↓ usa
LoginUseCase (domain)
    ↓ usa
AuthRepository (domain interface)
    ↑ implementa
AuthRepositoryImpl (data)
    ↓ usa
SessionManager (simple token in-memory)
```

### Componentes Clave

#### SessionManager (Básico)
```kotlin
class SessionManager {
    private var authToken: String? = null
    
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun hasToken(): Boolean
}
```

#### AuthRepository (Clean Architecture)
```kotlin
// Domain layer (interface)
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun isUserAuthenticated(): Boolean
}

// Data layer (implementation mock)
class AuthRepositoryImpl : AuthRepository {
    private var currentUser: User? = null
    
    override suspend fun login(email: String, password: String): Result<User> {
        // Mock implementation con delay(500)
        // Validación básica de email/password
    }
}
```

#### LoginViewModel
```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)
            loginUseCase(email, password).fold(
                onSuccess = { user ->
                    _state.value = LoginState(user = user)
                },
                onFailure = { error ->
                    _state.value = LoginState(error = error)
                }
            )
        }
    }
}
```

### Fortalezas

- **Simplicidad extrema**: Fácil de entender para principiantes
- **Clean Architecture pura**: Domain/Data/Presentation bien separados
- **UseCase pattern**: LoginUseCase encapsula lógica de negocio
- **ViewModel MVVM**: Estado reactivo con StateFlow
- **Sin dependencias extras**: No requiere librerías adicionales
- **Mockeable**: AuthRepositoryImpl es fácil de reemplazar con implementación real

### Debilidades

- **Sin persistencia real**: SessionManager es solo in-memory (TODO en comentarios)
- **Sin estado global**: Cada ViewModel gestiona su estado independientemente
- **Sin manejo de expiración**: No hay refresh de tokens
- **Sin eventos de sesión**: No hay onSessionExpired o similares
- **Mock básico**: AuthRepositoryImpl simula con delay, no hay backend real
- **Sin secure storage**: Tokens en memoria plana
- **Sin validación robusta**: Validación mínima de email/password

### Patrones Únicos de Template-Kmp-Clean

1. **UseCase pattern**: `LoginUseCase` como interactor entre ViewModel y Repository
2. **Repository en domain**: Interface en domain, implementación en data
3. **ViewModel con State único**: `LoginState` data class con todos los campos
4. **Fold sobre Result**: `result.fold(onSuccess = {...}, onFailure = {...})`

---

## 4. Comparación Lado a Lado

### 4.1. Gestión de Estado

| Aspecto | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|---------|----------------------|------------|---------------------|
| **Sealed Class** | `SessionState` (6 estados) | `AuthState` (3 estados) | `LoginState` (data class) |
| **Estados** | Unknown, Loading, LoggedIn, LoggedOut, Expired, Error | Authenticated, Unauthenticated, Loading | isLoading, user, error |
| **Reactividad** | `StateFlow<SessionState>` | `StateFlow<AuthState>` | `StateFlow<LoginState>` |
| **Extension helpers** | No | Sí (15+) | No |
| **Pattern matching** | No | `fold()`, `ifAuthenticated()` | `fold()` en Result |
| **Persistencia de estado** | No especificado | StateFlowStorage | No |

**Ganador**: **Kmp-Common** - AuthState con extension properties es el más poderoso y usable.

### 4.2. Arquitectura de Capas

| Capa | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|------|----------------------|------------|---------------------|
| **Presentation** | SessionManager observa estado | UI observa AuthService | ViewModel con UseCase |
| **Domain** | AuthService (lógica) | AuthService + UseCases implícitos | UseCase + Repository interface |
| **Data** | Storage directo | AuthRepository + Storage | AuthRepositoryImpl |
| **Coordinación** | SessionManager (nuevo módulo) | AuthService lo hace todo | ViewModel + UseCase |

**Ganador**: **Template-Kmp-Clean** - Separación Clean Architecture más pura (Domain/Data/Presentation).

### 4.3. Storage

| Aspecto | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|---------|----------------------|------------|---------------------|
| **Capa básica** | EduGoStorage existente | EduGoStorage | SessionManager in-memory |
| **Validación de keys** | No | SafeEduGoStorage | No |
| **Storage reactivo** | No | StateFlowStorage | No |
| **Serialización JSON** | Sí (existente) | Sí (StorageSerializationExtensions) | No |
| **Error handling** | Result<T> | Result<T> + logs automáticos | Result<T> |
| **Secure storage** | Fase 5 opcional | No (mencionado en TODOs) | No (TODO en comentarios) |

**Ganador**: **Kmp-Common** - 3 capas de storage con validación y reactividad.

### 4.4. Manejo de Sesión

| Aspecto | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|---------|----------------------|------------|---------------------|
| **Login** | `SessionManager.login()` | `AuthService.login()` | `LoginUseCase()` |
| **Logout** | `SessionManager.logout()` | `AuthService.logout()` + `logoutWithDetails()` | `AuthRepository.logout()` |
| **Restore session** | `AuthService.restoreSession()` | `AuthService.restoreSession()` | No |
| **Token refresh** | TokenRefreshManager existente | TokenRefreshManager integrado | No |
| **Session expiration** | `SessionState.Expired` | `onSessionExpired: Flow<Unit>` | No |
| **Logout events** | No | `onLogout: Flow<LogoutResult>` | No |

**Ganador**: **Kmp-Common** - Manejo completo de sesión con eventos y refresh automático.

### 4.5. Eventos y Observabilidad

| Aspecto | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|---------|----------------------|------------|---------------------|
| **Estado reactivo** | `StateFlow<SessionState>` | `StateFlow<AuthState>` | `StateFlow<LoginState>` |
| **Evento de expiración** | Emitir `SessionState.Expired` | `onSessionExpired: Flow<Unit>` | No |
| **Evento de logout** | No | `onLogout: Flow<LogoutResult>` | No |
| **Eventos de refresh** | No | `TokenRefreshManager.onRefreshFailed` | No |
| **Observación de storage** | No | StateFlowStorage | No |

**Ganador**: **Kmp-Common** - Sistema completo de eventos con Flows separados.

### 4.6. Testing

| Aspecto | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|---------|----------------------|------------|---------------------|
| **Tests existentes** | 1694 (previo) | 171 en auth, 231 en storage | LoginViewModelTest básico |
| **Tests planificados** | +100 nuevos | Ya implementados | Mocks manuales |
| **Coverage objetivo** | >80% | Ya alcanzado | No especificado |
| **Mocks disponibles** | No especificado | StubAuthRepository | AuthRepositoryImpl mock |

**Ganador**: **Kmp-Common** - Suite de tests completa y robusta.

### 4.7. Complejidad

| Aspecto | Sprint 5 Planificado | Kmp-Common | Template-Kmp-Clean |
|---------|----------------------|------------|---------------------|
| **Número de clases** | ~8 nuevas | ~20 (completo) | ~6 (básico) |
| **Líneas de código (estimado)** | ~800-1000 | ~2000+ | ~300-400 |
| **Curva de aprendizaje** | Media | Alta | Baja |
| **Tiempo de implementación** | 8-13 días | Ya implementado | 2-3 días (MVP) |
| **Mantenibilidad** | Alta | Muy alta | Media |

**Ganador**: **Template-Kmp-Clean** - Complejidad mínima para MVP.

---

## 5. Mejores Patrones Identificados

### De Kmp-Common

#### 1. Extension Properties sobre Sealed Classes
```kotlin
// En vez de:
if (sessionState is SessionState.LoggedIn) {
    val user = (sessionState as SessionState.LoggedIn).user
}

// Usar:
val user = authState.currentUser  // null-safe
if (authState.isAuthenticated) { ... }
```

**Ventaja**: Código más limpio y type-safe.

#### 2. Flow de Eventos Separados
```kotlin
// En vez de callback o listener
interface SessionListener {
    fun onSessionExpired()
    fun onLogout()
}

// Usar Flows
val onSessionExpired: Flow<Unit>
val onLogout: Flow<LogoutResult>

// En UI:
LaunchedEffect(Unit) {
    authService.onSessionExpired.collect {
        navigateToLogin()
    }
}
```

**Ventaja**: Reactivo, composable, fácil de testear.

#### 3. Pattern Matching con fold()
```kotlin
val message = authState.fold(
    onAuthenticated = { user, token -> "Welcome ${user.fullName}" },
    onUnauthenticated = { "Please login" },
    onLoading = { "Loading..." }
)
```

**Ventaja**: Exhaustivo, funcional, evita when expressions largas.

#### 4. Storage con Validación
```kotlin
// SafeEduGoStorage valida keys automáticamente
val result = safeStorage.putStringSafe("invalid key!", "value")
// Returns Result.Failure sin lanzar excepción
```

**Ventaja**: Previene bugs sutiles, logs automáticos, nunca crashea.

#### 5. StateFlowStorage para Preferencias
```kotlin
class UserPrefs(scope: CoroutineScope, storage: EduGoStorage) {
    private val stateStorage = StateFlowStorage(scope, storage)
    
    val userName: StateFlow<String> = stateStorage.stateFlowString("user.name", "Guest")
    
    suspend fun setUserName(name: String) {
        stateStorage.putString("user.name", name)
    }
}

// En Compose:
val userName by prefs.userName.collectAsState()
```

**Ventaja**: Preferencias reactivas sin boilerplate.

#### 6. Logout Granular
```kotlin
val result = authService.logoutWithDetails(forceLocal = true)
when (result) {
    is LogoutResult.Success -> "Logout completo"
    is LogoutResult.PartialSuccess -> "Sin conexión, logout local"
    is LogoutResult.AlreadyLoggedOut -> "Ya estaba deslogueado"
}
```

**Ventaja**: UX mejorada, soporte offline, idempotencia.

### De Template-Kmp-Clean

#### 1. UseCase Pattern
```kotlin
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(email, password)
    }
}

// En ViewModel:
fun login(email: String, password: String) {
    viewModelScope.launch {
        loginUseCase(email, password).fold(
            onSuccess = { user -> _state.value = LoginState(user = user) },
            onFailure = { error -> _state.value = LoginState(error = error) }
        )
    }
}
```

**Ventaja**: Lógica de negocio encapsulada, testeable, reutilizable.

#### 2. Repository en Domain Layer
```kotlin
// features/auth/domain/repositories/AuthRepository.kt (interface)
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
}

// features/auth/data/repositories/AuthRepositoryImpl.kt (implementation)
class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(...): Result<User> { ... }
}
```

**Ventaja**: Clean Architecture pura, inversión de dependencias.

#### 3. State como Data Class
```kotlin
data class LoginState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: Throwable? = null
)

// En ViewModel:
private val _state = MutableStateFlow(LoginState())
val state: StateFlow<LoginState> = _state.asStateFlow()
```

**Ventaja**: Inmutable, fácil de testear, todos los campos en un solo lugar.

---

## 6. Anti-Patrones a Evitar

### 1. Duplicación de Estado (Sprint 5 Planificado)
```kotlin
// EVITAR:
class SessionManager {
    val sessionState: StateFlow<SessionState>
}

class AuthService {
    val authState: StateFlow<AuthState>
}

// Dos fuentes de verdad para el mismo concepto
```

**Problema**: Sincronización manual, bugs sutiles, confusión.

### 2. Storage sin Validación (Sprint 5 + Template-Kmp-Clean)
```kotlin
// EVITAR:
storage.putString("invalid key!", "value")  // No falla, guarda datos corruptos
```

**Problema**: Keys inválidas pueden causar crashes en iOS/Wasm.

### 3. Logout sin Soporte Offline (Sprint 5 Planificado)
```kotlin
// EVITAR:
suspend fun logout(): Result<Unit> {
    val result = api.logout()  // Falla sin internet
    if (result.isSuccess) {
        storage.clear()  // Usuario queda "logueado" si no hay internet
    }
}
```

**Problema**: Usuario no puede cerrar sesión sin conexión.

### 4. SessionManager Solo en Memoria (Template-Kmp-Clean)
```kotlin
// EVITAR:
class SessionManager {
    private var authToken: String? = null  // Se pierde al cerrar app
}
```

**Problema**: Sin persistencia real, no cumple el objetivo del sprint.

---

## 7. Recomendación Final: Enfoque Híbrido

### Estrategia Propuesta: 3 Fases

#### FASE 1: MVP Simple (Semana 1) - Base de Template-Kmp-Clean

**Objetivo**: Persistencia básica funcional rápidamente.

**Arquitectura**:
```
LoginViewModel
    ↓ usa
LoginUseCase
    ↓ usa
AuthRepository (interface)
    ↑ implementa
AuthRepositoryImpl
    ↓ usa
SessionManager (MEJORADO: guarda en EduGoStorage)
```

**Cambios respecto a Template-Kmp-Clean**:
1. **SessionManager con persistencia real**:
```kotlin
class SessionManager(private val storage: EduGoStorage) {
    fun saveToken(token: String) {
        storage.putString(KEY_TOKEN, token)
    }
    
    fun getToken(): String? {
        return storage.getString(KEY_TOKEN, "").takeIf { it.isNotEmpty() }
    }
    
    fun clearToken() {
        storage.remove(KEY_TOKEN)
    }
}
```

2. **AuthRepository con restoreSession()**:
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun restoreSession(): Result<User?>  // NUEVO
}
```

3. **SplashScreen con verificación**:
```kotlin
@Composable
fun SplashScreen(authRepository: AuthRepository, navigator: Navigator) {
    LaunchedEffect(Unit) {
        val user = authRepository.restoreSession().getOrNull()
        if (user != null) {
            navigator.navigate(Screen.Home)
        } else {
            navigator.navigate(Screen.Login)
        }
    }
}
```

**Entregables**:
- SessionManager con persistencia real
- AuthRepository.restoreSession()
- SplashScreen verifica sesión
- Tests básicos

**Tiempo**: 3-5 días  
**Tests**: ~30 nuevos

---

#### FASE 2: Estado Reactivo (Semana 2) - Inspirado en Kmp-Common

**Objetivo**: Añadir estado global reactivo y manejo de expiración.

**Añadir**:
1. **AuthState sealed class** (de Kmp-Common):
```kotlin
sealed class AuthState {
    data class Authenticated(val user: User, val token: AuthToken) : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
}

// Extension properties
val AuthState.isAuthenticated: Boolean
val AuthState.currentUser: User?
```

2. **AuthService con StateFlow** (nuevo módulo):
```kotlin
interface AuthService {
    val authState: StateFlow<AuthState>
    val onSessionExpired: Flow<Unit>  // NUEVO (de Kmp-Common)
    
    suspend fun login(credentials: LoginCredentials): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun restoreSession()
}
```

3. **UI observa AuthService**:
```kotlin
@Composable
fun App(authService: AuthService) {
    val authState by authService.authState.collectAsState()
    
    // Navegar según estado
    when (authState) {
        is AuthState.Authenticated -> HomeScreen()
        is AuthState.Unauthenticated -> LoginScreen()
        is AuthState.Loading -> LoadingScreen()
    }
    
    // Manejar expiración
    LaunchedEffect(Unit) {
        authService.onSessionExpired.collect {
            showSnackbar("Sesión expirada")
            navigateToLogin()
        }
    }
}
```

**Entregables**:
- AuthState con extension properties
- AuthService con StateFlow + Flows de eventos
- UI reactiva a cambios de estado
- Manejo de expiración

**Tiempo**: 3-5 días  
**Tests**: ~40 nuevos

---

#### FASE 3: Robustez Avanzada (Semana 3) - Patrones de Kmp-Common

**Objetivo**: Añadir features avanzadas de producción.

**Añadir**:
1. **SafeEduGoStorage** (de Kmp-Common):
```kotlin
class SafeEduGoStorage(
    private val storage: EduGoStorage,
    private val logger: Logger
) {
    fun putStringSafe(key: String, value: String): Result<Unit> {
        if (!StorageKeyValidator.isValid(key)) {
            logger.w("Invalid key: $key")
            return Result.Failure("Invalid key")
        }
        return try {
            storage.putString(key, value)
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.e("Storage error", e)
            Result.Failure(e)
        }
    }
}
```

2. **logoutWithDetails** (de Kmp-Common):
```kotlin
sealed class LogoutResult {
    object Success : LogoutResult()
    data class PartialSuccess(val remoteError: String) : LogoutResult()
    object AlreadyLoggedOut : LogoutResult()
}

suspend fun logoutWithDetails(forceLocal: Boolean = true): LogoutResult {
    if (authState.value is AuthState.Unauthenticated) {
        return LogoutResult.AlreadyLoggedOut
    }
    
    val remoteResult = api.logout()
    
    if (forceLocal || remoteResult.isSuccess) {
        storage.clear()
        _authState.value = AuthState.Unauthenticated
    }
    
    return when {
        remoteResult.isSuccess -> LogoutResult.Success
        forceLocal -> LogoutResult.PartialSuccess(remoteResult.error)
        else -> LogoutResult.PartialSuccess("Offline")
    }
}
```

3. **StateFlowStorage** (de Kmp-Common) - Opcional:
```kotlin
class StateFlowStorage(
    private val scope: CoroutineScope,
    private val storage: EduGoStorage
) {
    fun stateFlowString(key: String, default: String): StateFlow<String>
    suspend fun putString(key: String, value: String)
}

// Uso para preferencias:
class UserPrefs(storage: StateFlowStorage) {
    val theme: StateFlow<String> = storage.stateFlowString("ui.theme", "light")
    val language: StateFlow<String> = storage.stateFlowString("ui.language", "es")
}
```

4. **Pattern matching** (de Kmp-Common):
```kotlin
inline fun <R> AuthState.fold(
    onAuthenticated: (User, AuthToken) -> R,
    onUnauthenticated: () -> R,
    onLoading: () -> R
): R
```

**Entregables**:
- SafeEduGoStorage con validación
- Logout granular con soporte offline
- StateFlowStorage para preferencias (opcional)
- Extension functions avanzadas

**Tiempo**: 4-6 días  
**Tests**: ~40 nuevos

---

### Resumen de Fases

| Fase | Tiempo | Tests | Componentes Clave |
|------|--------|-------|-------------------|
| **Fase 1: MVP** | 3-5 días | ~30 | SessionManager + restoreSession |
| **Fase 2: Reactivo** | 3-5 días | ~40 | AuthState + StateFlow + eventos |
| **Fase 3: Robustez** | 4-6 días | ~40 | SafeStorage + logoutWithDetails |
| **TOTAL** | **10-16 días** | **~110** | Sistema completo |

---

## 8. Arquitectura Final Recomendada

### Diagrama de Capas

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  • App.kt - Observa authState y eventos                 │
│  • SplashScreen - Verifica sesión                       │
│  • LoginScreen - Login con ViewModel                    │
│  • SettingsScreen - Logout                              │
└────────────────────┬────────────────────────────────────┘
                     │ observes StateFlow<AuthState>
                     │ collects Flow<Unit> (eventos)
┌────────────────────▼────────────────────────────────────┐
│              Presentation Layer (ViewModels)             │
│  • LoginViewModel - Llama LoginUseCase                   │
│  • HomeViewModel - Observa authState.currentUser         │
└────────────────────┬────────────────────────────────────┘
                     │ usa
┌────────────────────▼────────────────────────────────────┐
│                 Domain Layer (UseCases)                  │
│  • LoginUseCase(authRepository)                          │
│  • LogoutUseCase(authService)                            │
│  • RestoreSessionUseCase(authService)                    │
└────────────────────┬────────────────────────────────────┘
                     │ usa
┌────────────────────▼────────────────────────────────────┐
│            Application Layer (AuthService)               │
│  • AuthService: Estado global y coordinación             │
│    - authState: StateFlow<AuthState>                     │
│    - onSessionExpired: Flow<Unit>                        │
│    - login/logout/restoreSession                         │
└────────────────────┬────────────────────────────────────┘
                     │ usa
┌────────────────────▼────────────────────────────────────┐
│              Data Layer (Repository)                     │
│  • AuthRepository: Interface en domain                   │
│  • AuthRepositoryImpl: Implementación en data            │
└────────────────────┬────────────────────────────────────┘
                     │ usa
┌────────────────────▼────────────────────────────────────┐
│           Infrastructure Layer (Storage)                 │
│  • EduGoStorage - Storage básico                         │
│  • SafeEduGoStorage - Storage con validación (Fase 3)    │
│  • StateFlowStorage - Storage reactivo (Fase 3 opcional) │
└──────────────────────────────────────────────────────────┘
```

### Módulos y Responsabilidades

```
kmp_new/
├── modules/
│   ├── auth/                    # Ya existe
│   │   └── AuthService          # MODIFICAR: Añadir restoreSession()
│   ├── storage/                 # Ya existe
│   │   ├── EduGoStorage         # USAR: Ya funcional
│   │   ├── SafeEduGoStorage     # FASE 3: Añadir validación
│   │   └── StateFlowStorage     # FASE 3: Opcional
│   └── di/                      # Ya existe
│       └── sessionModule        # FASE 2: Añadir DI de AuthService
├── features/
│   └── auth/                    # NUEVO (inspirado en Template-Kmp-Clean)
│       ├── domain/
│       │   ├── models/
│       │   │   ├── User.kt
│       │   │   ├── AuthState.kt         # FASE 2
│       │   │   └── LogoutResult.kt      # FASE 3
│       │   ├── repositories/
│       │   │   └── AuthRepository.kt    # FASE 1
│       │   └── usecases/
│       │       ├── LoginUseCase.kt      # FASE 1
│       │       ├── LogoutUseCase.kt     # FASE 2
│       │       └── RestoreSessionUseCase.kt  # FASE 1
│       ├── data/
│       │   ├── repositories/
│       │   │   └── AuthRepositoryImpl.kt  # FASE 1
│       │   └── services/
│       │       └── SessionManager.kt      # FASE 1 (mejorado)
│       └── presentation/
│           └── viewmodels/
│               └── LoginViewModel.kt    # FASE 1
└── kmp-screens/                # Ya existe
    ├── splash/SplashScreen.kt  # FASE 1: Verificar sesión
    ├── login/LoginScreen.kt    # FASE 1: Integrar ViewModel
    ├── home/HomeScreen.kt      # FASE 2: Mostrar currentUser
    └── settings/SettingsScreen.kt  # FASE 2: Logout
```

---

## 9. Código de Ejemplo: Implementación Híbrida

### Fase 1: MVP

#### SessionManager.kt (Mejorado)
```kotlin
package com.edugo.kmp.auth.data.services

import com.edugo.kmp.storage.EduGoStorage

class SessionManager(private val storage: EduGoStorage) {
    
    fun saveToken(token: String) {
        storage.putString(KEY_ACCESS_TOKEN, token)
    }
    
    fun getToken(): String? {
        return storage.getString(KEY_ACCESS_TOKEN, "").takeIf { it.isNotEmpty() }
    }
    
    fun clearToken() {
        storage.remove(KEY_ACCESS_TOKEN)
        storage.remove(KEY_USER_DATA)
    }
    
    fun hasToken(): Boolean = getToken() != null
    
    fun saveUser(user: User) {
        val json = Json.encodeToString(user)
        storage.putString(KEY_USER_DATA, json)
    }
    
    fun getUser(): User? {
        val json = storage.getString(KEY_USER_DATA, "")
        return if (json.isNotEmpty()) {
            try {
                Json.decodeFromString<User>(json)
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "auth.token"
        private const val KEY_USER_DATA = "auth.user"
    }
}
```

#### AuthRepository.kt
```kotlin
package com.edugo.kmp.auth.domain.repositories

import com.edugo.kmp.auth.domain.models.User
import com.edugo.kmp.foundation.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun restoreSession(): Result<User?>  // NUEVO
}
```

#### AuthRepositoryImpl.kt
```kotlin
package com.edugo.kmp.auth.data.repositories

import com.edugo.kmp.auth.data.services.SessionManager
import com.edugo.kmp.auth.domain.models.User
import com.edugo.kmp.auth.domain.repositories.AuthRepository
import com.edugo.kmp.foundation.Result
import com.edugo.kmp.network.EduGoHttpClient

class AuthRepositoryImpl(
    private val httpClient: EduGoHttpClient,
    private val sessionManager: SessionManager
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = httpClient.post<LoginResponse>("/auth/login") {
                setBody(LoginRequest(email, password))
            }
            
            // Guardar token y usuario
            sessionManager.saveToken(response.token)
            sessionManager.saveUser(response.user)
            
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // Intentar logout remoto (ignora errores)
            try {
                httpClient.post<Unit>("/auth/logout")
            } catch (e: Exception) {
                // Ignorar error de backend
            }
            
            // Limpiar local SIEMPRE
            sessionManager.clearToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        return sessionManager.getUser()
    }
    
    override suspend fun restoreSession(): Result<User?> {
        return try {
            val token = sessionManager.getToken()
            val user = sessionManager.getUser()
            
            if (token != null && user != null) {
                // TODO: Validar token (Fase 2)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### SplashScreen.kt
```kotlin
@Composable
fun SplashScreen(
    navigationState: NavigationState,
    authRepository: AuthRepository = koinInject()
) {
    var isCheckingSession by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(1000)  // Mínimo delay para mostrar logo
        
        val user = authRepository.restoreSession().getOrNull()
        
        if (user != null) {
            navigationState.navigate(Routes.Home)
        } else {
            navigationState.navigate(Routes.Login)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isCheckingSession) {
            CircularProgressIndicator()
        }
        // Logo, etc.
    }
}
```

### Fase 2: Estado Reactivo

#### AuthState.kt
```kotlin
package com.edugo.kmp.auth.domain.models

import kotlinx.serialization.Serializable

sealed class AuthState {
    data class Authenticated(
        val user: User,
        val token: String
    ) : AuthState()
    
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
}

// Extension properties (de Kmp-Common)
val AuthState.isAuthenticated: Boolean
    get() = this is AuthState.Authenticated

val AuthState.currentUser: User?
    get() = (this as? AuthState.Authenticated)?.user

val AuthState.currentToken: String?
    get() = (this as? AuthState.Authenticated)?.token

// Pattern matching (de Kmp-Common)
inline fun <R> AuthState.fold(
    onAuthenticated: (User, String) -> R,
    onUnauthenticated: () -> R,
    onLoading: () -> R
): R = when (this) {
    is AuthState.Authenticated -> onAuthenticated(user, token)
    is AuthState.Unauthenticated -> onUnauthenticated()
    is AuthState.Loading -> onLoading()
}
```

#### AuthService.kt (NUEVO en módulo auth)
```kotlin
package com.edugo.kmp.auth

import com.edugo.kmp.auth.domain.models.AuthState
import com.edugo.kmp.auth.domain.models.User
import com.edugo.kmp.auth.domain.repositories.AuthRepository
import com.edugo.kmp.foundation.Result
import kotlinx.coroutines.flow.*

interface AuthService {
    val authState: StateFlow<AuthState>
    val onSessionExpired: Flow<Unit>
    
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun restoreSession()
}

class AuthServiceImpl(
    private val authRepository: AuthRepository
) : AuthService {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _onSessionExpired = MutableSharedFlow<Unit>()
    override val onSessionExpired: Flow<Unit> = _onSessionExpired.asSharedFlow()
    
    override suspend fun login(email: String, password: String): Result<User> {
        _authState.value = AuthState.Loading
        
        val result = authRepository.login(email, password)
        
        _authState.value = if (result.isSuccess) {
            val user = result.getOrNull()!!
            val token = authRepository.getCurrentToken() ?: ""
            AuthState.Authenticated(user, token)
        } else {
            AuthState.Unauthenticated
        }
        
        return result
    }
    
    override suspend fun logout(): Result<Unit> {
        val result = authRepository.logout()
        _authState.value = AuthState.Unauthenticated
        return result
    }
    
    override suspend fun restoreSession() {
        _authState.value = AuthState.Loading
        
        val result = authRepository.restoreSession()
        
        _authState.value = when {
            result.isSuccess && result.getOrNull() != null -> {
                val user = result.getOrNull()!!
                val token = authRepository.getCurrentToken() ?: ""
                AuthState.Authenticated(user, token)
            }
            else -> AuthState.Unauthenticated
        }
    }
}
```

#### App.kt (UI reactiva)
```kotlin
@Composable
fun App(
    authService: AuthService = koinInject()
) {
    val authState by authService.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Manejar expiración de sesión
    LaunchedEffect(Unit) {
        authService.onSessionExpired.collect {
            snackbarHostState.showSnackbar(
                message = "Tu sesión ha expirado",
                duration = SnackbarDuration.Long
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                HomeScreen(user = state.user)
            }
            is AuthState.Unauthenticated -> {
                LoginScreen()
            }
            is AuthState.Loading -> {
                LoadingScreen()
            }
        }
    }
}
```

### Fase 3: Robustez

#### SafeEduGoStorage.kt (de Kmp-Common)
```kotlin
package com.edugo.kmp.storage

import com.edugo.kmp.foundation.Result
import com.edugo.kmp.logger.EduGoLogger

class SafeEduGoStorage(
    private val storage: EduGoStorage,
    private val logger: EduGoLogger
) {
    fun putStringSafe(key: String, value: String): Result<Unit> {
        if (!isValidKey(key)) {
            logger.w("Invalid storage key: $key")
            return Result.failure("Invalid key: $key")
        }
        
        return try {
            storage.putString(key, value)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Storage error on putString($key)", e)
            Result.failure(e)
        }
    }
    
    fun getStringSafe(key: String, default: String = ""): String {
        if (!isValidKey(key)) {
            logger.w("Invalid storage key: $key")
            return default
        }
        
        return try {
            storage.getString(key, default)
        } catch (e: Exception) {
            logger.e("Storage error on getString($key)", e)
            default
        }
    }
    
    private fun isValidKey(key: String): Boolean {
        // Validación básica: no vacío, sin espacios, caracteres válidos
        return key.isNotEmpty() && 
               !key.contains(" ") && 
               key.matches(Regex("^[a-zA-Z0-9._-]+$"))
    }
}
```

#### LogoutWithDetails (de Kmp-Common)
```kotlin
sealed class LogoutResult {
    object Success : LogoutResult()
    data class PartialSuccess(val remoteError: String) : LogoutResult()
    object AlreadyLoggedOut : LogoutResult()
}

suspend fun logoutWithDetails(forceLocal: Boolean = true): LogoutResult {
    if (_authState.value is AuthState.Unauthenticated) {
        return LogoutResult.AlreadyLoggedOut
    }
    
    // Intentar logout remoto
    val remoteResult = try {
        authRepository.logout()
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    // Decidir si limpiar local
    if (forceLocal || remoteResult.isSuccess) {
        authRepository.clearLocalSession()
        _authState.value = AuthState.Unauthenticated
    }
    
    return when {
        remoteResult.isSuccess -> LogoutResult.Success
        forceLocal -> LogoutResult.PartialSuccess("Backend error: ${remoteResult.error}")
        else -> LogoutResult.PartialSuccess("Offline, local not cleared")
    }
}
```

---

## 10. Testing Strategy

### Tests por Fase

#### Fase 1: MVP (30 tests)
```kotlin
class SessionManagerTest {
    @Test fun `saveToken stores token in storage`()
    @Test fun `getToken returns saved token`()
    @Test fun `getToken returns null when no token`()
    @Test fun `clearToken removes token`()
    @Test fun `saveUser serializes and stores user`()
    @Test fun `getUser deserializes stored user`()
    @Test fun `getUser returns null when no user`()
    @Test fun `getUser returns null on corrupted data`()
}

class AuthRepositoryImplTest {
    @Test fun `login saves token and user on success`()
    @Test fun `login returns error on failure`()
    @Test fun `logout clears local session always`()
    @Test fun `logout ignores backend errors`()
    @Test fun `restoreSession returns user when valid token`()
    @Test fun `restoreSession returns null when no token`()
    @Test fun `restoreSession returns null when no user`()
}

class RestoreSessionUseCaseTest {
    @Test fun `invoke calls repository restoreSession`()
    @Test fun `invoke returns user on success`()
    @Test fun `invoke returns null on failure`()
}

class SplashScreenTest {
    @Test fun `navigates to Home when session exists`()
    @Test fun `navigates to Login when no session`()
}
```

#### Fase 2: Estado Reactivo (40 tests)
```kotlin
class AuthStateTest {
    @Test fun `isAuthenticated returns true for Authenticated`()
    @Test fun `currentUser returns user for Authenticated`()
    @Test fun `currentUser returns null for Unauthenticated`()
    @Test fun `fold executes correct branch`()
}

class AuthServiceImplTest {
    @Test fun `login emits Loading then Authenticated on success`()
    @Test fun `login emits Loading then Unauthenticated on failure`()
    @Test fun `logout emits Unauthenticated`()
    @Test fun `restoreSession emits Authenticated when valid token`()
    @Test fun `restoreSession emits Unauthenticated when no token`()
    @Test fun `onSessionExpired emits when token expires`()
}

class AppTest {
    @Test fun `shows HomeScreen when Authenticated`()
    @Test fun `shows LoginScreen when Unauthenticated`()
    @Test fun `shows LoadingScreen when Loading`()
    @Test fun `shows snackbar on session expired`()
}
```

#### Fase 3: Robustez (40 tests)
```kotlin
class SafeEduGoStorageTest {
    @Test fun `putStringSafe returns Success for valid key`()
    @Test fun `putStringSafe returns Failure for invalid key`()
    @Test fun `putStringSafe logs warning on invalid key`()
    @Test fun `getStringSafe returns default for invalid key`()
    @Test fun `getStringSafe returns default on storage error`()
}

class LogoutWithDetailsTest {
    @Test fun `returns Success when remote and local succeed`()
    @Test fun `returns PartialSuccess when remote fails but forceLocal true`()
    @Test fun `returns AlreadyLoggedOut when already unauthenticated`()
    @Test fun `clears local when forceLocal true even if remote fails`()
}

class StateFlowStorageTest {  // Opcional
    @Test fun `stateFlowString emits initial value from storage`()
    @Test fun `putString updates StateFlow`()
    @Test fun `refresh updates all flows from storage`()
}
```

---

## 11. Migración desde Sprint 5 Planificado

### Qué Mantener

- **Módulos existentes**: auth, storage, di, kmp-screens
- **TokenRefreshManager**: Ya implementado y funcional
- **EduGoStorage**: Ya funcional con 231 tests
- **Estructura de navegación**: NavigationState y Routes

### Qué Cambiar

#### NO crear módulo kmp-session
```diff
- kmp_new/modules/kmp-session/  # ❌ No crear
+ kmp_new/features/auth/         # ✅ Crear structure Clean Architecture
```

**Razón**: SessionManager simple en data layer es suficiente. No necesita módulo completo.

#### Simplificar SessionState
```diff
- sealed class SessionState {
-     data object Unknown : SessionState()
-     data object Loading : SessionState()
-     data class LoggedIn(val user: User) : SessionState()
-     data object LoggedOut : SessionState()
-     data object Expired : SessionState()  # ❌ Redundante
-     data class Error(val error: AppError) : SessionState()  # ❌ Innecesario
- }

+ sealed class AuthState {
+     data class Authenticated(val user: User, val token: String) : AuthState()
+     data object Unauthenticated : AuthState()
+     data object Loading : AuthState()
+ }
+ // Expiración se maneja con Flow<Unit> separado
```

**Razón**: Estados más simples. Expired es un evento, no un estado. Error se maneja con Result<T>.

#### Integrar AuthService en módulo auth existente
```diff
- kmp_new/modules/kmp-session/SessionManager.kt  # ❌
+ kmp_new/modules/auth/AuthService.kt            # ✅ Extender existente
```

**Razón**: AuthService ya existe. Solo añadir StateFlow y eventos.

### Qué Añadir

1. **features/auth/domain/**: UseCases y Repository interface (de Template-Kmp-Clean)
2. **features/auth/data/**: SessionManager mejorado (Fase 1)
3. **Extension properties en AuthState** (de Kmp-Common)
4. **Flows de eventos** (de Kmp-Common): onSessionExpired, onLogout
5. **SafeEduGoStorage** (Fase 3)

---

## 12. Checklist de Implementación

### Fase 1: MVP Simple (Semana 1)

#### Backend Prerequisites
- [ ] Endpoint `/auth/login` funcional
- [ ] Endpoint `/auth/logout` funcional
- [ ] JWT tokens con claims `sub`, `exp`

#### Código
- [ ] Crear `features/auth/domain/repositories/AuthRepository.kt` (interface)
- [ ] Crear `features/auth/data/repositories/AuthRepositoryImpl.kt`
- [ ] Crear `features/auth/data/services/SessionManager.kt` (con persistencia)
- [ ] Modificar `modules/auth/AuthService.kt` para añadir `restoreSession()`
- [ ] Crear `features/auth/domain/usecases/LoginUseCase.kt`
- [ ] Crear `features/auth/domain/usecases/RestoreSessionUseCase.kt`
- [ ] Crear `features/auth/presentation/viewmodels/LoginViewModel.kt`
- [ ] Modificar `kmp-screens/splash/SplashScreen.kt` para verificar sesión
- [ ] Modificar `kmp-screens/login/LoginScreen.kt` para usar ViewModel
- [ ] Añadir DI en `modules/di/` para nuevos componentes

#### Tests
- [ ] Tests para `SessionManager` (8 tests)
- [ ] Tests para `AuthRepositoryImpl` (7 tests)
- [ ] Tests para `RestoreSessionUseCase` (3 tests)
- [ ] Tests para `LoginViewModel` (5 tests)
- [ ] Tests de integración para `SplashScreen` (2 tests)
- [ ] Tests para flujo completo login → logout (5 tests)

#### QA Manual
- [ ] Login guarda sesión
- [ ] Reabrir app va a Home (no a Login)
- [ ] Logout funciona
- [ ] Sin conexión: logout local funciona

---

### Fase 2: Estado Reactivo (Semana 2)

#### Código
- [ ] Crear `features/auth/domain/models/AuthState.kt` (sealed class)
- [ ] Añadir extension properties a `AuthState`
- [ ] Modificar `AuthService` para exponer `StateFlow<AuthState>`
- [ ] Añadir `onSessionExpired: Flow<Unit>` a `AuthService`
- [ ] Crear `LogoutUseCase.kt`
- [ ] Modificar `App.kt` para observar authState
- [ ] Modificar `HomeScreen.kt` para mostrar currentUser
- [ ] Modificar `SettingsScreen.kt` para logout reactivo
- [ ] Añadir manejo de onSessionExpired en App.kt

#### Tests
- [ ] Tests para `AuthState` extension properties (10 tests)
- [ ] Tests para `AuthState.fold()` (5 tests)
- [ ] Tests para `AuthServiceImpl` con StateFlow (15 tests)
- [ ] Tests para evento `onSessionExpired` (5 tests)
- [ ] Tests de UI para App.kt (5 tests)

#### QA Manual
- [ ] Home muestra nombre de usuario
- [ ] Settings muestra email
- [ ] Expiración muestra Snackbar
- [ ] Navegación automática tras expiración
- [ ] Logout navega a Login

---

### Fase 3: Robustez Avanzada (Semana 3)

#### Código
- [ ] Crear `modules/storage/SafeEduGoStorage.kt`
- [ ] Añadir validación de keys en SafeEduGoStorage
- [ ] Modificar SessionManager para usar SafeEduGoStorage
- [ ] Crear `LogoutResult` sealed class
- [ ] Añadir `logoutWithDetails()` a AuthService
- [ ] (Opcional) Crear `modules/storage/StateFlowStorage.kt`
- [ ] (Opcional) Crear UserPrefs con StateFlowStorage

#### Tests
- [ ] Tests para SafeEduGoStorage (15 tests)
- [ ] Tests para validación de keys (10 tests)
- [ ] Tests para logoutWithDetails (10 tests)
- [ ] Tests para StateFlowStorage (15 tests - opcional)

#### QA Manual
- [ ] Logout sin conexión muestra mensaje apropiado
- [ ] Keys inválidas son rechazadas (verificar logs)
- [ ] Performance de checkSession < 500ms
- [ ] Storage no tiene keys corruptas

---

## 13. Métricas de Éxito

### Funcionales
- [ ] Usuario mantiene sesión entre reinicios (100% de casos)
- [ ] Logout funciona offline (100% de casos)
- [ ] Expiración navega a Login automáticamente
- [ ] Funciona en Android + Desktop + Wasm

### Técnicas
- [ ] Mínimo 110 tests nuevos
- [ ] Coverage > 80% en código nuevo
- [ ] Performance: checkSession() < 500ms (p95)
- [ ] Sin memory leaks (verificado con Profiler)
- [ ] 0 crashes en QA manual (20 escenarios)

### Arquitectura
- [ ] Separación clara: Domain/Data/Presentation
- [ ] AuthState con extension properties útiles
- [ ] Eventos con Flows (onSessionExpired, onLogout)
- [ ] Storage con validación (SafeEduGoStorage)
- [ ] DI completo con Koin

---

## 14. Decisiones de Diseño Clave

### Por qué NO módulo kmp-session separado

**Sprint 5 Original**: Crear módulo `kmp-session` con `SessionManager`

**Decisión**: NO crear módulo separado, integrar en `features/auth/data/`

**Razones**:
1. **YAGNI**: No necesitamos un módulo completo para un componente simple
2. **Overhead**: Crear módulo requiere build.gradle, DI, etc.
3. **Template-Kmp-Clean lo hace así**: SessionManager en data layer funciona bien
4. **Facilita refactor futuro**: Si crece, moverlo a módulo es fácil

### Por qué AuthState en vez de SessionState

**Sprint 5 Original**: `SessionState` con 6 estados

**Decisión**: `AuthState` con 3 estados (de Kmp-Common)

**Razones**:
1. **Expired es un evento, no un estado**: Se maneja con `onSessionExpired: Flow<Unit>`
2. **Error se maneja con Result<T>**: No necesita estado dedicado
3. **Unknown es redundante**: App siempre sabe si está Authenticated o no
4. **Más simple**: 3 estados son suficientes para todas las UIs

### Por qué Flows de eventos en vez de callbacks

**Sprint 5 Original**: No especificado claramente

**Decisión**: `onSessionExpired: Flow<Unit>` (de Kmp-Common)

**Razones**:
1. **Reactivo**: UI puede `collectAsState()` o `collect` en LaunchedEffect
2. **Composable**: Fácil combinar múltiples flows con `merge`, `combine`
3. **Lifecycle-aware**: Flow se cancela automáticamente en Compose
4. **Testeable**: Fácil verificar emisiones en tests

### Por qué SafeEduGoStorage

**Sprint 5 Original**: No mencionado

**Decisión**: Añadir en Fase 3 (inspirado en Kmp-Common)

**Razones**:
1. **Keys inválidas causan crashes**: Especialmente en iOS/Wasm
2. **Debugging más fácil**: Logs automáticos de errores
3. **Nunca crashea**: Retorna Result o default, nunca lanza excepción
4. **Bajo costo**: Wrapper simple sobre EduGoStorage existente

### Por qué logoutWithDetails

**Sprint 5 Original**: Logout simple

**Decisión**: Añadir `logoutWithDetails(forceLocal)` en Fase 3 (de Kmp-Common)

**Razones**:
1. **Soporte offline**: Usuario puede cerrar sesión sin internet
2. **UX mejorada**: Mensajes diferenciados ("Logout exitoso" vs "Sin conexión")
3. **Idempotencia**: Múltiples llamadas son seguras
4. **Transparencia**: UI sabe exactamente qué pasó

---

## 15. Conclusión

### Recomendación Final

**Implementar enfoque híbrido en 3 fases**:

1. **Fase 1 (MVP)**: Base de Template-Kmp-Clean con persistencia real
2. **Fase 2 (Reactivo)**: AuthState + Flows de eventos de Kmp-Common
3. **Fase 3 (Robustez)**: SafeEduGoStorage + logoutWithDetails de Kmp-Common

### Ventajas del Enfoque Híbrido

1. **Entrega rápida de valor**: MVP funcional en Semana 1
2. **Complejidad incremental**: No abruma al equipo desde el inicio
3. **Aprende de proyectos reales**: Usa patrones probados
4. **Flexible**: Fase 3 puede posponerse si hay presión de tiempo
5. **Testeable**: Tests incrementales en cada fase
6. **Documentado**: Este documento guía toda la implementación

### Diferencias con Sprint 5 Original

| Aspecto | Sprint 5 Original | Enfoque Híbrido |
|---------|-------------------|-----------------|
| **Módulo kmp-session** | Sí (nuevo módulo) | No (features/auth/data) |
| **SessionState** | 6 estados | 3 estados (AuthState) |
| **Eventos** | No especificado | Flows (onSessionExpired, onLogout) |
| **Storage validation** | No | SafeEduGoStorage (Fase 3) |
| **Clean Architecture** | Parcial | Completo (Domain/Data/Presentation) |
| **Tiempo estimado** | 8-13 días | 10-16 días (más robusto) |
| **Complejidad inicial** | Media | Baja (MVP simple) |

### Próximos Pasos

1. **Revisar este documento con el equipo**
2. **Aprobar enfoque por fases**
3. **Asignar desarrolladores a cada fase**
4. **Crear branch `feature/auth-persistence-hybrid`**
5. **Implementar Fase 1** (prioridad ALTA)
6. **Code review antes de Fase 2**
7. **Decidir si Fase 3 va en este sprint o siguiente**

---

**Última actualización**: 2026-02-10  
**Autor**: Claude AI (Análisis comparativo)  
**Versión**: 1.0  
**Estado**: RECOMENDACIÓN FINAL
