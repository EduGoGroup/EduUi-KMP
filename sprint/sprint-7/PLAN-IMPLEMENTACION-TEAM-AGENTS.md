# Plan de Implementacion - Dynamic UI Phase 2
# Ejecucion con Team Agents

**Fecha**: 2026-02-18
**Estado**: Listo para ejecucion
**Prerequisito**: Todos los repos en branch `dev`, sincronizados

---

## 1. CONTEXTO Y ESTRATEGIA

### 1.1 Repositorios Involucrados

| Repo | Ruta Absoluta | Branch Actual | Branch de Trabajo |
|------|---------------|---------------|-------------------|
| kmp_new (frontend) | `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new` | `dev` | `feature/dynamic-ui-phase2` |
| edugo-api-mobile | `/Users/jhoanmedina/source/EduGo/repos-separados/edugo-api-mobile` | `dev` | `feature/dynamic-navigation` |
| edugo-api-administracion | `/Users/jhoanmedina/source/EduGo/repos-separados/edugo-api-administracion` | `dev` | `feature/handler-key-crud` |
| edugo-shared | `/Users/jhoanmedina/source/EduGo/repos-separados/edugo-shared` | `dev` | `feature/dynamic-ui-types` |
| edugo-infrastructure | `/Users/jhoanmedina/source/EduGo/repos-separados/edugo-infrastructure` | `dev` | `feature/screen-handler-key` |
| edugo-dev-environment | `/Users/jhoanmedina/source/EduGo/repos-separados/edugo-dev-environment` | `dev` | Sin cambios |
| edugo-worker | `/Users/jhoanmedina/source/EduGo/repos-separados/edugo-worker` | `dev` | Sin cambios |

### 1.2 Reglas de Trabajo

1. **Todo es LOCAL** - No se hace commit, no se hace push. Solo cambios en archivos.
2. **Ramas desde dev** - Para cada proyecto Go que se modifique, crear rama desde dev:
   ```bash
   cd <repo>
   git checkout dev
   git checkout -b feature/<nombre>
   ```
3. **go.work** - Ya existe en `/Users/jhoanmedina/source/EduGo/repos-separados/go.work` con los replace directives. Esto permite que los cambios locales en shared/infrastructure se vean automaticamente en los APIs sin necesidad de `go get`.
4. **kmp_new** - Tambien esta en `dev`. Crear rama `feature/dynamic-ui-phase2` desde `dev`:
   ```bash
   cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new
   git checkout dev
   git checkout -b feature/dynamic-ui-phase2
   ```
5. **Convencion de codigo** - Respetar arquitectura limpia existente:
   - Go: `domain/repository/` (interfaces) → `application/service/` (logica) → `infrastructure/http/handler/` (HTTP) + `infrastructure/persistence/postgres/repository/` (DB)
   - KMP: `modules/<name>/src/commonMain/` (logica) → `kmp-screens/src/commonMain/` (UI)

### 1.3 go.work Actual

```
go 1.25.3

use (
    ./edugo-api-administracion
    ./edugo-api-mobile
    ./edugo-dev-environment/migrator
    ./edugo-infrastructure/postgres
    ./edugo-shared/auth
    ./edugo-shared/common
    ./edugo-shared/middleware/gin
    ./edugo-shared/screenconfig
)
```

**IMPORTANTE**: El `go.work` NO necesita modificacion. Los `use` directives ya cubren todos los modulos. Cuando un agente modifique `edugo-shared/screenconfig`, el cambio sera visible automaticamente para `edugo-api-administracion` (que es el unico que lo importa).

**NOTA CLAVE**: `edugo-api-mobile` NO importa `edugo-shared/screenconfig`. Tiene sus propios DTOs locales en `internal/application/dto/screen_dto.go`. Cualquier cambio al DTO mobile se hace localmente en ese archivo.

---

## 2. ESTRUCTURA DE EQUIPO

```
Team Lead (orquestador)
    |
    +-- Agent 1: "shared-infra"
    |   Repos: edugo-shared, edugo-infrastructure
    |   Tipo: general-purpose
    |   Prioridad: PRIMERA (otros dependen de esto)
    |
    +-- Agent 2: "backend-mobile"
    |   Repos: edugo-api-mobile
    |   Tipo: general-purpose
    |   Dependencia: Agent 1 (para compilacion con go.work)
    |   NOTA: Puede empezar en paralelo con la parte que NO depende de shared
    |
    +-- Agent 3: "backend-admin"
    |   Repos: edugo-api-administracion
    |   Tipo: general-purpose
    |   Dependencia: Agent 1 (usa screenconfig de shared)
    |
    +-- Agent 4: "frontend-handlers"
    |   Repos: kmp_new
    |   Tipo: general-purpose
    |   Dependencia: NINGUNA (trabajo independiente)
    |
    +-- Agent 5: "frontend-navigation"
    |   Repos: kmp_new
    |   Tipo: general-purpose
    |   Dependencia: Agent 2 (necesita saber formato del response)
    |   NOTA: Puede empezar con el formato actual, adaptar despues
    |
    +-- Agent 6: "validator"
        Repos: todos
        Tipo: general-purpose
        Dependencia: Agents 1-5 (validacion final)
```

### Orden de Ejecucion

```
Fase A (Paralelo):
  - Agent 1: shared-infra      ← INICIA PRIMERO
  - Agent 4: frontend-handlers  ← INICIA EN PARALELO (independiente)

Fase B (Despues de Agent 1):
  - Agent 2: backend-mobile     ← Cuando Agent 1 termine
  - Agent 3: backend-admin      ← Cuando Agent 1 termine
  - Agent 5: frontend-navigation ← Puede iniciar con Agent 4

Fase C (Validacion):
  - Agent 6: validator          ← Cuando todos terminen
```

---

## 3. TAREA: AGENT 1 - "shared-infra"

### 3.1 Objetivo
Agregar soporte para `handler_key` en screen_instances y `ActionType.CUSTOM` en tipos compartidos.

### 3.2 Crear Ramas
```bash
cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-shared
git checkout dev && git checkout -b feature/dynamic-ui-types

cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-infrastructure
git checkout dev && git checkout -b feature/screen-handler-key
```

### 3.3 Cambios en edugo-infrastructure

#### 3.3.1 Migracion SQL: Agregar `handler_key` a `screen_instances`

