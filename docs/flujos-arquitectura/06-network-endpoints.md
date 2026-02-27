# 06 — Conexion a Endpoints

## Clases Involucradas

```mermaid
classDiagram
    class EduGoHttpClient {
        +createPlatformEngine(): HttpClientEngine
        +create(): EduGoHttpClient
        +builder(): EduGoHttpClientBuilder
        +getSafe~T~(url, params): Result~T~
        +postSafe~T,R~(url, body): Result~T~
        +putSafe~T,R~(url, body): Result~T~
        +deleteSafe(url): Result~Unit~
    }
    class EduGoHttpClientBuilder {
        -interceptors: List~Interceptor~
        -retryConfig: RetryConfig?
        -loggingEnabled: Boolean
        +interceptor(i): Builder
        +retry(config): Builder
        +logging(enabled): Builder
        +build(): EduGoHttpClient
    }
    class Interceptor {
        <<interface>>
        +order: Int
        +interceptRequest(request)
        +interceptResponse(response)
    }
    class AuthInterceptor {
        -tokenProvider: TokenProvider
        -autoRefresh: Boolean
        +order = 20
        +interceptRequest: inject Authorization header
    }
    class HeadersInterceptor {
        +order = 10
        +interceptRequest: inject Content-Type
    }
    class TokenProvider {
        <<interface>>
        +getToken(): String?
        +isTokenExpired(): Boolean
        +refreshToken(): String?
    }
    class CircuitBreaker {
        -state: CircuitBreakerState
        -failureThreshold: Int
        -successThreshold: Int
        -openTimeout: Duration
        +execute~T~(block): Result~T~
    }
    class RetryPolicy {
        -maxAttempts: Int
        -initialDelay: Duration
        -multiplier: Double
        -retryOn: (Throwable) -> Boolean
        +execute~T~(block): Result~T~
    }
    class ExceptionMapper {
        +map(throwable): NetworkException
    }
    class NetworkException {
        <<sealed>>
        +Timeout, Unauthorized, Forbidden
        +NotFound, ServerError, NoConnectivity
        +Unknown(message)
    }
    class NetworkObserver {
        <<interface>>
        +status: StateFlow~NetworkStatus~
        +isOnline: Boolean
        +start()
        +stop()
    }

    EduGoHttpClientBuilder --> Interceptor : agrega
    EduGoHttpClient --> EduGoHttpClientBuilder
    AuthInterceptor ..|> Interceptor
    HeadersInterceptor ..|> Interceptor
    AuthInterceptor --> TokenProvider
    EduGoHttpClient --> ExceptionMapper
    ExceptionMapper --> NetworkException
    CircuitBreaker --> RetryPolicy
```

---

## Flujo Secuencial: Request HTTP Autenticado

```mermaid
sequenceDiagram
    participant App as RemoteDataLoader
    participant Client as EduGoHttpClient
    participant Chain as InterceptorChain
    participant HI as HeadersInterceptor (order=10)
    participant AI as AuthInterceptor (order=20)
    participant TP as TokenProvider (AuthService)
    participant Ktor as Ktor Engine
    participant Backend as Backend API

    App->>Client: getSafe("/api/v1/schools", params)

    Client->>Chain: execute(request)
    Chain->>HI: interceptRequest(request)
    HI->>HI: request.header("Content-Type", "application/json")

    Chain->>AI: interceptRequest(request)
    AI->>TP: getToken()
    TP-->>AI: "eyJhbGc..."

    alt Token proximo a expirar
        AI->>TP: isTokenExpired()
        TP-->>AI: true (o dentro de threshold)
        AI->>TP: refreshToken()
        TP->>Backend: POST /api/v1/auth/refresh (JWT-based refresh token)
        Backend-->>TP: { access_token, refresh_token, active_context }
        Note over TP: Token rotation: nuevo refresh_token en cada refresh
        TP->>TP: Guardar nuevo token + refresh_token + context
        TP-->>AI: nuevo access token string
    end

    AI->>AI: request.header("Authorization", "Bearer ${token}")
    Chain-->>Client: request con headers

    Client->>Ktor: execute(request)
    Ktor->>Backend: HTTP GET /api/v1/schools?page=1

    alt Respuesta 200
        Backend-->>Ktor: 200 { items: [...] }
        Ktor-->>Client: HttpResponse 200
        Client->>Client: json.decodeFromString
        Client-->>App: Result.Success(DataPage)
    else Respuesta 401
        Backend-->>Ktor: 401 Unauthorized
        Ktor-->>Client: HttpResponse 401
        Client->>ExceptionMapper: map(ResponseException 401)
        ExceptionMapper-->>Client: NetworkException.Unauthorized
        Client-->>App: Result.Failure("401: ...")
    else Red no disponible
        Ktor-->>Client: ConnectException / timeout
        Client->>ExceptionMapper: map(IOException)
        ExceptionMapper-->>Client: NetworkException.NoConnectivity
        Client-->>App: Result.Failure("Sin conexion")
    end
```

