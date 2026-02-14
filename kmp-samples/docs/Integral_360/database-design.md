# Diseño de Base de Datos - Schema de Configuración de UI

## Vista General

Todos los datos de configuración de pantallas viven en un schema dedicado de PostgreSQL `ui_config`, separado del schema `public` que contiene los datos de dominio (usuarios, colegios, materiales, etc.).

## Schema: `ui_config`

### Diagrama de Entidad-Relación

```
public.resources ──────────── ui_config.resource_screens
       │                              │
       │                              │
       │                     ui_config.screen_instances
       │                              │
       │                     ui_config.screen_templates
       │
       │                     ui_config.screen_data_bindings
       │
       │                     ui_config.screen_user_preferences
```

---

## Tablas

### 1. `ui_config.screen_templates`

Definiciones de layout reutilizables. Un template define el patrón estructural (lista, formulario, detalle, etc.) sin datos específicos.

```sql
CREATE TABLE ui_config.screen_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern VARCHAR(50) NOT NULL,
    -- 'login', 'form', 'list', 'dashboard', 'settings',
    -- 'detail', 'search', 'profile', 'modal', 'notification',
    -- 'onboarding', 'empty-state'
    name VARCHAR(200) NOT NULL,
    description TEXT,
    version INT NOT NULL DEFAULT 1,
    definition JSONB NOT NULL,
    -- JSON completo del template: zones, slots, controls, overrides por plataforma
    is_active BOOLEAN DEFAULT true,
    created_by UUID REFERENCES public.users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(name, version)
);

CREATE INDEX idx_screen_templates_pattern ON ui_config.screen_templates(pattern);
CREATE INDEX idx_screen_templates_active ON ui_config.screen_templates(is_active);
CREATE INDEX idx_screen_templates_definition ON ui_config.screen_templates USING GIN (definition);
```

**Estructura JSONB de `definition`** (sigue el template-json-schema de kmp-samples/docs):
```json
{
  "navigation": {
    "topBar": {
      "title": "slot:page_title",
      "showBack": false,
      "actions": []
    }
  },
  "zones": [
    {
      "id": "main_content",
      "type": "container",
      "distribution": "stacked",
      "slots": [
        {
          "id": "title_field",
          "controlType": "label",
          "style": "headline",
          "bind": "slot:title"
        },
        {
          "id": "description_field",
          "controlType": "label",
          "style": "body",
          "bind": "slot:description"
        }
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "distribution": "side-by-side",
      "weights": [0.4, 0.6]
    },
    "web": {
      "breakpoints": {
        "compact": { "maxWidth": 600 },
        "medium": { "maxWidth": 840 },
        "expanded": { "minWidth": 841 }
      }
    }
  }
}
```

---

### 2. `ui_config.screen_instances`

Configuraciones concretas de pantalla que combinan un template con bindings de datos específicos.

```sql
CREATE TABLE ui_config.screen_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_key VARCHAR(100) NOT NULL UNIQUE,
    -- Identificador único: 'materials-list', 'material-detail', 'dashboard-teacher'
    template_id UUID NOT NULL REFERENCES ui_config.screen_templates(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    slot_data JSONB NOT NULL DEFAULT '{}',
    -- Etiquetas estáticas, placeholders, valores por defecto para slots del template
    actions JSONB NOT NULL DEFAULT '[]',
    -- Definiciones de acciones para esta pantalla
    data_endpoint VARCHAR(500),
    -- Endpoint de API para obtener datos dinámicos (ej: '/v1/materials')
    data_config JSONB DEFAULT '{}',
    -- Configuración del endpoint: método, params, paginación, etc.
    scope VARCHAR(20) DEFAULT 'school',
    -- 'system', 'school', 'unit'
    required_permission VARCHAR(100),
    -- Permiso necesario para acceder a esta pantalla (ej: 'materials:read')
    is_active BOOLEAN DEFAULT true,
    created_by UUID REFERENCES public.users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_screen_instances_template ON ui_config.screen_instances(template_id);
CREATE INDEX idx_screen_instances_key ON ui_config.screen_instances(screen_key);
CREATE INDEX idx_screen_instances_scope ON ui_config.screen_instances(scope);
CREATE INDEX idx_screen_instances_active ON ui_config.screen_instances(is_active);
```

