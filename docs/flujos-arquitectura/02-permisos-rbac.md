# 02 — Permisos y RBAC

## Clases Involucradas

```mermaid
classDiagram
    class UserContext {
        +roleId: String
        +roleName: String
        +schoolId: String?
        +schoolName: String?
        +academicUnitId: String?
        +permissions: List~String~
        +hasPermission(permission): Boolean
        +hasAnyPermission(vararg): Boolean
        +hasAllPermissions(vararg): Boolean
        +hasRole(roleName): Boolean
        +hasSchool(): Boolean
    }
    class AuthState {
        <<sealed>>
        +Authenticated(user, token, activeContext)
        +Unauthenticated
        +Loading
    }
    class AuthUserInfo {
        +id: String
        +email: String
        +fullName: String
        +avatarUrl: String?
    }
    class AuthToken {
        +accessToken: String
        +refreshToken: String
        +expiresAt: Long
        +isExpired(): Boolean
        +hasRefreshToken(): Boolean
    }
    class AuthServiceImpl {
        -_authState: MutableStateFlow
        -_onTokenRefreshed: MutableSharedFlow
        -stateMutex: Mutex
        +authState: StateFlow~AuthState~
        +onTokenRefreshed: Flow~Unit~
        +onSessionExpired: Flow~Unit~
        +onLogout: Flow~LogoutResult~
        +login(credentials): LoginResult
        +logout()
        +restoreSession()
        +switchContext(schoolId): Result~UserContext~
        +getAvailableContexts(): Result~AvailableContextsResponse~
        +getToken(): String?
        +refreshToken(): String?
    }
    class LoginResponse {
        +accessToken: String
        +refreshToken: String
        +expiresIn: Int
        +user: AuthUserInfo
        +activeContext: UserContext
        +schools: List~SchoolInfo~
    }
    class RefreshResponse {
        +accessToken: String
        +expiresIn: Int
        +refreshToken: String?
        +activeContext: UserContext?
    }
    class SwitchContextResponse {
        +accessToken: String
        +refreshToken: String
        +expiresIn: Int
        +context: SwitchContextInfo?
    }
    class JwtParser {
        +parse(token): JwtParseResult
        +parseOrThrow(token): JwtClaims
    }
    class DataSyncService {
        +currentBundle: StateFlow~UserDataBundle?~
        +syncState: StateFlow~SyncState~
        +fullSync(): Result~UserDataBundle~
        +deltaSync(): Result~UserDataBundle~
        +restoreFromLocal(): UserDataBundle?
        +clearAll()
    }
    class UserDataBundle {
        +menu: MenuResponse
        +permissions: List~String~
        +screens: Map~String ScreenDefinition~
        +availableContexts: List~UserContext~
        +hashes: Map~String String~
        +syncedAt: Instant
    }
    class PermissionCheckerImpl~R,P~ {
        -resolver: (R, P) -> Boolean
        +check(resource, permission): Boolean
    }

    AuthState "1" --> "1" AuthUserInfo : contiene
    AuthState "1" --> "1" AuthToken : contiene
    AuthState "1" --> "1" UserContext : activeContext
    AuthServiceImpl --> AuthState : emite
    AuthServiceImpl --> UserContext : gestiona
    AuthServiceImpl --> JwtParser : extrae contexto del JWT
    AuthServiceImpl --> DataSyncService : triggerea sync
    DataSyncService --> UserDataBundle : emite
    UserDataBundle --> UserContext : availableContexts
    PermissionCheckerImpl --> UserContext : consulta
    LoginResponse --> AuthUserInfo : contiene
    LoginResponse --> UserContext : activeContext
    RefreshResponse --> UserContext : activeContext opcional
```

---

## Flujo Secuencial: Login con RBAC

