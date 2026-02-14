# Plan de Trabajo - Cronograma de Ejecución Detallado

## Políticas de Desarrollo

### Manejo de Ramas

Todos los repos siguen el flujo: `feature/* → dev → main → release`.

| Repo | Rama actual | Rama `dev` existe | Acción necesaria |
|------|------------|-------------------|------------------|
| edugo-infrastructure | dev | Si | `git pull origin dev` + crear feature branch |
| edugo-shared | dev | Si | `git pull origin dev` + crear feature branch |
| edugo-api-administracion | dev | Si | `git pull origin dev` + crear feature branch |
| edugo-api-mobile | dev | Si | `git pull origin dev` + crear feature branch |
| edugo-dev-environment | dev | Si | `git pull origin dev` + crear feature branch |
| edugo-worker | dev | Si | Sin cambios en Fase 1 (solo validar compilación) |
| kmp_new | main | **No** | **Crear rama `dev` desde `main`** + crear feature branch |

**Nombre de rama**: `feature/dynamic-ui-phase1` (uniforme en todos los repos)

**Protocolo de primera ejecución**:
1. Verificar que `dev` está actualizado: `git checkout dev && git pull origin dev`
2. Crear feature branch: `git checkout -b feature/dynamic-ui-phase1`
3. Trabajar exclusivamente en la feature branch durante toda la Fase 1
4. Al completar un sprint: PR de `feature/dynamic-ui-phase1 → dev`

### Desarrollo Local con go.work

El archivo `go.work` en `/repos-separados/` permite desarrollo local sin necesidad de `go get` ni releases. Cualquier cambio en un módulo se refleja inmediatamente en los demás.

**go.work actual**:
```
go 1.25.3
use (
    ./edugo-api-administracion
    ./edugo-api-mobile
    ./edugo-infrastructure/postgres
    ./edugo-shared/auth
    ./edugo-shared/common
    ./edugo-shared/middleware/gin
)
```

**go.work actualizado** (Sprint 0):
```
go 1.25.3
use (
    ./edugo-api-administracion
    ./edugo-api-mobile
    ./edugo-infrastructure/postgres
    ./edugo-shared/auth
    ./edugo-shared/common
    ./edugo-shared/middleware/gin
    ./edugo-shared/screenconfig          // NUEVO: módulo de tipos compartidos
    ./edugo-dev-environment/migrator     // NUEVO: para usar infra local en migraciones
)
```

**Flujo de desarrollo local**:
- Docker: solo infraestructura (postgres, mongodb, rabbitmq, redis)
- Migrador: ejecutar localmente con `go run` desde `/repos-separados/` (go.work resuelve módulos locales)
- APIs: ejecutar localmente (go.work resuelve dependencias cruzadas)
- KMP: ejecutar con Gradle normalmente

### Validación de Base de Datos con dev-environment

El migrador en `edugo-dev-environment` usa `edugo-infrastructure/postgres/migrations` para crear la BD.

**Estructura actual de migraciones** (en edugo-infrastructure):
```
postgres/migrations/
├── structure/    → 000-015 (CREATE TABLE sin FK)
├── constraints/  → 001-015 (FK, índices, triggers)
├── seeds/        → 001-004 (datos del sistema: recursos, roles, permisos)
└── testing/      → 001-005 (datos demo: usuarios, colegios, materiales)
```

**Nuevos archivos que se agregarán**:
```
structure/016_create_screen_templates.sql
structure/017_create_screen_instances.sql
structure/018_create_resource_screens.sql
structure/019_create_screen_user_preferences.sql
constraints/016_screen_templates_constraints.sql
constraints/017_screen_instances_constraints.sql
constraints/018_resource_screens_constraints.sql
constraints/019_screen_user_preferences_constraints.sql
seeds/005_seed_screen_config_permissions.sql    → permisos RBAC para screen config
seeds/006_seed_screen_templates.sql            → 5 templates base (login, dashboard, list, detail, settings)
seeds/007_seed_screen_instances.sql            → instancias para las 5 pantallas principales
seeds/008_seed_resource_screens.sql            → mapeo recurso-pantalla
testing/006_demo_screen_config.sql             → datos de prueba adicionales
```

**Flujo de validación**:
1. Levantar Docker con infraestructura: `docker compose up -d` (postgres, mongodb, rabbitmq)
2. Levantar Redis si se necesita: `docker compose --profile with-redis up -d`
3. Ejecutar migrador localmente: `cd /repos-separados && go run ./edugo-dev-environment/migrator/cmd/main.go`
4. Si necesita recrear desde cero: `FORCE_MIGRATION=true go run ./edugo-dev-environment/migrator/cmd/main.go`
5. Verificar tablas y datos con `make psql` desde dev-environment