---

## Endpoints de Sincronizacion (Sync Bundle)

El sistema de sincronizacion centraliza la descarga de datos del usuario (menu, permisos, pantallas, contextos) en un unico bundle via el IAM Platform API.

```mermaid
sequenceDiagram
    participant App as DataSyncService
    participant Repo as SyncRepository
    participant Http as EduGoHttpClient (autenticado)
    participant IAM as IAM Platform API

    Note over App: fullSync() - Primera carga o cambio de contexto
    App->>Repo: getBundle()
    Repo->>Http: GET /api/v1/sync/bundle
    Http->>IAM: GET /api/v1/sync/bundle
    IAM-->>Http: SyncBundleResponse { menu, permissions, screens, available_contexts, hashes }
    Http-->>Repo: Result.Success(SyncBundleResponse)
    Repo-->>App: Result.Success(...)
    App->>App: mapBundleResponse → UserDataBundle
    App->>App: store.saveBundle(bundle)
    App->>App: seedScreenLoader(screens)

    Note over App: deltaSync() - Splash / reconexion
    App->>Repo: deltaSync(hashes)
    Repo->>Http: POST /api/v1/sync/delta { hashes: Map }
    Http->>IAM: POST /api/v1/sync/delta
    IAM-->>Http: DeltaSyncResponse { changed: Map, unchanged: List }
    Http-->>Repo: Result.Success(DeltaSyncResponse)
    Repo-->>App: Result.Success(...)
    App->>App: applyDeltaToBundle(base, changed, now) — construye bundle actualizado en memoria
    App->>App: persistDeltaChanges(changed) — persiste solo los buckets modificados a storage
    App->>App: seedScreenLoader(changedScreens) — solo screens que cambiaron
```

### Endpoints de sync registrados

| Endpoint | Metodo | API | Descripcion |
|----------|--------|-----|-------------|
| `/api/v1/sync/bundle` | GET | IAM Platform | Bundle completo: menu, permisos, screens, contextos disponibles, hashes por bucket |
| `/api/v1/sync/delta` | POST | IAM Platform | Sync delta: envia `{ hashes: Map<String, String> }` con hashes locales. Retorna solo buckets que cambiaron |
| `/screen-config/version/{key}` | GET | IAM Platform | Verificacion ligera de version de un screen (retorna version + updatedAt) |

### Parametros de los endpoints de sync

#### GET `/api/v1/sync/bundle`
| Header | Tipo | Requerido | Descripcion |
|--------|------|:---------:|-------------|
| `Authorization` | `Bearer {jwt}` | Si | Token JWT del usuario autenticado |

