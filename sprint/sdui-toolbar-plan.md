# Plan: SDUI Standard Toolbar (ActionBar)

## Objetivo
Implementar una toolbar estándar (TopAppBar) que aparece arriba del contenido en todas las pantallas dinámicas. Proporciona acciones contextuales según el patrón de pantalla (list, form, detail, dashboard).

## Decisiones de diseño
- **Layout**: TopAppBar arriba del content area (al lado del sidebar en desktop)
- **Acciones**: Mínimo viable primero
- **Nuevo**: Via `ScreenEvent.CREATE` (agregar al enum, el contract resuelve navegación)
- **Adaptativo**: En mobile se colapsa a TopAppBar con hamburger menu

## Acciones por patrón

### List Pattern
| Posición | Acción | Tipo |
|----------|--------|------|
| Leading | (ninguno, el sidebar ya navega) | - |
| Title | `slot_data.page_title` | Text |
| Trailing | Botón "Nuevo" (+) | IconButton → ScreenEvent.CREATE |

### Form Pattern
| Posición | Acción | Tipo |
|----------|--------|------|
| Leading | Back arrow (←) | IconButton → navState.back() |
| Title | `slot_data.page_title` o `edit_title` según modo | Text |
| Trailing | "Guardar" | FilledButton → ScreenEvent.SAVE_NEW/SAVE_EXISTING |

### Detail Pattern
| Posición | Acción | Tipo |
|----------|--------|------|
| Leading | Back arrow (←) | IconButton → navState.back() |
| Title | (del data) | Text |
| Trailing | (futuro: editar, eliminar) | - |

### Dashboard/Settings Pattern
| Posición | Acción | Tipo |
|----------|--------|------|
| Title | `slot_data.page_title` | Text |
| (sin acciones extra) | - | - |

## Arquitectura

### 1. Nuevo ScreenEvent: CREATE
```kotlin
enum class ScreenEvent {
    LOAD_DATA, SEARCH, SELECT_ITEM, LOAD_MORE,
    SAVE_NEW, SAVE_EXISTING, DELETE, REFRESH,
    CREATE  // ← NUEVO
}
```

### 2. BaseCrudContract: endpointFor(CREATE)
```kotlin
ScreenEvent.CREATE -> null  // No endpoint, handled by EventOrchestrator
```

EventOrchestrator para CREATE → busca custom handler "create" o retorna NavigateTo("{resource}-form")

### 3. Componente DynamicToolbar
Archivo: `kmp-screens/.../dynamic/components/DynamicToolbar.kt`

```kotlin
@Composable
fun DynamicToolbar(
    screen: ScreenDefinition,
    isEditMode: Boolean,
    onBack: (() -> Unit)?,
    onEvent: (ScreenEvent) -> Unit,
    onCustomEvent: (String) -> Unit,
)
```

Detecta el patrón y renderiza la TopAppBar correspondiente.

### 4. Integración en PatternRouter o DynamicScreen
El `DynamicScreen` wrappea el content con un Scaffold que incluye `DynamicToolbar` como `topBar`.

### 5. Eliminar botones submit/cancel del form template
Los botones "Guardar" y "Cancelar" pasan a la toolbar. Se elimina la zone `form_actions` del template o se oculta cuando hay toolbar.

## Archivos a modificar

| Archivo | Cambio |
|---------|--------|
| `contract/ScreenEvent.kt` | Agregar `CREATE` |
| `contract/ScreenContract.kt` | `permissionFor(CREATE)` → `"$resource:create"` |
| `orchestrator/EventOrchestrator.kt` | Manejar CREATE → NavigateTo form |
| `BaseCrudContract.kt` | `endpointFor(CREATE) -> null` |
| **NUEVO** `components/DynamicToolbar.kt` | Componente toolbar |
| `DynamicScreen.kt` | Integrar Scaffold con DynamicToolbar |
| `MainScreen.kt` | Pasar `onBack` al DynamicScreen |
| Todos los renderers | Remover botones de acción que ahora van en toolbar |

## Orden de implementación

1. Agregar `ScreenEvent.CREATE` + permisos + orchestrator handling
2. Crear `DynamicToolbar` composable
3. Integrar toolbar en `DynamicScreen` (Scaffold wrapper)
4. Conectar back navigation desde toolbar
5. Conectar "Nuevo" → CREATE → NavigateTo form
6. Conectar "Guardar" desde toolbar → SAVE_NEW/SAVE_EXISTING
7. Eliminar botones duplicados del form template
8. Testing: list→form→guardar→back, crear nuevo, etc.
