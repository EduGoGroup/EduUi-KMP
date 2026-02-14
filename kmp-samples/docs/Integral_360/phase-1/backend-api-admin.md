# Fase 1: Cambios en API Admin (edugo-api-administracion)

## Descripcion General

Agregar capacidades de gestion de configuracion de pantallas a la API de administracion. Esto incluye operaciones CRUD para templates de pantalla, instancias de pantalla y mapeos recurso-pantalla.

## Proyecto: `edugo-api-administracion`

### 1. Nuevo Modulo de Dominio: Configuracion de Pantalla

#### Interfaz de Repository

```go
// internal/domain/repository/screen_config_repository.go

type ScreenTemplateRepository interface {
    Create(ctx context.Context, template *entity.ScreenTemplate) error
    GetByID(ctx context.Context, id uuid.UUID) (*entity.ScreenTemplate, error)
    GetByPattern(ctx context.Context, pattern string) ([]*entity.ScreenTemplate, error)
    List(ctx context.Context, filter ScreenTemplateFilter) ([]*entity.ScreenTemplate, error)
    Update(ctx context.Context, template *entity.ScreenTemplate) error
    Delete(ctx context.Context, id uuid.UUID) error
}

type ScreenInstanceRepository interface {
    Create(ctx context.Context, instance *entity.ScreenInstance) error
    GetByID(ctx context.Context, id uuid.UUID) (*entity.ScreenInstance, error)
    GetByScreenKey(ctx context.Context, key string) (*entity.ScreenInstance, error)
    List(ctx context.Context, filter ScreenInstanceFilter) ([]*entity.ScreenInstance, error)
    Update(ctx context.Context, instance *entity.ScreenInstance) error
    Delete(ctx context.Context, id uuid.UUID) error
}

type ResourceScreenRepository interface {
    Create(ctx context.Context, rs *entity.ResourceScreen) error
    GetByResourceID(ctx context.Context, resourceID uuid.UUID) ([]*entity.ResourceScreen, error)
    GetByResourceKey(ctx context.Context, key string) ([]*entity.ResourceScreen, error)
    Update(ctx context.Context, rs *entity.ResourceScreen) error
    Delete(ctx context.Context, id uuid.UUID) error
}
```

#### Capa de Servicio

```go
// internal/application/service/screen_config_service.go

type ScreenConfigService struct {
    templateRepo  repository.ScreenTemplateRepository
    instanceRepo  repository.ScreenInstanceRepository
    resourceRepo  repository.ResourceScreenRepository
    logger        logger.Logger
}

// Operaciones de template
func (s *ScreenConfigService) CreateTemplate(ctx context.Context, req CreateTemplateRequest) (*ScreenTemplateDTO, error)
func (s *ScreenConfigService) GetTemplate(ctx context.Context, id uuid.UUID) (*ScreenTemplateDTO, error)
func (s *ScreenConfigService) ListTemplates(ctx context.Context, filter TemplateFilter) ([]*ScreenTemplateDTO, error)
func (s *ScreenConfigService) UpdateTemplate(ctx context.Context, id uuid.UUID, req UpdateTemplateRequest) (*ScreenTemplateDTO, error)
func (s *ScreenConfigService) DeleteTemplate(ctx context.Context, id uuid.UUID) error

// Operaciones de instancia
func (s *ScreenConfigService) CreateInstance(ctx context.Context, req CreateInstanceRequest) (*ScreenInstanceDTO, error)
func (s *ScreenConfigService) GetInstance(ctx context.Context, id uuid.UUID) (*ScreenInstanceDTO, error)
func (s *ScreenConfigService) GetInstanceByKey(ctx context.Context, key string) (*ScreenInstanceDTO, error)
func (s *ScreenConfigService) ListInstances(ctx context.Context, filter InstanceFilter) ([]*ScreenInstanceDTO, error)
func (s *ScreenConfigService) UpdateInstance(ctx context.Context, id uuid.UUID, req UpdateInstanceRequest) (*ScreenInstanceDTO, error)
func (s *ScreenConfigService) DeleteInstance(ctx context.Context, id uuid.UUID) error

// Mapeo recurso-pantalla
func (s *ScreenConfigService) LinkScreenToResource(ctx context.Context, req LinkScreenRequest) error
func (s *ScreenConfigService) GetScreensForResource(ctx context.Context, resourceID uuid.UUID) ([]*ResourceScreenDTO, error)
func (s *ScreenConfigService) UnlinkScreen(ctx context.Context, id uuid.UUID) error
```

