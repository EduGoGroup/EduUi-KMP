# Fase 2: Pantallas Restantes - Mapeo Endpoint → Pantalla

## Objetivo

Continuar el desarrollo de pantallas adicionales basándose en los endpoints que ya exponen api-mobile y api-admin. Para cada pantalla se identifica: qué endpoints la alimentan, qué datos semilla necesita (template, instancia, resource_screen), qué pattern renderer usa, y las tareas necesarias para implementarla.

> **Nivel de detalle**: Más que una deuda técnica, menos que una implementación línea por línea. Cada pantalla tiene su plan de tareas identificado para que se pueda estimar y ejecutar.

---

## A. Evaluaciones (api-mobile) - Prioridad Alta

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/materials/{id}/assessment` | GET | Obtener preguntas del cuestionario de un material |
| `/v1/materials/{id}/assessment/attempts` | POST | Enviar respuestas del intento |
| `/v1/attempts/{id}/results` | GET | Ver resultados de un intento |
| `/v1/users/me/attempts` | GET | Historial de intentos del usuario |

### Pantalla: `assessment-view`
- **Pattern**: `form`
- **Descripción**: Muestra preguntas de selección múltiple, permite responder y enviar
- **Data endpoint**: `GET /v1/materials/{id}/assessment`
- **Tareas**:
  1. Crear template de pattern `form` orientado a cuestionarios (zones: header, questions, actions)
  2. Crear instancia `assessment-view` con slot_data y field_mapping para preguntas
  3. Crear acción SUBMIT_FORM que llame a `POST /v1/materials/{id}/assessment/attempts`
  4. Crear acción onSuccess que navegue a `assessment-result`
  5. Insertar datos semilla (template + instancia + resource_screen)
  6. Validar renderizado en KMP con datos reales

### Pantalla: `assessment-result`
- **Pattern**: `detail`
- **Descripción**: Muestra puntuación, retroalimentación, respuestas correctas/incorrectas
- **Data endpoint**: `GET /v1/attempts/{id}/results`
- **Tareas**:
  1. Crear instancia `assessment-result` basada en template `detail`
  2. Configurar zones: score_hero, feedback, answers_list, actions
  3. Configurar field_mapping para mapear campos del response
  4. Crear acción NAVIGATE para volver al material o al historial
  5. Insertar datos semilla
  6. Validar renderizado

### Pantalla: `assessment-history`
- **Pattern**: `list`
- **Descripción**: Lista de intentos previos con puntuaciones y fechas
- **Data endpoint**: `GET /v1/users/me/attempts`
- **Tareas**:
  1. Crear instancia `assessment-history` basada en template `list`
  2. Configurar ItemLayout: fecha, material, puntuación, estado
  3. Configurar paginación (offset/limit)
  4. Crear acción item_click que navegue a `assessment-result`
  5. Insertar datos semilla
  6. Validar renderizado

---

## B. Progreso (api-mobile) - Prioridad Alta

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/progress` | PUT | Actualizar progreso de lectura |

### Endpoint nuevo necesario
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/users/me/progress` | GET | Resumen de progreso del usuario actual |

### Pantalla: `material-reader`
- **Pattern**: `detail` (variante personalizada)
- **Descripción**: Lector de documentos con barra de progreso y tracking automático
- **Data endpoint**: `GET /v1/materials/{id}` (existente) + `GET /v1/materials/{id}/download-url` para contenido
- **Tareas**:
  1. Definir variante de template `detail` para lectura (zones: reader_toolbar, content_area, progress_bar)
  2. Crear instancia `material-reader` con configuración de tracking
  3. Implementar acción local que llame `PUT /v1/progress` periódicamente
  4. Insertar datos semilla
  5. Validar en KMP (requiere componente de visualización de documentos)

### Pantalla: `progress-overview`
- **Pattern**: `dashboard`
- **Descripción**: Dashboard del estudiante con progreso general
- **Data endpoint**: `GET /v1/users/me/progress` (**nuevo**)
- **Tareas**:
  1. **Crear endpoint** `GET /v1/users/me/progress` en api-mobile (agrega datos de materiales leídos, evaluaciones completadas, porcentaje general)
  2. Crear instancia `progress-overview` basada en template `dashboard`
  3. Configurar zones: summary_kpis, materials_progress_list, recent_activity
  4. Insertar datos semilla
  5. Validar renderizado

---

## C. Gestión de Escuelas (api-admin) - Prioridad Media

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/schools` | GET | Listar escuelas |
| `/v1/schools` | POST | Crear escuela |
| `/v1/schools/:id` | GET | Detalle de escuela |
| `/v1/schools/:id` | PUT | Actualizar escuela |
| `/v1/schools/:id` | DELETE | Eliminar escuela |
| `/v1/schools/:id/units` | GET | Listar unidades académicas |
| `/v1/schools/:id/units` | POST | Crear unidad |
| `/v1/schools/:id/units/tree` | GET | Árbol jerárquico de unidades |

