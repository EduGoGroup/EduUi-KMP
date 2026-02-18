# 4. Modelos KMP (Frontend)

Ubicacion: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/model/`

Todos los modelos usan `@Serializable` de kotlinx.serialization y mapean directamente la respuesta JSON del endpoint `resolve`.

---

## ScreenDefinition

Modelo raiz que representa una pantalla completa. Corresponde al `CombinedScreenDTO` del backend.

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
    val slotData: JsonObject? = null,         // Valores para resolver bindings
    val dataEndpoint: String? = null,         // Endpoint para cargar datos
    val dataConfig: DataConfig? = null,       // Config de paginacion/params
    val actions: List<ActionDefinition> = emptyList(),
    val userPreferences: JsonObject? = null,
    val updatedAt: String
)
```

## ScreenPattern

Determina cual `PatternRenderer` usar.

```kotlin
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
```

## ScreenTemplate

La estructura visual: zonas, navegacion, overrides por plataforma.

```kotlin
@Serializable
data class ScreenTemplate(
    val navigation: NavigationConfig? = null,
    val zones: List<Zone>,
    val platformOverrides: PlatformOverrides? = null
)
```

## Zone

Contenedor visual que agrupa slots.

```kotlin
// model/Zone.kt
@Serializable
data class Zone(
    val id: String,
    val type: ZoneType,
    val distribution: Distribution = Distribution.STACKED,
    val condition: String? = null,    // Renderizado condicional: "data.isEmpty", "!data.isEmpty"
    val slots: List<Slot> = emptyList(),
    val zones: List<Zone> = emptyList(),   // Zonas anidadas
    val itemLayout: ItemLayout? = null     // Layout para items de lista
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
    @SerialName("stacked") STACKED,        // Column vertical
    @SerialName("side-by-side") SIDE_BY_SIDE, // Row horizontal
    @SerialName("grid") GRID,              // Grilla 2 columnas
    @SerialName("flow-row") FLOW_ROW       // FlowRow (chips)
}

@Serializable
data class ItemLayout(
    val slots: List<Slot> = emptyList()
)
```

## Slot

Unidad minima de UI. Cada slot es un control visual.

```kotlin
// model/Slot.kt
@Serializable
data class Slot(
    val id: String,
    val controlType: ControlType,
    val bind: String? = null,         // "slot:key_name" → resuelve desde slotData
    val style: String? = null,        // Estilo de texto
    val value: String? = null,        // Valor fijo o resuelto
    val field: String? = null,        // Binding a datos dinamicos (JSON path)
    val placeholder: String? = null,
    val label: String? = null,        // Label fijo o resuelto
    val icon: String? = null,
    val required: Boolean = false,
    val readOnly: Boolean = false,
    val width: String? = null,
    val weight: Float? = null         // Peso en distribucion side-by-side
)
```

### ControlType y `usesLabel`

La propiedad `usesLabel` determina si el binding resuelve al campo `label` o `value`:

```kotlin
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
    @SerialName("rating") RATING;

    val usesLabel: Boolean
        get() = when (this) {
            SWITCH, CHECKBOX, LIST_ITEM, LIST_ITEM_NAVIGATION,
            METRIC_CARD, TEXT_INPUT, EMAIL_INPUT, PASSWORD_INPUT,
            NUMBER_INPUT, SEARCH_BAR -> true
            else -> false
        }
}
```

**Regla:** Si `usesLabel == true`, el SlotBindingResolver pone el valor resuelto en `label`. Si `usesLabel == false`, lo pone en `value`.

## ActionDefinition

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

## DataConfig

```kotlin
// model/DataConfig.kt
@Serializable
data class DataConfig(
    val defaultParams: Map<String, String> = emptyMap(),
    val pagination: PaginationConfig? = null,
    val refreshInterval: Long? = null
)

@Serializable
data class PaginationConfig(
    val pageSize: Int = 20,
    val limitParam: String = "limit",
    val offsetParam: String = "offset"
)
```

## NavigationConfig

```kotlin
// model/NavigationConfig.kt
@Serializable
data class NavigationConfig(
    val type: NavigationType = NavigationType.TOP_BAR,
    val title: String? = null,
    val showBack: Boolean = false,
    val actions: List<NavigationAction> = emptyList()
)

@Serializable
enum class NavigationType {
    @SerialName("top-bar") TOP_BAR,
    @SerialName("bottom-nav") BOTTOM_NAV,
    @SerialName("drawer") DRAWER,
    @SerialName("tabs") TABS
}
```
