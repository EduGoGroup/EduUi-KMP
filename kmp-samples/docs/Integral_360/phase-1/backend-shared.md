# Fase 1: Cambios en el Modulo Shared (edugo-shared)

## Descripcion General

Agregar tipos compartidos, DTOs y validacion para definiciones de pantalla que tanto api-admin como api-mobile utilizaran.

## Proyecto: `edugo-shared`

### 1. Nuevo Modulo: `screenconfig`

Ubicacion: `screenconfig/` (nuevo modulo de nivel superior en edugo-shared)

#### Tipos

```go
// screenconfig/types.go

package screenconfig

// Pattern enumera los patterns de pantalla soportados
type Pattern string

const (
    PatternLogin        Pattern = "login"
    PatternForm         Pattern = "form"
    PatternList         Pattern = "list"
    PatternDashboard    Pattern = "dashboard"
    PatternSettings     Pattern = "settings"
    PatternDetail       Pattern = "detail"
    PatternSearch       Pattern = "search"
    PatternProfile      Pattern = "profile"
    PatternModal        Pattern = "modal"
    PatternNotification Pattern = "notification"
    PatternOnboarding   Pattern = "onboarding"
    PatternEmptyState   Pattern = "empty-state"
)

// ScreenType define como una pantalla se relaciona con un recurso
type ScreenType string

const (
    ScreenTypeList   ScreenType = "list"
    ScreenTypeDetail ScreenType = "detail"
    ScreenTypeCreate ScreenType = "create"
    ScreenTypeEdit   ScreenType = "edit"
    ScreenTypeDashboard ScreenType = "dashboard"
    ScreenTypeSettings  ScreenType = "settings"
)

// ActionType enumera las acciones estandar
type ActionType string

const (
    ActionNavigate     ActionType = "NAVIGATE"
    ActionNavigateBack ActionType = "NAVIGATE_BACK"
    ActionAPICall      ActionType = "API_CALL"
    ActionSubmitForm   ActionType = "SUBMIT_FORM"
    ActionRefresh      ActionType = "REFRESH"
    ActionConfirm      ActionType = "CONFIRM"
    ActionLogout       ActionType = "LOGOUT"
)
```

#### DTOs

```go
// screenconfig/dto.go

package screenconfig

import (
    "encoding/json"
    "time"
)

// ScreenTemplateDTO representa un template de pantalla
type ScreenTemplateDTO struct {
    ID          string          `json:"id"`
    Pattern     Pattern         `json:"pattern"`
    Name        string          `json:"name"`
    Description string          `json:"description,omitempty"`
    Version     int             `json:"version"`
    Definition  json.RawMessage `json:"definition"`
    IsActive    bool            `json:"is_active"`
    CreatedAt   time.Time       `json:"created_at"`
    UpdatedAt   time.Time       `json:"updated_at"`
}

// ScreenInstanceDTO representa una instancia de pantalla
type ScreenInstanceDTO struct {
    ID                 string          `json:"id"`
    ScreenKey          string          `json:"screen_key"`
    TemplateID         string          `json:"template_id"`
    Name               string          `json:"name"`
    Description        string          `json:"description,omitempty"`
    SlotData           json.RawMessage `json:"slot_data"`
    Actions            json.RawMessage `json:"actions"`
    DataEndpoint       string          `json:"data_endpoint,omitempty"`
    DataConfig         json.RawMessage `json:"data_config,omitempty"`
    Scope              string          `json:"scope"`
    RequiredPermission string          `json:"required_permission,omitempty"`
    IsActive           bool            `json:"is_active"`
    CreatedAt          time.Time       `json:"created_at"`
    UpdatedAt          time.Time       `json:"updated_at"`
}

// CombinedScreenDTO es lo que recibe el frontend (template + instancia combinados)
type CombinedScreenDTO struct {
    ScreenID         string          `json:"screenId"`
    ScreenKey        string          `json:"screenKey"`
    ScreenName       string          `json:"screenName"`
    Pattern          Pattern         `json:"pattern"`
    Version          int             `json:"version"`
    Template         json.RawMessage `json:"template"`
    DataEndpoint     string          `json:"dataEndpoint,omitempty"`
    DataConfig       json.RawMessage `json:"dataConfig,omitempty"`
    Actions          json.RawMessage `json:"actions"`
    UserPreferences  json.RawMessage `json:"userPreferences,omitempty"`
    UpdatedAt        time.Time       `json:"updatedAt"`
}

// ResourceScreenDTO mapea un recurso a sus pantallas
type ResourceScreenDTO struct {
    ResourceID       string `json:"resource_id"`
    ResourceKey      string `json:"resource_key"`
    ScreenKey        string `json:"screen_key"`
    ScreenType       string `json:"screen_type"`
    IsDefault        bool   `json:"is_default"`
}

// ActionDefinitionDTO representa una accion de pantalla
type ActionDefinitionDTO struct {
    ID            string          `json:"id"`
    Trigger       string          `json:"trigger"`
    TriggerSlotID string          `json:"triggerSlotId,omitempty"`
    Type          ActionType      `json:"type"`
    Config        json.RawMessage `json:"config"`
}
```