### Pantalla: `schools-list`
- **Pattern**: `list`
- **Data endpoint**: `GET /v1/schools`
- **Tareas**:
  1. Crear instancia con field_mapping para nombre, ubicación, nivel de suscripción, cantidad de estudiantes
  2. Configurar filtros por estado y nivel de suscripción
  3. Crear acción item_click → `school-detail`
  4. Crear acción para "Crear Escuela" → `school-create`
  5. Insertar datos semilla + vincular con resource_screens

### Pantalla: `school-detail`
- **Pattern**: `detail`
- **Data endpoint**: `GET /v1/schools/:id`
- **Tareas**:
  1. Crear instancia con zones: header, info, subscription, stats, units_preview, actions
  2. Configurar field_mapping completo
  3. Crear acciones: editar, ver unidades, eliminar (con confirmación)
  4. Insertar datos semilla

### Pantalla: `school-create`
- **Pattern**: `form`
- **Data endpoint**: `POST /v1/schools` / `PUT /v1/schools/:id`
- **Tareas**:
  1. Crear template `form` si no existe aún (o reusar si se creó en evaluaciones)
  2. Crear instancia con campos: nombre, código, dirección, nivel de suscripción, etc.
  3. Configurar validación de campos requeridos
  4. Crear acción SUBMIT_FORM con onSuccess → `school-detail`
  5. Insertar datos semilla

### Pantalla: `academic-units-tree`
- **Pattern**: `list` (variante árbol)
- **Data endpoint**: `GET /v1/schools/:id/units/tree`
- **Tareas**:
  1. Evaluar si el list renderer soporta datos jerárquicos o se necesita variante
  2. Crear instancia con ItemLayout que muestre nivel de anidación
  3. Configurar acciones de expandir/colapsar nodos
  4. Crear acción item_click → `academic-unit-detail`
  5. Insertar datos semilla

### Pantalla: `academic-unit-detail`
- **Pattern**: `detail`
- **Data endpoint**: `GET /v1/schools/:id/units/:unitId`
- **Tareas**:
  1. Crear instancia con zones: header, info, members_list, actions
  2. Configurar field_mapping
  3. Insertar datos semilla

### Pantalla: `academic-unit-create`
- **Pattern**: `form`
- **Data endpoint**: `POST /v1/schools/:id/units`
- **Tareas**:
  1. Crear instancia con campos: nombre, tipo, unidad padre (selector jerárquico)
  2. Configurar acción SUBMIT_FORM
  3. Insertar datos semilla

---

## D. Gestión de Usuarios (api-admin) - Prioridad Media

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/users` | GET | Listar usuarios (con filtros) |
| `/v1/users/:id` | GET | Detalle de usuario |
| `/v1/users/:id/roles` | GET/PUT | Roles del usuario |
| `/v1/memberships` | GET/POST/DELETE | Membresías |

### Pantalla: `users-list`
- **Pattern**: `list`
- **Data endpoint**: `GET /v1/users`
- **Tareas**:
  1. Crear instancia con field_mapping: nombre, email, rol, estado, último acceso
  2. Configurar filtros por rol y estado
  3. Configurar búsqueda por nombre/email
  4. Crear acciones: item_click → `user-detail`, crear usuario → `user-create`
  5. Insertar datos semilla

### Pantalla: `user-detail`
- **Pattern**: `profile`
- **Data endpoint**: `GET /v1/users/:id`
- **Tareas**:
  1. Evaluar si `profile` necesita template propio o es variante de `detail`
  2. Crear instancia con zones: avatar_header, info, roles, memberships, actions
  3. Configurar field_mapping
  4. Crear acciones: editar roles, gestionar membresías
  5. Insertar datos semilla

### Pantalla: `user-create`
- **Pattern**: `form`
- **Data endpoint**: `POST /v1/users`
- **Tareas**:
  1. Crear instancia con campos: nombre, apellido, email, rol, escuela
  2. Configurar validación
  3. Crear acción SUBMIT_FORM
  4. Insertar datos semilla

### Pantalla: `user-roles`
- **Pattern**: `settings`
- **Data endpoint**: `GET /v1/users/:id/roles`
- **Tareas**:
  1. Crear instancia con lista de roles disponibles (switches/checkboxes)
  2. Configurar acción de guardar cambios → `PUT /v1/users/:id/roles`
  3. Insertar datos semilla

### Pantalla: `memberships-list`
- **Pattern**: `list`
- **Data endpoint**: `GET /v1/memberships`
- **Tareas**:
  1. Crear instancia filtrada por unidad académica
  2. Configurar acciones: agregar miembro, eliminar miembro
  3. Insertar datos semilla

---

## E. Gestión de Permisos (api-admin) - Prioridad Baja

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/roles` | GET | Listar roles |
| `/v1/permissions` | GET | Listar permisos |
| `/v1/resources` | GET | Listar recursos |

