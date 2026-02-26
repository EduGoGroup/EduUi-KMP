# 03 — Carga de UI Dinamica (SDUI)

## Clases Involucradas

```mermaid
classDiagram
    class ScreenDefinition {
        +screenKey: String
        +screenName: String
        +pattern: ScreenPattern
        +version: Int
        +template: ScreenTemplate
        +slotData: JsonObject?
        +dataConfig: DataConfig?
        +handlerKey: String?
    }
    class ScreenTemplate {
        +navigation: NavigationConfig?
        +zones: List~Zone~
        +platformOverrides: Map?
    }
    class Zone {
        +id: String
        +type: ZoneType
        +slots: List~Slot~
        +zones: List~Zone~
    }
    class Slot {
        +id: String
        +controlType: ControlType
        +bind: String?
        +eventId: String?
        +label: String?
        +placeholder: String?
        +required: Boolean
        +readOnly: Boolean
    }
    class ScreenPattern {
        <<enum>>
        LOGIN, FORM, LIST
        DASHBOARD, SETTINGS
        DETAIL
    }
    class CacheConfig {
        +screenTtlByPattern: Map~ScreenPattern Duration~
        +dataTtlByPattern: Map~ScreenPattern Duration~
        +screenTtlOverrides: Map~String Duration~
        +dataTtlOverrides: Map~String Duration~
        +maxScreenMemoryEntries: Int = 20
        +maxDataMemoryEntries: Int = 30
        +screenTtlFor(pattern, screenKey): Duration
        +dataTtlFor(pattern, screenKey): Duration
    }
    class CachedScreenLoader {
        -memoryCache: MutableMap
        -storage: SafeEduGoStorage
        -cacheConfig: CacheConfig
        -networkObserver: NetworkObserver?
        -versionChecker: ScreenVersionChecker?
        +loadScreen(screenKey): Result~ScreenDefinition~
        +seedFromBundle(screens: Map)
        +clearCache()
        +evict(screenKey)
        +prefetchScreens(screenKeys)
        -launchVersionCheck(screenKey, cachedVersion)
    }
    class ScreenVersionChecker {
        <<fun interface>>
        +checkVersion(screenKey): Int?
    }
    class RemoteScreenLoader {
        -httpClient: EduGoHttpClient
        +loadScreen(screenKey): Result~ScreenDefinition~
    }
    class RecentScreenTracker {
        -accessMap: LinkedHashMap~String Instant~
        -maxEntries: Int = 10
        +recordAccess(screenKey)
        +getRecentKeys(within: Duration): List~String~
        +clear()
    }
    class DataSyncService {
        +syncState: StateFlow~SyncState~
        +currentBundle: StateFlow~UserDataBundle?~
        +fullSync(): Result~UserDataBundle~
        +deltaSync(): Result~UserDataBundle~
        +restoreFromLocal(): UserDataBundle?
        +clearAll()
        -seedScreenLoader(screens)
    }
    class MutationQueue {
        +pendingCount: StateFlow~Int~
        +enqueue(mutation): Boolean
        +dequeue(): PendingMutation?
        +markFailed(id)
        +markConflicted(id)
        +remove(id)
        +clear()
    }
    class CachedDataLoader {
        -memoryCache: LinkedHashMap
        -storage: SafeEduGoStorage
        -cacheConfig: CacheConfig
        -networkObserver: NetworkObserver?
        -mutationQueue: MutationQueue?
        +loadData(endpoint, config): Result~DataPage~
        +loadDataWithStaleness(...): Result~CachedDataResult~
        +submitData(endpoint, body, method): Result~JsonObject?~
    }
    class DynamicScreenViewModel {
        +screenState: StateFlow~ScreenState~
        +dataState: StateFlow~DataState~
        +fieldValues: StateFlow~Map~
        +fieldErrors: StateFlow~Map~
        +isOnline: StateFlow~Boolean~
        +pendingMutationCount: StateFlow~Int~
        +loadScreen(key, placeholders)
        +loadData(endpoint, config)
        +loadMore()
        +search(query)
        +executeEvent(key, event, context)
        +executeCustomEvent(key, eventId, context)
        +submitForm(endpoint, method, fieldValues)
        -loadDataWithStaleness(...)
        -applyFieldMapping(items, mapping)
    }
    class NetworkObserver {
        <<interface>>
        +status: StateFlow~NetworkStatus~
        +isOnline: Boolean
        +start()
        +stop()
    }

    ScreenDefinition "1" --> "1" ScreenTemplate
    ScreenDefinition --> ScreenPattern
    ScreenTemplate "1" --> "*" Zone
    Zone "1" --> "*" Slot
    CachedScreenLoader --> RemoteScreenLoader : fallback red
    CachedScreenLoader --> CacheConfig : TTL por patron
    CachedScreenLoader --> NetworkObserver : offline check
    CachedScreenLoader --> ScreenVersionChecker : version async
    DynamicScreenViewModel --> CachedScreenLoader
    DynamicScreenViewModel --> CachedDataLoader
    DynamicScreenViewModel --> NetworkObserver
    DynamicScreenViewModel --> RecentScreenTracker
    DynamicScreenViewModel --> MutationQueue
    DataSyncService --> CachedScreenLoader : seedFromBundle
    CachedDataLoader --> CacheConfig : TTL por patron
    CachedDataLoader --> NetworkObserver : offline check
    CachedDataLoader --> MutationQueue : offline mutations
```

