# Plan de Migración - UI Dinámica (SDUI) para Nueva Arquitectura de 3 APIs

## 1. Estado Actual del Sistema SDUI

El sistema de Server-Driven UI (SDUI) actual funciona con los siguientes componentes:

### Carga de Pantallas
- **RemoteScreenLoader**: Carga definiciones de pantallas desde `mobileApiBaseUrl/v1/screens/{screenKey}`
- **CachedScreenLoader**: Cachea las definiciones de pantallas en `SafeEduGoStorage` para acceso offline y rendimiento

### Resolución de Datos
- **RemoteDataLoader**: Resuelve datos dinámicos usando prefijos para enrutar a la API correcta:
  - `admin:` → `adminApiBaseUrl`
  - `mobile:` → `mobileApiBaseUrl`

### Procesamiento de Acciones
Los **ActionHandlers** procesan las acciones del usuario. Handlers registrados actualmente:

| Handler | Función |
|---------|---------|
| `LoginActionHandler` | Autenticación de usuario |
| `DashboardActionHandler` | Pantalla principal con estadísticas |
| `SettingsActionHandler` | Configuraciones y preferencias |
| `MaterialCreateHandler` | Creación de materiales educativos |
| `MaterialEditHandler` | Edición de materiales educativos |
| `AssessmentTakeHandler` | Toma de evaluaciones |
| `ProgressHandler` | Progreso del estudiante |
| `UserCrudHandler` | CRUD de usuarios |
| `SchoolCrudHandler` | CRUD de escuelas |
| `UnitCrudHandler` | CRUD de unidades académicas |
| `MembershipHandler` | Gestión de membresías |
| `GuardianHandler` | Relaciones de tutor/acudiente |

---

## 2. Nueva Arquitectura de 3 APIs

El backend se dividió en 3 APIs independientes:

| API | Puerto | Responsabilidad |
|-----|--------|-----------------|
| **IAM Platform** | 8070 | Auth, roles, permisos, menú, screen-config (templates) |
| **Admin** | 8060 | Escuelas, unidades académicas, membresías, usuarios, materiales admin |
| **Mobile** | 8065 | Materiales, assessments, progreso, pantallas dinámicas, estadísticas |

---

## 3. Cambios Necesarios para UI Dinámica

### Fase 2A: RemoteDataLoader - Agregar prefijo `iam:`

**Archivo:** `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/data/RemoteDataLoader.kt`

**Estado actual:** Soporta prefijos `admin:` y `mobile:` para enrutar peticiones.

**Cambio requerido:** Agregar soporte para el prefijo `iam:` que resuelva a `iamApiBaseUrl`.

Resultado esperado:
- `admin:endpoint` → `adminApiBaseUrl/endpoint`
- `mobile:endpoint` → `mobileApiBaseUrl/endpoint`
- `iam:endpoint` → `iamApiBaseUrl/endpoint` *(nuevo)*

### Fase 2B: Cambio de Paths `/v1/` → `/api/v1/`

Todos los endpoints de las nuevas APIs cambiaron su prefijo de path.

**Componentes afectados:**
- `RemoteScreenLoader` - URLs de carga de pantallas
- `RemoteDataLoader` - URLs de resolución de datos
- `ApiCallHandler` - URLs de llamadas API
- `SubmitFormHandler` - URLs de envío de formularios

**Estrategia:** Actualizar las URLs base o los paths construidos para usar `/api/v1/` en lugar de `/v1/`.

### Fase 2C: ApiCallHandler - Enrutamiento Multi-API

**Problema:** Actualmente usa solo `adminApiBaseUrl` para todas las llamadas API.

**Solución propuesta:** Implementar enrutamiento inteligente basado en prefijos en las URLs de acción:
- URLs con prefijo `iam:` → `iamApiBaseUrl`
- URLs con prefijo `admin:` → `adminApiBaseUrl`
- URLs con prefijo `mobile:` → `mobileApiBaseUrl`

### Fase 2D: SubmitFormHandler - Enrutamiento Multi-API

**Problema:** Mismo que ApiCallHandler - necesita enrutar formularios a la API correcta.

**Solución:** Aplicar el mismo patrón de prefijos que ApiCallHandler para determinar la API destino del formulario.

### Fase 2E: Screen-Config en IAM

Las configuraciones de templates de pantallas (`/screen-config/*`) ahora residen en IAM Platform.

**Impacto:** Afecta la administración de pantallas (creación/edición de templates), no la carga de pantallas para el usuario final (que sigue en Mobile API).

---

## 4. Endpoints Relevantes por API

