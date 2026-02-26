# 08 — Arquitectura Global y DI

## Capas de la Arquitectura

```mermaid
graph TB
    subgraph Platforms ["Plataformas (expect/actual)"]
        AND[Android\nMainActivity]
        IOS[iOS\nMainViewController]
        DESK[Desktop\nMain.kt Window]
        WEB[WasmJS\nCanvasBasedWindow]
    end

    subgraph KmpScreens ["kmp-screens (Presentacion Compartida)"]
        APP[App.kt\nKoin + Tema + Navegacion]
        NAVSTATE[NavigationState\nBackstack]
        MAINSCREEN[MainScreen\nSidebar + Content]
        DYNSCREEN[DynamicScreen\nPatternRouter + Toolbar]
        TOOLBAR[DynamicToolbar\nAcciones contextuales por pattern]
        CONTRACTS[ScreenContracts\nlogica de negocio UI]
        SCREENS["Screens especificas\n(Login, Splash, Dashboard...)"]
        SCHOOLSEL[SchoolSelectionScreen\nPost-login multi-escuela]
        CONNBANNER[ConnectivityBanner\nEstado offline/sync]
        STALEINDICATOR[StaleDataIndicator\nDatos en cache]
    end

    subgraph DynamicUi ["modules/dynamic-ui (Dominio UI)"]
        VM[DynamicScreenViewModel\nState + Effects]
        EO[EventOrchestrator\nPermisos + Routing]
        CSL[CachedScreenLoader\nSDUI definitions]
        CDL[CachedDataLoader\nDatos de pantalla]
        DSS[DataSyncService\nBundle + fullSync + deltaSync]
        LSS[LocalSyncStore\nPersistencia local del bundle]
        SYNCREP[SyncRepository\nHTTP sync endpoints]
        MQ[MutationQueue\nCola offline de escrituras]
        SE[SyncEngine\nFlush mutations al reconectar]
        CR[ConflictResolver\nResolucion de conflictos]
        CSM[ConnectivitySyncManager\nAuto-sync al reconectar]
        CCFG[CacheConfig\nTTL + politicas de cache]
        RST[RecentScreenTracker\nPantallas recientes para prefetch]
    end

    subgraph Auth ["modules/auth (Dominio Auth)"]
        AS[AuthServiceImpl\nStateFlow + Mutex]
        TRM[TokenRefreshManagerImpl\nAuto-refresh]
        CB[CircuitBreaker\nResiliencia]
        RL2[RateLimiter\nProteccion login]
    end

    subgraph Network ["modules/network (Infraestructura)"]
        HTTP[EduGoHttpClient\nKtor]
        IC[InterceptorChain\nAuth + Headers]
        EM[ExceptionMapper\nError handling]
        NO[NetworkObserver\ncreateNetworkObserver\nEstado de conectividad]
    end

    subgraph Storage ["modules/storage (Infraestructura)"]
        SES[SafeEduGoStorage\nWrapper seguro]
        ES[EduGoStorage\nType-safe wrapper]
        S[Settings\nmultiplatform-settings]
    end

    subgraph DI ["modules/di (Inyeccion de Dependencias)"]
        KI[KoinInitializer\nallModules]
        NM[networkModule]
        SM[storageModule]
        AM[authModule]
        DUM[dynamicUiModule]
        SCM[screenContractsModule\nen kmp-screens]
    end

    Platforms --> KmpScreens
    KmpScreens --> DynamicUi
    KmpScreens --> Auth
    DynamicUi --> Auth
    DynamicUi --> Network
    DynamicUi --> Storage
    Auth --> Network
    Auth --> Storage
    DI --> Platforms
    DI --> KmpScreens
    DI --> DynamicUi
    DI --> Auth
    DI --> Network
    DI --> Storage
```

---

## Modulos Koin: Grafo de Dependencias