**Estructura JSONB de `slot_data`**:
```json
{
  "page_title": "Materiales Educativos",
  "search_placeholder": "Buscar por título o materia...",
  "empty_state_title": "Aún no hay materiales",
  "empty_state_description": "Sube tu primer material para comenzar",
  "empty_state_action_label": "Subir Material"
}
```

**Estructura JSONB de `actions`**:
```json
[
  {
    "id": "navigate-to-detail",
    "trigger": "item_click",
    "type": "NAVIGATE",
    "config": {
      "target": "material-detail",
      "params": { "id": "{item.id}" }
    }
  },
  {
    "id": "create-material",
    "trigger": "fab_click",
    "triggerSlotId": "create_btn",
    "type": "NAVIGATE",
    "config": {
      "target": "material-create"
    }
  },
  {
    "id": "refresh-list",
    "trigger": "pull_refresh",
    "type": "REFRESH"
  }
]
```

**Estructura JSONB de `data_config`**:
```json
{
  "method": "GET",
  "pagination": {
    "type": "offset",
    "pageSize": 20,
    "pageParam": "offset",
    "limitParam": "limit"
  },
  "defaultParams": {
    "sort": "created_at",
    "order": "desc"
  },
  "fieldMapping": {
    "title": "title",
    "subtitle": "subject",
    "description": "description",
    "status": "status",
    "date": "created_at",
    "icon": "file_type"
  }
}
```

---

### 3. `ui_config.resource_screens`

Mapea recursos (del sistema RBAC) a instancias de pantalla.

```sql
CREATE TABLE ui_config.resource_screens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID NOT NULL REFERENCES public.resources(id),
    screen_instance_id UUID NOT NULL REFERENCES ui_config.screen_instances(id),
    screen_type VARCHAR(50) NOT NULL,
    -- 'list', 'detail', 'create', 'edit', 'dashboard', 'settings'
    is_default BOOLEAN DEFAULT false,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(resource_id, screen_type)
);

CREATE INDEX idx_resource_screens_resource ON ui_config.resource_screens(resource_id);
CREATE INDEX idx_resource_screens_instance ON ui_config.resource_screens(screen_instance_id);
```

---

### 4. `ui_config.screen_user_preferences`

Almacena preferencias por usuario para pantallas configurables (elecciones de tema, orden de columnas, etc.).

```sql
CREATE TABLE ui_config.screen_user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_instance_id UUID NOT NULL REFERENCES ui_config.screen_instances(id),
    user_id UUID NOT NULL REFERENCES public.users(id),
    preferences JSONB NOT NULL DEFAULT '{}',
    -- Overrides específicos del usuario: orden de clasificación, columnas visibles, etc.
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(screen_instance_id, user_id)
);

CREATE INDEX idx_screen_user_prefs_user ON ui_config.screen_user_preferences(user_id);
```

---

## Datos Semilla de Ejemplo