### 2. Nuevos Endpoints de API

Todos los endpoints bajo `/v1/screen-config/` requieren permisos de administrador.

#### Templates de Pantalla

| Metodo | Ruta | Permiso | Proposito |
|--------|------|---------|-----------|
| POST | `/v1/screen-config/templates` | `screen_templates:create` | Crear nuevo template |
| GET | `/v1/screen-config/templates` | `screen_templates:read` | Listar todos los templates (filtrar por pattern) |
| GET | `/v1/screen-config/templates/:id` | `screen_templates:read` | Obtener template por ID |
| PUT | `/v1/screen-config/templates/:id` | `screen_templates:update` | Actualizar template |
| DELETE | `/v1/screen-config/templates/:id` | `screen_templates:delete` | Eliminacion logica del template |

#### Instancias de Pantalla

| Metodo | Ruta | Permiso | Proposito |
|--------|------|---------|-----------|
| POST | `/v1/screen-config/instances` | `screen_instances:create` | Crear instancia de pantalla |
| GET | `/v1/screen-config/instances` | `screen_instances:read` | Listar todas las instancias |
| GET | `/v1/screen-config/instances/:id` | `screen_instances:read` | Obtener instancia por ID |
| GET | `/v1/screen-config/instances/key/:key` | `screen_instances:read` | Obtener instancia por screen_key |
| PUT | `/v1/screen-config/instances/:id` | `screen_instances:update` | Actualizar instancia |
| DELETE | `/v1/screen-config/instances/:id` | `screen_instances:delete` | Eliminacion logica de la instancia |

#### Mapeos Recurso-Pantalla

| Metodo | Ruta | Permiso | Proposito |
|--------|------|---------|-----------|
| POST | `/v1/screen-config/resource-screens` | `screen_instances:create` | Vincular pantalla a recurso |
| GET | `/v1/screen-config/resource-screens/:resourceId` | `screen_instances:read` | Obtener pantallas de un recurso |
| DELETE | `/v1/screen-config/resource-screens/:id` | `screen_instances:delete` | Desvincular pantalla de recurso |

### 3. Ejemplos de Request/Response

#### Crear Template

```json
// POST /v1/screen-config/templates
// Request
{
  "pattern": "list",
  "name": "Basic List Template",
  "description": "Standard list with search bar, empty state, and item list",
  "definition": {
    "navigation": {
      "topBar": { "title": "slot:page_title", "showBack": false }
    },
    "zones": [
      {
        "id": "search_zone",
        "type": "container",
        "slots": [
          { "id": "search_bar", "controlType": "search-bar", "bind": "slot:search_placeholder" }
        ]
      },
      {
        "id": "list_content",
        "type": "simple-list",
        "itemLayout": {
          "slots": [
            { "id": "item_title", "controlType": "label", "style": "headline-small", "bind": "item:title" },
            { "id": "item_subtitle", "controlType": "label", "style": "body-small", "bind": "item:subtitle" }
          ]
        }
      }
    ]
  }
}

// Response (201)
{
  "id": "550e8400-...",
  "pattern": "list",
  "name": "Basic List Template",
  "version": 1,
  "is_active": true,
  "created_at": "2026-02-14T10:00:00Z"
}
```

#### Crear Instancia

```json
// POST /v1/screen-config/instances
// Request
{
  "screen_key": "materials-list",
  "template_id": "550e8400-...",
  "name": "Materials List",
  "slot_data": {
    "page_title": "Educational Materials",
    "search_placeholder": "Search materials..."
  },
  "actions": [
    {
      "id": "item-click",
      "trigger": "item_click",
      "type": "NAVIGATE",
      "config": { "target": "material-detail", "params": { "id": "{item.id}" } }
    }
  ],
  "data_endpoint": "/v1/materials",
  "data_config": {
    "method": "GET",
    "pagination": { "type": "offset", "pageSize": 20 },
    "fieldMapping": { "title": "title", "subtitle": "subject" }
  },
  "required_permission": "materials:read"
}
```