### Mobile API (puerto 8065)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/screens/{screenKey}` | Cargar definición de pantalla |
| GET | `/api/v1/screens/navigation` | Definición de navegación |
| GET | `/api/v1/screens/resource/{resourceKey}` | Pantalla por recurso |
| GET | `/api/v1/screens/{screenKey}/preferences` | Preferencias de pantalla |
| GET | `/api/v1/materials` | Listar materiales |
| POST | `/api/v1/materials` | Crear material |
| GET | `/api/v1/materials/{id}` | Detalle de material |
| * | `/api/v1/materials/{id}/assessment/*` | Endpoints de evaluaciones |
| GET | `/api/v1/progress` | Progreso del estudiante |
| GET | `/api/v1/stats/global` | Estadísticas globales |

### Admin API (puerto 8060)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| CRUD | `/api/v1/schools/*` | Gestión de escuelas |
| CRUD | `/api/v1/schools/{id}/units` | Unidades académicas |
| CRUD | `/api/v1/memberships/*` | Gestión de membresías |
| CRUD | `/api/v1/users/*` | Gestión de usuarios |
| GET | `/api/v1/subjects` | Listar materias |
| CRUD | `/api/v1/guardian-relations/*` | Relaciones de tutores |

### IAM API (puerto 8070)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| * | `/api/v1/auth/*` | Autenticación (login, refresh, etc.) |
| GET | `/api/v1/menu` | Menú del usuario |
| GET | `/api/v1/menu/full` | Menú completo |
| CRUD | `/api/v1/roles/*` | Gestión de roles |
| CRUD | `/api/v1/permissions/*` | Gestión de permisos |
| CRUD | `/api/v1/resources/*` | Gestión de recursos |
| CRUD | `/api/v1/screen-config/*` | Configuración de templates de pantallas |

---

## 5. Mapeo de ActionHandlers a APIs

| Handler | API Destino | Notas |
|---------|-------------|-------|
| `LoginActionHandler` | **IAM** | Ya migrado en Fase 2 del Sprint 7 |
| `DashboardActionHandler` | **Mobile** + **Admin** | Stats desde Mobile, info de escuelas desde Admin |
| `SettingsActionHandler` | **IAM** | Permisos y contexto de usuario |
| `MaterialCreateHandler` | **Mobile** | Creación de materiales educativos |
| `MaterialEditHandler` | **Mobile** | Edición de materiales educativos |
| `AssessmentTakeHandler` | **Mobile** | Evaluaciones y respuestas |
| `ProgressHandler` | **Mobile** | Progreso del estudiante |
| `UserCrudHandler` | **Admin** | CRUD de usuarios |
| `SchoolCrudHandler` | **Admin** | CRUD de escuelas |
| `UnitCrudHandler` | **Admin** | CRUD de unidades académicas |
| `MembershipHandler` | **Admin** | Gestión de membresías |
| `GuardianHandler` | **Admin** | Relaciones tutor-estudiante |

---

## 6. Plataformas Objetivo (Orden de Prioridad)

1. **Android Phone / iPhone** - Sin diferencia entre ambos, primera prioridad
2. **Android Tablet** - Layouts adaptados a pantalla grande
3. **Desktop FHD** - Aplicación de escritorio (1920x1080)
4. **Web** - Material Design 3.0 (WasmJS target)

---

## 7. Estimación de Esfuerzo y Prioridades

### Orden de Implementación Sugerido

**Prioridad Alta (infraestructura base):**
1. Actualizar paths `/v1/` → `/api/v1/` en todos los componentes (Fase 2B)
2. Agregar prefijo `iam:` a RemoteDataLoader (Fase 2A)

**Prioridad Media (handlers de lectura primero):**
3. Adaptar `DashboardActionHandler` (GET - uso muy frecuente)
4. Adaptar `ProgressHandler` (GET - uso frecuente)
5. Adaptar `SchoolCrudHandler` - lectura (GET - admin frecuente)
6. Adaptar `UserCrudHandler` - lectura (GET - admin frecuente)

**Prioridad Normal (handlers de escritura):**
7. Adaptar `MaterialCreateHandler` / `MaterialEditHandler` (POST/PUT)
8. Adaptar `AssessmentTakeHandler` (POST)
9. Adaptar `MembershipHandler` (POST/PUT/DELETE)
10. Adaptar `UnitCrudHandler` (POST/PUT/DELETE)
11. Adaptar `GuardianHandler` (POST/PUT/DELETE)

**Prioridad Baja (ya migrados o menor uso):**
12. Migrar screen-config a IAM (Fase 2E) - afecta solo administración
13. `LoginActionHandler` - ya migrado
14. `SettingsActionHandler` - ajuste menor

### Criterios de Priorización
- **Primero lectura (GET), después escritura (POST/PUT/DELETE):** Los handlers de lectura se usan con más frecuencia y su migración permite validar el enrutamiento antes de las operaciones de escritura.
- **Primero los más usados:** Dashboard y Progress son las pantallas más visitadas.
- **Infraestructura antes que features:** Los cambios de paths y el nuevo prefijo son prerequisitos para todo lo demás.