**Crear archivo**: `postgres/migrations/structure/020_alter_screen_instances_handler_key.sql`

```sql
-- ====================================================================
-- ALTER screen_instances: Add handler_key for frontend action routing
-- VERSION: postgres/v0.19.0
-- ====================================================================
ALTER TABLE ui_config.screen_instances
ADD COLUMN IF NOT EXISTS handler_key VARCHAR(100) DEFAULT NULL;

COMMENT ON COLUMN ui_config.screen_instances.handler_key IS
'Optional key that the frontend uses to route to a specific ScreenActionHandler implementation. NULL means use generic handlers.';

CREATE INDEX IF NOT EXISTS idx_screen_instances_handler_key
ON ui_config.screen_instances(handler_key)
WHERE handler_key IS NOT NULL;
```

#### 3.3.2 Actualizar seed de screen_instances con handler_key

**Modificar archivo**: `postgres/migrations/seeds/007_seed_screen_instances.sql`

Agregar `handler_key` a los INSERTs existentes:
- `app-login` → `handler_key = 'login'`
- `dashboard-teacher` → `handler_key = NULL` (usa handlers genericos)
- `dashboard-student` → `handler_key = NULL`
- `materials-list` → `handler_key = NULL`
- `material-detail` → `handler_key = 'material-detail'`
- `app-settings` → `handler_key = 'settings'`

#### 3.3.3 Actualizar entity ScreenInstance

**Modificar archivo**: `postgres/entities/screen_instance.go`

Agregar campo:
```go
HandlerKey *string `json:"handler_key,omitempty" gorm:"column:handler_key;type:varchar(100)"`
```

### 3.4 Cambios en edugo-shared

#### 3.4.1 Agregar ActionType CUSTOM

**Modificar archivo**: `screenconfig/types.go`

Agregar constante:
```go
ActionCustom ActionType = "CUSTOM"
```

Agregar a la lista de validacion `validActionTypes`:
```go
ActionCustom: true,
```

#### 3.4.2 Actualizar DTOs con handler_key

**Modificar archivo**: `screenconfig/dto.go`

En `ScreenInstanceDTO`, agregar:
```go
HandlerKey *string `json:"handler_key,omitempty"`
```

En `CombinedScreenDTO`, agregar:
```go
HandlerKey *string `json:"handler_key,omitempty"`
```

#### 3.4.3 Agregar NavigationItemDTO

**Modificar archivo**: `screenconfig/dto.go`

Agregar al final:
```go
// NavigationItemDTO represents a menu/navigation item
type NavigationItemDTO struct {
    Key       string              `json:"key"`
    Label     string              `json:"label"`
    Icon      string              `json:"icon,omitempty"`
    ScreenKey string              `json:"screenKey,omitempty"`
    SortOrder int                 `json:"sortOrder"`
    Children  []NavigationItemDTO `json:"children,omitempty"`
}

// NavigationConfigDTO represents the complete navigation configuration
type NavigationConfigDTO struct {
    BottomNav   []NavigationItemDTO `json:"bottomNav"`
    DrawerItems []NavigationItemDTO `json:"drawerItems"`
    Version     int                 `json:"version"`
}
```

### 3.5 Validacion Agent 1
```bash
# Desde el directorio raiz del workspace
cd /Users/jhoanmedina/source/EduGo/repos-separados

# Compilar shared/screenconfig
cd edugo-shared/screenconfig && go build ./... && go test ./...

# Compilar infrastructure/postgres
cd ../../edugo-infrastructure/postgres && go build ./...

# Verificar que api-admin compila (usa screenconfig)
cd ../../edugo-api-administracion && go build ./...
```

---

## 4. TAREA: AGENT 2 - "backend-mobile"

### 4.1 Objetivo
Implementar navegacion dinamica basada en RBAC y agregar handler_key al response de screens.

### 4.2 Crear Rama
```bash
cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-api-mobile
git checkout dev && git checkout -b feature/dynamic-navigation
```

### 4.3 Nuevo: ResourceReader Repository

#### 4.3.1 Interface en dominio

**Crear archivo**: `internal/domain/repository/resource_repository.go`

```go
package repository

import (
    "context"

    pgentities "github.com/EduGoGroup/edugo-infrastructure/postgres/entities"
)

// ResourceReader reads resources for navigation building
type ResourceReader interface {
    // GetMenuResources returns active, menu-visible resources ordered by sort_order
    GetMenuResources(ctx context.Context) ([]*pgentities.Resource, error)

    // GetResourceScreenMappings returns screen mappings for given resource keys
    GetResourceScreenMappings(ctx context.Context, resourceKeys []string) ([]*ResourceScreenMapping, error)
}

// ResourceScreenMapping maps a resource to its default screen
type ResourceScreenMapping struct {
    ResourceKey string
    ScreenKey   string
    ScreenType  string
    IsDefault   bool
    SortOrder   int
}
```

#### 4.3.2 Implementacion PostgreSQL

**Crear archivo**: `internal/infrastructure/persistence/postgres/repository/resource_repository.go`

```go
package repository

import (
    "context"

    pgentities "github.com/EduGoGroup/edugo-infrastructure/postgres/entities"
    "github.com/EduGoGroup/edugo-api-mobile/internal/domain/repository"
    "gorm.io/gorm"
)

type postgresResourceRepository struct {
    db *gorm.DB
}

func NewPostgresResourceRepository(db *gorm.DB) repository.ResourceReader {
    return &postgresResourceRepository{db: db}
}

func (r *postgresResourceRepository) GetMenuResources(ctx context.Context) ([]*pgentities.Resource, error) {
    var resources []*pgentities.Resource
    err := r.db.WithContext(ctx).
        Where("is_active = ? AND is_menu_visible = ?", true, true).
        Order("sort_order ASC").
        Find(&resources).Error
    if err != nil {
        return nil, err
    }
    return resources, nil
}

func (r *postgresResourceRepository) GetResourceScreenMappings(ctx context.Context, resourceKeys []string) ([]*repository.ResourceScreenMapping, error) {
    var mappings []*repository.ResourceScreenMapping
    err := r.db.WithContext(ctx).
        Table("ui_config.resource_screens").
        Select("resource_key, screen_key, screen_type, is_default, sort_order").
        Where("resource_key IN ? AND is_active = ?", resourceKeys, true).
        Where("is_default = ?", true).
        Order("sort_order ASC").
        Scan(&mappings).Error
    if err != nil {
        return nil, err
    }
    return mappings, nil
}
```

