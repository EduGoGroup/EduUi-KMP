# Onboarding - Distribucion Estructural

## Mobile (Android/iOS)

Pantalla completa con paso actual, indicador de pagina, y navegacion.

```
PANTALLA
├── Distribucion: stacked (columna, full-size)
├── Padding: horizontal medio, vertical grande
│
├── ZONA: Header
│   └── Fila (alineado derecha):
│       └── TextButton "Saltar" (alineado end)
│
├── ZONA: Contenido del Paso [weight=1, centrado vertical y horizontal]
│   ├── Icono grande (icono del paso actual)
│   ├── Espaciado
│   ├── Titulo del paso (headline, centrado)
│   ├── Espaciado
│   └── Descripcion (body, centrado, max-lines)
│
├── ZONA: Indicador de Pagina
│   └── Fila centrada (dots):
│       ├── Dot activo (primary, grande)
│       ├── Dot inactivo (outline, pequeno)
│       ├── Dot inactivo
│       └── Dot inactivo
│
├── Espaciado
│
└── ZONA: Accion
    └── Boton "Siguiente" o "Comenzar" (filled, full-width)
```

## Web

Todos los pasos visibles como cards horizontales.

```
PANTALLA
├── Scroll: vertical
├── Alineacion: centrado horizontal
├── Padding: grande
│
├── ZONA: Steps Overview
│   └── Fila (side-by-side, espaciado):
│       ├── StepCard [weight=1]:
│       │   ├── Icono (grande, centrado)
│       │   ├── Titulo paso (centrado)
│       │   └── Descripcion (centrado)
│       ├── StepCard [weight=1]
│       ├── StepCard [weight=1]
│       └── StepCard [weight=1]
│
├── ZONA: Indicador
│   └── Dots centrados
│
└── ZONA: Accion
    └── Boton "Comenzar" (filled, ancho fijo)
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "onboarding",
  "zones": [
    {
      "id": "skip",
      "type": "container",
      "alignment": "end",
      "slots": [
        {"id": "skip_btn", "controlType": "text-button"}
      ]
    },
    {
      "id": "step_content",
      "type": "paged-container",
      "alignment": "center-both",
      "expandVertical": true,
      "pageLayout": {
        "distribution": "stacked",
        "alignment": "center",
        "slots": [
          {"id": "step_icon", "controlType": "icon", "size": "xlarge"},
          {"id": "step_title", "controlType": "label", "style": "headline"},
          {"id": "step_description", "controlType": "label", "style": "body"}
        ]
      }
    },
    {
      "id": "page_indicator",
      "type": "page-indicator",
      "alignment": "center",
      "activeColor": "primary",
      "inactiveColor": "outline"
    },
    {
      "id": "action",
      "type": "action",
      "slots": [
        {"id": "next_btn", "controlType": "filled-button", "width": "full",
         "labelRule": {"default": "slot:next_label", "lastPage": "slot:start_label"}}
      ]
    }
  ],
  "platformOverrides": {
    "web": {
      "showAllSteps": true,
      "step_content": {"distribution": "side-by-side", "perStepWeight": 1},
      "action": {"width": "fixed"}
    }
  }
}
```
