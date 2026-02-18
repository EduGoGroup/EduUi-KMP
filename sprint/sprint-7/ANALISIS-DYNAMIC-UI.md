# Analisis Dynamic UI - Sprint 7

**Fecha**: 2026-02-18
**Estado**: Analisis completado
**Rama**: `feature/dynamic-ui-phase1`

---

## Resumen Ejecutivo

Se realizo un analisis completo de los 5 repositorios del ecosistema EduGo (kmp_new, api-mobile, api-admin, shared, infrastructure) para evaluar el estado actual del sistema de UI Dinamica y responder 3 preguntas fundamentales sobre la arquitectura.

---

## PUNTO 1: Es sano tener todo dinamico? O tener ambos mundos?

### Estado Actual

Actualmente **conviven ambos mundos**:

| Tipo | Ejemplo | Ubicacion |
|------|---------|-----------|
| **Estatica** | `LoginScreen.kt` (hardcoded email/password) | `screens/ui/` |
| **Dinamica** | `DynamicDashboardScreen.kt` (server-driven) | `screens/dynamic/screens/` |
| **Hibrida** | `DynamicLoginScreen.kt` (carga dinamica pero intercepta SUBMIT_FORM para usar AuthService) | `screens/dynamic/screens/` |

### Analisis

**NO es sano tener todo 100% dinamico.** Razones:

1. **Login**: Necesita integracion profunda con `AuthService`, token management, CircuitBreaker, RateLimiter. El backend no puede orquestar esto desde un JSON.

2. **Pantallas complejas**: Una pantalla que necesita orquestar 3-4 llamados API en secuencia, manejar estados intermedios, o interactuar con servicios nativos (camara, GPS, biometria) no puede ser puramente dinamica.

3. **Performance**: Las pantallas estaticas no requieren un round-trip HTTP para obtener su definicion. Para pantallas criticas (splash, login), esto es una ventaja real.

4. **Adaptaciones por plataforma**: El `platformOverrides` actual solo modifica zones a nivel superficial. Para diferencias profundas entre desktop (columnas, navigation-rail) y mobile (bottom-nav, stacked), la flexibilidad de una pantalla explicita es necesaria.

### Recomendacion: Modelo Hibrido con 3 Niveles

```
Nivel 1: FULL DINAMICO
  - Pantallas CRUD simples (listar, ver detalle)
  - Pantallas de configuracion/settings
  - Dashboards con metricas
  - El 100% de la UI viene del backend
  - Ejemplo: materials-list, material-detail, assessments-list

Nivel 2: HIBRIDO (Recomendado para mayoria)
  - La ESTRUCTURA viene del backend (template + slots)
  - Los EVENTOS COMPLEJOS se implementan en el front
  - El front intercepta acciones especificas
  - Ejemplo actual: DynamicLoginScreen intercepta SUBMIT_FORM
  - Ejemplo futuro: pantalla de Settings obtiene layout del backend
    pero el "cambiar tema" lo implementa el front

Nivel 3: FULL ESTATICO
  - Pantallas que no se benefician de ser dinamicas
  - Splash screen, onboarding nativo, pantallas offline-first
  - Pantallas con integracion profunda de hardware (camara, etc)
```

### Como Conviven

El mecanismo ya existe y funciona bien. La clave es el patron de **interception** en `DynamicScreen`:

```kotlin
// El front decide QUE interceptar
DynamicScreen(
    screenKey = "app-login",
    onAction = { action, _, scope ->
        when {
            action.type == ActionType.SUBMIT_FORM -> {
                // INTERCEPTO: uso AuthService en vez del handler generico
                authService.login(...)
            }
            else -> {
                // DELEGO: el handler generico maneja el resto
                viewModel.executeAction(action)
            }
        }
    }
)
```

**Lo que falta para que esto sea robusto:**

1. Un **registro formal** de que pantallas son nivel 1, 2 o 3
2. Un **EventBus/Handler registry** donde el front pueda registrar handlers custom por screenKey + actionId
3. Documentacion de que acciones son interceptables

---

## PUNTO 2: Eventos y Acciones de la UI

### 2A) Carga de Data (Data Endpoints)

**Estado actual:**
- `screen_instances.data_endpoint` = `/v1/materials` (un solo endpoint)
- `screen_instances.data_config` = `{method, pagination, defaultParams, fieldMapping}`
- El front llama `RemoteDataLoader.loadData(endpoint, config)`
- Solo soporta UN endpoint por pantalla, con paginacion offset