**Respuesta:** `SyncBundleResponse`
```json
{
  "menu": [
    { "name": "Escuelas", "icon": "school", "screen_key": "schools-list", "permissions": ["schools.read"], "children": [] }
  ],
  "permissions": ["schools.read", "schools.write", "students.read"],
  "screens": {
    "schools-list": {
      "screen_key": "schools-list",
      "screen_name": "Lista de Escuelas",
      "pattern": "list",
      "version": 3,
      "template": { "zones": ["..."] },
      "slot_data": { "page_title": "Escuelas" },
      "data_config": { "endpoint": "admin:/api/v1/schools", "field_mapping": {}, "pagination": {} },
      "handler_key": "schools",
      "updated_at": "2026-02-25T10:30:00Z"
    }
  },
  "available_contexts": [
    { "role_id": "uuid", "role_name": "admin", "school_id": "uuid", "school_name": "Colegio ABC", "permissions": ["..."] }
  ],
  "hashes": {
    "menu": "a1b2c3...",
    "permissions": "d4e5f6...",
    "screen:schools-list": "g7h8i9...",
    "available_contexts": "j0k1l2..."
  }
}
```

**Nota:** El backend construye el bundle en paralelo (errgroup de Go) con 4 goroutines: menu, permisos, contextos, y screens. Las screens se resuelven secuencialmente dentro de su goroutine.

#### POST `/api/v1/sync/delta`
| Header | Tipo | Requerido | Descripcion |
|--------|------|:---------:|-------------|
| `Authorization` | `Bearer {jwt}` | Si | Token JWT del usuario autenticado |

**Request body:**
```json
{
  "hashes": {
    "menu": "a1b2c3...",
    "permissions": "d4e5f6...",
    "screen:schools-list": "g7h8i9...",
    "screen:schools-form": "m3n4o5...",
    "available_contexts": "j0k1l2..."
  }
}
```

**Respuesta:** `DeltaSyncResponse`
```json
{
  "changed": {
    "menu": {
      "data": [{ "name": "Escuelas" }],
      "hash": "x1y2z3..."
    },
    "screen:schools-list": {
      "data": { "screen_key": "schools-list", "pattern": "list" },
      "hash": "p6q7r8..."
    }
  },
  "unchanged": ["permissions", "available_contexts", "screen:schools-form"]
}
```

**Nota:** Solo retorna en `changed` los buckets cuyo hash difiere. El servidor recalcula el bundle completo internamente y compara hashes SHA256.

#### GET `/api/v1/screen-config/version/{screenKey}`
| Header | Tipo | Requerido | Descripcion |
|--------|------|:---------:|-------------|
| `Authorization` | `Bearer {jwt}` | Si | Token JWT del usuario autenticado |

| Path param | Tipo | Descripcion |
|------------|------|-------------|
| `screenKey` | `String` | Clave unica del screen (ej: `schools-list`) |

**Respuesta:**
```json
{
  "version": 3,
  "updated_at": "2026-02-25T10:30:00Z"
}
```

### Modelo de respuesta del bundle

```
SyncBundleResponse {
    menu: List<MenuItem>           // Items del menu lateral
    permissions: List<String>      // Permisos del contexto activo (ej: "schools.read")
    screens: Map<String, ScreenBundleEntry>  // Clave = screenKey
    available_contexts: List<UserContext>     // Escuelas/roles disponibles
    hashes: Map<String, String>              // Hash por bucket para delta sync
}
```

---

## Calculo de Hashes: Mecanismo del Delta Sync

El delta sync se basa en comparar hashes SHA256 entre cliente y servidor.
**Los hashes no se persisten en base de datos** — se recalculan en cada request.

### Funciones de hash en el backend (sync_service.go)

```mermaid
flowchart TD
    subgraph hashJSON ["hashJSON(v interface{})"]
        HJ1["json.Marshal(v)"] --> HJ2["sha256.Sum256(bytes)"]
        HJ2 --> HJ3["hex string"]
    end

    subgraph hashPermissions ["hashPermissions(permissions []string)"]
        HP1["sort.Strings(copy)"] --> HP2["strings.Join(sorted, ',')"]
        HP2 --> HP3["sha256.Sum256(joined)"]
        HP3 --> HP4["hex string"]
    end

    subgraph hashScreen ["hashScreen(version int, updatedAt string)"]
        HS1["fmt.Sprintf('%d:%s', version, updatedAt)"]
        HS1 --> HS2["sha256.Sum256(formatted)"]
        HS2 --> HS3["hex string"]
    end
```

