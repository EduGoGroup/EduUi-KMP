# 07 — Conectado y Offline

## Estrategia General

```mermaid
flowchart TD
    subgraph estrategia ["Estrategia: Cache-first con Stale fallback + Mutation queue offline"]
        A[Request de datos] --> B{NetworkObserver\nisOnline?}
        B -- No --> C{Cache disponible?}
        C -- Si --> D[Retornar datos de cache\nisStale=true]
        C -- No --> E["Result.Failure\n'Sin conexión y sin datos en caché'"]
        B -- Si --> F{Cache L1 fresco?}
        F -- Si --> G[Retornar de memoria\nisStale=false]
        F -- No --> H[GET a backend]
        H --> I{Exito?}
        I -- Si --> J[Actualizar L1 + L2\nRetornar datos frescos\nisStale=false]
        I -- No --> K{Cache stale disponible?}
        K -- Si --> L[Retornar datos viejos\nisStale=true]
        K -- No --> M[Result.Failure\nmostrar error]
    end
```

```mermaid
flowchart TD
    subgraph mutaciones ["Estrategia: Mutaciones offline con queue"]
        MA[submitData POST/PUT/DELETE] --> MB{NetworkObserver\nisOnline?}
        MB -- No --> MC{MutationQueue\ndisponible?}
        MC -- Si --> MD[enqueue PendingMutation\nRetornar Result.Success null\nUI muestra 'Guardado localmente']
        MC -- No --> ME["Result.Failure\n'Sin conexión'"]
        MB -- Si --> MF[Enviar a backend]
        MF --> MG{Exito?}
        MG -- Si --> MH[invalidateByPrefix\nRetornar Result.Success]
        MG -- No --> MI{MutationQueue\ndisponible?}
        MI -- Si --> MJ[enqueue para retry\nRetornar Result.Success null]
        MI -- No --> MK[Result.Failure]
    end
```

---

## NetworkObserver: Deteccion de Conectividad Multiplataforma

Implementado en `modules/network/src/commonMain/kotlin/.../connectivity/`.

```mermaid
classDiagram
    class NetworkObserver {
        <<interface>>
        +status: StateFlow~NetworkStatus~
        +isOnline: Boolean
        +start()
        +stop()
    }

    class NetworkStatus {
        <<enum>>
        AVAILABLE
        UNAVAILABLE
        LOSING
    }

    class AndroidNetworkObserver {
        -context: Context
        -callback: NetworkCallback?
        +start() ConnectivityManager.registerNetworkCallback
        +stop() unregisterNetworkCallback
    }

    class IosNetworkObserver {
        -monitor: nw_path_monitor_t?
        +start() nw_path_monitor_start
        +stop() nw_path_monitor_cancel
    }

    class DesktopNetworkObserver {
        -pollIntervalMs: Long = 30000
        -healthCheckUrl: String
        +start() coroutine HEAD polling
        +stop() cancel scope
    }

    class WasmJsNetworkObserver {
        -listening: Boolean
        +start() navigator.onLine + addEventListener
        +stop() removeEventListener
    }

    NetworkObserver --> NetworkStatus
    AndroidNetworkObserver ..|> NetworkObserver
    IosNetworkObserver ..|> NetworkObserver
    DesktopNetworkObserver ..|> NetworkObserver
    WasmJsNetworkObserver ..|> NetworkObserver
```

### API comun

```kotlin
interface NetworkObserver {
    val status: StateFlow<NetworkStatus>
    val isOnline: Boolean  // status.value == AVAILABLE
    fun start()
    fun stop()
}

enum class NetworkStatus { AVAILABLE, UNAVAILABLE, LOSING }
```

### Creacion por plataforma

```kotlin
// expect/actual en commonMain
expect fun createNetworkObserver(): NetworkObserver

// Registrado en networkModule (Koin)
single<NetworkObserver> { createNetworkObserver() }
```

**Nota Android:** `createNetworkObserver()` lanza `UnsupportedOperationException`. En Android se usa `createAndroidNetworkObserver(context)` inyectado via Koin con el application Context.

### Implementaciones por plataforma

