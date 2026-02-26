# 10 — Multiidioma (i18n)

> Diseño de arquitectura para internacionalizacion en EduGo KMP.
> Usa el estandar de JetBrains (composeResources) para strings del sistema.
> La terminologia por institucion (Grado/Level/Modulo) es un tema SEPARADO
> documentado en [11-conceptos-terminologia.md](./11-conceptos-terminologia.md).

---

## Estado Actual

```mermaid
flowchart TD
    subgraph actual ["Como funciona hoy (solo español)"]
        A["kmp-resources/Strings.kt\n(expect interface, 43 strings)"]
        B["Android: strings.xml\n(44 entries)"]
        C["iOS/Desktop/WasmJS:\nhardcoded Spanish"]
        D["12 strings sueltos\nen DynamicToolbar,\nSchoolSelector, etc."]
        E["SDUI labels: del backend\n(menu, form fields, page_title)"]
    end

    A --> B
    A --> C
    D --> |"no usan Strings"| actual
    E --> |"ya server-driven"| actual
```

### Inventario de strings

| Categoria | Cantidad | Fuente actual | Ejemplo |
|-----------|:--------:|---------------|---------|
| Framework UI (botones, nav) | ~43 | `kmp-resources/Strings` | "Guardar", "Cancelar", "Cerrar sesion" |
| Strings sueltos hardcoded | ~12 | Directos en .kt | "Volver", "Nuevo", "Seleccionar escuela" |
| SDUI labels (del backend) | ~100+ | ScreenDefinition.slotData | `page_title`, `label`, `placeholder` |
| Menu items | ~15 | Sync bundle (MenuItem.displayName) | "Escuelas", "Materias", "Dashboard" |
| Mensajes de error API | Variable | Backend responses | "No active membership" |

---

## Diseño Propuesto: Arquitectura de 2 Capas

> **Nota:** La terminologia por institucion (Grado/Level/Modulo, Estudiante/Participante)
> NO es un problema de idioma. Es un problema de **conceptos** resuelto con el sistema
> de `school_concepts` documentado en [doc 11](./11-conceptos-terminologia.md).

```mermaid
flowchart TB
    subgraph L1 ["CAPA 1: Strings locales (composeResources)"]
        direction LR
        CR["composeResources/\nvalues/strings.xml (en - fallback)\nvalues-es/strings.xml (es - base)\nvalues-pt-rBR/strings.xml (portugues)"]
        RES["Res.string.action_save\nRes.string.error_network\nRes.string.offline_banner"]
        CR --> RES
    end

    subgraph L2 ["CAPA 2: SDUI pre-traducido por backend"]
        direction LR
        SDUI["ScreenDefinition:\nslotData traducido segun locale"]
        MENU["MenuItem.displayName:\ntraducido segun locale"]
        GLOSS["school_concepts:\nterminologia de la institucion"]
        SDUI --- MENU --- GLOSS
    end

    UI["Composable UI"] --> L1
    UI --> L2

    style L1 fill:#e8f5e9
    style L2 fill:#fff3e0
```

### Que va en cada capa

| Capa | Que contiene | Cuando se usa | Offline? |
|------|-------------|---------------|:--------:|
| **L1: Local** | Botones ("Guardar"), errores del sistema, estados vacios, conectividad, accesibilidad | Siempre disponible, no depende de red | ✅ Siempre |
| **L2: Backend** | Labels SDUI (page_title, field labels), menu items, terminologia de la institucion (via school_concepts) | Viaja en sync bundle, cached localmente | ✅ Cache |

### Idioma vs Terminologia (son independientes)

| | Idioma (i18n) | Terminologia (conceptos) |
|---|---|---|
| **Pertenece a** | Usuario | Institucion |
| **Cuando cambia** | En Settings | Al hacer switchContext |
| **Afecta** | "Guardar"→"Save", "Sin conexion"→"No connection" | "Grado"→"Level", "Estudiante"→"Participant" |
| **Documentado en** | Este documento (10) | [Doc 11](./11-conceptos-terminologia.md) |
| **Mecanismo** | composeResources locales | school_concepts via sync bundle |

---

## Capa 1: composeResources (Estandar JetBrains)

### Estructura de directorios

```
kmp-screens/src/commonMain/
  composeResources/
    values/                          ← Fallback (ingles)
      strings.xml
    values-es/                       ← Español base
      strings.xml
    values-es-rCO/                   ← Colombia (overrides)
      strings.xml
    values-es-rAR/                   ← Argentina (overrides)
      strings.xml
    values-es-rMX/                   ← Mexico (overrides)
      strings.xml
    values-pt-rBR/                   ← Portugues Brasil
      strings.xml
```

