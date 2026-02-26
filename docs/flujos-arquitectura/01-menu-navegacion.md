# 01 — Menu y Navegacion

## Clases Involucradas

```mermaid
classDiagram
    class Route {
        <<sealed>>
        +Splash
        +Login
        +SchoolSelection
        +Dashboard
        +Dynamic(screenKey, params)
    }
    class NavigationState {
        -backstack: List~Route~
        +push(route)
        +pop()
        +replaceAll(route)
        +currentRoute: Route
    }
    class RouteRegistry {
        -screenKeyToRoute: Map
        +resolve(screenKey): Route
    }
    class MainScreen {
        +selectedKey: String
        +menuItems: List~NavigationItem~
        +activeContext: UserContext
    }
    class AdaptiveNavigationLayout {
        +breakpoint: Breakpoint
        +COMPACT, MEDIUM, EXPANDED
    }
    class NavigationItem {
        +id: String
        +label: String
        +screenKey: String
        +iconName: String
        +children: List~NavigationItem~
        +requiredPermission: String?
    }
    class DataSyncService {
        +currentBundle: StateFlow~UserDataBundle?~
        +syncState: StateFlow~SyncState~
        +fullSync(): Result~UserDataBundle~
        +deltaSync(): Result~UserDataBundle~
        +restoreFromLocal(): UserDataBundle?
        +clearAll()
    }
    class UserDataBundle {
        +menu: MenuResponse
        +permissions: List~String~
        +screens: Map~String, ScreenDefinition~
        +availableContexts: List~UserContext~
        +hashes: Map~String, String~
        +syncedAt: Instant
    }
    class SchoolSelectionScreen {
        +schools: List~SchoolInfo~
        +onSyncComplete: () -> Unit
    }
    class MenuItem {
        +id, label, icon
        +screenKey, children
        +requiredPermission
    }

    NavigationState --> Route
    MainScreen --> NavigationState
    MainScreen --> AdaptiveNavigationLayout
    MainScreen --> NavigationItem
    MainScreen --> DataSyncService : "observa currentBundle"
    DataSyncService --> UserDataBundle
    UserDataBundle --> MenuItem : "menu.items"
    MenuItem --> NavigationItem : "toNavigationItem()"
    RouteRegistry --> Route
    SchoolSelectionScreen --> DataSyncService : "switchContext + fullSync"
```

---

## Flujo Secuencial: Arranque y Carga del Menu