```mermaid
graph LR
    subgraph KoinInitializer
        allModules["allModules = networkModule\n+ storageModule + authModule\n+ dynamicUiModule + configModule\n+ screenContractsModule"]
    end

    subgraph networkModule
        NM1["single: EduGoHttpClient (default)"]
        NM2["single named('plainHttp'): EduGoHttpClient"]
        NM3["single: AuthInterceptor"]
        NM4["single: NetworkObserver\ncreateNetworkObserver()"]
    end

    subgraph storageModule
        SM1["single: Settings (platform)"]
        SM2["single: EduGoStorage"]
        SM3["single: SafeEduGoStorage"]
    end

    subgraph authModule
        AM1["single: AuthRepositoryImpl"]
        AM2["single: AuthServiceImpl"]
        AM3["single: TokenRefreshManagerImpl"]
        AM4["single: MenuRepositoryImpl"]
    end

    subgraph dynamicUiModule
        DM1["single: RemoteScreenLoader"]
        DM2["single: CachedScreenLoader"]
        DM3["single: RemoteDataLoader"]
        DM4["single: CachedDataLoader"]
        DM5["single: ScreenContractRegistry"]
        DM6["single: EventOrchestrator"]
        DM7["factory: DynamicScreenViewModel"]
        DM8["single: CacheConfig"]
        DM9["single: RecentScreenTracker"]
        DM10["single: MutationQueue"]
        DM11["single: ConflictResolver"]
        DM12["single: SyncEngine"]
        DM13["single: ConnectivitySyncManager"]
        DM14["single: LocalSyncStore"]
        DM15["single: SyncRepository"]
        DM16["single: DataSyncService"]
    end

    subgraph screenContractsModule
        SCM1["single: SchoolsListContract"]
        SCM2["single: SchoolsFormContract"]
        SCM3["single: SubjectsListContract"]
        SCM4["single: DashboardContract(s)"]
        SCM5["Registra en ScreenContractRegistry"]
    end

    %% Storage -> Auth
    SM3 --> AM2

    %% Storage -> DynamicUI
    SM3 --> DM2
    SM3 --> DM4
    SM3 --> DM10
    SM3 --> DM14

    %% Network -> Auth
    NM1 --> AM1
    NM2 --> AM1

    %% Network -> DynamicUI
    NM1 --> DM3
    NM1 --> DM15
    NM4 --> DM2
    NM4 --> DM4
    NM4 --> DM7
    NM4 --> DM13

    %% Auth -> Network
    AM2 --> NM3
    NM3 --> NM1

    %% Auth -> Orchestrator
    AM2 --> DM6

    %% Contracts
    SCM5 --> DM5

    %% Offline engine dependencies
    DM10 --> DM12
    DM11 --> DM12
    DM3 --> DM12
    DM12 --> DM13
    DM9 --> DM13
    DM2 --> DM13

    %% Sync bundle dependencies
    DM14 --> DM16
    DM15 --> DM16
    DM2 --> DM16

    %% ViewModel dependencies
    DM2 --> DM7
    DM4 --> DM7
    DM6 --> DM7
    DM5 --> DM7
    DM9 --> DM7
    DM10 --> DM7
```

---

## Flujo Completo de Datos: Desde Tap hasta Renderizado

```mermaid
sequenceDiagram
    participant User as Usuario
    participant Composable as DynamicScreen
    participant VM as DynamicScreenViewModel
    participant EO as EventOrchestrator
    participant CSL as CachedScreenLoader
    participant CDL as CachedDataLoader
    participant DSS as DataSyncService
    participant Auth as AuthService
    participant Net as EduGoHttpClient
    participant NetObs as NetworkObserver
    participant MQ as MutationQueue
    participant Backend as Backend APIs

    Note over User,Backend: Fase 0: Sync Bundle (post-login o splash)
    User->>DSS: Login exitoso → fullSync()
    DSS->>Net: GET /api/v1/sync/bundle
    Net->>Backend: GET /api/v1/sync/bundle
    Backend-->>Net: SyncBundleResponse (menu + screens + permissions + contexts)
    Net-->>DSS: SyncBundleResponse
    DSS->>DSS: mapBundleResponse → UserDataBundle
    DSS->>DSS: store.saveBundle (persistencia local)
    DSS->>CSL: seedFromBundle(screens) → pre-carga cache
    DSS->>DSS: _currentBundle.value = bundle
    DSS-->>User: MainScreen observa currentBundle → renderiza sidebar

    Note over User,Backend: Fase 1: Navegacion al item del menu
    User->>Composable: toca "Escuelas" en sidebar
    Composable->>VM: LaunchedEffect(screenKey="schools-list")

    Note over User,Backend: Fase 2: Carga de la definicion de pantalla
    VM->>CSL: loadScreen("schools-list")
    alt Cache hit (seeded desde bundle)
        CSL-->>VM: ScreenDefinition (desde cache)
    else Cache miss
        CSL->>Backend: GET /screen-config/resolve/key/schools-list
        Backend-->>CSL: ScreenDefinition JSON
        CSL-->>VM: ScreenDefinition
    end

    Note over User,Backend: Fase 3: Carga de datos de la lista
    VM->>EO: executeEvent("schools-list", LOAD_DATA, context)
    EO->>Auth: userContextProvider() → UserContext
    Auth-->>EO: UserContext { permissions: ["schools:read",...] }
    EO->>EO: hasPermission("schools:read") → true
    EO->>CDL: loadData("admin:/api/v1/schools", DataConfig)

    alt Online
        CDL->>Net: GET admin-api/api/v1/schools?page=1
        Note over Net: AuthInterceptor inyecta Bearer token
        Net->>Backend: GET /api/v1/schools
        Backend-->>Net: { items: [...], total: 45 }
        Net-->>CDL: DataPage
    else Offline (cache fallback)
        CDL->>CDL: Lee datos del cache local (SafeEduGoStorage)
        CDL-->>CDL: DataPage (cache)
    end

    CDL-->>EO: DataPage
    EO-->>VM: EventResult.Success(DataPage)

    Note over User,Backend: Fase 4: Renderizado reactivo
    VM->>VM: _dataState.value = DataState.Success(items)
    VM->>Composable: StateFlow emite nuevo estado
    Composable->>Composable: PatternRouter(LIST) → ListPatternRenderer
    Composable->>User: Lista de escuelas renderizada

    Note over User,Backend: Fase 5: Offline mutation (si aplica)
    User->>VM: Edita un recurso (form submit)
    VM->>NetObs: isOnline?
    alt Offline
        VM->>MQ: enqueue(mutation)
        MQ->>MQ: Persiste en SafeEduGoStorage
        VM-->>User: Guardado localmente (pendiente sync)
    else Online
        VM->>EO: submitForm → HTTP POST/PUT
        EO-->>VM: EventResult.Success
    end
```