---

## Pipeline Completo de Carga de Pantalla

```mermaid
sequenceDiagram
    participant Nav as Navegacion
    participant DS as DynamicScreen
    participant VM as DynamicScreenViewModel
    participant Sync as DataSyncService
    participant CL as CachedScreenLoader
    participant RL as RemoteScreenLoader
    participant Backend as Screen Config Backend
    participant FR as FormFieldsResolver
    participant PR as PlaceholderResolver
    participant DL as CachedDataLoader
    participant NO as NetworkObserver

    Note over Sync,CL: Al hacer login / deltaSync,<br/>DataSyncService llama seedFromBundle<br/>para pre-poblar L1+L2

    Nav->>DS: Route.Dynamic(screenKey, params)
    DS->>VM: LaunchedEffect(screenKey) → loadScreen(screenKey, params)

    VM->>VM: recentScreenTracker.recordAccess(screenKey)
    Note over VM: isOnline es StateFlow reactivo<br/>directo del NetworkObserver.status

    VM->>CL: loadScreen(screenKey)

    CL->>CL: 1. buscar en memoria (L1)
    alt Cache de memoria valido (TTL segun CacheConfig + patron)
        CL->>CL: launchVersionCheck(screenKey, version) en background
        CL-->>VM: ScreenDefinition (de memoria)
    else Sin cache de memoria valido
        CL->>CL: 2. buscar en storage (L2)
        alt Cache de storage valido (TTL segun CacheConfig + patron)
            CL->>CL: promover a memoria + launchVersionCheck
            CL-->>VM: ScreenDefinition (de storage)
        else Expirado o no existe en storage
            CL->>NO: isOnline?
            alt Offline (NetworkObserver dice no conectado)
                alt Hay datos stale en storage
                    CL-->>VM: ScreenDefinition (stale, sin ir a red)
                else Sin datos
                    CL-->>VM: Result.Failure
                end
            else Online
                CL->>RL: loadScreen(screenKey)
                RL->>Backend: GET /api/v1/screen-config/resolve/key/{screenKey}
                Backend-->>RL: ScreenDefinition JSON
                RL-->>CL: Result.Success(ScreenDefinition)
                CL->>CL: guardar en memoria (L1) + storage (L2)
                CL-->>VM: ScreenDefinition
            end
        end
    end

    VM->>FR: FormFieldsResolver.resolve(screen)
    Note over FR: Si slotData.fields existe,<br/>convierte en slots dinamicos
    VM->>VM: SlotBindingResolver.resolve(screen)
    VM->>PR: PlaceholderResolver.resolve(screen, params)
    Note over PR: Sustituye {param} en endpoints

    alt Pattern == FORM && params["id"] existe
        VM->>DL: loadFormData(endpoint, config)
        DL->>Backend: GET /api/v1/resource/{id}
        Backend-->>DL: objeto entidad
        DL-->>VM: fieldValues pre-llenados
    else Pattern tiene endpoint de datos (via contract)
        VM->>DL: loadDataWithStaleness(endpoint, config, pattern, screenKey)
        alt Offline
            DL->>DL: retornar cache local con isStale=true
        else Online + cache fresco
            DL-->>VM: CachedDataResult(data, isStale=false)
        else Online + cache expirado
            DL->>Backend: GET endpoint (con pagination, search, filters)
            Backend-->>DL: { items: [...], total, page }
            DL-->>VM: CachedDataResult(data, isStale=false)
        end
        VM->>VM: applyFieldMapping(items, config.fieldMapping)
    end

    VM-->>DS: screenState + dataState actualizados (DataState.Success incluye isStale)
    DS->>DS: PatternRouter.route(screen.pattern)
    DS->>DS: renderizar Renderer correcto
```

