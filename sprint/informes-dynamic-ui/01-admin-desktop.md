# Informe Dynamic UI - Usuario Admin (Desktop)

**Fecha:** 2026-02-20
**Usuario:** `admin@edugo.test`
**Plataforma:** Desktop (Kotlin)
**Ambiente:** Staging (Azure Container Apps)
**Rol:** `super_admin`

---

## 1. Datos del Usuario

| Campo | Valor |
|-------|-------|
| ID | `a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| Email | `admin@edugo.test` |
| Nombre | Admin Demo |
| Rol | `super_admin` |
| Role ID | `10000000-0000-0000-0000-000000000001` |

## 2. Permisos del Super Admin (46 permisos)

El `super_admin` tiene acceso completo al sistema. Permisos agrupados por dominio:

| Dominio | Permisos |
|---------|----------|
| **assessments** | attempt, create, delete, grade, publish, read, update, view_results |
| **materials** | create, delete, download, publish, read, update |
| **permissions_mgmt** | read, update |
| **progress** | read, read:own, update |
| **schools** | create, delete, manage, read, update |
| **screen_instances** | create, delete, read, update |
| **screen_templates** | create, delete, read, update |
| **screens** | read |
| **stats** | global, school, unit |
| **units** | create, delete, read, update |
| **users** | create, delete, read, read:own, update, update:own |

## 3. Endpoints Consultados

### APIs de Staging
- **Admin API:** `https://edugo-api-admin.wittyhill-f6d656fb.eastus.azurecontainerapps.io`
- **Mobile API:** `https://edugo-api-mobile.wittyhill-f6d656fb.eastus.azurecontainerapps.io`

### Endpoints utilizados
| Endpoint | API | Status | Descripcion |
|----------|-----|--------|-------------|
| `POST /v1/auth/login` | Admin | OK | Login con active_context RBAC |
| `GET /v1/menu` | Admin | OK (vacio) | Menu filtrado por permisos del usuario |
| `GET /v1/menu/full` | Admin | OK | Menu completo del sistema |
| `GET /v1/screens/navigation?platform=desktop` | Mobile | ERROR | Error de base de datos (MongoDB) |
| `GET /v1/screens/{screenKey}?platform=desktop` | Mobile | OK | Definicion de pantallas individuales |

## 4. Estructura del Menu (Full Menu)

El menu completo del sistema tiene **6 secciones principales** con hijos:

```
Dashboard (dashboard) [icon: dashboard, scope: system]
  -> Screens: dashboard-teacher, dashboard-guardian, dashboard-schooladmin,
              dashboard-student, dashboard-superadmin

Administracion (admin) [icon: settings, scope: system]
  |-- Usuarios (users) [icon: users, scope: school]
  |     -> Screens: users-list, user-create, user-edit, user-detail
  |-- Escuelas (schools) [icon: school, scope: system]
  |     -> Screens: schools-list, school-create, school-edit, school-detail
  |-- Roles (roles) [icon: shield, scope: system]
  |     -> Screens: roles-list, role-detail
  |-- Permisos (permissions_mgmt) [icon: key, scope: system]
        -> Screens: resources-list, permissions-list

Academico (academic) [icon: graduation-cap, scope: school]
  |-- Unidades Academicas (units) [icon: layers, scope: school]
  |     -> Screens: units-list, unit-create, unit-edit, unit-detail
  |-- Miembros (memberships) [icon: user-plus, scope: school]
        -> Screens: memberships-list, membership-add

Contenido (content) [icon: book-open, scope: unit]
  |-- Materiales (materials) [icon: file-text, scope: unit]
  |     -> Screens: materials-list, material-create, material-edit, material-detail
  |-- Evaluaciones (assessments) [icon: clipboard, scope: unit]
        -> Screens: assessments-list, assessment-take, assessment-result, attempts-history

Reportes (reports) [icon: bar-chart, scope: school]
  |-- Progreso (progress) [icon: trending-up, scope: unit]
  |     -> Screens: progress-my, progress-unit-list, progress-student-detail
  |-- Estadisticas (stats) [icon: pie-chart, scope: school]
        -> Sin screens asignados
```

### Menu filtrado (`/v1/menu`) vs Menu full (`/v1/menu/full`)

**Hallazgo importante:** El endpoint `/v1/menu` retorna `{"items":[]}` incluso para el `super_admin`. Esto indica que:
- El filtrado del menu por permisos del usuario NO esta conectando con la tabla `menu_items` en la BD
- O el sistema de filtrado requiere que los `menu_items` tengan `required_permissions` configurados, y como no los tienen, el filtrado los excluye todos
- El endpoint `/v1/menu/full` (que requiere permiso `permissions_mgmt:read`) si retorna el menu completo

