# Fase 1: Cambios en el Frontend KMP

## Descripcion General

Crear un nuevo modulo `dynamic-ui` en el proyecto KMP que se encargue de cargar las configuraciones de pantalla desde el backend y renderizarlas usando pattern renderers pre-construidos.

## Proyecto: `kmp_new` (EduGo KMP Frontend)

### 1. Nuevo Modulo: `modules/dynamic-ui`

#### Estructura del Modulo
```
modules/dynamic-ui/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/edugo/kmp/dynamicui/
    │   ├── model/
    │   │   ├── ScreenDefinition.kt       # Modelo principal de definicion de pantalla
    │   │   ├── Zone.kt                    # Modelo de contenedor zone
    │   │   ├── Slot.kt                    # Modelo de slot/control
    │   │   ├── ActionDefinition.kt        # Modelo de definicion de accion
    │   │   ├── DataConfig.kt              # Configuracion de endpoint de datos
    │   │   ├── NavigationConfig.kt        # Modelo de estructura de navegacion
    │   │   └── PlatformOverride.kt        # Overrides especificos por plataforma
    │   ├── loader/
    │   │   ├── ScreenLoader.kt            # Interfaz para cargar pantallas
    │   │   ├── RemoteScreenLoader.kt      # Implementacion con cliente HTTP
    │   │   └── CachedScreenLoader.kt      # Decorador envolvente de cache
    │   ├── action/
    │   │   ├── ActionHandler.kt           # Interfaz de handler de acciones
    │   │   ├── ActionContext.kt           # Contexto de ejecucion de acciones
    │   │   ├── ActionResult.kt            # Clase sellada de resultado de accion
    │   │   ├── ActionRegistry.kt          # Mapea tipos de accion a handlers
    │   │   └── handlers/
    │   │       ├── NavigateHandler.kt
    │   │       ├── ApiCallHandler.kt
    │   │       ├── RefreshHandler.kt
    │   │       ├── SubmitFormHandler.kt
    │   │       └── ConfirmHandler.kt
    │   ├── data/
    │   │   ├── DataLoader.kt              # Interfaz para cargar datos de pantalla
    │   │   ├── RemoteDataLoader.kt        # Carga datos desde el dataEndpoint
    │   │   └── DataState.kt               # Estados Loading/Success/Error
    │   └── viewmodel/
    │       └── DynamicScreenViewModel.kt  # ViewModel compartido para pantallas dinamicas
    ├── commonTest/kotlin/com/edugo/kmp/dynamicui/
    │   ├── model/
    │   │   └── ScreenDefinitionTest.kt
    │   ├── loader/
    │   │   └── CachedScreenLoaderTest.kt
    │   └── action/
    │       └── ActionRegistryTest.kt
    └── desktopMain/  (especifico de plataforma si es necesario)
```

### 2. Modelos Principales

#### ScreenDefinition

```kotlin
// model/ScreenDefinition.kt

@Serializable
data class ScreenDefinition(
    val screenId: String,
    val screenKey: String,
    val screenName: String,
    val pattern: ScreenPattern,
    val version: Int,
    val template: ScreenTemplate,
    val dataEndpoint: String? = null,
    val dataConfig: DataConfig? = null,
    val actions: List<ActionDefinition> = emptyList(),
    val userPreferences: JsonObject? = null,
    val updatedAt: String
)

@Serializable
enum class ScreenPattern {
    @SerialName("login") LOGIN,
    @SerialName("form") FORM,
    @SerialName("list") LIST,
    @SerialName("dashboard") DASHBOARD,
    @SerialName("settings") SETTINGS,
    @SerialName("detail") DETAIL,
    @SerialName("search") SEARCH,
    @SerialName("profile") PROFILE,
    @SerialName("modal") MODAL,
    @SerialName("notification") NOTIFICATION,
    @SerialName("onboarding") ONBOARDING,
    @SerialName("empty-state") EMPTY_STATE
}

@Serializable
data class ScreenTemplate(
    val navigation: NavigationConfig? = null,
    val zones: List<Zone>,
    val platformOverrides: PlatformOverrides? = null
)
```

#### Zone y Slot