| Plataforma | Clase | Mecanismo | Latencia |
|------------|-------|-----------|----------|
| Android | `AndroidNetworkObserver` | `ConnectivityManager.registerNetworkCallback` con `NET_CAPABILITY_INTERNET` | Inmediata (callback del sistema) |
| iOS | `IosNetworkObserver` | `nw_path_monitor_create` + `nw_path_monitor_set_update_handler` en main queue | Inmediata (callback del sistema) |
| Desktop/JVM | `DesktopNetworkObserver` | HTTP HEAD a `clients3.google.com/generate_204` cada 30s | Hasta 30s (polling) |
| WasmJS | `WasmJsNetworkObserver` | `navigator.onLine` + `window.addEventListener('online'/'offline')` | Inmediata (eventos del navegador) |

---

## Estados del Sistema por Conectividad

```mermaid
stateDiagram-v2
    [*] --> Online : app inicia con red

    Online --> Degraded : request falla (timeout/5xx)
    Online --> Offline : NetworkObserver emite UNAVAILABLE

    Degraded --> Online : request exitoso
    Degraded --> Offline : NetworkObserver emite UNAVAILABLE

    Offline --> Syncing : NetworkObserver emite AVAILABLE\n(wasOffline && isNowOnline)
    Syncing --> Online : SyncEngine.Completed\n(queue vacia)
    Syncing --> Online : SyncEngine.Error\n(algunos fallos)

    state Online {
        [*] --> FreshData
        FreshData --> RefreshingInBackground : TTL expirado (CacheConfig)
        RefreshingInBackground --> FreshData : refresh exitoso
        RefreshingInBackground --> StaleData : refresh falla
    }

    state Offline {
        [*] --> SinCache : primera vez sin datos
        [*] --> ConCache : datos cacheados disponibles
        ConCache --> MutacionEncolada : POST/PUT/DELETE offline
        MutacionEncolada --> ConCache : UI muestra exito optimista
    }

    state Syncing {
        [*] --> ProcessingQueue : SyncEngine.processQueue()
        ProcessingQueue --> RefreshScreens : ConnectivitySyncManager\nevict + reload
    }
```

---

## CachedDataLoader: Flujo Detallado

```mermaid
sequenceDiagram
    participant VM as DynamicScreenViewModel
    participant CDL as CachedDataLoader
    participant NO as NetworkObserver
    participant Mem as Memoria (LinkedHashMap)
    participant Storage as SafeEduGoStorage
    participant Net as RemoteDataLoader
    participant Backend as API Backend

    VM->>CDL: loadDataWithStaleness(endpoint, config, params, pattern, key)
    CDL->>CDL: buildCacheKey("data:schoolId123:admin:/api/v1/schools:page=1")
    CDL->>CDL: resolveTtl(config, pattern, key) via CacheConfig

    CDL->>NO: isOnline?

    alt Offline (NetworkObserver.isOnline == false)
        CDL->>Mem: buscar(cacheKey) sin verificar TTL
        alt Hay en memoria
            Mem-->>CDL: CacheEntry(data, timestamp)
            CDL-->>VM: Result.Success(CachedDataResult(data, isStale=true))
        else Sin memoria, buscar storage
            CDL->>Storage: getStringSafe("data.cache.{hashCode}")
            alt Hay en storage
                Storage-->>CDL: StorageCacheEntry
                CDL-->>VM: Result.Success(CachedDataResult(data, isStale=true))
            else Sin ningun cache
                CDL-->>VM: Result.Failure("Sin conexión y sin datos en caché")
            end
        end
    else Online
        CDL->>Mem: buscar(cacheKey)
        alt Memoria fresca (< TTL segun CacheConfig)
            Mem-->>CDL: CacheEntry(items, timestamp)
            CDL-->>VM: Result.Success(CachedDataResult(data, isStale=false))
        else Sin cache fresco
            CDL->>Net: loadData(endpoint, params)
            Net->>Backend: GET /api/v1/schools?page=1&pageSize=20

            alt Respuesta exitosa
                Backend-->>Net: { items: [...], total: 45 }
                Net-->>CDL: Result.Success(DataPage)
                CDL->>Mem: guardar(key, DataPage, now())
                CDL->>Storage: putStringSafe("data.cache.{hash}", entry)
                CDL-->>VM: Result.Success(CachedDataResult(data, isStale=false))
            else Error de red
                Net-->>CDL: Result.Failure("Sin conexion")
                CDL->>Mem: buscar stale (sin verificar TTL)
                alt Hay cache stale
                    Mem-->>CDL: CacheEntry(items, oldTimestamp)
                    CDL-->>VM: Result.Success(CachedDataResult(data, isStale=true))
                else Sin stale en memoria, buscar storage
                    CDL->>Storage: buscar stale en storage
                    alt Hay cache stale en storage
                        Storage-->>CDL: StorageCacheEntry
                        CDL-->>VM: Result.Success(CachedDataResult(data, isStale=true))
                    else Sin ningun cache
                        CDL-->>VM: Result.Failure("error de red")
                    end
                end
            end
        end
    end
```