**Nota**: El migrador dentro de Docker usa versiones publicadas de infraestructura. Para desarrollo local usamos el migrador fuera de Docker con go.work. Al finalizar y hacer release, se actualiza el go.mod del migrador.

### Política de Código

- **No código deprecado**: Si algo cambia de enfoque, se elimina lo anterior y se adapta. No se dejan funciones, tipos o archivos marcados como "deprecated".
- **Consumidores se adaptan**: Si un cambio rompe un consumidor, se actualiza al nuevo esquema inmediatamente.
- **Eliminar, no comentar**: Código muerto se elimina, no se comenta.

---

## Fase 1: Fundación + Pantallas Principales

### Sprint 0: Preparación del Ambiente

**Objetivo**: Todos los repos listos para trabajar, ambiente de desarrollo configurado

#### Tareas

| # | Tarea | Proyecto | Depende De | Entregable |
|---|-------|----------|------------|------------|
| 0.1 | Actualizar `dev` en todos los repos Go (`git pull origin dev`) | all Go repos | - | dev actualizado |
| 0.2 | Crear rama `dev` en kmp_new desde `main` | kmp_new | - | rama dev creada |
| 0.3 | Crear rama `feature/dynamic-ui-phase1` en todos los repos | all | 0.1, 0.2 | feature branches creadas |
| 0.4 | Crear módulo `screenconfig` con `go.mod` inicial | edugo-shared | 0.3 | `go.mod` creado |
| 0.5 | Actualizar `go.work`: agregar `screenconfig` y `migrator` | repos-separados | 0.4 | go.work actualizado |
| 0.6 | Verificar Docker compose con profile `with-redis` | dev-environment | - | Redis verificado en puerto 6379 |
| 0.7 | Verificar que `FORCE_MIGRATION=true` recrea la BD correctamente | dev-environment | 0.5 | BD limpia confirmada |

---

### Sprint 1: Infraestructura y Fundación Compartida

**Objetivo**: Esquema de base de datos listo, tipos compartidos definidos, migrador validado

#### Tareas

| # | Tarea | Proyecto | Depende De | Entregable |
|---|-------|----------|------------|------------|
| 1.1 | Crear `structure/016_create_screen_templates.sql` | edugo-infrastructure | 0.3 | Tabla screen_templates |
| 1.2 | Crear `structure/017_create_screen_instances.sql` | edugo-infrastructure | 1.1 | Tabla screen_instances |
| 1.3 | Crear `structure/018_create_resource_screens.sql` | edugo-infrastructure | 1.2 | Tabla resource_screens |
| 1.4 | Crear `structure/019_create_screen_user_preferences.sql` | edugo-infrastructure | 1.2 | Tabla screen_user_preferences |
| 1.5 | Crear `constraints/016_screen_templates_constraints.sql` | edugo-infrastructure | 1.1 | Índices y triggers |
| 1.6 | Crear `constraints/017_screen_instances_constraints.sql` | edugo-infrastructure | 1.2 | FK, índices, triggers |
| 1.7 | Crear `constraints/018_resource_screens_constraints.sql` | edugo-infrastructure | 1.3 | FK, índices, unique |
| 1.8 | Crear `constraints/019_screen_user_preferences_constraints.sql` | edugo-infrastructure | 1.4 | FK, índices |
| 1.9 | Crear módulo `screenconfig`: tipos (Pattern, ScreenType, ActionType) | edugo-shared | 0.4 | types.go |
| 1.10 | Crear DTOs (ScreenTemplateDTO, ScreenInstanceDTO, CombinedScreenDTO, etc.) | edugo-shared | 1.9 | dto.go |
| 1.11 | Crear validación (ValidatePattern, ValidateTemplateDefinition, etc.) | edugo-shared | 1.9 | validation.go |
| 1.12 | Agregar permisos de screen config al enum de permisos | edugo-shared | 0.3 | Enum actualizado en common |
| 1.13 | Crear `seeds/005_seed_screen_config_permissions.sql` (permisos + recurso RBAC) | edugo-infrastructure | 1.3, 1.12 | Permisos RBAC |
| 1.14 | Crear `seeds/006_seed_screen_templates.sql` (5 templates base) | edugo-infrastructure | 1.1, 1.9 | Templates semilla |
| 1.15 | Crear `seeds/007_seed_screen_instances.sql` (5 pantallas principales) | edugo-infrastructure | 1.2, 1.14 | Instancias semilla |
| 1.16 | Crear `seeds/008_seed_resource_screens.sql` (mapeo recurso-pantalla) | edugo-infrastructure | 1.3, 1.15 | Mapeos semilla |
| 1.17 | Crear `testing/006_demo_screen_config.sql` (datos de prueba) | edugo-infrastructure | 1.15 | Datos testing |
| 1.18 | Actualizar migrador para manejar schema `ui_config` en FORCE_MIGRATION | edugo-dev-environment | 1.1 | Migrador actualizado |
| 1.19 | Ejecutar migrador local con FORCE_MIGRATION=true y validar todas las tablas | edugo-dev-environment | 1.1-1.18 | BD validada con datos |

