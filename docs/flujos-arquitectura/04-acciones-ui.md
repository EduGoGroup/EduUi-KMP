# 04 — Acciones de la UI (ScreenEvent + EventOrchestrator)

## Clases Involucradas

```mermaid
classDiagram
    class ScreenEvent {
        <<enum>>
        LOAD_DATA
        SAVE_NEW
        SAVE_EXISTING
        DELETE
        SEARCH
        SELECT_ITEM
        REFRESH
        LOAD_MORE
        CREATE
    }
    class EventContext {
        +screenKey: String
        +userContext: UserContext?
        +fieldValues: Map~String,String~
        +params: Map~String,String~
        +query: String?
        +itemId: String?
        +selectedItem: JsonObject?
    }
    class EventResult {
        <<sealed>>
        +Success(message, data)
        +NavigateTo(screenKey, params)
        +Error(message, retry)
        +PermissionDenied
        +Logout
        +Cancelled
        +NoOp
        +SubmitTo(endpoint, method, fieldValues)
    }
    class ScreenContract {
        <<interface>>
        +screenKey: String
        +resource: String
        +endpointFor(event, context): String?
        +permissionFor(event): String?
        +dataConfig(): DataConfig?
        +customEventHandlers(): Map~String,CustomEventHandler~
    }
    class BaseCrudContract {
        <<abstract>>
        +apiPrefix: String
        +basePath: String
        +endpointFor() default impl
        +permissionFor() default impl
    }
    class ScreenContractRegistry {
        -contracts: Map~String,ScreenContract~
        +register(contract)
        +find(screenKey): ScreenContract?
    }
    class EventOrchestrator {
        -registry: ScreenContractRegistry
        -dataLoader: CachedDataLoader
        -userContextProvider: () -> UserContext?
        +execute(key, event, context): EventResult
        +executeCustom(key, eventId, context): EventResult
    }
    class CustomEventHandler {
        <<interface>>
        +eventId: String
        +requiredPermission: String?
        +execute(context): EventResult
    }
    class NetworkObserver {
        <<interface>>
        +status: StateFlow~NetworkStatus~
        +isOnline: Boolean
        +start()
        +stop()
    }
    class MutationQueue {
        -storage: SafeEduGoStorage
        -mutations: MutableList~PendingMutation~
        +pendingCount: StateFlow~Int~
        +enqueue(mutation): Boolean
        +dequeue(): PendingMutation?
        +markFailed(id)
        +markConflicted(id)
        +remove(id)
        +getAll(): List~PendingMutation~
    }
    class SyncEngine {
        -mutationQueue: MutationQueue
        -remoteDataLoader: DataLoader
        -networkObserver: NetworkObserver
        -conflictResolver: ConflictResolver
        +syncState: StateFlow~SyncState~
        +start(scope)
        +processQueue()
    }
    class ConnectivitySyncManager {
        -syncEngine: SyncEngine
        -recentScreenTracker: RecentScreenTracker
        -screenLoader: CachedScreenLoader
        +events: SharedFlow~SyncManagerEvent~
        +start(scope)
    }

    ScreenContract <|-- BaseCrudContract
    BaseCrudContract <|-- SchoolsListContract
    BaseCrudContract <|-- SchoolsFormContract
    BaseCrudContract <|-- SubjectsListContract
    BaseCrudContract <|-- SubjectsFormContract
    ScreenContractRegistry "1" --> "*" ScreenContract
    EventOrchestrator --> ScreenContractRegistry
    EventOrchestrator --> EventResult : produce
    EventOrchestrator --> CachedDataLoader : delega submit/load
    ScreenContract --> ScreenEvent : mapea
    ScreenContract --> CustomEventHandler : registra
    CachedDataLoader --> NetworkObserver : consulta isOnline
    CachedDataLoader --> MutationQueue : enqueue offline
    SyncEngine --> MutationQueue : procesa cola
    SyncEngine --> NetworkObserver : detecta reconexion
    ConnectivitySyncManager --> SyncEngine : dispara sync
```