---

## MutationQueue: Cola de Mutaciones Offline

Implementado en `modules/dynamic-ui/.../offline/MutationQueue.kt`.

```mermaid
classDiagram
    class MutationQueue {
        -storage: SafeEduGoStorage
        -maxMutations: Int = 50
        -mutex: Mutex
        -mutations: MutableList~PendingMutation~
        +pendingCount: StateFlow~Int~
        +enqueue(mutation) Boolean
        +dequeue() PendingMutation?
        +peek() PendingMutation?
        +markFailed(id)
        +markConflicted(id)
        +remove(id)
        +getAll() List~PendingMutation~
        +clear()
        -isDuplicate(mutation) Boolean
        -persistAndNotify()
        -restoreFromStorage()
    }

    class PendingMutation {
        +id: String
        +endpoint: String
        +method: String
        +body: JsonObject
        +createdAt: Long
        +retryCount: Int = 0
        +maxRetries: Int = 3
        +status: MutationStatus
        +entityUpdatedAt: String?
    }

    class MutationStatus {
        <<enum>>
        PENDING
        SYNCING
        FAILED
        CONFLICTED
    }

    MutationQueue --> PendingMutation
    PendingMutation --> MutationStatus
```

### Caracteristicas

- **Thread-safe:** Todas las operaciones protegidas con `Mutex`
- **Deduplicacion:** `isDuplicate()` compara `endpoint|method|body` contra mutaciones PENDING existentes
- **Persistente:** Serializada a `SafeEduGoStorage` con key `offline.queue.mutations`, sobrevive reinicio de app
- **Observable:** `pendingCount: StateFlow<Int>` para que la UI muestre el badge de cambios pendientes
- **Limite:** Maximo 50 mutaciones encoladas (configurable via `maxMutations`)
- **Ciclo de vida:** `init` llama `restoreFromStorage()` para recuperar mutaciones de sesiones anteriores

### Flujo de una mutacion

```mermaid
stateDiagram-v2
    [*] --> PENDING : enqueue()
    PENDING --> SYNCING : dequeue()
    SYNCING --> Removed : submitData exitoso → remove()
    SYNCING --> PENDING : markFailed() con retryCount < maxRetries
    SYNCING --> FAILED : markFailed() con retryCount >= maxRetries
    SYNCING --> CONFLICTED : markConflicted() tras conflicto 409
    SYNCING --> Resolved : ConflictResolver.Skip → remove()
```

---

## SyncEngine: Procesamiento de Cola al Reconectar

Implementado en `modules/dynamic-ui/.../offline/SyncEngine.kt`.

```mermaid
sequenceDiagram
    participant NO as NetworkObserver
    participant SE as SyncEngine
    participant MQ as MutationQueue
    participant RDL as RemoteDataLoader
    participant CR as ConflictResolver

    Note over SE: start(scope) observa NetworkObserver.status

    NO-->>SE: status: UNAVAILABLE → AVAILABLE\n(wasOffline && isNowOnline)
    SE->>MQ: getAll() filtrar PENDING

    loop Para cada mutacion pendiente
        SE->>SE: _syncState = Syncing(current, total)
        SE->>MQ: dequeue() → marca SYNCING
        SE->>RDL: submitData(endpoint, body, method)

        alt Exito (Result.Success)
            SE->>MQ: remove(id)
        else Conflicto (409)
            SE->>CR: resolve(mutation, isEntityDeleted?)

            alt isEntityDeleted (404)
                CR-->>SE: Resolution.Skip
                SE->>MQ: remove(id)
            else Entidad existe pero fue modificada
                CR-->>SE: Resolution.RetryWithoutCheck
                SE->>RDL: submitData (retry sin check)
                alt Retry exitoso
                    SE->>MQ: remove(id)
                else Retry falla
                    SE->>MQ: markConflicted(id)
                end
            end
        else Otro error
            SE->>SE: retryWithBackoff (1s, 2s, 4s)
            alt Algun retry exitoso
                SE->>MQ: remove(id)
            else Todos los retries fallan
                SE->>MQ: markFailed(id)
            end
        end
    end

    SE->>SE: _syncState = Completed o Error
```

### SyncState

