# Action Binding Strategy - Enlace de Eventos y Acciones

## El Problema

Ya tenemos:
- **Template**: como se distribuye la UI
- **Data**: que campos mostrar

Falta:
- **Behavior**: que pasa cuando el usuario interactua (guardar, validar, eliminar, navegar, buscar...)

## Las Tres Capas Completas

```
┌─────────────────────┐
│   TEMPLATE (Layout)  │  → Como se ve
├─────────────────────┤
│   DATA (Campos)      │  → Que muestra
├─────────────────────┤
│   BEHAVIOR (Acciones)│  → Que hace
└─────────────────────┘
```

## Observacion Clave

La mayoria de acciones en una app son **operaciones estandar**:

| Accion | Que hace | Frecuencia |
|--------|----------|------------|
| `save` | Persiste los campos del formulario | Muy alta |
| `delete` | Elimina un registro | Alta |
| `search` | Filtra una lista | Alta |
| `navigate` | Ir a otra pantalla | Muy alta |
| `validate` | Verificar campos antes de enviar | Muy alta |
| `refresh` | Recargar datos | Alta |
| `select` | Elegir un item de lista | Alta |
| `toggle` | Cambiar estado booleano | Alta |
| `confirm` | Mostrar dialogo de confirmacion | Media |
| `logout` | Cerrar sesion | Baja |
| `share` | Compartir contenido | Baja |
| `export` | Exportar datos | Baja |

Esto significa que podemos crear un **catalogo de acciones estandar** y solo implementar custom lo que sea realmente especifico del negocio.

---

## Arquitectura Propuesta

### Nivel 1: ActionType (Enum de acciones estandar)

```kotlin
enum class StandardAction {
    SAVE,           // Persistir campos → API POST/PUT
    DELETE,         // Eliminar registro → API DELETE
    SEARCH,         // Filtrar datos → API GET con query
    NAVIGATE,       // Ir a pantalla → Router
    NAVIGATE_BACK,  // Volver → Router.pop
    VALIDATE,       // Verificar campos → Reglas locales
    REFRESH,        // Recargar → API GET
    SELECT,         // Seleccionar item → Estado local
    TOGGLE,         // Cambiar booleano → Estado local o API
    CONFIRM,        // Mostrar dialogo → Modal
    SUBMIT_FORM,    // Validar + Guardar (combo)
    LOGOUT,         // Cerrar sesion → AuthService
    SHARE,          // Compartir → Share intent
    CUSTOM,         // Logica especifica → Handler custom
}
```

### Nivel 2: ActionDefinition (JSON declarativo)

Se define en el template o en la instancia de pantalla:

```json
{
  "actions": [
    {
      "id": "submit",
      "trigger": "button_click",
      "triggerSlotId": "submit_btn",
      "type": "SUBMIT_FORM",
      "config": {
        "validateFirst": true,
        "endpoint": "/api/students",
        "method": "POST",
        "fieldMapping": "auto",
        "onSuccess": {"action": "NAVIGATE", "target": "success-screen"},
        "onError": {"action": "SHOW_ERROR"}
      }
    },
    {
      "id": "delete",
      "trigger": "button_click",
      "triggerSlotId": "delete_btn",
      "type": "CONFIRM",
      "config": {
        "title": "Eliminar registro?",
        "message": "Esta accion no se puede deshacer",
        "confirmLabel": "Eliminar",
        "onConfirm": {
          "type": "DELETE",
          "endpoint": "/api/students/{id}",
          "onSuccess": {"action": "NAVIGATE_BACK"}
        }
      }
    }
  ]
}
```

### Nivel 3: ActionHandler (Interface Kotlin)

```kotlin
/**
 * Interface base para todos los handlers de accion.
 * Cada accion estandar tiene una implementacion por defecto.
 */
interface ActionHandler {
    suspend fun execute(context: ActionContext): ActionResult
}

/**
 * Contexto que recibe cada handler con todo lo necesario.
 */
data class ActionContext(
    val screenId: String,
    val actionId: String,
    val fieldValues: Map<String, String>,       // valores actuales de todos los campos
    val config: ActionConfig,                    // configuracion del JSON
    val triggeredBy: String,                     // slotId que disparo la accion
    val selectedItemId: String? = null,          // para listas: item seleccionado
    val extraParams: Map<String, String> = emptyMap(),
)

/**
 * Resultado de ejecutar una accion.
 */
sealed class ActionResult {
    data class Success(
        val message: String? = null,
        val data: Map<String, Any>? = null,
        val nextAction: ActionDefinition? = null,  // encadenar accion
    ) : ActionResult()

    data class ValidationError(
        val fieldErrors: Map<String, String>,      // fieldId -> mensaje error
    ) : ActionResult()

    data class Error(
        val message: String,
        val retry: Boolean = false,
    ) : ActionResult()

    data object Cancelled : ActionResult()
}
```