### Que hashea cada bucket

| Bucket key | Funcion | Input | Fuente en BD | Determinista? |
|------------|---------|-------|-------------|:---:|
| `menu` | `hashJSON()` | `[]MenuItemDTO` serializado | `iam.resources` con `ORDER BY sort_order` | ✅ |
| `permissions` | `hashPermissions()` | Permisos **ordenados** y unidos por coma | `iam.permissions` via `SELECT DISTINCT` | ✅ (ordena antes) |
| `available_contexts` | `hashJSON()` | `[]UserContextDTO` serializado | `iam.user_roles` **sin ORDER BY** | ❌ |
| `screen:{key}` | `hashScreen()` | `"{version}:{updated_at}"` | `ui_config.screen_templates.version` + `screen_instances.updated_at` | ✅ |

### Flujo del delta sync en el servidor

```mermaid
sequenceDiagram
    participant Client as KMP Client
    participant Handler as SyncHandler
    participant Service as SyncService
    participant DB as PostgreSQL

    Client->>Handler: POST /api/v1/sync/delta { hashes: { "menu": "abc", "permissions": "def", ... } }
    Handler->>Service: GetDeltaSync(userID, activeContext, clientHashes)

    Note over Service: Paso 1: Recalcular COMPLETO el bundle del servidor
    Service->>DB: Fetch menu, permissions, contexts, ALL screens (en paralelo con errgroup)
    DB-->>Service: datos completos

    Note over Service: Paso 2: Calcular hashes del servidor
    Service->>Service: serverHashes = { "menu": hashJSON(menu), "permissions": hashPermissions(perms), ... }

    Note over Service: Paso 3: Comparar con hashes del cliente
    loop Para cada bucket
        alt clientHash == serverHash
            Service->>Service: unchanged.append(key)
        else clientHash != serverHash
            Service->>Service: changed[key] = BucketData{ data, newHash }
        end
    end

    Service-->>Handler: DeltaSyncResponse { changed, unchanged }
    Handler-->>Client: JSON response (solo buckets que cambiaron)
```

### Cuando cambia un hash (deteccion de cambios)

```mermaid
flowchart LR
    subgraph menu ["Hash de 'menu' cambia si:"]
        M1["Se agrega/elimina item de menu en BD"]
        M2["Se cambia sort_order de un item"]
        M3["Se cambia label/icon/screen_key"]
    end

    subgraph perms ["Hash de 'permissions' cambia si:"]
        P1["Se asigna/revoca un permiso al rol"]
        P2["Se cambia de rol el usuario"]
        P3["Se agrega/elimina role_permission"]
    end

    subgraph screen ["Hash de 'screen:key' cambia si:"]
        S1["Se modifica slot_data del screen_instance\n(trigger actualiza updated_at)"]
        S2["Se incrementa version en screen_template\n(manual, no automatico)"]
    end

    subgraph contexts ["Hash de 'available_contexts' cambia si:"]
        C1["Se agrega/remueve un user_role"]
        C2["Cambia la escuela o rol activo"]
        C3["⚠ O si PostgreSQL retorna filas en orden distinto\n(bug: query sin ORDER BY)"]
    end
```

### ⚠ Bug conocido: Hashes no deterministas

El hash de `available_contexts` **no es estable entre reinicios de la API** porque:

1. **`FindByUser()` en user_role_repository** no tiene `ORDER BY` — PostgreSQL puede retornar filas en cualquier orden
2. **`GetUserPermissions()` SQL** usa `SELECT DISTINCT` sin `ORDER BY` — el array de permisos dentro de cada contexto varia
3. **`hashJSON()` no ordena** — serializa el slice tal como llega