---

## Flujo Secuencial: Evento de Carga de Datos (LOAD_DATA)

```mermaid
sequenceDiagram
    participant DS as DynamicScreen
    participant VM as DynamicScreenViewModel
    participant EO as EventOrchestrator
    participant Reg as ScreenContractRegistry
    participant Con as SchoolsListContract
    participant DL as CachedDataLoader
    participant NO as NetworkObserver
    participant Backend as Admin API

    DS->>VM: LaunchedEffect → executeEvent(screenKey, LOAD_DATA, context)
    VM->>EO: execute(screenKey, LOAD_DATA, context)

    EO->>Reg: find(screenKey)
    Reg-->>EO: SchoolsListContract
    EO->>Con: permissionFor(LOAD_DATA)
    Con-->>EO: "schools:read"
    EO->>EO: userContext.hasPermission("schools:read")

    EO->>Con: endpointFor(LOAD_DATA, context)
    Con-->>EO: "admin:/api/v1/schools"
    EO->>Con: dataConfig()
    Con-->>EO: DataConfig(pagination, fieldMapping, searchFields)

    EO->>DL: loadData("admin:/api/v1/schools", config, params)
    DL->>NO: isOnline?

    alt Online
        DL->>Backend: GET /api/v1/schools?page=1&pageSize=20
        Backend-->>DL: { items: [...], total: 45, page: 1 }
        DL->>DL: updateMemoryCache + persistToStorage
        DL-->>EO: DataPage(items, hasMore=true)
    else Offline - hay cache
        DL->>DL: memoryCache[key] ?? loadFromStorage(key)
        DL-->>EO: DataPage (datos en cache, isStale=true)
    else Offline - sin cache
        DL-->>EO: Result.Failure("Sin conexión y sin datos en caché")
    end

    EO-->>VM: EventResult.Success(data=DataPage)
    VM->>VM: _dataState.value = DataState.Success(items)
    VM-->>DS: dataState actualizado
    DS->>DS: re-renderiza lista + StaleDataIndicator si isStale
```

---

## Flujo Secuencial: Guardar Formulario (FORM SAVE)

```mermaid
sequenceDiagram
    participant Toolbar as DynamicToolbar
    participant DS as DynamicScreen
    participant VM as DynamicScreenViewModel
    participant EO as EventOrchestrator
    participant Con as SchoolsFormContract
    participant DL as CachedDataLoader
    participant NO as NetworkObserver
    participant MQ as MutationQueue
    participant Backend as Admin API

    Toolbar->>DS: onEvent(SAVE_NEW)
    DS->>VM: executeCustomEvent(screenKey, "submit-form", context)

    VM->>EO: executeCustom("schools-form", "submit-form", context)
    EO->>Con: customEventHandlers()["submit-form"]
    Con-->>EO: SubmitFormHandler
    EO->>Con: SubmitFormHandler.execute(context)
    Con-->>EO: EventResult.SubmitTo(endpoint="admin:/api/v1/schools", method="POST", fieldValues)

    EO-->>VM: EventResult.SubmitTo
    VM->>VM: filtrar solo campos de FORM_SECTION zones
    VM->>VM: convertir tipos (String→Int/Bool segun controlType)
    VM->>DL: submitData("admin:/api/v1/schools", body, "POST")
    DL->>NO: isOnline?

    alt Online - exito
        DL->>Backend: POST /api/v1/schools { name, city, code, ... }
        Backend-->>DL: 201 Created { id: "uuid", ... }
        DL->>DL: invalidateByPrefix("/api/v1/schools")
        DL-->>VM: Result.Success(JsonObject)
        VM-->>DS: EventResult.Success
        DS->>DS: mostrar Snackbar exito
        DS->>DS: onBack() → volver a schools-list
    else Online - fallo de red (con MutationQueue)
        DL->>Backend: POST /api/v1/schools { ... }
        Backend--xDL: Error de red / timeout
        DL->>MQ: enqueue(PendingMutation)
        MQ->>MQ: pendingCount++
        DL-->>VM: Result.Success(null)
        VM->>VM: detecta data==null → guardado local
        VM-->>DS: Snackbar "Guardado localmente, se sincronizará al reconectar"
        DS->>DS: onBack() → volver a schools-list
    else Offline (con MutationQueue)
        DL->>MQ: enqueue(PendingMutation)
        MQ->>MQ: pendingCount++
        DL-->>VM: Result.Success(null)
        VM->>VM: detecta data==null → guardado local
        VM-->>DS: Snackbar "Guardado localmente, se sincronizará al reconectar"
        DS->>DS: onBack() → volver a schools-list
    else Offline (sin MutationQueue)
        DL-->>VM: Result.Failure("Sin conexión")
        VM-->>DS: mostrar error
    end

    Note over MQ,DS: ConnectivityBanner muestra pendingCount mientras haya mutaciones encoladas
```

