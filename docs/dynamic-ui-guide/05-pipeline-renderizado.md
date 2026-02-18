# 5. Pipeline de Renderizado

Desde la llamada HTTP hasta el Composable en pantalla. Cada paso transforma los datos.

## Pipeline Completo

```
Screen Composable (ej: DynamicDashboardScreen)
    │
    │ screenKey = "dashboard-teacher"
    │ placeholders = {user.firstName: "Juan", today_date: "February 15, 2026"}
    │
    v
DynamicScreen (Composable wrapper)
    │
    │ LaunchedEffect → viewModel.loadScreen(screenKey, placeholders)
    │
    v
DynamicScreenViewModel.loadScreen()
    │
    ├─ 1. ScreenLoader.loadScreen(screenKey)
    │     └─ CachedScreenLoader
    │          ├─ L1: memoria (mutableMapOf)
    │          ├─ L2: disco (SafeEduGoStorage)
    │          └─ L3: RemoteScreenLoader → HTTP GET /v1/screen-config/resolve/key/{key}
    │                 → Deserializa JSON a ScreenDefinition
    │
    ├─ 2. SlotBindingResolver.resolve(screen)
    │     └─ bind:"slot:xxx" → busca en slotData → popula label/value
    │
    ├─ 3. PlaceholderResolver.resolve(screen, placeholders)
    │     └─ {user.firstName} en label/value → reemplaza con valor real
    │
    ├─ 4. _screenState = ScreenState.Ready(resolved)
    │
    └─ 5. Si hay dataEndpoint → loadData(endpoint, dataConfig)
           └─ RemoteDataLoader → HTTP GET al endpoint
              → DataState.Success(items, hasMore)
    │
    v
PatternRouter (Composable)
    │
    │ when (screen.pattern)
    ├─ LOGIN → LoginPatternRenderer
    ├─ DASHBOARD → DashboardPatternRenderer
    ├─ LIST → ListPatternRenderer
    ├─ DETAIL → DetailPatternRenderer
    ├─ SETTINGS → SettingsPatternRenderer
    ├─ FORM → FormPatternRenderer
    └─ else → UnsupportedPatternFallback
    │
    v
PatternRenderer (ej: SettingsPatternRenderer)
    │
    │ Itera zones del template
    │
    v
ZoneRenderer
    │
    │ 1. Evalua condition ("data.isEmpty", "!data.isEmpty")
    │ 2. Segun zone.type:
    │    - SIMPLE_LIST + itemLayout → renderiza items con data
    │    - GROUPED_LIST + itemLayout → renderiza items agrupados
    │    - otros → aplica distribution (STACKED/SIDE_BY_SIDE/GRID/FLOW_ROW)
    │ 3. Renderiza slots con SlotRenderer
    │ 4. Renderiza zonas hijas recursivamente
    │
    v
SlotRenderer
    │
    │ 1. resolveSlotValue(slot, fieldValues, itemData)
    │    - Si hay field binding + itemData → resuelve desde JSON
    │    - Si hay field binding + fieldValues → resuelve desde form state
    │    - Fallback: slot.value ?? ""
    │
    │ 2. when (slot.controlType)
    │    LABEL → Text(displayValue, style=mapTextStyle(slot.style))
    │    TEXT_INPUT → DSOutlinedTextField(label=slot.label)
    │    FILLED_BUTTON → DSFilledButton(text=displayValue, onClick=action)
    │    SWITCH → DSSwitch(label=slot.label, checked=fieldValues[slot.id])
    │    LIST_ITEM_NAVIGATION → DSListItem(headline=slot.label, trailing=arrow)
    │    METRIC_CARD → DSElevatedCard(label=slot.label, value=displayValue)
    │    ...etc
    │
    v
   Composable renderizado en pantalla
```

---

## Paso 1: ScreenLoader

```kotlin
// loader/ScreenLoader.kt
interface ScreenLoader {
    suspend fun loadScreen(screenKey: String, platform: String? = null): Result<ScreenDefinition>
    suspend fun loadNavigation(): Result<NavigationDefinition>
}
```

**CachedScreenLoader** implementa un cache de 2 niveles:
- L1: `mutableMapOf` en memoria (rapido, se pierde al cerrar app)
- L2: `SafeEduGoStorage` en disco (persiste entre sesiones)
- TTL: 1 hora por defecto
- Invalidacion: `clearCache()` borra todo, `evict(key)` borra una pantalla

**RemoteScreenLoader** hace el HTTP GET:
```kotlin
httpClient.getSafe<ScreenDefinition>(
    url = "$baseUrl/v1/screen-config/resolve/key/$screenKey"
)
```