```mermaid
sequenceDiagram
    participant UI as LoginScreen
    participant Auth as AuthServiceImpl
    participant Repo as AuthRepositoryImpl
    participant Net as EduGoHttpClient
    participant Backend as IAM Backend
    participant Storage as SafeEduGoStorage
    participant App as App.kt
    participant Sync as DataSyncService

    UI->>Auth: login(credentials)
    Auth->>Auth: credentials.validate()
    Auth->>Auth: rateLimiter.execute { }

    Auth->>Repo: login(credentials)
    Repo->>Net: POST /api/v1/auth/login (plainHttp, sin token)
    Net->>Backend: { email, password }
    Backend-->>Net: LoginResponse { token, user, active_context, schools }
    Net-->>Repo: HttpResponse 200
    Repo-->>Auth: Result.Success(LoginResponse)

    Auth->>Auth: stateMutex.withLock { }
    Auth->>Storage: putStringSafe("auth_token", token.toJson())
    Auth->>Storage: putStringSafe("auth_user", user.toJson())
    Auth->>Storage: putStringSafe("auth_context", activeContext.toJson())
    Auth->>Auth: _authState.value = Authenticated(user, token, activeContext)
    Auth->>Auth: tokenRefreshManager.startAutomaticRefresh(token)
    Auth-->>UI: LoginResult.Success(response)

    UI->>App: onLoginSuccess(response.schools)

    alt schools.size > 1
        App->>App: navState.replaceAll(Route.SchoolSelection)
        Note over App: SchoolSelectionScreen muestra lista de escuelas<br/>Al seleccionar: switchContext + fullSync → Dashboard
    else schools.size <= 1
        App->>Sync: fullSync()
        Sync->>Backend: GET /api/v1/sync/bundle
        Backend-->>Sync: SyncBundleResponse { menu, permissions, screens, contexts, hashes }
        Sync->>Sync: _currentBundle.value = bundle
        App->>App: navState.replaceAll(Route.Dashboard)
    end
```

---

## Flujo: Verificacion de Permiso en un Evento SDUI

```mermaid
flowchart TD
    A([onEvent ScreenEvent.SAVE_NEW]) --> B[EventOrchestrator.execute]
    B --> C[registry.find screenKey → ScreenContract]
    C --> D[contract.permissionFor SAVE_NEW]
    D --> E["permiso = 'schools:create'"]
    E --> F[userContextProvider]
    F --> G{userContext != null?}
    G -- No --> H[EventResult.PermissionDenied]
    G -- Si --> I{userContext.hasPermission\nschools:create ?}
    I -- No --> H
    I -- Si --> J[executeSubmit POST]
    J --> K[HTTP POST al endpoint]
    K --> L{Respuesta HTTP}
    L -- 200 --> M[EventResult.Success]
    L -- 403 --> N[EventResult.PermissionDenied]
    L -- 500 --> O[EventResult.Error retry=true]
    H --> P[UI muestra dialogo\nSin permisos]
    M --> Q[snackbar exito + onBack]
```

---

## Mapa de Permisos Predeterminados por Evento

```mermaid
flowchart LR
    subgraph ScreenEvent
        LD[LOAD_DATA]
        S[SEARCH]
        SI[SELECT_ITEM]
        LM[LOAD_MORE]
        SN[SAVE_NEW]
        SE[SAVE_EXISTING]
        D[DELETE]
        R[REFRESH]
        C[CREATE]
    end

    subgraph Permiso ["Permiso Requerido"]
        PR["resource:read"]
        PC["resource:create"]
        PU["resource:update"]
        PD["resource:delete"]
        PN[null - sin permiso]
    end

    LD --> PR
    S --> PR
    SI --> PR
    LM --> PR
    SN --> PC
    C --> PC
    SE --> PU
    D --> PD
    R --> PN
```

---

## Estado de Autenticacion: Maquina de Estados

```mermaid
stateDiagram-v2
    [*] --> Loading : app inicia / restoreSession()

    Loading --> Authenticated : token valido en storage
    Loading --> Unauthenticated : sin token / token invalido

    Unauthenticated --> Authenticated : login() exitoso
    Authenticated --> Unauthenticated : logout() / sesion expirada

    state Authenticated {
        [*] --> ConContextoActivo
        ConContextoActivo --> CambiandoContexto : switchContext(schoolId)
        CambiandoContexto --> ConContextoActivo : exito + fullSync
        CambiandoContexto --> ConContextoActivo : fallo - mantiene contexto anterior
        ConContextoActivo --> RefrescandoToken : token proximo a expirar
        RefrescandoToken --> ConContextoActivo : exito + deltaSync
        RefrescandoToken --> ConContextoActivo : fallo de red (reintenta)
    }

    state Unauthenticated {
        [*] --> Libre
        Libre --> EnEspera : rate limit activado
        EnEspera --> Libre : tiempo de espera cumplido
    }
```

---

## Flujo: Cambio de Escuela (Switch Context)

