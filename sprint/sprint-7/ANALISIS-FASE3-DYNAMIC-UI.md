# ANALISIS COMPLETO - Dynamic UI Fase 3: EduGo

**Fecha**: 2026-02-18
**Estado**: Analisis completado, listo para ejecucion

## A. MAPA DE ENDPOINTS POR API

### Mobile API (localhost:9091) - Operaciones frecuentes

| Dominio | Metodo | Endpoint | Descripcion |
|---------|--------|----------|-------------|
| **Health** | GET | `/health` | Health check del sistema |
| **Materials** | GET | `/v1/materials` | Listar todos los materiales |
| | POST | `/v1/materials` | Crear nuevo material |
| | GET | `/v1/materials/{id}` | Obtener material por ID |
| | PUT | `/v1/materials/{id}` | Actualizar material |
| | GET | `/v1/materials/{id}/summary` | Resumen IA del material |
| | GET | `/v1/materials/{id}/download-url` | URL presigned de descarga |
| | POST | `/v1/materials/{id}/upload-url` | URL presigned de upload |
| | POST | `/v1/materials/{id}/upload-complete` | Notificar upload completado |
| | GET | `/v1/materials/{id}/versions` | Historial de versiones |
| **Evaluaciones** | GET | `/v1/materials/{id}/assessment` | Obtener cuestionario (sin respuestas) |
| | POST | `/v1/materials/{id}/assessment/attempts` | Crear intento y obtener calificacion |
| | GET | `/v1/attempts/{id}/results` | Resultados de un intento |
| | GET | `/v1/users/me/attempts` | Historial de intentos del usuario |
| **Progress** | PUT | `/v1/progress` | Upsert progreso (idempotente) |
| **Stats** | GET | `/v1/stats/global` | Estadisticas globales |
| | GET | `/v1/materials/{id}/stats` | Estadisticas de un material |

**Total Mobile API: 17 endpoints**

---

### Admin API (localhost:8081) - CRUD y Administracion

| Dominio | Metodo | Endpoint | Descripcion |
|---------|--------|----------|-------------|
| **Auth** | POST | `/v1/auth/login` | Login |
| | POST | `/v1/auth/logout` | Logout |
| | POST | `/v1/auth/refresh` | Refrescar token |
| | POST | `/v1/auth/verify` | Verificar token |
| | POST | `/v1/auth/verify-bulk` | Verificar multiples tokens |
| | POST | `/v1/auth/switch-context` | Cambiar contexto escuela |
| **Users** | POST | `/v1/users` | Crear usuario |
| | GET | `/v1/users/{id}` | Obtener usuario |
| | PATCH | `/v1/users/{id}` | Actualizar usuario |
| | DELETE | `/v1/users/{id}` | Eliminar usuario |
| | GET | `/v1/users/{user_id}/memberships` | Memberships de un usuario |
| | GET | `/v1/users/{user_id}/roles` | Roles de un usuario |
| | POST | `/v1/users/{user_id}/roles` | Asignar rol a usuario |
| | DELETE | `/v1/users/{user_id}/roles/{role_id}` | Revocar rol de usuario |
| **Schools** | GET | `/v1/schools` | Listar escuelas |
| | POST | `/v1/schools` | Crear escuela |
| | GET | `/v1/schools/{id}` | Obtener escuela |
| | GET | `/v1/schools/code/{code}` | Obtener escuela por codigo |
| | PUT | `/v1/schools/{id}` | Actualizar escuela |
| | DELETE | `/v1/schools/{id}` | Eliminar escuela |
| **Academic Units** | GET | `/v1/schools/{schoolId}/units` | Listar unidades de escuela |
| | POST | `/v1/schools/{schoolId}/units` | Crear unidad academica |
| | GET | `/v1/schools/{schoolId}/units/by-type` | Listar unidades por tipo |
| | GET | `/v1/schools/{schoolId}/units/tree` | Arbol jerarquico de unidades |
| | GET | `/v1/units/{id}` | Obtener unidad |
| | PUT | `/v1/units/{id}` | Actualizar unidad |
| | DELETE | `/v1/units/{id}` | Eliminar unidad |
| | PATCH | `/v1/units/{id}` | Update parcial de unidad |
| | GET | `/v1/units/{id}/hierarchy-path` | Ruta jerarquica de unidad |
| | POST | `/v1/units/{id}/members` | Asignar miembro a unidad |
| | POST | `/v1/units/{id}/restore` | Restaurar unidad eliminada |
| **Memberships** | POST | `/v1/memberships` | Crear membership |
| | GET | `/v1/memberships/{id}` | Obtener membership |
| | PUT | `/v1/memberships/{id}` | Actualizar membership |
| | DELETE | `/v1/memberships/{id}` | Eliminar membership |
| | POST | `/v1/memberships/{id}/expire` | Expirar membership |
| | GET | `/v1/units/{unitId}/memberships` | Listar memberships de unidad |
| | GET | `/v1/units/{unitId}/memberships/by-role` | Memberships por rol |
| **Guardians** | POST | `/v1/guardian-relations` | Crear relacion guardian-estudiante |
| | GET | `/v1/guardian-relations/{id}` | Obtener relacion |
| | PUT | `/v1/guardian-relations/{id}` | Actualizar relacion |
| | DELETE | `/v1/guardian-relations/{id}` | Eliminar relacion |
| | GET | `/v1/guardians/{guardian_id}/relations` | Relaciones de un guardian |
| | GET | `/v1/students/{student_id}/guardians` | Guardianes de un estudiante |
| **Materials** | DELETE | `/v1/materials/{id}` | Eliminar material |
| **Subjects** | GET | `/v1/subjects` | Listar materias |
| | POST | `/v1/subjects` | Crear materia |
| | GET | `/v1/subjects/{id}` | Obtener materia |
| | PATCH | `/v1/subjects/{id}` | Actualizar materia |
| | DELETE | `/v1/subjects/{id}` | Eliminar materia |
| **Roles** | GET | `/v1/roles` | Listar roles |
| | GET | `/v1/roles/{id}` | Obtener rol |
| | GET | `/v1/roles/{id}/permissions` | Permisos de un rol |
| **Permissions** | GET | `/v1/permissions` | Listar permisos |
| | GET | `/v1/permissions/{id}` | Obtener permiso |
| **Resources** | GET | `/v1/resources` | Listar resources |
| | POST | `/v1/resources` | Crear resource |
| | GET | `/v1/resources/{id}` | Obtener resource |
| | PUT | `/v1/resources/{id}` | Actualizar resource |
| **Menu** | GET | `/v1/menu` | Menu del usuario autenticado |
| | GET | `/v1/menu/full` | Menu completo |
| **Screen Config** | GET | `/v1/screen-config/templates` | Listar templates |
| | POST | `/v1/screen-config/templates` | Crear template |
| | GET | `/v1/screen-config/templates/{id}` | Obtener template |
| | PUT | `/v1/screen-config/templates/{id}` | Actualizar template |
| | DELETE | `/v1/screen-config/templates/{id}` | Eliminar template |
| | GET | `/v1/screen-config/instances` | Listar instancias |
| | POST | `/v1/screen-config/instances` | Crear instancia |
| | GET | `/v1/screen-config/instances/{id}` | Obtener instancia |
| | GET | `/v1/screen-config/instances/key/{key}` | Obtener instancia por key |
| | PUT | `/v1/screen-config/instances/{id}` | Actualizar instancia |
| | DELETE | `/v1/screen-config/instances/{id}` | Eliminar instancia |
| | POST | `/v1/screen-config/resource-screens` | Vincular pantalla a recurso |
| | DELETE | `/v1/screen-config/resource-screens/{id}` | Desvincular pantalla |
| | GET | `/v1/screen-config/resource-screens/{resourceId}` | Pantallas de un recurso |
| **Stats** | GET | `/v1/stats/global` | Estadisticas globales |