---

## Paso 2: SlotBindingResolver

Resuelve `bind: "slot:key_name"` → busca el valor en `slotData`.

```kotlin
// resolver/SlotBindingResolver.kt
object SlotBindingResolver {
    fun resolve(screen: ScreenDefinition): ScreenDefinition
}
```

**Logica de resolucion por slot:**

```
Si el slot no tiene bind → no hace nada
Si bind no empieza con "slot:" → no hace nada
Extrae el key: bind = "slot:dark_mode_label" → key = "dark_mode_label"
Busca en slotData["dark_mode_label"] → "Dark Mode"

Si el slot ya tiene label explicito → no sobreescribe
Si controlType.usesLabel == true → slot.copy(label = "Dark Mode")
Si el slot ya tiene value explicito → no sobreescribe
Si controlType.usesLabel == false → slot.copy(value = "Dark Mode")
```

**Ejemplo antes/despues:**

```
ANTES: Slot(id="dark_mode", controlType=SWITCH, bind="slot:dark_mode_label")
       slotData = {"dark_mode_label": "Dark Mode"}

DESPUES: Slot(id="dark_mode", controlType=SWITCH, bind="slot:dark_mode_label", label="Dark Mode")
```

---

## Paso 3: PlaceholderResolver

Reemplaza `{key}` en `label` y `value` de los slots con valores reales.

```kotlin
// resolver/PlaceholderResolver.kt
object PlaceholderResolver {
    fun resolve(screen: ScreenDefinition, placeholders: Map<String, String>): ScreenDefinition
}
```

Los placeholders se construyen desde el contexto de autenticacion:

```kotlin
// screens/DynamicDashboardScreen.kt
private fun buildPlaceholders(user: AuthUserInfo?, context: UserContext?): Map<String, String> {
    return buildMap {
        put("today_date", dateFormatted)
        user?.let {
            put("user.firstName", it.firstName)
            put("user.lastName", it.lastName)
            put("user.fullName", it.fullName)
            put("user.email", it.email)
            put("user.initials", it.getInitials())
        }
        context?.let {
            put("context.roleName", it.roleName)
            put("context.schoolName", it.schoolName ?: "")
        }
    }
}
```

**Ejemplo antes/despues:**

```
ANTES: Slot(value="Good morning, {user.firstName}")
       placeholders = {"user.firstName": "Juan"}

DESPUES: Slot(value="Good morning, Juan")
```

---

## Paso 4: PatternRouter

Selecciona el renderer Composable segun `screen.pattern`:

```kotlin
// renderer/PatternRouter.kt
@Composable
fun PatternRouter(screen, dataState, fieldValues, fieldErrors, onFieldChanged, onAction, onNavigate) {
    when (screen.pattern) {
        ScreenPattern.LOGIN -> LoginPatternRenderer(...)
        ScreenPattern.DASHBOARD -> DashboardPatternRenderer(...)
        ScreenPattern.LIST -> ListPatternRenderer(...)
        ScreenPattern.DETAIL -> DetailPatternRenderer(...)
        ScreenPattern.SETTINGS -> SettingsPatternRenderer(...)
        ScreenPattern.FORM -> FormPatternRenderer(...)
        else -> UnsupportedPatternFallback(...)
    }
}
```

Cada renderer recibe los mismos parametros y los pasa a `ZoneRenderer`.

---

## Paso 5: ZoneRenderer

Renderiza una zona segun su tipo y distribucion:

```kotlin
// renderer/ZoneRenderer.kt
@Composable
fun ZoneRenderer(zone, actions, data, fieldValues, fieldErrors, onFieldChanged, onAction)
```

1. **Evalua condicion**: si `zone.condition` existe, evalua contra `data`
2. **Segun tipo de zona**:
   - `SIMPLE_LIST` con `itemLayout` → itera `data` y renderiza slots del layout por cada item
   - `GROUPED_LIST` → similar pero con agrupacion
   - Otros → aplica distribucion:
     - `STACKED` → `Column`
     - `SIDE_BY_SIDE` → `Row` (usa `slot.weight`)
     - `GRID` → Chunked en filas de 2
     - `FLOW_ROW` → `FlowRow`
3. **Renderiza zonas hijas** recursivamente

---

## Paso 6: SlotRenderer

Renderiza un slot individual como Composable:

```kotlin
// renderer/SlotRenderer.kt
@Composable
fun SlotRenderer(slot, actions, fieldValues, fieldErrors, onFieldChanged, onAction, itemData?)
```