**Problema identificado:**

Para pantallas simples (lista de materiales) funciona perfecto. Pero para pantallas complejas:

| Escenario | Problema |
|-----------|----------|
| Dashboard con KPIs + activity + announcements | Necesita 3 endpoints diferentes |
| Detalle con datos + comentarios + archivos | Necesita orquestar secuencialmente |
| Formulario que depende de catalogos | Necesita cargar selects antes de mostrar el form |
| Pantalla con datos en tiempo real | No hay soporte para WebSocket/SSE |

**Solucion propuesta: Multi-DataSource**

En vez de un solo `dataEndpoint`, soportar multiples fuentes de datos nombradas:

```json
// ACTUAL (screen_instance)
{
  "dataEndpoint": "/v1/materials",
  "dataConfig": { "method": "GET", "pagination": {...} }
}

// PROPUESTO (screen_instance)
{
  "dataSources": {
    "primary": {
      "endpoint": "/v1/materials",
      "method": "GET",
      "pagination": { "type": "offset", "pageSize": 20 },
      "autoLoad": true
    },
    "stats": {
      "endpoint": "/v1/stats/materials",
      "method": "GET",
      "autoLoad": true
    },
    "categories": {
      "endpoint": "/v1/catalogs/categories",
      "method": "GET",
      "autoLoad": true,
      "cache": "session"
    }
  }
}
```

**Impacto en backend:**
- Requiere migrar `data_endpoint` + `data_config` a `data_sources` (JSONB) en `screen_instances`
- El endpoint `/v1/screens/{key}` devuelve el nuevo formato
- Retrocompatible: si solo hay `primary`, se comporta igual que ahora

**Impacto en front:**
- `DataLoader` pasa de cargar 1 fuente a N fuentes nombradas
- `DataState` se convierte en `Map<String, DataState>` (por nombre de fuente)
- Los slots bindean con `field:primary.title` o `field:stats.totalCount`

**PERO**: Para el Nivel 2 (hibrido), el front puede ignorar `dataSources` del backend y cargar datos como quiera. Esto es la flexibilidad que necesitas.

### 2B) Eventos que Desencadena la UI

**Estado actual:**

7 tipos de acciones definidos:
| ActionType | Handler | Estado |
|-----------|---------|--------|
| NAVIGATE | NavigateHandler | Funcional |
| API_CALL | ApiCallHandler | Funcional (solo GET) |
| SUBMIT_FORM | SubmitFormHandler | Funcional (POST con fieldValues) |
| REFRESH | RefreshHandler | Funcional |
| CONFIRM | ConfirmHandler | Funcional |
| LOGOUT | LogoutHandler | Funcional |
| NAVIGATE_BACK | (no implementado) | Falta handler |

**Problema identificado:**

Tienes razon en que los eventos no se pueden canalizar a una sola via. El sistema actual tiene estas limitaciones:

1. **Acciones del backend son genericas**, pero la implementacion real puede requerir logica especifica de plataforma (KMP-incompatible)
2. **No hay forma de registrar handlers custom** sin modificar el composable de la pantalla
3. **Las acciones del backend no llegan al front de forma util** - el front recibe un `ActionDefinition` con config JSON, pero no sabe que hacer con configs complejas
4. **Falta un mecanismo de "hooks"** donde el front registre listeners por evento

**Solucion propuesta: Screen Action Handlers**

```
screens/
  dynamic/
    handlers/                          <-- NUEVO: handlers por pantalla
      LoginActionHandler.kt            <-- Maneja acciones de app-login
      SettingsActionHandler.kt         <-- Maneja acciones de app-settings
      MaterialDetailActionHandler.kt   <-- Maneja acciones de material-detail

    registry/
      ScreenHandlerRegistry.kt         <-- Mapea screenKey -> handler

// Interface
interface ScreenActionHandler {
    /** Screen keys que este handler maneja */
    val screenKeys: Set<String>

    /** Retorna true si este handler maneja esta accion */
    fun canHandle(action: ActionDefinition): Boolean

    /** Ejecuta la accion con logica custom */
    suspend fun handle(
        action: ActionDefinition,
        context: ActionContext,
        itemData: JsonObject?
    ): ActionResult
}

// Ejemplo: LoginActionHandler
class LoginActionHandler(
    private val authService: AuthService
) : ScreenActionHandler {
    override val screenKeys = setOf("app-login", "app-login-es")

    override fun canHandle(action: ActionDefinition) =
        action.type == ActionType.SUBMIT_FORM

    override suspend fun handle(...): ActionResult {
        val email = context.fieldValues["email"] ?: ""
        val password = context.fieldValues["password"] ?: ""
        return when (val result = authService.login(LoginCredentials(email, password))) {
            is LoginResult.Success -> ActionResult.NavigateTo("dashboard-home")
            is LoginResult.Error -> ActionResult.Error(result.error.message)
        }
    }
}
```

