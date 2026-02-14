# Analisis y Plan de Trabajo: Sistema de Diseno EduGo KMP

> Fecha: 2026-02-13
> Sprint: 5 - Mejora del Sistema de Diseno
> Documento generado a partir del analisis de:
> - Documentacion de diseno: `/Documentation/GuideDesign/Design/` (393 archivos)
> - Modulo kmp-design actual (48+ componentes, 9 categorias de tokens)
> - Estructura del proyecto KMP (16 modulos, 3 plataformas)

---

## TABLA DE CONTENIDOS

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Analisis de Brechas (GAP Analysis)](#2-analisis-de-brechas)
3. [Plan de Trabajo](#3-plan-de-trabajo)
4. [Estructura del Modulo Samples](#4-estructura-del-modulo-samples)
5. [Definicion de Tareas por Agente](#5-definicion-de-tareas-por-agente)
6. [Catalogo de Pantallas Sample](#6-catalogo-de-pantallas-sample)
7. [Orden de Ejecucion y Dependencias](#7-orden-de-ejecucion-y-dependencias)

---

## 1. RESUMEN EJECUTIVO

### Estado Actual
El proyecto EduGo KMP cuenta con:
- **48 componentes DS** implementados en `kmp-design` (Material Design 3)
- **9 categorias de tokens**: color, spacing, typography, shapes, motion, opacity, elevation, sizes, semantic colors
- **4 pantallas funcionales**: Splash, Login, Home, Settings
- **3 plataformas**: Android/iOS (mobile), Desktop (JVM), Web (WasmJS)
- **393 archivos de documentacion** de diseno con especificaciones completas

### Objetivo
1. **Complementar tokens y componentes** faltantes en `kmp-design`
2. **Crear modulo `samples`** con pantallas de referencia visual (no funcionales)
3. **Cubrir 11 patrones de pantalla** documentados como guias reutilizables
4. **Separar UI por plataforma** solo cuando sea necesario (responsive breakpoints, interacciones desktop)

### Alcance
- Las pantallas `samples` son **solo visuales** con datos de prueba
- Se veran unicamente en **Preview** del IDE (no se enlazan al sistema)
- Sirven como **guia de referencia** para implementar pantallas reales
- Cada patron tendra variante: comun (shared), y platform-specific solo si difiere

---

## 2. ANALISIS DE BRECHAS

### 2.1 Tokens: Implementados vs Documentados

| Categoria | En Codigo | Documentado | Brecha |
|-----------|-----------|-------------|--------|
| **Colors MD3** | 30+ Light/Dark | 30+ Light/Dark | Completo |
| **Extended Colors** | success, warning, info (6 colores) | success, warning, info (12 colores c/u) | Faltan containers |
| **Spacing** | spacing0-16 (0-64dp) | 16 valores + responsive | Faltan variantes responsive |
| **Elevation** | 6 niveles + 9 semanticos | 6 niveles + 15+ semanticos | Faltan aliases desktop/web |
| **Typography** | 13 estilos MD3 | 13 estilos + responsive | Falta escala responsive |
| **Shapes** | 7 radius + pre-built | 8 radius + asimetricos | Faltan shapes asimetricos |
| **Motion** | 16 duraciones + easing | 20+ duraciones + platform | Parcialmente completo |
| **Opacity** | StateLayer + Surface + Effect | StateLayer + Surface + Effect | Completo |
| **Sizes** | Icons, Avatar, TouchTarget | Icons, Avatar, TouchTarget + responsive | Faltan breakpoints |

### 2.2 Componentes: Implementados vs Documentados

| Categoria | Implementados | Documentados Extra | Prioridad |
|-----------|--------------|-------------------|-----------|
| **Buttons** | 6 + FABs | SegmentedButton, StateButton | ALTA |
| **Cards** | 3 variantes | Card con media, Card horizontal | MEDIA |
| **Inputs** | 4 (text, password, search, filled) | NumberInput, ChipInput, MultilineCounter | MEDIA |
| **Selection** | 5 (checkbox, radio, switch, slider, chip) | DatePicker, TimePicker | BAJA |
| **Navigation** | 5 (topbar, bottom, tabs, drawer, rail) | Breadcrumbs, Stepper | MEDIA |
| **Dialogs** | 4 (alert, basic, bottom sheet, fullscreen) | FormDialog | BAJA |
| **Overlays** | 4 (toast, menu, popup, tooltip) | Snackbar (vacio) | ALTA |
| **Progress** | 3 (circular, linear, skeleton) | Pull-to-refresh, SwipeRefresh | MEDIA |
| **Lists** | 2 (item, group) | ExpandableList, StickyHeaders, SwipeActions | ALTA |
| **Media** | 3 (avatar, badge, divider) | AudioPlayer, PasswordStrength, Image | MEDIA |

### 2.3 Componentes Nuevos Necesarios (Para Samples)

Estos componentes NO existen en `kmp-design` pero se necesitan para crear samples completos:

| Componente | Patron que lo necesita | Prioridad |
|-----------|----------------------|-----------|
| **DSSnackbar** | Form, Login, cualquier accion | ALTA |
| **DSSegmentedButton** | Dashboard, Settings, Filtros | ALTA |
| **DSExpandableListItem** | Settings, Lists | ALTA |
| **DSPasswordStrengthIndicator** | Login/Register | MEDIA |
| **DSStickyHeader** | Lists, Contacts | MEDIA |
| **DSSwipeToAction** | Lists (mobile) | MEDIA |
| **DSEmptyState** | Lists, Search, Dashboard | ALTA |
| **DSPullToRefresh** | Lists (mobile) | MEDIA |
| **DSNumberInput** | Forms | BAJA |
| **DSBreadcrumb** | Navigation (desktop/web) | BAJA |
| **DSStepper** | Onboarding, Forms multi-paso | MEDIA |
| **DSImageCard** | Dashboard, Detail | BAJA |

### 2.4 Tokens Faltantes para Plataformas Especificas

**Desktop (nuevos)**:
- `DesktopSpacing`: margenes de ventana, panel gaps
- `DesktopClickTargets`: 32-44dp (vs 48dp mobile)
- Hover states en componentes interactivos

**Web/WASM (nuevos)**:
- `WebZIndex`: sistema de capas (base=0, nav=10, dropdown=100, modal=1000)
- `ResponsiveBreakpoints`: mobile <600dp, tablet 600-900dp, desktop 900dp+
- Responsive typography scale

---

## 3. PLAN DE TRABAJO

### Fase 1: Preparacion de Infraestructura
> Dependencia: Ninguna | Agente: Infra

1. **Crear modulo `kmp-samples`** con estructura Gradle
   - Dependencia en `kmp-design` y `kmp-resources`
   - Sin dependencia en `modules:di`, `modules:auth`, ni ningun servicio
   - Targets: Android, Desktop, WasmJS (iOS comparte Android)
   - Solo `@Preview` composables, sin navigation real

2. **Estructura de carpetas del modulo samples**:
```
kmp-samples/
  src/
    commonMain/kotlin/com/edugo/kmp/samples/
      patterns/          # Pantallas patron (shared)
        LoginSample.kt
        FormSample.kt
        ListSample.kt
        DetailViewSample.kt
        DashboardSample.kt
        SearchSample.kt
        SettingsSample.kt
        ModalSample.kt
        NavigationSample.kt
        OnboardingSample.kt
        EmptyStateSample.kt
      catalog/            # Catalogo de componentes individuales
        ButtonsCatalog.kt
        InputsCatalog.kt
        CardsCatalog.kt
        SelectionCatalog.kt
        NavigationCatalog.kt
        DialogsCatalog.kt
        OverlaysCatalog.kt
        ProgressCatalog.kt
        ListsCatalog.kt
        MediaCatalog.kt
      data/               # Datos fake para las samples
        SampleData.kt
    desktopMain/kotlin/com/edugo/kmp/samples/
      patterns/           # Variantes desktop-only
        DashboardDesktopSample.kt   # Layout multi-panel
        NavigationDesktopSample.kt  # NavigationRail + PermanentDrawer
        ListDesktopSample.kt        # Con hover states, context menu
    wasmJsMain/kotlin/com/edugo/kmp/samples/
      patterns/           # Variantes web-only
        NavigationWebSample.kt      # Responsive nav
        DashboardWebSample.kt       # Responsive grid
  build.gradle.kts
```

### Fase 2: Complementar Tokens y Componentes
> Dependencia: Ninguna (paralelo con Fase 1) | Agentes: Tokens, Components

#### 2A. Tokens Nuevos (en `kmp-design`)

| Archivo | Contenido | Detalle |
|---------|-----------|---------|
| `ResponsiveTokens.kt` | Breakpoints + helpers | `Breakpoint.COMPACT/MEDIUM/EXPANDED`, `currentBreakpoint()` composable |
| `ZIndexTokens.kt` | Capas z-index para web | `ZIndex.base/navigation/dropdown/modal/tooltip/overlay` |
| `PlatformSpacing.kt` | Spacing platform-aware | Desktop window margins, web responsive padding |

#### 2B. Componentes Nuevos (en `kmp-design`)

**Prioridad ALTA** (necesarios para samples basicos):

| Componente | Archivo | Descripcion |
|-----------|---------|-------------|
| `DSSnackbar` | `components/overlays/DSSnackbar.kt` | Snackbar con accion, variantes por MessageType |
| `DSSegmentedButton` | `components/buttons/DSSegmentedButton.kt` | Grupo de botones segmentados (single/multi select) |
| `DSEmptyState` | `components/feedback/DSEmptyState.kt` | Estado vacio con icono, titulo, descripcion, CTA |
| `DSExpandableListItem` | `components/lists/DSExpandableListItem.kt` | ListItem expandible con contenido colapsable |

**Prioridad MEDIA** (mejoran la calidad de samples):

| Componente | Archivo | Descripcion |
|-----------|---------|-------------|
| `DSPasswordStrength` | `components/inputs/DSPasswordStrength.kt` | Indicador visual de fuerza de password |
| `DSStickyHeader` | `components/lists/DSStickyHeader.kt` | Header sticky para listas agrupadas |
| `DSStepper` | `components/navigation/DSStepper.kt` | Indicador de pasos (horizontal/vertical) |
| `DSImageCard` | `components/cards/DSImageCard.kt` | Card con imagen hero + contenido |

### Fase 3: Crear Pantallas Sample (Patrones)
> Dependencia: Fase 1 + Fase 2A completada | Agentes: Samples (3 agentes paralelos)

Cada patron se implementa como:
1. **Composable Content** (stateless, datos por parametro)
2. **@Preview Light** con datos de prueba
3. **@Preview Dark** con datos de prueba
4. **Variante Desktop** solo si el layout difiere significativamente
5. **Variante Web** solo si hay consideraciones responsive

### Fase 4: Crear Catalogo de Componentes
> Dependencia: Fase 2B completada | Agente: Catalog

Cada archivo de catalogo muestra TODOS los estados y variantes de una categoria:
- Normal, Disabled, Loading, Error, Success
- Tamanios (small, medium, large)
- Variantes (filled, outlined, tonal, etc.)
- Con/sin iconos, con/sin texto secundario

---

## 4. ESTRUCTURA DEL MODULO SAMPLES

### 4.1 build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget()
    jvm("desktop")
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmp-design"))
            implementation(project(":kmp-resources"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
        }
    }
}
```

### 4.2 SampleData.kt (datos fake compartidos)

```kotlin
object SampleData {
    // Login
    val emailPlaceholder = "usuario@ejemplo.com"
    val passwordPlaceholder = "********"

    // Lists
    val contacts = listOf(
        Contact("Ana Garcia", "ana@email.com", "AG"),
        Contact("Bruno Lopez", "bruno@email.com", "BL"),
        // ... 10-15 items
    )

    // Dashboard
    val metrics = listOf(
        Metric("Usuarios Activos", "1,234", "+12%"),
        Metric("Cursos Completados", "456", "+5%"),
        // ...
    )

    // Search
    val searchResults = listOf(...)
    val recentSearches = listOf("Matematicas", "Historia", "Fisica")

    // Settings
    val settingsGroups = listOf(...)

    // Navigation
    val navigationItems = listOf(
        NavItem("Inicio", Icons.Default.Home),
        NavItem("Cursos", Icons.Default.School),
        NavItem("Perfil", Icons.Default.Person),
        NavItem("Config", Icons.Default.Settings),
    )
}
```

---

## 5. DEFINICION DE TAREAS POR AGENTE

### Equipo de Agentes

| Agente | Rol | Responsabilidad |
|--------|-----|----------------|
| **infra** | Infraestructura | Crear modulo samples, configurar Gradle, estructura de carpetas |
| **tokens** | Tokens & Theme | Nuevos tokens (responsive, z-index, platform spacing), complementar ExtendedColorScheme |
| **components** | Componentes DS | Implementar componentes nuevos en `kmp-design` |
| **samples-patterns** | Pantallas Patron | Crear las 11 pantallas sample en commonMain |
| **samples-platform** | Variantes Platform | Crear variantes desktop y web donde aplique |
| **catalog** | Catalogo Visual | Crear catalogo de componentes por categoria |

### Detalle de Tareas

#### AGENTE: infra
```
Tarea 1.1: Crear modulo kmp-samples con build.gradle.kts
Tarea 1.2: Registrar modulo en settings.gradle.kts
Tarea 1.3: Crear estructura de carpetas (commonMain, desktopMain, wasmJsMain)
Tarea 1.4: Crear SampleData.kt con todos los datos fake
Tarea 1.5: Crear SampleThemeWrapper.kt (helper para previews con InitStringsForPreview + EduGoTheme)
Tarea 1.6: Verificar compilacion del modulo vacio
```

#### AGENTE: tokens
```
Tarea 2.1: Crear ResponsiveTokens.kt (Breakpoint enum, currentBreakpoint composable)
Tarea 2.2: Crear ZIndexTokens.kt (capas z-index)
Tarea 2.3: Complementar ExtendedColorScheme con containers (successContainer, warningContainer, infoContainer, etc.)
Tarea 2.4: Agregar aliases semanticos faltantes en Elevation.kt (tooltip, menu, appBar)
Tarea 2.5: Agregar tests para nuevos tokens
Tarea 2.6: Verificar compilacion de kmp-design
```

#### AGENTE: components
```
Tarea 3.1: Implementar DSSnackbar (con MessageType, accion, auto-dismiss)
Tarea 3.2: Implementar DSSegmentedButton (single y multi select)
Tarea 3.3: Implementar DSEmptyState (icono, titulo, descripcion, boton CTA)
Tarea 3.4: Implementar DSExpandableListItem (animacion expand/collapse)
Tarea 3.5: Implementar DSPasswordStrength (barra + nivel + requisitos)
Tarea 3.6: Implementar DSStepper (horizontal, vertical, con estados)
Tarea 3.7: Agregar Previews a cada componente nuevo
Tarea 3.8: Verificar compilacion de kmp-design
```

#### AGENTE: samples-patterns
```
Tarea 4.1:  LoginSample - Email/password, forgot password, remember me, social auth, error states
Tarea 4.2:  FormSample - Formulario multi-campo con validacion visual, grupos, submit
Tarea 4.3:  ListSample - Lista con avatar, acciones, seccion sticky, empty state
Tarea 4.4:  DetailViewSample - Header hero, secciones de contenido, acciones flotantes
Tarea 4.5:  DashboardSample - KPI cards, lista rapida, acciones, graficos placeholder
Tarea 4.6:  SearchSample - Barra busqueda, filtros chip, resultados, recientes, empty
Tarea 4.7:  SettingsSample - Grupos expandibles, toggles, seleccion, navegacion sub-pantalla
Tarea 4.8:  ModalSample - Bottom sheet, dialog, fullscreen dialog, form dialog
Tarea 4.9:  NavigationSample - Tab bar, drawer, rail, breadcrumbs, back stack
Tarea 4.10: OnboardingSample - Pages con stepper, skip, permisos, setup
Tarea 4.11: EmptyStateSample - Sin datos, error red, sin resultados, primer uso
```

#### AGENTE: samples-platform
```
Tarea 5.1: DashboardDesktopSample - Layout multi-panel con NavigationRail
Tarea 5.2: NavigationDesktopSample - PermanentDrawer + content area
Tarea 5.3: ListDesktopSample - Context menu en right-click, hover highlight
Tarea 5.4: NavigationWebSample - Responsive: bottom nav (mobile) vs rail (tablet) vs drawer (desktop)
Tarea 5.5: DashboardWebSample - Grid responsive por breakpoint
```

#### AGENTE: catalog
```
Tarea 6.1:  ButtonsCatalog - Todos los botones, estados, tamanios, con/sin iconos
Tarea 6.2:  InputsCatalog - Todos los campos, estados, validacion visual
Tarea 6.3:  CardsCatalog - Todas las variantes, clickable/static, con contenido
Tarea 6.4:  SelectionCatalog - Checkbox, radio, switch, slider, chips en todos sus estados
Tarea 6.5:  NavigationCatalog - TopBar variantes, BottomNav, Tabs, Rail, Drawer
Tarea 6.6:  DialogsCatalog - Alert, Basic, BottomSheet, FullScreen, con/sin acciones
Tarea 6.7:  OverlaysCatalog - Toast, Menu, Popup, Tooltip, Snackbar
Tarea 6.8:  ProgressCatalog - Circular, Linear, Skeleton en distintos estados
Tarea 6.9:  ListsCatalog - Items simples, con avatar, expandibles, sticky headers
Tarea 6.10: MediaCatalog - Avatar tamanios, Badge conteos, Dividers variantes
```

---

## 6. CATALOGO DE PANTALLAS SAMPLE

### 6.1 LoginSample

```
+------------------------------------------+
|              [Logo/Titulo]               |
|                                          |
|  +------------------------------------+  |
|  | Email                               |  |
|  +------------------------------------+  |
|                                          |
|  +------------------------------------+  |
|  | Password                 [Eye]      |  |
|  +------------------------------------+  |
|  [===] Password Strength: Fuerte        |
|                                          |
|  [x] Recordarme      Olvide password >  |
|                                          |
|  +------------------------------------+  |
|  |          INICIAR SESION             |  |
|  +------------------------------------+  |
|                                          |
|  ---- o continuar con ----              |
|                                          |
|  [Google]  [Apple]  [Facebook]          |
|                                          |
|  No tienes cuenta? Registrate >         |
+------------------------------------------+
```

**Variantes Preview**:
- Light theme con campos vacios
- Dark theme con datos prellenados
- Estado de error (credenciales invalidas)
- Estado loading (boton con spinner)

### 6.2 FormSample

```
+------------------------------------------+
| < Formulario de Registro                 |
|------------------------------------------|
| Datos Personales                         |
|  +------------------------------------+  |
|  | Nombre completo*                    |  |
|  +------------------------------------+  |
|  +------------------+ +---------------+  |
|  | Fecha nacimiento | | Genero    [v] |  |
|  +------------------+ +---------------+  |
|                                          |
| Contacto                                 |
|  +------------------------------------+  |
|  | Email*                              |  |
|  +------------------------------------+  |
|  helper: Usaremos este para notificaciones|
|  +------------------------------------+  |
|  | Telefono                            |  |
|  +------------------------------------+  |
|                                          |
| Preferencias                             |
|  [x] Recibir notificaciones             |
|  [ ] Acepto terminos y condiciones*      |
|                                          |
|  +------------------------------------+  |
|  |            ENVIAR                   |  |
|  +------------------------------------+  |
+------------------------------------------+
```

**Variantes Preview**:
- Formulario vacio
- Con errores de validacion
- Formulario completo valido
- Estado submitting

### 6.3 ListSample

```
+------------------------------------------+
| Contactos                    [Search] [+]|
|------------------------------------------|
| A                                        |
|  [AG] Ana Garcia                    [>]  |
|       ana@email.com                      |
|  ----------------------------------------|
|  [AL] Alberto Lopez               [>]   |
|       alberto@email.com                  |
| B                                        |
|  [BM] Bruno Martinez              [>]   |
|       bruno@email.com                    |
|  ----------------------------------------|
| ... (scroll)                             |
|                                          |
| [FAB +]                                 |
+------------------------------------------+
```

**Variantes Preview**:
- Lista con datos
- Lista vacia (EmptyState)
- Lista con seleccion multiple
- Lista con swipe actions (mobile)

### 6.4 DetailViewSample

```
+------------------------------------------+
| < Detalle                      [...] |
|------------------------------------------|
| +--------------------------------------+ |
| |                                      | |
| |         [Hero Image/Banner]          | |
| |                                      | |
| +--------------------------------------+ |
|                                          |
| Titulo del Curso                         |
| Subtitulo o categoria                    |
|                                          |
| [Tag1] [Tag2] [Tag3]                    |
|                                          |
| Descripcion                              |
| Lorem ipsum dolor sit amet...            |
|                                          |
| Detalles                                 |
| Duracion:        3 horas                 |
| Nivel:           Intermedio              |
| Instructor:      Juan Perez              |
|                                          |
| +------------------------------------+   |
| |          INSCRIBIRSE               |   |
| +------------------------------------+   |
+------------------------------------------+
```

### 6.5 DashboardSample

```
+------------------------------------------+
| Dashboard                    [Notif] [?] |
|------------------------------------------|
| Buenos dias, Usuario                     |
|                                          |
| +--------+ +--------+ +--------+        |
| | 1,234  | |  456   | |  89%   |        |
| |Usuarios| |Cursos  | |Compl.  |        |
| | +12%   | | +5%    | | +3%    |        |
| +--------+ +--------+ +--------+        |
|                                          |
| Actividad Reciente                       |
|  [icon] Maria completo Curso A   2h     |
|  [icon] Pedro inicio Curso B     5h     |
|  [icon] Ana obtuvo certificado   1d     |
|                                          |
| Acciones Rapidas                         |
| [Nuevo Curso] [Ver Reportes] [Config]   |
+------------------------------------------+
```

**Variante Desktop** (multi-panel):
```
+------+---------------------------+----------+
| Rail |  KPI Cards (3 col)       | Activity |
|      |                           | Panel    |
| Home |  +------+ +------+ +---+ |          |
| Dash |  |1,234 | | 456  | |89%| | Recent   |
| Users|  +------+ +------+ +---+ | items... |
| ...  |                           |          |
|      |  Chart/Table Area         | Quick    |
|      |                           | Actions  |
+------+---------------------------+----------+
```

### 6.6 SearchSample

```
+------------------------------------------+
| +--------------------------------------+ |
| | [Q] Buscar cursos...          [X]    | |
| +--------------------------------------+ |
|                                          |
| [Matematicas] [Ciencias] [Historia] [+] |
|                                          |
| Recientes                      Limpiar  |
|  [clock] Algebra lineal                 |
|  [clock] Fisica cuantica                |
|                                          |
| --- resultados para "mate" ---          |
|  [img] Matematicas I                    |
|        Fundamentos de algebra    4.5*   |
|  [img] Matematicas II                   |
|        Calculo diferencial       4.2*   |
|  [img] Estadistica                      |
|        Probabilidad y datos      4.8*   |
+------------------------------------------+
```

**Variantes**: con resultados, sin resultados (empty), buscando (loading)

### 6.7 SettingsSample

```
+------------------------------------------+
| < Configuracion                          |
|------------------------------------------|
| Cuenta                                [>]|
|  [avatar] Juan Perez                     |
|           juan@edugo.com                 |
|------------------------------------------|
| Apariencia                               |
|  Tema          [Light|Dark|System]       |
|  Tamano texto  [-----|o--------]         |
|------------------------------------------|
| Notificaciones                           |
|  Push                          [ON ]     |
|  Email                         [OFF]     |
|  Sonido                        [ON ]     |
|------------------------------------------|
| Privacidad                            [>]|
| Idioma                   Espanol      [>]|
| Acerca de                v1.0.0       [>]|
|------------------------------------------|
|  [Cerrar Sesion]                         |
+------------------------------------------+
```

### 6.8 ModalSample

```
Muestra 4 variantes en una sola pantalla:

1. Bottom Sheet
+------------------------------------------+
| [====]  (drag handle)                    |
| Seleccionar Opcion                       |
|  ( ) Opcion A                            |
|  (x) Opcion B                            |
|  ( ) Opcion C                            |
| [CANCELAR]  [ACEPTAR]                   |
+------------------------------------------+

2. Alert Dialog
+------------------------------------------+
| [!] Confirmar Eliminacion                |
|                                          |
| Esta seguro de eliminar este elemento?   |
| Esta accion no se puede deshacer.        |
|                                          |
|              [CANCELAR] [ELIMINAR]       |
+------------------------------------------+

3. Fullscreen Dialog
+------------------------------------------+
| [X] Nuevo Elemento              [GUARDAR]|
|------------------------------------------|
| (formulario completo...)                 |
+------------------------------------------+

4. Form Dialog
+------------------------------------------+
| Agregar Comentario                       |
| +------------------------------------+   |
| |                                    |   |
| | (multiline input)                  |   |
| |                                    |   |
| +------------------------------------+   |
|              [CANCELAR] [ENVIAR]         |
+------------------------------------------+
```

### 6.9 NavigationSample

Muestra variantes de navegacion:
- **Mobile**: BottomNavigationBar con 4-5 items + badge
- **Tablet**: NavigationRail lateral
- **Desktop**: PermanentNavigationDrawer
- **Tabs**: Primary tabs y Secondary tabs
- **Breadcrumbs**: Home > Seccion > Sub-seccion > Actual

### 6.10 OnboardingSample

```
Pagina 1/4:
+------------------------------------------+
|                                   Skip > |
|                                          |
|         [Ilustracion/Icono grande]       |
|                                          |
|        Bienvenido a EduGo                |
|   Tu plataforma de aprendizaje          |
|                                          |
|           o  o  o  o                     |
|                                          |
|  +------------------------------------+  |
|  |           SIGUIENTE                 |  |
|  +------------------------------------+  |
+------------------------------------------+
```

### 6.11 EmptyStateSample

Muestra 4 variantes:
- **Sin datos**: "Aun no tienes cursos" + [Explorar Cursos]
- **Error de red**: "Sin conexion" + [Reintentar]
- **Sin resultados**: "No encontramos resultados para X" + [Limpiar filtros]
- **Primer uso**: "Comienza tu primer curso!" + [Empezar]

---

## 7. ORDEN DE EJECUCION Y DEPENDENCIAS

```
Fase 1 (infra) ─────────────────┐
                                 ├──> Fase 3 (samples-patterns)
Fase 2A (tokens) ───────────────┤         |
                                 │         v
Fase 2B (components) ───────────┤    Fase 5 (samples-platform)
                                 │
                                 └──> Fase 4 (catalog)
```

### Ejecucion Paralela Optima

| Paso | Agentes Activos | Tareas |
|------|----------------|--------|
| **1** | infra, tokens, components | Crear modulo + tokens + componentes (paralelo) |
| **2** | samples-patterns (x3), catalog | 11 patrones + 10 catalogos (paralelo, tras paso 1) |
| **3** | samples-platform | 5 variantes desktop/web (tras paso 2) |

### Estimacion de Tareas

| Fase | Tareas | Archivos Nuevos | Dependencia |
|------|--------|-----------------|-------------|
| Fase 1: Infra | 6 | ~5 | Ninguna |
| Fase 2A: Tokens | 6 | ~4 | Ninguna |
| Fase 2B: Components | 8 | ~7 | Ninguna |
| Fase 3: Patterns | 11 | ~11 | Fases 1, 2A |
| Fase 4: Catalog | 10 | ~10 | Fases 1, 2B |
| Fase 5: Platform | 5 | ~5 | Fase 3 |
| **TOTAL** | **46** | **~42 archivos** | |

---

## NOTAS IMPORTANTES

### Principios de Diseno para Samples

1. **Composables stateless**: Todas las samples reciben datos por parametro
2. **Solo @Preview**: No se enlazan con ningun sistema, sin navigation real
3. **Datos de prueba**: Todo usa `SampleData` con datos hardcodeados en espanol
4. **Light + Dark**: Cada sample tiene minimo 2 previews (light y dark)
5. **Platform-specific solo cuando difiere**: La mayoria es commonMain
6. **Naming convention**: `{Pattern}Sample.kt`, `{Category}Catalog.kt`
7. **Sin over-engineering**: Simple, legible, como referencia visual

### Criterio para Separar UI por Plataforma

Se crea variante platform-specific SOLO cuando:
- El **layout cambia estructuralmente** (ej: dashboard multi-panel en desktop)
- Hay **interacciones propias** de la plataforma (ej: right-click en desktop)
- El **componente de navegacion difiere** (ej: bottom nav vs rail vs drawer)
- NO se separa por diferencias cosmeticas menores (esas van en tokens)

### Reuso de Codigo

- **commonMain**: ~80% del codigo de samples
- **desktopMain**: ~15% (layouts multi-panel, interacciones mouse)
- **wasmJsMain**: ~5% (responsive breakpoints)

---

## APENDICE: RESUMEN DE COMPONENTES DS EXISTENTES

### Referencia Rapida (48 componentes en kmp-design)

**Buttons**: DSFilledButton, DSOutlinedButton, DSElevatedButton, DSTonalButton, DSTextButton, DSIconButton, DSFloatingActionButton
**Cards**: DSFilledCard, DSElevatedCard, DSOutlinedCard
**Inputs**: DSOutlinedTextField, DSFilledTextField, DSPasswordField, DSSearchBar
**Selection**: DSCheckbox, DSRadioButton, DSSwitch, DSSlider, DSChip
**Navigation**: DSTopAppBar, DSBottomNavigationBar, DSTabs, DSModalNavigationDrawer, DSPermanentNavigationDrawer, DSNavigationRail
**Dialogs**: DSAlertDialog, DSBasicDialog, DSBottomSheet, DSFullScreenDialog
**Overlays**: DSToast, DSMenu, DSPopup, DSPlainTooltip, DSRichTooltip
**Progress**: DSCircularProgress, DSLinearProgress, DSSkeleton
**Lists**: DSListItem, DSListGroup
**Media**: DSAvatar, DSBadge, DSDivider, DSVerticalDivider, DSInsetDivider

### Tokens Disponibles

- `Spacing.spacing0` ... `Spacing.spacing16` (0-64dp)
- `Sizes.iconSmall/Medium/Large/XLarge/XXLarge/Massive`
- `Sizes.Avatar.small/medium/large/xlarge/xxlarge`
- `Sizes.TouchTarget.minimum/comfortable/generous`
- `Elevation.level0` ... `level5` + aliases semanticos
- `Shapes.none/extraSmall/small/medium/large/extraLarge/full`
- `CornerRadius.none` ... `full`
- `AnimationDuration.*`, `ScreenDuration.*`, `InteractiveDuration.*`
- `AnimationEasing.*`
- `StateLayerOpacity.*`, `SurfaceOpacity.*`, `EffectOpacity.*`
- `ButtonSpacing.*`, `CardSpacing.*`, `ListSpacing.*`, `FormSpacing.*`
- `LocalExtendedColorScheme.current` (success, warning, info)
