# 05 — Almacenamiento Local

## Clases Involucradas

```mermaid
classDiagram
    class SafeEduGoStorage {
        -storage: EduGoStorage
        -logger: Logger?
        +putStringSafe(key, value)
        +getStringSafe(key, default): String
        +putIntSafe(key, value)
        +getIntSafe(key, default): Int
        +putBooleanSafe(key, value)
        +getBooleanSafe(key, default): Boolean
        +putObjectSafe~T~(key, value)
        +getObjectSafe~T~(key): T?
        +removeSafe(key)
        +containsSafe(key): Boolean
        +clearSafe()
        +unsafe: EduGoStorage
    }
    class EduGoStorage {
        -settings: Settings
        -keyPrefix: String
        +putString(key, value)
        +getString(key, default): String
        +putObject~T~(key, value)
        +getObject~T~(key): T?
        +remove(key)
        +clear()
        +withSettings(settings): EduGoStorage
    }
    class StorageKeyValidator {
        +validate(key): Boolean
        +isValidKey(key): Boolean
    }
    class Settings {
        <<interface - multiplatform-settings>>
        +putString(key, value)
        +getString(key, defaultValue): String
        +putInt(key, value)
        +remove(key)
        +clear()
        +keys: Set~String~
    }
    class AndroidSettings {
        <<SharedPreferences>>
    }
    class IOSSettings {
        <<NSUserDefaults>>
    }
    class JVMSettings {
        <<java.util.prefs.Preferences>>
    }
    class WasmJSSettings {
        <<localStorage>>
    }
    class LocalSyncStore {
        -storage: SafeEduGoStorage
        -json: Json
        +saveBundle(bundle: UserDataBundle) «suspend»
        +loadBundle(): UserDataBundle? «suspend»
        +getHashes(): Map~String String~
        +updateMenu(menu, hash)
        +updatePermissions(perms, hash)
        +updateContexts(contexts, hash)
        +updateScreen(key, screen, hash)
        +removeScreen(key)
        +updateSyncedAt(instant)
        +clearAll()
    }
    class MutationQueue {
        -storage: SafeEduGoStorage
        -maxMutations: Int
        -mutex: Mutex
        +pendingCount: StateFlow~Int~
        +enqueue(mutation): Boolean
        +dequeue(): PendingMutation?
        +peek(): PendingMutation?
        +markFailed(id)
        +markConflicted(id)
        +remove(id)
        +getAll(): List~PendingMutation~
        +clear()
    }
    class StorageMigrator {
        -storage: SafeEduGoStorage
        -migrations: List~StorageMigration~
        -logger: TaggedLogger
        +migrate(): Int
        +currentVersion(): Int
        +VERSION_KEY: String
    }
    class StorageMigration {
        <<interface>>
        +version: Int
        +migrate(storage: SafeEduGoStorage)
    }

    SafeEduGoStorage --> EduGoStorage : wraps
    SafeEduGoStorage --> StorageKeyValidator
    EduGoStorage --> Settings : delegates
    Settings <|-- AndroidSettings
    Settings <|-- IOSSettings
    Settings <|-- JVMSettings
    Settings <|-- WasmJSSettings
    LocalSyncStore --> SafeEduGoStorage : usa
    MutationQueue --> SafeEduGoStorage : usa
    StorageMigrator --> SafeEduGoStorage : usa
    StorageMigrator --> StorageMigration : ejecuta
```

---

## Jerarquia de Wrappers

```mermaid
flowchart TD
    App["Codigo de aplicacion (AuthService, CachedLoaders...)"]
    Safe["SafeEduGoStorage\n- Valida keys\n- No lanza excepciones\n- Logger de errores"]
    Storage["EduGoStorage\n- Type-safe (serializacion JSON)\n- Key prefix 'com.edugo.'\n- JSON con kotlinx.serialization"]
    Settings["Settings (multiplatform-settings)\n- Interface comun multiplataforma"]
    Platform["Implementacion por plataforma\n(SharedPrefs / NSUserDefaults / JavaPrefs / localStorage)"]

    App --> Safe
    Safe --> Storage
    Storage --> Settings
    Settings --> Platform
```

---

## Keys de Almacenamiento: Mapa Completo