### 4.4 Actualizar ScreenService para Navegacion Dinamica

**Modificar archivo**: `internal/application/service/screen_service.go`

#### 4.4.1 Agregar ResourceReader al constructor

Agregar `resourceReader repository.ResourceReader` como parametro del constructor `NewScreenService`.

Actualizar la struct `screenService`:
```go
type screenService struct {
    repo           repository.ScreenRepository
    resourceReader repository.ResourceReader
    logger         logger.Logger
    mu             sync.RWMutex
    cache          map[string]*screenCache
    ttl            time.Duration
}
```

#### 4.4.2 Reemplazar GetNavigationConfig hardcodeado

La implementacion actual retorna datos hardcodeados. Reemplazar con:

```go
func (s *screenService) GetNavigationConfig(ctx context.Context, userID uuid.UUID, platform string) (*NavigationConfigDTO, error) {
    // 1. Get all menu-visible resources
    resources, err := s.resourceReader.GetMenuResources(ctx)
    if err != nil {
        s.logger.Error("Failed to get menu resources", "error", err)
        return nil, fmt.Errorf("failed to get navigation config: %w", err)
    }

    if len(resources) == 0 {
        return &NavigationConfigDTO{
            BottomNav:   []NavItemDTO{},
            DrawerItems: []NavItemDTO{},
            Version:     1,
        }, nil
    }

    // 2. Get user permissions from context
    userPerms := middleware.GetPermissionsFromContext(ctx)

    // 3. Filter resources by user permissions
    var allowedResources []*pgentities.Resource
    var resourceKeys []string
    for _, res := range resources {
        permKey := res.Key + ":read"
        if hasPermission(userPerms, permKey) || res.Scope == "system" {
            allowedResources = append(allowedResources, res)
            resourceKeys = append(resourceKeys, res.Key)
        }
    }

    // 4. Get screen mappings for allowed resources
    mappings, err := s.resourceReader.GetResourceScreenMappings(ctx, resourceKeys)
    if err != nil {
        s.logger.Error("Failed to get resource screen mappings", "error", err)
        return nil, fmt.Errorf("failed to get screen mappings: %w", err)
    }

    screenMap := make(map[string]string) // resourceKey -> screenKey
    for _, m := range mappings {
        if m.IsDefault {
            screenMap[m.ResourceKey] = m.ScreenKey
        }
    }

    // 5. Build navigation tree
    bottomNav, drawerItems := s.buildNavigationTree(allowedResources, screenMap, platform)

    return &NavigationConfigDTO{
        BottomNav:   bottomNav,
        DrawerItems: drawerItems,
        Version:     1,
    }, nil
}
```

**Agregar helpers**:

```go
func hasPermission(perms []string, required string) bool {
    for _, p := range perms {
        if p == required {
            return true
        }
    }
    return false
}

func (s *screenService) buildNavigationTree(resources []*pgentities.Resource, screenMap map[string]string, platform string) ([]NavItemDTO, []NavItemDTO) {
    // Separate top-level (no parent) from children
    topLevel := make([]*pgentities.Resource, 0)
    children := make(map[string][]*pgentities.Resource) // parentID -> children

    for _, res := range resources {
        if res.ParentID == nil {
            topLevel = append(topLevel, res)
        } else {
            pid := res.ParentID.String()
            children[pid] = append(children[pid], res)
        }
    }

    // Convert to NavItemDTO
    var allItems []NavItemDTO
    for _, res := range topLevel {
        item := NavItemDTO{
            Key:       res.Key,
            Label:     res.DisplayName,
            SortOrder: res.SortOrder,
        }
        if res.Icon != nil {
            item.Icon = *res.Icon
        }
        if sk, ok := screenMap[res.Key]; ok {
            item.ScreenKey = sk
        }
        // Add children
        if kids, ok := children[res.ID.String()]; ok {
            for _, kid := range kids {
                childItem := NavItemDTO{
                    Key:       kid.Key,
                    Label:     kid.DisplayName,
                    SortOrder: kid.SortOrder,
                }
                if kid.Icon != nil {
                    childItem.Icon = *kid.Icon
                }
                if sk, ok := screenMap[kid.Key]; ok {
                    childItem.ScreenKey = sk
                }
                item.Children = append(item.Children, childItem)
            }
        }
        allItems = append(allItems, item)
    }

    // Split: mobile gets max 5 in bottomNav, rest in drawer
    // desktop/web gets all in drawer (no bottom nav)
    maxBottomNav := 5
    if platform == "desktop" || platform == "web" {
        maxBottomNav = 0
    }

    var bottomNav, drawerItems []NavItemDTO
    for i, item := range allItems {
        if i < maxBottomNav {
            bottomNav = append(bottomNav, item)
        } else {
            drawerItems = append(drawerItems, item)
        }
    }

    return bottomNav, drawerItems
}
```

#### 4.4.3 Agregar NavItemDTO con Children

**Modificar archivo**: `internal/application/service/screen_service.go`

Actualizar la struct `NavItemDTO`:
```go
type NavItemDTO struct {
    Key       string       `json:"key"`
    Label     string       `json:"label"`
    Icon      string       `json:"icon,omitempty"`
    ScreenKey string       `json:"screenKey,omitempty"`
    SortOrder int          `json:"sortOrder"`
    Children  []NavItemDTO `json:"children,omitempty"`
}
```

### 4.5 Agregar handler_key al Response de Screens

#### 4.5.1 Actualizar CombinedScreen en domain

**Modificar archivo**: `internal/domain/repository/screen_repository.go`

Agregar a `CombinedScreen`:
```go
HandlerKey *string
```

#### 4.5.2 Actualizar query SQL

**Modificar archivo**: `internal/infrastructure/persistence/postgres/repository/screen_repository.go`

En el SELECT de `GetCombinedScreen`, agregar `si.handler_key`:
```sql
SELECT si.id, si.screen_key, si.name, st.pattern, st.version, st.definition,
       si.slot_data, si.actions, si.data_endpoint, si.data_config,
       si.handler_key,
       COALESCE(sup.preferences, '{}'::jsonb) as user_preferences,
       GREATEST(si.updated_at, st.updated_at) as last_updated
FROM ...
```

#### 4.5.3 Actualizar DTO local

