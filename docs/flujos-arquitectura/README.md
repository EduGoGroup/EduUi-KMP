# Flujos de Arquitectura — EduGo KMP

> **Actualizado en febrero 2026** para reflejar la implementacion completa hasta Sprint 9:
> Sync Bundle System, Arquitectura Offline-First, DynamicToolbar, catalogo expandido de
> ScreenContracts (30+), SelectField, ThemeService, StorageMigrator y DynamicDashboard por rol.

> Documentacion detallada de los procesos internos del proyecto multiplataforma EduGo.
> Incluye diagramas secuenciales, de componentes, de estados y comparativas por plataforma.

---

## Indice de Documentos

| # | Documento | Descripcion |
|---|-----------|-------------|
| 01 | [Menu y Navegacion](./01-menu-navegacion.md) | Flujo de construccion del menu, navegacion adaptativa y routing por rol |
| 02 | [Permisos y RBAC](./02-permisos-rbac.md) | Sistema RBAC con ActiveContext, verificacion de permisos y switch de escuela |
| 03 | [Carga de UI Dinamica (SDUI)](./03-carga-sdui.md) | Pipeline completo desde backend hasta renderizado de pantallas dinamicas |
| 04 | [Acciones de la UI](./04-acciones-ui.md) | ScreenEvent → EventOrchestrator → EventResult, contratos y handlers |
| 05 | [Almacenamiento Local](./05-storage-local.md) | SafeEduGoStorage, estrategia de cache multinivel, migraciones y diferencias por plataforma |
| 06 | [Conexion a Endpoints](./06-network-endpoints.md) | Ktor, interceptores, multi-API routing y resiliencia |
| 07 | [Conectado y Offline](./07-offline-conectividad.md) | Estrategias de cache, stale-while-revalidate y busqueda offline |
| 08 | [Arquitectura Global y DI](./08-arquitectura-global.md) | Capas, modulos Koin, ciclo de vida y flujo completo de datos |
| 09 | [Mejoras Propuestas](./09-mejoras-propuestas.md) | Analisis de gaps y propuestas de mejora tecnica |
| 10 | [Multiidioma (i18n)](./10-multiidioma-i18n.md) | Arquitectura hibrida: composeResources + strings del servidor via sync bundle |
| 11 | [Conceptos y Terminologia](./11-conceptos-terminologia.md) | Sistema de terminologia dinamica por institucion (concept_types + school_concepts) |
| 12 | [SDUI Remote Select](./12-sdui-remote-select.md) | Dropdown dinamico para formularios: select (implementado) y remote_select (pendiente) |

---

## Convenciones de los Diagramas

Los diagramas usan sintaxis **Mermaid** (compatible con GitHub, GitLab, Obsidian, VSCode con extension).

```
flowchart      → Flujo de proceso y decisiones
sequenceDiagram → Interacciones entre componentes en orden temporal
stateDiagram   → Maquinas de estados
classDiagram   → Relaciones entre clases/interfaces
graph LR/TD    → Grafos de dependencias
```

---

## Capas del Proyecto

```
┌─────────────────────────────────────────────────────────┐
│  PLATAFORMAS: Android · iOS · Desktop · WasmJS          │
├─────────────────────────────────────────────────────────┤
│  kmp-screens: Composables + Navegacion + Contratos      │
│               + DynamicToolbar + ConnectivityBanner      │
│               + 30+ ScreenContracts (CRUD, Dashboard)   │
│               + 22 ControlTypes (incl. SelectField)     │
│               + DynamicDashboard (por rol)               │
│               + DynamicSettings (ThemeService)           │
│               + ZoneErrorBoundary (error por zona)       │
├─────────────────────────────────────────────────────────┤
│  modules/dynamic-ui: SDUI ViewModel + Orchestrator      │
│                      + DataSyncService + MutationQueue   │
│                      + SyncEngine + CacheConfig          │
│                      + SlotBindingResolver + FormFields   │
├─────────────────────────────────────────────────────────┤
│  modules/auth: RBAC + JWT + Refresh (con rotacion)      │
├─────────────────────────────────────────────────────────┤
│  modules/network: Ktor + Interceptors + CircuitBreaker  │
│                   + NetworkObserver multiplataforma      │
├─────────────────────────────────────────────────────────┤
│  modules/storage: SafeStorage + multiplatform-settings  │
│                   + LocalSyncStore + StorageMigrator     │
├─────────────────────────────────────────────────────────┤
│  modules/settings: ThemeService + ThemeOption            │
├─────────────────────────────────────────────────────────┤
│  modules/validation: AccumulativeValidation             │
├─────────────────────────────────────────────────────────┤
│  modules/foundation: Result + DomainMapper + AppError   │
└─────────────────────────────────────────────────────────┘
```

---