**Total Admin API: 69 endpoints**

**Total combinado: 86 endpoints**

---

## B. MENU POR TIPO DE USUARIO

### Arbol de recursos RBAC (jerarquia)

```
dashboard              (system)  -- Panel principal
admin                  (system)  -- Administracion
  |-- users            (school)  -- Gestion de usuarios
  |-- schools          (system)  -- Gestion de escuelas
  |-- roles            (system)  -- Gestion de roles
  |-- permissions_mgmt (system)  -- Gestion de permisos
academic               (school)  -- Academico
  |-- units            (school)  -- Unidades academicas
  |-- memberships      (school)  -- Miembros
content                (unit)    -- Contenido
  |-- materials        (unit)    -- Materiales educativos
  |-- assessments      (unit)    -- Evaluaciones
reports                (school)  -- Reportes
  |-- progress         (unit)    -- Progreso
  |-- stats            (school)  -- Estadisticas
```

---

### 1. SUPER_ADMIN
**Permisos**: TODOS (full access)

**Menu:**
- Dashboard (stats globales de toda la plataforma)
- Administracion
  - Usuarios (CRUD completo)
  - Escuelas (CRUD completo)
  - Roles (ver)
  - Permisos (ver/editar)
- Academico
  - Unidades Academicas (CRUD)
  - Miembros (gestion)
- Contenido
  - Materiales (CRUD + publicar + descargar)
  - Evaluaciones (CRUD + publicar + calificar)
- Reportes
  - Progreso (ver/actualizar)
  - Estadisticas (global, school, unit)
- Configuracion (settings)

**Endpoints consumidos:**
- `GET /v1/stats/global` (Admin API)
- `GET/POST/PATCH/DELETE /v1/users/*`
- `GET/POST/PUT/DELETE /v1/schools/*`
- `GET /v1/roles/*`, `GET /v1/permissions/*`
- `GET/POST/PUT/DELETE /v1/schools/{schoolId}/units/*`, `/v1/units/*`
- `GET/POST/PUT/DELETE /v1/memberships/*`
- `GET/POST/PUT /v1/materials` (Mobile) + `DELETE /v1/materials/{id}` (Admin)
- Todos los endpoints de evaluaciones
- `GET /v1/resources/*`, `PUT /v1/resources/*`
- `GET /v1/menu`, `GET /v1/menu/full`

---

### 2. SCHOOL_ADMIN
**Permisos**: users:read, users:update, schools:read, schools:update, schools:manage, units:*, materials:read/update/delete, assessments:read/update/delete, progress:read/update, stats:school

**Menu:**
- Dashboard (stats de escuela)
- Administracion
  - Usuarios (ver/editar, scope escuela)