### Formato de los XML

```xml
<!-- values/strings.xml (ingles - fallback) -->
<resources>
    <!-- Actions -->
    <string name="action_save">Save</string>
    <string name="action_cancel">Cancel</string>
    <string name="action_delete">Delete</string>
    <string name="action_back">Back</string>
    <string name="action_create">New</string>
    <string name="action_edit">Edit</string>
    <string name="action_logout">Log out</string>
    <string name="action_switch_context">Switch context</string>
    <string name="action_retry">Retry</string>
    <string name="action_search">Search</string>

    <!-- Connectivity -->
    <string name="offline_banner">No connection — using saved data</string>
    <string name="offline_syncing">Syncing %1$d/%2$d…</string>
    <string name="offline_pending">Syncing %1$d pending changes…</string>
    <string name="stale_data_indicator">Cached data</string>
    <string name="saved_locally">Saved locally, will sync on reconnect</string>

    <!-- School selection -->
    <string name="school_selection_title">Select a school</string>
    <string name="school_selection_subtitle">Select a school to continue</string>
    <string name="school_selection_empty">No schools available</string>
    <string name="school_selection_error">Error loading schools: %1$s</string>

    <!-- Form -->
    <string name="form_required_field">This field is required</string>
    <string name="form_invalid_email">Invalid email address</string>
    <string name="form_save_success">Saved successfully</string>
    <string name="form_save_error">Error saving: %1$s</string>
    <string name="form_edit_title">Edit %1$s</string>
    <string name="form_create_title">New %1$s</string>

    <!-- Empty states -->
    <string name="empty_list">No items found</string>
    <string name="empty_search">No results for "%1$s"</string>

    <!-- Errors -->
    <string name="error_generic">Something went wrong</string>
    <string name="error_no_permission">You don't have permission for this action</string>
    <string name="error_session_expired">Session expired, please log in again</string>
    <string name="error_network">Connection error, try again</string>

    <!-- Plurals -->
    <plurals name="pending_changes">
        <item quantity="one">%1$d pending change</item>
        <item quantity="other">%1$d pending changes</item>
    </plurals>
</resources>
```

```xml
<!-- values-es/strings.xml (español base) -->
<resources>
    <string name="action_save">Guardar</string>
    <string name="action_cancel">Cancelar</string>
    <string name="action_delete">Eliminar</string>
    <string name="action_back">Volver</string>
    <string name="action_create">Nuevo</string>
    <string name="action_edit">Editar</string>
    <string name="action_logout">Cerrar sesion</string>
    <string name="action_switch_context">Cambiar contexto</string>
    <string name="action_retry">Reintentar</string>
    <string name="action_search">Buscar</string>

    <string name="offline_banner">Sin conexion — usando datos guardados</string>
    <string name="offline_syncing">Sincronizando %1$d/%2$d…</string>
    <string name="offline_pending">Sincronizando %1$d cambios pendientes…</string>
    <string name="stale_data_indicator">Datos en cache</string>
    <string name="saved_locally">Guardado localmente, se sincronizara al reconectar</string>

    <string name="school_selection_title">Seleccionar escuela</string>
    <string name="school_selection_subtitle">Selecciona una escuela para continuar</string>
    <string name="school_selection_empty">No hay escuelas disponibles</string>
    <string name="school_selection_error">Error cargando escuelas: %1$s</string>

    <string name="form_required_field">Este campo es obligatorio</string>
    <string name="form_invalid_email">Direccion de correo invalida</string>
    <string name="form_save_success">Guardado exitosamente</string>
    <string name="form_save_error">Error al guardar: %1$s</string>
    <string name="form_edit_title">Editar %1$s</string>
    <string name="form_create_title">Nuevo %1$s</string>

    <string name="empty_list">No se encontraron elementos</string>
    <string name="empty_search">Sin resultados para "%1$s"</string>

    <string name="error_generic">Ocurrio un error</string>
    <string name="error_no_permission">No tienes permiso para esta accion</string>
    <string name="error_session_expired">Sesion expirada, inicia sesion nuevamente</string>
    <string name="error_network">Error de conexion, intenta de nuevo</string>

    <plurals name="pending_changes">
        <item quantity="one">%1$d cambio pendiente</item>
        <item quantity="other">%1$d cambios pendientes</item>
    </plurals>
</resources>
```