**Tracks paralelos**:
- Track A (1.1-1.8, 1.13-1.19): Infraestructura + dev-environment
- Track B (1.9-1.12): Módulo compartido

**Validación del Sprint 1**:
- [ ] Todas las tablas del schema `ui_config` existen con datos semilla
- [ ] Los permisos RBAC para screen_config están asignados al rol super_admin
- [ ] El módulo `screenconfig` compila y los tipos se exportan correctamente
- [ ] Los DTOs serializan/deserializan correctamente con JSON
- [ ] La validación rechaza patterns y action types inválidos
- [ ] El migrador funciona con FORCE_MIGRATION=true (recrea todo limpio)

---

### Sprint 2: Endpoints de API del Backend

**Objetivo**: El administrador puede gestionar configuraciones de pantalla, Mobile puede servirlas

#### Tareas

| # | Tarea | Proyecto | Depende De | Entregable |
|---|-------|----------|------------|------------|
| 2.1 | Implementar `ScreenTemplateRepository` (PostgreSQL) | api-admin | 1.1, 1.9 | Repository |
| 2.2 | Implementar `ScreenInstanceRepository` (PostgreSQL) | api-admin | 1.2, 1.9 | Repository |
| 2.3 | Implementar `ResourceScreenRepository` (PostgreSQL) | api-admin | 1.3, 1.9 | Repository |
| 2.4 | Implementar `ScreenConfigService` (operaciones CRUD) | api-admin | 2.1-2.3 | Capa de servicio |
| 2.5 | Crear `ScreenConfigHandler` (handler HTTP) | api-admin | 2.4 | Handler REST |
| 2.6 | Registrar rutas bajo `/v1/screen-config/` con middleware de permisos | api-admin | 2.5 | Configuración de rutas |
| 2.7 | Mejorar `GET /v1/menu` para incluir mapeo de pantallas (`screens` field) | api-admin | 2.3 | Menú mejorado |
| 2.8 | Escribir pruebas de integración para CRUD de screen config | api-admin | 2.6 | Suite de pruebas |
| 2.9 | Implementar `ScreenRepository` (consulta JOIN: template + instancia) | api-mobile | 1.1, 1.2, 1.9 | Repository |
| 2.10 | Implementar `ScreenService` (resolución de slot, cache en memoria) | api-mobile | 2.9 | Capa de servicio |
| 2.11 | Crear `ScreenHandler` (handler HTTP) | api-mobile | 2.10 | Handler REST |
| 2.12 | Registrar rutas `/v1/screens/` | api-mobile | 2.11 | Configuración de rutas |
| 2.13 | Implementar cache ETag/304 para respuestas de pantallas | api-mobile | 2.11 | Capa de cache |
| 2.14 | Escribir pruebas de integración para servicio de pantallas | api-mobile | 2.12 | Suite de pruebas |
| 2.15 | Validación E2E: crear template en admin → servir en mobile | all | 2.8, 2.14 | Flujo validado |

**Tracks paralelos**:
- Track A (2.1-2.8): api-admin (CRUD de gestión)
- Track B (2.9-2.14): api-mobile (lectura optimizada)
- Punto de convergencia: 2.15

**Validación del Sprint 2**:
- [ ] CRUD completo funciona para templates, instancias y resource-screens
- [ ] El JSON de definición de template se valida al crear/actualizar
- [ ] Los permisos se aplican correctamente en todos los endpoints
- [ ] GET /v1/menu retorna los mapeos de pantalla
- [ ] GET /v1/screens/:screenKey retorna el template combinado con datos de slot
- [ ] ETag/304 funciona correctamente para requests repetidos
- [ ] Las pruebas de integración pasan en ambas APIs

---