**Impacto en KMP Desktop:** El endpoint `/v1/screens/navigation` del mobile API es el que usa el cliente KMP, y este falla con `DATABASE_ERROR`. El cliente caera al **fallback hardcoded** en `MainScreen.kt`.

## 5. Pantallas Disponibles para Super Admin

### 5.1 Login (`app-login`)
| Propiedad | Valor |
|-----------|-------|
| Pattern | `login` |
| Handler | `login` |
| Zonas | 3 (brand, form, social) |

**Estructura Desktop (2 paneles):**
- **Panel izquierdo:** Logo EduGo + nombre + tagline
- **Panel derecho:** Formulario (email, password, remember me, Sign In, Forgot password) + Social login (Google)

**Accion principal:** `SUBMIT_FORM` -> POST `/v1/auth/login` -> Navega a `dashboard-home`

### 5.2 Dashboard Super Admin (`dashboard-superadmin`)
| Propiedad | Valor |
|-----------|-------|
| Pattern | `dashboard` |
| Handler | ninguno (usa handler generico) |
| Data endpoint | `admin:/v1/stats/global` |
| Zonas | 4 |

**Estructura:**
- **Greeting:** "Welcome, {user.firstName}" + fecha actual
- **KPIs (grid):** Total Schools, Total Users, Total Materials, System Health (metric-card)
- **Recent Activity (lista):** Actividad del sistema con iconos y tiempos relativos
- **Quick Actions (flow-row):** "Manage Schools" -> `schools-list`, "View Stats" -> `stats-global`

**Field mapping (data):**
```
total_students -> total_schools (del API)
total_materials -> total_users (del API)
avg_score -> total_materials (del API)
completion_rate -> system_health (del API)
```

### 5.3 Lista de Usuarios (`users-list`)
| Propiedad | Valor |
|-----------|-------|
| Pattern | `list` |
| Data endpoint | `admin:/v1/users` |
| Paginacion | offset, 20 items/pagina |
| Zonas | 4 |

**Estructura:**
- Search bar
- Filtros (chips): All, Active, Inactive
- Empty state (condicional: `data.isEmpty`)
- Lista con layout master-detail (weights: [0.4, 0.6])
  - Cada item: icon, title (full_name), subtitle (email), status chip, created_at

**Acciones:**
- Click item -> Navega a `user-detail` con `{item.id}`
- FAB click -> Navega a `user-create`
- Pull refresh -> Refresh data

### 5.4 Otras Listas (todas patron `list`)

| Screen Key | Data Endpoint | API |
|------------|---------------|-----|
| `schools-list` | `admin:/v1/schools` | Admin |
| `materials-list` | `/v1/materials` | Mobile |
| `assessments-list` | `/v1/materials/{materialId}/assessment` | Mobile |
| `units-list` | `admin:/v1/schools/{schoolId}/units` | Admin |
| `memberships-list` | `admin:/v1/memberships` | Admin |

Todas tienen 4 zonas (search, filters, empty_state, list_content) y 3 acciones (item_click, fab_click, pull_refresh).

### 5.5 Settings (`app-settings`)
| Propiedad | Valor |
|-----------|-------|
| Pattern | `settings` |
| Handler | `settings` |
| Zonas | 6 |

**Secciones:**
1. **User card (panel left):** Avatar, nombre, email, rol (chip)
2. **Appearance:** Dark Mode (switch), Theme Color (navigation)
3. **Notifications:** Push (switch), Email (switch)
4. **Account:** Change Password, Language
5. **About:** Version, Privacy Policy, Terms of Service
6. **Logout:** Boton "Sign Out" con confirmacion

**Preferencias default:**
```json
{
  "language": "en",
  "dark_mode": false,
  "push_enabled": true,
  "email_enabled": true
}
```

## 6. Navegacion en Desktop (KMP)

### Flujo esperado
```
1. App inicia -> MainScreen.kt
2. LaunchedEffect llama screenLoader.loadNavigation()
3. RemoteScreenLoader -> GET /v1/screens/navigation?platform=desktop
4. SI falla (como ahora) -> Usa NavigationDefinition fallback:
   - Dashboard (dashboard)
   - Materials (materials-list)
   - Settings (app-settings)
5. AdaptiveNavigationLayout detecta pantalla >= 840dp
6. Renderiza NavigationRail (barra lateral izquierda)
7. Al seleccionar tab, carga DynamicScreen con screenKey correspondiente
```

### Layout Desktop (>= 840dp)
```
+------------------+--------------------------------------------+
| NavigationRail   |                                            |
|                  |              Content Area                  |
| [Dashboard]      |     (DynamicScreen renderiza segun         |
| [Materials]      |      ScreenDefinition del backend)         |
| [Settings]       |                                            |
|                  |                                            |
+------------------+--------------------------------------------+
```

