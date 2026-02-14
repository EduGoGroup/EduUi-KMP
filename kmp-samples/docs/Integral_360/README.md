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
                          |   Middleware, Events) |
                          +---------------------+
```

### Decisiones Clave Tomadas

| Decisión | Elección | Justificación |
|----------|----------|---------------|
| ¿Nueva API para config de pantallas? | **No** - Extender api-admin (escritura) + api-mobile (lectura) | Evita complejidad operacional; se alinea con la separación actual escritura/lectura |
| BD para definiciones de pantalla | **PostgreSQL** con nuevo schema `ui_config` + JSONB | Metadatos estructurados con contenido JSON flexible; schema separado para aislamiento limpio |
| Enfoque de UI dinámica | **Híbrido** - Config del backend + renderers pre-construidos en KMP | Balance pragmático entre flexibilidad y velocidad de desarrollo |
| Relación Pantalla-Recurso | Tabla **resource_screens** vinculando recursos con templates de pantalla | Extiende naturalmente el sistema RBAC/menú existente |
| Formato de almacenamiento | **JSONB** en PostgreSQL | Flexibilidad de consultas, indexación, validación; no necesita MongoDB |

### Resumen de Fases

| Fase | Enfoque | Alcance | Estado |
|------|---------|---------|--------|
| **Fase 1** | Base + Pantallas Principales | Schema backend, endpoints API, módulo dynamic-ui KMP, 5 pantallas principales | Planificación detallada |
| **Fase 2** | Expansión + Integración Completa | Todas las pantallas restantes según endpoints, funcionalidades avanzadas | Planificación alto nivel |

### Índice de Documentos

#### Arquitectura y Diseño
- [Decisiones de Arquitectura](./architecture-decisions.md) - ADRs clave con justificación
- [Diseño de Base de Datos](./database-design.md) - Nuevo schema `ui_config`, tablas, estructuras JSONB

#### Fase 1 - Base (Detallada)
- [Resumen Fase 1](./phase-1/README.md) - Resumen y dependencias
- [Cambios en Infraestructura](./phase-1/backend-infrastructure.md) - Migraciones, schema, Docker
- [Cambios en Módulo Shared](./phase-1/backend-shared.md) - DTOs, validación, tipos compartidos
- [Cambios en API Admin](./phase-1/backend-api-admin.md) - Endpoints de gestión de pantallas
- [Cambios en API Mobile](./phase-1/backend-api-mobile.md) - Endpoints de servicio de pantallas
- [Cambios en Frontend KMP](./phase-1/frontend-kmp.md) - Módulo dynamic-ui, renderers
- [Especificación Pantallas Principales](./phase-1/main-screens.md) - Login, Dashboard, Lista de Materiales, Detalle de Material, Configuración

#### Fase 2 - Expansión (Alto Nivel)
- [Resumen Fase 2](./phase-2/README.md) - Pantallas restantes y funcionalidades avanzadas

#### Ejecución
- [Plan de Trabajo](./work-plan.md) - Cronograma detallado, tareas, dependencias e hitos
