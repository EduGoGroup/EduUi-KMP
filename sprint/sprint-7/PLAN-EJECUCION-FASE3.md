# Plan de Ejecucion - Dynamic UI Fase 3
# Para ejecutar con Team Agents en nueva sesion

**Fecha**: 2026-02-18
**Estado**: Listo para ejecucion
**Prerequisito**: PRs de Phase 2 mergeados a dev (o trabajar sobre las ramas actuales)
**Analisis completo**: ver `ANALISIS-FASE3-DYNAMIC-UI.md`

---

## 0. DECISIONES TECNICAS YA TOMADAS

### 0.1 Dual API Routing (YA IMPLEMENTADO)
El `RemoteDataLoader` soporta prefijos en `data_endpoint`:
- `"/v1/materials"` → mobile API (default)
- `"mobile:/v1/materials"` → mobile API (explicito)
- `"admin:/v1/users"` → admin API

### 0.2 Handler Registration (YA IMPLEMENTADO)
Los handlers se registran en `ScreenHandlersModule.kt` con auto-discovery via `getAll()`.
Para agregar un handler nuevo: una linea `single { NuevoHandler() } bind ScreenActionHandler::class`

### 0.3 Convenciones de data_endpoint por modulo
| Modulo | API | Prefijo en data_endpoint | Ejemplo |
|--------|-----|--------------------------|---------|
| Materials | Mobile | (sin prefijo) | `/v1/materials` |
| Stats | Mobile | (sin prefijo) | `/v1/stats/global` |
| Assessments | Mobile | (sin prefijo) | `/v1/materials/{id}/assessment` |
| Progress | Mobile | (sin prefijo) | `/v1/progress` |
| Users | Admin | `admin:` | `admin:/v1/users/{id}` |
| Schools | Admin | `admin:` | `admin:/v1/schools` |
| Units | Admin | `admin:` | `admin:/v1/schools/{schoolId}/units` |
| Memberships | Admin | `admin:` | `admin:/v1/memberships` |
| Roles | Admin | `admin:` | `admin:/v1/roles` |
| Permissions | Admin | `admin:` | `admin:/v1/permissions` |
| Subjects | Admin | `admin:` | `admin:/v1/subjects` |
| Guardian Relations | Admin | `admin:` | `admin:/v1/guardians/{id}/relations` |

---

## 1. ESTRUCTURA DE EJECUCION

### Fase 3A - Core Flow (Prioridad ALTA)
Objetivo: Flujo completo Teacher/Student con creacion de contenido, evaluaciones y progreso.

```
Fase 3A-1 (Paralelo - Fundamentos):
  - Agent A1: "form-template" → Crear template form-basic-v1 (BD + renderer KMP)
  - Agent A2: "dashboards" → Crear dashboards para superadmin y schooladmin (BD)
  - Agent A3: "platform-adapt" → Implementar platformOverrides en renderers (KMP)

Fase 3A-2 (Despues de A1 - Materiales):
  - Agent A4: "materials-crud" → Screen instances + handlers para material-create/edit
  - Agent A5: "assessments" → Screen instances + handlers para assessments

Fase 3A-3 (Despues de A4/A5 - Progreso):
  - Agent A6: "progress" → Screen instances + handlers para progreso
```

### Fase 3B - Administracion CRUD (Prioridad MEDIA)
```
Fase 3B-1 (Paralelo):
  - Agent B1: "users-crud" → users-list, user-detail, user-create, user-edit + handler
  - Agent B2: "schools-crud" → schools-list, school-detail, school-create, school-edit + handler
  - Agent B3: "units-memberships" → units + memberships CRUD + handlers
```

### Fase 3C - Guardian y RBAC (Prioridad BAJA)
```
  - Agent C1: "guardian" → dashboard-guardian, children-list, child-progress + handler
  - Agent C2: "rbac-screens" → roles-list, role-detail, resources-list, permissions-list
```

---

## 2. TAREA: AGENT A1 - "form-template"

### 2.1 Objetivo
Crear el template `form-basic-v1` en la BD y asegurar que el `FormPatternRenderer` en KMP lo renderiza correctamente.

### 2.2 Cambios en edugo-infrastructure