```mermaid
graph LR
    subgraph Auth ["Auth Module"]
        A1["auth_token → AuthToken JSON\n{ accessToken, refreshToken, expiresAt }"]
        A2["auth_user → AuthUserInfo JSON\n{ id, email, fullName, avatarUrl }"]
        A3["auth_context → UserContext JSON\n{ roleId, roleName, schoolId, permissions[] }"]
    end

    subgraph ScreenCache ["Screen Cache (dynamic-ui)"]
        SC1["screen.cache.schools-list → CacheEntry JSON"]
        SC2["screen.cache.schools-form → CacheEntry JSON"]
        SC3["screen.cache.__invalidatedAt → Long timestamp"]
        SCN["screen.cache.{screenKey} → ..."]
    end

    subgraph DataCache ["Data Cache (dynamic-ui)"]
        DC1["data.cache.{hash} → StorageCacheEntry JSON\nKey: 'data:{schoolId}:{endpoint}:{params}'"]
    end

    subgraph SyncBundle ["Sync Bundle (DataSyncService)"]
        SB1["sync.menu → MenuResponse JSON"]
        SB2["sync.permissions → List&lt;String&gt; JSON"]
        SB3["sync.contexts → List&lt;UserContext&gt; JSON"]
        SB4["sync.screen.{key} → ScreenDefinition JSON"]
        SB5["sync.screen_keys → List&lt;String&gt; JSON"]
        SB6["sync.hashes → Map&lt;String, String&gt; JSON"]
        SB7["sync.synced_at → Long (epoch millis)"]
    end

    subgraph MutationStorage ["Mutation Queue (MutationQueue)"]
        MQ1["offline.queue.mutations → List&lt;PendingMutation&gt; JSON"]
    end

    subgraph UserPrefs ["User Preferences"]
        UP1["theme → 'light' | 'dark' | 'system'"]
        UP2["language → 'es' | 'en'"]
    end
```

---

## Flujo de Escritura Segura

```mermaid
flowchart TD
    A[SafeEduGoStorage.putObjectSafe key, value] --> B[StorageKeyValidator.validate key]
    B --> C{Key valida?}
    C -- No --> D[logger.w Invalid key:\nkey]
    C -- Si --> E[EduGoStorage.putObject key, value]
    E --> F[json.encodeToString value]
    F --> G{Serializable?}
    G -- Si --> H[settings.putString key, jsonString]
    G -- No --> I[captura exception\nlogger.e Storage write failed]
    H --> J[Escritura completada]
    I --> K[Retorna sin lanzar exception]
    D --> K
```

---

## Flujo de Lectura con Fallback

```mermaid
flowchart TD
    A[SafeEduGoStorage.getObjectSafe key] --> B[StorageKeyValidator.validate key]
    B --> C{Key valida?}
    C -- No --> D[retorna null]
    C -- Si --> E[settings.getStringOrNull key]
    E --> F{String existe?}
    F -- No --> D
    F -- Si --> G[json.decodeFromString string]
    G --> H{Deserializable?}
    H -- Si --> I[retorna T]
    H -- No --> J[captura exception\nlogger.w Deserialize failed]
    J --> D
```

---

## Estrategia de Cache: Multinivel

```mermaid
flowchart LR
    subgraph L1 ["Nivel 1: Memoria (LinkedHashMap)"]
        M1["screenKey → ScreenDefinition + Instant\nMax 20 entradas (configurable)\nTTL configurable por patron via CacheConfig:\n- Dashboard: 60s\n- List: 5min\n- Form: 60min\n- Detail: 10min\n- Settings: 30min\nNo persiste entre sesiones"]
    end

    subgraph L2 ["Nivel 2: Storage (SafeEduGoStorage)"]
        S1["screen.cache.{key} → CacheEntry JSON\nPersiste entre sesiones\nTTL: mismo CacheConfig por patron\nInvalidacion global por timestamp\nPre-poblado via seedFromBundle()"]
    end

    subgraph L3 ["Nivel 3: Red"]
        N1["GET /api/v1/screen-config/...\nActualiza L1 y L2"]
    end

    subgraph Seed ["Pre-poblado desde Sync Bundle"]
        SB["DataSyncService.fullSync/deltaSync\n→ seedScreenLoader(screens)\n→ CachedScreenLoader.seedFromBundle()\nInyecta en L1 (memoria) y L2 (storage)"]
    end

    L1 --"miss"--> L2
    L2 --"miss"--> L3
    L3 --"actualiza"--> L1
    L3 --"actualiza"--> L2
    Seed --"pre-popula"--> L1
    Seed --"pre-popula"--> L2
```

