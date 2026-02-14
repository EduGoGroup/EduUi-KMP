# Roadmap de Implementacion - UI Dinamica Completa

## Vision General

```
Fase 1: Renderer basico (YA EXISTE)     ██████████ 100%
Fase 2: Validaciones declarativas        ░░░░░░░░░░   0%
Fase 3: Acciones estandar                ░░░░░░░░░░   0%
Fase 4: Renderer multi-pattern           ░░░░░░░░░░   0%
Fase 5: Backend + persistencia           ░░░░░░░░░░   0%
Fase 6: Acciones custom + encadenamiento ░░░░░░░░░░   0%
```

---

## Fase 1: Renderer de Formulario Basico ✅ COMPLETADA

Lo que ya existe en `patterns/dynamic/`:

- [x] `DynamicFormModel.kt` - Modelos serializables (DynamicField, DynamicSection, DynamicFormDefinition)
- [x] `DynamicFormRenderer.kt` - Composable que renderiza campos desde JSON
- [x] `DynamicFormSample.kt` - Ejemplos con preview (registro + contacto)
- [x] Soporta 10 tipos de control (text, email, password, number, textarea, checkbox, switch, radio, date, select)
- [x] Estado local con `mutableStateMapOf`
- [x] Callback `onSubmit(Map<String, String>)`

**Resultado**: un JSON genera una pantalla funcional con campos editables.

---

## Fase 2: Validaciones Declarativas

### Objetivo
Agregar reglas de validacion al JSON y que el renderer las aplique automaticamente.

### Archivos a Crear

```
modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/
├── validation/
│   ├── ValidationRule.kt          → data class serializable
│   ├── ValidationEngine.kt        → motor que ejecuta reglas
│   └── ValidationResult.kt        → sealed class con errores por campo
```

### Tareas

1. **Modelo de reglas** (`ValidationRule.kt`)
   - Data class: `fieldId`, `type`, `min`, `max`, `pattern`, `message`
   - Serializable desde JSON
   - Tipos: required, email, minLength, maxLength, pattern, numeric, range, equals

2. **Motor de validacion** (`ValidationEngine.kt`)
   - Recibe `List<ValidationRule>` + `Map<String, String>` (valores)
   - Retorna `Map<String, String>` (fieldId → error message)
   - Puro (sin dependencias de UI ni red)
   - Testeable unitariamente

3. **Integrar con renderer**
   - Agregar campo `validations` al `DynamicFormDefinition`
   - El renderer muestra errores debajo de cada campo (`isError=true`, `supportingText=error`)
   - Validar al hacer submit y tambien en `onBlur` (al salir del campo)

4. **Tests**
   - Validar cada tipo de regla
   - Validar combinacion de reglas en un campo
   - Validar campo con multiples errores (mostrar el primero)

### Dependencias
- Ninguna externa (puro Kotlin)

---

## Fase 3: Acciones Estandar

### Objetivo
Crear el sistema de ActionHandler para que botones ejecuten acciones sin codigo custom.

### Archivos a Crear

```
modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/
├── action/
│   ├── ActionHandler.kt           → interface base
│   ├── ActionContext.kt           → data class con contexto
│   ├── ActionResult.kt            → sealed class resultado
│   ├── ActionDefinition.kt        → modelo serializable desde JSON
│   ├── ActionRegistry.kt          → registro de handlers
│   └── handlers/
│       ├── SaveHandler.kt         → POST/PUT al endpoint
│       ├── DeleteHandler.kt       → DELETE al endpoint
│       ├── SearchHandler.kt       → GET con query params
│       ├── ValidateHandler.kt     → ejecuta ValidationEngine
│       ├── SubmitFormHandler.kt   → validate + save combo
│       ├── NavigateHandler.kt     → router navigation
│       ├── RefreshHandler.kt      → recargar datos
│       └── ConfirmHandler.kt      → mostrar dialogo antes
```

### Tareas

1. **Interfaces base** (ActionHandler, ActionContext, ActionResult)
   - Definir contratos
   - ActionContext lleva: screenId, fieldValues, config, params

2. **ActionDefinition** (modelo JSON)
   - Serializable
   - Campos: id, trigger, triggerSlotId, type, config, onSuccess, onError

3. **Handlers estandar CRUD**
   - SaveHandler: requiere HttpClient (inyectado via Koin)
   - DeleteHandler: endpoint con `{id}` sustituible
   - SearchHandler: query params automaticos

4. **ActionRegistry**
   - Registra handlers estandar en constructor
   - Metodo `registerCustom()` para extensiones
   - Resolucion por tipo de accion

5. **Integrar con DynamicFormDefinition**
   - Agregar `actions: List<ActionDefinition>` al modelo
   - Mapear `triggerSlotId` al boton correspondiente

6. **Tests**
   - Mock de HttpClient para probar SaveHandler, DeleteHandler
   - Test de ActionRegistry resolve
   - Test de SubmitFormHandler (validate + save)

### Dependencias
- `modules/network` (HttpClient)
- Fase 2 (ValidateHandler usa ValidationEngine)

---

## Fase 4: Renderer Multi-Pattern

### Objetivo
Expandir el renderer mas alla de formularios: soportar list, settings, detail, dashboard.

### Archivos a Crear