**Modificar archivo**: `internal/application/dto/screen_dto.go`

Agregar a `CombinedScreenDTO`:
```go
HandlerKey *string `json:"handlerKey,omitempty"`
```

#### 4.5.4 Actualizar mapeo en service

En `screen_service.go`, donde se construye el `CombinedScreenDTO` desde `CombinedScreen`, agregar:
```go
HandlerKey: combined.HandlerKey,
```

### 4.6 Actualizar DI Container

**Modificar archivo**: `internal/container/repositories.go`

Agregar:
```go
ResourceReader: pgrepository.NewPostgresResourceRepository(c.Infrastructure.PostgresDB),
```

**Modificar archivo**: `internal/container/services.go`

Actualizar la construccion de `ScreenService`:
```go
ScreenService: service.NewScreenService(
    c.Repositories.ScreenRepository,
    c.Repositories.ResourceReader,
    c.Infrastructure.Logger,
),
```

### 4.7 Pasar Permisos al Contexto del Servicio

El servicio necesita acceder a los permisos del usuario. Hay dos opciones:
- **Opcion A**: Pasar permisos como parametro a `GetNavigationConfig`
- **Opcion B**: Extraerlos del `gin.Context` en el handler y pasarlos

**Recomendacion**: Opcion A - agregar parametro `permissions []string`:

**Modificar interface ScreenService**:
```go
GetNavigationConfig(ctx context.Context, userID uuid.UUID, platform string, permissions []string) (*NavigationConfigDTO, error)
```

**Modificar handler** `GetNavigation`:
```go
func (h *ScreenHandler) GetNavigation(c *gin.Context) {
    // ... existing userID extraction ...
    platform := c.DefaultQuery("platform", "mobile")

    // Get permissions from JWT context
    activeCtx := middleware.GetActiveContext(c)
    var permissions []string
    if activeCtx != nil {
        permissions = activeCtx.Permissions
    }

    config, err := h.screenService.GetNavigationConfig(c.Request.Context(), userID, platform, permissions)
    // ... rest unchanged ...
}
```

### 4.8 Tests para Agent 2

**Crear archivo**: `internal/infrastructure/persistence/postgres/repository/resource_repository_test.go`
- Test GetMenuResources (retorna solo activos y visibles)
- Test GetResourceScreenMappings (retorna solo defaults activos)

**Crear archivo**: `internal/application/service/screen_service_navigation_test.go`
- Test GetNavigationConfig con permisos completos
- Test GetNavigationConfig con permisos parciales (filtra items)
- Test GetNavigationConfig sin permisos (retorna vacio)
- Test GetNavigationConfig para platform desktop (todo en drawer)
- Test buildNavigationTree con jerarquia padre-hijo

**Seguir patron existente**: Usar mocks como en `screen_service_test.go` y `screen_handler_test.go`

### 4.9 Validacion Agent 2
```bash
cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-api-mobile
go build ./...
go test ./internal/application/service/...
go test ./internal/infrastructure/persistence/postgres/repository/...
```

---

## 5. TAREA: AGENT 3 - "backend-admin"

### 5.1 Objetivo
Agregar CRUD de `handler_key` en screen_instances y mejorar endpoint de menu.

### 5.2 Crear Rama
```bash
cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-api-administracion
git checkout dev && git checkout -b feature/handler-key-crud
```

### 5.3 Cambios

#### 5.3.1 Actualizar ScreenConfigService

**Modificar archivos en**: `internal/application/service/`

En `CreateInstanceRequest` y `UpdateInstanceRequest`, agregar:
```go
HandlerKey *string `json:"handler_key,omitempty"`
```

En la logica de `CreateInstance` y `UpdateInstance`, mapear `HandlerKey` al entity.

#### 5.3.2 Actualizar Handler

**Modificar archivo**: `internal/infrastructure/http/handler/screen_config_handler.go`

Asegurar que el request body acepta `handler_key` y lo pasa al service.

#### 5.3.3 Actualizar el endpoint de resolve

El endpoint `/v1/screen-config/resolve/key/{screenKey}` debe retornar `handler_key` en el response.

Verificar que el mapeo de entity a `CombinedScreenDTO` (de screenconfig shared) incluye `HandlerKey`.

### 5.4 Tests
Actualizar tests existentes para incluir `handler_key` en los requests de creacion/actualizacion.

### 5.5 Validacion Agent 3
```bash
cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-api-administracion
go build ./...
go test ./internal/application/service/...
```

---

## 6. TAREA: AGENT 4 - "frontend-handlers"

### 6.1 Objetivo
Crear el sistema formal de ScreenActionHandler que formaliza el patron de interception existente en DynamicLoginScreen, DynamicSettingsScreen, etc.

### 6.2 Crear Rama
```bash
cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new
git checkout dev && git checkout -b feature/dynamic-ui-phase2
```

**NOTA**: Si Agent 5 (frontend-navigation) trabaja en el mismo repo, ambos agentes comparten la misma rama `feature/dynamic-ui-phase2`. Coordinar para no editar el mismo archivo simultaneamente.

### 6.3 Nuevos Archivos en modules/dynamic-ui

#### 6.2.1 ScreenActionHandler Interface

**Crear archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/ScreenActionHandler.kt`

```kotlin
package com.edugo.kmp.dynamicui.handler

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionDefinition

/**
 * Interface for screen-specific action handlers.
 * Allows screens to override default action behavior with custom logic.
 */
interface ScreenActionHandler {
    /** Screen keys this handler is responsible for */
    val screenKeys: Set<String>

    /** Returns true if this handler can process the given action */
    fun canHandle(action: ActionDefinition): Boolean

    /** Execute the action with custom logic */
    suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult
}
```

#### 6.2.2 ScreenHandlerRegistry

**Crear archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/ScreenHandlerRegistry.kt`