## Sprint 8 - Resumen de cambios

### Sync Bundle System

Se centralizo la carga de datos del usuario en un unico bundle de sincronizacion. Los componentes principales son:

- **DataSyncService**: Orquesta la descarga del bundle completo (menu, permisos, pantallas, contextos) en una sola llamada al endpoint `/api/v1/sync/bundle`. Tambien soporta delta sync via `/api/v1/sync/delta` enviando hashes para recibir solo los datos que cambiaron.
- **LocalSyncStore**: Persiste el bundle en storage local para restauracion inmediata al abrir la app.
- **SyncRepository**: Capa intermedia entre el servicio remoto y el store local, maneja la logica de cuando usar datos locales vs remotos.

### Arquitectura Offline-First

Se implemento soporte completo para operacion sin conexion en todas las plataformas:

- **NetworkObserver**: Observador de conectividad multiplataforma (Android, iOS, Desktop, WasmJS) que emite un `StateFlow<Boolean>` con el estado de red en tiempo real.
- **MutationQueue**: Cola persistente de operaciones de escritura (crear, editar, eliminar) que se ejecutaron offline. Las mutaciones se encolan y se envian automaticamente cuando se recupera la conexion.
- **SyncEngine**: Motor de sincronizacion que coordina la reconciliacion de datos locales con el servidor al reconectarse. Ejecuta delta sync y procesa la cola de mutaciones pendientes.
- **ConflictResolver**: Estrategia de resolucion de conflictos cuando los datos locales y remotos divergen (por defecto: server-wins con notificacion al usuario).

### DynamicToolbar

Toolbar dinamico que se adapta automaticamente segun el patron de la pantalla SDUI:

- **LIST**: Titulo de la pantalla + boton "+" para crear (visible solo si el usuario tiene permiso CREATE).
- **FORM**: Flecha de retroceso + titulo (edit_title o page_title) + boton "Guardar" que dispara el evento custom `submit-form`.
- **DETAIL**: Flecha de retroceso + titulo de la pantalla.
- **DASHBOARD / SETTINGS**: Solo titulo.
- **LOGIN**: Sin toolbar.

### ConnectivityBanner y StaleDataIndicator

- **ConnectivityBanner**: Banner visual que aparece cuando la app pierde conexion a internet, informando al usuario que esta operando en modo offline.
- **StaleDataIndicator**: Indicador que se muestra cuando los datos en pantalla provienen de cache local y pueden estar desactualizados, junto con la fecha de la ultima sincronizacion exitosa.

### CacheConfig con TTLs por patron

Configuracion de cache diferenciada segun el tipo de pantalla:

- Las pantallas de tipo DASHBOARD tienen TTLs mas cortos (datos que cambian frecuentemente).
- Las pantallas de tipo LIST y DETAIL tienen TTLs intermedios.
- Las pantallas de tipo SETTINGS tienen TTLs mas largos (datos que cambian raramente).
- La configuracion es extensible y se puede ajustar por screen key individual.

### Rotacion de JWT Refresh Token

Se migraron los refresh tokens de formato random base64 a JWT firmados. Cada vez que se usa un refresh token, el servidor emite uno nuevo (rotacion), invalidando el anterior. Esto mejora la seguridad y permite detectar reutilizacion de tokens comprometidos. El `RefreshResponse` ahora incluye `refreshToken` y `activeContext`.

### SchoolSelectionScreen

Nueva pantalla post-login para usuarios que pertenecen a mas de una escuela. El flujo es:

1. Login exitoso retorna la lista de escuelas del usuario.
2. Si tiene mas de una escuela, se muestra `SchoolSelectionScreen` para que elija.
3. Al seleccionar una escuela, se ejecuta `fullSync()` para cargar los datos del contexto seleccionado.
4. Si tiene una sola escuela, se salta directo al dashboard con sync automatico.

### Versionado de pantallas

Endpoint de versionado que permite al cliente verificar si las definiciones de pantallas SDUI han cambiado desde la ultima sincronizacion, evitando descargas innecesarias del bundle completo.

---

## Sprint 9 - Resumen de cambios

### Catalogo expandido de ScreenContracts (30+)

Se paso de ~5 contracts a mas de 30, cubriendo todos los flujos CRUD de la aplicacion:

| Categoria | Contracts | Descripcion |
|-----------|-----------|-------------|
| **Auth** | `LoginContract` | Login SDUI con custom event `submit-login` |
| **Settings** | `SettingsContract` | Logout, theme toggle, navigate back |
| **Dashboard** | `DashboardSuperadminContract`, `DashboardSchooladminContract`, `DashboardTeacherContract`, `DashboardStudentContract`, `DashboardGuardianContract`, `ProgressDashboardContract`, `StatsDashboardContract` | 7 dashboards por rol, todos heredan de `BaseDashboardContract` |
| **Escuelas** | `SchoolsListContract`, `SchoolsFormContract`, `SchoolCrudContract` (×2: create/edit) | CRUD completo de escuelas |
| **Usuarios** | `UsersListContract`, `UserCrudContract` (×2: create/edit) | CRUD de usuarios con navegacion a detalle |
| **Unidades Academicas** | `UnitsListContract`, `UnitCrudContract` (×2: create/edit) | CRUD anidado bajo escuelas (`/schools/{id}/units`) |
| **Materias** | `SubjectsListContract`, `SubjectsFormContract` | Lista y formulario de materias |
| **Memberships** | `MembershipsListContract`, `MembershipAddContract` | Asignacion de usuarios a unidades por rol |
| **Materiales** | `MaterialsListContract`, `MaterialCreateContract`, `MaterialEditContract`, `MaterialDetailContract` | CRUD completo de material educativo |
| **Evaluaciones** | `AssessmentsListContract`, `AssessmentTakeContract` | Lista y toma de evaluaciones |
| **Roles/Permisos** | `RolesListContract`, `PermissionsListContract` | Listas de roles y permisos (solo lectura) |
| **Guardian** | `GuardianContract` (×2: children-list, child-progress) | Pantallas de seguimiento para acudientes |

### 22 ControlTypes SDUI

Se expandio el catalogo de controles renderizables desde 6 a 22:

```
text-input, email-input, password-input, number-input, search-bar,
select, checkbox, switch, radio-group, chip, rating,
filled-button, outlined-button, text-button, icon-button,
label, icon, avatar, image, divider,
list-item, list-item-navigation, metric-card
```

### SelectField implementado

Componente `SelectField.kt` implementado en `kmp-screens/dynamic/components/`. Soporta dropdown con opciones fijas (`select` controlType con `options` en el slot). El `remote_select` (opciones desde endpoint) queda pendiente.

### DynamicDashboardScreen (Dashboard por rol)

Pantalla que selecciona automaticamente el screenKey del dashboard segun el rol del usuario autenticado:
- `super_admin` / `platform_admin` → `dashboard-superadmin`
- `school_admin` / `school_director` → `dashboard-schooladmin`
- `teacher` → `dashboard-teacher`
- `guardian` → `dashboard-guardian`
- default → `dashboard-student`

Inyecta placeholders contextuales: `user.firstName`, `user.fullName`, `context.roleName`, `context.schoolName`, `today_date`.

### DynamicSettingsScreen (Tema)

Pantalla de settings integrada con `ThemeService`. Detecta cambios en el campo `dark_mode` y aplica el tema via `ThemeServiceImpl` (LIGHT, DARK, SYSTEM).

### StorageMigrator

Sistema de migraciones de esquema para storage local:
- `StorageMigration` (interface): Define un `version` y `migrate(storage)`.
- `StorageMigrator`: Ejecuta migraciones pendientes secuencialmente y persiste la version actual en `storage.schema.version`.
- Crash-safe: cada migracion persiste su version inmediatamente.

### DisplayValueFormatter

Utilidad para formato automatico de valores mostrados:
- Booleanos: `true` → "Activo", `false` → "Inactivo"
- Fechas ISO: `2026-02-15T10:30:00Z` → "15/02/2026"

### ListItemRendererRegistry

Registro de renderers personalizados por `screenKey`. Permite que una lista use un renderer custom en vez del `DefaultListItemRenderer`. Actualmente vacio (todas las listas usan el renderer por defecto).

### DI expandido

KoinInitializer ahora agrupa modulos en 3 niveles:
- **Core**: foundationModule + coreModule + loggerModule + validationModule
- **Infrastructure**: storageModule + configModule + networkModule
- **Domain**: authModule + settingsModule + dynamicUiModule

---

## Estado de la Documentacion

| Documento | Diagramas | Plataformas | Mejoras | Actualizado |
|-----------|-----------|-------------|---------|-------------|
| 01 Menu   | si        | si          | si      | Sprint 8    |
| 02 RBAC   | si        | si          | si      | Sprint 8    |
| 03 SDUI   | si        | si          | si      | Sprint 10   |
| 04 Acciones | si      | si          | si      | Sprint 9    |
| 05 Storage | si       | si          | si      | Sprint 9    |
| 06 Network | si       | si          | si      | Sprint 8    |
| 07 Offline | si       | si          | si      | Sprint 8    |
| 08 Arquitectura | si  | si          | si      | Sprint 9    |
| 09 Mejoras | si       | —           | si      | Sprint 10   |
| 10 i18n    | si       | —           | si      | Sprint 8    |
| 11 Conceptos | si     | —           | si      | Sprint 8    |
| 12 Select  | si       | —           | si      | Sprint 9    |