### Mapeo ControlType → Composable

| ControlType | Composable | Datos que usa |
|-------------|-----------|---------------|
| `LABEL` | `Text` | `displayValue`, `slot.style` |
| `TEXT_INPUT` | `DSOutlinedTextField` | `fieldValues[slot.id]`, `slot.label`, `slot.placeholder` |
| `EMAIL_INPUT` | `DSOutlinedTextField` (keyboard=Email) | idem |
| `PASSWORD_INPUT` | `DSPasswordField` | idem |
| `NUMBER_INPUT` | `DSOutlinedTextField` (keyboard=Number) | idem |
| `SEARCH_BAR` | `DSOutlinedTextField` | `fieldValues[slot.id]`, `slot.placeholder` |
| `FILLED_BUTTON` | `DSFilledButton` | `displayValue`, action por `triggerSlotId` |
| `OUTLINED_BUTTON` | `DSOutlinedButton` | idem |
| `TEXT_BUTTON` | `DSTextButton` | idem |
| `ICON_BUTTON` | `DSIconButton` | `slot.label`, action |
| `ICON` | `Icon` | `displayValue` como contentDescription |
| `AVATAR` | `DSAvatar` | `displayValue.take(2)` como iniciales |
| `SWITCH` | `DSSwitch` | `fieldValues[slot.id]`, `slot.label` |
| `CHECKBOX` | `DSCheckbox` | `fieldValues[slot.id]`, `slot.label` |
| `LIST_ITEM` | `DSListItem` | `displayValue` como headline, `slot.label` como supporting |
| `LIST_ITEM_NAVIGATION` | `DSListItem` + arrow | `slot.label` como headline, `displayValue` como supporting |
| `METRIC_CARD` | `DSElevatedCard` | `slot.label` como titulo, `displayValue` como valor |
| `CHIP` | `DSChip` | `displayValue`, `fieldValues[slot.id]` como selected |
| `DIVIDER` | `DSDivider` | ninguno |

### Resolucion de valor (`resolveSlotValue`)

```kotlin
private fun resolveSlotValue(slot, fieldValues, itemData): String {
    // 1. Si hay field binding + itemData → resuelve del JSON del item
    if (slot.field != null && itemData != null) {
        resolveFieldFromJson(slot.field, itemData)?.let { return it }
    }

    // 2. Si hay field binding + fieldValues → resuelve del estado del form
    if (slot.field != null && fieldValues.containsKey(slot.field)) {
        return fieldValues[slot.field] ?: ""
    }

    // 3. Valor estatico (ya resuelto por SlotBindingResolver/PlaceholderResolver)
    return slot.value ?: ""
}
```

---

## Paso 7: Actions

Cuando un usuario interactua con un boton o item:

```
SlotRenderer detecta click
    → findActionForSlot(slot.id, actions)  // Busca action con triggerSlotId == slot.id
    → onAction(action, itemData)
    → DynamicScreen scope.launch { viewModel.executeAction(action, item) }
    → ActionRegistry.resolve(action.type) → ActionHandler
    → handler.execute(ActionContext)
    → ActionResult
        Success → mensaje
        NavigateTo → onNavigate(screenKey, params)
        Error → muestra error
```

### ActionHandlers

| Handler | ActionType | Que hace |
|---------|-----------|----------|
| `NavigateHandler` | NAVIGATE, NAVIGATE_BACK, LOGOUT | Extrae `screenKey` del config, retorna `NavigateTo` |
| `SubmitFormHandler` | SUBMIT_FORM | POST a `endpoint` con `fieldValues` como body |
| `ApiCallHandler` | API_CALL | GET a `endpoint` con params |
| `RefreshHandler` | REFRESH | Retorna `Success("refresh")` |
| `ConfirmHandler` | CONFIRM | Retorna `Success(message)` |

---

## DI (Koin)

```kotlin
// di/module/DynamicUiModule.kt
val dynamicUiModule = module {
    single<ScreenLoader> {
        CachedScreenLoader(
            remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl),
            storage = get<SafeEduGoStorage>()
        )
    }
    single<DataLoader> {
        RemoteDataLoader(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl)
    }
    single { NavigateHandler() }
    single { ApiCallHandler(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl) }
    single { RefreshHandler() }
    single { SubmitFormHandler(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl) }
    single { ConfirmHandler() }
    single { ActionRegistry(get(), get(), get(), get(), get()) }
    factory { DynamicScreenViewModel(get(), get(), get()) }
}
```
