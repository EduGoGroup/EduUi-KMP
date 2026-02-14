# Fase 1: Especificacion de Pantallas Principales

## Descripcion General

La Fase 1 entrega 5 pantallas principales que forman la navegacion central de la aplicacion EduGo. Cada pantalla es controlada por la configuracion del backend y renderizada usando pattern renderers.

## Flujo de Navegacion

```
Inicio de la App
    ↓
[Splash] → Verificar estado de autenticacion
    ├── Autenticado → [Dashboard]
    └── No autenticado → [Login]

[Login] → Autenticar
    └── Exitoso → [Dashboard]

[Dashboard] (Pestana de Inicio)
    ├── Pestana: Materiales → [Lista de Materiales]
    │       └── Clic en item → [Detalle de Material]
    ├── Pestana: Dashboard → [Dashboard]
    └── Pestana: Configuracion → [Configuracion]

Navegacion Inferior:
    [Dashboard] | [Lista de Materiales] | [Configuracion]
```

---

## Pantalla 1: Login

### Configuracion del Backend
- **screen_key**: `app-login`
- **pattern**: `login`
- **data_endpoint**: `null` (sin carga de datos)

### Disposicion por Plataforma
| Plataforma | Disposicion |
|------------|-------------|
| Mobile | Columna unica: marca → formulario → redes sociales → enlace de registro |
| Desktop | Division horizontal: 40% panel de marca | 60% formulario |
| Web | Tarjeta centrada (max-width: 480dp) |

### Zones y Slots
```
Zone: brand
  - Slot: app_logo (icon, value: "edugo_logo")
  - Slot: app_name (label, style: "headline-large", value: "EduGo")
  - Slot: app_tagline (label, style: "body", value: "Learning made easy")

Zone: form (form-section)
  - Slot: email (email-input, label: "Email", required: true)
  - Slot: password (password-input, label: "Password", required: true)
  - Slot: remember_me (checkbox, label: "Remember me")
  - Slot: login_btn (filled-button, value: "Sign In")
  - Slot: forgot_password (text-button, value: "Forgot password?")

Zone: social
  - Slot: divider_text (label, value: "or continue with")
  - Slot: google_btn (outlined-button, value: "Google", icon: "google")
```

### Acciones
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
      "fieldMapping": { "email": "email", "password": "password" },
      "onSuccess": { "type": "NAVIGATE", "config": { "target": "dashboard-home" } }
    }
  }
]
```

### Notas de Integracion
- La accion de login llama a `AuthService.login()` directamente (no es una llamada generica a la API)
- La configuracion del backend define la estructura del formulario; la logica de autenticacion permanece en el modulo de auth
- El campo de contrasena tiene `secureTextEntry: true` (comportamiento nativo de la plataforma)
- "Recordarme" se integra con SafeEduGoStorage

---

## Pantalla 2: Dashboard

### Configuracion del Backend
- **screen_key**: `dashboard-teacher` / `dashboard-student`
- **pattern**: `dashboard`
- **data_endpoint**: `/v1/stats/global` (profesor) o personalizado (estudiante)

### Disposicion por Plataforma
| Plataforma | Disposicion |
|------------|-------------|
| Mobile | Columna unica: saludo → KPIs → actividad → acciones rapidas |
| Desktop | Tres zones: nav-rail izquierdo | contenido central | barra lateral derecha (actividad + acciones) |
| Web | Tarjetas responsivas con grilla de KPIs |

### Zones y Slots (Dashboard del Profesor)
```
Zone: greeting (container)
  - Slot: greeting_text (label, style: "headline-large", value: "Good morning, {user.firstName}")
  - Slot: date_text (label, style: "body", value: "{today_date}")