### Nivel 4: ActionRegistry (Registro de handlers)

```kotlin
/**
 * Registro central de handlers.
 * Los handlers estandar se registran automaticamente.
 * Los handlers custom se registran por pantalla.
 */
class ActionRegistry {

    private val standardHandlers = mapOf(
        StandardAction.SAVE to SaveHandler(),
        StandardAction.DELETE to DeleteHandler(),
        StandardAction.SEARCH to SearchHandler(),
        StandardAction.NAVIGATE to NavigateHandler(),
        StandardAction.NAVIGATE_BACK to NavigateBackHandler(),
        StandardAction.VALIDATE to ValidateHandler(),
        StandardAction.REFRESH to RefreshHandler(),
        StandardAction.SUBMIT_FORM to SubmitFormHandler(),  // = validate + save
        StandardAction.LOGOUT to LogoutHandler(),
        StandardAction.CONFIRM to ConfirmHandler(),
    )

    private val customHandlers = mutableMapOf<String, ActionHandler>()

    fun resolve(actionDef: ActionDefinition): ActionHandler {
        if (actionDef.type == StandardAction.CUSTOM) {
            return customHandlers[actionDef.id]
                ?: throw IllegalStateException("No custom handler for: ${actionDef.id}")
        }
        return standardHandlers[actionDef.type]
            ?: throw IllegalStateException("No handler for: ${actionDef.type}")
    }

    /**
     * Registrar handler custom para logica de negocio especifica.
     * Ejemplo: "calcular-nota-final" → CalcularNotaHandler()
     */
    fun registerCustom(actionId: String, handler: ActionHandler) {
        customHandlers[actionId] = handler
    }
}
```

---

## Handlers Estandar en Detalle

### SaveHandler (el mas comun)

```kotlin
class SaveHandler(
    private val apiClient: HttpClient,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val endpoint = context.config.endpoint
            ?: return ActionResult.Error("No endpoint configured")

        val method = context.config.method ?: "POST"

        // fieldMapping = "auto" → enviar todos los campos como JSON body
        val body = if (context.config.fieldMapping == "auto") {
            context.fieldValues
        } else {
            // fieldMapping explicito: mapear solo los campos indicados
            context.config.fieldMappings.associate { mapping ->
                mapping.apiField to (context.fieldValues[mapping.slotId] ?: "")
            }
        }

        return try {
            val response = when (method) {
                "POST" -> apiClient.post(endpoint) { setBody(body) }
                "PUT" -> apiClient.put(endpoint) { setBody(body) }
                "PATCH" -> apiClient.patch(endpoint) { setBody(body) }
                else -> return ActionResult.Error("Unsupported method: $method")
            }

            if (response.status.isSuccess()) {
                ActionResult.Success(message = "Guardado exitosamente")
            } else {
                ActionResult.Error("Error del servidor: ${response.status}")
            }
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Error de conexion", retry = true)
        }
    }
}
```

### ValidateHandler

```kotlin
class ValidateHandler : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val errors = mutableMapOf<String, String>()

        context.config.validationRules.forEach { rule ->
            val value = context.fieldValues[rule.fieldId] ?: ""
            val error = validateField(value, rule)
            if (error != null) {
                errors[rule.fieldId] = error
            }
        }

        return if (errors.isEmpty()) {
            ActionResult.Success()
        } else {
            ActionResult.ValidationError(errors)
        }
    }

    private fun validateField(value: String, rule: ValidationRule): String? {
        return when (rule.type) {
            "required" -> if (value.isBlank()) rule.message ?: "Campo obligatorio" else null
            "email" -> if (!value.contains("@")) rule.message ?: "Email invalido" else null
            "minLength" -> if (value.length < (rule.min ?: 0)) rule.message ?: "Minimo ${rule.min} caracteres" else null
            "maxLength" -> if (value.length > (rule.max ?: Int.MAX_VALUE)) rule.message ?: "Maximo ${rule.max} caracteres" else null
            "pattern" -> if (!Regex(rule.pattern ?: "").matches(value)) rule.message ?: "Formato invalido" else null
            "numeric" -> if (value.isNotBlank() && value.toDoubleOrNull() == null) rule.message ?: "Debe ser numerico" else null
            "range" -> {
                val num = value.toDoubleOrNull() ?: return rule.message ?: "Debe ser numerico"
                if (num < (rule.min ?: 0) || num > (rule.max ?: Int.MAX_VALUE)) {
                    rule.message ?: "Debe estar entre ${rule.min} y ${rule.max}"
                } else null
            }
            else -> null
        }
    }
}
```