**Nota sobre CacheConfig:** Los TTLs son configurables por patron (`ScreenPattern`) y tambien por `screenKey` individual via `screenTtlOverrides` y `dataTtlOverrides`. El data cache tiene los mismos TTLs por defecto que el screen cache. El maximo de entradas en memoria es configurable: `maxScreenMemoryEntries = 20`, `maxDataMemoryEntries = 30`.

---

## LocalSyncStore: Flujo de Guardado y Carga

### Guardado del Bundle (fullSync)

```mermaid
sequenceDiagram
    participant DS as DataSyncService
    participant LSS as LocalSyncStore
    participant Storage as SafeEduGoStorage
    participant CSL as CachedScreenLoader

    DS->>DS: fullSync() llamado
    DS->>DS: repository.getBundle() → SyncBundleResponse
    DS->>DS: mapBundleResponse() → UserDataBundle
    DS->>LSS: saveBundle(bundle)
    LSS->>Storage: putStringSafe("sync.menu", menuJson)
    LSS->>Storage: putStringSafe("sync.permissions", permsJson)
    LSS->>Storage: putStringSafe("sync.contexts", contextsJson)
    LSS->>Storage: putStringSafe("sync.hashes", hashesJson)
    LSS->>Storage: putStringSafe("sync.synced_at", epochMillis)
    par cada screen en bundle.screens (Dispatchers.Default)
        LSS->>Storage: async { putStringSafe("sync.screen.{key}", screenJson) }
    end
    Note over LSS: awaitAll() — serialización paralela
    LSS->>Storage: putStringSafe("sync.screen_keys", keysListJson)
    DS->>CSL: seedFromBundle(screens)
    Note over CSL: Inyecta cada screen en<br/>L1 (memoria) y L2 (storage)
    DS->>DS: _currentBundle.value = bundle
    DS->>DS: _syncState.value = Synced(now)
```

### Carga del Bundle (restoreFromLocal)

```mermaid
sequenceDiagram
    participant DS as DataSyncService
    participant LSS as LocalSyncStore
    participant Storage as SafeEduGoStorage
    participant CSL as CachedScreenLoader

    DS->>LSS: loadBundle()
    LSS->>Storage: getStringSafe("sync.menu")
    LSS->>Storage: getStringSafe("sync.permissions")
    LSS->>Storage: getStringSafe("sync.contexts")
    LSS->>Storage: getStringSafe("sync.hashes")
    LSS->>Storage: getStringSafe("sync.synced_at")
    LSS->>Storage: getStringSafe("sync.screen_keys") → lista de keys
    par cada key (Dispatchers.Default)
        LSS->>Storage: async { getStringSafe + decodeFromString }
    end
    Note over LSS: awaitAll() — deserialización paralela de 21 screens
    LSS-->>DS: UserDataBundle (o null si datos corruptos/ausentes)
    alt bundle != null
        DS->>CSL: seedFromBundle(bundle.screens)
        DS->>DS: _currentBundle.value = bundle
        DS->>DS: _syncState.value = Stale(bundle.syncedAt)
    end
```

### Delta Sync (actualizacion parcial)

```mermaid
sequenceDiagram
    participant DS as DataSyncService
    participant LSS as LocalSyncStore
    participant Repo as SyncRepository
    participant CSL as CachedScreenLoader

    DS->>LSS: getHashes() → localHashes
    DS->>Repo: deltaSync(localHashes)
    Repo-->>DS: DeltaSyncResponse(changed)
    loop cada bucket en changed
        alt key == "menu"
            DS->>LSS: updateMenu(menu, hash)
        else key == "permissions"
            DS->>LSS: updatePermissions(perms, hash)
        else key == "available_contexts"
            DS->>LSS: updateContexts(contexts, hash)
        else key.startsWith("screen:")
            DS->>LSS: updateScreen(screenKey, screen, hash)
            DS->>CSL: seedFromBundle({screenKey: screen})
        end
    end
    DS->>DS: applyDeltaToBundle(base, changed, now)
    Note over DS: Construye bundle actualizado IN-MEMORY<br/>sin recargar de storage
    DS->>DS: persistDeltaChanges(changed) → solo guarda buckets modificados
    DS->>LSS: updateSyncedAt(now)
    DS->>DS: _currentBundle.value = updatedBundle
    DS->>CSL: seedScreenLoader(changedScreens) solo screens que cambiaron
```