### Sprint 3: Módulo de UI Dinámica KMP

**Objetivo**: El frontend puede cargar y renderizar pantallas desde la configuración del backend

#### Tareas

| # | Tarea | Proyecto | Depende De | Entregable |
|---|-------|----------|------------|------------|
| 3.1 | Crear esqueleto del módulo `modules/dynamic-ui` (build.gradle.kts, estructura) | kmp_new | 0.3 | Estructura del módulo |
| 3.2 | Implementar modelos serializables (ScreenDefinition, Zone, Slot, Action, etc.) | kmp_new | 3.1 | Modelos Kotlin |
| 3.3 | Escribir pruebas de serialización/deserialización de modelos | kmp_new | 3.2 | Suite de pruebas |
| 3.4 | Implementar `ScreenLoader` interfaz + `RemoteScreenLoader` | kmp_new | 3.2, 2.12 | Loader |
| 3.5 | Implementar `CachedScreenLoader` (decorator con SafeEduGoStorage) | kmp_new | 3.4 | Cache wrapper |
| 3.6 | Implementar `DataLoader` interfaz + `RemoteDataLoader` | kmp_new | 3.2 | Cargador de datos |
| 3.7 | Implementar `ActionHandler` interfaz + `ActionRegistry` | kmp_new | 3.2 | Sistema de acciones |
| 3.8 | Implementar handlers estándar (Navigate, NavigateBack, Refresh, APICall, SubmitForm, Confirm, Logout) | kmp_new | 3.7 | 7 handlers |
| 3.9 | Escribir pruebas de handlers de acciones | kmp_new | 3.8 | Suite de pruebas |
| 3.10 | Implementar `DynamicScreenViewModel` (ScreenState, DataState) | kmp_new | 3.4-3.8 | ViewModel |
| 3.11 | Escribir pruebas del ViewModel (gestión de estado, ciclo de vida) | kmp_new | 3.10 | Suite de pruebas |
| 3.12 | Agregar `dynamicUiModule` al DI de Koin | kmp_new | 3.4-3.10 | Registro en DI |

**Validación del Sprint 3**:
- [ ] Los modelos deserializan correctamente el JSON del endpoint `/v1/screens/:key`
- [ ] El loader con cache funciona (primera carga remota, segunda desde cache)
- [ ] Los 7 handlers de acciones están registrados y procesan sus tipos
- [ ] El ViewModel maneja correctamente los estados: Loading → Loaded → Error
- [ ] Todas las pruebas pasan en `desktopTest`
- [ ] El módulo compila en todas las plataformas

---

### Sprint 4: Pattern Renderers

**Objetivo**: Los 5 pattern principales se renderizan correctamente desde la configuración del backend

#### Tareas

| # | Tarea | Proyecto | Depende De | Entregable |
|---|-------|----------|------------|------------|
| 4.1 | Implementar `ZoneRenderer` composable (stacked, side-by-side, grid, flow-row) | kmp_new | 3.2 | Zone renderer |
| 4.2 | Implementar `SlotRenderer` composable (mapea controlType a componentes del Design System) | kmp_new | 3.2 | Slot renderer |
| 4.3 | Implementar `PatternRouter` composable (despacha por tipo de pattern) | kmp_new | 4.1-4.2 | Router |
| 4.4 | Implementar `LoginPatternRenderer` | kmp_new | 4.1-4.3 | Login renderer |
| 4.5 | Implementar `DashboardPatternRenderer` | kmp_new | 4.1-4.3 | Dashboard renderer |
| 4.6 | Implementar `ListPatternRenderer` | kmp_new | 4.1-4.3 | List renderer |
| 4.7 | Implementar `DetailPatternRenderer` | kmp_new | 4.1-4.3 | Detail renderer |
| 4.8 | Implementar `SettingsPatternRenderer` | kmp_new | 4.1-4.3 | Settings renderer |
| 4.9 | Implementar composable `DynamicScreen` (punto de entrada que conecta ViewModel + PatternRouter) | kmp_new | 3.10, 4.3 | Composable principal |
| 4.10 | Manejo de layouts específicos por plataforma (mobile vs desktop) | kmp_new | 4.1 | Lógica de plataforma |

**Validación del Sprint 4**:
- [ ] Cada pattern renderer renderiza correctamente desde JSON de prueba
- [ ] Los componentes del Design System se mapean correctamente desde controlType
- [ ] El layout se adapta según la plataforma (columna en mobile, split en desktop)
- [ ] Las acciones de los slots se disparan correctamente al interactuar
- [ ] Preview/pruebas visuales en al menos una plataforma