```kotlin
// model/Zone.kt

@Serializable
data class Zone(
    val id: String,
    val type: ZoneType,
    val distribution: Distribution = Distribution.STACKED,
    val condition: String? = null,
    val slots: List<Slot> = emptyList(),
    val zones: List<Zone> = emptyList(),  // Anidamiento recursivo
    val itemLayout: ItemLayout? = null     // Para zones de tipo lista
)

@Serializable
enum class ZoneType {
    @SerialName("container") CONTAINER,
    @SerialName("form-section") FORM_SECTION,
    @SerialName("simple-list") SIMPLE_LIST,
    @SerialName("grouped-list") GROUPED_LIST,
    @SerialName("metric-grid") METRIC_GRID,
    @SerialName("action-group") ACTION_GROUP,
    @SerialName("card-list") CARD_LIST
}

@Serializable
enum class Distribution {
    @SerialName("stacked") STACKED,
    @SerialName("side-by-side") SIDE_BY_SIDE,
    @SerialName("grid") GRID,
    @SerialName("flow-row") FLOW_ROW
}

// model/Slot.kt

@Serializable
data class Slot(
    val id: String,
    val controlType: ControlType,
    val style: String? = null,
    val value: String? = null,       // Valor estatico
    val field: String? = null,       // Campo dinamico desde los datos
    val placeholder: String? = null,
    val label: String? = null,
    val icon: String? = null,
    val required: Boolean = false,
    val readOnly: Boolean = false,
    val width: String? = null,
    val weight: Float? = null
)

@Serializable
enum class ControlType {
    @SerialName("label") LABEL,
    @SerialName("text-input") TEXT_INPUT,
    @SerialName("email-input") EMAIL_INPUT,
    @SerialName("password-input") PASSWORD_INPUT,
    @SerialName("number-input") NUMBER_INPUT,
    @SerialName("search-bar") SEARCH_BAR,
    @SerialName("checkbox") CHECKBOX,
    @SerialName("switch") SWITCH,
    @SerialName("radio-group") RADIO_GROUP,
    @SerialName("select") SELECT,
    @SerialName("filled-button") FILLED_BUTTON,
    @SerialName("outlined-button") OUTLINED_BUTTON,
    @SerialName("text-button") TEXT_BUTTON,
    @SerialName("icon-button") ICON_BUTTON,
    @SerialName("icon") ICON,
    @SerialName("avatar") AVATAR,
    @SerialName("image") IMAGE,
    @SerialName("divider") DIVIDER,
    @SerialName("list-item") LIST_ITEM,
    @SerialName("list-item-navigation") LIST_ITEM_NAVIGATION,
    @SerialName("metric-card") METRIC_CARD,
    @SerialName("chip") CHIP,
    @SerialName("rating") RATING
}
```

#### ActionDefinition

```kotlin
// model/ActionDefinition.kt

@Serializable
data class ActionDefinition(
    val id: String,
    val trigger: ActionTrigger,
    val triggerSlotId: String? = null,
    val type: ActionType,
    val config: JsonObject = JsonObject(emptyMap())
)

@Serializable
enum class ActionTrigger {
    @SerialName("button_click") BUTTON_CLICK,
    @SerialName("item_click") ITEM_CLICK,
    @SerialName("pull_refresh") PULL_REFRESH,
    @SerialName("fab_click") FAB_CLICK,
    @SerialName("swipe") SWIPE,
    @SerialName("long_press") LONG_PRESS
}

@Serializable
enum class ActionType {
    @SerialName("NAVIGATE") NAVIGATE,
    @SerialName("NAVIGATE_BACK") NAVIGATE_BACK,
    @SerialName("API_CALL") API_CALL,
    @SerialName("SUBMIT_FORM") SUBMIT_FORM,
    @SerialName("REFRESH") REFRESH,
    @SerialName("CONFIRM") CONFIRM,
    @SerialName("LOGOUT") LOGOUT
}
```

### 3. Screen Loader