```mermaid
sequenceDiagram
    participant UI as ContextPickerOverlay
    participant Auth as AuthServiceImpl
    participant JWT as JwtParser
    participant Repo as AuthRepositoryImpl
    participant Backend as IAM Backend
    participant Storage as SafeEduGoStorage
    participant Sync as DataSyncService
    participant Main as MainScreen

    UI->>Auth: switchContext(schoolId)
    Auth->>Repo: switchContext(currentToken, schoolId)
    Repo->>Backend: POST /api/v1/auth/switch-context { schoolId }
    Backend-->>Repo: SwitchContextResponse { access_token, refresh_token, expires_in, context: ContextInfo }
    Note over Repo: SwitchContextInfo es mas simple que UserContext<br/>{ school_id, school_name, role, user_id, email }
    Repo-->>Auth: Result.Success(SwitchContextResponse)

    Auth->>JWT: parse(accessToken)
    JWT-->>Auth: JwtParseResult con claims

    alt JWT contiene active_context en claims
        Auth->>Auth: UserContext completo (con permissions) desde JWT
    else JWT sin active_context o error de parse
        Auth->>Auth: construir UserContext basico desde ContextInfo<br/>(roleId="", roleName=role, schoolId, schoolName)
    end

    Auth->>Storage: actualizar "auth_token" (nuevo access + refresh)
    Auth->>Storage: actualizar "auth_context" (nuevo UserContext)
    Auth->>Auth: _authState.value = Authenticated(user, newToken, newContext)
    Auth->>Auth: tokenRefreshManager.startAutomaticRefresh(newToken)
    Auth-->>UI: Result.Success(newContext)

    UI->>Sync: fullSync()
    Note over Sync: Descarga bundle completo para nueva escuela
    Sync->>Backend: GET /api/v1/sync/bundle
    Backend-->>Sync: SyncBundleResponse { menu, permissions, screens, contexts }
    Sync->>Sync: _currentBundle.value = newBundle

    Note over Main: LaunchedEffect(bundle) se reactiva<br/>Reconstruye navegacion con nuevo menu
    Main->>Main: allItems = bundle.menu.items
    Main->>Main: availableContexts = bundle.availableContexts
    Main->>Main: auto-select primer item de navegacion
```

---

## Flujo: Renovacion de Token (Token Refresh)

```mermaid
sequenceDiagram
    participant TRM as TokenRefreshManagerImpl
    participant Repo as AuthRepositoryImpl
    participant Backend as IAM Backend
    participant Storage as SafeEduGoStorage
    participant Auth as AuthServiceImpl
    participant App as App.kt
    participant Sync as DataSyncService

    Note over TRM: scheduleNextRefresh calcula:<br/>delay = (expiresAt - now) - threshold<br/>threshold = 300s (5 min antes)

    TRM->>TRM: delay(delayMs) — espera hasta umbral
    TRM->>Repo: refresh(currentRefreshToken)
    Note over Repo: Refresh tokens son JWT (no random base64)<br/>Backend valida con ValidateRefreshJWT()
    Repo->>Backend: POST /api/v1/auth/refresh { refresh_token }

    Backend->>Backend: ValidateRefreshJWT(refreshToken)
    Backend->>Backend: GenerateRefreshJWT() — token rotation

    Backend-->>Repo: RefreshResponse { access_token, expires_in, refresh_token?, active_context? }
    Note over Repo: Token rotation: nuevo refresh_token en cada refresh<br/>Si no viene refresh_token, se reutiliza el existente

    Repo-->>TRM: Result.Success(RefreshResponse)
    TRM->>TRM: newToken = toAuthToken(existingRefreshToken)
    TRM->>Storage: putStringSafe("auth_token", newToken)

    alt RefreshResponse incluye activeContext
        TRM->>Storage: putStringSafe("auth_context", activeContext)
        Note over TRM: Contexto puede cambiar si el backend<br/>actualizo permisos o memberships
    end

    TRM->>TRM: _onRefreshSuccess.emit(newToken)

    Note over Auth: init { } collector recibe onRefreshSuccess
    Auth->>Auth: stateMutex.withLock { }
    Auth->>Auth: Lee auth_context de storage (puede estar actualizado)
    Auth->>Auth: _authState.value = Authenticated(user, newToken, updatedContext)
    Auth->>Auth: _onTokenRefreshed.emit(Unit)

    Note over App: LaunchedEffect(Unit) { onTokenRefreshed.collect }
    App->>Sync: deltaSync()
    Sync->>Backend: POST /api/v1/sync/delta { hashes }
    Backend-->>Sync: DeltaSyncResponse { changed buckets }
    Sync->>Sync: _currentBundle.value = updatedBundle
    Note over Sync: Si hay cambios, MainScreen reacciona<br/>via LaunchedEffect(bundle)

    TRM->>TRM: scheduleNextRefresh(newToken) — programa siguiente ciclo
```

