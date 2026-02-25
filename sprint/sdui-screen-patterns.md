# SDUI Screen Patterns - Guia de Estandarizacion

## Arquitectura

```
BD (screen_instances) --> IAM Platform API --> RemoteScreenLoader --> DynamicScreenViewModel --> PatternRouter --> Renderer
```

### Endpoint: `GET /api/v1/screen-config/resolve/key/{screen_key}`

Retorna `ScreenDefinition` con: screen_key, screen_name, pattern, template (zones/slots), slot_data, actions, data_endpoint, data_config.

### Data Loading: `RemoteDataLoader`

Endpoints con prefijo de API:
- `admin:/api/v1/...` → Admin API
- `iam:/api/v1/...` → IAM Platform
- `/api/v1/...` → Mobile API (default)

---

## Como crear un Screen Instance

### 1. SQL Insert

```sql
INSERT INTO ui_config.screen_instances (
  id, screen_key, template_id, name, description,
  slot_data, actions, data_endpoint, data_config,
  scope, required_permission, handler_key
) VALUES (
  'UUID', 'mi-screen-key',
  'a0000000-0000-0000-0000-000000000003',  -- list-basic-v1
  'Mi Pantalla', 'Descripcion',
  '{"page_title": "Titulo", "search_placeholder": "Buscar..."}'::jsonb,
  '[{"id": "item-click", "trigger": "item_click", "type": "NAVIGATE", "config": {"target": "mi-detail", "params": {"id": "{item.id}"}}}]'::jsonb,
  'admin:/api/v1/mi-endpoint',
  '{"method": "GET", "pagination": {"type": "offset", "pageSize": 20}}'::jsonb,
  'school', 'mi_recurso:read', NULL
);
```

### 2. Resource-Screen Mapping

```sql
INSERT INTO ui_config.resource_screens (id, resource_key, screen_key, screen_type, is_default)
VALUES ('UUID', 'mi_recurso', 'mi-screen-key', 'list', TRUE);
```

### 3. Templates disponibles

| template_key | template_id | Uso |
|---|---|---|
| login-v1 | a0000000-...-000001 | Login screen |
| dashboard-basic-v1 | a0000000-...-000002 | Dashboard con KPIs |
| list-basic-v1 | a0000000-...-000003 | Lista con busqueda/filtros |
| detail-basic-v1 | a0000000-...-000004 | Detalle de item |
| settings-v1 | a0000000-...-000005 | Configuracion |
| form-basic-v1 | a0000000-...-000006 | Formulario CRUD |

---

## Como crear un CRUD completo

### Paso 1: Recurso y permisos en BD

```sql
-- Recurso
INSERT INTO iam.resources (id, key, display_name, icon, scope, parent_id, sort_order, is_menu_visible)
VALUES ('UUID', 'mi_recurso', 'Mi Recurso', 'icon', 'school', 'parent_uuid', 3, TRUE);

-- Permisos CRUD
INSERT INTO iam.permissions (id, resource_id, name, display_name, action, scope)
VALUES
  ('UUID', 'resource_uuid', 'mi_recurso:create', 'Crear', 'create', 'school'),
  ('UUID', 'resource_uuid', 'mi_recurso:read', 'Ver', 'read', 'school'),
  ('UUID', 'resource_uuid', 'mi_recurso:update', 'Editar', 'update', 'school'),
  ('UUID', 'resource_uuid', 'mi_recurso:delete', 'Eliminar', 'delete', 'school');

-- Asignar a roles
INSERT INTO iam.role_permissions (id, role_id, permission_id) VALUES ...;
```

### Paso 2: Screen Instance para Lista

- Template: `list-basic-v1`
- Actions: `item_click` (NAVIGATE), `fab_click` (NAVIGATE to form), `pull_refresh` (REFRESH)
- slot_data: page_title, search_placeholder, empty_state_title, columns

### Paso 3: Screen Instance para Form

- Template: `form-basic-v1`
- Actions: `submit_btn` (SUBMIT_FORM), `cancel_btn` (NAVIGATE_BACK)
- slot_data: page_title, edit_title, submit_label, cancel_label, fields[]

### Paso 4: Resource-Screen Mappings

```sql
INSERT INTO ui_config.resource_screens VALUES
  ('UUID', 'mi_recurso', 'mi-recurso-list', 'list', TRUE),
  ('UUID', 'mi_recurso', 'mi-recurso-form', 'form', FALSE);
```

---

## Platform Overrides

Los templates pueden incluir `platformOverrides` en el JSON:

```json
{
  "platformOverrides": {
    "desktop": {
      "zones": { "kpis": { "distribution": "side-by-side" } }
    },
    "mobile": {
      "zones": { "kpis": { "distribution": "stacked" } }
    }
  }
}
```

Breakpoints:
- Mobile: < 600dp
- Tablet: 600-840dp
- Desktop: >= 840dp

---

## Checklist por pantalla

- [ ] Screen instance creada en `ui_config.screen_instances`
- [ ] Resource-screen mapping creado en `ui_config.resource_screens`
- [ ] Permisos asignados a roles en `iam.role_permissions`
- [ ] data_endpoint incluye `/api` prefix
- [ ] data_config tiene pagination si es lista
- [ ] slot_data tiene labels en espanol
- [ ] actions definidas (item_click, fab_click, pull_refresh)
- [ ] Verificar con `GET /api/v1/screen-config/resolve/key/{key}`
- [ ] Verificar en desktop app que carga sin errores
- [ ] Seed files actualizados en `edugo-infrastructure/postgres/seeds/production/`

---

## Pantallas implementadas

| screen_key | pattern | data_endpoint | Estado |
|---|---|---|---|
| dashboard-superadmin | dashboard | admin:/api/v1/stats/global | OK |
| dashboard-schooladmin | dashboard | /api/v1/stats/global | OK |
| dashboard-teacher | dashboard | /api/v1/stats/global | OK |
| dashboard-student | dashboard | /api/v1/stats/student | OK |
| dashboard-guardian | dashboard | /api/v1/guardians/me/stats | OK |
| users-list | list | admin:/api/v1/users | OK |
| schools-list | list | admin:/api/v1/schools | OK |
| roles-list | list | admin:/api/v1/roles | OK |
| permissions-list | list | admin:/api/v1/permissions | OK |
| units-list | list | admin:/api/v1/schools/{schoolId}/units | OK |
| memberships-list | list | admin:/api/v1/memberships | OK |
| materials-list | list | /api/v1/materials | OK |
| material-detail | detail | /api/v1/materials/{id} | OK |
| assessments-list | list | /api/v1/assessments | OK |
| progress-dashboard | dashboard | /api/v1/stats/progress | OK |
| stats-dashboard | dashboard | admin:/api/v1/stats/global | OK |
| subjects-list | list | admin:/api/v1/subjects | OK |
| subjects-form | form | admin:/api/v1/subjects | OK |
| app-login | login | - | OK |
| app-settings | settings | - | OK |