```kotlin
// loader/ScreenLoader.kt

interface ScreenLoader {
    suspend fun loadScreen(screenKey: String, platform: String? = null): Result<ScreenDefinition>
    suspend fun loadNavigation(): Result<NavigationDefinition>
}

// loader/RemoteScreenLoader.kt

class RemoteScreenLoader(
    private val httpClient: EduGoHttpClient
) : ScreenLoader {

    override suspend fun loadScreen(screenKey: String, platform: String?): Result<ScreenDefinition> {
        val queryParams = buildMap {
            platform?.let { put("platform", it) }
        }
        return httpClient.getSafe<ScreenDefinition>(
            path = "/v1/screens/$screenKey",
            queryParams = queryParams
        )
    }

    override suspend fun loadNavigation(): Result<NavigationDefinition> {
        return httpClient.getSafe<NavigationDefinition>(
            path = "/v1/screens/navigation"
        )
    }
}

// loader/CachedScreenLoader.kt

class CachedScreenLoader(
    private val remote: ScreenLoader,
    private val storage: SafeEduGoStorage,
    private val cacheDuration: Duration = 1.hours
) : ScreenLoader {

    private val memoryCache = mutableMapOf<String, Pair<ScreenDefinition, Instant>>()

    override suspend fun loadScreen(screenKey: String, platform: String?): Result<ScreenDefinition> {
        // 1. Verificar cache en memoria
        memoryCache[screenKey]?.let { (screen, timestamp) ->
            if (Clock.System.now() - timestamp < cacheDuration) {
                return Result.Success(screen)
            }
        }

        // 2. Verificar cache persistente
        val cached = storage.getStringOrNull("screen_cache_$screenKey")
        if (cached != null) {
            val screen = Json.decodeFromString<ScreenDefinition>(cached)
            memoryCache[screenKey] = screen to Clock.System.now()
            // Actualizacion en segundo plano si esta obsoleto
        }

        // 3. Cargar desde el remoto
        return remote.loadScreen(screenKey, platform).also { result ->
            if (result is Result.Success) {
                val screen = result.value
                memoryCache[screenKey] = screen to Clock.System.now()
                storage.putString("screen_cache_$screenKey", Json.encodeToString(screen))
            }
        }
    }
}
```

### 4. Data Loader

```kotlin
// data/DataLoader.kt

interface DataLoader {
    suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String> = emptyMap()
    ): Result<DataPage>
}

@Serializable
data class DataPage(
    val items: List<JsonObject>,
    val total: Int? = null,
    val hasMore: Boolean = false
)

// data/RemoteDataLoader.kt

class RemoteDataLoader(
    private val httpClient: EduGoHttpClient
) : DataLoader {

    override suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String>
    ): Result<DataPage> {
        val queryParams = buildMap {
            putAll(config.defaultParams)
            putAll(params)
            config.pagination?.let {
                put(it.limitParam, it.pageSize.toString())
            }
        }
        return httpClient.getSafe<DataPage>(
            path = endpoint,
            queryParams = queryParams
        )
    }
}
```

### 5. Sistema de Acciones

```kotlin
// action/ActionHandler.kt

interface ActionHandler {
    suspend fun execute(context: ActionContext): ActionResult
}

data class ActionContext(
    val screenKey: String,
    val actionId: String,
    val config: JsonObject,
    val fieldValues: Map<String, String> = emptyMap(),
    val selectedItemId: String? = null,
    val selectedItem: JsonObject? = null
)

sealed class ActionResult {
    data class Success(
        val message: String? = null,
        val data: JsonObject? = null
    ) : ActionResult()

    data class NavigateTo(
        val screenKey: String,
        val params: Map<String, String> = emptyMap()
    ) : ActionResult()

    data class Error(
        val message: String,
        val retry: Boolean = false
    ) : ActionResult()

    data object Cancelled : ActionResult()
}

// action/ActionRegistry.kt

class ActionRegistry(
    private val navigateHandler: NavigateHandler,
    private val apiCallHandler: ApiCallHandler,
    private val refreshHandler: RefreshHandler,
    private val submitFormHandler: SubmitFormHandler,
    private val confirmHandler: ConfirmHandler
) {
    fun resolve(actionType: ActionType): ActionHandler = when (actionType) {
        ActionType.NAVIGATE -> navigateHandler
        ActionType.NAVIGATE_BACK -> navigateHandler
        ActionType.API_CALL -> apiCallHandler
        ActionType.SUBMIT_FORM -> submitFormHandler
        ActionType.REFRESH -> refreshHandler
        ActionType.CONFIRM -> confirmHandler
        ActionType.LOGOUT -> navigateHandler  // Logout se maneja a traves del servicio de autenticacion
    }
}
```

### 6. DynamicScreenViewModel