#### Validacion

```go
// screenconfig/validation.go

package screenconfig

import "fmt"

// ValidatePattern verifica si una cadena de pattern es valida
func ValidatePattern(p string) error {
    switch Pattern(p) {
    case PatternLogin, PatternForm, PatternList, PatternDashboard,
         PatternSettings, PatternDetail, PatternSearch, PatternProfile,
         PatternModal, PatternNotification, PatternOnboarding, PatternEmptyState:
        return nil
    default:
        return fmt.Errorf("pattern invalido: %s", p)
    }
}

// ValidateTemplateDefinition valida la estructura JSON de una definicion de template
func ValidateTemplateDefinition(definition []byte) error {
    // Validar campos requeridos: el array de zones debe existir
    // Validar que cada zone tenga id y type
    // Validar que cada slot tenga id y controlType
    // Retornar errores detallados para estructuras invalidas
}

// ValidateActionType verifica si un tipo de accion es valido
func ValidateActionType(a string) error {
    switch ActionType(a) {
    case ActionNavigate, ActionNavigateBack, ActionAPICall,
         ActionSubmitForm, ActionRefresh, ActionConfirm, ActionLogout:
        return nil
    default:
        return fmt.Errorf("tipo de accion invalido: %s", a)
    }
}
```

### 2. Nuevos Permisos

Ubicacion: `common/types/enum/permission.go`

Agregar nuevos permisos para la gestion de configuracion de pantallas:

```go
// Permisos de configuracion de pantalla (solo admin)
PermissionScreenTemplatesRead   Permission = "screen_templates:read"
PermissionScreenTemplatesCreate Permission = "screen_templates:create"
PermissionScreenTemplatesUpdate Permission = "screen_templates:update"
PermissionScreenTemplatesDelete Permission = "screen_templates:delete"
PermissionScreenInstancesRead   Permission = "screen_instances:read"
PermissionScreenInstancesCreate Permission = "screen_instances:create"
PermissionScreenInstancesUpdate Permission = "screen_instances:update"
PermissionScreenInstancesDelete Permission = "screen_instances:delete"

// Permiso de servir pantallas (solo lectura para mobile)
PermissionScreensRead Permission = "screens:read"
```

### 3. Actualizacion de Version

Actualizar la version del modulo para incluir el nuevo paquete `screenconfig`.

## Criterios de Aceptacion

- [ ] El modulo `screenconfig` compila y pasa las pruebas
- [ ] Los DTOs serializan/deserializan correctamente con JSON
- [ ] La validacion de Pattern y ActionType funciona
- [ ] La validacion de definicion de template detecta errores estructurales
- [ ] Nuevos permisos registrados en el enum
- [ ] Version del modulo actualizada