```kotlin
sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(val current: Int, val total: Int) : SyncState()
    data object Completed : SyncState()
    data class Error(val message: String) : SyncState()
}
```

### Exponential Backoff

```
Intento 0: delay 1s  (1000 * 2^0)
Intento 1: delay 2s  (1000 * 2^1)
Intento 2: delay 4s  (1000 * 2^2)
```

---

## ConflictResolver: Resolucion de Conflictos

Implementado en `modules/dynamic-ui/.../offline/ConflictResolver.kt`.

```mermaid
flowchart TD
    A[Mutacion con conflicto] --> B{Entidad eliminada\nen servidor? 404}
    B -- Si --> C[Resolution.Skip\nEmit EntityDeleted\nMutation removida]
    B -- No --> D[Resolution.RetryWithoutCheck\nEmit Resolved 'last-write-wins'\nReintento sin updated_at check]
    D --> E{Retry exitoso?}
    E -- Si --> F[remove mutation]
    E -- No --> G[markConflicted]
```

### Estrategia actual: Last-Write-Wins

La implementacion actual es basica:
- **Entidad eliminada (404):** Skip, la mutacion se descarta silenciosamente
- **Entidad modificada (409):** Retry sin verificacion de `updated_at` (last-write-wins)
- **Eventos observables:** `ConflictEvent.Resolved`, `ConflictEvent.EntityDeleted`, `ConflictEvent.Failed` via `SharedFlow`

---

## ConnectivitySyncManager: Coordinacion al Reconectar

Implementado en `modules/dynamic-ui/.../offline/ConnectivitySyncManager.kt`.

Coordina `NetworkObserver` + `SyncEngine` + `RecentScreenTracker` + `CachedScreenLoader` para ejecutar una secuencia de recuperacion completa cuando la app vuelve a estar online.

```mermaid
sequenceDiagram
    participant NO as NetworkObserver
    participant CSM as ConnectivitySyncManager
    participant SE as SyncEngine
    participant RST as RecentScreenTracker
    participant CSL as CachedScreenLoader
    participant UI as DynamicScreen

    NO-->>CSM: status: UNAVAILABLE → AVAILABLE
    Note over CSM: onReconnect()

    CSM->>SE: processQueue()
    Note over SE: Enviar mutaciones pendientes

    CSM->>RST: getRecentKeys(within=15min)
    RST-->>CSM: ["schools-list", "dashboard"]

    loop Para cada screen reciente
        CSM->>CSL: evict(screenKey)
        Note over CSL: Elimina de cache para forzar recarga
    end

    CSM->>UI: emit SyncManagerEvent.ReloadCurrentScreen
    Note over UI: Recarga la pantalla actual con datos frescos
```

### RecentScreenTracker

```kotlin
class RecentScreenTracker(maxEntries: Int = 10) {
    fun recordAccess(screenKey: String)      // LRU con timestamp
    fun getRecentKeys(within: Duration = 15.minutes): List<String>
    fun clear()
}
```

Registrado como dependencia del `DynamicScreenViewModel`:
```kotlin
// En loadScreen():
recentScreenTracker?.recordAccess(screenKey)
```

---

## CacheConfig: TTL Diferenciado por Tipo

Implementado en `modules/dynamic-ui/.../cache/CacheConfig.kt`.

```kotlin
class CacheConfig(
    val screenTtlByPattern: Map<ScreenPattern, Duration>,
    val dataTtlByPattern: Map<ScreenPattern, Duration>,
    val screenTtlOverrides: Map<String, Duration>,  // por screenKey especifico
    val dataTtlOverrides: Map<String, Duration>,
    val maxScreenMemoryEntries: Int = 20,
    val maxDataMemoryEntries: Int = 30,
)
```

### TTLs por defecto

| Pattern | TTL Screen | TTL Data |
|---------|-----------|----------|
| DASHBOARD | 60s | 60s |
| LIST | 5 min | 5 min |
| FORM | 60 min | 60 min |
| DETAIL | 10 min | 10 min |
| SETTINGS | 30 min | 30 min |

### Prioridad de resolucion

1. Override por `screenKey` especifico (ej: `"schools-list"` → 2 min)
2. TTL por `ScreenPattern` (ej: `LIST` → 5 min)
3. Default global: 5 min

---

## Busqueda Offline en 2 Pasos

