# 6. Ejemplo Paso a Paso: Crear una Nueva Pantalla

Vamos a crear una pantalla de **"Lista de Cursos"** (`courses-list`) usando el template existente `list-basic-v1`.

---

## Paso 1: Identificar el Template

Revisar los templates existentes. Si alguno sirve, reutilizarlo. Si no, crear uno nuevo.

**Templates disponibles (seeds):**

| ID | Pattern | Nombre | Descripcion |
|----|---------|--------|-------------|
| `a0...01` | login | login-basic-v1 | Login con branding y social |
| `a0...02` | dashboard | dashboard-basic-v1 | Dashboard con KPIs y actividad |
| `a0...03` | list | list-basic-v1 | Lista con busqueda, filtros, empty state |
| `a0...04` | detail | detail-basic-v1 | Detalle con header, info, acciones |
| `a0...05` | settings | settings-basic-v1 | Settings con secciones y toggle |

Para nuestra lista de cursos, usamos `list-basic-v1` (id: `a0000000-0000-0000-0000-000000000003`).

### Estructura del template list-basic-v1

```
zones:
├── header (container)
│   └── page_title (label, bind:"slot:page_title")
│
├── search_zone (container)
│   └── search_bar (search-bar, bind:"slot:search_placeholder")
│
├── filter_zone (container, distribution:flow-row)
│   ├── filter_all (chip, bind:"slot:filter_all_label")
│   ├── filter_ready (chip, bind:"slot:filter_ready_label")
│   └── filter_processing (chip, bind:"slot:filter_processing_label")
│
├── empty_state (container, condition:"data.isEmpty")
│   ├── empty_icon (icon, bind:"slot:empty_icon")
│   ├── empty_title (label, bind:"slot:empty_state_title")
│   ├── empty_desc (label, bind:"slot:empty_state_description")
│   └── empty_action (filled-button, bind:"slot:empty_action_label")
│
└── list_content (simple-list, condition:"!data.isEmpty")
    └── itemLayout:
        ├── item_icon (icon, field:"file_type_icon")
        ├── item_title (label, field:"title")
        ├── item_subtitle (label, field:"subtitle")
        └── item_status (chip, field:"status")
```

---

## Paso 2: Crear la Instancia en la Base de Datos

### Opcion A: Via SQL (migracion/seed)

```sql
INSERT INTO ui_config.screen_instances
(id, screen_key, template_id, name, description, slot_data, actions, data_endpoint, data_config, scope, required_permission)
VALUES (
    gen_random_uuid(),
    'courses-list',                                    -- screen_key unico
    'a0000000-0000-0000-0000-000000000003',            -- template list-basic-v1
    'Lista de Cursos',
    'Pantalla de cursos disponibles',

    -- slot_data: valores para cada bind:"slot:xxx" del template
    '{
        "page_title": "My Courses",
        "search_placeholder": "Search courses...",
        "filter_all_label": "All",
        "filter_ready_label": "Active",
        "filter_processing_label": "Completed",
        "empty_icon": "school",
        "empty_state_title": "No courses yet",
        "empty_state_description": "You are not enrolled in any course",
        "empty_action_label": "Browse Courses"
    }'::jsonb,

    -- actions: que pasa cuando el usuario interactua
    '[
        {
            "id": "item-click",
            "trigger": "item_click",
            "type": "NAVIGATE",
            "config": {
                "screenKey": "course-detail",
                "params": {"id": "{item.id}"}
            }
        },
        {
            "id": "pull-refresh",
            "trigger": "pull_refresh",
            "type": "REFRESH"
        },
        {
            "id": "browse-courses",
            "trigger": "button_click",
            "triggerSlotId": "empty_action",
            "type": "NAVIGATE",
            "config": {"screenKey": "courses-catalog"}
        }
    ]'::jsonb,

    -- data_endpoint: de donde cargar la lista
    '/v1/courses',

    -- data_config: como cargar y mapear datos
    '{
        "method": "GET",
        "defaultParams": {"sort": "name", "order": "asc"},
        "pagination": {
            "pageSize": 20,
            "limitParam": "limit",
            "offsetParam": "offset"
        },
        "fieldMapping": {
            "title": "name",
            "subtitle": "teacher_name",
            "status": "enrollment_status",
            "file_type_icon": "category_icon",
            "id": "id"
        }
    }'::jsonb,

    'unit',              -- scope
    'courses:read'       -- required_permission
);
```

### Opcion B: Via API

```bash
curl -X POST http://localhost:8081/v1/screen-config/instances \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "screen_key": "courses-list",
    "template_id": "a0000000-0000-0000-0000-000000000003",
    "name": "Lista de Cursos",
    "slot_data": {
        "page_title": "My Courses",
        "search_placeholder": "Search courses...",
        "filter_all_label": "All",
        "filter_ready_label": "Active",
        "filter_processing_label": "Completed",
        "empty_icon": "school",
        "empty_state_title": "No courses yet",
        "empty_state_description": "You are not enrolled in any course",
        "empty_action_label": "Browse Courses"
    },
    "actions": [...],
    "data_endpoint": "/v1/courses",
    "data_config": {...},
    "scope": "unit",
    "required_permission": "courses:read"
}'
```

---

## Paso 3: (Opcional) Vincular a Recurso RBAC

Si existe un recurso "courses" en el sistema RBAC:

```sql
INSERT INTO ui_config.resource_screens
(resource_id, resource_key, screen_key, screen_type, is_default)
VALUES (
    '20000000-0000-0000-0000-000000000040',  -- ID del recurso 'courses'
    'courses',
    'courses-list',
    'list',
    true
);
```