- Academico
  - Unidades Academicas (CRUD)
  - Miembros (gestion)
- Contenido
  - Materiales (ver/editar/eliminar)
  - Evaluaciones (ver/editar/eliminar)
- Reportes
  - Progreso (ver/actualizar)
  - Estadisticas (de escuela)
- Configuracion

**Endpoints consumidos:**
- `GET /v1/stats/global` (filtrado por school)
- `GET/PATCH /v1/users/*`
- `GET/PUT /v1/schools/{id}`
- `GET/POST/PUT/DELETE /v1/schools/{schoolId}/units/*`
- `GET/POST/PUT/DELETE /v1/memberships/*`
- `GET /v1/materials` (Mobile), `DELETE /v1/materials/{id}` (Admin)
- `GET /v1/materials/{id}/assessment`, attempts
- `PUT /v1/progress`

---

### 3. TEACHER
**Permisos**: users:read:own, users:update:own, units:read, materials:create/read/update/publish/download, assessments:create/read/update/publish/grade, progress:read/update, stats:unit

**Menu:**
- Dashboard (stats de clase/unidad)
- Contenido
  - Materiales (crear/ver/editar/publicar/descargar)
  - Evaluaciones (crear/ver/editar/publicar/calificar)
- Reportes
  - Progreso (ver estudiantes, actualizar)
  - Estadisticas (de unidad)
- Configuracion

**Endpoints consumidos:**
- `GET /v1/stats/global` (filtrado por unit) o nuevo endpoint stats/unit
- `GET/POST/PUT /v1/materials` (Mobile)
- `POST /v1/materials/{id}/upload-url`, `POST /v1/materials/{id}/upload-complete`
- `GET /v1/materials/{id}/download-url`
- `GET /v1/materials/{id}/summary`
- `GET /v1/materials/{id}/stats`
- `GET /v1/materials/{id}/assessment`, `POST .../attempts` (para testing)
- `PUT /v1/progress`
- `GET /v1/units/{id}` (ver su unidad)

---

### 4. STUDENT
**Permisos**: users:read:own, users:update:own, materials:read/download, assessments:read/attempt/view_results, progress:read:own

**Menu:**
- Dashboard (mi progreso, cursos inscritos)
- Contenido
  - Materiales (ver/descargar)
  - Evaluaciones (rendir/ver resultados)
- Mi Progreso
- Configuracion

**Endpoints consumidos:**
- `GET /v1/stats/global` (filtrado como student) o endpoint stats/student
- `GET /v1/materials` (Mobile)
- `GET /v1/materials/{id}`, `GET /v1/materials/{id}/summary`
- `GET /v1/materials/{id}/download-url`
- `GET /v1/materials/{id}/assessment`
- `POST /v1/materials/{id}/assessment/attempts`
- `GET /v1/attempts/{id}/results`
- `GET /v1/users/me/attempts`
- `PUT /v1/progress` (solo propio)

---

### 5. GUARDIAN
**Permisos**: users:read:own, users:update:own, materials:read, assessments:view_results, progress:read

**Menu:**
- Dashboard (resumen de hijos)
- Mis Hijos (lista de estudiantes vinculados)
  - Progreso por hijo
  - Resultados de evaluaciones por hijo
- Contenido
  - Materiales (solo ver, sin descargar)
- Configuracion

**Endpoints consumidos:**
- `GET /v1/guardians/{guardian_id}/relations` (Admin)
- `GET /v1/students/{student_id}/guardians` (Admin)
- `GET /v1/materials` (Mobile, solo lectura)
- `GET /v1/attempts/{id}/results`
- Necesario nuevo endpoint: `GET /v1/students/{id}/progress` o `GET /v1/guardians/{id}/children-progress`

---

## C. PANTALLAS NECESARIAS

### C.1 - Pantallas compartidas (todos los roles)

| # | Pantalla | Template | screenKey | Descripcion |
|---|----------|----------|-----------|-------------|
| 1 | Login | login | `app-login` | YA EXISTE. Pantalla de inicio de sesion |
| 2 | Settings | settings | `app-settings` | YA EXISTE. Configuracion de app |

---

### C.2 - Dashboards (por rol)

| # | Pantalla | Template | screenKey | Rol | Endpoint |
|---|----------|----------|-----------|-----|----------|
| 3 | Dashboard Teacher | dashboard | `dashboard-teacher` | YA EXISTE. KPIs: students, materials, avg_score, completion | `GET /v1/stats/global` |
| 4 | Dashboard Student | dashboard | `dashboard-student` | YA EXISTE. KPIs: courses, materials, my_score, progress | `GET /v1/stats/student` (pendiente) |
| 5 | Dashboard Super Admin | dashboard | `dashboard-superadmin` | NUEVO. KPIs: total_schools, total_users, total_materials, system_health | `GET /v1/stats/global` |
| 6 | Dashboard School Admin | dashboard | `dashboard-schooladmin` | NUEVO. KPIs: total_teachers, total_students, total_units, school_score | `GET /v1/stats/global` (filtrado) |
| 7 | Dashboard Guardian | dashboard | `dashboard-guardian` | NUEVO. KPIs: num_children, children_avg_score, pending_assessments, recent_activity | Endpoint nuevo necesario |

---

### C.3 - Modulo Materiales