```mermaid
sequenceDiagram
    participant User as Usuario
    participant DS as DynamicScreen
    participant VM as DynamicScreenViewModel
    participant CDL as CachedDataLoader
    participant Backend as API Backend

    User->>DS: escribe "Bogota" en barra de busqueda
    DS->>VM: search("Bogota")

    Note over VM: Paso 1: Filtrado local inmediato
    VM->>VM: filtrar _allItems donde\ncualquier valor string contiene "Bogota"
    VM->>DS: DataState.Success(filteredItems, isOfflineFiltered=true)
    DS->>DS: mostrar resultados con indicador naranja

    Note over VM: Paso 2: Busqueda server-side asincrona
    VM->>CDL: loadData(endpoint, config, params + search="Bogota" + search_fields=...)
    CDL->>Backend: GET /api/v1/schools?search=Bogota&search_fields=name,city

    alt Red disponible y respuesta exitosa
        Backend-->>CDL: { items: [escuelas con "Bogota"], total: 3 }
        CDL-->>VM: Result.Success(serverResults)
        VM->>DS: DataState.Success(serverResults, isOfflineFiltered=false)
        DS->>DS: reemplazar con resultados del servidor (indicador verde)
    else Sin red o error
        Backend-->>CDL: Error de conexion
        CDL-->>VM: Result.Failure
        Note over VM: Mantener resultados locales del paso 1
        Note over DS: UI mantiene indicador naranja "Resultados locales"
    end
```

---

## Componentes UI de Conectividad

### ConnectivityBanner

Implementado en `kmp-screens/.../dynamic/components/ConnectivityBanner.kt`.

```mermaid
flowchart LR
    subgraph estados ["Estados del banner"]
        E1["isOnline=false"]
        E2["syncState=Syncing"]
        E3["pendingCount > 0"]
        E4["isOnline=true\nsin pendientes"]
    end

    subgraph visual ["Apariencia"]
        V1["Fondo amber\nTexto naranja\n'Sin conexión - usando datos guardados'"]
        V2["Fondo azul\nSpinner + texto\n'Sincronizando N/M...'"]
        V3["Fondo azul\nTexto azul\n'Sincronizando N cambios pendientes...'"]
        V4["Banner oculto\n(AnimatedVisibility collapse)"]
    end

    E1 --> V1
    E2 --> V2
    E3 --> V3
    E4 --> V4
```

**Parametros:**
```kotlin
@Composable
fun ConnectivityBanner(
    isOnline: Boolean,
    pendingMutationCount: Int,
    syncState: SyncEngine.SyncState,
    modifier: Modifier = Modifier,
)
```

Usa `AnimatedVisibility` con `expandVertically()`/`shrinkVertically()` para transiciones suaves.

### StaleDataIndicator

Implementado en `kmp-screens/.../dynamic/components/StaleDataIndicator.kt`.

```kotlin
@Composable
fun StaleDataIndicator(isStale: Boolean, modifier: Modifier)
```

- Chip amarillo con texto "Datos en cache"
- Visible solo cuando `isStale=true` (retornado por `CachedDataResult`)
- Colores: fondo `#FFF8E1`, texto `#F57F17`
- Implementado como `SuggestionChip` de Material3

---

## Manejo de Token Expirado sin Red

```mermaid
flowchart TD
    A[Request con token] --> B{Token expirado?}
    B -- No --> C[Request normal]
    B -- Si --> D[TokenRefreshManager.forceRefresh]
    D --> E{Red disponible?}
    E -- Si --> F[POST /api/v1/auth/refresh]
    F --> G{Respuesta?}
    G -- 200 nuevo token --> H[Actualizar storage + authState\nReintentar request original]
    G -- 401/403 --> I[Razon: TokenExpired / TokenRevoked]
    I --> J[clearSession / logout forzado]
    J --> K[navState.replaceAll Login]
    E -- No --> L[Razon: NetworkError]
    L --> M[No cerrar sesion\nMantener Authenticated]
    M --> N[Request queda pendiente\nUI puede seguir navegando\ncon cache disponible]
```

---

## Invalidacion de Cache tras Mutacion

```mermaid
sequenceDiagram
    participant VM as DynamicScreenViewModel
    participant CDL as CachedDataLoader
    participant MQ as MutationQueue
    participant Mem as Memoria
    participant Storage as SafeEduGoStorage

    VM->>CDL: submitData("admin:/api/v1/schools", newSchool, "POST")

    alt Online y request exitoso
        Note over CDL: Request POST exitoso (201 Created)
        CDL->>CDL: invalidateByPrefix("admin:/api/v1/schools")
        CDL->>Mem: eliminar keys que contienen\n"admin:/api/v1/schools"
        Note over VM,Storage: Al volver a schools-list,\nse recarga desde backend
    else Offline o request fallo
        CDL->>MQ: enqueue(PendingMutation)
        CDL-->>VM: Result.Success(null)
        Note over VM: UI muestra "Guardado localmente,\nse sincronizará al reconectar"
    end
```