**Consecuencia:** tras un reinicio del API (o incluso entre requests), el hash de `available_contexts` puede cambiar sin que los datos hayan cambiado realmente, causando descargas innecesarias en el delta sync.

**Fix requerido (3 cambios de una linea):**

```
1. iam_repositories.go FindByUser():     agregar .Order("id ASC")
2. iam_repositories.go GetUserPermissions(): agregar ORDER BY p.name
3. sync_service.go:  ordenar contexts slice antes de hashJSON()
```

### Nota de rendimiento

El delta sync **no es un delta real a nivel de servidor**: recalcula todo el bundle completo y solo ahorra bandwidth al cliente. Para escalar, considerar:
- Cache de hashes en Redis (Upstash) con TTL de 5 min
- Invalidar cache via evento cuando hay escrituras (POST/PUT/DELETE)
- O persistir hashes en una tabla `sync_hashes(user_id, bucket_key, hash, updated_at)`

---

## Token Refresh: JWT con Rotacion

El sistema de refresh tokens usa tokens JWT (no random base64). Cada refresh genera un nuevo par de tokens.

```mermaid
sequenceDiagram
    participant TRM as TokenRefreshManagerImpl
    participant Repo as AuthRepository
    participant Backend as IAM Platform
    participant Storage as SafeEduGoStorage

    TRM->>TRM: shouldRefresh(token)? → true
    TRM->>Repo: refresh(refreshToken)
    Repo->>Backend: POST /api/v1/auth/refresh { refresh_token: "jwt..." }
    Backend-->>Repo: RefreshResponse { access_token, expires_in, token_type, refresh_token, active_context }
    Repo-->>TRM: Result.Success(RefreshResponse)

    TRM->>TRM: refreshResponse.toAuthToken(existingRefreshToken)
    Note over TRM: Si refresh_token viene en respuesta,<br/>usa el nuevo; si no, mantiene el anterior

    TRM->>Storage: putStringSafe("auth_token", newTokenJson)

    alt active_context presente en respuesta
        TRM->>Storage: putStringSafe("auth_context", contextJson)
    end

    TRM->>TRM: emitir onRefreshSuccess
```

### RefreshResponse — Campos clave

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| `access_token` | `String` | Nuevo JWT de acceso |
| `expires_in` | `Int` | Segundos hasta expiracion |
| `token_type` | `String` | Siempre "Bearer" |
| `refresh_token` | `String?` | **Nuevo refresh token** (rotacion). Si viene, reemplaza al anterior |
| `active_context` | `UserContext?` | Contexto activo actualizado (si hubo cambios server-side) |

---

## NetworkObserver: Conectividad Multiplataforma

`NetworkObserver` esta registrado en `networkModule` via la funcion `createNetworkObserver()` (expect/actual por plataforma).

```mermaid
flowchart TD
    createNetworkObserver{"createNetworkObserver()"}

    createNetworkObserver --"Android"--> CM["ConnectivityManager\nregisterDefaultNetworkCallback"]
    createNetworkObserver --"iOS"--> NW["NWPathMonitor\n(Network framework)"]
    createNetworkObserver --"Desktop/JVM"--> Poll["Coroutine-based\nHTTP health-check polling"]
    createNetworkObserver --"WasmJS"--> Nav["navigator.onLine\n+ online/offline events"]

    subgraph Interfaz
        NO["NetworkObserver"]
        NO --> |"status"| SF["StateFlow<NetworkStatus>"]
        NO --> |"isOnline"| Bool["Boolean (derived)"]
        NO --> |"start()"| Start["Iniciar observacion"]
        NO --> |"stop()"| Stop["Detener y liberar recursos"]
    end

    subgraph Estados ["NetworkStatus"]
        S1["AVAILABLE"]
        S2["UNAVAILABLE"]
        S3["LOSING"]
    end
```

### Registro en DI

