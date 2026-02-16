# 2. Base de Datos

Schema: `ui_config`

## Tabla 1: `screen_templates`

Define la estructura visual reutilizable de una pantalla.

```sql
CREATE TABLE ui_config.screen_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern VARCHAR(50) NOT NULL,         -- login, dashboard, list, detail, settings, form...
    name VARCHAR(200) NOT NULL,           -- Nombre unico del template (ej: "login-basic-v1")
    description TEXT,
    version INT NOT NULL DEFAULT 1,       -- Auto-incrementa al modificar definition
    definition JSONB NOT NULL,            -- Estructura completa (zones/slots)
    is_active BOOLEAN DEFAULT true,
    created_by UUID,                      -- FK → public.users
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(name, version)
);
```

### Estructura de `definition` (JSONB)

```json
{
  "navigation": {
    "topBar": {
      "title": "slot:page_title",
      "showBack": false
    }
  },
  "zones": [
    {
      "id": "zone_header",
      "type": "container",
      "distribution": "stacked",
      "slots": [
        {
          "id": "greeting",
          "controlType": "label",
          "style": "headline-large",
          "bind": "slot:greeting_text"
        }
      ],
      "zones": []
    },
    {
      "id": "kpi_grid",
      "type": "metric-grid",
      "distribution": "grid",
      "slots": [
        {
          "id": "kpi_1",
          "controlType": "metric-card",
          "bind": "slot:kpi_students_label",
          "field": "total_students"
        }
      ]
    },
    {
      "id": "data_list",
      "type": "simple-list",
      "itemLayout": {
        "slots": [
          {"id": "item_title", "controlType": "label", "field": "title"},
          {"id": "item_status", "controlType": "chip", "field": "status"}
        ]
      }
    }
  ],
  "platformOverrides": {
    "desktop": {"distribution": "side-by-side"}
  }
}
```

#### Tipos de Zona (`type`)

| Tipo | Descripcion |
|------|-------------|
| `container` | Contenedor generico de slots |
| `form-section` | Seccion de formulario (con divider entre secciones) |
| `metric-grid` | Grilla de tarjetas de metricas (2 columnas) |
| `simple-list` | Lista plana con `itemLayout` |
| `grouped-list` | Lista agrupada |
| `action-group` | Grupo de botones de accion |
| `card-list` | Lista de tarjetas |

#### Distribuciones (`distribution`)

| Distribucion | Layout |
|-------------|--------|
| `stacked` | Vertical (Column, default) |
| `side-by-side` | Horizontal (Row) |
| `grid` | Grilla de 2 columnas |
| `flow-row` | FlowRow (chips, tags) |

#### Tipos de Control (`controlType`)

| Control | Descripcion | Usa `label` | Usa `value` |
|---------|-------------|:-----------:|:-----------:|
| `label` | Texto estatico | - | Si |
| `text-input` | Campo de texto | Si | - |
| `email-input` | Campo de email | Si | - |
| `password-input` | Campo de password | Si | - |
| `number-input` | Campo numerico | Si | - |
| `search-bar` | Barra de busqueda | Si | - |
| `filled-button` | Boton primario | - | Si |
| `outlined-button` | Boton secundario | - | Si |
| `text-button` | Boton de texto | - | Si |
| `icon-button` | Boton de icono | - | - |
| `icon` | Icono decorativo | - | - |
| `avatar` | Avatar con iniciales | - | Si |
| `switch` | Toggle on/off | Si | - |
| `checkbox` | Casilla de verificacion | Si | - |
| `list-item` | Item de lista | Si | Si |
| `list-item-navigation` | Item con flecha de navegacion | Si | Si |
| `metric-card` | Tarjeta con label + valor | Si | Si |
| `chip` | Chip/tag seleccionable | - | Si |
| `divider` | Linea separadora | - | - |
| `image` | Imagen | - | - |
| `radio-group` | Grupo de radio buttons | - | Si |
| `select` | Dropdown | - | Si |
| `rating` | Estrellas de calificacion | - | Si |

#### Campos de un Slot

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| `id` | string | Identificador unico del slot en el template |
| `controlType` | string | Tipo de control (ver tabla arriba) |
| `bind` | string? | Binding a slotData: `"slot:key_name"` |
| `field` | string? | Binding a datos dinamicos: `"title"`, `"user.full_name"` |
| `style` | string? | Estilo de texto: `headline-large`, `body`, `caption`, etc. |
| `label` | string? | Texto fijo del label (tiene prioridad sobre bind) |
| `value` | string? | Valor fijo (tiene prioridad sobre bind) |
| `placeholder` | string? | Placeholder para inputs |
| `icon` | string? | Nombre del icono |
| `required` | boolean | Si el campo es obligatorio (default: false) |
| `readOnly` | boolean | Si el campo es de solo lectura (default: false) |
| `width` | string? | Ancho del slot |
| `weight` | float? | Peso para distribucion side-by-side |