---

## Estructura del ScreenDefinition (Ejemplo: schools-list)

```mermaid
graph TD
    SD["ScreenDefinition\nscreenKey: schools-list\npattern: LIST"]
    T["ScreenTemplate"]
    Z1["Zone: TOOLBAR\nslots: [titulo, boton-create]"]
    Z2["Zone: SIMPLE_LIST\nslots: [list-item-template]"]
    Z3["Zone: SEARCH_BAR\nslots: [search-input]"]

    S1["Slot: list-item\ncontrolType: LIST_ITEM\nbind: items\neventId: select-item"]
    S2["Slot: search\ncontrolType: TEXT_INPUT\nbind: search_query"]
    S3["Slot: titulo\ncontrolType: TEXT_DISPLAY\nbind: page_title"]

    SD --> T
    T --> Z1
    T --> Z2
    T --> Z3
    Z2 --> S1
    Z3 --> S2
    Z1 --> S3
```

---

## Renderers por Patron

```mermaid
flowchart TD
    PatternRouter --> |LIST| ListPatternRenderer
    PatternRouter --> |FORM| FormPatternRenderer
    PatternRouter --> |DASHBOARD| DashboardPatternRenderer
    PatternRouter --> |DETAIL| DetailPatternRenderer
    PatternRouter --> |SETTINGS| SettingsPatternRenderer
    PatternRouter --> |LOGIN| LoginPatternRenderer

    ListPatternRenderer --> |renderiza| DSLazyColumn
    ListPatternRenderer --> |renderiza| DSSearchBar
    ListPatternRenderer --> |renderiza| DynamicToolbar["DynamicToolbar (LIST)"]

    FormPatternRenderer --> |renderiza| DSTextField
    FormPatternRenderer --> |renderiza| DSDropdown
    FormPatternRenderer --> |renderiza| DynamicToolbar2["DynamicToolbar (FORM)"]
    FormPatternRenderer --> |filtra| ACTION_GROUP["Omite zones ACTION_GROUP\n(evita botones duplicados)"]

    DashboardPatternRenderer --> |renderiza| DSMetricCard
    DashboardPatternRenderer --> |renderiza| DSChartCard
    DashboardPatternRenderer --> |renderiza| DynamicToolbar3["DynamicToolbar (DASHBOARD)"]

    DetailPatternRenderer --> |renderiza| DSDetailField
    DetailPatternRenderer --> |renderiza| DynamicToolbar4["DynamicToolbar (DETAIL)"]
```

---

## DynamicToolbar por Patron

```mermaid
flowchart LR
    subgraph FORM_TOOLBAR ["FORM: flecha + titulo + Guardar"]
        FT1[←] --> FT2["Titulo (edit_title o page_title)"]
        FT2 --> FT3["Guardar"]
    end
    subgraph LIST_TOOLBAR ["LIST: titulo + boton crear"]
        LT1["Titulo (page_title)"] --> LT2{canCreate?}
        LT2 -- Si --> LT3["+"]
        LT2 -- No --> LT4[sin boton]
    end
    subgraph DETAIL_TOOLBAR ["DETAIL: flecha + titulo"]
        DT1[←] --> DT2["Titulo"]
    end
    subgraph DASH_TOOLBAR ["DASHBOARD/SETTINGS: solo titulo"]
        ST1["Titulo"]
    end
    subgraph LOGIN_TOOLBAR ["LOGIN: sin toolbar"]
        NO["(ninguno)"]
    end
```

---

## Pre-carga desde Sync Bundle (seedFromBundle)