```kotlin
package com.edugo.kmp.dynamicui.handler

import com.edugo.kmp.dynamicui.model.ActionDefinition

/**
 * Registry that maps screen keys to their custom action handlers.
 * When a screen has a registered handler that can process an action,
 * it takes priority over the generic ActionRegistry handlers.
 */
class ScreenHandlerRegistry(
    handlers: List<ScreenActionHandler> = emptyList()
) {
    private val handlerMap: Map<String, List<ScreenActionHandler>>

    init {
        val map = mutableMapOf<String, MutableList<ScreenActionHandler>>()
        handlers.forEach { handler ->
            handler.screenKeys.forEach { key ->
                map.getOrPut(key) { mutableListOf() }.add(handler)
            }
        }
        handlerMap = map
    }

    /**
     * Find a handler for the given screen key and action.
     * Returns null if no custom handler is registered.
     */
    fun findHandler(screenKey: String, action: ActionDefinition): ScreenActionHandler? {
        return handlerMap[screenKey]?.firstOrNull { it.canHandle(action) }
    }

    /**
     * Check if a custom handler exists for this screen + action combination.
     */
    fun hasHandler(screenKey: String, action: ActionDefinition): Boolean {
        return findHandler(screenKey, action) != null
    }
}
```

#### 6.2.3 LoginActionHandler

**Crear archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/handlers/LoginActionHandler.kt`

```kotlin
package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType

class LoginActionHandler(
    private val authService: AuthService
) : ScreenActionHandler {

    override val screenKeys = setOf("app-login", "app-login-es")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val email = context.fieldValues["email"] ?: ""
        val password = context.fieldValues["password"] ?: ""

        if (email.isBlank() || password.isBlank()) {
            return ActionResult.Error("Email and password are required")
        }

        return when (val result = authService.login(LoginCredentials(email, password))) {
            is LoginResult.Success -> {
                val targetScreen = action.config["onSuccess"]
                    ?.let { it as? kotlinx.serialization.json.JsonObject }
                    ?.get("config")
                    ?.let { it as? kotlinx.serialization.json.JsonObject }
                    ?.get("target")
                    ?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
                    ?: "dashboard-home"
                ActionResult.NavigateTo(targetScreen)
            }
            is LoginResult.Error -> ActionResult.Error(result.error.message)
            is LoginResult.NetworkError -> ActionResult.Error(result.message, retry = true)
        }
    }
}
```

#### 6.2.4 SettingsActionHandler

**Crear archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/handlers/SettingsActionHandler.kt`

```kotlin
package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType
// Import ThemeService interface from settings module

class SettingsActionHandler(
    private val authService: com.edugo.kmp.auth.service.AuthService
    // private val themeService: ThemeService  // if available
) : ScreenActionHandler {

    override val screenKeys = setOf("app-settings")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.LOGOUT ||
               action.type == ActionType.NAVIGATE_BACK ||
               (action.type == ActionType.CONFIRM && action.id == "theme_toggle")
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        return when (action.type) {
            ActionType.LOGOUT -> {
                authService.logout()
                ActionResult.Logout
            }
            ActionType.NAVIGATE_BACK -> ActionResult.NavigateTo("back")
            ActionType.CONFIRM -> {
                // Handle theme toggle or other settings-specific confirms
                ActionResult.Success(message = action.id)
            }
            else -> ActionResult.Error("Unhandled action: ${action.type}")
        }
    }
}
```

### 6.4 Integrar en DynamicScreenViewModel

**Modificar archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/viewmodel/DynamicScreenViewModel.kt`

Agregar `ScreenHandlerRegistry` como parametro del constructor:

```kotlin
class DynamicScreenViewModel(
    private val screenLoader: ScreenLoader,
    private val dataLoader: DataLoader,
    private val actionRegistry: ActionRegistry,
    private val screenHandlerRegistry: ScreenHandlerRegistry = ScreenHandlerRegistry()
)
```

Modificar `executeAction` para consultar el registry primero:

```kotlin
suspend fun executeAction(actionDef: ActionDefinition, itemData: JsonObject? = null): ActionResult {
    val currentScreen = (screenState.value as? ScreenState.Ready)?.screen
    val screenKey = currentScreen?.screenKey ?: ""

    // 1. Check for screen-specific custom handler
    val customHandler = screenHandlerRegistry.findHandler(screenKey, actionDef)
    if (customHandler != null) {
        val context = ActionContext(
            screenKey = screenKey,
            actionId = actionDef.id,
            config = actionDef.config,
            fieldValues = fieldValues.value,
            selectedItem = itemData
        )
        return customHandler.handle(actionDef, context)
    }

    // 2. Fall back to generic action registry
    val context = ActionContext(
        screenKey = screenKey,
        actionId = actionDef.id,
        config = actionDef.config,
        fieldValues = fieldValues.value,
        selectedItem = itemData
    )
    return actionRegistry.execute(actionDef.type, context)
}
```

### 6.5 Actualizar DI Module

**Modificar archivo**: `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/DynamicUiModule.kt`

Agregar los handlers y el registry:

```kotlin
// Screen-specific action handlers
single { LoginActionHandler(get()) }
single { SettingsActionHandler(get()) }

// Screen handler registry
single {
    ScreenHandlerRegistry(
        handlers = listOf(
            get<LoginActionHandler>(),
            get<SettingsActionHandler>()
        )
    )
}

// Update DynamicScreenViewModel factory
factory {
    DynamicScreenViewModel(
        screenLoader = get(),
        dataLoader = get(),
        actionRegistry = get(),
        screenHandlerRegistry = get()
    )
}
```

### 6.6 Simplificar Screen Wrappers

Ahora que los handlers estan en el registry, los screen wrappers se simplifican significativamente.

#### 6.5.1 Simplificar DynamicLoginScreen.kt

**Modificar archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/screens/DynamicLoginScreen.kt`

Eliminar la logica de interception manual. El `LoginActionHandler` registrado en el registry maneja `SUBMIT_FORM` automaticamente.

```kotlin
@Composable
fun DynamicLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit
) {
    val viewModel = koinInject<DynamicScreenViewModel>()

    DynamicScreen(
        screenKey = "app-login",
        viewModel = viewModel,
        onNavigate = { screenKey, params ->
            if (screenKey == "dashboard-home" || screenKey.startsWith("dashboard")) {
                onLoginSuccess()
            } else {
                onNavigate(screenKey, params)
            }
        }
        // NO onAction override needed - LoginActionHandler handles SUBMIT_FORM
    )
}
```

#### 6.5.2 Simplificar DynamicSettingsScreen.kt

Similar: eliminar interception manual, dejar que `SettingsActionHandler` maneje.

### 6.7 Tests

**Crear archivo**: `modules/dynamic-ui/src/commonTest/kotlin/com/edugo/kmp/dynamicui/handler/ScreenHandlerRegistryTest.kt`

