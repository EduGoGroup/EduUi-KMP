# Estrategia de Persistencia - Template vs Data

## El Problema

Queremos construir pantallas dinamicamente combinando:
1. **Template**: la plantilla de distribucion (layout, zonas, controles)
2. **Data**: los datos concretos que llenan esos controles (labels, valores, opciones)

La pregunta es: como guardar ambos en base de datos y combinarlos en runtime?

## Modelo de Datos

### Tabla: `screen_templates`

Guarda la definicion de layout. Es **reutilizable** (un template puede servir para muchas pantallas).

```sql
CREATE TABLE screen_templates (
    id          TEXT PRIMARY KEY,       -- "settings-v1"
    pattern     TEXT NOT NULL,          -- "settings"
    version     INTEGER DEFAULT 1,
    definition  JSONB NOT NULL,         -- JSON completo del template
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);
```

Ejemplo de `definition`:
```json
{
  "navigation": {"topBar": {"title": "slot:page_title", "leadingAction": "back"}},
  "zones": [
    {
      "id": "notifications",
      "type": "settings-section",
      "title": "slot:section_title",
      "slots": [
        {"id": "push", "controlType": "switch"},
        {"id": "email_notif", "controlType": "switch"},
        {"id": "sound", "controlType": "switch"}
      ]
    }
  ],
  "platformOverrides": { "..." }
}
```

### Tabla: `screen_instances`

Una instancia concreta de pantalla: "la pantalla de configuracion de la app EduGo".

```sql
CREATE TABLE screen_instances (
    id              TEXT PRIMARY KEY,       -- "edugo-settings"
    template_id     TEXT NOT NULL,          -- FK -> "settings-v1"
    name            TEXT NOT NULL,          -- "Configuracion EduGo"
    slot_data       JSONB NOT NULL,         -- datos que llenan los slots
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES screen_templates(id)
);
```

Ejemplo de `slot_data`:
```json
{
  "page_title": "Configuracion",
  "section_title": "Notificaciones",
  "push": {
    "label": "Notificaciones push",
    "defaultValue": "true"
  },
  "email_notif": {
    "label": "Notificaciones por email",
    "defaultValue": "false"
  },
  "sound": {
    "label": "Sonido",
    "defaultValue": "true"
  }
}
```

### Tabla: `screen_field_values` (valores de usuario en runtime)

Guarda el estado actual de los campos por usuario.

```sql
CREATE TABLE screen_field_values (
    id              TEXT PRIMARY KEY,
    screen_id       TEXT NOT NULL,          -- FK -> screen_instances.id
    user_id         TEXT NOT NULL,
    field_id        TEXT NOT NULL,          -- "push", "email_notif", etc.
    value           TEXT,                   -- "true", "false", "Espanol", etc.
    updated_at      TIMESTAMP,
    FOREIGN KEY (screen_id) REFERENCES screen_instances(id),
    UNIQUE(screen_id, user_id, field_id)
);
```

## Flujo en Runtime

```
1. App pide pantalla "edugo-settings"
   │
2. Backend busca screen_instances WHERE id = "edugo-settings"
   │  → Obtiene template_id = "settings-v1" y slot_data
   │
3. Backend busca screen_templates WHERE id = "settings-v1"
   │  → Obtiene definition (el layout JSON)
   │
4. Backend busca screen_field_values WHERE screen_id AND user_id
   │  → Obtiene valores actuales del usuario
   │
5. Backend combina todo y envia al cliente:
   │
   ├── template.definition  → Como distribuir la UI
   ├── instance.slot_data   → Que labels/config poner
   └── user.field_values    → Que valores tiene el usuario
   │
6. Cliente (KMP) parsea y renderiza con DynamicFormRenderer
```

## JSON Combinado que Recibe el Cliente

El backend puede combinar las tres fuentes en un solo JSON:

```json
{
  "screenId": "edugo-settings",
  "template": {
    "pattern": "settings",
    "navigation": {
      "topBar": {"title": "Configuracion", "leadingAction": "back"}
    },
    "zones": [
      {
        "id": "notifications",
        "type": "settings-section",
        "title": "Notificaciones",
        "slots": [
          {
            "id": "push",
            "controlType": "switch",
            "label": "Notificaciones push",
            "value": "true"
          },
          {
            "id": "email_notif",
            "controlType": "switch",
            "label": "Notificaciones por email",
            "value": "false"
          },
          {
            "id": "sound",
            "controlType": "switch",
            "label": "Sonido",
            "value": "true"
          }
        ]
      }
    ],
    "platformOverrides": {
      "desktop": {
        "rootDistribution": "side-by-side",
        "extractCategories": true
      }
    }
  }
}
```