---

## Tabla 2: `screen_instances`

Define una pantalla concreta: su contenido, acciones y fuente de datos.

```sql
CREATE TABLE ui_config.screen_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_key VARCHAR(100) NOT NULL UNIQUE,  -- Clave unica (ej: "materials-list")
    template_id UUID NOT NULL,                -- FK → screen_templates
    name VARCHAR(200) NOT NULL,
    description TEXT,
    slot_data JSONB NOT NULL DEFAULT '{}',    -- Valores para los slots del template
    actions JSONB NOT NULL DEFAULT '[]',      -- Array de acciones
    data_endpoint VARCHAR(500),               -- Endpoint para cargar datos
    data_config JSONB DEFAULT '{}',           -- Config de paginacion, params, mapping
    scope VARCHAR(20) DEFAULT 'school',       -- system | school | unit
    required_permission VARCHAR(100),         -- Permiso RBAC requerido (ej: "materials:read")
    is_active BOOLEAN DEFAULT true,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Estructura de `slot_data` (JSONB)

Objeto plano clave-valor. Las claves corresponden a lo que los slots referencian via `bind: "slot:xxx"`.

```json
{
  "page_title": "Settings",
  "greeting_text": "Good morning, {user.firstName}",
  "date_text": "{today_date}",
  "kpi_students_label": "Students",
  "dark_mode_label": "Dark Mode",
  "logout_label": "Sign Out"
}
```

Nota: Los valores pueden contener **placeholders** como `{user.firstName}` que el frontend resuelve con datos del usuario autenticado.

### Estructura de `actions` (JSONB Array)

```json
[
  {
    "id": "submit-login",
    "trigger": "button_click",
    "triggerSlotId": "login_btn",
    "type": "SUBMIT_FORM",
    "config": {
      "endpoint": "/v1/auth/login",
      "method": "POST",
      "fieldMapping": {"email": "email", "password": "password"},
      "onSuccess": {"type": "NAVIGATE", "config": {"target": "dashboard-home"}}
    }
  }
]
```

#### Tipos de Action (`type`)

| Tipo | Descripcion |
|------|-------------|
| `NAVIGATE` | Navegar a otra pantalla. Config: `{"screenKey": "...", "params": {...}}` |
| `NAVIGATE_BACK` | Volver a la pantalla anterior |
| `SUBMIT_FORM` | Enviar formulario. Config: `{"endpoint": "...", "method": "POST", "fieldMapping": {...}}` |
| `API_CALL` | Llamada API generica. Config: `{"endpoint": "...", "method": "GET"}` |
| `REFRESH` | Recargar datos de la pantalla |
| `CONFIRM` | Mostrar dialogo de confirmacion |
| `LOGOUT` | Cerrar sesion |

#### Triggers

| Trigger | Cuando se dispara |
|---------|-------------------|
| `button_click` | Click en un boton (requiere `triggerSlotId`) |
| `item_click` | Click en un item de lista |
| `pull_refresh` | Pull-to-refresh |
| `fab_click` | Click en FAB |
| `swipe` | Swipe en un item |
| `long_press` | Long press en un item |

### Estructura de `data_config` (JSONB)

```json
{
  "method": "GET",
  "defaultParams": {"sort": "created_at", "order": "desc"},
  "pagination": {
    "pageSize": 20,
    "limitParam": "limit",
    "offsetParam": "offset"
  },
  "fieldMapping": {
    "title": "material_title",
    "subtitle": "subject_name"
  }
}
```

---

## Tabla 3: `resource_screens`

Mapea recursos RBAC a pantallas. Permite que el sistema sepa que pantalla mostrar para cada recurso.

```sql
CREATE TABLE ui_config.resource_screens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID NOT NULL,          -- FK → public.resources
    resource_key VARCHAR(100) NOT NULL,  -- Nombre del recurso (ej: "materials")
    screen_key VARCHAR(100) NOT NULL,    -- FK → screen_instances.screen_key
    screen_type VARCHAR(50) NOT NULL,    -- list | detail | create | edit | dashboard | settings
    is_default BOOLEAN DEFAULT false,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(resource_id, screen_type)    -- Un tipo de pantalla por recurso
);
```

Ejemplo: El recurso "materials" tiene:
- `screen_type: "list"` → `screen_key: "materials-list"`
- `screen_type: "detail"` → `screen_key: "material-detail"`

---

## Tabla 4: `screen_user_preferences`

Preferencias personalizadas por usuario para una pantalla.

```sql
CREATE TABLE ui_config.screen_user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_instance_id UUID NOT NULL,   -- FK → screen_instances
    user_id UUID NOT NULL,
    preferences JSONB NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(screen_instance_id, user_id)
);
```

Ejemplo:
```json
{
  "dark_mode": true,
  "theme": "indigo",
  "language": "es",
  "push_enabled": true,
  "compact_view": false
}
```