```
modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/
├── model/
│   ├── DynamicScreenDefinition.kt  → reemplaza DynamicFormDefinition
│   ├── Zone.kt                     → modelo de zona
│   ├── Slot.kt                     → modelo de slot
│   ├── NavigationConfig.kt         → configuracion de topbar/bottomnav
│   └── PlatformOverride.kt         → overrides por plataforma
├── renderer/
│   ├── DynamicScreenRenderer.kt    → renderer principal (despacha por pattern)
│   ├── ZoneRenderer.kt             → renderiza una zona segun su tipo
│   ├── SlotRenderer.kt             → renderiza un slot segun su controlType
│   ├── platform/
│   │   ├── MobileLayoutResolver.kt → aplica layout mobile
│   │   ├── DesktopLayoutResolver.kt→ aplica layout desktop (split panels)
│   │   └── WebLayoutResolver.kt    → aplica layout web (responsive cards)
│   └── patterns/
│       ├── FormPatternRenderer.kt  → logica especifica de formularios
│       ├── ListPatternRenderer.kt  → logica especifica de listas
│       ├── SettingsPatternRenderer.kt
│       ├── DetailPatternRenderer.kt
│       └── DashboardPatternRenderer.kt
```

### Tareas

1. **Modelos genericos** (Zone, Slot, DynamicScreenDefinition)
   - Reemplazar DynamicFormDefinition por DynamicScreenDefinition
   - Zone contiene Slots u otras Zones (recursivo)
   - Slot mapea 1:1 a un componente DS

2. **SlotRenderer** (el nucleo)
   - Switch por `controlType` → componente DS correspondiente
   - Reutiliza la logica de DynamicFieldRenderer pero generalizada

3. **ZoneRenderer**
   - Aplica distribucion (stacked, side-by-side, grid, flow-row)
   - Maneja separadores entre items
   - Soporta titulo de seccion

4. **Platform Resolvers**
   - Detectan plataforma (actual() en KMP)
   - Aplican `platformOverrides` del JSON
   - Reorganizan zones en panels (left/center/right)

5. **Pattern Renderers**
   - Cada patron tiene logica especifica:
     - List: agrupacion, seleccion, master-detail
     - Settings: categorias selectables, contenido dinamico
     - Dashboard: metric cards, grids

### Dependencias
- Fase 1 (base del renderer)
- `kmp-design` (todos los componentes DS)

---

## Fase 5: Backend + Persistencia

### Objetivo
API para servir templates y datos, con persistencia en base de datos.

### Tablas (ver persistence-strategy.md)

```sql
screen_templates      → definiciones de layout
screen_instances      → instancias con datos concretos
screen_field_values   → valores por usuario
```

### Endpoints API

```
GET    /api/screens/{screenId}           → JSON combinado (template + data + values)
PUT    /api/screens/{screenId}/values    → guardar valores del usuario
GET    /api/templates                    → listar templates disponibles
POST   /api/templates                    → crear template nuevo
GET    /api/screens                      → listar pantallas configuradas
POST   /api/screens                      → crear instancia de pantalla
```

### Tareas

1. **Modelo de datos** en backend (tablas, relaciones)
2. **API REST** para CRUD de templates y screens
3. **Endpoint combinado** que resuelve slots y envia JSON listo
4. **Cache** (templates: largo plazo, values: corto plazo)
5. **DynamicScreenLoader** en KMP (cliente HTTP que llama al API)

### Dependencias
- Backend (edugo-api-mobile o nuevo microservicio)
- Fase 4 (modelos del cliente deben matchear el API)

---

## Fase 6: Acciones Custom + Encadenamiento

### Objetivo
Soporte completo para logica de negocio custom y flujos multi-paso.

### Tareas

1. **Encadenamiento de acciones**
   - `onSuccess` / `onError` disparan otra ActionDefinition
   - Flujos: validate → save → navigate, o delete → confirm → refresh

2. **Acciones custom** registrables por modulo
   - Interface ActionHandler que los modulos implementan
   - Registro via Koin en el modulo que corresponda
   - Ejemplo: `CalcularNotaHandler`, `GenerarReporteHandler`

3. **Acciones de UI** (no-API)
   - SHOW_SNACKBAR, SHOW_TOAST, SHOW_DIALOG
   - TOGGLE_FIELD (activar/desactivar otro campo)
   - SET_FIELD_VALUE (setear valor de otro campo programaticamente)
   - SHOW_LOADING, HIDE_LOADING

4. **Acciones condicionales**
   - Ejecutar accion solo si una condicion se cumple
   - Ejemplo: "si edad < 18, mostrar campo tutor"
   ```json
   {"type": "TOGGLE_FIELD", "condition": "edad < 18", "targetField": "tutor"}
   ```

5. **Tests de integracion**
   - Flujos end-to-end: cargar pantalla → llenar campos → submit → validar → guardar → navegar

### Dependencias
- Fase 3 (ActionRegistry)
- Fase 5 (backend para probar end-to-end)

---

## Orden de Prioridad Sugerido

```
Fase 2 (validaciones)         → valor inmediato, sin dependencias
Fase 3 (acciones estandar)    → depende de Fase 2
Fase 4 (multi-pattern)        → puede ir en paralelo con Fase 3
Fase 5 (backend)              → puede iniciar en paralelo con Fase 4
Fase 6 (custom + chains)      → al final, cuando todo lo demas esta estable
```

## Modulo Sugerido

Todo el framework de UI dinamica deberia vivir en un nuevo modulo:

```
modules/dynamic-ui/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/edugo/kmp/dynamicui/
    │   ├── model/          → modelos serializables
    │   ├── validation/     → motor de validacion
    │   ├── action/         → handlers de acciones
    │   ├── renderer/       → composables de renderizado
    │   └── loader/         → carga desde API/cache
    └── commonTest/kotlin/com/edugo/kmp/dynamicui/
        ├── validation/     → tests de validacion
        ├── action/         → tests de handlers
        └── renderer/       → tests de renderizado
```

Esto mantiene la separacion de responsabilidades y permite que `kmp-samples` solo sea para demos/previews.