**Flujo:**
```
SlotRenderer onClick
    -> DynamicScreen onAction callback
    -> ScreenHandlerRegistry.findHandler(screenKey, action)
    -> Si hay handler custom -> handler.handle(action, context)
    -> Si NO hay handler -> viewModel.executeAction(action) [handler generico]
```

**Sobre compatibilidad KMP:**
- Los handlers se implementan en `commonMain` (logica compartida)
- Si una accion requiere logica de plataforma, se usa `expect/actual`:
  ```kotlin
  // commonMain
  expect class PlatformShareHandler : ScreenActionHandler

  // androidMain
  actual class PlatformShareHandler : ScreenActionHandler {
      override suspend fun handle(...) {
          // Android share intent
      }
  }
  ```

---

## PUNTO 3: Navegacion

### Estado Actual

**Frontend:**
- `Routes.kt`: sealed class con rutas estaticas (Splash, Login, Dashboard, MaterialsList, MaterialDetail, Settings)
- `NavigationState`: backstack manual con `navigateTo()`, `back()`, `replaceAll()`
- `MainScreen.kt`: 3 tabs hardcodeados (Dashboard, Materials, Settings)
- `Route.fromScreenKey()`: mapeo manual de screenKey -> Route

**Backend API Mobile:**
- `/v1/screens/navigation`: endpoint existe pero retorna **data hardcodeada**
  ```json
  {
    "bottomNav": [
      {"key": "dashboard", "label": "Home", "icon": "home", "screenKey": "dashboard-teacher"},
      {"key": "materials", "label": "Materials", "icon": "folder", "screenKey": "materials-list"}
    ],
    "drawerItems": [],
    "version": 1
  }
  ```

**Backend API Admin:**
- `/v1/screen-config/menu`: construye menu jerarquico desde tabla `resources` + permisos del JWT
- Tabla `resources` tiene: key, displayName, icon, parentId (jerarquia), sortOrder, isMenuVisible, scope

**Problema principal:**
El menu del front esta completamente desconectado del backend. Las 3 tabs estan hardcodeadas. No hay forma de que el backend controle que items de navegacion ve cada rol.

### Arquitectura de Navegacion Propuesta

```
                    Backend (API Mobile)
                    /v1/screens/navigation
                           |
                    Retorna NavigationConfig
                    basado en permisos del JWT
                           |
            +--------------+--------------+
            |                             |
      bottomNav[]                   drawerItems[]
      (mobile: max 5)              (overflow / secondary)
            |                             |
    +-----------+                  +-----------+
    | NavItem   |                  | NavItem   |
    | key       |                  | key       |
    | label     |                  | label     |
    | icon      |                  | icon      |
    | screenKey |                  | screenKey |
    | children[]|                  | children[]|
    +-----------+                  +-----------+
            |
            v
      Frontend (KMP)
            |
    +-------+--------+
    |                |
  Mobile           Desktop/Web
  BottomNav        NavigationRail / Drawer permanente
  max 5 items      Items expandidos + subitems
```

### Flujo propuesto:

1. **Login exitoso** -> el front obtiene el token con `activeContext` (permisos)
2. **Front llama** `/v1/screens/navigation` con el token
3. **Backend construye** el menu filtrando por permisos del usuario:
   - Consulta `resources` donde `is_menu_visible = true`
   - Filtra por permisos del JWT
   - Estructura jerarquicamente (parent_id)
   - Retorna `bottomNav` (top-level visible) + `drawerItems` (secondary)
4. **Front renderiza** segun plataforma:
   - **Mobile**: `BottomNavigationBar` con hasta 5 items, drawer para el resto
   - **Tablet**: `NavigationRail` lateral + contenido expandido
   - **Desktop**: `PermanentDrawer` lateral con subitems expandibles
   - **Web**: Top navigation bar o sidebar

### Adaptaciones por plataforma:

```kotlin
// NavigationLayout.kt (nuevo)
@Composable
fun AdaptiveNavigationLayout(
    navConfig: NavigationConfig,
    currentScreenKey: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val windowSize = currentWindowSize()  // Compact, Medium, Expanded

    when (windowSize) {
        WindowSize.Compact -> {
            // Mobile: Bottom nav + scaffold
            Scaffold(
                bottomBar = { BottomNavBar(navConfig.bottomNav, currentScreenKey, onNavigate) }
            ) { content() }
        }
        WindowSize.Medium -> {
            // Tablet: Navigation rail lateral
            Row {
                NavigationRail(navConfig.bottomNav + navConfig.drawerItems, ...)
                content()
            }
        }
        WindowSize.Expanded -> {
            // Desktop: Permanent drawer con subitems
            PermanentDrawerLayout(
                drawer = { FullMenuDrawer(navConfig, ...) }
            ) { content() }
        }
    }
}
```

### Sobre parent-child screens:

Las pantallas hijas (ej: `material-detail`) no aparecen en el menu. Se navega a ellas por acciones (NAVIGATE) desde pantallas padre. El backstack ya maneja esto:

```
Dashboard -> MaterialsList -> MaterialDetail
                                    |
                              back() -> MaterialsList
```

Para desktop, las pantallas hijas podrian mostrarse en un **panel lateral** o **tab** sin reemplazar al padre:

```
+------------------+-------------------+
| MaterialsList    | MaterialDetail    |
| (padre, fijo)    | (hijo, dinamico)  |
|                  |                   |
| [Item 1]  <--   | Titulo: Item 1    |
| [Item 2]        | Contenido...      |
| [Item 3]        |                   |
+------------------+-------------------+
```

Esto requiere un `SplitPane` layout para desktop, pero es una optimizacion futura (no bloquea Phase 1).

---

## Cambios Necesarios por Repositorio

### edugo-infrastructure (postgres)

| Cambio | Prioridad | Descripcion |
|--------|-----------|-------------|
| Migrar `data_endpoint` + `data_config` a `data_sources` | Media | Nueva columna JSONB, migrar datos existentes |
| Agregar `handler_key` a `screen_instances` | Alta | Para que el front sepa que ScreenActionHandler usar |
| Agregar `children` a endpoint de navigation | Media | Soporte de subitems en menu |

### edugo-shared

| Cambio | Prioridad | Descripcion |
|--------|-----------|-------------|
| Actualizar `ScreenInstanceDTO` con `handler_key` | Alta | Nuevo campo string opcional |
| Agregar `ActionType.NAVIGATE_BACK` | Baja | Falta en el enum compartido |
| Agregar `ActionType.CUSTOM` | Alta | Para acciones que el front define |

### edugo-api-mobile

| Cambio | Prioridad | Descripcion |
|--------|-----------|-------------|
| Implementar `/v1/screens/navigation` dinamico | **Critica** | Construir menu desde resources + permisos JWT |
| Retornar `handler_key` en CombinedScreenDTO | Alta | Para que el front sepa que handler usar |
| Soporte para `dataSources` (multi-endpoint) | Media | Reemplaza dataEndpoint simple |

### edugo-api-administracion

| Cambio | Prioridad | Descripcion |
|--------|-----------|-------------|
| CRUD de `handler_key` en screen instances | Alta | Permitir configurar desde admin |
| Endpoint para gestionar navigation/menu | Media | Ya tiene `/v1/screen-config/menu` pero necesita mejoras |

### kmp_new (frontend)

| Cambio | Prioridad | Descripcion |
|--------|-----------|-------------|
| `ScreenHandlerRegistry` + handlers por pantalla | **Critica** | Sistema de interceptacion formal |
| `AdaptiveNavigationLayout` | **Critica** | Menu dinamico adaptativo por plataforma |
| Cargar navigation config del backend | **Critica** | Reemplazar tabs hardcodeados |
| Multi-DataSource en DataLoader | Media | Soporte para N fuentes de datos |
| `NavigateBackHandler` | Baja | Completar handlers |
| `Route` dinamico (no sealed class) | Alta | Permitir rutas que no estan hardcodeadas |

---

## Sobre Merge de PRs Actuales

**Recomendacion: SI, hacer merge de todo a dev y luego a main.**