```mermaid
sequenceDiagram
    participant Platform as Plataforma<br/>(Android/iOS/Desktop)
    participant App as App.kt
    participant Koin as KoinApplication
    participant NavState as NavigationState
    participant Splash as SplashScreen
    participant Auth as AuthService
    participant Sync as DataSyncService
    participant Login as LoginScreen
    participant SchoolSel as SchoolSelectionScreen
    participant Main as MainScreen
    participant Backend as IAM Backend

    Platform->>App: iniciar()
    App->>Koin: KoinApplication { allModules() + screenContractsModule }
    App->>NavState: NavigationState(initial=Splash)
    App->>Splash: render()

    Note over Splash: restoreSession + restoreFromLocal + deltaSync<br/>en paralelo con splash delay

    Splash->>Auth: restoreSession()
    alt Sesion valida en storage
        Auth-->>Splash: isAuthenticated() == true
        par Sync en paralelo con delay
            Splash->>Sync: restoreFromLocal()
            Sync-->>Splash: UserDataBundle? (cache local)
            Splash->>Sync: deltaSync()
            Sync->>Backend: POST /api/v1/sync/delta (hashes)
            Backend-->>Sync: DeltaSyncResponse (changed buckets)
            Sync-->>Splash: Result~UserDataBundle~
        and Splash delay
            Splash->>Splash: delay(splashMs)
        end
        Splash->>NavState: replaceAll(Route.Dashboard)
    else Sin sesion
        Auth-->>Splash: isAuthenticated() == false
        Splash->>Splash: delay(splashMs)
        Splash->>NavState: replaceAll(Route.Login)
    end

    Note over Login: Usuario ingresa credenciales

    Login->>Auth: login(credentials)
    Auth->>Backend: POST /api/v1/auth/login
    Backend-->>Auth: LoginResponse { schools, activeContext, token }
    Auth-->>Login: LoginResult.Success(response)
    Login->>App: onLoginSuccess(schools: List~SchoolInfo~)

    alt schools.size > 1
        App->>NavState: replaceAll(Route.SchoolSelection)
        NavState->>SchoolSel: render(schools)
        Note over SchoolSel: Usuario selecciona una escuela
        SchoolSel->>Auth: switchContext(schoolId)
        Auth->>Backend: POST /api/v1/auth/switch-context
        Backend-->>Auth: SwitchContextResponse
        SchoolSel->>Sync: fullSync()
        Sync->>Backend: GET /api/v1/sync/bundle
        Backend-->>Sync: SyncBundleResponse { menu, screens, permissions }
        Sync-->>SchoolSel: Result.Success(bundle)
        SchoolSel->>App: onSyncComplete()
        App->>NavState: replaceAll(Route.Dashboard)
    else schools.size <= 1
        App->>Sync: fullSync()
        Sync->>Backend: GET /api/v1/sync/bundle
        Backend-->>Sync: SyncBundleResponse { menu, screens, permissions }
        Sync-->>App: Result.Success(bundle)
        App->>NavState: replaceAll(Route.Dashboard)
    end

    NavState->>Main: render(Route.Dashboard)
    Main->>Sync: observar currentBundle (StateFlow)
    Note over Main: bundle ya disponible por fullSync previo
    Main->>Main: bundle.menu.items.map { toNavigationItem() }
    Main->>Main: filtrar por activeContext.hasPermission()
    Main->>Main: auto-seleccionar firstLeaf()
    Main->>AdaptiveLayout: render(menuItems, breakpoint)
```

---

## Flujo de Seleccion de Item del Menu

```mermaid
flowchart TD
    A([Usuario toca item del menu]) --> B{¿Tiene children?}
    B -- Si --> C[Expandir/contraer seccion]
    B -- No --> D{¿Tiene screenKey?}
    D -- No --> E[No-op]
    D -- Si --> F[navState.push Route.Dynamic screenKey]
    F --> G{¿Es pantalla especial?}
    G -- schools / subjects --> H[Route.Dynamic con screenKey]
    G -- dashboard / inicio --> I[Route.Dashboard]
    G -- settings --> J[Route.Settings]
    H --> K[DynamicScreen renderiza]
    I --> L[DashboardScreen renderiza]
    J --> M[SettingsScreen renderiza]
    K --> N[MainScreen detecta currentScreenKey\ny renderiza contenido en el area central]
    L --> N
    M --> N
```

---

## Layout Adaptativo por Breakpoint

```mermaid
flowchart LR
    subgraph EXPANDED ["EXPANDED ≥ 840dp (Tablet / Desktop)"]
        direction TB
        E1[PermanentNavigationDrawer]
        E2[Header usuario en drawer]
        E3[Secciones expandibles]
        E4[Area de contenido principal]
        E1 --- E2
        E1 --- E3
        E1 --- E4
    end

    subgraph MEDIUM ["MEDIUM 600–840dp"]
        direction TB
        M1[DSNavigationRail lateral]
        M2[Tabs secundarios para children]
        M3[Area de contenido]
        M1 --- M2
        M1 --- M3
    end

    subgraph COMPACT ["COMPACT < 600dp (Phone)"]
        direction TB
        C1[DSBottomNavigationBar max 5 items]
        C2[Tabs secundarios arriba del contenido]
        C3[Area de contenido]
        C1 --- C2
        C1 --- C3
    end

    BoxWithConstraints --> breakpointFromWidth
    breakpointFromWidth --"≥840dp"--> EXPANDED
    breakpointFromWidth --"600–840dp"--> MEDIUM
    breakpointFromWidth --"<600dp"--> COMPACT
```

---

## Resolucion de Iconos del Backend