```mermaid
sequenceDiagram
    participant App as App.kt
    participant DSS as DataSyncService
    participant Repo as SyncRepository
    participant Backend as IAM Platform
    participant CL as CachedScreenLoader
    participant Store as LocalSyncStore

    Note over App: Login exitoso o restaurar sesion

    App->>DSS: fullSync() / deltaSync()
    DSS->>Repo: getBundle() / deltaSync(hashes)
    Repo->>Backend: GET /api/v1/sync/bundle
    Backend-->>Repo: SyncBundleResponse (menu, permissions, screens, contexts)
    Repo-->>DSS: Result.Success(response)

    DSS->>DSS: mapBundleResponse → UserDataBundle
    DSS->>Store: saveBundle(bundle) → persiste en storage
    DSS->>CL: seedFromBundle(bundle.screens)
    CL->>CL: Fase 1 (secuencial): putMemoryEntry (L1)<br/>para cada screen — HashMap no es thread-safe
    CL->>CL: Fase 2 (paralelo): withContext(Dispatchers.Default)<br/>async { json.encodeToString + storage.putStringSafe } (L2)
    Note over CL: awaitAll() — 21 screens serializados en paralelo

    Note over CL: Todas las pantallas quedan<br/>disponibles inmediatamente,<br/>incluso sin red
```

---

## Cache de Pantallas: Estrategia de 2 Niveles con TTL Configurable

### TTL por Patron (CacheConfig)

| Patron | TTL de Screen | TTL de Data | Justificacion |
|--------|---------------|-------------|---------------|
| DASHBOARD | 60s | 60s | Datos cambian frecuentemente |
| LIST | 5min | 5min | Balance entre frescura y rendimiento |
| FORM | 60min | 60min | Estructura de formularios cambia raramente |
| DETAIL | 10min | 10min | Datos individuales, frescura moderada |
| SETTINGS | 30min | 30min | Configuracion cambia poco |
| (default) | 5min | 5min | Fallback si patron no esta mapeado |

Ademas, `CacheConfig` soporta `screenTtlOverrides` y `dataTtlOverrides` por `screenKey` especifico para ajustes finos.

### Flujo de Cache

```mermaid
flowchart TD
    A["loadScreen(screenKey)"] --> B{Nivel 1: Memoria}
    B -- "Fresco (TTL por CacheConfig + patron)" --> VC1["launchVersionCheck en background"]
    VC1 --> C[Retornar de memoria]
    B -- "Expirado o invalidado" --> D{Nivel 2: Storage}
    D -- "Fresco (TTL por CacheConfig + patron)" --> VC2["Promover a memoria + launchVersionCheck"]
    VC2 --> E[Retornar de storage]
    D -- "Expirado o no existe" --> OFFLINE{NetworkObserver: isOnline?}

    OFFLINE -- "Offline + hay stale" --> J["Retornar stale sin ir a red"]
    OFFLINE -- "Offline + sin datos" --> K[Result.Failure]
    OFFLINE -- "Online" --> F["Red: GET /screen-config/resolve/key/{key}"]

    F -- "Exito" --> G[Guardar en memoria L1 + storage L2]
    G --> H[Retornar ScreenDefinition]
    F -- "Error de red" --> I{Hay entrada stale en storage?}
    I -- "Si" --> J2[Retornar stale como fallback]
    I -- "No" --> K2[Result.Failure]

    clearCache --> L["Escribir timestamp en\n'screen.cache.__invalidatedAt'"]
    L --> M["Toda entrada anterior al timestamp\nes considerada invalida"]

    seedFromBundle --> N["Para cada screen del bundle:\nputMemoryEntry L1 +\nstorage.putStringSafe L2"]
    N --> O["Screens disponibles inmediatamente\nincluso offline"]
```

### Versionado Asincrono de Pantallas

```mermaid
sequenceDiagram
    participant CL as CachedScreenLoader
    participant BG as BackgroundScope
    participant VC as ScreenVersionChecker
    participant Backend as IAM Platform

    Note over CL: Al retornar screen desde cache L1 o L2

    CL->>CL: isOnline? && versionChecker != null?
    alt Si
        CL->>BG: launch (no bloquea)
        BG->>VC: checkVersion(screenKey)
        VC->>Backend: GET /api/v1/screen-config/version/{screenKey}
        Backend-->>VC: serverVersion: Int
        alt serverVersion > cachedVersion
            BG->>CL: evict(screenKey)
            Note over CL: Proxima carga ira a red
        else Misma version
            Note over BG: No hacer nada
        end
    else No (offline o sin versionChecker)
        Note over CL: Omitir check
    end
```

---

## Cache de Datos: CachedDataLoader