```kotlin
// networkModule
single<NetworkObserver> { createNetworkObserver() }
```

Consumidores principales: `CachedScreenLoader`, `CachedDataLoader`, `DynamicScreenViewModel`, `SyncEngine`, `ConnectivitySyncManager`.

---

## Motor HTTP por Plataforma

```mermaid
flowchart TD
    createPlatformEngine{createPlatformEngine}

    createPlatformEngine --"Android"--> OkHttp
    createPlatformEngine --"iOS"--> Darwin["Darwin (NSURLSession)"]
    createPlatformEngine --"Desktop/JVM"--> CIO["CIO (Coroutine IO)"]
    createPlatformEngine --"WasmJS"--> Fetch["Fetch (browser fetch API)"]

    OkHttp --> |caracteristicas| OK1["- HTTP/2, connection pooling\n- Certificate pinning posible\n- OkHttp interceptors si necesario"]
    Darwin --> |caracteristicas| D1["- NSURLSession nativo iOS/macOS\n- ATS (App Transport Security)\n- Sin HTTP/2 custom en KMP"]
    CIO --> |caracteristicas| C1["- Pure Kotlin coroutines\n- Sin dependencias nativas\n- Bueno para testing"]
    Fetch --> |caracteristicas| F1["- API Fetch del navegador\n- CORS aplica\n- No hay acceso a raw sockets"]
```

---

## Multi-API Routing: Resolucion de URLs

```mermaid
flowchart LR
    subgraph Contrato ["ScreenContract.endpointFor()"]
        E1["admin:/api/v1/schools"]
        E2["iam:/api/v1/menu"]
        E3["mobile:/api/v1/profile"]
        E4["/api/v1/screen-config/..."]
    end

    subgraph Resolver ["RemoteDataLoader.resolveBaseUrl()"]
        R{prefijo?}
    end

    subgraph BaseURLs ["AppConfig (por environment)"]
        U1["adminBaseUrl\nhttps://admin-api.staging.edugo.app"]
        U2["iamBaseUrl\nhttps://iam.staging.edugo.app"]
        U3["mobileBaseUrl\nhttps://mobile-api.staging.edugo.app"]
    end

    E1 --> R
    E2 --> R
    E3 --> R
    E4 --> R

    R --"admin:"--> U1
    R --"iam:"--> U2
    R --"mobile:"--> U3
    R --"sin prefijo"--> U3
```

---

## CircuitBreaker: Maquina de Estados

```mermaid
stateDiagram-v2
    [*] --> Closed : inicio

    state Closed {
        [*] --> Operando
        Operando --> ContandoFallos : fallo
        ContandoFallos --> Operando : exito (reset contador)
    }

    Closed --> Open : fallos >= failureThreshold
    note right of Open : Rechaza inmediatamente\nsin llamar al backend

    Open --> HalfOpen : openTimeout transcurrido

    state HalfOpen {
        [*] --> PruebaUnica
        PruebaUnica --> ContandoExitos : exito
    }

    HalfOpen --> Closed : exitos >= successThreshold
    HalfOpen --> Open : fallo en prueba
```

---

## Configuracion por Ambiente

```mermaid
graph TD
    subgraph LOCAL
        L1["adminBaseUrl: http://localhost:8081"]
        L2["iamBaseUrl: http://localhost:8080"]
        L3["mobileBaseUrl: http://localhost:8082"]
        L4["logging: true\nretryEnabled: false"]
    end

    subgraph STAGING
        S1["adminBaseUrl: https://admin-api.staging.edugo.app"]
        S2["iamBaseUrl: https://iam.staging.edugo.app"]
        S3["mobileBaseUrl: https://mobile-api.staging.edugo.app"]
        S4["logging: true\nretryEnabled: true"]
    end

    subgraph PROD
        P1["adminBaseUrl: https://admin-api.edugo.app"]
        P2["iamBaseUrl: https://iam.edugo.app"]
        P3["mobileBaseUrl: https://mobile-api.edugo.app"]
        P4["logging: false\nretryEnabled: true"]
    end

    AppConfig --> |"forEnvironment(LOCAL)"| LOCAL
    AppConfig --> |"forEnvironment(STAGING)"| STAGING
    AppConfig --> |"forEnvironment(PROD)"| PROD
```