Zone: kpis (metric-grid, distribution: grid)
  - Slot: total_students (metric-card, label: "Students", field: "total_students", icon: "people")
  - Slot: total_materials (metric-card, label: "Materials", field: "total_materials", icon: "folder")
  - Slot: avg_score (metric-card, label: "Avg Score", field: "avg_score", icon: "trending_up")
  - Slot: completion_rate (metric-card, label: "Completion", field: "completion_rate", icon: "check_circle")

Zone: recent_activity (simple-list)
  - Slot: section_title (label, style: "title-medium", value: "Recent Activity")
  - ItemLayout:
    - Slot: activity_icon (icon, field: "type_icon")
    - Slot: activity_text (label, style: "body", field: "description")
    - Slot: activity_time (label, style: "caption", field: "time_ago")

Zone: quick_actions (action-group, distribution: flow-row)
  - Slot: upload_material (outlined-button, value: "Upload Material", icon: "upload")
  - Slot: view_progress (outlined-button, value: "View Progress", icon: "bar_chart")
```

### Acciones
```json
[
  {
    "id": "navigate-materials",
    "trigger": "button_click",
    "triggerSlotId": "upload_material",
    "type": "NAVIGATE",
    "config": { "target": "materials-list" }
  },
  {
    "id": "refresh-dashboard",
    "trigger": "pull_refresh",
    "type": "REFRESH"
  }
]
```

### Fuente de Datos
- Datos de KPI desde `GET /v1/stats/global` (api-mobile)
- Actividad reciente: necesitaria un nuevo endpoint o agregar datos de los existentes
- Saludo al usuario: desde AuthService.currentUser

---

## Pantalla 3: Lista de Materiales

### Configuracion del Backend
- **screen_key**: `materials-list`
- **pattern**: `list`
- **data_endpoint**: `/v1/materials`
- **required_permission**: `materials:read`

### Disposicion por Plataforma
| Plataforma | Disposicion |
|------------|-------------|
| Mobile | Barra de busqueda → Chips de filtro → Lista desplazable |
| Desktop | Division maestro-detalle: 40% lista | 60% panel de detalle |
| Web | Grilla responsiva de tarjetas (1/2/3 columnas) |

### Zones y Slots
```
Zone: search_zone (container)
  - Slot: search_bar (search-bar, placeholder: "Search materials...")

Zone: filters (container, distribution: flow-row)
  - Slot: filter_all (chip, value: "All", selected: true)
  - Slot: filter_ready (chip, value: "Ready")
  - Slot: filter_processing (chip, value: "Processing")

Zone: empty_state (container, condition: "data.isEmpty")
  - Slot: empty_icon (icon, value: "folder_open")
  - Slot: empty_title (label, style: "headline", value: "No materials yet")
  - Slot: empty_desc (label, style: "body", value: "Upload your first educational material")
  - Slot: empty_action (filled-button, value: "Upload Material")

Zone: list_content (simple-list, condition: "!data.isEmpty")
  ItemLayout:
    - Slot: item_icon (icon, field: "file_type_icon")
    - Slot: item_title (label, style: "headline-small", field: "title")
    - Slot: item_subtitle (label, style: "body-small", field: "subject")
    - Slot: item_status (chip, field: "status")
    - Slot: item_date (label, style: "caption", field: "created_at")
```

### Configuracion de Datos
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
    "subject": "subject",
    "status": "status",
    "file_type_icon": "file_type",
    "created_at": "created_at",
    "id": "id"
  }
}
```

### Acciones
```json
[
  {
    "id": "item-click",
    "trigger": "item_click",
    "type": "NAVIGATE",
    "config": { "target": "material-detail", "params": { "id": "{item.id}" } }
  },
  {
    "id": "search",
    "trigger": "button_click",
    "triggerSlotId": "search_bar",
    "type": "REFRESH",
    "config": { "addParams": { "search": "{search_bar.value}" } }
  },
  {
    "id": "pull-refresh",
    "trigger": "pull_refresh",
    "type": "REFRESH"
  }
]
```

