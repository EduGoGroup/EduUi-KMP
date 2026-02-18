# 3. API Endpoints

Base URL: `http://localhost:8081` (API Admin)
Todos los endpoints requieren JWT en header `Authorization: Bearer {token}`.

---

## Endpoint Principal: Resolve (Frontend)

Este es el UNICO endpoint que el frontend consume para renderizar pantallas.

```
GET /v1/screen-config/resolve/key/{screenKey}
```

**Permiso requerido:** `screen_instances:read`

**Ejemplo:**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/v1/screen-config/resolve/key/app-settings
```

**Respuesta (`CombinedScreenDTO`):**
```json
{
  "screenId": "b0000000-0000-0000-0000-000000000006",
  "screenKey": "app-settings",
  "screenName": "Configuracion",
  "pattern": "settings",
  "version": 1,
  "template": {
    "zones": [
      {
        "id": "section_appearance",
        "type": "form-section",
        "slots": [
          {"id": "appearance_title", "controlType": "label", "style": "title-medium", "bind": "slot:appearance_title"},
          {"id": "dark_mode", "controlType": "switch", "bind": "slot:dark_mode_label"}
        ]
      }
    ]
  },
  "slotData": {
    "page_title": "Settings",
    "appearance_title": "Appearance",
    "dark_mode_label": "Dark Mode"
  },
  "actions": [
    {
      "id": "logout",
      "trigger": "button_click",
      "triggerSlotId": "logout_btn",
      "type": "CONFIRM",
      "config": {"message": "Are you sure you want to sign out?"}
    }
  ],
  "dataEndpoint": null,
  "dataConfig": null,
  "updatedAt": "2026-02-15T00:00:00Z"
}
```

### Que hace internamente

```go
func ResolveScreenByKey(ctx, key string) (*CombinedScreenDTO, error) {
    // 1. Busca instance por screen_key
    instance := instanceRepo.GetByScreenKey(ctx, key)

    // 2. Busca template por template_id
    template := templateRepo.GetByID(ctx, instance.TemplateID)

    // 3. Combina en un solo DTO
    return &CombinedScreenDTO{
        ScreenID:     instance.ID,
        ScreenKey:    instance.ScreenKey,
        ScreenName:   instance.Name,
        Pattern:      template.Pattern,
        Version:      template.Version,
        Template:     template.Definition,   // JSONB del template
        SlotData:     instance.SlotData,     // JSONB de la instancia
        Actions:      instance.Actions,
        DataEndpoint: instance.DataEndpoint,
        DataConfig:   instance.DataConfig,
        UpdatedAt:    instance.UpdatedAt,
    }
}
```

---

## Endpoints CRUD: Templates

### Crear template
```
POST /v1/screen-config/templates
Permiso: screen_templates:create
```

```json
{
  "pattern": "list",
  "name": "list-card-v1",
  "description": "Lista con variante de tarjetas",
  "definition": {
    "zones": [
      {
        "id": "content",
        "type": "card-list",
        "itemLayout": {
          "slots": [
            {"id": "title", "controlType": "label", "style": "headline-small", "field": "title"},
            {"id": "subtitle", "controlType": "label", "style": "body-small", "field": "subtitle"}
          ]
        }
      }
    ]
  }
}
```

### Listar templates
```
GET /v1/screen-config/templates?pattern=list&page=1&per_page=20
Permiso: screen_templates:read
```

### Obtener template por ID
```
GET /v1/screen-config/templates/{id}
Permiso: screen_templates:read
```

### Actualizar template
```
PUT /v1/screen-config/templates/{id}
Permiso: screen_templates:update
```
Nota: Si se modifica `definition`, el `version` se incrementa automaticamente.

### Eliminar template (logico)
```
DELETE /v1/screen-config/templates/{id}
Permiso: screen_templates:delete
```
Pone `is_active = false`. No hace hard delete.

---

## Endpoints CRUD: Instances

### Crear instancia
```
POST /v1/screen-config/instances
Permiso: screen_instances:create
```

```json
{
  "screen_key": "courses-list",
  "template_id": "a0000000-0000-0000-0000-000000000003",
  "name": "Lista de Cursos",
  "slot_data": {
    "page_title": "Courses",
    "search_placeholder": "Search courses...",
    "empty_state_title": "No courses yet"
  },
  "actions": [
    {"id": "item-click", "trigger": "item_click", "type": "NAVIGATE", "config": {"target": "course-detail"}}
  ],
  "data_endpoint": "/v1/courses",
  "data_config": {
    "method": "GET",
    "pagination": {"pageSize": 20, "limitParam": "limit", "offsetParam": "offset"}
  },
  "scope": "unit",
  "required_permission": "courses:read"
}
```

### Listar instancias
```
GET /v1/screen-config/instances?template_id={id}&page=1&per_page=20
Permiso: screen_instances:read
```

### Obtener instancia por ID
```
GET /v1/screen-config/instances/{id}
Permiso: screen_instances:read
```

### Obtener instancia por screen_key
```
GET /v1/screen-config/instances/key/{key}
Permiso: screen_instances:read
```

### Actualizar instancia
```
PUT /v1/screen-config/instances/{id}
Permiso: screen_instances:update
```

### Eliminar instancia (logico)
```
DELETE /v1/screen-config/instances/{id}
Permiso: screen_instances:delete
```

---

## Endpoints: Resource-Screen Mapping

### Vincular pantalla a recurso
```
POST /v1/screen-config/resource-screens
Permiso: screen_instances:create
```

```json
{
  "resource_id": "20000000-0000-0000-0000-000000000030",
  "resource_key": "materials",
  "screen_key": "materials-list",
  "screen_type": "list",
  "is_default": true
}
```

### Obtener pantallas de un recurso
```
GET /v1/screen-config/resource-screens/{resourceId}
Permiso: screen_instances:read
```

### Desvincular
```
DELETE /v1/screen-config/resource-screens/{id}
Permiso: screen_instances:delete
```

---

## Validaciones del Backend

| Campo | Regla |
|-------|-------|
| `pattern` | Debe ser uno de: login, form, list, dashboard, settings, detail, search, profile, modal, notification, onboarding, empty-state |
| `screen_type` | Debe ser uno de: list, detail, create, edit, dashboard, settings |
| `action.type` | Debe ser uno de: NAVIGATE, NAVIGATE_BACK, API_CALL, SUBMIT_FORM, REFRESH, CONFIRM, LOGOUT |
| `template.definition` | Debe tener al menos 1 zona. Cada zona necesita `id` y `type`. Cada slot necesita `id` y `controlType`. |
| `screen_key` | Unico en toda la tabla |

---

## DTOs Go (edugo-shared)

```go
// screenconfig/dto.go

type CombinedScreenDTO struct {
    ScreenID        string          `json:"screenId"`
    ScreenKey       string          `json:"screenKey"`
    ScreenName      string          `json:"screenName"`
    Pattern         Pattern         `json:"pattern"`
    Version         int             `json:"version"`
    Template        json.RawMessage `json:"template"`
    SlotData        json.RawMessage `json:"slotData,omitempty"`
    DataEndpoint    string          `json:"dataEndpoint,omitempty"`
    DataConfig      json.RawMessage `json:"dataConfig,omitempty"`
    Actions         json.RawMessage `json:"actions"`
    UserPreferences json.RawMessage `json:"userPreferences,omitempty"`
    UpdatedAt       time.Time       `json:"updatedAt"`
}
```
