# EduGo KMP — Reporte de Rendimiento y Recursos
**Fecha:** 2026-02-25
**Contexto:** Sistema SDUI con arquitectura ScreenContract/EventOrchestrator
**Scope:** Detección (sin resolución) de pérdidas de rendimiento y recursos

---

## 1. Auth — Doble SESSION_EXPIRED al iniciar

### Síntoma
```
Warn: SESSION_EXPIRED | reason=RefreshFailureReason.TokenExpired
Info: LOGIN_ATTEMPT | user=super@edugo.test
Info: LOGIN_SUCCESS | user=...
Warn: SESSION_EXPIRED | reason=RefreshFailureReason.TokenExpired  ← inesperado
```

### ¿El auto-refresh funciona?
**Sí**, en condiciones normales. `scheduleNextRefresh()` es recursivo:
- Calcula `delay = expiresAt - now - threshold(300s)` (5 min antes de expirar)
- Espera el delay → `performRefresh()` → si éxito → vuelve a `scheduleNextRefresh()` con el nuevo token

### Causa del segundo SESSION_EXPIRED — Race Condition en `restoreSession()`

**Archivo:** `AuthServiceImpl.kt:310-323`

```kotlin
} else if (token.hasRefreshToken()) {
    // forceRefresh FUERA del mutex  ← problema aquí
    val refreshResult = tokenRefreshManager.forceRefresh()
    stateMutex.withLock {                           // ← entra al mutex DESPUÉS
        if (refreshResult is Result.Success) { ... }
        else {
            clearAuthData()                         // ← borra datos del login exitoso
            _authState.value = AuthState.Unauthenticated  // ← sobrescribe Authenticated
        }
    }
}
```

**Secuencia de la race condition:**

| Tiempo | `restoreSession()` | `init {}` collector | UI |
|--------|--------------------|---------------------|-----|
| t=0 | Detecta token expirado, llama `forceRefresh()` **fuera del mutex** | — | — |
| t=1 | Esperando respuesta de red | `onRefreshFailed` → `clearSession()` → **1er SESSION_EXPIRED** | Detecta Unauthenticated |
| t=2 | Aún esperando | — | `LOGIN_ATTEMPT` → `LOGIN_SUCCESS` → `Authenticated` |
| t=3 | `forceRefresh()` retorna `Failure` | — | App funciona autenticada |
| t=4 | Adquiere mutex → `clearAuthData()` + `Unauthenticated` | `onRefreshFailed` (del forceRefresh de restoreSession) → **2do SESSION_EXPIRED** | Detecta Unauthenticated nuevamente |

El `forceRefresh()` de `restoreSession()` llama a `performRefresh()` que **siempre emite `_onRefreshFailed`** cuando falla (línea 152 de `TokenRefreshManagerImpl`). Ese emit llega al collector del `init {}` que llama a `authLogger?.logSessionExpired()` — **ese es el segundo warn**.

**Adicionalmente**, el bloque `stateMutex.withLock` del paso t=4 sobrescribe el estado `Authenticated` del login exitoso con `Unauthenticated`, causando un segundo ciclo de login innecesario.

---

## 2. SDUI — Llamadas API por navegación

### Flujo actual: 2 calls por cada pantalla

Cada vez que el usuario navega a una pantalla (incluyendo volver):

```
LaunchedEffect(screenKey, placeholders) {
    viewModel.loadScreen(screenKey, ...)  // siempre se ejecuta
}
```

| Call | Endpoint | ¿Cacheado? |
|------|----------|------------|
| #1 definición | `GET /screen-config/resolve/key/{key}` | Sí, `CachedScreenLoader` (1h, 20 entries max) |
| #2 datos | `GET /api/v1/{resource}` | **No** — siempre fresco |

**Escenario típico 5 min (estimado):**

| Acción | Calls |
|--------|-------|
| Login + menú | 1 |
| Abrir pantalla nueva | 2 |
| SELECT_ITEM → detalle | 2 |
| Volver a pantalla anterior | **2** (datos no cacheados) |
| loadMore (scroll) | 1 por página |
| Switch school context | 2 (switchContext + menú) |
| **Total sesión ~5 min** | **~11-15 calls** |

---

## 3. ViewModel instanciado N veces

**Archivo:** `MainScreen.kt:169,229`

