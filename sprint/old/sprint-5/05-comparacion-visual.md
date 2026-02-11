# Sprint 5 - Comparación Visual: 3 Enfoques

## Tabla Comparativa Rápida

| Característica | Plan Original Sprint 5 | Kmp-Common | Template-Kmp-Clean | Recomendación Híbrida |
|---------------|------------------------|------------|---------------------|----------------------|
| **ARQUITECTURA** |
| Módulo kmp-session | ✅ Sí (nuevo) | ❌ No | ❌ No | ❌ No (solo SessionManager) |
| Clean Architecture | 🟡 Parcial | ❌ No | ✅ Sí | ✅ Sí |
| Separación capas | SessionManager → AuthService | AuthService todo-en-uno | Domain/Data/Presentation | Domain/Data/Presentation |
| **GESTIÓN DE ESTADO** |
| Sealed class | SessionState (6 estados) | AuthState (3 estados) | LoginState (data class) | AuthState (3 estados) |
| Estados | Unknown, Loading, LoggedIn, LoggedOut, Expired, Error | Authenticated, Unauthenticated, Loading | isLoading, user, error | Authenticated, Unauthenticated, Loading |
| StateFlow | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí |
| Extension properties | ❌ No | ✅ Sí (15+) | ❌ No | ✅ Sí (copiado) |
| Pattern matching fold() | ❌ No | ✅ Sí | ❌ No | ✅ Sí (copiado) |
| **EVENTOS** |
| onSessionExpired | 🟡 Estado Expired | ✅ Flow<Unit> | ❌ No | ✅ Flow<Unit> |
| onLogout | ❌ No | ✅ Flow<LogoutResult> | ❌ No | ✅ Flow<LogoutResult> |
| **PERSISTENCIA** |
| Storage básico | ✅ EduGoStorage | ✅ EduGoStorage | 🟡 In-memory | ✅ EduGoStorage |
| Validación de keys | ❌ No | ✅ SafeEduGoStorage | ❌ No | ✅ SafeEduGoStorage (Fase 3) |
| Storage reactivo | ❌ No | ✅ StateFlowStorage | ❌ No | 🟡 Opcional (Fase 3) |
| Secure storage | 🟡 Fase 5 opcional | 🟡 TODO | ❌ No | 🟡 Futuro |
| **SESIÓN** |
| restoreSession() | ✅ Sí | ✅ Sí | ❌ No | ✅ Sí |
| Token refresh | ✅ Sí (ya existe) | ✅ Sí | ❌ No | ✅ Sí |
| Logout offline | 🟡 No especificado | ✅ logoutWithDetails() | ❌ No | ✅ logoutWithDetails (Fase 3) |
| **TESTING** |
| Tests planificados | ~100 nuevos | 171 (auth) + 231 (storage) | Básico (mocks) | ~110 nuevos |
| Coverage objetivo | >80% | >90% (real) | No especificado | >80% |
| **COMPLEJIDAD** |
| Líneas de código | ~800-1000 | ~2000+ | ~300-400 | ~1000-1200 |
| Curva aprendizaje | 🟡 Media | 🔴 Alta | 🟢 Baja | 🟡 Media |
| Tiempo implementación | 8-13 días | Ya hecho | 2-3 días | 7-9 días (Fase 1+2) |
| **PRODUCCIÓN** |
| Production-ready | 🟡 Casi | ✅ Sí | ❌ No (MVP) | ✅ Sí (con Fase 3) |
| Mantenibilidad | 🟢 Alta | 🟢 Muy alta | 🟡 Media | 🟢 Muy alta |

**Leyenda**:
- ✅ Implementado / Recomendado
- 🟡 Parcial / Opcional
- ❌ No incluido / No recomendado

---

## Diagrama de Arquitecturas

### Plan Original Sprint 5

```
┌─────────────────────────────────────┐
│      UI Layer (kmp-screens)         │
│  • SplashScreen                     │
│  • LoginScreen                      │
│  • HomeScreen                       │
│  • SettingsScreen                   │
└──────────┬──────────────────────────┘
           │ observa StateFlow<SessionState>
┌──────────▼──────────────────────────┐
│   kmp-session (NUEVO MÓDULO)        │
│  • SessionManager                   │
│    - sessionState: StateFlow        │
│    - checkSession()                 │
│    - login()                        │
│    - logout()                       │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│      auth module (MODIFICADO)       │
│  • AuthService                      │
│    - restoreSession() (NUEVO)       │
│    - login()                        │
│    - logout()                       │
│    - refreshToken()                 │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│      storage module (EXISTENTE)     │
│  • EduGoStorage                     │
└─────────────────────────────────────┘

Problemas:
❌ Duplicación: SessionManager + AuthService
❌ Módulo completo para componente simple
❌ Sin validación de storage
❌ Sin Clean Architecture completa
```

