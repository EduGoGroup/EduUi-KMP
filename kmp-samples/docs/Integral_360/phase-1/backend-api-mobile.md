# Fase 1: Cambios en la API Mobile (edugo-api-mobile)

## Descripcion General

Agregar endpoints de solo lectura para servir definiciones de pantalla combinadas al frontend KMP. La API mobile es el unico punto de contacto para todos los clientes frontend.

## Proyecto: `edugo-api-mobile`

### 1. Nuevo Modulo de Dominio: Screen Loader

#### Interfaz del Repository

```go
// internal/domain/repository/screen_repository.go

type ScreenRepository interface {
    // GetCombinedScreen carga template + instancia + preferencias de usuario en una sola consulta
    GetCombinedScreen(ctx context.Context, screenKey string, userID uuid.UUID) (*CombinedScreen, error)

    // GetScreensForResource retorna todas las configuraciones de pantalla vinculadas a un recurso
    GetScreensForResource(ctx context.Context, resourceKey string) ([]*ResourceScreenInfo, error)

    // GetUserPreferences retorna las preferencias especificas del usuario para una pantalla
    GetUserPreferences(ctx context.Context, screenKey string, userID uuid.UUID) (json.RawMessage, error)

    // SaveUserPreferences guarda las preferencias especificas del usuario
    SaveUserPreferences(ctx context.Context, screenKey string, userID uuid.UUID, prefs json.RawMessage) error
}
```

#### Capa de Servicio

```go
// internal/application/service/screen_service.go

type ScreenService struct {
    repo   repository.ScreenRepository
    cache  cache.Cache  // Redis o en memoria
    logger logger.Logger
}

// GetScreen retorna la definicion de pantalla combinada para el renderizado del frontend
func (s *ScreenService) GetScreen(ctx context.Context, screenKey string, userID uuid.UUID, platform string) (*CombinedScreenDTO, error) {
    // 1. Verificar cache (clave: "screen:{screenKey}:v{version}")
    // 2. Si no hay hit, cargar desde BD (consulta con un solo JOIN)
    // 3. Resolver referencias de slot (reemplazar "slot:xxx" con valores reales)
    // 4. Aplicar overrides de plataforma si corresponde
    // 5. Fusionar preferencias de usuario
    // 6. Almacenar en cache
    // 7. Retornar DTO combinado
}

// GetNavigationConfig retorna la estructura completa de navegacion para el usuario
func (s *ScreenService) GetNavigationConfig(ctx context.Context, userID uuid.UUID, platform string) (*NavigationConfigDTO, error) {
    // 1. Cargar menu del usuario (filtrado por permisos - llama a api-admin o consulta local)
    // 2. Para cada item del menu, resolver los mapeos de screen_key
    // 3. Retornar estructura de navegacion con referencias a pantallas
}

// SaveUserPreferences almacena las preferencias de pantalla especificas del usuario
func (s *ScreenService) SaveUserPreferences(ctx context.Context, screenKey string, userID uuid.UUID, prefs json.RawMessage) error
```

### 2. Nuevos Endpoints de la API

Todos los endpoints requieren autenticacion JWT.

| Metodo | Ruta | Permiso | Proposito |
|--------|------|---------|-----------|
| GET | `/v1/screens/:screenKey` | `screens:read` | Obtener definicion de pantalla combinada |
| GET | `/v1/screens/resource/:resourceKey` | `screens:read` | Obtener pantallas para un recurso |
| GET | `/v1/screens/navigation` | `screens:read` | Obtener configuracion completa de navegacion con mapeos de pantallas |
| PUT | `/v1/screens/:screenKey/preferences` | `screens:read` | Guardar preferencias de usuario para una pantalla |

### 3. Contrato de la API

#### GET /v1/screens/:screenKey

Parametros de consulta:
- `platform` (opcional): `mobile`, `desktop`, `web` - aplica overrides especificos de plataforma