| # | Pantalla | Template | screenKey | Pantallas hijas | Roles |
|---|----------|----------|-----------|-----------------|-------|
| 8 | Lista Materiales | list | `materials-list` | YA EXISTE | teacher, student, school_admin, super_admin, guardian(lectura) |
| 9 | Detalle Material | detail | `material-detail` | YA EXISTE | todos con materials:read |
| 10 | Crear Material | form (NUEVO) | `material-create` | NUEVO | teacher, super_admin |
| 11 | Editar Material | form (NUEVO) | `material-edit` | NUEVO | teacher, school_admin, super_admin |

**Flujo Materiales (Teacher):**
```
materials-list -> material-create -> upload-url -> upload-to-S3 -> upload-complete -> material-detail
materials-list -> material-detail -> material-edit
material-detail -> download (OPEN_URL)
material-detail -> assessment-view (take quiz)
```

---

### C.4 - Modulo Evaluaciones

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 12 | Lista Evaluaciones | list | `assessments-list` | NUEVO. Lista de evaluaciones disponibles | teacher, student |
| 13 | Vista Evaluacion (Quiz) | form (NUEVO) | `assessment-take` | NUEVO. Formulario para rendir evaluacion | student |
| 14 | Resultado Evaluacion | detail | `assessment-result` | NUEVO. Resultado de un intento | student, guardian |
| 15 | Historial Intentos | list | `attempts-history` | NUEVO. Historial de mis intentos | student |
| 16 | Calificar Evaluacion | detail (NUEVO) | `assessment-grade` | NUEVO. Vista de calificacion para teacher | teacher |

**Flujo Student:**
```
dashboard-student -> materials-list -> material-detail -> assessment-take -> assessment-result
dashboard-student -> attempts-history -> assessment-result
```

**Flujo Teacher:**
```
dashboard-teacher -> assessments-list -> assessment-grade (ver resultados de alumnos)
```

---

### C.5 - Modulo Usuarios (Admin)

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 17 | Lista Usuarios | list | `users-list` | NUEVO. Lista de usuarios con busqueda/filtros | super_admin, school_admin |
| 18 | Detalle Usuario | detail | `user-detail` | NUEVO. Perfil de usuario con roles y memberships | super_admin, school_admin |
| 19 | Crear Usuario | form | `user-create` | NUEVO. Formulario de registro | super_admin |
| 20 | Editar Usuario | form | `user-edit` | NUEVO. Edicion de datos de usuario | super_admin, school_admin |
| 21 | Mi Perfil | detail | `user-profile` | NUEVO. Mi propio perfil | todos |

**Flujo:**
```
users-list -> user-detail -> user-edit
users-list -> user-create
settings -> user-profile -> user-edit (own)
```

---

### C.6 - Modulo Escuelas (Admin)

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 22 | Lista Escuelas | list | `schools-list` | NUEVO | super_admin, platform_admin |
| 23 | Detalle Escuela | detail | `school-detail` | NUEVO. Info escuela + unidades + stats | super_admin, platform_admin, school_admin |
| 24 | Crear Escuela | form | `school-create` | NUEVO | super_admin, platform_admin |
| 25 | Editar Escuela | form | `school-edit` | NUEVO | super_admin, platform_admin, school_admin |

**Flujo:**
```
schools-list -> school-detail -> school-edit
schools-list -> school-create
school-detail -> units-list (ver unidades de la escuela)
```

---

### C.7 - Modulo Unidades Academicas

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 26 | Lista Unidades | list | `units-list` | NUEVO. Arbol/lista de unidades | school_admin, super_admin |
| 27 | Detalle Unidad | detail | `unit-detail` | NUEVO. Miembros, materiales, stats | school_admin, teacher, super_admin |
| 28 | Crear Unidad | form | `unit-create` | NUEVO | school_admin, super_admin |
| 29 | Editar Unidad | form | `unit-edit` | NUEVO | school_admin, super_admin |

---

### C.8 - Modulo Memberships

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 30 | Lista Miembros (de unidad) | list | `memberships-list` | NUEVO. Miembros de una unidad | school_admin, teacher |
| 31 | Asignar Miembro | form | `membership-create` | NUEVO. Asignar usuario a unidad | school_admin |

---

### C.9 - Modulo Guardian

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 32 | Mis Hijos | list | `guardian-children-list` | NUEVO. Lista de hijos vinculados | guardian |
| 33 | Progreso Hijo | detail/dashboard | `guardian-child-progress` | NUEVO. Dashboard de progreso del hijo | guardian |

---

### C.10 - Modulo Reportes/Progreso

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 34 | Progreso de Clase | list | `progress-unit-list` | NUEVO. Progreso de todos los alumnos | teacher, school_admin |
| 35 | Progreso Estudiante | detail | `progress-student-detail` | NUEVO. Progreso individual de un estudiante | teacher, school_admin, guardian |
| 36 | Mi Progreso | dashboard | `progress-my` | NUEVO. Dashboard de mi progreso | student |
| 37 | Estadisticas Generales | dashboard | `stats-dashboard` | NUEVO. Dashboard de estadisticas avanzadas | school_admin, super_admin |

---

### C.11 - Modulo RBAC (solo super_admin)

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 38 | Lista Roles | list | `roles-list` | NUEVO | super_admin |
| 39 | Detalle Rol | detail | `role-detail` | NUEVO. Rol con sus permisos | super_admin |
| 40 | Lista Resources | list | `resources-list` | NUEVO | super_admin |
| 41 | Permisos de Sistema | list | `permissions-list` | NUEVO | super_admin |