---

## Paso 4: Verificar el Resolve

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/v1/screen-config/resolve/key/courses-list | jq .
```

Debe retornar el `CombinedScreenDTO` con el template completo + slotData + actions.

---

## Paso 5: Frontend - Navegar a la Pantalla

En el frontend, solo necesitas navegar con el `screenKey`. El sistema de Dynamic UI se encarga de todo.

### Si es una pantalla accesible por navegacion (ej: desde dashboard)

Ya esta! La accion `NAVIGATE` del dashboard apunta al `screenKey`:

```json
{
    "id": "go-to-courses",
    "trigger": "button_click",
    "triggerSlotId": "courses_btn",
    "type": "NAVIGATE",
    "config": {"screenKey": "courses-list"}
}
```

El `DynamicScreen` Composable carga automaticamente la pantalla:

```kotlin
DynamicScreen(
    screenKey = "courses-list",
    viewModel = viewModel,
    onNavigate = onNavigate,
)
```

### Si necesita un screen Composable dedicado (como el Dashboard)

Solo necesario si la pantalla requiere datos del contexto de auth (placeholders):

```kotlin
@Composable
fun DynamicCoursesScreen(
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<DynamicScreenViewModel>()

    DynamicScreen(
        screenKey = "courses-list",
        viewModel = viewModel,
        onNavigate = onNavigate,
        modifier = modifier,
    )
}
```

---

## Paso 6: El Endpoint de Datos

El `data_endpoint: "/v1/courses"` debe existir en la API y retornar un JSON con la lista de items.

El frontend espera un formato compatible con `DataPage`:

```json
{
    "items": [
        {
            "id": "course-1",
            "name": "Mathematics 101",
            "teacher_name": "Prof. Garcia",
            "enrollment_status": "active",
            "category_icon": "calculate"
        },
        {
            "id": "course-2",
            "name": "English Literature",
            "teacher_name": "Prof. Smith",
            "enrollment_status": "completed",
            "category_icon": "book"
        }
    ],
    "total": 15,
    "hasMore": true
}
```

El `fieldMapping` en `data_config` mapea los campos del JSON a los `field` del template:

```
Template field     →  data_config.fieldMapping  →  JSON del API
─────────────────────────────────────────────────────────────────
slot.field="title"       "title": "name"         item.name
slot.field="subtitle"    "subtitle": "teacher_name"  item.teacher_name
slot.field="status"      "status": "enrollment_status"  item.enrollment_status
```

---

## Resumen: Que Necesitas Para una Nueva Pantalla

| Paso | Donde | Que hacer |
|------|-------|-----------|
| 1 | DB / API | Elegir o crear un template |
| 2 | DB / API | Crear screen_instance con slot_data, actions, data_endpoint |
| 3 | DB / API | (Opcional) Vincular a recurso RBAC |
| 4 | Backend | Implementar el data_endpoint si no existe |
| 5 | Frontend | Navegar con screenKey (nada mas que hacer!) |

El frontend NO necesita cambios de codigo para pantallas nuevas si:
- El template ya existe
- El pattern ya tiene renderer
- No se necesitan placeholders de auth

---

## Crear un Template Nuevo

Si ningun template existente sirve, crear uno. Ejemplo: template de "Perfil de Usuario":

```sql
INSERT INTO ui_config.screen_templates
(id, pattern, name, description, version, definition)
VALUES (
    gen_random_uuid(),
    'profile',
    'profile-standard-v1',
    'Perfil de usuario con avatar, info y estadisticas',
    1,
    '{
        "navigation": {
            "topBar": {"title": "slot:page_title", "showBack": true}
        },
        "zones": [
            {
                "id": "avatar_zone",
                "type": "container",
                "distribution": "stacked",
                "slots": [
                    {"id": "user_avatar", "controlType": "avatar", "field": "user.initials"},
                    {"id": "user_name", "controlType": "label", "style": "headline-medium", "field": "user.full_name"},
                    {"id": "user_email", "controlType": "label", "style": "body", "field": "user.email"}
                ]
            },
            {
                "id": "stats_zone",
                "type": "metric-grid",
                "distribution": "grid",
                "slots": [
                    {"id": "stat_courses", "controlType": "metric-card", "bind": "slot:courses_label", "field": "stats.courses"},
                    {"id": "stat_score", "controlType": "metric-card", "bind": "slot:score_label", "field": "stats.avg_score"}
                ]
            },
            {
                "id": "actions_zone",
                "type": "action-group",
                "slots": [
                    {"id": "edit_btn", "controlType": "outlined-button", "bind": "slot:edit_label"},
                    {"id": "logout_btn", "controlType": "filled-button", "bind": "slot:logout_label", "style": "error"}
                ]
            }
        ]
    }'::jsonb
);
```

Luego crear la instancia que usa este template, con su `slot_data` y `actions`.

---

## Checklist

- [ ] Template existe (o creado nuevo)
- [ ] screen_instance creada con screen_key unico
- [ ] slot_data contiene valor para cada `bind: "slot:xxx"` del template
- [ ] actions vinculadas a los slots correctos via `triggerSlotId`
- [ ] data_endpoint implementado en el API (si aplica)
- [ ] data_config.fieldMapping mapea correctamente campos del API a `field` del template
- [ ] Verificado con `curl` al endpoint resolve
- [ ] Frontend navega correctamente al screenKey