### Pantalla: `roles-list`
- **Pattern**: `list`
- **Tareas**: Crear instancia con field_mapping para roles, insertar datos semilla

### Pantalla: `role-detail`
- **Pattern**: `detail`
- **Tareas**: Crear instancia con zones para permisos del rol, insertar datos semilla

### Pantalla: `permissions-matrix`
- **Pattern**: `dashboard` (variante personalizada)
- **Tareas**: Evaluar si se necesita un pattern renderer especial para grilla roles × recursos. Si es muy complejo, posponer para Fase 3.

---

## F. Relaciones de Acudientes (api-admin) - Prioridad Media

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/guardian-relations` | CRUD | Gestionar relaciones |
| `/v1/guardians/:id/relations` | GET | Relaciones de un acudiente |
| `/v1/students/:id/guardians` | GET | Acudientes de un estudiante |

### Pantalla: `guardian-students`
- **Pattern**: `list`
- **Tareas**: Crear instancia, configurar field_mapping, insertar datos semilla

### Pantalla: `student-guardians`
- **Pattern**: `list`
- **Tareas**: Crear instancia, configurar field_mapping, insertar datos semilla

### Pantalla: `guardian-link`
- **Pattern**: `form`
- **Tareas**: Crear instancia con selector de acudiente y estudiante, configurar SUBMIT_FORM

---

## G. Gestión de Materias (api-admin) - Prioridad Baja

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/subjects` | CRUD | Gestionar materias |

### Pantalla: `subjects-list`
- **Pattern**: `list`
- **Tareas**: Crear instancia, insertar datos semilla

### Pantalla: `subject-create`
- **Pattern**: `form`
- **Tareas**: Crear instancia con campos de materia, SUBMIT_FORM

---

## H. Estadísticas (api-mobile) - Prioridad Media