```mermaid
flowchart TD
    A["loadDataWithStaleness(endpoint, config, pattern, screenKey)"] --> TTL["Resolver TTL:\n1. DataConfig.refreshInterval\n2. CacheConfig.dataTtlFor(pattern, screenKey)"]
    TTL --> OFFLINE{NetworkObserver: isOnline?}

    OFFLINE -- Offline --> CACHE_OFF["Buscar en memoria o storage"]
    CACHE_OFF -- Encontrado --> STALE["CachedDataResult(data, isStale=true)"]
    CACHE_OFF -- No encontrado --> ERR["Result.Failure: Sin conexion"]

    OFFLINE -- Online --> MEM{Memoria: fresco?}
    MEM -- "Si (< TTL)" --> FRESH["CachedDataResult(data, isStale=false)"]
    MEM -- "No" --> REMOTE["Cargar desde red"]
    REMOTE -- Exito --> SAVE["Guardar en memoria + storage"]
    SAVE --> FRESH2["CachedDataResult(data, isStale=false)"]
    REMOTE -- Fallo --> STALE_FALLBACK{Hay cache stale?}
    STALE_FALLBACK -- Si --> STALE2["CachedDataResult(data, isStale=true)"]
    STALE_FALLBACK -- No --> ERR2["Propagar error"]
```

### Offline Mutations (MutationQueue)

Cuando `submitData` se invoca estando offline:

1. Se encola en `MutationQueue` (persiste en storage, max 50 mutaciones)
2. Se retorna `Result.Success(null)` (indica "guardado local")
3. El ViewModel muestra "Guardado localmente, se sincronizara al reconectar"
4. `pendingMutationCount: StateFlow<Int>` se actualiza para que la UI muestre indicador
5. Deteccion de duplicados: no encola si ya existe mutacion identica (endpoint+method+body)
6. Si la red falla al enviar (no solo offline), tambien se encola como fallback

---

## DynamicScreenViewModel: Estado y Capacidades

```mermaid
graph LR
    subgraph Inputs
        SL[ScreenLoader]
        DL[DataLoader / CachedDataLoader]
        EO[EventOrchestrator]
        CR[ScreenContractRegistry]
        NO[NetworkObserver]
        RST[RecentScreenTracker]
        MQ[MutationQueue]
    end

    subgraph ViewModel ["DynamicScreenViewModel"]
        SS["screenState: StateFlow&lt;ScreenState&gt;"]
        DSt["dataState: StateFlow&lt;DataState&gt;\n(incluye isStale: Boolean)"]
        FV["fieldValues: StateFlow&lt;Map&gt;"]
        FE["fieldErrors: StateFlow&lt;Map&gt;"]
        IO["isOnline: StateFlow&lt;Boolean&gt;"]
        PMC["pendingMutationCount: StateFlow&lt;Int&gt;"]
    end

    subgraph Funciones
        LS["loadScreen(key, params)"]
        LD["loadData / loadDataWithStaleness"]
        LM["loadMore()"]
        SR["search(query)"]
        EE["executeEvent / executeCustomEvent"]
        SF["submitForm(endpoint, method, fieldValues)"]
        AFM["applyFieldMapping(items, mapping)"]
    end

    SL --> LS
    DL --> LD
    DL --> SF
    EO --> EE
    CR --> LS
    NO --> IO
    RST --> LS
    MQ --> PMC

    LS --> SS
    LD --> DSt
    AFM --> DSt
```

### DataState.Success: campo isStale

Cuando `isStale = true`, la UI puede mostrar un indicador visual (por ejemplo, `StaleDataIndicator`) para informar al usuario que los datos provienen del cache y pueden no estar actualizados. Esto ocurre cuando:

- Se esta offline y se retorna cache local
- La red fallo y se uso cache stale como fallback

### applyFieldMapping

Transforma los nombres de campos de la respuesta API a los nombres esperados por el template. El mapeo se define en `DataConfig.fieldMapping` con formato `"templateField" -> "apiField"`. Los campos originales se preservan junto a los alias mapeados.

---

## DataConfig y FieldMapping

```mermaid
graph LR
    subgraph Backend ["API Response"]
        BF["{ full_name: 'Juan', city: 'Bogota', country_code: 'CO' }"]
    end

    subgraph DataConfig
        FM["fieldMapping:\n'title' → 'full_name'\n'subtitle' → 'city'"]
        PC["pagination:\npageSize: 20\npageParam: 'page'"]
        SF["searchFields:\n['name', 'city', 'code']"]
        RI["refreshInterval: Int?\n(override de TTL en segundos)"]
    end

    subgraph UI ["List Item"]
        UI1["title: 'Juan'"]
        UI2["subtitle: 'Bogota'"]
    end

    BF --> FM --> UI
```