---

### Sprint 5: Integración de Pantallas Principales

**Objetivo**: 5 pantallas principales conectadas de extremo a extremo con datos reales del backend

#### Tareas

| # | Tarea | Proyecto | Depende De | Entregable |
|---|-------|----------|------------|------------|
| 5.1 | Integrar pantalla de Login con AuthService | kmp_new | 4.4 | Pantalla de login funcional |
| 5.2 | Integrar pantalla de Dashboard con datos de KPIs (GET /v1/stats/global) | kmp_new | 4.5 | Pantalla de dashboard funcional |
| 5.3 | Integrar Lista de Materiales con paginación (GET /v1/materials) | kmp_new | 4.6 | Lista de materiales funcional |
| 5.4 | Integrar Detalle de Material con datos reales (GET /v1/materials/{id}) | kmp_new | 4.7 | Detalle de material funcional |
| 5.5 | Integrar Configuración con ThemeService + preferencias locales | kmp_new | 4.8 | Pantalla de configuración funcional |
| 5.6 | Implementar navegación inferior desde configuración del backend (GET /v1/screens/navigation) | kmp_new | 3.4 | Navegación inferior |
| 5.7 | Conectar navegación entre pantallas (lista → detalle, dashboard → materiales, etc.) | kmp_new | 5.1-5.6 | Flujo de navegación completo |
| 5.8 | Probar en Desktop (plataforma más rápida para iterar) | kmp_new | 5.7 | Validación Desktop |
| 5.9 | Probar en emulador Android | kmp_new | 5.7 | Validación Android |
| 5.10 | Probar en WasmJS (navegador) | kmp_new | 5.7 | Validación Web |
| 5.11 | Prueba E2E del flujo completo: login → dashboard → materiales → detalle → configuración → logout | all | 5.7 | Validación E2E |

**Validación del Sprint 5**:
- [ ] Login funcional con credenciales de prueba
- [ ] Dashboard muestra KPIs reales del backend
- [ ] Lista de materiales con paginación, búsqueda y filtros
- [ ] Detalle de material muestra toda la información
- [ ] Configuración permite cambiar tema y ver datos del usuario
- [ ] Navegación inferior funciona entre las 3 pestañas
- [ ] Funciona en Desktop, Android y WasmJS
- [ ] El flujo E2E completo funciona sin errores

---

## Resumen de Hitos

| Hito | Sprint | Entregables Clave |
|------|--------|-------------------|
| **H0: Ambiente Listo** | Sprint 0 | Ramas creadas, go.work actualizado, Docker verificado |
| **H1: Esquema Listo** | Sprint 1 | Tablas PostgreSQL, migraciones, tipos compartidos, BD validada |
| **H2: APIs Listas** | Sprint 2 | CRUD de admin + endpoint de lectura de Mobile funcionando |
| **H3: Módulo Listo** | Sprint 3 | Módulo dynamic-ui con modelos, loaders, acciones, ViewModel |
| **H4: Renderers Listos** | Sprint 4 | Los 5 pattern renderers renderizando desde JSON |
| **H5: Fase 1 Completa** | Sprint 5 | 5 pantallas principales funcionando en todas las plataformas |

---

## Mitigación de Riesgos

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Rendimiento de consultas JSONB | Medio | Índices GIN, consulta JOIN optimizada, cache en memoria |
| Evolución del esquema JSON de template | Alto | Campo de versión en templates, cambios aditivos |
| Complejidad de layouts por plataforma | Medio | Comenzar mobile-first, agregar overrides incrementalmente |
| Latencia en carga de pantallas | Medio | Cache multicapa (Redis + memoria + SafeEduGoStorage) |
| go.work no funciona dentro de Docker | Bajo | Ejecutar migrador localmente, Docker solo para infraestructura |
| Diferencias de renderizado entre plataformas | Medio | Usar kmp-samples como referencia visual |

---

## Fase 2: Pantallas Restantes (Referencia)

Ver [phase-2/README.md](./phase-2/README.md) para el detalle de las 30 pantallas restantes organizadas por dominio, con el mapeo endpoint → pantalla y las tareas necesarias por cada una.

---

## Definición de Terminado (por tarea)

- [ ] El código compila en todas las plataformas objetivo
- [ ] Pruebas escritas y pasando
- [ ] Sin regresiones en pruebas existentes
- [ ] Sin código deprecado ni comentado
- [ ] Probado en al menos una plataforma objetivo
- [ ] Los commits siguen las convenciones del proyecto
