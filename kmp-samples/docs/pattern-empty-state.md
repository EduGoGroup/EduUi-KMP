# Empty State - Distribucion Estructural

## Mobile (Android/iOS)

Multiples variantes de estado vacio, cada uno en una card.

```
PANTALLA
├── Contenido (scroll vertical)
│   │
│   ├── VARIANTE 1: Sin datos
│   │   └── ElevatedCard (full-width, padding):
│   │       └── EmptyState:
│   │           ├── Icono grande (centrado)
│   │           ├── Titulo "No hay datos" (headline, centrado)
│   │           ├── Descripcion (body, centrado)
│   │           └── Boton accion (filled)
│   │
│   ├── VARIANTE 2: Error de red
│   │   └── ElevatedCard:
│   │       └── EmptyState:
│   │           ├── Icono wifi-off
│   │           ├── Titulo "Sin conexion"
│   │           ├── Descripcion
│   │           └── Boton "Reintentar"
│   │
│   ├── VARIANTE 3: Sin resultados de busqueda
│   │   └── ElevatedCard:
│   │       └── EmptyState:
│   │           ├── Icono search-off
│   │           ├── Titulo "Sin resultados"
│   │           └── Descripcion
│   │
│   └── VARIANTE 4: Primer uso
│       └── ElevatedCard:
│           └── EmptyState:
│               ├── Icono celebracion
│               ├── Titulo "Bienvenido"
│               ├── Descripcion
│               └── Boton "Empezar"
```

## Estructura del Componente EmptyState

```
EmptyState (componente reutilizable)
├── Alineacion: centrado total (horizontal + vertical)
├── Padding: grande
│
├── Icono [opcional] (grande, color=onSurfaceVariant)
├── Espaciado
├── Titulo (headline-small, centrado)
├── Espaciado
├── Descripcion (body-medium, centrado, color=onSurfaceVariant)
├── Espaciado
└── Boton accion [opcional] (filled o outlined)
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "empty-state",
  "type": "feedback-pattern",
  "variants": [
    {
      "id": "no_data",
      "trigger": "collection.isEmpty && !isLoading && !isError",
      "slots": [
        {"id": "icon", "controlType": "icon", "icon": "inbox", "optional": true},
        {"id": "title", "controlType": "label", "style": "headline-small"},
        {"id": "description", "controlType": "label", "style": "body"},
        {"id": "action", "controlType": "filled-button", "optional": true}
      ]
    },
    {
      "id": "network_error",
      "trigger": "isError && errorType == 'network'",
      "slots": [
        {"id": "icon", "controlType": "icon", "icon": "wifi-off"},
        {"id": "title", "controlType": "label"},
        {"id": "description", "controlType": "label"},
        {"id": "retry", "controlType": "filled-button"}
      ]
    },
    {
      "id": "no_results",
      "trigger": "searchResults.isEmpty && query.isNotEmpty",
      "slots": [
        {"id": "icon", "controlType": "icon", "icon": "search-off"},
        {"id": "title", "controlType": "label"},
        {"id": "description", "controlType": "label"}
      ]
    },
    {
      "id": "first_use",
      "trigger": "isFirstUse",
      "slots": [
        {"id": "icon", "controlType": "icon", "icon": "celebration"},
        {"id": "title", "controlType": "label"},
        {"id": "description", "controlType": "label"},
        {"id": "start", "controlType": "filled-button"}
      ]
    }
  ]
}
```
