# Casos de Uso: Menu y Navegacion por Usuario y Plataforma

> **Proyecto:** EduGo KMP - Dynamic UI Phase 3
> **Fecha:** 2026-02-24
> **Sprint:** 7+
> **Objetivo:** Documento de referencia para implementar y testear el menu dinamico por plataforma

---

## Tabla de Contenidos

1. [Resumen de Arquitectura](#1-resumen-de-arquitectura)
2. [Recursos del Menu (BD)](#2-recursos-del-menu-bd)
3. [Usuarios de Prueba](#3-usuarios-de-prueba)
4. [Reglas de Plataforma](#4-reglas-de-plataforma)
5. [Casos de Uso por Usuario](#5-casos-de-uso-por-usuario)
   - 5.1 [super_admin](#51-super_admin)
   - 5.2 [school_admin](#52-school_admin)
   - 5.3 [teacher](#53-teacher)
   - 5.4 [student](#54-student)
   - 5.5 [guardian](#55-guardian)
6. [Matriz de Navegacion Cruzada](#6-matriz-de-navegacion-cruzada)
7. [Gaps y Pendientes](#7-gaps-y-pendientes)
8. [Datos de Prueba Necesarios](#8-datos-de-prueba-necesarios)

---

## 1. Resumen de Arquitectura

### Flujo de Datos

```
  BD (PostgreSQL)           Backend (IAM Platform)         Frontend (KMP)
  ================          ======================         ==============
  ui_config.resources  -->  GET /api/v1/menu          -->  MenuResponse
  ui_config.permissions     (filtrado por permisos         |
  iam.role_permissions      del token JWT)                 v
                                                      MenuItem[]
                                                           |
                                                      toNavigationDefinition()
                                                           |
                                                      NavigationDefinition
                                                      (drawerItems / bottomNav)
                                                           |
                                                      AdaptiveNavigationLayout
                                                           |
                                          +----------------+----------------+
                                          |                                 |
                                    >= 840dp                          < 840dp
                                    EXPANDED                          COMPACT
                                          |                                 |
                                   DSNavigationRail              DSBottomNavigationBar
                                   (vertical izq)               (horizontal abajo)
                                          |                                 |
                                          +----------------+----------------+
                                                           |
                                                     DynamicScreen
                                                     (SDUI por screenKey)
```

### Breakpoint Critico: 840dp

| Rango            | Clasificacion | Dispositivos                    | Componente Nav         |
|------------------|---------------|---------------------------------|------------------------|
| < 840dp          | COMPACT       | Mobile, Tablet portrait         | DSBottomNavigationBar  |
| >= 840dp         | EXPANDED      | Desktop, Web, Tablet landscape  | DSNavigationRail       |

### Logica de Conversion Menu -> Nav

```
Si items tienen children -> van a NavigationDefinition.drawerItems
Si items NO tienen children -> van a NavigationDefinition.bottomNav

En EXPANDED: usa drawerItems (o bottomNav si drawerItems vacio)
En COMPACT:  usa bottomNav (o drawerItems si bottomNav vacio)

Resultado: AMBOS modos muestran los MISMOS items top-level
La diferencia es COMO se muestran las subopciones
```

### Endpoints Clave

| Endpoint                                    | API          | Proposito                          |
|---------------------------------------------|--------------|------------------------------------|
| `GET /api/v1/menu`                          | IAM Platform | Menu filtrado por permisos         |
| `GET /api/v1/menu/full`                     | IAM Platform | Menu completo (solo admin)         |
| `GET /api/v1/screen-config/resolve/key/:key`| IAM Platform | Pantalla SDUI resuelta             |
| `GET /api/v1/auth/contexts`                 | IAM Platform | Contextos disponibles (escuelas)   |
| `POST /api/v1/auth/switch-context`          | IAM Platform | Cambiar contexto (escuela)         |
| `GET /api/v1/schools`                       | Admin API    | Lista de escuelas (para selector)  |

---

## 2. Recursos del Menu (BD)

Arbol jerarquico de `ui_config.resources`:

```
dashboard (Dashboard)                    [scope: system, icon: dashboard]
|
admin (Administracion)                   [scope: system, icon: settings]
|  |-- users (Usuarios)                  [icon: users]
|  |-- schools (Escuelas)                [icon: school]
|  |-- roles (Roles)                     [icon: shield]
|  +-- permissions_mgmt (Permisos)       [icon: key]
|
academic (Academico)                     [scope: school, icon: graduation-cap]
|  |-- units (Unidades Academicas)       [icon: layers]
|  +-- memberships (Miembros)            [icon: user-plus]
|
content (Contenido)                      [scope: school, icon: book-open]
|  |-- materials (Materiales)            [icon: file-text]
|  +-- assessments (Evaluaciones)        [icon: clipboard]
|
reports (Reportes)                       [scope: school, icon: bar-chart]
   |-- progress (Progreso)               [icon: trending-up]
   +-- stats (Estadisticas)              [icon: pie-chart]
```

### Iconos Material3 Mapeados

| icon BD            | Material Filled         | Material Outlined       |
|--------------------|-------------------------|-------------------------|
| dashboard          | Icons.Filled.Dashboard  | Icons.Outlined.Dashboard|
| settings           | Icons.Filled.Settings   | Icons.Outlined.Settings |
| users              | Icons.Filled.Group      | Icons.Outlined.Group    |
| school             | Icons.Filled.School     | Icons.Outlined.School   |
| shield             | Icons.Filled.Shield     | Icons.Outlined.Shield   |
| key                | Icons.Filled.Key        | Icons.Outlined.Key      |
| graduation-cap     | Icons.Filled.School     | Icons.Outlined.School   |
| layers             | Icons.Filled.Layers     | Icons.Outlined.Layers   |
| user-plus          | Icons.Filled.PersonAdd  | Icons.Outlined.PersonAdd|
| book-open          | Icons.Filled.Book       | Icons.Outlined.Book     |
| file-text          | Icons.Filled.Description| Icons.Outlined.Description|
| clipboard          | Icons.Filled.Assessment | Icons.Outlined.Assessment|
| bar-chart          | Icons.Filled.BarChart   | Icons.Outlined.BarChart |
| trending-up        | Icons.AutoMirrored.Filled.TrendingUp | AutoMirrored.Outlined.TrendingUp |
| pie-chart          | Icons.Filled.PieChart   | Icons.Outlined.PieChart |

---

## 3. Usuarios de Prueba

| Email                          | Rol          | Escuela                   | Password       | Permisos |
|--------------------------------|--------------|---------------------------|----------------|----------|
| super@edugo.test               | super_admin  | (ninguna - elige escuela) | EduGoTest123!  | 52       |
| admin.primaria@edugo.test      | school_admin | Escuela Primaria Demo     | EduGoTest123!  | 19       |
| admin.secundario@edugo.test    | school_admin | Colegio Secundario Demo   | EduGoTest123!  | 19       |
| teacher.math@edugo.test        | teacher      | Escuela Primaria Demo     | EduGoTest123!  | 17       |
| student.carlos@edugo.test      | student      | Escuela Primaria Demo     | EduGoTest123!  | 9        |
| guardian.roberto@edugo.test    | guardian     | Escuela Primaria Demo     | EduGoTest123!  | 6        |

---

## 4. Reglas de Plataforma

### 4.1 COMPACT (< 840dp) - Mobile y Tablet Portrait

```
+------------------------------------------+
|  [<=]  Titulo Seccion        [Avatar \/] |  <-- DSTopAppBar
|------------------------------------------|
|                                          |
|                                          |
|           CONTENIDO PRINCIPAL            |
|          (DynamicScreen / SDUI)          |
|                                          |
|                                          |
|------------------------------------------|
| [ico1] [ico2] [ico3] [ico4] [ico5]      |  <-- DSBottomNavigationBar
|  Lab1   Lab2   Lab3   Lab4   Lab5        |     (max 5 items recomendado)
+------------------------------------------+
```

**Reglas:**
- Bottom bar muestra items top-level como tabs con icono + label
- Material3 recomienda maximo 5 items en bottom bar
- Al seleccionar un item padre con children: navega a pantalla con subopciones
- Subopciones se muestran como lista o tabs dentro del contenido
- Header con avatar en topBar (UserMenuHeader)
- Menu dropdown en avatar: "Cambiar contexto" (solo super_admin) + "Cerrar sesion"
- Boton back para volver de subopciones a nivel padre

### 4.2 EXPANDED (>= 840dp) - Desktop, Web, Tablet Landscape

```
+----+--------------------------------------------------+
| H  |                                                  |
| E  |                                                  |
| A  |                                                  |
| D  |            CONTENIDO PRINCIPAL                   |
| E  |           (DynamicScreen / SDUI)                 |
| R  |                                                  |
|----|                                                  |
|icon|                                                  |
|Lab |                                                  |
|----|                                                  |
|icon|                                                  |
|Lab |                                                  |
|----|                                                  |
|icon|                                                  |
|Lab |                                                  |
|----|                                                  |
|icon|                                                  |
|Lab |                                                  |
|----|                                                  |
|icon|                                                  |
|Lab |                                                  |
+----+--------------------------------------------------+
 Rail               Contenido (weight 1f)
 (80dp)
```

**Reglas:**
- NavigationRail vertical a la izquierda (ancho 80dp)
- Header del rail: UserMenuHeader (avatar + nombre + rol)
- Items del rail: icono + label debajo
- Al seleccionar item padre con children: contenido muestra subopciones
- Sin limite tecnico de items en rail (puede scrollear)
- Menu dropdown en header: "Cambiar contexto" (solo super_admin) + "Cerrar sesion"

### 4.3 Diferencias Desktop vs Web (ambos EXPANDED)

| Aspecto              | Desktop                        | Web (WasmJS)                   |
|----------------------|--------------------------------|--------------------------------|
| Breakpoint           | >= 840dp                       | >= 840dp                       |
| Layout               | NavigationRail + contenido     | NavigationRail + contenido     |
| Comportamiento       | Identico                       | Identico                       |
| Diferencia notable   | Ventana nativa redimensionable | Viewport del browser           |
| Si se achica < 840dp | Cambia a BottomNav             | Cambia a BottomNav (responsive)|

### 4.4 Tablet (zona intermedia)

| Orientacion | Ancho tipico | Clasificacion | Layout              |
|-------------|--------------|---------------|---------------------|
| Portrait    | ~600-800dp   | COMPACT       | BottomNavigationBar |
| Landscape   | ~900-1200dp  | EXPANDED      | NavigationRail      |

**Comportamiento:** Se adapta automaticamente al rotar el dispositivo.

---

## 5. Casos de Uso por Usuario

---

### 5.1 super_admin

**Usuario:** `super@edugo.test`
**Permisos:** 52 (todos)
**Escuela:** Ninguna asignada - debe elegir escuela

#### 5.1.1 Flujo de Entrada

```
  LOGIN                    SELECTOR ESCUELA              MENU PRINCIPAL
  ======                   ================              ==============

  [Email:    ]             +-------------------+         Menu completo
  [Password: ]    ---->    | Seleccionar       |  ---->  segun escuela
  [  Login   ]             | Escuela           |         seleccionada
                           |                   |
                           | * Esc. Primaria   |
                           | * Col. Secundario |
                           +-------------------+
```

**Detalle del flujo:**
1. Login con `super@edugo.test` / `EduGoTest123!`
2. Backend retorna `LoginResponse` con `activeContext` SIN `schoolId` (super_admin no tiene escuela fija)
3. `MainScreen` carga menu via `GET /api/v1/menu`
4. Al navegar a seccion school-scoped: `activeContext.schoolId` es null/blank
5. Se muestra `SchoolSelectorScreen` que llama `GET /api/v1/schools`
6. Usuario selecciona escuela -> `authService.switchContext(schoolId)`
7. Contexto se actualiza -> menu se recarga con datos de esa escuela

#### 5.1.2 Menu Visible (5 items top-level)

```
dashboard (Dashboard)                     [icon: dashboard]
|
admin (Administracion)                    [icon: settings]
|  |-- users (Usuarios)                   [icon: users]
|  |-- schools (Escuelas)                 [icon: school]
|  |-- roles (Roles)                      [icon: shield]
|  +-- permissions_mgmt (Permisos)        [icon: key]
|
academic (Academico)                      [icon: graduation-cap]
|  |-- units (Unidades Academicas)        [icon: layers]
|  +-- memberships (Miembros)             [icon: user-plus]
|
content (Contenido)                       [icon: book-open]
|  |-- materials (Materiales)             [icon: file-text]
|  +-- assessments (Evaluaciones)         [icon: clipboard]
|
reports (Reportes)                        [icon: bar-chart]
   |-- progress (Progreso)                [icon: trending-up]
   +-- stats (Estadisticas)               [icon: pie-chart]
```

#### 5.1.3 super_admin en DESKTOP (>= 840dp)

```
+--------+----------------------------------------------------------+
| [JM]   |                                                          |
| Super  |                                                          |
| Admin  |                                                          |
| [v]    |                                                          |
|--------|                                                          |
|[Dash]  |              CONTENIDO SEGUN TAB                         |
| Dashb  |                                                          |
|--------|    Tab "Dashboard":                                      |
|[Sett]  |      -> DynamicDashboardScreen (KPIs globales)           |
| Admin  |                                                          |
|--------|    Tab "Administracion":                                  |
|[Grad]  |      -> Lista de subopciones:                            |
| Acad   |         [Users] [Schools] [Roles] [Permisos]             |
|--------|         Click "Usuarios" -> DynamicScreen(users)         |
|[Book]  |         Click "Escuelas" -> DynamicScreen(schools)       |
| Conte  |         Click "Roles" -> DynamicScreen(roles)            |
|--------|         Click "Permisos" -> DynamicScreen(permissions)   |
|[Chart] |                                                          |
| Repor  |    Tab "Academico":                                      |
|--------|      -> SchoolSelectorScreen (si no hay escuela)         |
|        |      -> Con escuela: [Unidades] [Miembros]               |
|        |                                                          |
|        |    Tab "Contenido":                                      |
|        |      -> SchoolSelectorScreen (si no hay escuela)         |
|        |      -> Con escuela: [Materiales] [Evaluaciones]         |
|        |                                                          |
|        |    Tab "Reportes":                                       |
|        |      -> SchoolSelectorScreen (si no hay escuela)         |
|        |      -> Con escuela: [Progreso] [Estadisticas]           |
+--------+----------------------------------------------------------+
  Rail                        Contenido
  (80dp)
```

**Header del Rail (UserMenuHeader):**
```
+--------+
| [JM]   |  <-- Avatar circular 40dp con iniciales
| Super  |  <-- Nombre
| Admin  |  <-- Rol formateado
| [v]    |  <-- Indicador de dropdown
+--------+
Click en header -> DropdownMenu:
  * "Cambiar contexto"  -> Abre SchoolSelectorScreen
  * "Cerrar sesion"     -> Logout
```

**Navegacion detallada:**

| Accion                       | Resultado                                           |
|------------------------------|-----------------------------------------------------|
| Click "Dashboard"            | DynamicDashboardScreen con metricas globales         |
| Click "Administracion"       | Panel con 4 subopciones (Users/Schools/Roles/Perms) |
| Click "Usuarios"             | DynamicScreen(screenKey="users") - lista de usuarios |
| Click "Escuelas"             | DynamicScreen(screenKey="schools") - lista escuelas  |
| Click "Roles"                | DynamicScreen(screenKey="roles") - gestion de roles  |
| Click "Permisos"             | DynamicScreen(screenKey="permissions_mgmt")          |
| Click "Academico"            | Si no hay escuela: SchoolSelectorScreen              |
| (con escuela) "Unidades"     | DynamicScreen(screenKey="units")                     |
| (con escuela) "Miembros"     | DynamicScreen(screenKey="memberships")               |
| Click "Contenido"            | Si no hay escuela: SchoolSelectorScreen              |
| (con escuela) "Materiales"   | DynamicScreen(screenKey="materials")                 |
| (con escuela) "Evaluaciones" | DynamicScreen(screenKey="assessments")               |
| Click "Reportes"             | Si no hay escuela: SchoolSelectorScreen              |
| (con escuela) "Progreso"     | DynamicScreen(screenKey="progress")                  |
| (con escuela) "Estadisticas" | DynamicScreen(screenKey="stats")                     |
| Click "Cambiar contexto"     | SchoolSelectorScreen para elegir otra escuela        |
| Click "Cerrar sesion"        | Logout -> LoginScreen                                |

#### 5.1.4 super_admin en WEB (>= 840dp)

**Identico a Desktop.** Mismo breakpoint (840dp), mismo layout (NavigationRail).

Diferencias menores:
- Corre en browser (WasmJS) en vez de ventana nativa
- Si el usuario achica la ventana del browser a < 840dp -> cambia a BottomNav automaticamente
- Scroll nativo del browser

#### 5.1.5 super_admin en MOBILE (< 840dp)

```
+------------------------------------------+
|  [<=]  Dashboard             [JM \/]     |  <-- TopAppBar
|------------------------------------------|
|                                          |
|                                          |
|         CONTENIDO SEGUN TAB              |
|                                          |
|  Tab "Dashboard":                        |
|    -> DynamicDashboardScreen             |
|                                          |
|  Tab "Administracion":                   |
|    -> Pantalla con lista de subopciones  |
|       [Usuarios]                         |
|       [Escuelas]                         |
|       [Roles]                            |
|       [Permisos]                         |
|    -> Click en subopcion navega a        |
|       pantalla completa con back         |
|                                          |
|  Tab "Academico":                        |
|    -> SchoolSelectorScreen               |
|    -> Con escuela: lista subopciones     |
|                                          |
|  Tab "Contenido":                        |
|    -> SchoolSelectorScreen               |
|    -> Con escuela: lista subopciones     |
|                                          |
|  Tab "Reportes":                         |
|    -> SchoolSelectorScreen               |
|    -> Con escuela: lista subopciones     |
|                                          |
|------------------------------------------|
|[Dash] [Admin] [Acad] [Cont] [Repo]      |  <-- BottomNavigationBar
| Dash   Admin   Acad   Cont   Repo       |     (5 items = max recomendado)
+------------------------------------------+
```

**Header en TopAppBar (UserMenuHeader):**
```
+------------------------------------------+
|  [<=]  Dashboard             [JM \/]     |
+------------------------------------------+
Click en [JM \/] -> DropdownMenu:
  * "Cambiar contexto"  -> SchoolSelectorScreen
  * "Cerrar sesion"     -> Logout
```

**Navegacion detallada:**

| Accion                       | Resultado                                             |
|------------------------------|-------------------------------------------------------|
| Tap "Dashboard" (bottom bar) | Contenido cambia a DynamicDashboardScreen              |
| Tap "Admin" (bottom bar)     | Contenido cambia a lista de subopciones               |
| Tap "Usuarios" (subopcion)   | Navega a pantalla completa DynamicScreen(users)        |
| Tap [<=] (back)              | Vuelve a lista de subopciones de Admin                 |
| Tap "Acad" (bottom bar)      | SchoolSelector si no hay escuela, subopciones si hay   |
| Tap "Cont" (bottom bar)      | SchoolSelector si no hay escuela, subopciones si hay   |
| Tap "Repo" (bottom bar)      | SchoolSelector si no hay escuela, subopciones si hay   |
| Tap [JM] (avatar)            | Dropdown: Cambiar contexto / Cerrar sesion             |

#### 5.1.6 super_admin en TABLET

| Orientacion | Layout                    | Comportamiento                  |
|-------------|---------------------------|---------------------------------|
| Portrait    | BottomNavigationBar       | Igual que Mobile (seccion 5.1.5)|
| Landscape   | NavigationRail            | Igual que Desktop (seccion 5.1.3)|

Al rotar el dispositivo, el layout cambia automaticamente segun el ancho resultante vs 840dp.

#### 5.1.7 super_admin - Flujo Escuela Primaria Demo vs Colegio Secundario Demo

```
  super_admin selecciona              super_admin selecciona
  "Escuela Primaria Demo"             "Colegio Secundario Demo"
  ========================             ===========================

  MISMO MENU (mismas opciones)         MISMO MENU (mismas opciones)
  pero datos filtrados por             pero datos filtrados por
  Escuela Primaria                     Colegio Secundario

  Dashboard: KPIs de Primaria          Dashboard: KPIs de Secundario
  Usuarios: de Primaria                Usuarios: de Secundario
  Unidades: de Primaria                Unidades: de Secundario
  Materiales: de Primaria              Materiales: de Secundario
  etc.                                 etc.
```

**Items de scope "system"** (siempre visibles sin importar escuela):
- Dashboard (dashboard global)
- Administracion > Roles
- Administracion > Permisos

**Items de scope "school"** (requieren escuela seleccionada):
- Administracion > Usuarios (de esa escuela)
- Administracion > Escuelas
- Academico > Unidades Academicas
- Academico > Miembros
- Contenido > Materiales
- Contenido > Evaluaciones
- Reportes > Progreso
- Reportes > Estadisticas

---

### 5.2 school_admin

**Usuario:** `admin.primaria@edugo.test` o `admin.secundario@edugo.test`
**Permisos:** 19
**Escuela:** Fija (Escuela Primaria Demo o Colegio Secundario Demo)

#### 5.2.1 Flujo de Entrada

```
  LOGIN                         MENU PRINCIPAL
  ======                        ==============

  [Email:    ]                  Menu directo
  [Password: ]    ---------->   (ya tiene escuela
  [  Login   ]                   en contexto)
```

**No necesita selector de escuela.** El `activeContext` ya incluye `schoolId`.

#### 5.2.2 Menu Visible (5 items top-level)

```
dashboard (Dashboard)                     [icon: dashboard]
|
admin (Administracion)                    [icon: settings]
|  |-- users (Usuarios)                   [icon: users]
|  +-- schools (Escuelas)                 [icon: school]
|
academic (Academico)                      [icon: graduation-cap]
|  +-- units (Unidades Academicas)        [icon: layers]
|
content (Contenido)                       [icon: book-open]
|  |-- materials (Materiales)             [icon: file-text]
|  +-- assessments (Evaluaciones)         [icon: clipboard]
|
reports (Reportes)                        [icon: bar-chart]
   |-- progress (Progreso)                [icon: trending-up]
   +-- stats (Estadisticas)               [icon: pie-chart]
```

**Diferencias vs super_admin:**
- NO tiene: Roles, Permisos (subopciones de Admin)
- NO tiene: Miembros (subopcion de Academico)
- Admin solo muestra: Usuarios, Escuelas
- Academico solo muestra: Unidades

#### 5.2.3 school_admin en DESKTOP (>= 840dp)

```
+--------+----------------------------------------------------------+
| [AP]   |                                                          |
| Admin  |                                                          |
| Primaria|                                                         |
| Esc.   |                                                          |
| Prima  |              CONTENIDO SEGUN TAB                         |
| [v]    |                                                          |
|--------|    Tab "Dashboard":                                      |
|[Dash]  |      -> DynamicDashboardScreen (KPIs de su escuela)     |
| Dashb  |                                                          |
|--------|    Tab "Administracion":                                  |
|[Sett]  |      -> [Usuarios] [Escuelas]                            |
| Admin  |      Click "Usuarios" -> lista usuarios de su escuela    |
|--------|      Click "Escuelas" -> info de su escuela              |
|[Grad]  |                                                          |
| Acad   |    Tab "Academico":                                      |
|--------|      -> [Unidades Academicas]                             |
|[Book]  |      Click "Unidades" -> lista unidades de su escuela    |
| Conte  |                                                          |
|--------|    Tab "Contenido":                                      |
|[Chart] |      -> [Materiales] [Evaluaciones]                      |
| Repor  |      Click "Materiales" -> materiales de su escuela      |
|--------|      Click "Evaluaciones" -> evaluaciones de su escuela  |
|        |                                                          |
|        |    Tab "Reportes":                                       |
|        |      -> [Progreso] [Estadisticas]                        |
+--------+----------------------------------------------------------+
```

**Header del Rail (UserMenuHeader):**
```
+--------+
| [AP]   |  <-- Avatar con iniciales
| Admin  |  <-- Nombre
| Primaria|
| school |  <-- Rol: "School Admin"
| _admin |
| [v]    |
+--------+
Click -> DropdownMenu:
  * "Cerrar sesion"     -> Logout
  (NO tiene "Cambiar contexto" - onSwitchContext es null)
```

#### 5.2.4 school_admin en WEB (>= 840dp)

**Identico a Desktop.** Mismo layout NavigationRail.

#### 5.2.5 school_admin en MOBILE (< 840dp)

```
+------------------------------------------+
|  [<=]  Dashboard             [AP \/]     |
|------------------------------------------|
|                                          |
|         CONTENIDO SEGUN TAB              |
|         (mismo que desktop pero          |
|          a pantalla completa)            |
|                                          |
|  Subopciones se muestran como            |
|  lista vertical al entrar en tab padre   |
|                                          |
|------------------------------------------|
|[Dash] [Admin] [Acad] [Cont] [Repo]      |
| Dash   Admin   Acad   Cont   Repo       |
+------------------------------------------+
```

**Navegacion:**
- 5 items en bottom bar (exacto al maximo recomendado)
- Sin "Cambiar contexto" en menu de avatar
- Solo "Cerrar sesion"

#### 5.2.6 school_admin en TABLET

| Orientacion | Layout                    |
|-------------|---------------------------|
| Portrait    | BottomNav (igual Mobile)  |
| Landscape   | NavigationRail (igual Desktop)|

---

### 5.3 teacher

**Usuario:** `teacher.math@edugo.test`
**Permisos:** 17
**Escuela:** Escuela Primaria Demo (fija)

#### 5.3.1 Flujo de Entrada

```
  LOGIN                         MENU PRINCIPAL
  ======                        ==============
  [Email:    ]                  Menu directo
  [Password: ]    ---------->   (tiene escuela
  [  Login   ]                   en contexto)
```

**No necesita selector de escuela.**

#### 5.3.2 Menu Visible (5 items top-level)

```
dashboard (Dashboard)                     [icon: dashboard]
|
admin (Administracion)                    [icon: settings]
|  +-- users (Usuarios - solo propio)     [icon: users]
|
academic (Academico)                      [icon: graduation-cap]
|  +-- units (Unidades Academicas)        [icon: layers]
|
content (Contenido)                       [icon: book-open]
|  |-- materials (Materiales)             [icon: file-text]
|  +-- assessments (Evaluaciones)         [icon: clipboard]
|
reports (Reportes)                        [icon: bar-chart]
   +-- progress (Progreso)                [icon: trending-up]
```

**Diferencias vs school_admin:**
- Admin: solo Usuarios (su propio perfil)
- NO tiene: Escuelas (en Admin)
- Academico: solo Unidades
- Reportes: solo Progreso (NO Estadisticas)

#### 5.3.3 teacher en DESKTOP (>= 840dp)

```
+--------+----------------------------------------------------------+
| [TM]   |                                                          |
| Prof   |                                                          |
| Math   |                                                          |
| teacher|              CONTENIDO SEGUN TAB                         |
| [v]    |                                                          |
|--------|    Tab "Dashboard":                                      |
|[Dash]  |      -> DynamicDashboardScreen (metricas docente)       |
| Dashb  |                                                          |
|--------|    Tab "Administracion":                                  |
|[Sett]  |      -> [Mi Perfil]                                      |
| Admin  |      Click -> DynamicScreen(users) con filtro propio     |
|--------|                                                          |
|[Grad]  |    Tab "Academico":                                      |
| Acad   |      -> [Unidades Academicas]                             |
|--------|      Click -> DynamicScreen(units) mis unidades          |
|[Book]  |                                                          |
| Conte  |    Tab "Contenido":                                      |
|--------|      -> [Materiales] [Evaluaciones]                      |
|[Chart] |      Click "Materiales" -> mis materiales                |
| Repor  |      Click "Evaluaciones" -> mis evaluaciones            |
|--------|                                                          |
|        |    Tab "Reportes":                                       |
|        |      -> [Progreso]                                       |
|        |      Click -> progreso de mis estudiantes                |
+--------+----------------------------------------------------------+
```

**Header:** Sin "Cambiar contexto" (solo "Cerrar sesion").

#### 5.3.4 teacher en WEB

**Identico a Desktop.**

#### 5.3.5 teacher en MOBILE (< 840dp)

```
+------------------------------------------+
|  [<=]  Dashboard             [TM \/]     |
|------------------------------------------|
|                                          |
|         CONTENIDO SEGUN TAB              |
|                                          |
|------------------------------------------|
|[Dash] [Admin] [Acad] [Cont] [Repo]      |
| Dash   Admin   Acad   Cont   Repo       |
+------------------------------------------+
```

5 items en bottom bar. Subopciones dentro de cada seccion.

#### 5.3.6 teacher en TABLET

| Orientacion | Layout                    |
|-------------|---------------------------|
| Portrait    | BottomNav (igual Mobile)  |
| Landscape   | NavigationRail (igual Desktop)|

---

### 5.4 student

**Usuario:** `student.carlos@edugo.test`
**Permisos:** 9
**Escuela:** Escuela Primaria Demo (fija)

#### 5.4.1 Flujo de Entrada

```
  LOGIN                         MENU PRINCIPAL
  ======                        ==============
  [Email:    ]                  Menu directo
  [Password: ]    ---------->   (tiene escuela
  [  Login   ]                   en contexto)
```

#### 5.4.2 Menu Visible (4 items top-level)

```
dashboard (Dashboard)                     [icon: dashboard]
|
admin (Administracion)                    [icon: settings]
|  +-- users (Usuarios - solo propio)     [icon: users]
|
content (Contenido)                       [icon: book-open]
|  |-- materials (Materiales)             [icon: file-text]
|  +-- assessments (Evaluaciones)         [icon: clipboard]
|
reports (Reportes)                        [icon: bar-chart]
   +-- progress (Progreso)                [icon: trending-up]
```

**Diferencias vs teacher:**
- NO tiene seccion Academico (no ve Unidades)
- Solo 4 items top-level (cabe comodamente en bottom bar)

#### 5.4.3 student en DESKTOP (>= 840dp)

```
+--------+----------------------------------------------------------+
| [SC]   |                                                          |
| Carlos |                                                          |
| student|              CONTENIDO SEGUN TAB                         |
| [v]    |                                                          |
|--------|    Tab "Dashboard":                                      |
|[Dash]  |      -> DynamicDashboardScreen (mi progreso)            |
| Dashb  |                                                          |
|--------|    Tab "Administracion":                                  |
|[Sett]  |      -> [Mi Perfil]                                      |
| Admin  |      Click -> DynamicScreen(users) solo mi perfil       |
|--------|                                                          |
|[Book]  |    Tab "Contenido":                                      |
| Conte  |      -> [Materiales] [Evaluaciones]                      |
|--------|      Click "Materiales" -> mis materiales asignados      |
|[Chart] |      Click "Evaluaciones" -> mis evaluaciones            |
| Repor  |                                                          |
|--------|    Tab "Reportes":                                       |
|        |      -> [Mi Progreso]                                    |
|        |      Click -> DynamicScreen(progress) mi avance          |
+--------+----------------------------------------------------------+
```

**Nota:** Solo 4 items en el rail. Header sin "Cambiar contexto".

#### 5.4.4 student en WEB

**Identico a Desktop.**

#### 5.4.5 student en MOBILE (< 840dp)

```
+------------------------------------------+
|  [<=]  Dashboard             [SC \/]     |
|------------------------------------------|
|                                          |
|         CONTENIDO SEGUN TAB              |
|                                          |
|------------------------------------------|
|[Dash]  [Admin]  [Cont]  [Repo]          |
| Dash    Admin    Cont    Repo            |
+------------------------------------------+
```

**4 items en bottom bar** - bien dentro del limite de 5.

#### 5.4.6 student en TABLET

| Orientacion | Layout                    |
|-------------|---------------------------|
| Portrait    | BottomNav (4 items)       |
| Landscape   | NavigationRail (4 items)  |

---

### 5.5 guardian

**Usuario:** `guardian.roberto@edugo.test`
**Permisos:** 6
**Escuela:** Escuela Primaria Demo (fija)

#### 5.5.1 Flujo de Entrada

```
  LOGIN                         MENU PRINCIPAL
  ======                        ==============
  [Email:    ]                  Menu directo
  [Password: ]    ---------->   (tiene escuela
  [  Login   ]                   en contexto)
```

#### 5.5.2 Menu Visible (4 items top-level)

```
dashboard (Dashboard)                     [icon: dashboard]
|
admin (Administracion)                    [icon: settings]
|  +-- users (Usuarios - solo propio)     [icon: users]
|
content (Contenido)                       [icon: book-open]
|  |-- materials (Materiales)             [icon: file-text]
|  +-- assessments (Evaluaciones)         [icon: clipboard]
|
reports (Reportes)                        [icon: bar-chart]
   +-- progress (Progreso)                [icon: trending-up]
```

**Identico en estructura al student.** Mismos 4 items, mismas subopciones.
La diferencia esta en los DATOS mostrados (progreso de sus hijos vs progreso propio).

#### 5.5.3 guardian en DESKTOP (>= 840dp)

```
+--------+----------------------------------------------------------+
| [RG]   |                                                          |
| Roberto|                                                          |
| guardian|              CONTENIDO SEGUN TAB                         |
| [v]    |                                                          |
|--------|    Tab "Dashboard":                                      |
|[Dash]  |      -> DynamicDashboardScreen (progreso hijos)         |
| Dashb  |                                                          |
|--------|    Tab "Administracion":                                  |
|[Sett]  |      -> [Mi Perfil]                                      |
| Admin  |                                                          |
|--------|    Tab "Contenido":                                      |
|[Book]  |      -> [Materiales] [Evaluaciones]                      |
| Conte  |      (ver materiales/evaluaciones de sus hijos)          |
|--------|                                                          |
|[Chart] |    Tab "Reportes":                                       |
| Repor  |      -> [Progreso]                                       |
|--------|      (progreso de sus hijos)                             |
+--------+----------------------------------------------------------+
```

#### 5.5.4 guardian en WEB

**Identico a Desktop.**

#### 5.5.5 guardian en MOBILE (< 840dp)

```
+------------------------------------------+
|  [<=]  Dashboard             [RG \/]     |
|------------------------------------------|
|                                          |
|         CONTENIDO SEGUN TAB              |
|                                          |
|------------------------------------------|
|[Dash]  [Admin]  [Cont]  [Repo]          |
| Dash    Admin    Cont    Repo            |
+------------------------------------------+
```

4 items en bottom bar.

#### 5.5.6 guardian en TABLET

| Orientacion | Layout                    |
|-------------|---------------------------|
| Portrait    | BottomNav (4 items)       |
| Landscape   | NavigationRail (4 items)  |

---

## 6. Matriz de Navegacion Cruzada

### 6.1 Items Top-Level por Rol

| Item          | super_admin | school_admin | teacher | student | guardian |
|---------------|:-----------:|:------------:|:-------:|:-------:|:--------:|
| Dashboard     |     SI      |      SI      |   SI    |   SI    |    SI    |
| Administracion|     SI      |      SI      |   SI    |   SI    |    SI    |
| Academico     |     SI      |      SI      |   SI    |   NO    |    NO    |
| Contenido     |     SI      |      SI      |   SI    |   SI    |    SI    |
| Reportes      |     SI      |      SI      |   SI    |   SI    |    SI    |
| **Total**     |   **5**     |    **5**     | **5**   | **4**   |  **4**   |

### 6.2 Subopciones por Rol

| Subopcion          | super_admin | school_admin | teacher | student | guardian |
|--------------------|:-----------:|:------------:|:-------:|:-------:|:--------:|
| Admin > Usuarios   |     SI      |      SI      |  propio |  propio |  propio  |
| Admin > Escuelas   |     SI      |      SI      |   NO    |   NO    |    NO    |
| Admin > Roles      |     SI      |      NO      |   NO    |   NO    |    NO    |
| Admin > Permisos   |     SI      |      NO      |   NO    |   NO    |    NO    |
| Acad > Unidades    |     SI      |      SI      |   SI    |   --    |    --    |
| Acad > Miembros    |     SI      |      NO      |   NO    |   --    |    --    |
| Cont > Materiales  |     SI      |      SI      |   SI    |   SI    |    SI    |
| Cont > Evaluaciones|     SI      |      SI      |   SI    |   SI    |    SI    |
| Rep > Progreso     |     SI      |      SI      |   SI    |   SI    |    SI    |
| Rep > Estadisticas |     SI      |      SI      |   NO    |   NO    |    NO    |

### 6.3 Selector de Escuela

| Aspecto                    | super_admin | school_admin | teacher | student | guardian |
|----------------------------|:-----------:|:------------:|:-------:|:-------:|:--------:|
| Necesita selector          |     SI      |      NO      |   NO    |   NO    |    NO    |
| Escuela en activeContext   |     NO      |      SI      |   SI    |   SI    |    SI    |
| Boton "Cambiar contexto"   |     SI      |      NO      |   NO    |   NO    |    NO    |
| onSwitchContext != null    |     SI      |      NO      |   NO    |   NO    |    NO    |

### 6.4 Layout por Plataforma

| Plataforma         | Ancho        | Layout             | Nav Component          |
|--------------------|--------------|--------------------|------------------------|
| Android Phone      | ~360-420dp   | COMPACT            | DSBottomNavigationBar  |
| iPhone             | ~375-430dp   | COMPACT            | DSBottomNavigationBar  |
| Android Tablet P   | ~600-800dp   | COMPACT            | DSBottomNavigationBar  |
| iPad Portrait      | ~768dp       | COMPACT            | DSBottomNavigationBar  |
| Android Tablet L   | ~900-1200dp  | EXPANDED           | DSNavigationRail       |
| iPad Landscape     | ~1024dp      | EXPANDED           | DSNavigationRail       |
| Desktop            | ~1200-1920dp | EXPANDED           | DSNavigationRail       |
| Web Full           | ~1200-1920dp | EXPANDED           | DSNavigationRail       |
| Web Responsive     | Variable     | Depende del ancho  | Cambia dinamicamente   |

### 6.5 Pantallas por screenKey

| screenKey          | Patron SDUI      | Contenido                        |
|--------------------|------------------|----------------------------------|
| dashboard          | dashboard        | KPIs, metricas, acciones rapidas |
| users              | list             | Lista de usuarios (filtrada)     |
| schools            | list             | Lista de escuelas                |
| roles              | list             | Lista de roles                   |
| permissions_mgmt   | list             | Gestion de permisos              |
| units              | list             | Unidades academicas              |
| memberships        | list             | Miembros de escuela              |
| materials          | list             | Materiales de contenido          |
| assessments        | list             | Evaluaciones                     |
| progress           | dashboard/list   | Progreso academico               |
| stats              | dashboard        | Estadisticas agregadas           |

---

## 7. Gaps y Pendientes

### 7.1 Pantallas Dummy / SDUI Faltantes

| screenKey        | Estado Actual          | Accion Necesaria                                    | Prioridad |
|------------------|------------------------|-----------------------------------------------------|-----------|
| dashboard        | DynamicDashboardScreen | Funciona - renderiza KPIs si hay screen_instance    | OK        |
| users            | DynamicScreen generico | Necesita screen_instance en BD para contenido real  | ALTA      |
| schools          | DynamicScreen generico | Necesita screen_instance en BD                      | ALTA      |
| roles            | DynamicScreen generico | Necesita screen_instance en BD                      | MEDIA     |
| permissions_mgmt | DynamicScreen generico | Necesita screen_instance en BD                      | MEDIA     |
| units            | DynamicScreen generico | Necesita screen_instance en BD                      | ALTA      |
| memberships      | DynamicScreen generico | Necesita screen_instance en BD                      | MEDIA     |
| materials        | DynamicScreen generico | Necesita screen_instance en BD                      | ALTA      |
| assessments      | DynamicScreen generico | Necesita screen_instance en BD                      | ALTA      |
| progress         | DynamicScreen generico | Necesita screen_instance en BD                      | ALTA      |
| stats            | DynamicScreen generico | Necesita screen_instance en BD                      | MEDIA     |

**Nota:** DynamicScreen generico carga una pantalla dummy automatica si no hay screen_instance mapeada.
Para las pruebas de navegacion esto es suficiente - se verifica que la pantalla se abre sin errores.

### 7.2 Funcionalidades de Navegacion Faltantes en Codigo

| Funcionalidad                              | Estado       | Detalle                                                    | Prioridad |
|--------------------------------------------|--------------|------------------------------------------------------------|-----------|
| Navegacion a subopciones en COMPACT        | PARCIAL      | Al tap en item padre con children, debe mostrar subopciones| ALTA      |
| Navegacion back desde subopcion            | PARCIAL      | Boton [<=] debe volver a nivel padre                       | ALTA      |
| Overflow menu si > 5 items en bottom bar   | NO EXISTE    | No aplica aun (max actual 5 items)                         | BAJA      |
| DSModalNavigationDrawer para mobile        | NO USADO     | Existe en DS pero no se integra en AdaptiveNavigationLayout| BAJA      |
| DSPermanentNavigationDrawer para desktop   | NO USADO     | Existe en DS pero no se integra                            | BAJA      |
| DSTabs para subopciones                    | NO USADO     | Existe en DS, podria usarse para subopciones               | MEDIA     |
| Deep linking por screenKey                 | NO EXISTE    | Navegacion directa via URL/link a pantalla especifica      | BAJA      |
| Animacion de transicion entre pantallas    | NO EXISTE    | Cambio de contenido sin animacion                          | BAJA      |
| Persistencia de tab seleccionada           | NO EXISTE    | Al volver de subopcion se pierde tab activo                | MEDIA     |
| Badge de notificaciones en items           | EXISTE en DS | DSNavigationBarItem tiene badge pero no se usa             | BAJA      |
| SchoolSelector scope filtering             | PARCIAL      | Detecta si falta schoolId pero la UX puede mejorar         | MEDIA     |

### 7.3 Prioridades para Pruebas

**Fase 1 - Critico (probar primero):**
1. Login con cada usuario de prueba
2. Menu se carga correctamente (items segun rol)
3. Navegacion entre tabs top-level funciona
4. Pantalla dummy se abre para cada subopcion
5. Breakpoint 840dp cambia layout correctamente

**Fase 2 - Importante:**
6. super_admin: selector de escuela funciona
7. super_admin: "Cambiar contexto" cambia escuela
8. Subopciones se muestran al entrar en item padre
9. Logout funciona desde menu de avatar
10. Datos se filtran por escuela del contexto

**Fase 3 - Mejoras:**
11. Navegacion back funciona correctamente
12. Tablet cambia layout al rotar
13. Web responsive cambia layout al redimensionar
14. Pantallas SDUI cargan contenido real

---

## 8. Datos de Prueba Necesarios

### 8.1 Seeds Existentes (OK)

Los siguientes datos ya existen en BD y son suficientes para pruebas:

- 6 usuarios de prueba (super_admin, 2 school_admin, teacher, student, guardian)
- 2 escuelas (Escuela Primaria Demo, Colegio Secundario Demo)
- 5 roles con permisos configurados
- Recursos del menu jerarquico completo
- Role-permission mappings

### 8.2 Screen Instances Necesarias

Para que las pantallas SDUI muestren contenido real (no dummy), se necesitan `screen_instances` en la tabla `ui_config.screen_instances`:

| screenKey        | pattern    | Necesario para pruebas nav? | Necesario para pruebas SDUI? |
|------------------|------------|:---------------------------:|:----------------------------:|
| dashboard        | dashboard  | NO (DynamicDashboardScreen) | SI (KPIs reales)             |
| users            | list       | NO (dummy suficiente)       | SI (lista real)              |
| schools          | list       | NO (dummy suficiente)       | SI (lista real)              |
| roles            | list       | NO (dummy suficiente)       | SI (lista real)              |
| permissions_mgmt | list       | NO (dummy suficiente)       | SI (lista real)              |
| units            | list       | NO (dummy suficiente)       | SI (lista real)              |
| memberships      | list       | NO (dummy suficiente)       | SI (lista real)              |
| materials        | list       | NO (dummy suficiente)       | SI (lista real)              |
| assessments      | list       | NO (dummy suficiente)       | SI (lista real)              |
| progress         | dashboard  | NO (dummy suficiente)       | SI (metricas reales)         |
| stats            | dashboard  | NO (dummy suficiente)       | SI (graficos reales)         |

**Conclusion:** Para pruebas de NAVEGACION no se necesitan nuevos seeds ni screen_instances.
Las pantallas dummy genericas son suficientes para validar que el menu funciona correctamente.

Para pruebas de CONTENIDO SDUI (fase posterior), se necesitaran screen_instances para cada screenKey.

### 8.3 Seeds Adicionales (NO necesarios para nav)

No se necesitan nuevos seeds para pruebas de navegacion.
Los usuarios, escuelas, roles y permisos existentes cubren todos los casos de uso documentados.

---

## Apendice A: Archivos Clave del Codigo

| Archivo                              | Proposito                                         |
|--------------------------------------|---------------------------------------------------|
| `kmp-design/.../DSBottomNavigationBar.kt` | Componente Material3 bottom navigation       |
| `kmp-design/.../DSNavigationRail.kt`      | Componente Material3 navigation rail          |
| `kmp-design/.../DSNavigationDrawer.kt`    | Componentes drawer (modal y permanent)        |
| `kmp-screens/.../AdaptiveNavigationLayout.kt` | Layout adaptativo (breakpoint 840dp)     |
| `kmp-screens/.../MainScreen.kt`           | Orquestador: carga menu, renderiza layout     |
| `kmp-screens/.../DynamicScreen.kt`        | Renderizador de pantallas SDUI                |
| `kmp-screens/.../UserMenuHeader.kt`       | Header con avatar, nombre, rol, dropdown      |
| `kmp-screens/.../SchoolSelectorScreen.kt` | Selector de escuela para super_admin          |
| `modules/auth/.../MenuResponse.kt`        | Modelo de datos desde IAM API                 |
| `modules/dynamic-ui/.../NavigationConfig.kt` | Modelos internos de navegacion             |
| `modules/auth/.../MenuRepositoryImpl.kt`  | Repositorio que obtiene menu del backend      |

## Apendice B: Checklist de Pruebas

### B.1 Pruebas por Usuario

#### super_admin (super@edugo.test)
- [ ] Login exitoso
- [ ] SchoolSelectorScreen aparece para secciones school-scoped
- [ ] Seleccionar Escuela Primaria Demo funciona
- [ ] Menu muestra 5 items top-level
- [ ] Dashboard carga correctamente
- [ ] Administracion muestra 4 subopciones (Users, Schools, Roles, Permisos)
- [ ] Academico muestra 2 subopciones (Unidades, Miembros)
- [ ] Contenido muestra 2 subopciones (Materiales, Evaluaciones)
- [ ] Reportes muestra 2 subopciones (Progreso, Estadisticas)
- [ ] Cada subopcion abre DynamicScreen sin error
- [ ] "Cambiar contexto" en menu avatar funciona
- [ ] Seleccionar Colegio Secundario Demo funciona
- [ ] Logout funciona

#### school_admin (admin.primaria@edugo.test)
- [ ] Login exitoso (directo a menu, sin selector escuela)
- [ ] Menu muestra 5 items top-level
- [ ] Administracion muestra 2 subopciones (Users, Schools)
- [ ] NO tiene Roles ni Permisos en Admin
- [ ] Academico muestra 1 subopcion (Unidades)
- [ ] NO tiene Miembros en Academico
- [ ] Contenido muestra 2 subopciones
- [ ] Reportes muestra 2 subopciones
- [ ] NO tiene "Cambiar contexto" en menu avatar
- [ ] Logout funciona

#### teacher (teacher.math@edugo.test)
- [ ] Login exitoso (directo a menu)
- [ ] Menu muestra 5 items top-level
- [ ] Administracion muestra 1 subopcion (Mi Perfil/Usuarios)
- [ ] Academico muestra 1 subopcion (Unidades)
- [ ] Contenido muestra 2 subopciones
- [ ] Reportes muestra 1 subopcion (Progreso)
- [ ] NO tiene Estadisticas en Reportes
- [ ] Logout funciona

#### student (student.carlos@edugo.test)
- [ ] Login exitoso (directo a menu)
- [ ] Menu muestra 4 items top-level (NO Academico)
- [ ] Administracion muestra 1 subopcion (Mi Perfil)
- [ ] Contenido muestra 2 subopciones
- [ ] Reportes muestra 1 subopcion (Progreso)
- [ ] Logout funciona

#### guardian (guardian.roberto@edugo.test)
- [ ] Login exitoso (directo a menu)
- [ ] Menu muestra 4 items top-level (NO Academico)
- [ ] Administracion muestra 1 subopcion (Mi Perfil)
- [ ] Contenido muestra 2 subopciones
- [ ] Reportes muestra 1 subopcion (Progreso)
- [ ] Logout funciona

### B.2 Pruebas por Plataforma

#### Desktop (>= 840dp)
- [ ] NavigationRail visible a la izquierda
- [ ] UserMenuHeader en header del rail
- [ ] Items con icono + label
- [ ] Click en item cambia contenido a la derecha
- [ ] Subopciones se muestran en area de contenido

#### Web (>= 840dp)
- [ ] Mismo layout que Desktop (NavigationRail)
- [ ] Responsive: achicando ventana < 840dp cambia a BottomNav

#### Mobile (< 840dp)
- [ ] BottomNavigationBar visible abajo
- [ ] UserMenuHeader en TopAppBar
- [ ] Items con icono + label en bottom bar
- [ ] Tap en item cambia contenido arriba
- [ ] Subopciones como lista dentro del contenido

#### Tablet
- [ ] Portrait (< 840dp): layout COMPACT (BottomNav)
- [ ] Landscape (>= 840dp): layout EXPANDED (NavigationRail)
- [ ] Cambio automatico al rotar

---

*Documento generado como referencia para implementacion y testing del menu dinamico EduGo KMP.*