---

## Flujo: Sincronizacion al Reconectar

```mermaid
sequenceDiagram
    participant NO as NetworkObserver
    participant CSM as ConnectivitySyncManager
    participant SE as SyncEngine
    participant MQ as MutationQueue
    participant DL as RemoteDataLoader
    participant CR as ConflictResolver
    participant RST as RecentScreenTracker
    participant SL as CachedScreenLoader
    participant UI as DynamicScreen

    NO-->>CSM: status cambia a AVAILABLE (reconexion)
    CSM->>SE: processQueue()

    loop Para cada PendingMutation en cola
        SE->>MQ: dequeue()
        MQ-->>SE: PendingMutation (status → SYNCING)
        SE->>DL: submitData(endpoint, body, method)

        alt Exito (200/201)
            DL-->>SE: Result.Success
            SE->>MQ: remove(mutation.id)
        else Conflicto (409)
            DL-->>SE: Result.Failure("409")
            SE->>CR: resolve(mutation, isEntityDeleted=false)
            CR-->>SE: Resolution.RetryWithoutCheck (last-write-wins)
            SE->>DL: submitData retry
        else Entidad eliminada (404)
            DL-->>SE: Result.Failure("404")
            SE->>CR: resolve(mutation, isEntityDeleted=true)
            CR-->>SE: Resolution.Skip
            SE->>MQ: remove(mutation.id)
        else Error transitorio (5xx)
            SE->>SE: retryWithBackoff (1s, 2s, 4s)
            SE->>MQ: markFailed(id) si agota reintentos
        end
    end

    SE->>SE: syncState → Completed o Error

    CSM->>RST: getRecentKeys()
    RST-->>CSM: lista de screenKeys recientes

    loop Para cada screenKey reciente
        CSM->>SL: evict(screenKey)
    end

    CSM-->>UI: SyncManagerEvent.ReloadCurrentScreen
    UI->>UI: recarga pantalla actual con datos frescos
```

---

## Decision Tree del EventOrchestrator