## 7. Problemas Encontrados

### 7.1 CRITICO: Navegacion dinamica NO funciona
- **Endpoint:** `GET /v1/screens/navigation?platform=desktop`
- **Error:** `{"error":"database error during get menu resources","code":"DATABASE_ERROR"}`
- **Causa probable:** La tabla `menu_resources` o `resource_screens` en MongoDB no tiene datos o la conexion falla
- **Impacto:** El cliente KMP Desktop usa el fallback hardcoded (solo 3 tabs)

### 7.2 MEDIO: Menu filtrado vacio
- **Endpoint:** `GET /v1/menu`
- **Respuesta:** `{"items":[]}`
- **Causa:** Los menu_items no tienen `required_permissions` configurados, o el filtrado no esta completo
- **Impacto:** Clientes web que usen este endpoint no veran menu

### 7.3 BAJO: Inconsistencia en nombres de screens del dashboard
- Menu dice `dashboard-superadmin` pero la accion de login navega a `dashboard-home`
- El screen `dashboard-home` no existe (404), pero `dashboard-superadmin` si
- **Impacto:** El handler de login debe resolver el screen correcto segun el rol

## 8. Resumen Visual

```
admin@edugo.test (super_admin) - Desktop
============================================

Pantalla de Login:
  [Logo EduGo]  |  Email: ___________
  "Learning     |  Password: ________
   made easy"   |  [x] Remember me
                |  [===  Sign In  ===]
                |  Forgot password?
                |  ---- or continue with ----
                |  [ Google ]

Post-Login -> Dashboard Superadmin:
  +--- Nav Rail ---+---- Dashboard --------------------------------+
  | [Dashboard]    | Welcome, Admin             February 20, 2026  |
  |                |                                                |
  | [Materials]    | [Schools: X] [Users: X] [Materials: X] [Health]|
  |                |                                                |
  | [Settings]     | System Activity             Quick Actions     |
  |                | - User created...           [Manage Schools]  |
  |                | - Material uploaded...       [View Stats]     |
  +----------------+-----------------------------------------------+
```

## 9. Screens Keys Completos Referenciados

| Screen Key | Existe en API | Pattern | Pertenece a |
|------------|:-------------:|---------|-------------|
| `app-login` | SI | login | Sistema |
| `dashboard-superadmin` | SI | dashboard | Dashboard |
| `dashboard-teacher` | ? | dashboard | Dashboard |
| `dashboard-student` | ? | dashboard | Dashboard |
| `dashboard-schooladmin` | ? | dashboard | Dashboard |
| `dashboard-guardian` | ? | dashboard | Dashboard |
| `users-list` | SI | list | Admin > Usuarios |
| `user-create` | ? | form | Admin > Usuarios |
| `user-edit` | ? | form | Admin > Usuarios |
| `user-detail` | ? | detail | Admin > Usuarios |
| `schools-list` | SI | list | Admin > Escuelas |
| `school-create` | ? | form | Admin > Escuelas |
| `school-edit` | ? | form | Admin > Escuelas |
| `school-detail` | ? | detail | Admin > Escuelas |
| `roles-list` | ? | list | Admin > Roles |
| `role-detail` | ? | detail | Admin > Roles |
| `resources-list` | ? | list | Admin > Permisos |
| `permissions-list` | ? | list | Admin > Permisos |
| `units-list` | SI | list | Academico > Unidades |
| `unit-create` | ? | form | Academico > Unidades |
| `unit-edit` | ? | form | Academico > Unidades |
| `unit-detail` | ? | detail | Academico > Unidades |
| `memberships-list` | SI | list | Academico > Miembros |
| `membership-add` | ? | form | Academico > Miembros |
| `materials-list` | SI | list | Contenido > Materiales |
| `material-create` | ? | form | Contenido > Materiales |
| `material-edit` | ? | form | Contenido > Materiales |
| `material-detail` | ? | detail | Contenido > Materiales |
| `assessments-list` | SI | list | Contenido > Evaluaciones |
| `assessment-take` | ? | form | Contenido > Evaluaciones |
| `assessment-result` | ? | detail | Contenido > Evaluaciones |
| `attempts-history` | ? | list | Contenido > Evaluaciones |
| `progress-my` | ? | dashboard | Reportes > Progreso |
| `progress-unit-list` | ? | list | Reportes > Progreso |
| `progress-student-detail` | ? | detail | Reportes > Progreso |
| `app-settings` | SI | settings | Sistema |

**Total referenciados:** 36 screen keys
**Verificados existentes:** 9 (los que consultamos directamente)

---

*Proximo paso: Repetir este analisis con otros tipos de usuario (teacher, student, school_admin, guardian) para comparar diferencias en menu y screens disponibles.*