### Notas de Integracion
- Paginacion: scroll infinito con parametro `offset`
- Busqueda: campo de busqueda con debounce que dispara la recarga de datos con parametro de consulta
- Chips de filtro: modifican el parametro de consulta `status` y recargan
- Pull-to-refresh: recarga la primera pagina

---

## Pantalla 4: Detalle de Material

### Configuracion del Backend
- **screen_key**: `material-detail`
- **pattern**: `detail`
- **data_endpoint**: `/v1/materials/{id}`
- **required_permission**: `materials:read`

### Disposicion por Plataforma
| Plataforma | Disposicion |
|------------|-------------|
| Mobile | Columna unica: hero → titulo → etiquetas → descripcion → detalles → acciones |
| Desktop | Division: 40% hero + metadatos | 60% contenido + acciones |
| Web | Tarjeta responsiva con max-width |

### Zones y Slots
```
Zone: hero (container)
  - Slot: file_type_icon (icon, style: "large", field: "file_type")
  - Slot: status_badge (chip, field: "status")

Zone: header (container)
  - Slot: title (label, style: "headline-large", field: "title")
  - Slot: subject (label, style: "body", field: "subject")
  - Slot: grade (label, style: "body-small", field: "grade")

Zone: details (container, type: pares clave-valor)
  - Slot: file_size (label, label: "File Size", field: "file_size_display")
  - Slot: uploaded_date (label, label: "Uploaded", field: "created_at")
  - Slot: status (label, label: "Status", field: "status")

Zone: description (container)
  - Slot: section_title (label, style: "title-medium", value: "Description")
  - Slot: description_text (label, style: "body", field: "description")

Zone: summary (container, condition: "data.summary != null")
  - Slot: summary_title (label, style: "title-medium", value: "AI Summary")
  - Slot: summary_content (label, style: "body", field: "summary.main_ideas")

Zone: actions (action-group)
  - Slot: download_btn (filled-button, value: "Download", icon: "download")
  - Slot: take_quiz_btn (outlined-button, value: "Take Quiz", icon: "quiz")
```

### Acciones
```json
[
  {
    "id": "download-material",
    "trigger": "button_click",
    "triggerSlotId": "download_btn",
    "type": "API_CALL",
    "config": {
      "endpoint": "/v1/materials/{id}/download-url",
      "method": "GET",
      "onSuccess": { "type": "OPEN_URL", "config": { "url": "{response.url}" } }
    }
  },
  {
    "id": "take-quiz",
    "trigger": "button_click",
    "triggerSlotId": "take_quiz_btn",
    "type": "NAVIGATE",
    "config": { "target": "assessment-view", "params": { "materialId": "{item.id}" } }
  },
  {
    "id": "go-back",
    "trigger": "button_click",
    "triggerSlotId": "back_btn",
    "type": "NAVIGATE_BACK"
  }
]
```

### Notas de Integracion
- El detalle del material se carga via `GET /v1/materials/{id}`
- El Resumen de IA se carga mediante una llamada separada: `GET /v1/materials/{id}/summary`
- La descarga genera una URL prefirmada, luego se abre en el navegador/visor
- La navegacion al quiz va a la pantalla de evaluacion (Fase 2)
- Seguimiento de progreso: se llama a `PUT /v1/progress` cuando el usuario comienza a leer

---

## Pantalla 5: Configuracion

### Configuracion del Backend
- **screen_key**: `app-settings`
- **pattern**: `settings`
- **data_endpoint**: `null` (preferencias locales + informacion de usuario desde auth)

### Disposicion por Plataforma
| Plataforma | Disposicion |
|------------|-------------|
| Mobile | Lista de secciones con switches, items de lista, items de navegacion |
| Desktop | Division: 30% barra lateral de categorias | 70% area de contenido |
| Web | Responsivo (apilado en compacto, dividido en expandido) |