```mermaid
flowchart TD
    A[execute screenKey, event, context] --> B[Buscar ScreenContract en registry]
    B --> C{Contract encontrado?}
    C -- No --> D[EventResult.NoOp]
    C -- Si --> E[permissionFor event]
    E --> F{Permiso requerido?}
    F -- null --> G[Ejecutar sin verificar]
    F -- permiso --> H{userContext.hasPermission?}
    H -- No --> I[EventResult.PermissionDenied]
    H -- Si --> J[endpointFor event, context]
    J --> K{Endpoint existe?}

    K -- No y evento CREATE --> L[NavigateTo resource-form por defecto]
    K -- No y SELECT_ITEM --> M{customHandlers select-item ?}
    M -- Si --> N[customHandler.execute context]
    M -- No --> D
    K -- Si y LOAD_DATA/SEARCH/REFRESH/LOAD_MORE --> O[executeGet]
    K -- Si y SAVE_NEW --> P[executeSubmit POST]
    K -- Si y SAVE_EXISTING --> Q[executeSubmit PUT]
    K -- Si y DELETE --> R[executeSubmit DELETE]

    O --> S[CachedDataLoader.loadData]
    P --> T[CachedDataLoader.submitData]
    Q --> T
    R --> T

    S --> S1{NetworkObserver.isOnline?}
    S1 -- Si --> S2[Fetch remoto + cache]
    S1 -- No --> S3{Hay cache local?}
    S3 -- Si --> S4[Result.Success datos stale]
    S3 -- No --> S5[Result.Failure sin conexion]

    T --> T1{NetworkObserver.isOnline?}
    T1 -- Si --> T2[Enviar al backend]
    T2 --> T3{Respuesta exitosa?}
    T3 -- Si --> V[EventResult.Success]
    T3 -- No y hay MutationQueue --> T4[Enqueue a MutationQueue]
    T4 --> T5[Result.Success null - optimistic]
    T1 -- No y hay MutationQueue --> T4
    T1 -- No y sin MutationQueue --> T6[Result.Failure sin conexion]

    S2 --> U{Resultado?}
    U -- Success --> V[EventResult.Success]
    U -- 401 --> W[EventResult.Logout]
    U -- 403 --> I
    U -- 4xx --> X[EventResult.Error retry=false]
    U -- 5xx/red --> Y[EventResult.Error retry=true]
    T5 --> V2[EventResult.Success data=null]
    V2 --> V3[ViewModel muestra guardado local]
```

---

## Contratos Existentes y sus Endpoints

```mermaid
graph LR
    subgraph Contratos ["ScreenContracts (kmp-screens)"]
        SLC[SchoolsListContract\nschools-list]
        SFC[SchoolsFormContract\nschools-form]
        SubLC[SubjectsListContract\nsubjects-list]
        SubFC[SubjectsFormContract\nsubjects-form]
        DashC[DashboardContract\ndashboard-*]
        SettC[SettingsContract\nsettings]
    end

    subgraph Endpoints ["Endpoints Resueltos"]
        E1["admin:/api/v1/schools"]
        E2["admin:/api/v1/subjects"]
        E3["iam:/api/v1/dashboard"]
        E4["mobile:/api/v1/settings"]
    end

    SLC --> E1
    SFC --> E1
    SubLC --> E2
    SubFC --> E2
    DashC --> E3
    SettC --> E4
```

---

## Custom Event Handlers Registrados

```mermaid
graph TD
    subgraph SchoolsListContract
        SL1["'select-item' → SelectItemHandler\n→ NavigateTo schools-form id=selectedItem.id\nrequires: schools:read"]
        SL2["'navigate-to-form' → NavigateToFormHandler\n→ NavigateTo schools-form\nrequires: schools:create"]
    end

    subgraph SchoolsFormContract
        SF1["'submit-form' → SubmitFormHandler\n→ SubmitTo admin:/api/v1/schools POST o PUT\nrequires: schools:create"]
        SF2["'go-back' → GoBackHandler\n→ NavigateTo schools-list\nrequires: ninguno"]
    end

    subgraph SubjectsListContract
        SubL1["'navigate-to-form' → NavigateToFormHandler\n→ NavigateTo subjects-form\nrequires: subjects:create"]
    end

    subgraph SubjectsFormContract
        SubF1["hereda submit-form de BaseCrudContract\n→ SubmitTo admin:/api/v1/subjects POST o PUT"]
    end

    subgraph DashboardContract
        D1["'refresh-metrics' → RefreshMetricsHandler\n→ recarga todos los metric cards"]
    end

    ScreenContractRegistry --> SchoolsListContract
    ScreenContractRegistry --> SchoolsFormContract
    ScreenContractRegistry --> SubjectsListContract
    ScreenContractRegistry --> SubjectsFormContract
    ScreenContractRegistry --> DashboardContract
```

---

## Routing de Prefijos de API