### Manejo de Errores en Refresh

```mermaid
flowchart TD
    A[performRefresh] --> B{getRefreshToken?}
    B -- null --> C[RefreshFailureReason.NoRefreshToken]
    B -- token --> D[repository.refresh]
    D --> E{Resultado}
    E -- Success --> F[saveToken + emit onRefreshSuccess]
    E -- Failure --> G{isRetryableError?}
    G -- Si: network/timeout/5xx --> H[delay con backoff + reintentar]
    H --> I{maxRetryAttempts?}
    I -- No --> D
    I -- Si: agotados --> J[RefreshFailureReason.NetworkError]
    G -- No: 401/expired/revoked --> K[RefreshFailureReason.TokenExpired]

    C --> L[AuthServiceImpl: clearSession]
    K --> L
    L --> M[_authState = Unauthenticated]
    L --> N[_onSessionExpired.emit]
    N --> O[App.kt: navState.replaceAll Login]

    J --> P[Solo log, no cierra sesion<br/>Reintentara en siguiente ciclo]
```

---

## Flujo: Splash — Restauracion de Sesion

```mermaid
sequenceDiagram
    participant UI as SplashScreen
    participant Auth as AuthServiceImpl
    participant Storage as SafeEduGoStorage
    participant TRM as TokenRefreshManager
    participant Sync as DataSyncService
    participant Backend as IAM Backend

    UI->>Auth: restoreSession()
    Auth->>Storage: getStringSafe("auth_token", "auth_user", "auth_context")

    alt Datos completos en storage
        Auth->>Auth: decode AuthToken, AuthUserInfo, UserContext

        alt Token no expirado
            Auth->>Auth: _authState = Authenticated(user, token, context)
            Auth->>TRM: startAutomaticRefresh(token)
        else Token expirado pero tiene refreshToken
            Auth->>TRM: forceRefresh()
            TRM->>Backend: POST /api/v1/auth/refresh
            alt Refresh exitoso
                Auth->>Auth: _authState = Authenticated(user, newToken, context)
                Auth->>TRM: startAutomaticRefresh(newToken)
            else Refresh fallido
                Auth->>Auth: clearAuthData()
                Auth->>Auth: _authState = Unauthenticated
            end
        else Sin refreshToken
            Auth->>Auth: clearAuthData()
            Auth->>Auth: _authState = Unauthenticated
        end
    else Datos incompletos
        Auth-->>UI: return (estado permanece Unauthenticated)
    end

    UI->>UI: isAuthenticated()?

    alt Autenticado
        par En paralelo con splash delay
            UI->>Sync: restoreFromLocal()
            Note over Sync: Carga bundle de storage local<br/>→ _currentBundle.value = bundle
            UI->>Sync: deltaSync()
            Note over Sync: Envio hashes, recibe solo cambios
        and
            UI->>UI: delay(splashMs)
        end
        UI->>UI: onNavigateToHome() → Route.Dashboard
    else No autenticado
        UI->>UI: delay(splashMs)
        UI->>UI: onNavigateToLogin() → Route.Login
    end
```

---

## Estructura de Permisos por Rol (Ejemplo)

```mermaid
graph TD
    subgraph super_admin
        SA1["schools:read, schools:create, schools:update, schools:delete"]
        SA2["users:read, users:create, users:update, users:delete"]
        SA3["subjects:read, subjects:create ..."]
        SA4["roles:read, roles:assign"]
    end

    subgraph admin_escuela["admin (por escuela)"]
        AD1["schools:read"]
        AD2["users:read, users:create, users:update"]
        AD3["subjects:read, subjects:create, subjects:update"]
        AD4["grades:read, grades:create, grades:update"]
    end

    subgraph teacher ["teacher (por escuela)"]
        TE1["subjects:read"]
        TE2["grades:read, grades:create, grades:update"]
        TE3["students:read"]
    end

    subgraph student ["student"]
        ST1["grades:read (propio)"]
        ST2["subjects:read"]
    end

    Note1["Los permisos vienen del sync bundle\nen currentBundle.permissions\ny del active_context en el JWT"]
    Note1 -.-> super_admin
    Note1 -.-> admin_escuela
    Note1 -.-> teacher
    Note1 -.-> student
```

---