---

### Kmp-Common (Referencia)

```
┌─────────────────────────────────────┐
│             UI Layer                │
│  • Observa authState                │
│  • Collect onSessionExpired         │
└──────────┬──────────────────────────┘
           │ observa StateFlow<AuthState>
           │ collect Flow<Unit> (eventos)
┌──────────▼──────────────────────────┐
│         AuthService                 │
│  • authState: StateFlow<AuthState>  │
│  • onSessionExpired: Flow<Unit>     │
│  • onLogout: Flow<LogoutResult>     │
│  • tokenRefreshManager              │
│  • login/logout/restoreSession      │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│       AuthRepository                │
│  • login/logout/refresh             │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│    Storage (3 capas)                │
│  • EduGoStorage (básico)            │
│  • SafeEduGoStorage (validación)    │
│  • StateFlowStorage (reactivo)      │
└─────────────────────────────────────┘

Ventajas:
✅ Robusto y probado (171 + 231 tests)
✅ Eventos como Flows
✅ Storage con validación
✅ Extension properties poderosas

Desventajas:
❌ No sigue Clean Architecture
❌ AuthService hace todo (God Object)
❌ Complejidad alta inicial
```

---

### Template-Kmp-Clean (Referencia)

```
┌─────────────────────────────────────┐
│      UI Layer (ViewModel)           │
│  • LoginViewModel                   │
│    - state: StateFlow<LoginState>   │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│   Domain Layer (UseCases)           │
│  • LoginUseCase                     │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│  Domain Layer (Repository Interface)│
│  • AuthRepository (interface)       │
└──────────┬──────────────────────────┘
           │ implementa
┌──────────▼──────────────────────────┐
│    Data Layer (Repository Impl)     │
│  • AuthRepositoryImpl               │
└──────────┬──────────────────────────┘
           │ usa
┌──────────▼──────────────────────────┐
│    Data Layer (SessionManager)      │
│  • SessionManager (in-memory)       │
│    - saveToken()                    │
│    - getToken()                     │
└─────────────────────────────────────┘

Ventajas:
✅ Clean Architecture pura
✅ Simple y fácil de entender
✅ UseCase pattern

Desventajas:
❌ Sin persistencia real (in-memory)
❌ Sin estado global
❌ Sin manejo de expiración
❌ No production-ready
```

---

### Recomendación Híbrida (FINAL)

```
┌─────────────────────────────────────────────┐
│         UI Layer (Compose)                  │
│  • App.kt - Observa authState + eventos    │
│  • SplashScreen, LoginScreen, etc.         │
└──────────┬──────────────────────────────────┘
           │ observa StateFlow<AuthState>
           │ collect Flow<Unit> (onSessionExpired)
┌──────────▼──────────────────────────────────┐
│  Presentation Layer (ViewModels)            │
│  • LoginViewModel                           │
│  • HomeViewModel                            │
└──────────┬──────────────────────────────────┘
           │ usa
┌──────────▼──────────────────────────────────┐
│     Domain Layer (UseCases)                 │
│  • LoginUseCase                             │
│  • LogoutUseCase                            │
│  • RestoreSessionUseCase                    │
└──────────┬──────────────────────────────────┘
           │ usa
┌──────────▼──────────────────────────────────┐
│  Application Layer (AuthService)            │
│  • authState: StateFlow<AuthState>          │
│  • onSessionExpired: Flow<Unit>             │
│  • onLogout: Flow<LogoutResult>             │
│  • login/logout/restoreSession              │
└──────────┬──────────────────────────────────┘
           │ usa
┌──────────▼──────────────────────────────────┐
│  Data Layer (Repository)                    │
│  • AuthRepository (interface en Domain)     │
│  • AuthRepositoryImpl (impl en Data)        │
└──────────┬──────────────────────────────────┘
           │ usa
┌──────────▼──────────────────────────────────┐
│  Data Layer (SessionManager + Storage)      │
│  • SessionManager (persistencia real)       │
│  • SafeEduGoStorage (validación - Fase 3)   │
│  • StateFlowStorage (opcional - Fase 3)     │
└─────────────────────────────────────────────┘

Combina lo mejor de los 3:
✅ Clean Architecture (Template-Kmp-Clean)
✅ Extension properties + Flows (Kmp-Common)
✅ Storage con validación (Kmp-Common)
✅ UseCase pattern (Template-Kmp-Clean)
✅ Logout offline (Kmp-Common)
✅ Persistencia real
✅ Production-ready
```

---

## Comparación de Código: AuthState

### Plan Original (6 estados)