### 4. Endpoint de Menu Mejorado

Extender el endpoint existente `GET /v1/menu` para incluir la configuracion de pantalla:

```json
// GET /v1/menu (respuesta mejorada)
{
  "items": [
    {
      "id": "res-materials",
      "key": "materials",
      "displayName": "Materials",
      "icon": "folder",
      "sortOrder": 1,
      "screens": {
        "list": "materials-list",
        "detail": "material-detail",
        "create": "material-create"
      },
      "children": []
    },
    {
      "id": "res-dashboard",
      "key": "dashboard",
      "displayName": "Dashboard",
      "icon": "dashboard",
      "sortOrder": 0,
      "screens": {
        "dashboard": "dashboard-teacher"
      },
      "children": []
    }
  ]
}
```

### 5. Implementacion del Repository PostgreSQL

Ubicacion: `internal/infrastructure/persistence/postgres/`

Nuevos archivos:
- `screen_template_repository.go`
- `screen_instance_repository.go`
- `resource_screen_repository.go`

Detalles clave de implementacion:
- Usar `database/sql` con queries directos (consistente con el codebase existente)
- Columnas JSONB manejadas via `json.RawMessage` y `sql.Scanner`
- Eliminacion logica via `is_active = false`
- Paginacion via offset/limit

### 6. Registro de Rutas

```go
// internal/infrastructure/http/router/router.go

// Configuracion de Pantalla (solo Admin)
screenConfig := v1.Group("/screen-config")
{
    templates := screenConfig.Group("/templates")
    templates.Use(ginmiddleware.RequirePermission(enum.PermissionScreenTemplatesRead))
    {
        templates.POST("", screenConfigHandler.CreateTemplate)
        templates.GET("", screenConfigHandler.ListTemplates)
        templates.GET("/:id", screenConfigHandler.GetTemplate)
    }
    templates.Use(ginmiddleware.RequirePermission(enum.PermissionScreenTemplatesUpdate))
    {
        templates.PUT("/:id", screenConfigHandler.UpdateTemplate)
    }
    // ... similar para instancias y resource-screens
}
```

### 7. Datos Semilla para RBAC

Agregar nuevos permisos de rol para la gestion de configuracion de pantallas:

```sql
-- Agregar permisos
INSERT INTO public.permissions (name, display_name, resource_id, resource_key, action, scope) VALUES
('screen_templates:read', 'View Screen Templates', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'read', 'system'),
('screen_templates:create', 'Create Screen Templates', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'create', 'system'),
('screen_templates:update', 'Update Screen Templates', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'update', 'system'),
('screen_templates:delete', 'Delete Screen Templates', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'delete', 'system'),
('screen_instances:read', 'View Screen Instances', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'read', 'system'),
('screen_instances:create', 'Create Screen Instances', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'create', 'system'),
('screen_instances:update', 'Update Screen Instances', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'update', 'system'),
('screen_instances:delete', 'Delete Screen Instances', (SELECT id FROM resources WHERE key = 'screen_config'), 'screen_config', 'delete', 'system');

-- Agregar recurso para configuracion de pantalla
INSERT INTO public.resources (key, display_name, icon, is_menu_visible, scope) VALUES
('screen_config', 'Screen Configuration', 'settings_applications', true, 'system');

-- Otorgar al rol admin
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM public.roles r, public.permissions p
WHERE r.name = 'super_admin' AND p.resource_key = 'screen_config';
```

## Criterios de Aceptacion

- [ ] Todos los endpoints CRUD funcionan para templates, instancias y resource-screens
- [ ] El JSON de definicion de template se valida al crear/actualizar
- [ ] Los permisos se aplican correctamente en todos los endpoints
- [ ] El endpoint de menu retorna los mapeos de pantalla para cada recurso
- [ ] La eliminacion logica funciona para templates e instancias
- [ ] La paginacion funciona en los endpoints de listado
- [ ] El filtro por pattern funciona en el listado de templates