Respuesta:
```json
{
  "screenId": "si-materials-list",
  "screenKey": "materials-list",
  "screenName": "Educational Materials",
  "pattern": "list",
  "version": 1,
  "template": {
    "navigation": {
      "topBar": {
        "title": "Educational Materials",
        "showBack": false,
        "actions": []
      }
    },
    "zones": [
      {
        "id": "search_zone",
        "type": "container",
        "distribution": "stacked",
        "slots": [
          {
            "id": "search_bar",
            "controlType": "search-bar",
            "placeholder": "Search materials..."
          }
        ]
      },
      {
        "id": "empty_state",
        "type": "container",
        "condition": "data.isEmpty",
        "slots": [
          { "id": "empty_icon", "controlType": "icon", "value": "folder_open" },
          { "id": "empty_title", "controlType": "label", "style": "headline", "value": "No materials available" },
          { "id": "empty_desc", "controlType": "label", "style": "body", "value": "Materials will appear here once uploaded" }
        ]
      },
      {
        "id": "list_content",
        "type": "simple-list",
        "condition": "!data.isEmpty",
        "itemLayout": {
          "slots": [
            { "id": "item_title", "controlType": "label", "style": "headline-small", "field": "title" },
            { "id": "item_subtitle", "controlType": "label", "style": "body-small", "field": "subject" },
            { "id": "item_trailing", "controlType": "icon", "field": "status" }
          ]
        }
      }
    ],
    "platformOverrides": {
      "desktop": {
        "zones": {
          "list_content": { "distribution": "grid", "columns": 2 }
        }
      }
    }
  },
  "dataEndpoint": "/v1/materials",
  "dataConfig": {
    "method": "GET",
    "pagination": { "type": "offset", "pageSize": 20, "pageParam": "offset", "limitParam": "limit" },
    "defaultParams": { "sort": "created_at", "order": "desc" },
    "fieldMapping": {
      "title": "title",
      "subject": "subject",
      "status": "status",
      "id": "id"
    }
  },
  "actions": [
    {
      "id": "item-click",
      "trigger": "item_click",
      "type": "NAVIGATE",
      "config": { "target": "material-detail", "params": { "id": "{item.id}" } }
    },
    {
      "id": "pull-refresh",
      "trigger": "pull_refresh",
      "type": "REFRESH"
    }
  ],
  "userPreferences": {},
  "updatedAt": "2026-02-14T10:00:00Z"
}
```

Encabezados retornados:
- `ETag`: Hash de la configuracion de pantalla para cache
- `Last-Modified`: Marca de tiempo de la ultima actualizacion
- `Cache-Control`: `max-age=3600` (1 hora)

#### GET /v1/screens/navigation

Retorna la estructura completa de navegacion para el usuario autenticado:

```json
{
  "bottomNav": [
    {
      "key": "dashboard",
      "label": "Home",
      "icon": "home",
      "screenKey": "dashboard-teacher",
      "sortOrder": 0
    },
    {
      "key": "materials",
      "label": "Materials",
      "icon": "folder",
      "screenKey": "materials-list",
      "sortOrder": 1
    },
    {
      "key": "settings",
      "label": "Settings",
      "icon": "settings",
      "screenKey": "app-settings",
      "sortOrder": 4
    }
  ],
  "drawerItems": [],
  "version": 3
}
```

### 4. Consulta a Base de Datos

Consulta optimizada unica para cargar la pantalla combinada:

```sql
SELECT
    si.id,
    si.screen_key,
    si.name,
    st.pattern,
    st.version,
    st.definition,
    si.slot_data,
    si.actions,
    si.data_endpoint,
    si.data_config,
    COALESCE(sup.preferences, '{}'::jsonb) as user_preferences,
    GREATEST(si.updated_at, st.updated_at) as last_updated
FROM ui_config.screen_instances si
JOIN ui_config.screen_templates st ON si.template_id = st.id
LEFT JOIN ui_config.screen_user_preferences sup
    ON sup.screen_instance_id = si.id AND sup.user_id = $2
WHERE si.screen_key = $1
    AND si.is_active = true
    AND st.is_active = true;
```