Tests:
- `findHandler returns correct handler for registered screenKey and action`
- `findHandler returns null for unregistered screenKey`
- `findHandler returns null when handler cannot handle action`
- `hasHandler returns correct boolean`
- `multiple handlers for same screenKey - first match wins`

**Crear archivo**: `modules/dynamic-ui/src/commonTest/kotlin/com/edugo/kmp/dynamicui/handler/handlers/LoginActionHandlerTest.kt`

Tests:
- `handle SUBMIT_FORM with valid credentials returns NavigateTo`
- `handle SUBMIT_FORM with empty email returns Error`
- `canHandle returns true for SUBMIT_FORM`
- `canHandle returns false for NAVIGATE`
- `screenKeys contains app-login`

### 6.8 Validacion Agent 4
```bash
cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new
./gradlew :modules:dynamic-ui:compileKotlinDesktop
./gradlew :modules:dynamic-ui:desktopTest
./gradlew :kmp-screens:compileKotlinDesktop
```

---

## 7. TAREA: AGENT 5 - "frontend-navigation"

### 7.1 Objetivo
Implementar navegacion dinamica en el frontend KMP: cargar menu del backend, renderizar adaptivamente segun plataforma, hacer el sistema de rutas extensible.

### 7.2 Rama de Trabajo
Usa la misma rama que Agent 4: `feature/dynamic-ui-phase2` en kmp_new.

```bash
cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new
# La rama ya fue creada por Agent 4, solo hacer checkout si no esta ya ahi
git checkout feature/dynamic-ui-phase2
```

**NOTA**: Agent 4 y Agent 5 comparten rama en el mismo repo. Los archivos que modifican NO se solapan (Agent 4 trabaja en `handler/` y Agent 5 en `navigation/` y `loader/`). El unico archivo compartido es `DynamicUiModule.kt` en DI - si Agent 4 ya lo modifico, Agent 5 debe leerlo antes de editar.

### 7.3 Modelo de Navegacion

#### 7.3.1 Actualizar NavigationDefinition

**Modificar archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/model/NavigationConfig.kt`

Agregar (o actualizar si existe parcialmente):

```kotlin
@Serializable
data class NavigationDefinition(
    val bottomNav: List<NavigationItem> = emptyList(),
    val drawerItems: List<NavigationItem> = emptyList(),
    val version: Int = 1
)

@Serializable
data class NavigationItem(
    val key: String,
    val label: String,
    val icon: String? = null,
    val screenKey: String? = null,
    val sortOrder: Int = 0,
    val children: List<NavigationItem> = emptyList()
)
```

### 7.4 NavigationLoader

#### 7.4.1 Actualizar RemoteScreenLoader

**Modificar archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/loader/RemoteScreenLoader.kt`

Implementar `loadNavigation()` (actualmente es TODO):

```kotlin
override suspend fun loadNavigation(): Result<NavigationDefinition> {
    return try {
        val platform = getPlatformName() // "mobile", "desktop", "web"
        val response = httpClient.get("$baseUrl/v1/screens/navigation?platform=$platform")

        if (response.status.value == 200) {
            val json = response.bodyAsText()
            val definition = Json.decodeFromString<NavigationDefinition>(json)
            Result.Success(definition)
        } else {
            Result.Failure("Failed to load navigation: HTTP ${response.status.value}")
        }
    } catch (e: Exception) {
        Result.Failure("Failed to load navigation: ${e.message}")
    }
}
```

**Nota**: `getPlatformName()` debe ser un `expect/actual` que retorna la plataforma actual.

#### 7.4.2 Agregar cache de navegacion en CachedScreenLoader

**Modificar archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/loader/CachedScreenLoader.kt`

Agregar cache para navegacion (similar al cache de screens pero con key fija):
```kotlin
private var navCache: Pair<NavigationDefinition, Long>? = null // definition + timestamp

override suspend fun loadNavigation(): Result<NavigationDefinition> {
    // Check memory cache
    navCache?.let { (def, ts) ->
        if (System.currentTimeMillis() - ts < ttlMillis) {
            return Result.Success(def)
        }
    }

    // Load from remote
    val result = remoteLoader.loadNavigation()
    if (result is Result.Success) {
        navCache = result.data to System.currentTimeMillis()
    }
    return result
}
```

### 7.5 Route Registry Dinamico

#### 7.5.1 Crear RouteRegistry

**Crear archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/navigation/RouteRegistry.kt`

```kotlin
package com.edugo.kmp.screens.navigation

/**
 * Registry that maps screen keys to Routes.
 * Extends the static Route.fromScreenKey() with dynamic registrations.
 */
class RouteRegistry {
    private val registrations = mutableMapOf<String, (Map<String, String>) -> Route>()

    init {
        // Register all known static routes
        register("splash") { Route.Splash }
        register("app-login") { Route.Login }
        register("dashboard-home") { Route.Dashboard }
        register("dashboard-teacher") { Route.Dashboard }
        register("dashboard-student") { Route.Dashboard }
        register("materials-list") { Route.MaterialsList }
        register("material-detail") { params ->
            Route.MaterialDetail(params["id"] ?: params["materialId"] ?: "")
        }
        register("app-settings") { Route.Settings }
    }

    fun register(screenKey: String, factory: (Map<String, String>) -> Route) {
        registrations[screenKey] = factory
    }

    fun resolve(screenKey: String, params: Map<String, String> = emptyMap()): Route? {
        return registrations[screenKey]?.invoke(params)
    }

    /**
     * For unknown screen keys, creates a DynamicRoute that the navigation
     * system can handle by loading the screen dynamically.
     */
    fun resolveOrDynamic(screenKey: String, params: Map<String, String> = emptyMap()): Route {
        return resolve(screenKey, params) ?: Route.Dynamic(screenKey, params)
    }
}
```

#### 7.5.2 Agregar Route.Dynamic

**Modificar archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/navigation/Routes.kt`

Agregar:
```kotlin
data class Dynamic(val screenKey: String, val params: Map<String, String> = emptyMap()) : Route("dynamic/$screenKey")
```

### 7.6 AdaptiveNavigationLayout

**Crear archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/AdaptiveNavigationLayout.kt`