---

## Submitform: Flujo Completo Online/Offline

```mermaid
sequenceDiagram
    participant UI as DynamicScreen
    participant VM as DynamicScreenViewModel
    participant CDL as CachedDataLoader
    participant MQ as MutationQueue
    participant Backend as API Backend

    UI->>VM: submitForm(endpoint, method, fieldValues)
    VM->>VM: filtrar solo campos del form-section\nconvertir tipos (int/bool/string)
    VM->>CDL: submitData(endpoint, body, method)

    alt Online
        CDL->>Backend: POST/PUT endpoint
        alt Exito
            Backend-->>CDL: 201 Created / 200 OK
            CDL->>CDL: invalidateByPrefix
            CDL-->>VM: Result.Success(response)
            VM-->>UI: EventResult.Success("Guardado exitosamente")
        else Fallo con MutationQueue
            CDL->>MQ: enqueue(mutation)
            CDL-->>VM: Result.Success(null)
            VM-->>UI: EventResult.Success("Guardado localmente, se sincronizará al reconectar")
        end
    else Offline
        CDL->>MQ: enqueue(mutation)
        CDL-->>VM: Result.Success(null)
        VM-->>UI: EventResult.Success("Guardado localmente, se sincronizará al reconectar")
    end
```

---

## Comportamiento por Escenario

```mermaid
flowchart LR
    subgraph escenarios ["Escenarios de Conectividad"]
        E1["App nueva sin datos\nSin red"]
        E2["App con cache\nSin red"]
        E3["App con cache\nRed lenta / timeout"]
        E4["App con cache\nRed normal"]
        E5["App nueva\nRed normal"]
        E6["App offline\nUsuario edita form"]
        E7["App vuelve online\nCon mutaciones pendientes"]
    end

    subgraph comportamiento ["Comportamiento"]
        B1["Error: Sin conexion\nNo hay nada que mostrar"]
        B2["Mostrar datos cacheados\nisStale=true, chip amarillo\nBanner amber 'Sin conexion'"]
        B3["Mostrar cache mientras carga\nActualizar cuando llegue respuesta"]
        B4["Cache de memoria si fresco segun CacheConfig\nO GET fresco a backend"]
        B5["Spinner + GET backend\nGuardar en cache L1+L2"]
        B6["MutationQueue.enqueue\nUI muestra exito optimista\nBadge de pendientes"]
        B7["SyncEngine.processQueue\nBanner azul 'Sincronizando N/M'\nEvict pantallas recientes\nReload pantalla actual"]
    end

    E1 --> B1
    E2 --> B2
    E3 --> B3
    E4 --> B4
    E5 --> B5
    E6 --> B6
    E7 --> B7
```

---

## DI: Registro de Componentes Offline

Todos los componentes estan registrados en Koin via `networkModule` y `dynamicUiModule`:

```kotlin
// networkModule
single<NetworkObserver> { createNetworkObserver() }

// dynamicUiModule
single { CacheConfig() }
single { RecentScreenTracker() }
single { MutationQueue(get<SafeEduGoStorage>()) }
single { ConflictResolver() }
single { SyncEngine(get(), remoteDataLoader, get(), get()) }
single { ConnectivitySyncManager(get(), get(), get(), screenLoader as CachedScreenLoader) }

// CachedDataLoader recibe NetworkObserver y MutationQueue
single<DataLoader> {
    CachedDataLoader(
        remote = remote,
        storage = get(),
        cacheConfig = get(),
        networkObserver = getOrNull<NetworkObserver>(),
        mutationQueue = get(),
    )
}

// ViewModel recibe NetworkObserver, RecentScreenTracker, MutationQueue
factory {
    DynamicScreenViewModel(
        networkObserver = getOrNull<NetworkObserver>(),
        recentScreenTracker = get(),
        mutationQueue = get(),
    )
}
```

**Nota:** `getOrNull<NetworkObserver>()` se usa en varios sitios para manejar gracefully el caso donde la plataforma no provee un observer (ej: Android sin Context inyectado).

