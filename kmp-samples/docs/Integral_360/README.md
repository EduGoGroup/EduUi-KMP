# EduGo Integral 360 - Plan Maestro de Integración

## Resumen Ejecutivo

Este documento presenta la estrategia integral de integración del ecosistema EduGo, conectando el frontend KMP multiplataforma con los servicios backend (api-admin, api-mobile, worker) a través de un **sistema de configuración de UI dirigido por el backend**.

### Vista General del Ecosistema

```
+-------------------+     +---------------------+     +------------------+
|   KMP Frontend    |     |   edugo-api-mobile   |     |   edugo-worker   |
|  (Android, iOS,   |<--->|   (Materiales, Eval, |<--->|  (NLP, PDF,      |
|   Desktop, Web)   |     |    Progreso, Stats)  |     |   Generación)    |
+-------------------+     +---------------------+     +------------------+
        |                          |                          |
        |                 +---------------------+             |
        +---------------->|  edugo-api-admin    |<------------+
                          |  (Auth, RBAC, Menú,  |
                          |   Colegios, Usuarios,|
                          |   Config Pantallas)  |
                          |         NUEVO        |
                          +---------------------+
                                   |
                          +---------------------+
                          |  edugo-infrastructure|
                          |  (PostgreSQL, MongoDB|
                          |   RabbitMQ, Docker)  |
                          +---------------------+
                                   |
                          +---------------------+
                          |   edugo-shared       |
                          |  (Auth, Logger, DB,  |
                          |   Middleware, Events, |
                          |   ScreenConfig) NUEVO|
                          +---------------------+
```

### Decisiones Clave

| Decisión | Elección | Justificación |
|----------|----------|---------------|
| ¿Nueva API para config de pantallas? | **No** - Extender api-admin (escritura) + api-mobile (lectura) | Evita complejidad operacional; se alinea con la separación actual escritura/lectura |
| BD para definiciones de pantalla | **PostgreSQL** con schema `ui_config` + JSONB | Metadatos estructurados con contenido JSON flexible; schema separado para aislamiento |
| Enfoque de UI dinámica | **Híbrido** - Config del backend + renderers pre-construidos en KMP | Balance pragmático entre flexibilidad y velocidad de desarrollo |
| Relación Pantalla-Recurso | Tabla **resource_screens** vinculando recursos con templates de pantalla | Extiende naturalmente el sistema RBAC/menú existente |
| Formato de almacenamiento | **JSONB** en PostgreSQL | Flexibilidad de consultas, indexación, validación |

### Políticas de Desarrollo

| Política | Descripción |
|----------|-------------|
| **Manejo de ramas** | Feature branch `feature/dynamic-ui-phase1` desde `dev` en todos los repos |
| **Desarrollo local** | `go.work` actualizado para resolución de módulos cruzados sin releases |
| **Base de datos** | Recrear desde cero con FORCE_MIGRATION=true (no ALTER TABLE innecesarios) |
| **Código deprecado** | **Prohibido** - Se elimina lo que no se necesita, se adaptan consumidores |

### Resumen de Fases

| Fase | Enfoque | Pantallas | Estado |
|------|---------|-----------|--------|
| **Fase 1** | Base + Pantallas Principales | 5 pantallas (Login, Dashboard, Materiales Lista, Material Detalle, Configuración) | Planificación detallada (6 sprints) |
| **Fase 2** | Pantallas Restantes por Endpoint | 30 pantallas agrupadas por dominio | Planificación a nivel de tareas |

### Índice de Documentos

#### Arquitectura y Diseño
- [Decisiones de Arquitectura](./architecture-decisions.md) - ADRs clave con justificación
- [Diseño de Base de Datos](./database-design.md) - Schema `ui_config`, tablas, estructuras JSONB

#### Fase 1 - Base (Detallada)
- [Resumen Fase 1](./phase-1/README.md) - Resumen y dependencias
- [Cambios en Infraestructura](./phase-1/backend-infrastructure.md) - Migraciones, schema, Docker
- [Cambios en Módulo Shared](./phase-1/backend-shared.md) - DTOs, validación, tipos compartidos
- [Cambios en API Admin](./phase-1/backend-api-admin.md) - Endpoints de gestión de pantallas
- [Cambios en API Mobile](./phase-1/backend-api-mobile.md) - Endpoints de servicio de pantallas
- [Cambios en Frontend KMP](./phase-1/frontend-kmp.md) - Módulo dynamic-ui, renderers
- [Especificación Pantallas Principales](./phase-1/main-screens.md) - Las 5 pantallas con zones, slots y acciones

#### Fase 2 - Pantallas Restantes (Mapeo Endpoint → Pantalla)
- [Fase 2: Pantallas por Dominio](./phase-2/README.md) - 30 pantallas con endpoints, patterns y tareas por cada una

#### Ejecución
- [Plan de Trabajo](./work-plan.md) - Políticas de desarrollo, 6 sprints detallados, go.work, validación de BD