### SubmitFormHandler (combo: validar + guardar)

```kotlin
class SubmitFormHandler(
    private val validateHandler: ValidateHandler,
    private val saveHandler: SaveHandler,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        // Paso 1: Validar
        if (context.config.validateFirst != false) {
            val validationResult = validateHandler.execute(context)
            if (validationResult is ActionResult.ValidationError) {
                return validationResult  // devuelve errores al UI
            }
        }

        // Paso 2: Guardar
        return saveHandler.execute(context)
    }
}
```

### NavigateHandler

```kotlin
class NavigateHandler(
    private val router: Router,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val target = context.config.target
            ?: return ActionResult.Error("No navigation target")

        // Sustituir params dinamicos: "/detail/{id}" → "/detail/123"
        val resolvedTarget = target.replace(Regex("\\{(\\w+)\\}")) { match ->
            context.fieldValues[match.groupValues[1]]
                ?: context.extraParams[match.groupValues[1]]
                ?: match.value
        }

        router.navigate(resolvedTarget)
        return ActionResult.Success()
    }
}
```

### SearchHandler

```kotlin
class SearchHandler(
    private val apiClient: HttpClient,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val endpoint = context.config.endpoint
            ?: return ActionResult.Error("No search endpoint")

        val query = context.fieldValues[context.config.queryFieldId ?: "search_input"] ?: ""
        val filters = context.config.filterFieldIds?.associate { fieldId ->
            fieldId to (context.fieldValues[fieldId] ?: "")
        } ?: emptyMap()

        return try {
            val response = apiClient.get(endpoint) {
                parameter("q", query)
                filters.forEach { (key, value) ->
                    if (value.isNotBlank()) parameter(key, value)
                }
            }
            ActionResult.Success(data = mapOf("results" to response.body()))
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Error buscando", retry = true)
        }
    }
}
```

### DeleteHandler

```kotlin
class DeleteHandler(
    private val apiClient: HttpClient,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val endpoint = context.config.endpoint
            ?: return ActionResult.Error("No delete endpoint")

        // Sustituir {id} en el endpoint
        val resolvedEndpoint = endpoint.replace("{id}",
            context.selectedItemId ?: context.fieldValues["id"] ?: ""
        )

        return try {
            apiClient.delete(resolvedEndpoint)
            ActionResult.Success(message = "Eliminado exitosamente")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Error eliminando", retry = true)
        }
    }
}
```

### ConfirmHandler (muestra dialogo antes de ejecutar)

```kotlin
class ConfirmHandler(
    private val registry: ActionRegistry,
    private val dialogService: DialogService,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val confirmed = dialogService.showConfirmation(
            title = context.config.title ?: "Confirmar",
            message = context.config.message ?: "Esta seguro?",
            confirmLabel = context.config.confirmLabel ?: "Aceptar",
            cancelLabel = context.config.cancelLabel ?: "Cancelar",
        )

        if (!confirmed) return ActionResult.Cancelled

        // Si confirmo, ejecutar la accion real
        val innerAction = context.config.onConfirm
            ?: return ActionResult.Success()

        val innerHandler = registry.resolve(innerAction)
        return innerHandler.execute(context.copy(config = innerAction.config))
    }
}
```

---

## Validaciones Declarativas (JSON)

Las reglas de validacion se definen en el JSON de la instancia de pantalla:

```json
{
  "validations": [
    {
      "fieldId": "nombre",
      "rules": [
        {"type": "required", "message": "El nombre es obligatorio"},
        {"type": "minLength", "min": 2, "message": "Minimo 2 caracteres"},
        {"type": "maxLength", "max": 100}
      ]
    },
    {
      "fieldId": "email",
      "rules": [
        {"type": "required"},
        {"type": "email", "message": "Ingresa un correo valido"}
      ]
    },
    {
      "fieldId": "edad",
      "rules": [
        {"type": "numeric"},
        {"type": "range", "min": 1, "max": 120, "message": "Edad no valida"}
      ]
    },
    {
      "fieldId": "password",
      "rules": [
        {"type": "required"},
        {"type": "minLength", "min": 8},
        {"type": "pattern", "pattern": "^(?=.*[A-Z])(?=.*\\d).+$", "message": "Debe tener mayuscula y numero"}
      ]
    },
    {
      "fieldId": "terminos",
      "rules": [
        {"type": "required", "message": "Debes aceptar los terminos"}
      ]
    }
  ]
}
```