---

## Ciclo de Vida de la App por Plataforma

```mermaid
flowchart TD
    subgraph Android
        A1[MainActivity.onCreate] --> A2[setContent { App() }]
        A2 --> A3[KoinApplication inicializa]
        A3 --> A4[App corriendo]
        A4 --> |"onStop"| A5[Compose se pausa\nCoroutines suspenden]
        A5 --> |"onStart"| A4
        A4 --> |"onDestroy"| A6[Cleanup]
    end

    subgraph iOS
        I1[MainViewController.viewDidLoad] --> I2[ComposeUIViewController { App() }]
        I2 --> I3[KoinApplication inicializa]
        I3 --> I4[App corriendo]
        I4 --> |"viewWillDisappear"| I5[Compose se pausa]
        I5 --> |"viewWillAppear"| I4
    end

    subgraph Desktop
        D1[main fun] --> D2[Window aplicacion { App() }]
        D2 --> D3[KoinApplication inicializa]
        D3 --> D4[App corriendo]
        D4 --> |"ventana cerrada"| D5[exitApplication\nCleanup]
    end

    subgraph WasmJS
        W1[main fun] --> W2[CanvasBasedWindow { App() }]
        W2 --> W3[KoinApplication inicializa]
        W3 --> W4[App corriendo en browser]
        W4 --> |"tab cerrado"| W5[JS context destruido]
    end
```

---

## Patron de Estado Reactivo

```mermaid
graph LR
    subgraph Sources ["Fuentes de Estado"]
        AS[AuthService._authState\nMutableStateFlow~AuthState~]
        VS[ViewModel._screenState\nMutableStateFlow~ScreenState~]
        VD[ViewModel._dataState\nMutableStateFlow~DataState~]
        VF[ViewModel._fieldValues\nMutableStateFlow~Map~]
        DSB[DataSyncService._currentBundle\nMutableStateFlow~UserDataBundle?~]
        DSS[DataSyncService._syncState\nMutableStateFlow~SyncState~]
        NO[NetworkObserver.status\nStateFlow~NetworkStatus~]
        MQ[MutationQueue.pendingCount\nStateFlow~Int~]
        VIO[ViewModel.isOnline\nStateFlow~Boolean~]
        VPC[ViewModel.pendingMutationCount\nStateFlow~Int~]
    end

    subgraph Composables ["Composables que observan"]
        APP[App.kt\nobserva authState → navegar a Login\nobserva onTokenRefreshed → deltaSync]
        MAIN[MainScreen\nobserva currentBundle → reconstruir sidebar\nobserva authState → context picker]
        DS[DynamicScreen\nobserva screenState + dataState\nobserva isOnline + pendingMutationCount]
        FORM[FormRenderer\nobserva fieldValues]
        CB[ConnectivityBanner\nobserva isOnline + pendingCount + syncState]
        SCHOOL[SchoolSelectionScreen\nobserva syncState (isSyncing)]
    end

    AS --> |"collectAsState()"| APP
    AS --> |"collectAsState()"| MAIN
    VS --> |"collectAsState()"| DS
    VD --> |"collectAsState()"| DS
    VF --> |"collectAsState()"| FORM
    DSB --> |"collectAsState()"| MAIN
    DSS --> |"collectAsState()"| SCHOOL
    VIO --> |"collectAsState()"| DS
    VIO --> |"collectAsState()"| CB
    VPC --> |"collectAsState()"| CB
    NO --> VIO
    MQ --> VPC
```