### Uso en Composables

```kotlin
// Uso basico
Text(stringResource(Res.string.action_save))

// Con parametros
Text(stringResource(Res.string.form_edit_title, "Escuela"))
Text(stringResource(Res.string.offline_syncing, current, total))

// Plurales
Text(pluralStringResource(Res.plurals.pending_changes, count, count))

// Fuera de Composable (suspend)
val msg = getString(Res.string.form_save_success)
```

### Convencion de nombres de keys

```
{categoria}.{contexto}_{elemento}

Categorias:
  action_*          → Botones y acciones del usuario
  offline_*         → Mensajes de conectividad
  stale_*           → Indicadores de datos en cache
  school_selection_* → Pantalla de seleccion de escuela
  form_*            → Formularios genericos
  empty_*           → Estados vacios
  error_*           → Mensajes de error
  nav_*             → Navegacion
  auth_*            → Autenticacion
```

---

## Terminologia por Institucion → Ver Doc 11

La terminologia dinamica (Grado/Level/Modulo, Estudiante/Participante) se resuelve
con el sistema de **concept_types** y **school_concepts** documentado en
[11-conceptos-terminologia.md](./11-conceptos-terminologia.md).

No es un problema de idioma. Es una configuracion por institucion que viaja
en el sync bundle como bucket `glossary`.

---

## Seccion Legacy: Strings del Servidor (Sync Bundle)

### Por que tener strings del servidor

```mermaid
flowchart LR
    subgraph problema ["Problema sin strings del servidor"]
        P1["'Grado' en Colombia"]
        P2["'Año' en Argentina"]
        P3["'Ciclo' en Mexico"]
        P4["Para cambiar: nuevo deploy\nen todas las plataformas"]
    end

    subgraph solucion ["Solucion: strings en BD"]
        S1["Backend sirve strings\nsegun locale del usuario"]
        S2["Viajan en sync bundle"]
        S3["Cambiar un string =\nUPDATE en BD"]
        S4["Sin deploy, sin app store"]
    end

    problema --> |"con i18n server"| solucion
```

### Modelo de datos en backend

```mermaid
erDiagram
    i18n_strings {
        uuid id PK
        string key "ej: edu.grade_label"
        string locale "ej: es-CO"
        string value "ej: Grado"
        string namespace "ej: education"
        timestamp created_at
        timestamp updated_at
    }

    i18n_namespaces {
        uuid id PK
        string name "ej: education, common, admin"
        string description
        boolean is_active
    }

    i18n_strings }o--|| i18n_namespaces : "pertenece a"
```

### Estructura del namespace

```
education.*         → Terminologia educativa que varia por pais
  education.grade_label        → "Grado" / "Año" / "Ciclo"
  education.period_label       → "Periodo" / "Cuatrimestre" / "Bimestre"
  education.school_label       → "Colegio" / "Escuela" / "Institucion"
  education.subject_label      → "Materia" / "Asignatura"
  education.student_label      → "Estudiante" / "Alumno"
  education.teacher_label      → "Docente" / "Profesor"
  education.guardian_label     → "Acudiente" / "Tutor" / "Apoderado"

admin.*             → Labels administrativos
  admin.dashboard_title        → "Panel de control"
  admin.users_section          → "Usuarios"

notifications.*     → Mensajes que se quieren cambiar sin deploy
  notifications.welcome        → "Bienvenido a EduGo"
  notifications.maintenance    → "Mantenimiento programado para..."
```

### Viaje en el Sync Bundle

```mermaid
sequenceDiagram
    participant Client as KMP Client
    participant Sync as SyncService
    participant DB as PostgreSQL
    participant Cache as LocalSyncStore

    Note over Client: Login / fullSync
    Client->>Sync: GET /api/v1/sync/bundle
    Note over Sync: Header Accept-Language: es-CO

    Sync->>DB: SELECT key, value FROM i18n_strings<br/>WHERE locale IN ('es-CO', 'es', 'en')<br/>ORDER BY locale DESC
    Note over DB: Prioridad: es-CO > es > en<br/>DISTINCT ON (key) retorna<br/>el mas especifico

    DB-->>Sync: { "edu.grade_label": "Grado", ... }

    Sync-->>Client: SyncBundleResponse {<br/>  ...,<br/>  strings: { "edu.grade_label": "Grado", ... },<br/>  hashes: { ..., "strings": "abc123" }<br/>}

    Client->>Cache: sync.strings = Map<String, String>
    Note over Cache: Disponible offline
```