---

## RecentScreenTracker

Rastreador LRU de las ultimas pantallas accedidas. Mantiene un maximo de 10 entradas (configurable).

```mermaid
flowchart LR
    VM["DynamicScreenViewModel\nloadScreen()"] --> RST["RecentScreenTracker\nrecordAccess(screenKey)"]
    RST --> MAP["LinkedHashMap (LRU)\nmax 10 entradas"]
    RST --> GET["getRecentKeys(within: 15min)\n→ lista de screenKeys recientes"]
```

Uso: al acceder una pantalla, el ViewModel registra el acceso. Se puede consultar `getRecentKeys()` para obtener las pantallas visitadas recientemente (ej. para prefetch o analytics).

---

## Diferencias por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Composable engine | Jetpack Compose | Compose Multiplatform (iOS rendering) | Compose for Desktop (JVM) | Compose for Web (Canvas) |
| Animaciones | Suaves, aceleradas por GPU | Suaves con Metal | Suaves con Skia | Limitadas (WASM canvas) |
| Cache de pantallas | SharedPreferences | NSUserDefaults | Java Prefs | localStorage |
| NetworkObserver | ConnectivityManager callback | NWPathMonitor | HTTP health-check polling | navigator.onLine + eventos |
| `platformOverrides` en ScreenTemplate | Puede activar configuracion especifica Android | Puede activar configuracion especifica iOS | Sin overrides especificos | Puede ajustar layouts |
| Lazy rendering | `LazyColumn` de Compose | `LazyColumn` (iOS rendering) | `LazyColumn` (JVM) | `LazyColumn` (WASM, puede ser lento con listas grandes) |

---

## Mejoras: Estado de Implementacion

| Mejora | Estado | Detalle |
|--------|--------|---------|
| Versionado de pantallas | PARCIAL | Backend tiene endpoint `GET /screen-config/version/{key}`. Cliente tiene `ScreenVersionChecker` y `launchVersionCheck` que evicta cache en background si la version del servidor es mayor. Falta auto-invalidacion proactiva (push/websocket). |
| Pre-carga de pantallas relacionadas | HECHO | `seedFromBundle` carga todas las pantallas del sync bundle en L1+L2 de una vez. Ademas existe `prefetchScreens(keys)` para pre-carga explicita. |
| Cache size configurable | HECHO | `CacheConfig` con TTLs por patron, overrides por screenKey, `maxScreenMemoryEntries=20`, `maxDataMemoryEntries=30`. |
| TTL configurable por patron | HECHO | Dashboard 60s, List 5min, Form 60min, Detail 10min, Settings 30min. Ademas `DataConfig.refreshInterval` permite override por endpoint. |
| Offline fallback con staleness | HECHO | `NetworkObserver` integrado en `CachedScreenLoader` y `CachedDataLoader`. Retorna datos stale con `isStale=true` cuando esta offline. |
| Mutation queue offline | HECHO | `MutationQueue` persiste mutaciones pendientes (max 50), con deteccion de duplicados y retry. `pendingMutationCount` expuesto al UI. |
| Skeleton loading | PENDIENTE | Mostrar placeholders mientras se carga la ScreenDefinition (actualmente es pantalla en blanco) |
| Error boundaries por zona | PENDIENTE | Si una Zone falla al renderizar, mostrar error solo en esa zona en vez de toda la pantalla |
| ScreenDefinition diff | PENDIENTE | Al recibir nueva version del servidor, hacer diff y re-renderizar solo las zonas cambiadas |
| Paralelización de seedFromBundle | HECHO | Fase 1: memoria secuencial (HashMap), Fase 2: serialización + storage en paralelo via `withContext(Dispatchers.Default)` + `async/awaitAll` |
| isOnline reactivo | HECHO | `DynamicScreenViewModel.isOnline` es StateFlow reactivo del `NetworkObserver.status`, elimina `updateOnlineStatus()` manual |
| Delta sync incremental | HECHO | `applyDeltaToBundle()` construye bundle en memoria sin recargar de storage. Solo persiste buckets cambiados |
| Splash paralelo | HECHO | `restoreSession()` + `restoreFromLocal()` en paralelo. `deltaSync()` en paralelo con splash delay |