#### 2.2.1 Agregar template form-basic-v1 al seed 006
**Modificar archivo**: `postgres/migrations/seeds/006_seed_screen_templates.sql`

Agregar template con UUID fijo `a0000000-0000-0000-0000-000000000006`:
```sql
INSERT INTO ui_config.screen_templates (id, pattern, name, description, version, definition) VALUES
('a0000000-0000-0000-0000-000000000006', 'form', 'form-basic-v1', 'Formulario generico con campos dinamicos, validacion, submit/cancel', 1, '{
  "navigation": {
    "topBar": {
      "title": "slot:page_title",
      "showBack": true
    }
  },
  "zones": [
    {
      "id": "form_header",
      "type": "container",
      "slots": [
        {"id": "form_title", "controlType": "label", "style": "headline-medium", "bind": "slot:form_title"},
        {"id": "form_description", "controlType": "label", "style": "body", "bind": "slot:form_description"}
      ]
    },
    {
      "id": "form_fields",
      "type": "form-section",
      "slots": []
    },
    {
      "id": "form_actions",
      "type": "action-group",
      "distribution": "flow-row",
      "slots": [
        {"id": "cancel_btn", "controlType": "outlined-button", "bind": "slot:cancel_label"},
        {"id": "submit_btn", "controlType": "filled-button", "bind": "slot:submit_label"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "distribution": "centered-card",
      "maxWidth": 700
    },
    "web": {
      "distribution": "centered-card",
      "maxWidth": 600
    }
  }
}'::jsonb)
ON CONFLICT (name, version) DO NOTHING;
```

NOTA: Los `form_fields` slots se definen en cada screen_instance via `slot_data`, no en el template.
El template provee la estructura (header + fields zone + actions), la instancia provee los campos especificos.

### 2.3 Cambios en KMP

#### 2.3.1 Verificar FormPatternRenderer
Leer `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/renderer/` y verificar que:
- Existe un renderer para pattern "form"
- Maneja `form-section` zones con slots dinamicos
- Conecta `onFieldChanged` y `onAction` correctamente
- Si no existe o esta incompleto, implementarlo siguiendo el patron de DashboardPatternRenderer

#### 2.3.2 Agregar ScreenPattern.FORM si no existe
Verificar en `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/model/` que `ScreenPattern` incluye `FORM`.

### 2.4 Validacion
```bash
# Recrear BD Neon con nuevo template
cd /Users/jhoanmedina/source/EduGo/repos-separados/edugo-dev-environment
make neon-reset

# Compilar KMP
cd /Users/jhoanmedina/source/EduGo/EduUI/kmp_new
./gradlew :kmp-screens:compileKotlinDesktop
```

---

## 3. TAREA: AGENT A2 - "dashboards"

### 3.1 Objetivo
Crear screen instances para dashboards de superadmin y schooladmin, y actualizar resource-screen mappings.

### 3.2 Cambios en edugo-infrastructure

#### 3.2.1 Agregar screen instances al seed 007
**Modificar archivo**: `postgres/migrations/seeds/007_seed_screen_instances.sql`

Agregar:
- `dashboard-superadmin` (UUID: `b0000000-0000-0000-0000-000000000010`)
  - Template: dashboard-basic-v1
  - KPIs: Total Schools, Total Users, Total Materials, System Health
  - data_endpoint: `admin:/v1/stats/global`
  - scope: system
  - handler_key: null

- `dashboard-schooladmin` (UUID: `b0000000-0000-0000-0000-000000000011`)
  - Template: dashboard-basic-v1
  - KPIs: Teachers, Students, Units, School Score
  - data_endpoint: `/v1/stats/global` (filtrado por school)
  - scope: school
  - handler_key: null

#### 3.2.2 Agregar resource-screen mappings al seed 008
**Modificar archivo**: `postgres/migrations/seeds/008_seed_resource_screens.sql`

Agregar mappings para:
- dashboard → dashboard-superadmin
- dashboard → dashboard-schooladmin
- dashboard → dashboard-student

### 3.3 Cambios en KMP

#### 3.3.1 Actualizar DynamicDashboardScreen
**Modificar archivo**: `kmp-screens/.../dynamic/screens/DynamicDashboardScreen.kt`