### Nuevo bucket en el bundle

```kotlin
// SyncBundleResponse (backend)
data class SyncBundleResponse(
    val menu: List<MenuItem>,
    val permissions: List<String>,
    val screens: Map<String, ScreenBundleEntry>,
    val availableContexts: List<UserContext>,
    val strings: Map<String, String>,         // ← NUEVO
    val hashes: Map<String, String>,
)

// UserDataBundle (KMP)
data class UserDataBundle(
    val menu: MenuResponse,
    val permissions: List<String>,
    val screens: Map<String, ScreenDefinition>,
    val availableContexts: List<UserContext>,
    val strings: Map<String, String>,         // ← NUEVO
    val hashes: Map<String, String>,
    val syncedAt: Instant,
)
```

### StringResolver: Resolucion unificada

```mermaid
flowchart TD
    A["stringResolver.get('edu.grade_label')"] --> B{Existe en sync bundle?}
    B -- Si --> C["Retorna valor del servidor\n'Grado' (es-CO)"]
    B -- No --> D{Existe en composeResources?}
    D -- Si --> E["Retorna valor local\nRes.string.edu_grade_label"]
    D -- No --> F["Retorna key como fallback\n'edu.grade_label'"]

    G["stringResource(Res.string.action_save)"] --> H["Siempre resuelve local\n(no necesita servidor)"]

    style C fill:#e3f2fd
    style E fill:#e8f5e9
    style F fill:#ffebee
```

```kotlin
// StringResolver.kt (commonMain)
class StringResolver(
    private val dataSyncService: DataSyncService,
) {
    /**
     * Busca un string primero en el bundle del servidor,
     * luego hace fallback al valor local proporcionado.
     */
    fun get(key: String, localFallback: String? = null): String {
        val bundle = dataSyncService.currentBundle.value
        return bundle?.strings?.get(key)
            ?: localFallback
            ?: key
    }

    /**
     * Para uso en Composable con fallback a composeResources.
     */
    @Composable
    fun resolve(key: String, localResource: StringResource): String {
        val serverValue = get(key)
        return if (serverValue != key) serverValue
               else stringResource(localResource)
    }
}
```

### Uso en la UI

```kotlin
@Composable
fun GradeField(resolver: StringResolver) {
    // Prioridad: servidor (puede variar por pais) > local (fallback)
    val label = resolver.resolve(
        key = "edu.grade_label",
        localResource = Res.string.edu_grade_label
    )
    DSTextField(label = label, ...)
}

// Para strings que NUNCA necesitan servidor (acciones basicas):
Text(stringResource(Res.string.action_save))  // Siempre local
```

---

## Capa 3: SDUI Pre-traducido

### Como funciona (ya implementado parcialmente)

```mermaid
flowchart LR
    subgraph Backend
        SD["ScreenDefinition\nslot_data.page_title = 'Escuelas'\nslot.label = 'Nombre completo'\nslot.placeholder = 'Ingrese el nombre'"]
        MI["MenuItem\ndisplay_name = 'Escuelas'"]
    end

    subgraph Bundle ["Sync Bundle"]
        SB["Viaja con locale del usuario\nBackend traduce antes de enviar"]
    end

    subgraph KMP
        DT["DynamicToolbar\ntitle = slotData['page_title']"]
        FR["FormRenderer\nlabel = slot.label"]
        MN["MainScreen\nmenuItem.displayName"]
    end

    Backend --> Bundle --> KMP
```

### Que debe cambiar en el backend

El backend debe resolver el locale del usuario al construir:
- `ScreenDefinition.slotData` → labels, titles, placeholders
- `MenuItem.displayName` → nombres del menu
- `DataConfig.fieldMapping` → podria incluir labels traducidos

```
// Actual: slotData esta en español fijo en la BD
{ "page_title": "Escuelas", "edit_title": "Editar Escuela" }

// Propuesto: slotData tiene keys, backend resuelve antes de enviar
// BD almacena:
{ "page_title": "i18n:schools.list.title", "edit_title": "i18n:schools.form.edit_title" }

// Backend detecta prefijo "i18n:" y resuelve contra i18n_strings
// Al enviar al cliente (locale es-CO):
{ "page_title": "Colegios", "edit_title": "Editar Colegio" }
// Al enviar al cliente (locale es-AR):
{ "page_title": "Escuelas", "edit_title": "Editar Escuela" }
```

---

## Cambio de Idioma en Runtime