Razones:
1. Todos los pipelines fluyen (excepto kmp_new que no esta en GitHub)
2. El avance es significativo y estable
3. Las nuevas features se construyen SOBRE lo que ya existe (no lo reemplazan)
4. Tener todo en main permite crear ramas limpias para el Sprint 7

**Proceso sugerido:**
1. Merge de todos los PRs abiertos a `dev`
2. Verificar pipelines en `dev`
3. Merge `dev` -> `main`
4. Crear nuevas ramas `feature/dynamic-ui-phase2-*` desde `main`
5. Subir kmp_new a GitHub, crear PR

---

## Plan de Trabajo (Sprint 7)

### Fase 1: Foundation (Prioridad Critica)

**1.1 - ScreenHandlerRegistry (Frontend)**
- Crear interface `ScreenActionHandler`
- Crear `ScreenHandlerRegistry` (mapea screenKey -> handler)
- Migrar `DynamicLoginScreen` a usar `LoginActionHandler`
- Migrar `DynamicSettingsScreen` a usar `SettingsActionHandler`
- Integrar registry en `DynamicScreen` composable
- Tests unitarios

**1.2 - Navigation Dinamica Backend (API Mobile)**
- Implementar `/v1/screens/navigation` que lea de tabla `resources`
- Filtrar por permisos del JWT
- Retornar estructura jerarquica (bottomNav + drawerItems)
- Tests

**1.3 - Navigation Dinamica Frontend (KMP)**
- Crear `NavigationLoader` (carga config del backend)
- Crear `AdaptiveNavigationLayout` (responsive por windowSize)
- Reemplazar tabs hardcodeados en `MainScreen`
- Hacer `Route` extensible (no sealed class o usar registry)
- Tests

### Fase 2: Enhancement (Prioridad Alta)

**2.1 - handler_key en Backend**
- Agregar campo `handler_key` a `screen_instances` (infraestructura)
- Actualizar DTOs en shared
- Retornar en API mobile
- CRUD en API admin

**2.2 - ActionType.CUSTOM**
- Agregar tipo al enum shared
- Frontend: cuando type == CUSTOM, busca handler por `handler_key` + `action.id`
- Permite extensibilidad total

**2.3 - Route Registry Dinamico**
- Reemplazar `Route.fromScreenKey()` hardcodeado
- Crear `RouteRegistry` que mapea screenKey -> Route (o crea ruta dinamica)
- Permitir que pantallas nuevas del backend funcionen sin release del front

### Fase 3: Data Enhancement (Prioridad Media)

**3.1 - Multi-DataSource**
- Migrar `data_endpoint` a `data_sources` en infraestructura
- Actualizar API mobile y admin
- Frontend: DataLoader soporta N fuentes
- DataState se convierte en mapa nombrado

**3.2 - Platform-Aware Layout**
- Implementar `SplitPane` para desktop (master-detail)
- Aplicar `platformOverrides` en ZoneRenderer
- Responsive breakpoints (Compact/Medium/Expanded)

### Fase 4: Polish (Prioridad Baja)

**4.1 - Offline Support**
- Cache de navigation config
- Queue de acciones offline
- Sync cuando hay conexion

**4.2 - Patterns Faltantes**
- Implementar renderers para: Search, Profile, Modal
- Agregar patterns al PatternRouter

---

## Estructura de Team Agents para Ejecucion

```
Team Lead (Coordinador)
    |
    +-- Agent 1: "backend-navigation"
    |   - Repo: edugo-api-mobile
    |   - Tarea: Implementar /v1/screens/navigation dinamico
    |   - Dependencia: ninguna
    |
    +-- Agent 2: "frontend-handlers"
    |   - Repo: kmp_new
    |   - Tarea: ScreenHandlerRegistry + handlers
    |   - Dependencia: ninguna (puede trabajar en paralelo)
    |
    +-- Agent 3: "frontend-navigation"
    |   - Repo: kmp_new
    |   - Tarea: AdaptiveNavigationLayout + NavigationLoader
    |   - Dependencia: Agent 1 (necesita saber formato del response)
    |   - Puede empezar con el formato actual hardcodeado
    |
    +-- Agent 4: "shared-models"
    |   - Repo: edugo-shared + edugo-infrastructure
    |   - Tarea: handler_key, ActionType.CUSTOM, data_sources
    |   - Dependencia: ninguna
    |
    +-- Agent 5: "integration-testing"
        - Todos los repos
        - Tarea: Tests de integracion end-to-end
        - Dependencia: Agents 1-4
```