```kotlin
// Se ejecuta en cada recomposición que cambia selectedKey
val viewModel = koinInject<DynamicScreenViewModel>()
```

- Koin con scope `factory` crea una instancia nueva en **cada llamada**
- Las instancias previas no se destruyen inmediatamente (esperan GC)
- Estado perdido en cada navegación: `_screenState`, `_dataState`, `_fieldValues`, `_fieldErrors`
- Con 10 navegaciones en una sesión → 10+ instancias en memoria simultáneamente

---

## 4. Listas que crecen sin límite (loadMore)

**Archivo:** `DynamicScreenViewModel.kt:139`

```kotlin
items = currentState.items + items  // concatenación acumulativa
```

Con scroll infinito la lista nunca se trunca:
- 1 loadMore → 40 items
- 5 loadMore → 120 items
- 10 loadMore → 220 items

Todos los items se renderizan en cada recomposición. La degradación de performance es proporcional al número de items acumulados.

---

## 5. Recomposiciones innecesarias

### 5a. Objetos `EventContext` nuevos en cada evento
**Archivo:** `DynamicScreen.kt:107-153`

```kotlin
onEvent = { event, item ->
    scope.launch {
        val context = EventContext(      // objeto nuevo en cada click
            fieldValues = viewModel.fieldValues.value,  // Map nuevo
            ...
        )
    }
}
```
Sin `remember {}` ni `@Stable`, Compose no detecta igualdad → recompone hijos.

### 5b. `FormPatternRenderer` filtra zones en cada render
**Archivo:** `FormPatternRenderer.kt:32`

```kotlin
val zones = screen.template.zones.filter { it.type != ZoneType.ACTION_GROUP }
// nueva List en cada recomposición aunque screen no cambió
```

### 5c. `ZoneRenderer` crea composables sin límite en loops
**Archivo:** `ZoneRenderer.kt:54-72`

```kotlin
data.forEachIndexed { index, item ->
    DefaultListItemRenderer(...)  // nuevo por item
    if (index < data.lastIndex) DSDivider()  // nuevo por divider
}
```
50 items = 99 composables nuevos en cada recomposición.

---

## 6. SearchBar — filtrado client-side en cada keystroke

**Archivo:** `ListPatternRenderer.kt:50-61`

```kotlin
val items by remember(rawItems, searchQuery) {
    derivedStateOf { filterItems(rawItems, searchQuery) }
}
```

- Sin debounce
- Sin server-side search
- 500 items × 10 keystrokes = 5,000 comparaciones de string en cada búsqueda

---

## 7. `MenuRepository.getMenu()` sin caché entre switches de contexto

**Archivo:** `MainScreen.kt:81,318,338`

El menú se recarga en cada `switchContext()`. Si el usuario cambia A→B→A, la API se llama 3 veces aunque el menú de A no cambió.

---

## 8. `ZoneRenderer` — recursión sin límite de profundidad

**Archivo:** `ZoneRenderer.kt:206-219`

```kotlin
zone.zones.forEach { childZone ->
    ZoneRenderer(zone = childZone, ...)  // sin límite de anidamiento
}
```

Zones con 4 niveles y 3 hijos = 81 composables anidados sin `@Stable` en parámetros → recomposiciones en cascada.

---

## 9. Coroutines sin protección contra clicks múltiples

**Archivo:** `DynamicScreen.kt:58+`

```kotlin
onEvent = { event ->
    scope.launch { ... }  // cada click lanza una coroutine
}
```

Sin debounce ni guards, clicks rápidos lanzan múltiples coroutines simultáneas en el mismo evento. Posible race condition en la actualización del ViewModel.

---

## 10. Estado de formulario se pierde al navegar

**Archivo:** `DynamicScreenViewModel.kt:237`

```kotlin
fun resetFields() {
    _fieldValues.value = emptyMap()
    _fieldErrors.value = emptyMap()
}
```

No hay persistencia de draft. Si el usuario llena un formulario, navega accidentalmente y vuelve, pierde todo lo escrito.

---

## 11. DELETE se envía como POST (bug latente)

**Archivos:** `EventOrchestrator.kt:69-74`, `RemoteDataLoader.kt`

`EventOrchestrator` resuelve el método HTTP y llama a `dataLoader.submitData(..., method = "DELETE")`, pero `RemoteDataLoader.submitData` solo distingue PUT vs "todo lo demás" (POST):