---

## Manejo de Errores: ExceptionMapper

```mermaid
flowchart TD
    Exception["Throwable capturado"] --> EM[ExceptionMapper.map]

    EM --> |"ResponseException 401"| UE[NetworkException.Unauthorized]
    EM --> |"ResponseException 403"| FE[NetworkException.Forbidden]
    EM --> |"ResponseException 404"| NF[NetworkException.NotFound]
    EM --> |"ResponseException 5xx"| SE[NetworkException.ServerError]
    EM --> |"HttpRequestTimeoutException"| TE[NetworkException.Timeout]
    EM --> |"IOException / ConnectException"| NC[NetworkException.NoConnectivity]
    EM --> |"cualquier otro"| UK[NetworkException.Unknown message]

    UE --> |"EventOrchestrator"| Logout[EventResult.Logout]
    FE --> |"EventOrchestrator"| PermDenied[EventResult.PermissionDenied]
    SE --> |"EventOrchestrator"| RetryError[EventResult.Error retry=true]
    TE --> |"EventOrchestrator"| RetryError
    NC --> |"EventOrchestrator"| RetryError
    NF --> |"EventOrchestrator"| NoRetryError[EventResult.Error retry=false]
```

---

## Dos Instancias de HttpClient (DI)

```mermaid
graph LR
    subgraph Koin DI
        C1["HttpClient named 'plainHttp'\n- Sin AuthInterceptor\n- Sin retry\n- Solo headers basicos"]
        C2["HttpClient (default)\n- Con AuthInterceptor\n- Con retry policy\n- Con logging en DEBUG"]
    end

    AuthRepo["AuthRepositoryImpl\n(login, refresh)\n→ usa plainHttp"]
    DynRepo["RemoteDataLoader\nMenuRepositoryImpl\nScreenLoader\n→ usa default (autenticado)"]
    SyncRepo["SyncRepositoryImpl\n(bundle, delta)\n→ usa default (autenticado)\napunta a IAM API"]
    SyncEng["SyncEngine\n→ RemoteDataLoader propio\n(instancia directa, no cached)\npara procesar MutationQueue"]

    C1 --> AuthRepo
    C2 --> DynRepo
    C2 --> SyncRepo
    C2 --> SyncEng
```

### Detalle de instancias en DynamicUiModule

| Componente | HttpClient | DataLoader | Nota |
|------------|-----------|------------|------|
| `RemoteDataLoader` (singleton via `DataLoader`) | default (autenticado) | `CachedDataLoader` wrapping `RemoteDataLoader` | Para pantallas dinamicas, con cache |
| `SyncRepositoryImpl` | default (autenticado) | N/A (usa httpClient directo) | Apunta a `iamApiBaseUrl` para bundle/delta |
| `SyncEngine` | default (autenticado) | `RemoteDataLoader` propio (sin cache) | Instancia separada para ejecutar MutationQueue sin pasar por cache |
| `CachedScreenLoader` | default (autenticado) | N/A | Via `RemoteScreenLoader` interno |

---

## Offline Queue: MutationQueue + SyncEngine

El sistema de offline queue permite encolar operaciones de escritura (SAVE_NEW, SAVE_EXISTING) cuando no hay red y procesarlas automaticamente al reconectar.