## Almacenamiento de Contexto: 3 Keys + Sync Bundle

```mermaid
flowchart LR
    subgraph Storage ["SafeEduGoStorage"]
        K1["auth_token → AuthToken JSON"]
        K2["auth_user → AuthUserInfo JSON"]
        K3["auth_context → UserContext JSON"]
    end

    subgraph SyncStore ["LocalSyncStore"]
        S1["sync_bundle → UserDataBundle JSON"]
        S2["sync_hashes → Map hashes por bucket"]
    end

    AuthServiceImpl --> K1
    AuthServiceImpl --> K2
    AuthServiceImpl --> K3

    K1 --> |"restoreSession()"| AuthServiceImpl
    K2 --> |"restoreSession()"| AuthServiceImpl
    K3 --> |"restoreSession()"| AuthServiceImpl

    DataSyncService --> S1
    DataSyncService --> S2

    S1 --> |"restoreFromLocal()"| DataSyncService
    S2 --> |"deltaSync() envia hashes"| DataSyncService

    Note1["auth_context se actualiza en:\n- login\n- switchContext\n- token refresh (si backend envia nuevo)"]
    Note1 -.-> K3

    Note2["sync_bundle contiene:\nmenu, permissions, screens,\navailableContexts, hashes"]
    Note2 -.-> S1
```

---

## Contextos Disponibles: Origen y Uso

```mermaid
flowchart TD
    A[Login exitoso] --> B[LoginResponse.schools]
    B --> C{schools.size > 1?}
    C -- Si --> D[SchoolSelectionScreen]
    C -- No --> E[auto fullSync → Dashboard]

    D --> F[Usuario selecciona escuela]
    F --> G[switchContext + fullSync]
    G --> H[Dashboard]

    E --> H

    H --> I[MainScreen: bundle.availableContexts]
    I --> J{Quiere cambiar escuela?}
    J -- Si --> K{Es super_admin?}
    K -- Si --> L[SchoolSelectorScreen\nLista completa del admin API]
    K -- No --> M[ContextPickerOverlay\nLista desde bundle.availableContexts]

    L --> N[switchContext + fullSync]
    M --> N
    N --> O[Bundle se actualiza\n→ MainScreen reacciona via LaunchedEffect bundle]
```

---

## Diferencias por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Storage del token | `SharedPreferences` | `NSUserDefaults` | `java.util.prefs` | `localStorage` |
| Seguridad del token | No cifrado (texto plano en SharedPrefs) | No cifrado en NSUserDefaults | Legible en plist | Legible en localStorage |
| Keychain/Keystore | No implementado | No implementado | No implementado | N/A |
| Thread safety | `Mutex` de coroutines | `Mutex` de coroutines | `Mutex` de coroutines | Single thread (JS) |
| super_admin sin escuela | Redirige a SchoolSelector | Redirige a SchoolSelector | Redirige a SchoolSelector | Redirige a SchoolSelector |
| Token refresh | Automatico con schedule | Automatico con schedule | Automatico con schedule | Automatico con schedule |

---

## Mejoras Propuestas

| Mejora | Justificacion | Estado |
|--------|--------------|--------|
| Cifrar token en storage | Los tokens JWT son texto plano en SharedPrefs/NSUserDefaults; usar Android Keystore / iOS Keychain | Pendiente - Alta prioridad |
| Permisos cacheados por rol | Si el mismo rol+escuela se usa seguido, evitar round-trip al backend en cada login | IMPLEMENTADO — los permisos vienen en el sync bundle y se cachean localmente en `LocalSyncStore` |
| Offline permission check | Si hay cache del UserContext, permitir navegar sin red aunque no se pueda verificar expiracion | IMPLEMENTADO — `UserContext` cacheado en sync bundle via `restoreFromLocal()` sobrevive offline; `SplashScreen` restaura bundle local antes de intentar delta sync |
| Renovacion silenciosa pre-expiracion | `TokenRefreshManager` renueva 5 min antes de expirar, configurado via `TokenRefreshConfig.refreshThresholdSeconds` | IMPLEMENTADO — funcionando con schedule automatico, token rotation JWT, y delta sync post-refresh |
| Audit log local | Registrar en storage los cambios de contexto para debugging | Pendiente - Baja prioridad |
| Refresh token rotation | Cada refresh emite nuevo refresh token para invalidar el anterior | IMPLEMENTADO — `RefreshResponse.refreshToken` se usa si viene, sino se reutiliza el existente |