### Zones y Slots
```
Zone: user_card (container)
  - Slot: avatar (avatar, field: "user.avatar_url")
  - Slot: user_name (label, style: "headline-small", field: "user.full_name")
  - Slot: user_email (label, style: "body-small", field: "user.email")
  - Slot: user_role (chip, field: "user.role")

Zone: section_appearance (form-section)
  - Slot: section_title (label, style: "title-medium", value: "Appearance")
  - Slot: dark_mode (switch, label: "Dark Mode", field: "preferences.dark_mode")
  - Slot: theme_color (list-item-navigation, label: "Theme Color", field: "preferences.theme")

Zone: section_notifications (form-section)
  - Slot: section_title (label, style: "title-medium", value: "Notifications")
  - Slot: push_notifications (switch, label: "Push Notifications", field: "preferences.push_enabled")
  - Slot: email_notifications (switch, label: "Email Notifications", field: "preferences.email_enabled")

Zone: section_account (form-section)
  - Slot: section_title (label, style: "title-medium", value: "Account")
  - Slot: change_password (list-item-navigation, label: "Change Password", icon: "lock")
  - Slot: language (list-item-navigation, label: "Language", icon: "language", field: "preferences.language")

Zone: section_about (form-section)
  - Slot: section_title (label, style: "title-medium", value: "About")
  - Slot: version (list-item, label: "App Version", value: "1.0.0")
  - Slot: privacy_policy (list-item-navigation, label: "Privacy Policy", icon: "privacy_tip")
  - Slot: terms (list-item-navigation, label: "Terms of Service", icon: "description")

Zone: logout (container)
  - Slot: logout_btn (filled-button, value: "Sign Out", style: "error")
```

### Acciones
```json
[
  {
    "id": "toggle-dark-mode",
    "trigger": "button_click",
    "triggerSlotId": "dark_mode",
    "type": "API_CALL",
    "config": {
      "handler": "theme_toggle",
      "local": true
    }
  },
  {
    "id": "logout",
    "trigger": "button_click",
    "triggerSlotId": "logout_btn",
    "type": "CONFIRM",
    "config": {
      "title": "Sign Out",
      "message": "Are you sure you want to sign out?",
      "confirmLabel": "Sign Out",
      "onConfirm": { "type": "LOGOUT" }
    }
  }
]
```

### Notas de Integracion
- El toggle de tema llama a `ThemeService` directamente (accion local, sin llamada a la API)
- La informacion del usuario viene de `AuthService.currentUser`
- Las preferencias de notificaciones se almacenan localmente en la Fase 1, se sincronizan con el backend en la Fase 2
- La accion de logout llama a `AuthService.logout()` y navega al Login
- Configuracion es un hibrido: parte de la configuracion del backend (estructura de secciones, etiquetas) + preferencias locales

---

## Matriz de Dependencias de Pantallas

| Pantalla | Endpoint de API Mobile | Se Necesita Nuevo Endpoint? | Notas |
|----------|------------------------|----------------------------|-------|
| Login | POST /v1/auth/login | No (usa api-admin) | La autenticacion se maneja con el AuthService existente |
| Dashboard | GET /v1/stats/global | Parcial (necesita feed de actividad) | Los KPIs existen, la actividad necesita agregacion |
| Lista de Materiales | GET /v1/materials | No | Endpoint existente con paginacion |
| Detalle de Material | GET /v1/materials/{id} | No | Endpoint existente |
| Detalle de Material | GET /v1/materials/{id}/summary | No | Endpoint existente |
| Configuracion | Ninguno (local) | No | La Fase 1 usa preferencias locales |

## Nuevos Endpoints Requeridos

1. **GET /v1/screens/:screenKey** - Configuracion de pantalla (nuevo)
2. **GET /v1/screens/navigation** - Estructura de navegacion (nuevo)
3. **GET /v1/dashboard/activity** - Feed de actividad reciente (nuevo, opcional para la Fase 1)
