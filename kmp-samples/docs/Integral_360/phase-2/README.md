# Fase 2: Expansión + Integración Completa (Alto Nivel)

## Objetivo

Entregar todas las pantallas restantes basadas en los endpoint del backend expuestos e implementar funcionalidades avanzadas como soporte offline, actualizaciones en tiempo real y analíticas.

> **Nota**: Esta fase está definida a alto nivel. Las especificaciones detalladas se crearán cuando la Fase 1 esté completa y validada.

---

## Pantallas Restantes por Dominio del Backend

### A. Módulo de Evaluaciones (api-mobile)

Basado en endpoints:
- `GET /v1/materials/{id}/assessment` - Ver preguntas del cuestionario
- `POST /v1/materials/{id}/assessment/attempts` - Enviar respuestas
- `GET /v1/attempts/{id}/results` - Ver resultados
- `GET /v1/users/me/attempts` - Historial de intentos

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `assessment-view` | form | Mostrar preguntas del cuestionario con opciones de selección múltiple |
| `assessment-result` | detail | Mostrar puntuación, retroalimentación, respuestas correctas |
| `assessment-history` | list | Intentos previos del estudiante con puntuaciones |

### B. Módulo de Progreso (api-mobile)

Basado en endpoints:
- `PUT /v1/progress` - Actualizar progreso de lectura

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `material-reader` | detail (personalizado) | Lector de documentos con seguimiento de progreso |
| `progress-overview` | dashboard | Progreso general del estudiante a través de los materiales |

### C. Gestión de Escuelas (api-admin)

Basado en endpoints:
- CRUD `/v1/schools`
- CRUD `/v1/schools/:id/units`
- Tree `/v1/schools/:id/units/tree`

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `schools-list` | list | Vista de administrador de todas las escuelas |
| `school-detail` | detail | Información de la escuela, nivel de suscripción, estadísticas |
| `school-create` | form | Formulario para crear/editar escuela |
| `academic-units-tree` | list (variante árbol) | Vista jerárquica de unidades |
| `academic-unit-detail` | detail | Información de la unidad con miembros |
| `academic-unit-create` | form | Formulario para crear/editar unidad académica |

### D. Gestión de Usuarios (api-admin)

Basado en endpoints:
- CRUD usuarios
- Asignación de roles `/v1/users/:id/roles`
- Membresías `/v1/memberships`

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `users-list` | list | Lista de administrador de usuarios con filtros |
| `user-detail` | profile | Perfil de usuario con roles y membresías |
| `user-create` | form | Formulario para crear/invitar usuario |
| `user-roles` | settings | Gestionar asignaciones de roles del usuario |
| `memberships-list` | list | Ver todas las membresías de una unidad |

### E. Gestión de Permisos (api-admin)

Basado en endpoints:
- GET `/v1/roles`, `/v1/permissions`, `/v1/resources`

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `roles-list` | list | Ver todos los roles |
| `role-detail` | detail | Rol con sus permisos |
| `permissions-matrix` | dashboard (personalizado) | Grilla de permisos: roles vs recursos |

### F. Relaciones de Acudientes (api-admin)

Basado en endpoints:
- CRUD `/v1/guardian-relations`
- GET `/v1/guardians/:id/relations`
- GET `/v1/students/:id/guardians`

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `guardian-students` | list | Estudiantes vinculados al acudiente |
| `student-guardians` | list | Acudientes del estudiante |
| `guardian-link` | form | Vincular acudiente a estudiante |

### G. Gestión de Materias (api-admin)

Basado en endpoints:
- CRUD `/v1/subjects`

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `subjects-list` | list | Todas las materias/cursos |
| `subject-create` | form | Crear/editar materia |

### H. Estadísticas y Analíticas (api-mobile)

Basado en endpoints:
- `GET /v1/materials/{id}/stats` - Estadísticas del material
- `GET /v1/stats/global` - Estadísticas globales

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `material-stats` | dashboard | Analíticas por material (vistas, completitud, puntuaciones) |
| `global-stats` | dashboard | Dashboard de analíticas de todo el sistema |

### I. Notificaciones

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `notifications-list` | notification | Notificaciones del usuario con estado leído/no leído |
| `notification-settings` | settings | Preferencias de notificaciones |

### J. Onboarding

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `onboarding-flow` | onboarding | Recorrido guiado para usuarios nuevos |