```sql
-- 1. Crear un template de "lista"
INSERT INTO ui_config.screen_templates (id, pattern, name, version, definition) VALUES
('t-list-basic', 'list', 'Template Lista Básica', 1, '{
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
      "id": "empty_state",
      "type": "container",
      "condition": "data.isEmpty",
      "slots": [
        { "id": "empty_icon", "controlType": "icon", "bind": "slot:empty_icon" },
        { "id": "empty_title", "controlType": "label", "style": "headline", "bind": "slot:empty_state_title" },
        { "id": "empty_desc", "controlType": "label", "style": "body", "bind": "slot:empty_state_description" }
      ]
    },
    {
      "id": "list_content",
      "type": "simple-list",
      "condition": "!data.isEmpty",
      "itemLayout": {
        "slots": [
          { "id": "item_title", "controlType": "label", "style": "headline-small", "bind": "item:title" },
          { "id": "item_subtitle", "controlType": "label", "style": "body-small", "bind": "item:subtitle" },
          { "id": "item_trailing", "controlType": "icon", "bind": "item:status_icon" }
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
}'::jsonb);

-- 2. Crear instancia de pantalla para "lista de materiales"
INSERT INTO ui_config.screen_instances (id, screen_key, template_id, name, slot_data, actions, data_endpoint, data_config, required_permission) VALUES
('si-materials-list', 'materials-list', 't-list-basic', 'Lista de Materiales',
'{
  "page_title": "Materiales Educativos",
  "search_placeholder": "Buscar materiales...",
  "empty_icon": "folder_open",
  "empty_state_title": "No hay materiales disponibles",
  "empty_state_description": "Los materiales aparecerán aquí una vez subidos"
}'::jsonb,
'[
  { "id": "item-click", "trigger": "item_click", "type": "NAVIGATE", "config": { "target": "material-detail", "params": { "id": "{item.id}" } } },
  { "id": "pull-refresh", "trigger": "pull_refresh", "type": "REFRESH" }
]'::jsonb,
'/v1/materials',
'{
  "method": "GET",
  "pagination": { "type": "offset", "pageSize": 20 },
  "fieldMapping": { "title": "title", "subtitle": "subject", "status_icon": "status" }
}'::jsonb,
'materials:read');

-- 3. Vincular al recurso
INSERT INTO ui_config.resource_screens (resource_id, screen_instance_id, screen_type, is_default) VALUES
((SELECT id FROM public.resources WHERE key = 'materials'), 'si-materials-list', 'list', true);
```

---

## Respuesta API: Definición Combinada de Pantalla

Cuando el frontend solicita una pantalla, la API combina template + instancia + preferencias del usuario:

```json
{
  "screenId": "materials-list",
  "screenName": "Lista de Materiales",
  "pattern": "list",
  "version": 1,
  "template": {
    "navigation": { "topBar": { "title": "Materiales Educativos" } },
    "zones": [
      {
        "id": "search_zone",
        "type": "container",
        "slots": [
          { "id": "search_bar", "controlType": "search-bar", "placeholder": "Buscar materiales..." }
        ]
      },
      {
        "id": "list_content",
        "type": "simple-list",
        "itemLayout": {
          "slots": [
            { "id": "item_title", "controlType": "label", "style": "headline-small", "field": "title" },
            { "id": "item_subtitle", "controlType": "label", "style": "body-small", "field": "subject" },
            { "id": "item_trailing", "controlType": "icon", "field": "status" }
          ]
        }
      }
    ],
    "platformOverrides": { }
  },
  "dataEndpoint": "/v1/materials",
  "dataConfig": {
    "method": "GET",
    "pagination": { "type": "offset", "pageSize": 20 },
    "fieldMapping": { "title": "title", "subtitle": "subject", "status_icon": "status" }
  },
  "actions": [
    { "id": "item-click", "trigger": "item_click", "type": "NAVIGATE", "config": { "target": "material-detail" } },
    { "id": "pull-refresh", "trigger": "pull_refresh", "type": "REFRESH" }
  ],
  "userPreferences": {}
}
```

---

## Estrategia de Migración

Migraciones agregadas en `edugo-infrastructure/postgres/migrations/`:

```
0018_create_ui_config_schema.up.sql
0018_create_ui_config_schema.down.sql
0019_create_screen_templates.up.sql
0019_create_screen_templates.down.sql
0020_create_screen_instances.up.sql
0020_create_screen_instances.down.sql
0021_create_resource_screens.up.sql
0021_create_resource_screens.down.sql
0022_create_screen_user_preferences.up.sql
0022_create_screen_user_preferences.down.sql
0023_seed_screen_templates.up.sql        -- Templates base para cada patrón
0023_seed_screen_templates.down.sql
0024_seed_screen_instances.up.sql        -- Configs iniciales para pantallas principales
0024_seed_screen_instances.down.sql
```