---

## Invalidacion de Cache

```mermaid
sequenceDiagram
    participant App as Aplicacion
    participant Loader as CachedScreenLoader
    participant Storage as SafeEduGoStorage
    participant Memory as Memoria (LinkedHashMap)

    App->>Loader: clearCache()
    Loader->>Storage: putLong("screen.cache.__invalidatedAt", now())
    Note over Storage: Las entradas antiguas siguen<br/>en storage pero se ignoran<br/>(cachedAt < invalidatedAt)
    Loader->>Memory: clear()

    Note over App,Memory: Al siguiente loadScreen(key):
    App->>Loader: loadScreen("schools-list")
    Loader->>Memory: buscar (vacío)
    Loader->>Storage: buscar entry
    Storage-->>Loader: CacheEntry(cachedAt=T1)
    Loader->>Loader: cachedAt(T1) < invalidatedAt? → Si
    Note over Loader: Entry descartada, va a red
    Loader->>Loader: GET /api/v1/screen-config/...
```

---

## MutationQueue: Flujo Offline

```mermaid
sequenceDiagram
    participant UI as UI (Form)
    participant MQ as MutationQueue
    participant Storage as SafeEduGoStorage
    participant SE as SyncEngine

    Note over UI,SE: Escritura offline
    UI->>MQ: enqueue(PendingMutation)
    MQ->>MQ: verificar duplicado + limite (max 50)
    MQ->>Storage: putStringSafe("offline.queue.mutations", listJson)
    MQ->>MQ: _pendingCount.value = N

    Note over UI,SE: Reconexion
    SE->>MQ: dequeue() → PendingMutation (status → SYNCING)
    SE->>SE: enviar al backend
    alt exito
        SE->>MQ: remove(id)
    else fallo (retryable)
        SE->>MQ: markFailed(id) → retryCount++ o FAILED si max
    else conflicto (409)
        SE->>MQ: markConflicted(id)
    end

    Note over MQ: Al iniciar la app, MutationQueue<br/>restaura automaticamente desde storage<br/>en el bloque init {}
```

---

## Implementaciones por Plataforma

### Android — SharedPreferences

```mermaid
flowchart LR
    EduGoStorage --> SharedPreferences
    SharedPreferences --> |"archivo XML en"| FileSystem["/data/data/com.edugo/shared_prefs/\nedugo_storage.xml"]
    Note1["- Sincronico en modo commit()\n- Backup automático si está habilitado\n- Cifrado posible con EncryptedSharedPreferences"]
```

### iOS — NSUserDefaults

```mermaid
flowchart LR
    EduGoStorage --> NSUserDefaults
    NSUserDefaults --> |"archivo plist en"| FileSystem2["~/Library/Preferences/\ncom.edugo.plist"]
    Note2["- Sincronico\n- NO cifrado por defecto\n- iCloud sync si habilitado"]
```

### Desktop — java.util.prefs

```mermaid
flowchart LR
    EduGoStorage --> JavaPrefs["java.util.prefs.Preferences\nnodo: 'com.edugo.storage'"]
    JavaPrefs --> |"en macOS"| Plist["~/Library/Preferences/\ncom.apple.java.util.prefs.plist\nbajo clave '/com.edugo.storage/'"]
    JavaPrefs --> |"en Windows"| Registry["HKCU\\Software\\JavaSoft\\Prefs\\..."]
    JavaPrefs --> |"en Linux"| XML2["~/.java/.userPrefs/\ncom/edugo/storage/prefs.xml"]
```

### WasmJS — localStorage

```mermaid
flowchart LR
    EduGoStorage --> WebStorage["window.localStorage"]
    WebStorage --> |"almacena"| KV["key-value pairs en el dominio\nvisible en DevTools → Application"]
    Note3["- Limite: ~5MB por dominio\n- No cifrado\n- Accesible por JS"]
```

---

## Tabla Comparativa por Plataforma