### Flujo del usuario

```mermaid
sequenceDiagram
    participant User as Usuario
    participant Settings as SettingsScreen
    participant AppLocale as LocalAppLocale
    participant Sync as DataSyncService
    participant Backend as IAM Platform
    participant UI as App (recompose)

    User->>Settings: selecciona "Portugues (Brasil)"
    Settings->>Settings: guardar preferencia en storage
    Settings->>AppLocale: customAppLocale = "pt-BR"

    Note over AppLocale: Actualiza Locale del sistema<br/>(expect/actual por plataforma)

    AppLocale->>UI: key(customAppLocale) fuerza recomposicion
    Note over UI: Todos los stringResource() se re-resuelven<br/>con el nuevo locale (Capa 1 ✅)

    Settings->>Sync: fullSync() con nuevo locale
    Sync->>Backend: GET /sync/bundle<br/>Header: Accept-Language: pt-BR
    Backend-->>Sync: Bundle con strings en portugues
    Sync-->>UI: currentBundle actualizado

    Note over UI: Menu, SDUI labels, strings del servidor<br/>se actualizan (Capas 2 y 3 ✅)
```

### Implementacion expect/actual del locale

```kotlin
// commonMain
var customAppLocale by mutableStateOf<String?>(null) // null = sistema

// androidMain
// Usa Locale.setDefault() + resources.updateConfiguration()

// iosMain
// Usa NSUserDefaults.setObject(arrayListOf(locale), "AppleLanguages")

// desktopMain
// Usa Locale.setDefault(Locale(language))

// wasmJsMain
// Usa window.__customLocale override en navigator.languages
```

### Persistencia de preferencia

```mermaid
flowchart LR
    subgraph Storage
        S1["user_prefs.locale → 'pt-BR' (o null = sistema)"]
    end

    subgraph Startup ["Al iniciar la app"]
        A1["Leer user_prefs.locale de storage"]
        A2{Es null?}
        A2 -- Si --> A3["Usar locale del sistema"]
        A2 -- No --> A4["Setear customAppLocale"]
    end

    subgraph SyncBundle ["Al hacer sync"]
        B1["Enviar locale en header Accept-Language"]
        B2["Backend filtra i18n_strings por locale"]
        B3["SDUI slotData resuelto por locale"]
    end

    Storage --> Startup
    Startup --> SyncBundle
```

---

## Cadena de Fallback por Locale

```mermaid
flowchart TD
    REQ["Buscar string para locale 'es-CO'"] --> L1{"¿Existe en es-CO?"}
    L1 -- Si --> R1["Usar valor es-CO\n'Colegio'"]
    L1 -- No --> L2{"¿Existe en es (base)?"}
    L2 -- Si --> R2["Usar valor es\n'Escuela'"]
    L2 -- No --> L3{"¿Existe en en (fallback)?"}
    L3 -- Si --> R3["Usar valor en\n'School'"]
    L3 -- No --> R4["Usar key como texto\n'edu.school_label'"]

    style R1 fill:#e8f5e9
    style R2 fill:#fff3e0
    style R3 fill:#ffebee
    style R4 fill:#ffcdd2
```

**En el backend (SQL):**
```sql
SELECT DISTINCT ON (key) key, value
FROM i18n.i18n_strings
WHERE locale IN ('es-CO', 'es', 'en')
  AND namespace = 'education'
ORDER BY key,
  CASE locale
    WHEN 'es-CO' THEN 1
    WHEN 'es' THEN 2
    WHEN 'en' THEN 3
  END
```

**En el cliente (offline):**
El bundle ya viene con los strings resueltos por el backend.
Si el usuario cambia de idioma offline, solo se actualiza la Capa 1 (composeResources locales).
Las Capas 2 y 3 se actualizan en el proximo sync con red.

---

## Diferencias por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Locale del sistema | `Locale.getDefault()` | `NSLocale.preferredLanguages` | `Locale.getDefault()` | `navigator.languages` |
| Cambio en runtime | `resources.updateConfiguration()` | `NSUserDefaults` + restart compose | `Locale.setDefault()` | Override `navigator.languages` |
| composeResources | Nativo | Nativo | Nativo | Nativo |
| Strings del bundle | SharedPreferences | NSUserDefaults | Java Preferences | localStorage |
| RTL support | Automatico (Compose) | Automatico (Compose) | Automatico (Compose) | Automatico (Compose) |

---

## Migracion desde el sistema actual

### Fase 1: Mover hardcoded strings a composeResources