```mermaid
flowchart LR
    Backend["Backend: iconName: 'dashboard'"] --> resolveIcon
    resolveIcon{resolveIcon\niconName, filled}
    resolveIcon --"dashboard"--> Icons.Default.Dashboard
    resolveIcon --"school"--> Icons.Default.School
    resolveIcon --"users"--> Icons.Default.Group
    resolveIcon --"subjects"--> Icons.Default.Book
    resolveIcon --"settings"--> Icons.Default.Settings
    resolveIcon --"desconocido"--> Icons.Default.Circle
```

---

## Estado de Navegacion (Backstack)

```mermaid
stateDiagram-v2
    [*] --> Splash : iniciar app
    Splash --> Login : sin sesion
    Splash --> Dashboard : sesion valida (restoreFromLocal + deltaSync)
    Login --> SchoolSelection : login exitoso, >1 escuela
    Login --> Dashboard : login exitoso, <=1 escuela (fullSync)
    SchoolSelection --> Dashboard : switchContext + fullSync completado
    Dashboard --> Dynamic : push(Route.Dynamic)
    Dynamic --> Dynamic : push(Route.Dynamic anidado)
    Dynamic --> Dashboard : pop()
    Dashboard --> Login : sesion expirada / logout (clearAll)
    Login --> [*] : app cerrada
```

---

## Origen de Datos del Menu (Sync Bundle)

```mermaid
flowchart TD
    subgraph Backend
        SyncEndpoint["/api/v1/sync/bundle"]
        DeltaEndpoint["/api/v1/sync/delta"]
    end

    subgraph DataSyncService
        FullSync["fullSync()"]
        DeltaSync["deltaSync()"]
        RestoreLocal["restoreFromLocal()"]
        CurrentBundle["currentBundle: StateFlow"]
    end

    subgraph LocalSyncStore
        SavedBundle["UserDataBundle en storage"]
    end

    subgraph MainScreen
        ObserveBundle["LaunchedEffect(bundle)"]
        BuildNav["bundle.menu.items.map { toNavigationItem() }"]
    end

    SyncEndpoint --> FullSync
    DeltaEndpoint --> DeltaSync
    FullSync --> SavedBundle
    DeltaSync --> SavedBundle
    RestoreLocal --> SavedBundle
    SavedBundle --> CurrentBundle
    CurrentBundle --> ObserveBundle
    ObserveBundle --> BuildNav
```

---

## Diferencias por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Entry point | `MainActivity.setContent {}` | `MainViewController.kt` | `Main.kt (Window)` | `main.kt (CanvasBasedWindow)` |
| Breakpoint inicial | Calculado por `LocalConfiguration.current.screenWidthDp` | Calculado por geometria de ventana | Siempre EXPANDED por defecto | Variable segun viewport |
| Back navigation | Boton fisico Android mapea a `navState.pop()` | Gesture iOS mapea a `pop()` | Alt+← o boton en toolbar | Boton del browser o toolbar |
| PermanentDrawer | Solo en tablets | Solo en iPad | Siempre visible | Solo en pantallas grandes |
| Animaciones | `AnimatedContent` de Compose | `AnimatedContent` de Compose | `AnimatedContent` de Compose | `AnimatedContent` (sin GPU accel) |

---

## Mejoras Propuestas

| Mejora | Justificacion | Prioridad | Estado |
|--------|--------------|-----------|--------|
| Deep-linking | Soportar URLs directas a `Route.Dynamic(screenKey)` en Android/iOS/Web | Alta | Pendiente |
| Cache del menu por rol | ~~Evitar GET al menu en cada re-composicion del MainScreen~~ El menu ahora viene del sync bundle (`DataSyncService.currentBundle`), no se re-fetcha individualmente | Media | **Completado** |
| Animacion de transicion entre pantallas | Actualmente usa `AnimatedContent` sin transicion personalizada | Baja | Pendiente |
| Resolucion de iconos extensible | El mapa de iconos esta hardcodeado; permitir iconos custom del backend (SVG/URL) | Media | Pendiente |
| Menu breadcrumb en EXPANDED | Mostrar la ruta `Escuelas > Lista` en la toolbar para pantallas anidadas | Media | Pendiente |