```kotlin
// viewmodel/DynamicScreenViewModel.kt

class DynamicScreenViewModel(
    private val screenLoader: ScreenLoader,
    private val dataLoader: DataLoader,
    private val actionRegistry: ActionRegistry,
    private val authService: AuthService
) {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _dataState = MutableStateFlow<DataState>(DataState.Idle)
    val dataState: StateFlow<DataState> = _dataState.asStateFlow()

    private val _fieldValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldValues: StateFlow<Map<String, String>> = _fieldValues.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    sealed class ScreenState {
        data object Loading : ScreenState()
        data class Ready(val screen: ScreenDefinition) : ScreenState()
        data class Error(val message: String) : ScreenState()
    }

    sealed class DataState {
        data object Idle : DataState()
        data object Loading : DataState()
        data class Success(val items: List<JsonObject>, val hasMore: Boolean) : DataState()
        data class Error(val message: String) : DataState()
    }

    suspend fun loadScreen(screenKey: String, platform: String? = null) {
        _screenState.value = ScreenState.Loading
        when (val result = screenLoader.loadScreen(screenKey, platform)) {
            is Result.Success -> {
                _screenState.value = ScreenState.Ready(result.value)
                // Cargar datos automaticamente si la pantalla tiene un dataEndpoint
                result.value.dataEndpoint?.let { endpoint ->
                    result.value.dataConfig?.let { config ->
                        loadData(endpoint, config)
                    }
                }
            }
            is Result.Failure -> {
                _screenState.value = ScreenState.Error(result.message)
            }
        }
    }

    suspend fun loadData(endpoint: String, config: DataConfig, extraParams: Map<String, String> = emptyMap()) {
        _dataState.value = DataState.Loading
        when (val result = dataLoader.loadData(endpoint, config, extraParams)) {
            is Result.Success -> {
                _dataState.value = DataState.Success(
                    items = result.value.items,
                    hasMore = result.value.hasMore
                )
            }
            is Result.Failure -> {
                _dataState.value = DataState.Error(result.message)
            }
        }
    }

    suspend fun executeAction(actionDef: ActionDefinition, itemData: JsonObject? = null) {
        val handler = actionRegistry.resolve(actionDef.type)
        val context = ActionContext(
            screenKey = (screenState.value as? ScreenState.Ready)?.screen?.screenKey ?: "",
            actionId = actionDef.id,
            config = actionDef.config,
            fieldValues = _fieldValues.value,
            selectedItem = itemData
        )
        // Ejecutar y manejar el resultado...
    }

    fun onFieldChanged(fieldId: String, value: String) {
        _fieldValues.update { it + (fieldId to value) }
        _fieldErrors.update { it - fieldId }
    }
}
```

### 7. Integracion con Koin DI

```kotlin
// En modules/di - agregar dynamicUiModule

val dynamicUiModule = module {
    // Loader
    single<ScreenLoader> {
        CachedScreenLoader(
            remote = RemoteScreenLoader(get()),
            storage = get()
        )
    }
    single<DataLoader> { RemoteDataLoader(get()) }

    // Handlers de acciones
    single { NavigateHandler() }
    single { ApiCallHandler(get()) }
    single { RefreshHandler() }
    single { SubmitFormHandler(get()) }
    single { ConfirmHandler() }
    single { ActionRegistry(get(), get(), get(), get(), get()) }

    // Fabrica de ViewModel
    factory { DynamicScreenViewModel(get(), get(), get(), get()) }
}
```

### 8. Pattern Renderers (integracion con kmp-screens)

Los pattern renderers residen en `kmp-screens` (no en `dynamic-ui`) porque dependen de los componentes del sistema de diseno.

```kotlin
// kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/dynamic/

// Punto de entrada principal
@Composable
fun DynamicScreen(
    screenKey: String,
    viewModel: DynamicScreenViewModel = koinInject(),
    onNavigate: (String, Map<String, String>) -> Unit
) {
    LaunchedEffect(screenKey) {
        viewModel.loadScreen(screenKey, currentPlatform())
    }

    val screenState by viewModel.screenState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()

    when (val state = screenState) {
        is ScreenState.Loading -> DSLinearProgress()
        is ScreenState.Ready -> PatternRouter(
            screen = state.screen,
            dataState = dataState,
            fieldValues = viewModel.fieldValues.collectAsState().value,
            fieldErrors = viewModel.fieldErrors.collectAsState().value,
            onFieldChanged = viewModel::onFieldChanged,
            onAction = { action, item -> viewModel.executeAction(action, item) },
            onNavigate = onNavigate
        )
        is ScreenState.Error -> DSEmptyState(
            title = "Error loading screen",
            description = state.message,
            actionLabel = "Retry",
            onAction = { viewModel.loadScreen(screenKey) }
        )
    }
}

// Enruta al pattern renderer correcto
@Composable
fun PatternRouter(screen: ScreenDefinition, ...) {
    when (screen.pattern) {
        ScreenPattern.LIST -> ListPatternRenderer(screen, ...)
        ScreenPattern.DETAIL -> DetailPatternRenderer(screen, ...)
        ScreenPattern.DASHBOARD -> DashboardPatternRenderer(screen, ...)
        ScreenPattern.SETTINGS -> SettingsPatternRenderer(screen, ...)
        ScreenPattern.FORM -> FormPatternRenderer(screen, ...)
        ScreenPattern.LOGIN -> LoginPatternRenderer(screen, ...)
        // Patterns de la Fase 2
        else -> UnsupportedPatternFallback(screen.pattern)
    }
}
```