---

### C.12 - Modulo Materias

| # | Pantalla | Template | screenKey | Descripcion | Roles |
|---|----------|----------|-----------|-------------|-------|
| 42 | Lista Materias | list | `subjects-list` | NUEVO | school_admin, super_admin |
| 43 | Crear/Editar Materia | form | `subject-form` | NUEVO | school_admin, super_admin |

---

## D. ADAPTACION POR PLATAFORMA

### D.1 - Pattern: Dashboard

| Aspecto | Mobile (compact) | Tablet (medium) | Desktop (expanded) | Web |
|---------|-----------------|-----------------|-------------------|-----|
| KPIs | 2 columnas, cards apiladas | 2x2 grid | 4 en una fila | 4 en fila, max-width 1200px |
| Actividad | Lista vertical debajo de KPIs | Lista vertical | Panel lateral derecho | Panel lateral o inferior |
| Acciones rapidas | FAB flotante + botones | Botones en toolbar | Botones en toolbar + sidebar | Toolbar |
| Navegacion | Bottom navigation (3-5 items) | Bottom nav o rail | Navigation rail + drawer | Side drawer persistente |
| Saludo | Compacto, sin fecha en linea separada | Con fecha | Con fecha + notificaciones | Con breadcrumbs |

### D.2 - Pattern: List

| Aspecto | Mobile (compact) | Tablet (medium) | Desktop (expanded) | Web |
|---------|-----------------|-----------------|-------------------|-----|
| Layout | Lista vertical, una columna | 2 columnas grid o lista ancha | Master-detail (40/60) | Grid responsivo 1-3 cols |
| Busqueda | Barra colapsable en top-bar | Barra visible | Barra visible + filtros laterales | Barra + filtros avanzados |
| Filtros | Chips horizontales scrollable | Chips + dropdown | Panel lateral de filtros | Panel lateral colapsable |
| Item click | Navega a detalle (nueva pantalla) | Navega a detalle | Abre en panel derecho | Abre en panel derecho o modal |
| Paginacion | Infinite scroll | Infinite scroll | Paginacion con numeros | Paginacion con numeros |
| FAB crear | Esquina inferior derecha | Esquina inferior derecha | Boton en toolbar | Boton en toolbar |

### D.3 - Pattern: Detail

| Aspecto | Mobile (compact) | Tablet (medium) | Desktop (expanded) | Web |
|---------|-----------------|-----------------|-------------------|-----|
| Layout | Scroll vertical lineal | Scroll con padding lateral | Side-by-side (hero/meta | info/actions) | Centrado, max-width |
| Hero | Full width arriba | Full width con padding | Panel izquierdo 40% | Panel izquierdo o banner |
| Acciones | Bottom bar fijo | Bottom bar | Botones en header panel derecho | Toolbar superior |
| Back | Arrow en top-bar | Arrow en top-bar | Breadcrumbs | Breadcrumbs |
| Secciones | Apiladas verticalmente | Apiladas con mas espacio | Tabs en panel derecho | Tabs o accordion |

### D.4 - Pattern: Form

| Aspecto | Mobile (compact) | Tablet (medium) | Desktop (expanded) | Web |
|---------|-----------------|-----------------|-------------------|-----|
| Layout | Full-screen, campos apilados | Centrado, max-width 600 | Centrado-card max-width 700 o side-by-side | Modal o pagina centrada |
| Campos | Uno por fila | 1-2 por fila | 2 por fila para campos cortos | 2 por fila |
| Submit | Boton full-width abajo | Boton centrado | Botones alineados a la derecha | Botones en footer |
| Validacion | Inline debajo de campo | Inline debajo de campo | Inline + resumen | Inline + resumen |
| File upload | Nativo del OS | Nativo del OS | Drag-and-drop zone | Drag-and-drop |

### D.5 - Pattern: Settings

| Aspecto | Mobile (compact) | Tablet (medium) | Desktop (expanded) | Web |
|---------|-----------------|-----------------|-------------------|-----|
| Layout | Lista vertical de secciones | Lista con padding | Side-by-side (nav | secciones) | Side-by-side |
| Card usuario | Compacta arriba | Mas grande | Panel izquierdo | Panel izquierdo |
| Secciones | Agrupadas con dividers | Cards agrupadas | Tabs en panel derecho | Tabs |

### D.6 - Navegacion Global

| Aspecto | Mobile (compact) | Tablet (medium) | Desktop (expanded) | Web |
|---------|-----------------|-----------------|-------------------|-----|
| Tipo | Bottom Navigation Bar (max 5 items) | Navigation Rail | Navigation Rail + Drawer expandible | Side Drawer persistente |
| Items visibles | Dashboard, Contenido, Reportes, Settings | Todos los de primer nivel | Arbol completo con sub-items | Arbol completo |
| Overflow | "More" menu para items extra | Dropdown en rail | Todos visibles | Todos visibles |

---

## E. INVENTARIO DE OBJETOS

### E.1 - Screen Instances Necesarias (43 total)