```kotlin
sealed class SessionState {
    data object Unknown : SessionState()
    data object Loading : SessionState()
    data class LoggedIn(val user: User) : SessionState()
    data object LoggedOut : SessionState()
    data object Expired : SessionState()  // ❌ Debería ser evento
    data class Error(val error: AppError) : SessionState()  // ❌ Result<T>
}

// Sin extension properties
// Sin pattern matching
```

**Problemas**:
- Expired es evento, no estado
- Error se maneja con Result<T>
- Unknown redundante

---

### Kmp-Common (3 estados + helpers)

```kotlin
sealed class AuthState {
    data class Authenticated(
        val user: AuthUserInfo,
        val token: AuthToken
    ) : AuthState()
    
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
}

// Extension properties (PODEROSAS)
val AuthState.isAuthenticated: Boolean
val AuthState.currentUser: AuthUserInfo?
val AuthState.currentToken: AuthToken?

// Pattern matching
inline fun <R> AuthState.fold(
    onAuthenticated: (AuthUserInfo, AuthToken) -> R,
    onUnauthenticated: () -> R,
    onLoading: () -> R
): R

// Inline helpers
inline fun AuthState.ifAuthenticated(action: (AuthUserInfo, AuthToken) -> Unit)
inline fun AuthState.ifUnauthenticated(action: () -> Unit)
```

**Ventajas**:
- Simple: solo 3 estados
- Extension properties útiles
- Pattern matching elegante

---

### Recomendación (Copia de Kmp-Common)

```kotlin
// Mismo que Kmp-Common, adaptado a EduGo
sealed class AuthState {
    data class Authenticated(
        val user: User,
        val token: String
    ) : AuthState()
    
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
}

// Extension properties
val AuthState.isAuthenticated: Boolean
    get() = this is AuthState.Authenticated

val AuthState.currentUser: User?
    get() = (this as? AuthState.Authenticated)?.user

// Pattern matching
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

---

## Comparación: Manejo de Expiración

### Plan Original

```kotlin
// Expiración como estado
sealed class SessionState {
    data object Expired : SessionState()
}

// En UI:
when (sessionState) {
    is SessionState.Expired -> {
        showMessage("Sesión expirada")
        navigateToLogin()
    }
}
```

**Problemas**:
- Expired es transitorio (evento), no persistente (estado)
- UI debe estar en when branch específico

---

### Kmp-Common + Recomendación

```kotlin
// Expiración como evento (Flow)
interface AuthService {
    val onSessionExpired: Flow<Unit>
}

// En UI:
LaunchedEffect(Unit) {
    authService.onSessionExpired.collect {
        // Se ejecuta SIEMPRE que expire, sin importar estado actual
        showSnackbar("Sesión expirada")
        navigateToLogin()
    }
}
```

**Ventajas**:
- Evento se captura desde cualquier pantalla
- No requiere estar observando estado específico
- Más flexible y composable

---

## Comparación: Logout Offline

### Plan Original

```kotlin
suspend fun logout(): Result<Unit> {
    // No especificado claramente
    val result = api.logout()
    if (result.isSuccess) {
        storage.clear()
    }
    return result
}
```

**Problema**: Sin internet, usuario queda "logueado"

---

### Kmp-Common + Recomendación

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
    
    // Intentar logout remoto
    val remoteResult = try {
        api.logout()
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    // SIEMPRE limpiar local si forceLocal=true
    if (forceLocal || remoteResult.isSuccess) {
        storage.clear()
        _authState.value = AuthState.Unauthenticated
    }
    
    return when {
        remoteResult.isSuccess -> LogoutResult.Success
        forceLocal -> LogoutResult.PartialSuccess("Backend error")
        else -> LogoutResult.PartialSuccess("Offline")
    }
}
```

**Ventajas**:
- Logout local siempre funciona
- Información granular del resultado
- Idempotente (múltiples llamadas seguras)

---

## Comparación: Storage

### Plan Original

```kotlin
// Solo EduGoStorage básico
storage.putString("key", "value")
val value = storage.getString("key", "default")

// Sin validación
storage.putString("invalid key!", "value")  // ⚠️ Puede causar crash en iOS
```

---

### Kmp-Common + Recomendación (Fase 3)

```kotlin
class SafeEduGoStorage(
    private val storage: EduGoStorage,
    private val logger: EduGoLogger
) {
    fun putStringSafe(key: String, value: String): Result<Unit> {
        // Validar key
        if (!isValidKey(key)) {
            logger.w("Invalid storage key: $key")
            return Result.failure("Invalid key")
        }
        
        // Nunca lanza excepción
        return try {
            storage.putString(key, value)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Storage error", e)
            Result.failure(e)
        }
    }
    
    private fun isValidKey(key: String): Boolean {
        return key.matches(Regex("^[a-zA-Z0-9._-]+$"))
    }
}
```

**Ventajas**:
- Previene crashes por keys inválidas
- Logs automáticos
- Nunca lanza excepciones

---

## Decisión Visual