## Ventajas de Esta Separacion

| Aspecto | Beneficio |
|---------|-----------|
| **Reutilizacion** | Un template "form-v1" sirve para registros, ediciones, encuestas |
| **A/B Testing** | Cambiar template_id en screen_instances para probar layouts distintos |
| **Versionado** | Crear "settings-v2" sin romper instancias existentes |
| **Multi-idioma** | slot_data puede tener variantes por locale |
| **Sin deploy** | Cambiar la pantalla desde la DB sin actualizar la app |
| **Auditoria** | screen_field_values guarda historial de cambios |

## Consideraciones de Implementacion

### 1. Resolucion de Slots

Los slots usan prefijo `slot:` en el template para indicar que el valor viene del data:

```
Template dice:  "title": "slot:page_title"
Data dice:      "page_title": "Configuracion"
Resultado:      "title": "Configuracion"
```

### 2. Platform Detection

El cliente detecta la plataforma y aplica los overrides:

```kotlin
val platform = when {
    isAndroid || isIOS -> "mobile"
    isDesktop -> "desktop"
    isWasmJs -> "web"
}

val effectiveLayout = template.applyOverrides(platform, screenWidth)
```

### 3. Responsive dentro de Web

Para web, ademas de la plataforma se necesita el breakpoint actual:

```kotlin
val breakpoint = when {
    screenWidth < 600.dp -> "compact"
    screenWidth < 840.dp -> "medium"
    else -> "expanded"
}
```

### 4. Cache

Templates cambian poco, los datos del usuario cambian seguido:

```
Templates     → Cache largo (1 dia, invalidar manualmente)
Slot data     → Cache medio (1 hora)
Field values  → Cache corto (5 min) o real-time
```

### 5. Fallback

Si el template no se puede cargar, la app deberia tener un fallback compilado:

```kotlin
val screen = try {
    dynamicScreenLoader.load("edugo-settings")
} catch (e: Exception) {
    // Fallback a pantalla estatica compilada
    SettingsMobileSampleContent()
}
```

## Diagrama ER

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ screen_templates │       │ screen_instances  │       │screen_field_values│
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │◄──────│ template_id (FK)  │◄──────│ screen_id (FK)   │
│ pattern          │       │ id (PK)           │       │ id (PK)          │
│ version          │       │ name              │       │ user_id          │
│ definition (JSON)│       │ slot_data (JSON)  │       │ field_id         │
│ created_at       │       │ created_at        │       │ value            │
│ updated_at       │       │ updated_at        │       │ updated_at       │
└──────────────────┘       └──────────────────┘       └──────────────────┘
                                                            │
                                                            │ user_id
                                                            ▼
                                                      ┌──────────┐
                                                      │  users   │
                                                      └──────────┘
```

## Ejemplo Completo: Crear una Pantalla de Encuesta

### 1. Reutilizar template "form-v1" existente

No necesitas crear un nuevo template. El template de formulario ya sabe como distribuir secciones con campos.

### 2. Crear la instancia

```json
{
  "id": "encuesta-satisfaccion-2024",
  "template_id": "form-v1",
  "name": "Encuesta de Satisfaccion",
  "slot_data": {
    "form_title": "Encuesta de Satisfaccion",
    "section_1_title": "Tu Experiencia",
    "field_1": {"label": "Como calificas la plataforma?", "controlType": "radio", "options": ["Excelente", "Buena", "Regular", "Mala"]},
    "field_2": {"label": "Que mejorias sugieres?", "controlType": "textarea", "placeholder": "Escribe aqui..."},
    "section_2_title": "Datos Opcionales",
    "field_3": {"label": "Nombre", "controlType": "text"},
    "field_4": {"label": "Email de contacto", "controlType": "email"},
    "submit_label": "Enviar Respuestas"
  }
}
```

### 3. El renderer hace el resto

La app carga el JSON combinado y el `DynamicFormRenderer` (o su version evolucionada `DynamicScreenRenderer`) dibuja todo automaticamente con los componentes del Design System.