### K. Búsqueda

| Pantalla | Pattern | Descripción |
|----------|---------|-------------|
| `global-search` | search | Búsqueda transversal (materiales, usuarios, escuelas) |

---

## Resumen de Cantidad de Pantallas

| Dominio | Pantallas | Prioridad |
|---------|-----------|-----------|
| Evaluaciones | 3 | Alta |
| Progreso | 2 | Alta |
| Gestión de Escuelas | 6 | Media |
| Gestión de Usuarios | 5 | Media |
| Gestión de Permisos | 3 | Baja |
| Relaciones de Acudientes | 3 | Media |
| Gestión de Materias | 2 | Baja |
| Estadísticas | 2 | Media |
| Notificaciones | 2 | Media |
| Onboarding | 1 | Baja |
| Búsqueda | 1 | Media |
| **Total** | **30** | |

---

## Funcionalidades Avanzadas (Fase 2+)

### Soporte Offline
- Almacenar en cache las definiciones de pantallas en SafeEduGoStorage (ya incluido en Fase 1)
- Almacenar en cache las respuestas de datos para lectura offline
- Encolar operaciones de escritura (envío de formularios, actualizaciones de progreso) para sincronizar al estar en línea
- Resolución de conflictos para ediciones offline

### Actualizaciones en Tiempo Real
- Conexión WebSocket para notificaciones
- Server-Sent Events para actualizaciones de KPIs en el dashboard
- Notificaciones push vía FCM (Android) y APNs (iOS)

### Pruebas A/B
- Múltiples versiones de template por pantalla
- El backend controla qué versión servir por segmento de usuario
- Seguimiento de analíticas para comparación de conversión

### Analíticas y Telemetría
- Seguimiento de vistas de pantalla
- Seguimiento de interacción con acciones
- Métricas de rendimiento (tiempos de carga, tiempos de renderizado)
- Seguimiento y reporte de errores

### Internacionalización
- slot_data multi-idioma por locale
- El backend sirve etiquetas localizadas según la preferencia de idioma del usuario
- Soporte RTL para árabe/hebreo si es necesario

### Accesibilidad
- Todos los pattern renderer cumplen con WCAG 2.1 AA
- Anotaciones para lectores de pantalla desde metadatos de slot
- Soporte de tamaño de texto dinámico
- Soporte de modo de alto contraste

---

## Nuevos Endpoint del Backend Necesarios (Fase 2)

### api-mobile (nuevos endpoints)
| Endpoint | Propósito |
|----------|-----------|
| GET `/v1/dashboard/activity` | Feed de actividad para el dashboard |
| GET `/v1/users/me/progress` | Resumen de progreso del usuario actual |
| GET `/v1/notifications` | Lista de notificaciones del usuario |
| PUT `/v1/notifications/:id/read` | Marcar notificación como leída |
| GET `/v1/search?q=term` | Búsqueda global a través de entidades |

### api-admin (nuevos endpoints)
| Endpoint | Propósito |
|----------|-----------|
| GET `/v1/stats/schools/:id` | Estadísticas a nivel de escuela |
| GET `/v1/stats/units/:id` | Estadísticas a nivel de unidad |
| POST `/v1/users/invite` | Invitar usuario por correo electrónico |

### Worker (nuevos tipos de evento)
| Evento | Propósito |
|--------|-----------|
| `notification.created` | Encolar notificación para entrega |
| `progress.milestone` | Disparar notificación al alcanzar un hito de progreso |

---

## Esfuerzo Estimado

| Área | Esfuerzo | Dependencias |
|------|----------|--------------|
| Pantallas de evaluaciones | 2-3 semanas | Renderer de la Fase 1 |
| Pantallas de progreso | 1-2 semanas | Renderer de la Fase 1 |
| Gestión de escuelas/usuarios | 3-4 semanas | Renderer de la Fase 1 + nuevos endpoint de admin |
| Dashboards de estadísticas | 2 semanas | Dashboard renderer de la Fase 1 |
| Notificaciones | 2 semanas | Nuevos endpoint de notificaciones |
| Soporte offline | 2-3 semanas | Infraestructura de cache de la Fase 1 |
| Búsqueda | 1-2 semanas | Nuevo endpoint de búsqueda |
| Pantallas restantes | 2-3 semanas | Todo lo anterior |
| **Total Fase 2** | **~15-20 semanas** | Fase 1 completa |