**YA EXISTEN (6):**
| screenKey | Template | Status |
|-----------|----------|--------|
| `app-login` | login-basic-v1 | OK |
| `dashboard-teacher` | dashboard-basic-v1 | OK |
| `dashboard-student` | dashboard-basic-v1 | OK |
| `materials-list` | list-basic-v1 | OK |
| `material-detail` | detail-basic-v1 | OK |
| `app-settings` | settings-basic-v1 | OK |

**NUEVAS (37):**
| screenKey | Template | Prioridad |
|-----------|----------|-----------|
| `dashboard-superadmin` | dashboard-basic-v1 | Alta |
| `dashboard-schooladmin` | dashboard-basic-v1 | Alta |
| `dashboard-guardian` | dashboard-basic-v1 | Media |
| `material-create` | form-basic-v1 (NUEVO) | Alta |
| `material-edit` | form-basic-v1 (NUEVO) | Alta |
| `assessments-list` | list-basic-v1 | Alta |
| `assessment-take` | form-quiz-v1 (NUEVO) | Alta |
| `assessment-result` | detail-basic-v1 | Alta |
| `attempts-history` | list-basic-v1 | Media |
| `assessment-grade` | detail-basic-v1 | Media |
| `users-list` | list-basic-v1 | Alta |
| `user-detail` | detail-basic-v1 | Alta |
| `user-create` | form-basic-v1 | Media |
| `user-edit` | form-basic-v1 | Media |
| `user-profile` | detail-basic-v1 | Media |
| `schools-list` | list-basic-v1 | Alta |
| `school-detail` | detail-basic-v1 | Alta |
| `school-create` | form-basic-v1 | Media |
| `school-edit` | form-basic-v1 | Media |
| `units-list` | list-basic-v1 | Alta |
| `unit-detail` | detail-basic-v1 | Alta |
| `unit-create` | form-basic-v1 | Media |
| `unit-edit` | form-basic-v1 | Media |
| `memberships-list` | list-basic-v1 | Media |
| `membership-create` | form-basic-v1 | Media |
| `guardian-children-list` | list-basic-v1 | Media |
| `guardian-child-progress` | dashboard-basic-v1 | Media |
| `progress-unit-list` | list-basic-v1 | Alta |
| `progress-student-detail` | detail-basic-v1 | Alta |
| `progress-my` | dashboard-basic-v1 | Alta |
| `stats-dashboard` | dashboard-basic-v1 | Media |
| `roles-list` | list-basic-v1 | Baja |
| `role-detail` | detail-basic-v1 | Baja |
| `resources-list` | list-basic-v1 | Baja |
| `permissions-list` | list-basic-v1 | Baja |
| `subjects-list` | list-basic-v1 | Media |
| `subject-form` | form-basic-v1 | Media |

---

### E.2 - Templates Nuevos Necesarios (2)

| Template | Pattern | Descripcion | Prioridad |
|----------|---------|-------------|-----------|
| `form-basic-v1` | form | Formulario generico con campos dinamicos, validacion, submit/cancel. Zones: header, form_fields, actions. Soporta create y edit mode. | **Alta** |
| `form-quiz-v1` | form | Formulario especializado para evaluaciones. Zones: quiz_header (titulo, tiempo, progreso), question_area (pregunta + opciones), navigation (anterior/siguiente), submit_area. | **Media** |

NOTA: Los templates `login`, `dashboard`, `list`, `detail`, `settings` ya existen. El template `form` ya tiene un pattern renderer (`FormPatternRenderer.kt`) pero NO tiene template seed en la BD.

---

### E.3 - Handlers de Accion Necesarios

**YA EXISTEN (3):**
| Handler | screenKeys |
|---------|------------|
| `LoginActionHandler` | app-login |
| `SettingsActionHandler` | app-settings |
| `DashboardActionHandler` | dashboard-teacher, dashboard-student |

**NUEVOS NECESARIOS (10):**
| Handler | screenKeys | Descripcion | Prioridad |
|---------|------------|-------------|-----------|
| `MaterialCreateHandler` | material-create | Flujo: crear material -> obtener upload URL -> subir a S3 -> notificar completado | Alta |
| `MaterialEditHandler` | material-edit | Actualizar metadata del material | Alta |
| `MaterialDetailHandler` | material-detail | YA EXISTE parcialmente (en actions). Agregar soporte de download y navegacion a quiz | Media |
| `AssessmentTakeHandler` | assessment-take | Flujo: cargar preguntas, recoger respuestas, submit attempt, mostrar resultado | Alta |
| `UserCrudHandler` | user-create, user-edit, user-detail | CRUD de usuarios via Admin API | Media |
| `SchoolCrudHandler` | school-create, school-edit, school-detail | CRUD de escuelas via Admin API | Media |
| `UnitCrudHandler` | unit-create, unit-edit, unit-detail | CRUD de unidades via Admin API | Media |
| `MembershipHandler` | membership-create, memberships-list | Asignacion de miembros a unidades | Media |
| `ProgressHandler` | progress-unit-list, progress-student-detail, progress-my | Lectura y actualizacion de progreso | Alta |
| `GuardianHandler` | guardian-children-list, guardian-child-progress | Consulta de hijos y su progreso | Baja |

---

### E.4 - Nuevos Recursos RBAC Necesarios

Los recursos actuales cubren bien la estructura general. Sin embargo, hay gaps:

| Recurso propuesto | Key | Parent | Scope | Justificacion |
|-------------------|-----|--------|-------|---------------|
| Materias | `subjects` | `academic` | school | No existe recurso RBAC para gestionar materias. Los endpoints existen en la Admin API pero no hay permiso dedicado |
| Relaciones Guardian | `guardian_relations` | `academic` | school | Los endpoints de guardian-relations existen pero no hay recurso RBAC ni permisos dedicados |
| Perfil Propio | `profile` | `dashboard` | system | Separar el acceso a "mi perfil" del recurso users general |

**Permisos nuevos sugeridos:**
- `subjects:create`, `subjects:read`, `subjects:update`, `subjects:delete`
- `guardian_relations:create`, `guardian_relations:read`, `guardian_relations:update`, `guardian_relations:delete`
- `profile:read`, `profile:update`

---

### E.5 - Nuevos Resource-Screen Mappings Necesarios

**YA EXISTEN (3):**
| Resource Key | Screen Key | Screen Type |
|-------------|------------|-------------|
| materials | materials-list | list |
| materials | material-detail | detail |
| dashboard | dashboard-teacher | dashboard |

**NUEVOS NECESARIOS (30+):**
| Resource Key | Screen Key | Screen Type | is_default |
|-------------|------------|-------------|------------|
| dashboard | dashboard-superadmin | dashboard | false |
| dashboard | dashboard-schooladmin | dashboard | false |
| dashboard | dashboard-student | dashboard | false |
| dashboard | dashboard-guardian | dashboard | false |
| materials | material-create | form | true |
| materials | material-edit | form | false |
| assessments | assessments-list | list | true |
| assessments | assessment-take | form | true |
| assessments | assessment-result | detail | true |
| assessments | attempts-history | list | false |
| assessments | assessment-grade | detail | false |
| users | users-list | list | true |
| users | user-detail | detail | true |
| users | user-create | form | true |
| users | user-edit | form | false |
| schools | schools-list | list | true |
| schools | school-detail | detail | true |
| schools | school-create | form | true |
| schools | school-edit | form | false |
| units | units-list | list | true |
| units | unit-detail | detail | true |
| units | unit-create | form | true |
| units | unit-edit | form | false |
| memberships | memberships-list | list | true |
| memberships | membership-create | form | true |
| progress | progress-unit-list | list | true |
| progress | progress-student-detail | detail | true |
| progress | progress-my | dashboard | false |
| stats | stats-dashboard | dashboard | true |
| roles | roles-list | list | true |
| roles | role-detail | detail | true |
| permissions_mgmt | permissions-list | list | true |

---

## F. PRIORIZACION

### Fase 3A - Core Flow Completo (Prioridad ALTA)
**Objetivo**: Completar el flujo principal de Teacher y Student con creacion de contenido y evaluaciones.
**Dependencia**: Ya existe todo el backend necesario.

| Orden | Objeto | Tipo | Justificacion |
|-------|--------|------|---------------|
| 1 | `form-basic-v1` template | Template | Prerequisito para todas las pantallas de creacion/edicion |
| 2 | `material-create` instance | Screen Instance | Teacher necesita crear materiales (flujo core) |
| 3 | `MaterialCreateHandler` | Handler | Maneja upload a S3 (complejo, multi-paso) |
| 4 | `material-edit` instance | Screen Instance | Teacher necesita editar materiales |
| 5 | `MaterialEditHandler` | Handler | PUT a /v1/materials/{id} |
| 6 | `assessments-list` instance | Screen Instance | Lista evaluaciones disponibles |
| 7 | `assessment-take` instance + `form-quiz-v1` template | Screen Instance + Template | Student necesita rendir evaluaciones |
| 8 | `AssessmentTakeHandler` | Handler | Flujo de rendir evaluacion |
| 9 | `assessment-result` instance | Screen Instance | Ver resultados de intento |
| 10 | `attempts-history` instance | Screen Instance | Historial de mis intentos |
| 11 | `progress-my` instance | Screen Instance | Mi progreso como estudiante |
| 12 | `ProgressHandler` | Handler | Lectura/escritura de progreso |
| 13 | `progress-unit-list` instance | Screen Instance | Teacher ve progreso de alumnos |
| 14 | `progress-student-detail` instance | Screen Instance | Detalle de progreso de un alumno |
| 15 | `dashboard-superadmin` instance | Screen Instance | Dashboard con KPIs globales |
| 16 | `dashboard-schooladmin` instance | Screen Instance | Dashboard con KPIs de escuela |

**Estimacion**: 15-20 screen instances, 2 templates, 4 handlers

---

### Fase 3B - Administracion y CRUD (Prioridad MEDIA)
**Objetivo**: Pantallas administrativas de escuelas, usuarios, unidades, memberships.
**Dependencia**: Todos los endpoints de Admin API ya existen.