### 5. Logica de Resolucion de Slot

El servicio resuelve las referencias `slot:xxx` en el template con valores reales de `slot_data`:

```go
func resolveSlots(template json.RawMessage, slotData map[string]interface{}) json.RawMessage {
    // Recorrer el arbol JSON del template
    // Encontrar todas las referencias "bind": "slot:xxx"
    // Reemplazar con el valor correspondiente de slotData
    // Retornar el template resuelto
    //
    // Ejemplo:
    // Template: { "title": "slot:page_title" }
    // SlotData: { "page_title": "Educational Materials" }
    // Resultado:   { "title": "Educational Materials" }
}
```

### 6. Estrategia de Cache

```go
// Formato de clave de cache: "screen:{screenKey}:v{version}"
// TTL: 1 hora
// Invalidacion: basada en TTL (sin invalidacion manual en la Fase 1)

func (s *ScreenService) getCached(screenKey string) (*CombinedScreenDTO, bool) {
    key := fmt.Sprintf("screen:%s", screenKey)
    return s.cache.Get(key)
}

func (s *ScreenService) setCache(screenKey string, screen *CombinedScreenDTO) {
    key := fmt.Sprintf("screen:%s", screenKey)
    s.cache.Set(key, screen, 1*time.Hour)
}
```

### 7. Soporte para Peticiones Condicionales

Soporte para encabezados `If-None-Match` e `If-Modified-Since`:

```go
func (h *ScreenHandler) GetScreen(c *gin.Context) {
    // Verificar encabezado ETag
    ifNoneMatch := c.GetHeader("If-None-Match")
    if ifNoneMatch != "" && ifNoneMatch == currentETag {
        c.Status(http.StatusNotModified) // 304
        return
    }

    // Retornar respuesta completa con ETag
    c.Header("ETag", currentETag)
    c.Header("Last-Modified", screen.UpdatedAt.Format(http.TimeFormat))
    c.JSON(http.StatusOK, screen)
}
```

### 8. Registro de Rutas

```go
// internal/infrastructure/http/router/router.go

screens := v1.Group("/screens")
screens.Use(authMiddleware)
{
    screens.GET("/:screenKey", screenHandler.GetScreen)
    screens.GET("/resource/:resourceKey", screenHandler.GetScreensForResource)
    screens.GET("/navigation", screenHandler.GetNavigation)
    screens.PUT("/:screenKey/preferences", screenHandler.SavePreferences)
}
```

### 9. Consideraciones de Middleware

Los endpoints de pantalla reutilizan la pila de middleware existente:
1. Recovery -> RequestID -> Metrics -> Logging -> CORS -> ClientInfo
2. **RemoteAuthMiddleware** - valida JWT (existente)
3. **Sin verificacion de permisos por endpoint** - utiliza el permiso generico `screens:read` para todos los endpoints de pantalla
4. El campo `required_permission` en screen_instances se aplica a nivel de navegacion (las pantallas que no estan en el menu del usuario no son accesibles)

## Criterios de Aceptacion

- [ ] `GET /v1/screens/:screenKey` retorna la definicion de pantalla combinada
- [ ] La resolucion de slot funciona correctamente (reemplaza las referencias `slot:xxx`)
- [ ] Los overrides de plataforma se aplican cuando se proporciona el parametro de consulta `platform`
- [ ] Las preferencias de usuario se fusionan en la respuesta
- [ ] El endpoint de navegacion retorna la estructura correcta del menu con las claves de pantalla
- [ ] El cache con ETag/304 funciona para peticiones condicionales
- [ ] El cache en Redis/memoria reduce las consultas a base de datos
- [ ] Consulta optimizada con un solo JOIN para la carga de pantallas
- [ ] Tiempo de respuesta < 50ms para pantallas en cache