```kotlin
// RemoteDataLoader — submitData
val response = if (method == "PUT") {
    client.put(url) { ... }
} else {
    client.post(url) { ... }   // DELETE llega aquí → se envía como POST
}
```

No hay contratos que usen DELETE actualmente, pero el `EventOrchestrator` ya tiene el `when ("DELETE")` enrutado. Cuando se implemente el primer contrato con DELETE, romperá silenciosamente la API.

---

## 12. ViewModel recreado en cada recomposición

**Archivo:** `MainScreen.kt:169,229` _(confirmado por Copilot review PR #7)_

```kotlin
// Dentro de AdaptiveNavigationLayout — se ejecuta en cada recomposición
if (contentOverride != null) {
    val overrideViewModel = koinInject<DynamicScreenViewModel>()  // factory: nueva instancia
    ...
}
val viewModel = koinInject<DynamicScreenViewModel>()  // ídem rama principal
```

Koin está registrado como `factory`, por lo que cada `koinInject()` sin `remember` produce una instancia nueva. Una recomposición por cambio de tema, rotación o cualquier estado padre recrea el ViewModel, perdiendo `_screenState`, `_dataState`, `_fieldValues` y cancelando los jobs de carga en curso.

**Alcance del problema:**
- El `contentOverride` branch se activa en toda navegación a form/detail (Route.Dynamic)
- La rama principal cubre todos los screens de lista y dashboard
- Ambas ramas sin `remember` → afecta la totalidad del sistema SDUI

---

## Tabla resumen

| # | Problema | Archivo clave | Severidad | Tipo | Estado |
|---|----------|---------------|-----------|------|--------|
| 1 | Race condition SESSION_EXPIRED doble | `AuthServiceImpl.kt:313` | **CRÍTICA** | Auth / Bug | ✅ Resuelto |
| 2 | Datos de lista nunca cacheados | `DynamicScreenViewModel.kt` | **ALTA** | API calls | Pendiente |
| 3 | ViewModel instanciado N veces | `MainScreen.kt:169,229` | **ALTA** | Memory | Pendiente |
| 4 | Listas crecen sin límite (loadMore) | `DynamicScreenViewModel.kt:139` | **ALTA** | UI freeze | Pendiente |
| 5 | EventContext sin estabilidad | `DynamicScreen.kt:107` | **MEDIA** | Recomposición | Pendiente |
| 6 | SearchBar sin debounce | `ListPatternRenderer.kt:50` | **MEDIA** | CPU | Pendiente |
| 7 | Menú recargado en cada switch | `MainScreen.kt:318` | **MEDIA** | API calls | Pendiente |
| 8 | ZoneRenderer recursión sin límite | `ZoneRenderer.kt:206` | **MEDIA** | Render | Pendiente |
| 9 | Coroutines sin guards anti-doble-click | `DynamicScreen.kt:58` | **MEDIA** | Race condition | Pendiente |
| 10 | FormPatternRenderer filtra sin memo | `FormPatternRenderer.kt:32` | **BAJA** | Recomposición | Pendiente |
| 11 | Estado form perdido al navegar | `DynamicScreenViewModel.kt:237` | **BAJA** | UX | Pendiente |
| 12 | DELETE enviado como POST | `RemoteDataLoader.kt` | **MEDIA** | Bug latente | Pendiente |
| 13 | ViewModel recreado en recomposición | `MainScreen.kt:169,229` | **ALTA** | Memory / State loss | Pendiente |

> **Nota:** #3 y #13 son el mismo problema visto desde ángulos distintos (performance report vs Copilot review). Se resuelven juntos.

---

## Archivos candidatos para mejora (en orden de impacto)

1. `MainScreen.kt` — scoping del ViewModel con `remember(key)` (resuelve #3/#13)
2. `DynamicScreenViewModel.kt` — caché de datos, límite en loadMore (resuelve #2, #4)
3. `RemoteDataLoader.kt` — soporte HTTP DELETE (resuelve #12)
4. `ListPatternRenderer.kt` — LazyColumn, debounce en search (resuelve #4, #6)
5. `ZoneRenderer.kt` — estabilidad de parámetros, límite de profundidad (resuelve #8)
6. `DynamicScreen.kt` — remember en callbacks, guards anti-doble-click (resuelve #5, #9)