```mermaid
flowchart TD
    subgraph antes ["Antes"]
        H1["DynamicToolbar.kt:\nText('Guardar')"]
        H2["SchoolSelector.kt:\nText('Seleccionar escuela')"]
        H3["ConnectivityBanner.kt:\nText('Sin conexion...')"]
    end

    subgraph despues ["Despues"]
        D1["DynamicToolbar.kt:\nText(stringResource(Res.string.action_save))"]
        D2["SchoolSelector.kt:\nText(stringResource(Res.string.school_selection_title))"]
        D3["ConnectivityBanner.kt:\nText(stringResource(Res.string.offline_banner))"]
    end

    antes --> |"Fase 1"| despues
```

**Archivos a modificar:**
- `DynamicToolbar.kt` — 5 strings
- `UserMenuHeader.kt` — 2 strings
- `SchoolSelectorScreen.kt` — 4 strings
- `SchoolSelectionScreen.kt` — 1 string
- `ConnectivityBanner.kt` — 3 strings
- `StaleDataIndicator.kt` — 1 string

### Fase 2: Deprecar kmp-resources/Strings

```mermaid
flowchart LR
    OLD["kmp-resources/Strings.kt\n(expect/actual, 43 strings)"]
    NEW["composeResources/\nvalues-es/strings.xml"]

    OLD --> |"migrar 43 strings"| NEW
    OLD --> |"eliminar modulo\nkmp-resources"| GONE["Eliminado"]
```

### Fase 3: Agregar strings del servidor

- Crear tabla `i18n_strings` en backend
- Agregar bucket `strings` al sync bundle
- Implementar `StringResolver` en KMP
- Migrar terminologia educativa a BD

### Fase 4: Agregar segundo idioma

- Crear `values/strings.xml` (ingles)
- Poblar `i18n_strings` con locale `en`
- Implementar selector de idioma en Settings
- Implementar `LocalAppLocale` expect/actual

---

## Que NO hacer

| Anti-patron | Por que evitarlo |
|------------|-----------------|
| Concatenar strings traducidos | `"Hola " + name` se rompe en idiomas con orden de palabras diferente. Usar `stringResource(Res.string.greeting, name)` |
| Reusar keys por coincidencia textual | "Guardar" en un formulario vs "Guardar" en un dialogo de archivo son contextos diferentes. Usar keys separadas |
| Traducir enums en el cliente | No hacer `when(status) { "ACTIVE" -> "Activo" }`. El backend debe enviar `statusDisplay: "Activo"` |
| Meter TODO en el servidor | Strings basicos ("Cancelar", "OK", "Error") deben ser locales para funcionar offline sin sync |
| Hardcodear locale | No hacer `if (locale == "es-CO")`. Usar la cadena de fallback |
| Usar strings como IDs | No hacer `if (buttonText == "Guardar")`. Usar `eventId` o `screenEvent` |

---

## Hash del Bucket de Strings

Para el delta sync, el bucket `strings` se hashea igual que los demas:

```go
// sync_service.go
func (s *SyncService) hashStrings(strings map[string]string) string {
    // Ordenar keys para hash determinista
    keys := make([]string, 0, len(strings))
    for k := range strings {
        keys = append(keys, k)
    }
    sort.Strings(keys)

    var sb strings.Builder
    for _, k := range keys {
        sb.WriteString(k)
        sb.WriteString("=")
        sb.WriteString(strings[k])
        sb.WriteString("\n")
    }
    return fmt.Sprintf("%x", sha256.Sum256([]byte(sb.String())))
}
```

**El hash cambia cuando:** se modifica cualquier string en `i18n_strings` para el locale del usuario.

---

## Mejoras Futuras

| Mejora | Descripcion | Prioridad |
|--------|-------------|-----------|
| ICU MessageFormat | Soporte para plurales complejos y genero en strings del servidor (`{count, plural, one {# escuela} other {# escuelas}}`) | Media |
| Panel de traduccion | UI admin para editar i18n_strings sin SQL directo | Alta (cuando haya >1 idioma) |
| OTA sin sync | Actualizar solo strings sin hacer full/delta sync (endpoint dedicado ligero) | Baja |
| Deteccion automatica de pais | Usar IP o GPS para sugerir locale, no solo el del sistema | Baja |
| Validar completitud | Script que compare keys entre locales y reporte strings faltantes | Media |
| A/B testing de copy | Servir diferentes strings a diferentes usuarios para medir conversion | Baja |