### ¿Cuál elegir?

```
┌─────────────────────────────────────────────────────────────┐
│                    RECOMENDACIÓN                            │
│                                                             │
│  Opción A: Enfoque Híbrido (Fases 1+2 en Sprint 5)         │
│                                                             │
│  ✅ Base simple (Template-Kmp-Clean)                        │
│  ✅ Patrones robustos (Kmp-Common)                          │
│  ✅ Clean Architecture completa                             │
│  ✅ Production-ready (con Fase 3 en Sprint 6)               │
│  ✅ Tiempo: 7-9 días (Fases 1+2)                            │
│                                                             │
│  Incluye:                                                   │
│  • Persistencia real (Fase 1)                               │
│  • Estado reactivo (Fase 2)                                 │
│  • Extension properties                                     │
│  • Flows de eventos                                         │
│  • UseCase pattern                                          │
│                                                             │
│  Postpone a Sprint 6:                                       │
│  • SafeEduGoStorage (Fase 3)                                │
│  • logoutWithDetails (Fase 3)                               │
│  • StateFlowStorage (Fase 3 opcional)                       │
└─────────────────────────────────────────────────────────────┘

vs

┌─────────────────────────────────────────────────────────────┐
│              PLAN ORIGINAL (Sprint 5)                       │
│                                                             │
│  ❌ Duplicación SessionManager + AuthService                │
│  ❌ Módulo completo para componente simple                  │
│  ❌ Sin validación de storage                               │
│  ❌ Sin Clean Architecture completa                         │
│  ❌ Sin soporte logout offline                              │
│  ⚠️  Tiempo: 8-13 días                                      │
└─────────────────────────────────────────────────────────────┘

vs

┌─────────────────────────────────────────────────────────────┐
│            Opción B: Híbrido Completo (Fases 1+2+3)        │
│                                                             │
│  ✅ Todo lo de Opción A                                     │
│  ✅ SafeEduGoStorage                                        │
│  ✅ logoutWithDetails                                       │
│  ⚠️  Tiempo: 11-15 días (muy largo para 1 sprint)          │
└─────────────────────────────────────────────────────────────┘

vs

┌─────────────────────────────────────────────────────────────┐
│              Opción C: Solo MVP (Fase 1)                    │
│                                                             │
│  ✅ Persistencia básica                                     │
│  ❌ Sin estado reactivo                                     │
│  ❌ Sin manejo elegante de expiración                       │
│  ⚠️  Solo para prototipo rápido                             │
│  ✅ Tiempo: 3-5 días                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Gráfico de Valor vs Complejidad

```
Valor
  │
  │                           ┌─────────────┐
  │                           │ Opción B    │
  │                           │ (Completo)  │
  │         ┌─────────────┐   └─────────────┘
  │         │ Opción A    │
  │         │(RECOMENDADO)│
  │         └─────────────┘
  │                          ┌──────────────┐
  │                          │Plan Original │
  │  ┌──────────────┐        └──────────────┘
  │  │ Opción C     │
  │  │ (Solo MVP)   │
  │  └──────────────┘
  └────────────────────────────────────────────── Complejidad

Opción C: Bajo valor, baja complejidad → Solo prototipo
Plan Original: Medio valor, media-alta complejidad → Duplicación
Opción A: Alto valor, media complejidad → BALANCE PERFECTO
Opción B: Muy alto valor, alta complejidad → Muy largo
```

---

## Tabla de Decisión Final

| Si necesitas... | Elige... |
|----------------|----------|
| Prototipo rápido para demo | Opción C (Solo Fase 1) |
| Balance valor/tiempo óptimo | **Opción A (Fases 1+2)** ⭐ |
| Máxima robustez en 1 sprint | Opción B (Fases 1+2+3) |
| Seguir plan original | Plan Original (no recomendado) |

---

## Resumen de Ventajas del Híbrido

### De Template-Kmp-Clean (Simplicidad)
✅ Clean Architecture (Domain/Data/Presentation)  
✅ UseCase pattern  
✅ Repository en domain  
✅ Curva de aprendizaje baja  

### De Kmp-Common (Robustez)
✅ AuthState con extension properties  
✅ Flows de eventos (onSessionExpired, onLogout)  
✅ Pattern matching con fold()  
✅ SafeEduGoStorage con validación  
✅ logoutWithDetails con soporte offline  
✅ Tests exhaustivos  

### Mejoras sobre Plan Original
✅ No duplica SessionManager + AuthService  
✅ No requiere módulo completo  
✅ Validación de storage  
✅ Soporte logout offline  
✅ Extension properties poderosas  

---

**Última actualización**: 2026-02-10  
**Documento**: Comparación Visual  
**Recomendación**: Opción A (Fases 1+2 en Sprint 5, Fase 3 en Sprint 6)