```mermaid
flowchart LR
    subgraph Prefijos ["Prefijo del endpoint"]
        P1["admin:/api/v1/..."]
        P2["mobile:/api/v1/..."]
        P3["iam:/api/v1/..."]
        P4["/api/v1/... (sin prefijo)"]
    end

    subgraph URLs ["Base URLs (AppConfig)"]
        U1["adminBaseUrl\n(staging: https://admin.edugo...)"]
        U2["mobileBaseUrl\n(staging: https://mobile.edugo...)"]
        U3["iamBaseUrl\n(staging: https://iam.edugo...)"]
    end

    P1 --> U1
    P3 --> U3
    P2 --> U2
    P4 --> U2
```

---

## Componentes Offline en la UI

### ConnectivityBanner

Componente visual que se muestra encima del contenido cuando hay situaciones offline o sincronizacion en curso:

| Estado | Color fondo | Texto | Indicador |
|--------|-------------|-------|-----------|
| Sin conexion | Amber (#FFF3E0) | "Sin conexión - usando datos guardados" | Texto naranja |
| Sincronizando | Azul (#E3F2FD) | "Sincronizando N/M..." | CircularProgressIndicator + texto azul |
| Pendientes (online) | Azul (#E3F2FD) | "Sincronizando N cambios pendientes..." | Texto azul |

### StaleDataIndicator

Se muestra cuando `CachedDataResult.isStale = true`, indicando que los datos vienen de cache y pueden no estar actualizados.

### MutationQueue

- Persiste en `SafeEduGoStorage` con key `offline.queue.mutations`
- Maximo 50 mutaciones encoladas
- Detecta duplicados por hash `endpoint|method|body`
- Estados: `PENDING` → `SYNCING` → (removido si exito) / `FAILED` / `CONFLICTED`
- `pendingCount: StateFlow<Int>` alimenta el ConnectivityBanner

### ConflictResolver

Estrategia actual: **last-write-wins**
- Conflicto 409: reintenta sin verificacion de `updated_at`
- Entidad eliminada 404: skip (descarta la mutacion)
- Emite eventos para observabilidad: `Resolved`, `EntityDeleted`, `Failed`

---

## Diferencias por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Coroutines scope | `viewModelScope` (Android lifecycle) | Custom scope en `ScreenModel` | Custom scope | Custom scope |
| Dispatch de eventos | Main thread via `Dispatchers.Main` | Main thread via `Dispatchers.Main` | Main thread via `Dispatchers.Main` | `Dispatchers.Main` (single thread) |
| Manejo de Back en FORM | Boton back fisico → `onBack()` | Gesture o boton toolbar → `onBack()` | Alt+← o boton toolbar | Boton toolbar |
| Snackbar de exito | `SnackbarHostState` Material3 | `SnackbarHostState` Material3 | `SnackbarHostState` Material3 | `SnackbarHostState` Material3 |
| NetworkObserver | ConnectivityManager callback | NWPathMonitor | HTTP health-check polling | navigator.onLine + eventos |
| Storage offline | SharedPreferences via Settings | NSUserDefaults via Settings | Java Preferences | localStorage |

---

## Mejoras Propuestas

| Mejora | Justificacion | Estado |
|--------|--------------|--------|
| ~~Transacciones locales~~ | ~~Para SAVE_NEW/SAVE_EXISTING, guardar borrador local y sincronizar cuando haya red~~ | **HECHO** - MutationQueue + SyncEngine + ConnectivitySyncManager |
| Undo despues de DELETE | Mostrar Snackbar con opcion "Deshacer" (patron Material 3) | Pendiente |
| Optimistic UI en SAVE | Actualizar lista inmediatamente, revertir si falla | Parcial - form saves offline retornan exito optimistico, pero la lista no muestra el item nuevo hasta sincronizar |
| Event Bus para cross-screen | Cuando una pantalla guarda datos, notificar a otras pantallas que tienen cache invalidada | Pendiente |
| Feedback de progreso en DELETE | Actualmente no hay indicador visual mientras se procesa el DELETE | Pendiente |
| Validacion client-side en FORM | Antes de llamar al backend, validar campos `required`, tipos, longitudes | Pendiente |