### Endpoints existentes
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/v1/materials/{id}/stats` | GET | Estadísticas por material |
| `/v1/stats/global` | GET | Estadísticas globales |

### Pantalla: `material-stats`
- **Pattern**: `dashboard`
- **Data endpoint**: `GET /v1/materials/{id}/stats`
- **Tareas**:
  1. Crear instancia con zones: kpis (vistas, completitud, puntuación promedio), chart_area, details
  2. Insertar datos semilla

### Pantalla: `global-stats`
- **Pattern**: `dashboard`
- **Data endpoint**: `GET /v1/stats/global`
- **Tareas**:
  1. Crear instancia con zones más amplias que el dashboard de Fase 1 (más métricas, más detalle)
  2. Evaluar si se reusan datos de `dashboard-teacher` o se crean separados
  3. Insertar datos semilla

---

## I. Notificaciones - Prioridad Media

### Endpoints nuevos necesarios
| Endpoint | Método | API | Descripción |
|----------|--------|-----|-------------|
| `/v1/notifications` | GET | api-mobile | Lista de notificaciones del usuario |
| `/v1/notifications/:id/read` | PUT | api-mobile | Marcar como leída |

### Pantalla: `notifications-list`
- **Pattern**: `notification` (patrón nuevo)
- **Tareas**:
  1. Evaluar si se necesita un pattern renderer `notification` o se puede resolver con `list` + estilos
  2. **Crear endpoint** `GET /v1/notifications` en api-mobile
  3. **Crear endpoint** `PUT /v1/notifications/:id/read` en api-mobile
  4. Crear tabla de notificaciones en infrastructure (si no existe)
  5. Crear template e instancia
  6. Insertar datos semilla

### Pantalla: `notification-settings`
- **Pattern**: `settings`
- **Tareas**:
  1. Crear instancia con switches para preferencias de notificación
  2. Configurar acciones locales para guardar preferencias
  3. Insertar datos semilla

---

## J. Onboarding - Prioridad Baja

### Pantalla: `onboarding-flow`
- **Pattern**: `onboarding` (patrón nuevo)
- **Tareas**:
  1. Evaluar si se necesita un pattern renderer `onboarding` (slides paginados)
  2. Diseñar template con zones por paso
  3. Implementar renderer con soporte de paginación horizontal
  4. Crear instancia con contenido de bienvenida
  5. Insertar datos semilla

---

## K. Búsqueda - Prioridad Media

### Endpoint nuevo necesario
| Endpoint | Método | API | Descripción |
|----------|--------|-----|-------------|
| `/v1/search?q=term` | GET | api-mobile | Búsqueda global |

### Pantalla: `global-search`
- **Pattern**: `search` (patrón nuevo)
- **Tareas**:
  1. Evaluar si se necesita un pattern renderer `search` o se puede resolver con `list` + barra de búsqueda prominente
  2. **Crear endpoint** `GET /v1/search` en api-mobile (busca en materiales, usuarios, escuelas)
  3. Crear template e instancia
  4. Configurar resultados agrupados por tipo de entidad
  5. Insertar datos semilla

---

## Resumen de Esfuerzo por Dominio

| Dominio | Pantallas | Endpoint nuevo? | Pattern nuevo? | Esfuerzo estimado |
|---------|-----------|-----------------|----------------|-------------------|
| Evaluaciones | 3 | No | No (usa form, detail, list) | 2-3 semanas |
| Progreso | 2 | Si (1 endpoint) | No (usa detail, dashboard) | 1-2 semanas |
| Escuelas | 6 | No | Posible (árbol) | 3-4 semanas |
| Usuarios | 5 | No | Posible (profile) | 2-3 semanas |
| Permisos | 3 | No | Posible (matrix) | 1-2 semanas |
| Acudientes | 3 | No | No | 1-2 semanas |
| Materias | 2 | No | No | 1 semana |
| Estadísticas | 2 | No | No (usa dashboard) | 1-2 semanas |
| Notificaciones | 2 | Si (2 endpoints + tabla) | Posible (notification) | 2-3 semanas |
| Onboarding | 1 | No | Si (onboarding) | 1-2 semanas |
| Búsqueda | 1 | Si (1 endpoint) | Posible (search) | 1-2 semanas |
| **Total** | **30** | **4 endpoints nuevos** | **3-5 patterns nuevos** | **~17-26 semanas** |

---

## Nuevos Patterns a Evaluar en Fase 2

Los patterns base de Fase 1 (login, dashboard, list, detail, settings) cubren la mayoría. Estos patterns adicionales necesitan evaluación:

| Pattern | Pantallas que lo usan | Decisión |
|---------|----------------------|----------|
| `form` | assessment-view, school-create, academic-unit-create, user-create, guardian-link, subject-create | **Necesario** - Fase 1 no incluye form renderer |
| `profile` | user-detail | Evaluar si `detail` con variante es suficiente |
| `notification` | notifications-list | Evaluar si `list` con estilos es suficiente |
| `onboarding` | onboarding-flow | Necesario si se implementa onboarding |
| `search` | global-search | Evaluar si `list` + search bar prominente es suficiente |
| `tree` | academic-units-tree | Evaluar si `list` con indentación es suficiente |

> **Nota importante**: El pattern `form` es el más crítico de Fase 2. Se recomienda implementarlo temprano ya que 6 pantallas lo necesitan.

---

## Orden de Implementación Sugerido

| Prioridad | Sprint | Dominio | Pantallas | Justificación |
|-----------|--------|---------|-----------|---------------|
| 1 | Sprint 6 | Evaluaciones | 3 | Flujo core del estudiante, endpoints existentes |
| 2 | Sprint 7 | Progreso + Estadísticas | 4 | Complementa evaluaciones, 1 endpoint nuevo |
| 3 | Sprint 8 | Escuelas | 6 | Admin, endpoints existentes, introduce pattern árbol |
| 4 | Sprint 9 | Usuarios + Acudientes | 8 | Admin, endpoints existentes |
| 5 | Sprint 10 | Notificaciones + Búsqueda | 3 | Requieren endpoints nuevos |
| 6 | Sprint 11 | Materias + Permisos + Onboarding | 6 | Baja prioridad, patrones nuevos |

---

## Prerrequisitos de Fase 1

Para iniciar Fase 2 se necesita que esté completo:
- [ ] Los 5 pattern renderers de Fase 1 funcionando
- [ ] El sistema de acciones procesando los 7 tipos estándar
- [ ] El flujo template → instancia → frontend validado end-to-end
- [ ] El patrón de inserción de datos semilla establecido
- [ ] El DynamicScreenViewModel estable y probado