### Tipos de Validacion Soportados

| Tipo | Parametros | Descripcion |
|------|-----------|-------------|
| `required` | - | Campo no puede estar vacio |
| `email` | - | Debe contener @ y formato basico |
| `minLength` | `min` | Largo minimo |
| `maxLength` | `max` | Largo maximo |
| `pattern` | `pattern` (regex) | Debe cumplir un patron |
| `numeric` | - | Debe ser numero |
| `range` | `min`, `max` | Rango numerico |
| `equals` | `otherFieldId` | Debe ser igual a otro campo (ej: confirmar password) |
| `custom` | `handlerId` | Validacion custom con logica especifica |

---

## Encadenamiento de Acciones

Las acciones pueden encadenarse: una accion dispara otra al terminar.

```json
{
  "id": "submit_registration",
  "type": "SUBMIT_FORM",
  "config": {
    "validateFirst": true,
    "endpoint": "/api/students",
    "method": "POST",
    "onSuccess": {
      "type": "CONFIRM",
      "config": {
        "title": "Registro exitoso!",
        "message": "Quieres ir a tu perfil?",
        "confirmLabel": "Ir al perfil",
        "cancelLabel": "Quedarme aqui",
        "onConfirm": {
          "type": "NAVIGATE",
          "config": {"target": "/profile/{id}"}
        }
      }
    },
    "onError": {
      "type": "SHOW_SNACKBAR",
      "config": {"message": "Error al registrar. Intenta de nuevo.", "variant": "error"}
    }
  }
}
```

Flujo:
```
SUBMIT_FORM
  ├── validate() → errores? → mostrar en campos
  ├── save() → error? → SHOW_SNACKBAR (error)
  └── save() → exito? → CONFIRM (dialogo)
                           ├── confirma → NAVIGATE (/profile/123)
                           └── cancela → nada
```

---

## Integracion con el Renderer Dinamico

### DynamicScreenViewModel