| Aspecto | Android | iOS | Desktop macOS | Desktop Windows | WasmJS |
|---------|---------|-----|---------------|-----------------|--------|
| Mecanismo | SharedPreferences | NSUserDefaults | Java Preferences (plist) | Java Preferences (Registry) | localStorage |
| Cifrado nativo | No (EncryptedSharedPrefs opcional) | No (Keychain es separado) | No | No | No |
| Limite de datos | Sin límite práctico | Sin límite práctico | Sin límite práctico | Sin límite práctico | ~5MB |
| Sincronía | Asíncrono (apply) / Síncrono (commit) | Asíncrono | Síncrono | Síncrono | Síncrono |
| Backup automático | Si (Android Backup) | Si (iCloud si configurado) | No | No | No |
| Persistencia entre updates | Si (mismo package) | Si (mismo bundle ID) | Si (mismo preference node) | Si (mismo node) | Si (mismo origen) |
| Accesible sin root | No (solo la app) | No (solo la app) | Si (legible por usuario) | Si (legible por usuario) | Si (via DevTools) |

---

## Mejoras Propuestas

| Mejora | Justificacion | Prioridad | Estado |
|--------|--------------|-----------|--------|
| TTL configurable por pantalla | Algunas pantallas cambian con mas frecuencia que otras (dashboards vs forms) | Media | **HECHO** - CacheConfig implementado con TTLs por ScreenPattern |
| Limite de cache de datos configurable | `MAX_DATA_CACHE_ENTRIES = 30` esta hardcodeado | Media | **HECHO** - CacheConfig tiene `maxDataMemoryEntries` y TTLs por patron |
| Migracion de keys | Al cambiar nombres de keys, los datos existentes se pierden silenciosamente | Media | **HECHO** - `StorageMigrator` + `StorageMigration` implementados (ver seccion abajo) |
| Cifrar tokens en Android Keystore | Los JWT en SharedPrefs son legibles con ADB backup | Alta | Pendiente |
| Cifrar tokens en iOS Keychain | NSUserDefaults es legible en dispositivos con backup | Alta | Pendiente |
| Encriptar toda la cache | Los datos de listas (nombres de alumnos, escuelas) estan en plain text en storage | Alta | Pendiente |
| Compresion de cache grande | Pantallas complejas con muchos slots pueden generar JSON de varios KB | Baja | Pendiente |

---

## StorageMigrator (Implementado)

Sistema de migraciones de esquema para storage local. Permite transformar datos almacenados cuando cambia la estructura de keys o formatos.

### Arquitectura

```mermaid
flowchart TD
    A["App.init / Koin setup"] --> B["StorageMigrator.migrate()"]
    B --> C["Leer version actual\nstorage.schema.version (default: 0)"]
    C --> D{"Hay migraciones pendientes?"}
    D -- No --> E["Retornar version actual\n(nothing to do)"]
    D -- Si --> F["Ordenar migraciones por version"]
    F --> G["Filtrar version > currentVersion"]
    G --> H["Para cada migracion:"]
    H --> I["migration.migrate(storage)"]
    I --> J["storage.putIntSafe(VERSION_KEY, migration.version)"]
    J --> K{"Mas migraciones?"}
    K -- Si --> H
    K -- No --> L["Retornar nueva version"]
```

### API

```kotlin
// Definir una migracion
class MigrationV2 : StorageMigration {
    override val version = 2
    override fun migrate(storage: SafeEduGoStorage) {
        // Renombrar key
        val old = storage.getStringSafe("old_key")
        if (old.isNotEmpty()) {
            storage.putStringSafe("new_key", old)
            storage.removeSafe("old_key")
        }
    }
}

// Ejecutar migraciones
val migrator = StorageMigrator(
    storage = safeStorage,
    migrations = listOf(MigrationV2(), MigrationV3())
)
val newVersion = migrator.migrate() // ejecuta solo las pendientes
```

### Caracteristicas

- **Crash-safe**: cada migracion persiste su version inmediatamente despues de completarse. Si la app crashea a mitad de una migracion, al reiniciar continua desde la ultima completada.
- **Idempotente**: `migrate()` se puede llamar multiples veces; solo ejecuta migraciones con version mayor a la almacenada.
- **Logging**: emite logs de progreso via `TaggedLogger` (`EduGo.Storage.Migrator`).
- **Key de version**: `storage.schema.version` en `SafeEduGoStorage`. |