Agregar logica para seleccionar screenKey segun rol:
```kotlin
val screenKey = when {
    context?.hasRole("super_admin") == true -> "dashboard-superadmin"
    context?.hasRole("platform_admin") == true -> "dashboard-superadmin"
    context?.hasRole("school_admin") == true -> "dashboard-schooladmin"
    context?.hasRole("school_director") == true -> "dashboard-schooladmin"
    context?.hasRole("teacher") == true -> "dashboard-teacher"
    else -> "dashboard-student"
}
```

### 3.4 Validacion
Recrear BD Neon, verificar que cada rol ve su dashboard correcto.

---

## 4. TAREA: AGENT A3 - "platform-adapt"

### 4.1 Objetivo
Implementar el sistema de platformOverrides en los renderers para que las pantallas se adapten segun la plataforma.

### 4.2 Cambios en KMP

#### 4.2.1 Crear PlatformDetector expect/actual
Si no existe, crear `expect fun getPlatformType(): PlatformType` con actual para cada plataforma:
- Android → COMPACT
- iOS → COMPACT
- Desktop → EXPANDED
- WasmJS → depende de window size

#### 4.2.2 Aplicar platformOverrides en PatternRouter
Antes de pasar la screen al renderer, aplicar los overrides:
```kotlin
val platform = getPlatformType()
val adjustedScreen = PlatformOverrideResolver.apply(screen, platform)
```

#### 4.2.3 Implementar layouts adaptativos
- `side-by-side` → Row con weights
- `three-panel` → Row con 3 secciones
- `centered-card` → Card centrada con maxWidth
- `master-detail` → Row con list (40%) + detail (60%)

#### 4.2.4 Actualizar AdaptiveNavigationLayout
- COMPACT: Bottom Navigation Bar
- MEDIUM: Navigation Rail
- EXPANDED: Navigation Rail + Drawer expandible

### 4.3 Validacion
Probar en Desktop que las pantallas usan los layouts correctos.

---

## 5. TAREA: AGENT A4 - "materials-crud"

### 5.1 Objetivo
Crear pantallas de creacion/edicion de materiales con handlers multi-paso.

### 5.2 Cambios en edugo-infrastructure (seeds)

#### Screen instances nuevas:
- `material-create` (UUID: `b0000000-0000-0000-0000-000000000020`)
  - Template: form-basic-v1
  - Campos: title, subject, grade, description, file (upload)
  - handler_key: 'material-create'

- `material-edit` (UUID: `b0000000-0000-0000-0000-000000000021`)
  - Template: form-basic-v1
  - Campos: title, subject, grade, description
  - data_endpoint: `/v1/materials/{id}` (carga datos existentes)
  - handler_key: 'material-edit'

#### Resource-screen mappings:
- materials → material-create (form, default)
- materials → material-edit (form)

### 5.3 Cambios en KMP

#### MaterialCreateHandler
Flujo multi-paso:
1. POST `/v1/materials` → crear material (obtener ID)
2. POST `/v1/materials/{id}/upload-url` → obtener presigned URL
3. PUT a S3 URL → subir archivo
4. POST `/v1/materials/{id}/upload-complete` → notificar
5. Return ActionResult.NavigateTo("material-detail", params={"id": materialId})

#### MaterialEditHandler
1. PUT `/v1/materials/{id}` con fieldValues
2. Return ActionResult.NavigateTo("material-detail", params={"id": materialId})

### 5.4 Agregar acciones a material-detail y materials-list
Actualizar seeds para agregar botones de "Edit" y "Create" que naveguen a los forms.

---

## 6. TAREA: AGENT A5 - "assessments"

### 6.1 Objetivo
Pantallas de evaluaciones: listar, rendir, ver resultado, historial.

### 6.2 Screen instances nuevas (seeds)
- `assessments-list` → list-basic-v1, data_endpoint: `/v1/materials/{materialId}/assessment`
- `assessment-take` → form-quiz-v1 (template nuevo), handler_key: 'assessment-take'
- `assessment-result` → detail-basic-v1, data_endpoint: `/v1/attempts/{id}/results`
- `attempts-history` → list-basic-v1, data_endpoint: `/v1/users/me/attempts`