```kotlin
class DynamicScreenViewModel(
    private val screenLoader: DynamicScreenLoader,
    private val actionRegistry: ActionRegistry,
) : ViewModel() {

    private val _screenState = MutableStateFlow<DynamicScreenState>(DynamicScreenState.Loading)
    val screenState: StateFlow<DynamicScreenState> = _screenState

    private val _fieldValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldValues: StateFlow<Map<String, String>> = _fieldValues

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadScreen(screenId: String) {
        viewModelScope.launch {
            _screenState.value = DynamicScreenState.Loading
            try {
                val screen = screenLoader.load(screenId)
                _fieldValues.value = screen.initialValues
                _screenState.value = DynamicScreenState.Ready(screen)
            } catch (e: Exception) {
                _screenState.value = DynamicScreenState.Error(e.message ?: "Error")
            }
        }
    }

    fun onFieldChanged(fieldId: String, value: String) {
        _fieldValues.update { it + (fieldId to value) }
        // Limpiar error del campo al editarlo
        _fieldErrors.update { it - fieldId }
    }

    fun onAction(actionId: String) {
        val screen = (_screenState.value as? DynamicScreenState.Ready)?.screen ?: return
        val actionDef = screen.actions.find { it.id == actionId } ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val handler = actionRegistry.resolve(actionDef)
                val context = ActionContext(
                    screenId = screen.id,
                    actionId = actionId,
                    fieldValues = _fieldValues.value,
                    config = actionDef.config,
                    triggeredBy = actionDef.triggerSlotId,
                )

                when (val result = handler.execute(context)) {
                    is ActionResult.Success -> {
                        // Ejecutar accion siguiente si existe
                        result.nextAction?.let { next ->
                            onAction(next.id)
                        }
                    }
                    is ActionResult.ValidationError -> {
                        _fieldErrors.value = result.fieldErrors
                    }
                    is ActionResult.Error -> {
                        // Mostrar error (snackbar, toast, etc.)
                    }
                    is ActionResult.Cancelled -> { /* nada */ }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### Uso en Composable

```kotlin
@Composable
fun DynamicScreen(screenId: String) {
    val viewModel: DynamicScreenViewModel = koinViewModel()

    LaunchedEffect(screenId) {
        viewModel.loadScreen(screenId)
    }

    val state by viewModel.screenState.collectAsState()
    val fieldValues by viewModel.fieldValues.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    when (val s = state) {
        is DynamicScreenState.Loading -> LoadingIndicator()
        is DynamicScreenState.Error -> ErrorState(s.message)
        is DynamicScreenState.Ready -> {
            DynamicScreenRenderer(
                definition = s.screen.template,
                fieldValues = fieldValues,
                fieldErrors = fieldErrors,
                isLoading = isLoading,
                onFieldChanged = viewModel::onFieldChanged,
                onAction = viewModel::onAction,
            )
        }
    }
}
```

---

## JSON Completo: Las 3 Capas Unidas

Ejemplo de lo que el backend enviaria al cliente:

```json
{
  "screenId": "registro-estudiante",
  "template": {
    "pattern": "form",
    "navigation": {
      "topBar": {"title": "Registro de Estudiante", "leadingAction": "back"}
    },
    "zones": [
      {
        "id": "datos_personales",
        "type": "form-section",
        "title": "Datos Personales",
        "slots": [
          {"id": "nombre", "controlType": "text-input", "width": "full"},
          {"id": "email", "controlType": "email-input", "width": "full"},
          {"id": "edad", "controlType": "number-input", "width": "full"}
        ]
      },
      {
        "id": "preferencias",
        "type": "form-section",
        "title": "Preferencias",
        "slots": [
          {"id": "notificaciones", "controlType": "switch"},
          {"id": "terminos", "controlType": "checkbox"}
        ]
      },
      {
        "id": "acciones",
        "type": "action",
        "slots": [
          {"id": "submit_btn", "controlType": "filled-button", "width": "full"}
        ]
      }
    ]
  },
  "data": {
    "nombre": {"label": "Nombre completo", "placeholder": "Ej: Juan Perez"},
    "email": {"label": "Correo electronico", "placeholder": "usuario@ejemplo.com"},
    "edad": {"label": "Edad", "placeholder": "25"},
    "notificaciones": {"label": "Recibir notificaciones", "defaultValue": "true"},
    "terminos": {"label": "Acepto terminos y condiciones"},
    "submit_btn": {"label": "Registrar"}
  },
  "validations": [
    {"fieldId": "nombre", "rules": [{"type": "required"}, {"type": "minLength", "min": 2}]},
    {"fieldId": "email", "rules": [{"type": "required"}, {"type": "email"}]},
    {"fieldId": "edad", "rules": [{"type": "numeric"}, {"type": "range", "min": 1, "max": 120}]},
    {"fieldId": "terminos", "rules": [{"type": "required", "message": "Debes aceptar los terminos"}]}
  ],
  "actions": [
    {
      "id": "submit",
      "trigger": "button_click",
      "triggerSlotId": "submit_btn",
      "type": "SUBMIT_FORM",
      "config": {
        "validateFirst": true,
        "endpoint": "/api/students",
        "method": "POST",
        "fieldMapping": "auto",
        "onSuccess": {"type": "NAVIGATE", "config": {"target": "/success"}},
        "onError": {"type": "SHOW_SNACKBAR", "config": {"message": "Error al registrar"}}
      }
    }
  ]
}
```

---

## Handlers Custom: Cuando lo Estandar No Alcanza

Para logica de negocio especifica, se registran handlers custom:

```kotlin
// En el modulo de la app, no en el framework dinamico
class CalcularNotaFinalHandler(
    private val gradingService: GradingService,
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val parcial1 = context.fieldValues["parcial1"]?.toDoubleOrNull() ?: 0.0
        val parcial2 = context.fieldValues["parcial2"]?.toDoubleOrNull() ?: 0.0
        val final = context.fieldValues["final"]?.toDoubleOrNull() ?: 0.0

        val nota = gradingService.calculate(parcial1, parcial2, final)

        return ActionResult.Success(
            data = mapOf("nota_final" to nota, "aprobado" to (nota >= 3.0))
        )
    }
}

// Registro al iniciar la app
actionRegistry.registerCustom("calcular-nota-final", CalcularNotaFinalHandler(gradingService))
```

Y en el JSON:
```json
{
  "id": "calcular",
  "trigger": "button_click",
  "triggerSlotId": "calcular_btn",
  "type": "CUSTOM",
  "customHandlerId": "calcular-nota-final"
}
```

---

## Resumen: Las Interfaces Clave

```
ActionHandler          → interface para TODA accion (estandar o custom)
ActionRegistry         → registro central: resuelve actionId → handler
ActionContext          → datos que recibe el handler (campos, config, params)
ActionResult           → resultado: Success, ValidationError, Error, Cancelled
ActionConfig           → configuracion del JSON (endpoint, validaciones, next action)
DynamicScreenViewModel → orquesta: carga pantalla, maneja campos, despacha acciones
```

Cuantas clases custom escribes para una nueva pantalla?
- Si es CRUD estandar: **cero** (todo declarativo en JSON)
- Si tiene logica especifica: **una** (el handler custom)