**Nota:** `DynamicScreenViewModel.isOnline` es un `StateFlow<Boolean>` reactivo que delega directamente al `networkObserver.status`. Ya NO usa snapshots manuales (`updateOnlineStatus()` fue eliminado). Cualquier cambio en el `NetworkObserver` se refleja inmediatamente en la UI via Compose `collectAsState()`.

---

## Diferencias por Plataforma en Offline

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Deteccion de conectividad | `ConnectivityManager` callback (inmediata) | `NWPathMonitor` (inmediata) | HTTP HEAD polling cada 30s | `navigator.onLine` + eventos (inmediata) |
| Estado LOSING | `onLosing()` callback | `nw_path_status_satisfiable` | No soportado (solo AVAILABLE/UNAVAILABLE) | No soportado |
| Offline queue | MutationQueue (SafeEduGoStorage) | MutationQueue (SafeEduGoStorage) | MutationQueue (SafeEduGoStorage) | MutationQueue (SafeEduGoStorage) |
| Sync al reconectar | SyncEngine + ConnectivitySyncManager | SyncEngine + ConnectivitySyncManager | SyncEngine + ConnectivitySyncManager | SyncEngine + ConnectivitySyncManager |
| Background sync | No implementado (WorkManager disponible) | No implementado (Background App Refresh disponible) | No aplica | No implementado (Service Worker disponible) |
| Cache persistencia | SharedPreferences (sobrevive reinicio) | NSUserDefaults (sobrevive reinicio) | Java Preferences (sobrevive reinicio) | localStorage (sobrevive reinicio) |
| Limpieza de cache | Manual con `clearCache()` | Manual con `clearCache()` | Manual con `clearCache()` | Manual con `clearCache()` |

---

## Archivos Relevantes

| Componente | Ruta |
|-----------|------|
| NetworkObserver (interfaz + expect) | `modules/network/src/commonMain/.../connectivity/NetworkObserver.kt` |
| Android NetworkObserver | `modules/network/src/androidMain/.../connectivity/NetworkObserver.android.kt` |
| iOS NetworkObserver | `modules/network/src/iosMain/.../connectivity/NetworkObserver.ios.kt` |
| Desktop NetworkObserver | `modules/network/src/desktopMain/.../connectivity/NetworkObserver.jvm.kt` |
| WasmJS NetworkObserver | `modules/network/src/wasmJsMain/.../connectivity/NetworkObserver.wasmJs.kt` |
| PendingMutation + MutationStatus | `modules/dynamic-ui/src/commonMain/.../offline/PendingMutation.kt` |
| MutationQueue | `modules/dynamic-ui/src/commonMain/.../offline/MutationQueue.kt` |
| SyncEngine | `modules/dynamic-ui/src/commonMain/.../offline/SyncEngine.kt` |
| ConflictResolver | `modules/dynamic-ui/src/commonMain/.../offline/ConflictResolver.kt` |
| ConnectivitySyncManager | `modules/dynamic-ui/src/commonMain/.../offline/ConnectivitySyncManager.kt` |
| CacheConfig | `modules/dynamic-ui/src/commonMain/.../cache/CacheConfig.kt` |
| RecentScreenTracker | `modules/dynamic-ui/src/commonMain/.../cache/RecentScreenTracker.kt` |
| CachedDataLoader | `modules/dynamic-ui/src/commonMain/.../data/CachedDataLoader.kt` |
| ConnectivityBanner | `kmp-screens/src/commonMain/.../dynamic/components/ConnectivityBanner.kt` |
| StaleDataIndicator | `kmp-screens/src/commonMain/.../dynamic/components/StaleDataIndicator.kt` |
| DI NetworkModule | `modules/di/src/commonMain/.../module/NetworkModule.kt` |
| DI DynamicUiModule | `modules/di/src/commonMain/.../module/DynamicUiModule.kt` |

---

## Optimizaciones de Rendimiento: Primera Carga (Sprint 8)

### Flujo Optimizado del SplashScreen

El splash screen ejecuta las operaciones de restauración en paralelo para minimizar el tiempo total:

```mermaid
sequenceDiagram
    participant Splash as SplashScreen
    participant Auth as AuthService
    participant DSS as DataSyncService
    participant Store as LocalSyncStore
    participant Repo as SyncRepository
    participant Backend as IAM Platform

    Note over Splash: Fase 1: Paralelo
    par restoreSession() + restoreFromLocal()
        Splash->>Auth: restoreSession()
        Auth->>Auth: leer token de storage
        alt token expirado
            Auth->>Backend: POST /api/v1/auth/refresh
            Backend-->>Auth: nuevo token
        end
    and
        Splash->>DSS: restoreFromLocal()
        DSS->>Store: loadBundle()
        Note over Store: Deserializa 21 screens EN PARALELO<br/>via withContext(Dispatchers.Default) + async
        Store-->>DSS: UserDataBundle (o null)
        DSS->>DSS: seedScreenLoader(screens) en paralelo
    end

    Note over Splash: Fase 2: Solo si autenticado
    par deltaSync() + splash delay
        Splash->>DSS: deltaSync()
        DSS->>Store: getHashes()
        DSS->>Repo: POST /api/v1/sync/delta { hashes }
        Backend-->>Repo: DeltaSyncResponse { changed, unchanged }
        DSS->>DSS: applyDeltaToBundle(base, changed)
        Note over DSS: Construye bundle actualizado IN-MEMORY<br/>sin recargar 21 screens de storage
        DSS->>Store: persistDeltaChanges(changed)
        Note over Store: Solo persiste los buckets que cambiaron
    and
        Splash->>Splash: delay(splashMs)
    end

    Splash->>Splash: onNavigateToHome()
```

### Comparación: Antes vs Después

| Paso | Antes | Después |
|------|-------|---------|
| Auth + restoreFromLocal | Secuencial (~1.5-3s) | Paralelo (~max 1-2s) |
| Screen deserialization (21 screens) | Secuencial (~300-840ms) | Paralelo ~4 cores (~80-200ms) |
| seedFromBundle serialization | Secuencial (~150-420ms) | Paralelo (~40-100ms) |
| Delta sync → reload bundle | Recargaba TODO de storage (~300-840ms) | Incremental en memoria (~10-50ms) |
| **Total primera carga** | **~3.5-5s** | **~1.5-2.5s** |

---

## Mejoras Implementadas vs Propuestas

### Completadas

| Mejora | Estado | Implementacion |
|--------|--------|---------------|
| NetworkObserver multiplataforma | HECHO | 4 implementaciones: Android, iOS, Desktop, WasmJS |
| Offline mutation queue | HECHO | MutationQueue con deduplicacion, persistencia, limites |
| Sync automatico al reconectar | HECHO | SyncEngine observa NetworkObserver, procesa cola |
| Banner de estado de red | HECHO | ConnectivityBanner con 3 estados (offline/syncing/pending) |
| Indicador de datos stale | HECHO | StaleDataIndicator chip amarillo + CachedDataResult.isStale |
| Cache TTL por tipo | HECHO | CacheConfig con TTLs por ScreenPattern + overrides por screenKey |
| Conflict resolution basico | HECHO | ConflictResolver con last-write-wins y skip para entidades eliminadas |
| Re-sync de pantallas recientes | HECHO | ConnectivitySyncManager + RecentScreenTracker evict + reload |
| isOnline reactivo en ViewModel | HECHO | `DynamicScreenViewModel.isOnline` ahora es StateFlow reactivo directo del `NetworkObserver.status`, eliminando snapshots manuales y race conditions al arranque |
| Paralelización del splash screen | HECHO | `restoreSession()` y `restoreFromLocal()` corren en paralelo. `deltaSync()` corre en paralelo con splash delay |
| Paralelización de screen I/O | HECHO | `LocalSyncStore.loadAllScreens()` y `CachedScreenLoader.seedFromBundle()` serializan/deserializan 21 screens en paralelo via `withContext(Dispatchers.Default)` + `async` |
| Delta sync incremental | HECHO | `DataSyncService.deltaSync()` construye bundle actualizado en memoria (`applyDeltaToBundle`) en lugar de recargar todo el bundle de storage |

### Pendientes

| Mejora | Justificacion | Prioridad |
|--------|--------------|-----------|
| Conflict resolution avanzado | Estrategias mas sofisticadas: merge, prompts al usuario, diff visual | Media |
| Background sync nativo | WorkManager (Android) / Background App Refresh (iOS) para sync sin app en foreground | Baja |
| Retry UI para mutaciones fallidas | Pantalla donde el usuario ve mutaciones FAILED/CONFLICTED y puede reintentar o descartar | Media |
| Sync parcial de pantallas | Actualmente evict + full reload; optimizar a delta refresh con hashes | Baja |
| Indicador visual en items stale | Items individuales de lista con timestamp de cache mostrando antiguedad | Baja |