### 9. Renderers de Zone y Slot

```kotlin
// Renderer reutilizable de zone
@Composable
fun ZoneRenderer(
    zone: Zone,
    data: List<JsonObject>,
    fieldValues: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onAction: (ActionDefinition, JsonObject?) -> Unit
) {
    // Verificar condicion
    if (zone.condition != null && !evaluateCondition(zone.condition, data)) return

    when (zone.distribution) {
        Distribution.STACKED -> Column {
            zone.slots.forEach { slot ->
                SlotRenderer(slot, fieldValues, onFieldChanged, onAction)
            }
        }
        Distribution.SIDE_BY_SIDE -> Row {
            zone.slots.forEach { slot ->
                Box(modifier = Modifier.weight(slot.weight ?: 1f)) {
                    SlotRenderer(slot, fieldValues, onFieldChanged, onAction)
                }
            }
        }
        Distribution.GRID -> {
            // LazyVerticalGrid con columnas configurables
        }
        Distribution.FLOW_ROW -> FlowRow {
            zone.slots.forEach { slot ->
                SlotRenderer(slot, fieldValues, onFieldChanged, onAction)
            }
        }
    }
}

// Mapea controlType a componente del Design System
@Composable
fun SlotRenderer(
    slot: Slot,
    fieldValues: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onAction: (ActionDefinition, JsonObject?) -> Unit
) {
    when (slot.controlType) {
        ControlType.LABEL -> DSText(text = slot.value ?: "", style = mapStyle(slot.style))
        ControlType.TEXT_INPUT -> DSOutlinedTextField(
            value = fieldValues[slot.id] ?: "",
            onValueChange = { onFieldChanged(slot.id, it) },
            label = slot.label ?: "",
            placeholder = slot.placeholder ?: ""
        )
        ControlType.FILLED_BUTTON -> DSFilledButton(
            text = slot.value ?: slot.label ?: "",
            onClick = { /* buscar accion por triggerSlotId */ }
        )
        ControlType.ICON -> DSIcon(name = slot.value ?: slot.icon ?: "")
        ControlType.AVATAR -> DSAvatar(/* ... */)
        ControlType.SWITCH -> DSSwitch(
            checked = fieldValues[slot.id]?.toBoolean() ?: false,
            onCheckedChange = { onFieldChanged(slot.id, it.toString()) }
        )
        ControlType.LIST_ITEM -> DSListItem(/* ... */)
        ControlType.METRIC_CARD -> DSElevatedCard(/* ... */)
        // ... mapear todos los tipos de control a componentes del sistema de diseno
    }
}
```

### 10. Configuracion de Build

```kotlin
// modules/dynamic-ui/build.gradle.kts

plugins {
    id("kmp.kotlin.multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:foundation"))
            implementation(project(":modules:network"))
            implementation(project(":modules:storage"))
            implementation(project(":modules:auth"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
```

## Criterios de Aceptacion

- [ ] El modulo `dynamic-ui` compila para todas las plataformas (Android, Desktop, WasmJS)
- [ ] ScreenDefinition serializa/deserializa correctamente desde el JSON de la API
- [ ] CachedScreenLoader almacena pantallas en SafeEduGoStorage
- [ ] DynamicScreenViewModel gestiona los estados de pantalla y datos correctamente
- [ ] ActionRegistry resuelve todos los tipos de accion estandar
- [ ] Los pattern renderers renderizan correctamente para list, detail, dashboard, settings, form
- [ ] Los renderers de Zone y Slot mapean a componentes del sistema de diseno
- [ ] Todos los tests pasan: `./gradlew :modules:dynamic-ui:desktopTest`