```mermaid
sequenceDiagram
    participant UI as DynamicScreenViewModel
    participant MQ as MutationQueue
    participant Storage as SafeEduGoStorage
    participant CSM as ConnectivitySyncManager
    participant SE as SyncEngine
    participant NO as NetworkObserver
    participant Remote as RemoteDataLoader
    participant API as Backend API

    Note over UI: Usuario guarda form sin conexion
    UI->>MQ: enqueue(PendingMutation)
    MQ->>MQ: Deduplicar, validar limite (max=50)
    MQ->>Storage: persistir cola en "offline.queue.mutations"

    Note over NO: Red se restablece
    NO-->>CSM: NetworkStatus.AVAILABLE
    CSM->>SE: processQueue()

    loop Por cada PendingMutation PENDING
        SE->>MQ: dequeue() → item
        SE->>Remote: submitData(endpoint, body, method)
        Remote->>API: POST/PUT endpoint

        alt 200 OK
            API-->>Remote: Success
            SE->>MQ: remove(id)
        else 409 Conflict
            SE->>SE: conflictResolver.resolve(item)
            SE->>MQ: markConflicted(id) o remove(id)
        else Error de red
            SE->>SE: retryWithBackoff (1s, 2s, 4s)
            SE->>MQ: markFailed(id) si agota reintentos
        end
    end

    CSM->>CSM: Invalida cache de screens recientes
    CSM-->>UI: SyncManagerEvent.ReloadCurrentScreen
```

### Componentes del sistema offline

| Clase | Responsabilidad |
|-------|----------------|
| `PendingMutation` | Modelo de una mutacion pendiente: id, endpoint, method, body, status, retryCount |
| `MutationQueue` | Cola FIFO persistida en storage. Deduplicacion, limite de 50 items. Expone `pendingCount: StateFlow<Int>` |
| `SyncEngine` | Procesa la cola al reconectar. Retry con backoff exponencial (1s, 2s, 4s). Resolucion de conflictos 409/404 |
| `ConflictResolver` | Decide que hacer con mutaciones en conflicto: `RetryWithoutCheck`, `Skip`, o `Failed` |
| `ConnectivitySyncManager` | Orquesta reconexion: procesa queue → invalida cache de screens recientes → notifica UI para reload |

---

## Diferencias por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Engine | OkHttp | Darwin (NSURLSession) | CIO | Fetch API |
| HTTP/2 | Si (OkHttp) | Si (NSURLSession) | No por defecto | Depende del browser |
| Timeout de conexion | 30s configurable | 30s configurable | 30s configurable | Depende de browser |
| Certificados personalizados | Si (OkHttp trust manager) | Si (URLSessionDelegate) | Si (JVM TrustManager) | No (solo certs del browser) |
| CORS | N/A | N/A | N/A | Requerido por browser |
| Background requests | Si (WorkManager si aplica) | Si (Background URLSession) | Si (coroutines) | Solo en Service Workers |
| Request cancellation | Ktor cancel via Job | Ktor cancel via Job | Ktor cancel via Job | Ktor cancel (abort signal) |
| NetworkObserver | ConnectivityManager callback | NWPathMonitor | HTTP health-check polling | navigator.onLine + events |

---

## Mejoras Propuestas

| Mejora | Justificacion | Estado |
|--------|--------------|--------|
| Offline queue | Encolar SAVE_NEW/SAVE_EXISTING cuando no hay red y enviar al reconectar | **IMPLEMENTADO** (MutationQueue + SyncEngine + ConnectivitySyncManager) |
| Certificate pinning en Android/iOS | Proteccion contra MITM en redes publicas | Pendiente (prioridad alta) |
| Request deduplication | Si dos pantallas piden el mismo endpoint simultaneamente, evitar doble request | Pendiente (prioridad media) |
| Response caching HTTP (ETag/Last-Modified) | Usar cabeceras HTTP estandar de cache para screens y datos | Pendiente (prioridad media) |
| Timeout diferenciado por tipo de request | Login puede tolerar mas espera que una busqueda | Pendiente (prioridad media) |
| Request metrics (latencia, bytes) | Para observabilidad y optimizacion en produccion | Pendiente (prioridad baja) |
| Compression (gzip) | Las respuestas JSON de listas pueden ser grandes | Pendiente (prioridad media) |