```kotlin
package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.NavigationItem
// Import DS components from kmp-design

/**
 * Adaptive navigation layout that renders differently based on window size.
 * - Compact (mobile): BottomNavigationBar
 * - Medium (tablet): NavigationRail
 * - Expanded (desktop): PermanentNavigationDrawer
 */
@Composable
fun AdaptiveNavigationLayout(
    navDefinition: NavigationDefinition,
    currentScreenKey: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // For Phase 2, start with bottom nav for all platforms
    // Platform-specific layouts will be added in Phase 3
    Scaffold(
        bottomBar = {
            if (navDefinition.bottomNav.isNotEmpty()) {
                NavigationBar {
                    navDefinition.bottomNav.forEach { item ->
                        NavigationBarItem(
                            selected = item.screenKey == currentScreenKey ||
                                       item.key == currentScreenKey,
                            onClick = {
                                item.screenKey?.let { onNavigate(it) }
                            },
                            icon = {
                                // Map icon string to actual icon
                                // Use DS icon resolver
                                Icon(
                                    imageVector = resolveIcon(item.icon),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
```

### 7.7 Actualizar MainScreen

**Modificar archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/screens/MainScreen.kt`

Cambiar de tabs hardcodeados a carga dinamica:

```kotlin
@Composable
fun MainScreen(
    onNavigate: (String, Map<String, String>) -> Unit,
    onLogout: () -> Unit
) {
    val screenLoader = koinInject<ScreenLoader>()
    var navDefinition by remember { mutableStateOf<NavigationDefinition?>(null) }
    var currentTabKey by remember { mutableStateOf("dashboard") }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Load navigation config from backend
    LaunchedEffect(Unit) {
        when (val result = screenLoader.loadNavigation()) {
            is Result.Success -> {
                navDefinition = result.data
                // Set first tab as default
                result.data.bottomNav.firstOrNull()?.let {
                    currentTabKey = it.key
                }
            }
            is Result.Failure -> {
                // Fallback to hardcoded if backend fails
                navDefinition = fallbackNavigation()
            }
        }
        isLoading = false
    }

    if (isLoading) {
        // Show loading while nav config loads
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val nav = navDefinition ?: return

    // Create ViewModels for each tab (hoisted to survive tab switches)
    val viewModels = remember(nav) {
        nav.bottomNav.associate { item ->
            item.key to DynamicScreenViewModel(/* injected */)
        }
    }

    AdaptiveNavigationLayout(
        navDefinition = nav,
        currentScreenKey = currentTabKey,
        onNavigate = { screenKey ->
            // Find the tab for this screen key
            val tab = nav.bottomNav.find { it.screenKey == screenKey }
            if (tab != null) {
                currentTabKey = tab.key
            } else {
                onNavigate(screenKey, emptyMap())
            }
        }
    ) {
        // Render current tab's screen
        val currentItem = nav.bottomNav.find { it.key == currentTabKey }
        val currentScreenKey = currentItem?.screenKey

        if (currentScreenKey != null) {
            // Use the appropriate screen wrapper based on screenKey
            when {
                currentScreenKey.startsWith("dashboard") -> {
                    DynamicDashboardScreen(
                        onNavigate = onNavigate,
                        onLogout = onLogout
                    )
                }
                currentScreenKey == "app-settings" -> {
                    DynamicSettingsScreen(
                        onNavigate = onNavigate,
                        onBack = { /* no back from main tab */ },
                        onLogout = onLogout
                    )
                }
                else -> {
                    // Generic dynamic screen for any other tab
                    val viewModel = koinInject<DynamicScreenViewModel>()
                    DynamicScreen(
                        screenKey = currentScreenKey,
                        viewModel = viewModel,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}

// Fallback if backend navigation fails
private fun fallbackNavigation() = NavigationDefinition(
    bottomNav = listOf(
        NavigationItem(key = "dashboard", label = "Dashboard", icon = "home", screenKey = "dashboard-teacher"),
        NavigationItem(key = "materials", label = "Materials", icon = "folder", screenKey = "materials-list"),
        NavigationItem(key = "settings", label = "Settings", icon = "settings", screenKey = "app-settings")
    ),
    version = 0
)
```

### 7.8 Actualizar App.kt

**Modificar archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/App.kt`

Agregar manejo de `Route.Dynamic`:

```kotlin
is Route.Dynamic -> {
    val viewModel = koinInject<DynamicScreenViewModel>()
    DynamicScreen(
        screenKey = route.screenKey,
        viewModel = viewModel,
        onNavigate = handleDynamicNavigate,
        placeholders = buildPlaceholders(authState)
    )
}
```

Reemplazar `Route.fromScreenKey()` por uso de `RouteRegistry`:

```kotlin
val routeRegistry = remember { RouteRegistry() }

val handleDynamicNavigate: (String, Map<String, String>) -> Unit = { screenKey, params ->
    val route = routeRegistry.resolveOrDynamic(screenKey, params)
    navState.navigateTo(route)
}
```

### 7.9 Tests

**Crear archivo**: `kmp-screens/src/commonTest/.../navigation/RouteRegistryTest.kt`
- resolve known screenKey returns correct Route
- resolve unknown screenKey returns null
- resolveOrDynamic unknown screenKey returns Route.Dynamic
- register custom screenKey works

### 7.10 Validacion Agent 5
```bash
cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new
./gradlew :modules:dynamic-ui:compileKotlinDesktop
./gradlew :kmp-screens:compileKotlinDesktop
```

---

## 8. TAREA: AGENT 6 - "validator"

### 8.1 Objetivo
Validar que todos los cambios compilan, tests pasan, y la integracion es coherente.

### 8.2 Validaciones Go (con go.work)

```bash
cd /Users/jhoanmedina/source/EduGo/repos-separados

# 1. Compilar todos los modulos
cd edugo-shared/screenconfig && go build ./... && go test ./...
cd ../../edugo-shared/common && go build ./...
cd ../../edugo-infrastructure/postgres && go build ./...
cd ../../edugo-api-mobile && go build ./... && go test ./...
cd ../../edugo-api-administracion && go build ./... && go test ./...
```

### 8.3 Validaciones KMP

```bash
cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new

# Compilar modulos
./gradlew :modules:dynamic-ui:compileKotlinDesktop
./gradlew :modules:di:compileKotlinDesktop
./gradlew :kmp-screens:compileKotlinDesktop

# Tests
./gradlew :modules:dynamic-ui:desktopTest
```

### 8.4 Checklist de Integracion

- [ ] `screen_instances` tiene columna `handler_key` (SQL migration)
- [ ] Seeds de screen_instances incluyen `handler_key` para login y settings
- [ ] Entity `ScreenInstance` tiene campo `HandlerKey`
- [ ] Shared DTO `CombinedScreenDTO` tiene `HandlerKey`
- [ ] Shared tiene `ActionCustom` en tipos
- [ ] Shared tiene `NavigationConfigDTO` y `NavigationItemDTO`
- [ ] Mobile API: `ResourceReader` interface + implementacion postgres
- [ ] Mobile API: `GetNavigationConfig` lee de `resources` + filtra por permisos
- [ ] Mobile API: `CombinedScreenDTO` local tiene `handlerKey`
- [ ] Mobile API: SQL query de `GetCombinedScreen` incluye `handler_key`
- [ ] Mobile API: DI container conecta `ResourceReader`
- [ ] Admin API: CRUD de `handler_key` en screen instances
- [ ] KMP: `ScreenActionHandler` interface
- [ ] KMP: `ScreenHandlerRegistry` con `findHandler`
- [ ] KMP: `LoginActionHandler` implementado
- [ ] KMP: `SettingsActionHandler` implementado
- [ ] KMP: `DynamicScreenViewModel.executeAction` usa registry
- [ ] KMP: DI module registra handlers y registry
- [ ] KMP: `NavigationDefinition` modelo con `bottomNav` + `drawerItems`
- [ ] KMP: `RemoteScreenLoader.loadNavigation()` implementado
- [ ] KMP: `RouteRegistry` con `resolveOrDynamic`
- [ ] KMP: `Route.Dynamic` agregado
- [ ] KMP: `AdaptiveNavigationLayout` composable
- [ ] KMP: `MainScreen` carga nav config del backend con fallback
- [ ] KMP: `App.kt` usa `RouteRegistry` y maneja `Route.Dynamic`
- [ ] Screen wrappers simplificados (no duplican logica de handlers)

---

## 9. RESUMEN DE ARCHIVOS POR AGENTE

### Agent 1: shared-infra (2 repos, ~8 archivos)
**CREAR:**
- `edugo-infrastructure/postgres/migrations/structure/020_alter_screen_instances_handler_key.sql`

**MODIFICAR:**
- `edugo-infrastructure/postgres/migrations/seeds/007_seed_screen_instances.sql`
- `edugo-infrastructure/postgres/entities/screen_instance.go`
- `edugo-shared/screenconfig/types.go`
- `edugo-shared/screenconfig/dto.go`
- `edugo-shared/screenconfig/validation.go` (agregar CUSTOM a validacion)

### Agent 2: backend-mobile (1 repo, ~10 archivos)
**CREAR:**
- `internal/domain/repository/resource_repository.go`
- `internal/infrastructure/persistence/postgres/repository/resource_repository.go`
- `internal/infrastructure/persistence/postgres/repository/resource_repository_test.go`
- `internal/application/service/screen_service_navigation_test.go`

**MODIFICAR:**
- `internal/domain/repository/screen_repository.go` (agregar HandlerKey a CombinedScreen)
- `internal/application/service/screen_service.go` (navegacion dinamica + HandlerKey)
- `internal/application/dto/screen_dto.go` (agregar HandlerKey)
- `internal/infrastructure/persistence/postgres/repository/screen_repository.go` (SQL + HandlerKey)
- `internal/infrastructure/http/handler/screen_handler.go` (pasar permissions)
- `internal/container/repositories.go` (agregar ResourceReader)
- `internal/container/services.go` (actualizar constructor)

### Agent 3: backend-admin (1 repo, ~4 archivos)
**MODIFICAR:**
- `internal/application/service/screen_config_service.go` (handler_key en create/update)
- `internal/application/dto/screen_config_dto.go` (handler_key en requests)
- `internal/infrastructure/http/handler/screen_config_handler.go` (si necesita cambios)
- Tests existentes actualizados

### Agent 4: frontend-handlers (1 repo, ~10 archivos)
**CREAR:**
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/ScreenActionHandler.kt`
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/ScreenHandlerRegistry.kt`
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/handlers/LoginActionHandler.kt`
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/handler/handlers/SettingsActionHandler.kt`
- `modules/dynamic-ui/src/commonTest/kotlin/com/edugo/kmp/dynamicui/handler/ScreenHandlerRegistryTest.kt`
- `modules/dynamic-ui/src/commonTest/kotlin/com/edugo/kmp/dynamicui/handler/handlers/LoginActionHandlerTest.kt`

**MODIFICAR:**
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/viewmodel/DynamicScreenViewModel.kt`
- `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/DynamicUiModule.kt`
- `kmp-screens/.../dynamic/screens/DynamicLoginScreen.kt` (simplificar)
- `kmp-screens/.../dynamic/screens/DynamicSettingsScreen.kt` (simplificar)

### Agent 5: frontend-navigation (1 repo, ~8 archivos)
**CREAR:**
- `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/navigation/RouteRegistry.kt`
- `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/AdaptiveNavigationLayout.kt`

**MODIFICAR:**
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/model/NavigationConfig.kt` (actualizar NavigationDefinition)
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/loader/RemoteScreenLoader.kt` (implementar loadNavigation)
- `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/loader/CachedScreenLoader.kt` (cache nav)
- `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/navigation/Routes.kt` (agregar Dynamic)
- `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/screens/MainScreen.kt` (nav dinamica)
- `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/App.kt` (RouteRegistry + Dynamic)

---

## 10. CRITERIOS DE EXITO

1. **Go build limpio**: `go build ./...` sin errores en todos los modulos del workspace
2. **Go tests pasan**: `go test ./...` sin fallos en mobile y admin
3. **KMP compila**: `./gradlew :modules:dynamic-ui:compileKotlinDesktop` y `:kmp-screens:compileKotlinDesktop` sin errores
4. **KMP tests pasan**: `./gradlew :modules:dynamic-ui:desktopTest` sin fallos
5. **Navegacion dinamica**: El endpoint `/v1/screens/navigation` retorna items filtrados por permisos
6. **Handler registry**: `DynamicScreenViewModel.executeAction()` consulta el registry antes del handler generico
7. **Rutas extensibles**: `Route.Dynamic` permite navegar a pantallas no registradas estaticamente
8. **Backward compatible**: El fallback en MainScreen garantiza que si el backend no responde, se muestra la navegacion por defecto