| Orden | Objeto | Tipo | Justificacion |
|-------|--------|------|---------------|
| 17 | `users-list` instance | Screen Instance | Gestion de usuarios |
| 18 | `user-detail` instance | Screen Instance | Ver perfil de usuario |
| 19 | `user-create` instance | Screen Instance | Crear usuario |
| 20 | `user-edit` instance | Screen Instance | Editar usuario |
| 21 | `UserCrudHandler` | Handler | CRUD de usuarios |
| 22 | `user-profile` instance | Screen Instance | Mi perfil |
| 23 | `schools-list` instance | Screen Instance | Gestion de escuelas |
| 24 | `school-detail` instance | Screen Instance | Ver escuela |
| 25 | `school-create` + `school-edit` instances | Screen Instance | CRUD escuela |
| 26 | `SchoolCrudHandler` | Handler | CRUD de escuelas |
| 27 | `units-list` instance | Screen Instance | Gestion de unidades |
| 28 | `unit-detail` instance | Screen Instance | Ver unidad |
| 29 | `unit-create` + `unit-edit` instances | Screen Instance | CRUD unidad |
| 30 | `UnitCrudHandler` | Handler | CRUD de unidades |
| 31 | `memberships-list` + `membership-create` instances | Screen Instance | Gestion de miembros |
| 32 | `MembershipHandler` | Handler | Asignacion de miembros |
| 33 | `subjects-list` + `subject-form` instances | Screen Instance | Gestion de materias |
| 34 | `stats-dashboard` instance | Screen Instance | Estadisticas avanzadas |
| 35 | Resource-screen mappings (batch) | Mapping | Vincular todo con RBAC |
| 36 | Nuevos permisos subjects | RBAC | Permisos para materias |

**Estimacion**: 17 screen instances, 4 handlers, RBAC updates

---

### Fase 3C - Guardian y Roles Avanzados (Prioridad BAJA)
**Objetivo**: Funcionalidad completa para guardianes, gestion RBAC avanzada.
**Dependencia**: Posiblemente necesita endpoints nuevos en backend (progreso por hijo).

| Orden | Objeto | Tipo | Justificacion |
|-------|--------|------|---------------|
| 37 | `dashboard-guardian` instance | Screen Instance | Dashboard del guardian |
| 38 | `guardian-children-list` instance | Screen Instance | Lista de hijos |
| 39 | `guardian-child-progress` instance | Screen Instance | Progreso de un hijo |
| 40 | `GuardianHandler` | Handler | Consulta relaciones guardian |
| 41 | `roles-list` instance | Screen Instance | Solo super_admin |
| 42 | `role-detail` instance | Screen Instance | Ver permisos de un rol |
| 43 | `resources-list` instance | Screen Instance | Catalogo de resources |
| 44 | `permissions-list` instance | Screen Instance | Catalogo de permisos |
| 45 | Nuevos recursos RBAC (subjects, guardian_relations, profile) | RBAC | Completar modelo |
| 46 | Endpoint nuevo: `GET /v1/guardians/{id}/children-progress` | Backend | No existe aun |

**Estimacion**: 8 screen instances, 1 handler, RBAC updates, 1+ endpoint nuevo

---

### Resumen de Volumetria

| Fase | Screen Instances | Templates | Handlers | RBAC Updates | Endpoints Nuevos |
|------|-----------------|-----------|----------|--------------|------------------|
| Ya Existe | 6 | 5 | 3 | base completa | 0 |
| 3A (Alta) | 16 | 2 | 4 | 0 | 0 (*) |
| 3B (Media) | 17 | 0 | 4 | 3 permisos | 0 |
| 3C (Baja) | 8 | 0 | 1 | 3 recursos + permisos | 1-2 |
| **TOTAL** | **47** | **7** | **12** | -- | 1-2 |

(*) El endpoint `GET /v1/stats/student` esta referenciado en `dashboard-student` pero no aparece en Swagger. Puede ser que las stats del student se obtengan filtrando el endpoint global, o puede necesitar crearse.

---

### Notas Tecnicas Importantes

1. **Dual API**: Las screen instances que usan endpoints de la Admin API (`:8081`) vs Mobile API (`:9091`) necesitan que el `data_endpoint` incluya el host correcto o que el DataLoader tenga configuracion de base URL por endpoint. Actualmente el `material-detail` usa `/v1/materials/{id}` que corresponde a Mobile. Para operaciones CRUD admin (users, schools, units), se necesita configurar la base URL de la Admin API.

2. **Routing**: El archivo `Routes.kt` actualmente tiene un mapping manual `fromScreenKey()` para 6 pantallas. Con 47 screen instances, se necesita migrar a un routing completamente dinamico usando `Route.Dynamic(screenKey, params)` como ruta generica.

3. **Navigation por rol**: El endpoint `GET /v1/menu` ya retorna el menu filtrado por rol del usuario autenticado. El sistema de Dynamic UI debe consumir este endpoint para construir el bottom nav / drawer dinamicamente en lugar de hardcodear items de navegacion.

4. **Form template**: Es el bloqueo mas critico. Sin `form-basic-v1`, no se pueden implementar pantallas de creacion/edicion. El `FormPatternRenderer.kt` ya existe en el frontend, lo que sugiere que el pattern esta soportado pero falta el template seed.

5. **Multi-step actions**: El handler `MaterialCreateHandler` es el mas complejo porque involucra 3 API calls secuenciales (crear material -> obtener upload URL -> notificar upload complete), ademas de la subida directa a S3. Esto puede requerir una extension del sistema de action handlers para soportar flujos multi-paso con estado.

6. **Assessment-take**: La pantalla de rendir evaluacion es un caso especial que no encaja bien en los patterns existentes. Necesita: timer, navegacion entre preguntas, persistencia local de respuestas, y submit final. El template `form-quiz-v1` es necesario para manejar esta complejidad.