---

## Arquitectura de Modulos: Dependencias

```mermaid
graph TD
    platforms --> kmp-screens
    kmp-screens --> modules/dynamic-ui
    kmp-screens --> modules/auth
    kmp-screens --> modules/settings
    kmp-screens --> modules/di
    modules/dynamic-ui --> modules/auth
    modules/dynamic-ui --> modules/network
    modules/dynamic-ui --> modules/storage
    modules/auth --> modules/network
    modules/auth --> modules/storage
    modules/auth --> modules/foundation
    modules/network --> modules/foundation
    modules/storage --> modules/foundation
    modules/di --> modules/dynamic-ui
    modules/di --> modules/auth
    modules/di --> modules/network
    modules/di --> modules/storage
    Note1["IMPORTANTE: modules/di NO depende de kmp-screens\n(evitar dependencia circular).\nscreenContractsModule vive en kmp-screens\ny se registra manualmente en App.kt"]
```

---

## Diferencias de Entry Point por Plataforma

| Aspecto | Android | iOS | Desktop | WasmJS |
|---------|---------|-----|---------|--------|
| Entry point | `MainActivity` | `MainViewController` | `main()` fun | `main()` fun |
| Window management | Android Activity | UIViewController | `Window {}` composable | Canvas |
| DI inicializacion | En `setContent {}` de Activity | En `viewDidLoad` | En `main()` antes de Window | En `main()` antes de Canvas |
| Lifecycle hooks | Activity lifecycle | ViewController lifecycle | Window close | Browser unload |
| Splash screen | Activity tema / SplashScreen API | LaunchScreen.storyboard | Pantalla en blanco (no implementado) | HTML loading state |
| Orientacion | `setRequestedOrientation` posible | UIInterfaceOrientationMask | No aplica | No aplica |

---

## Flujo de Inicio: Splash → Dashboard

```mermaid
flowchart TD
    START[App inicia] --> SPLASH[SplashScreen]
    SPLASH --> RESTORE[authService.restoreSession]
    RESTORE --> AUTH_CHECK{isAuthenticated?}

    AUTH_CHECK --> |No| LOGIN[LoginScreen]
    AUTH_CHECK --> |Si| PARALLEL["restoreFromLocal() +\ndeltaSync() en paralelo\ncon splash delay"]
    PARALLEL --> DASHBOARD[MainScreen / Dashboard]

    LOGIN --> LOGIN_OK{Login exitoso}
    LOGIN_OK --> SCHOOLS_CHECK{schools.size > 1?}
    SCHOOLS_CHECK --> |Si| SCHOOL_SEL[SchoolSelectionScreen]
    SCHOOLS_CHECK --> |No| AUTO_SYNC["fullSync() automatico"]
    AUTO_SYNC --> DASHBOARD

    SCHOOL_SEL --> SWITCH["switchContext(schoolId)\n+ fullSync()"]
    SWITCH --> DASHBOARD

    DASHBOARD --> BUNDLE_OBS["MainScreen observa\ncurrentBundle → sidebar"]
```

---

## Mejoras Propuestas

| Mejora | Justificacion | Prioridad |
|--------|--------------|-----------|
| Separar ScreenContracts en modulo propio | Actualmente en kmp-screens, deberia ser `modules/screen-contracts` independiente | Media |
| ViewModel Factory tipado | Actualmente `factory: DynamicScreenViewModel` en Koin sin parametros tipados | Media |
| Instrumentacion de telemetria | No hay observabilidad de errores en produccion (Crashlytics/Sentry) | Alta |
| Feature flags | No hay mecanismo para habilitar/deshabilitar features por ambiente | Media |
| Modularizacion por feature | Actualmente todo en modulos transversales; considerar `modules/schools`, `modules/subjects` | Baja |
| HMR en Desktop (Hot Module Replacement) | Compose for Desktop soporta reload parcial pero no esta configurado | Baja |