### 6.3 Template form-quiz-v1 (si se implementa en esta fase)
Alternativa: usar form-basic-v1 con slots de tipo radio-group para las preguntas.

### 6.4 AssessmentTakeHandler
1. Cargar preguntas (ya vienen en el assessment)
2. Recoger respuestas del fieldValues
3. POST `/v1/materials/{materialId}/assessment/attempts` con answers
4. Return ActionResult.NavigateTo("assessment-result", params={"id": attemptId})

---

## 7. TAREA: AGENT A6 - "progress"

### 7.1 Screen instances nuevas
- `progress-my` → dashboard-basic-v1, data_endpoint: `/v1/stats/global` (filtrado)
- `progress-unit-list` → list-basic-v1, data_endpoint: necesita endpoint de progreso por unidad
- `progress-student-detail` → detail-basic-v1

### 7.2 ProgressHandler
- Lectura de progreso via Mobile API
- Actualizacion via PUT `/v1/progress`

---

## 8. RESUMEN DE ARCHIVOS POR AGENTE (Fase 3A)

### Agent A1: form-template
**MODIFICAR BD:**
- `edugo-infrastructure/postgres/migrations/seeds/006_seed_screen_templates.sql`

**VERIFICAR/MODIFICAR KMP:**
- `kmp-screens/.../renderer/FormPatternRenderer.kt` (verificar/crear)
- `modules/dynamic-ui/.../model/ScreenPattern.kt` (agregar FORM si falta)

### Agent A2: dashboards
**MODIFICAR BD:**
- `edugo-infrastructure/postgres/migrations/seeds/007_seed_screen_instances.sql`
- `edugo-infrastructure/postgres/migrations/seeds/008_seed_resource_screens.sql`

**MODIFICAR KMP:**
- `kmp-screens/.../dynamic/screens/DynamicDashboardScreen.kt`

### Agent A3: platform-adapt
**CREAR/MODIFICAR KMP:**
- PlatformDetector expect/actual (si no existe)
- PlatformOverrideResolver
- PatternRouter (aplicar overrides)
- AdaptiveNavigationLayout (Rail + Drawer)
- Renderers (side-by-side, three-panel, centered-card, master-detail)

### Agent A4: materials-crud
**MODIFICAR BD:**
- Seeds 007, 008

**CREAR KMP:**
- `MaterialCreateHandler.kt` en ScreenHandlersModule
- `MaterialEditHandler.kt` en ScreenHandlersModule

### Agent A5: assessments
**MODIFICAR BD:**
- Seeds 006 (form-quiz-v1 si aplica), 007, 008

**CREAR KMP:**
- `AssessmentTakeHandler.kt` en ScreenHandlersModule

### Agent A6: progress
**MODIFICAR BD:**
- Seeds 007, 008

**CREAR KMP:**
- `ProgressHandler.kt` en ScreenHandlersModule

---

## 9. REGLAS DE TRABAJO

1. **Todo es LOCAL** - No commit, no push hasta validar
2. **BD**: Modificar seeds existentes, NO crear scripts ALTER
3. **Recrear BD**: `cd edugo-dev-environment && make neon-reset`
4. **Dual API**: Usar prefijo `admin:` para endpoints del Admin API
5. **Handlers**: Registrar en `ScreenHandlersModule.kt` con `bind ScreenActionHandler::class`
6. **Templates**: UUIDs fijos con patron `a0000000-0000-0000-0000-00000000000X`
7. **Screen instances**: UUIDs fijos con patron `b0000000-0000-0000-0000-0000000000XX`
8. **Compilar**: `./gradlew :kmp-screens:compileKotlinDesktop`

---

## 10. ORDEN DE EJECUCION RECOMENDADO

```
1. Merge PRs de Phase 2 a dev (o trabajar sobre ramas actuales)
2. Ejecutar Fase 3A-1 en paralelo (A1 + A2 + A3)
3. Ejecutar Fase 3A-2 en paralelo (A4 + A5) - depende de A1
4. Ejecutar Fase 3A-3 (A6) - depende de A4/A5
5. Validacion integral
6. Commit + Push + PRs
7. Si hay tiempo: Fase 3B (admin CRUD)
```
