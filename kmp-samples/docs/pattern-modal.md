# Modal - Distribucion Estructural

## Mobile (Android/iOS)

Cuatro variantes de dialogo, apiladas como ejemplos.

```
VARIANTE 1: Bottom Sheet
├── Card Elevada (parte inferior, full-width)
│   ├── Drag handle (centrado, pequeno)
│   ├── Opciones (radio buttons apilados):
│   │   ├── RadioButton "Opcion 1"
│   │   ├── RadioButton "Opcion 2"
│   │   └── RadioButton "Opcion 3"
│   └── Fila acciones (alineado derecha):
│       ├── TextButton "Cancelar"
│       └── FilledButton "Aceptar"

VARIANTE 2: Alert Dialog
├── Card Elevada (centrada)
│   ├── Icono advertencia (centrado)
│   ├── Titulo (headline, centrado)
│   ├── Descripcion (body, centrado)
│   └── Fila acciones (alineado derecha):
│       ├── OutlinedButton "Cancelar"
│       └── FilledButton "Aceptar"

VARIANTE 3: Fullscreen Dialog
├── Card Elevada (casi full-screen)
│   ├── TopBar:
│   │   ├── Izquierda: icono cerrar (X)
│   │   ├── Centro: titulo
│   │   └── Derecha: TextButton "Guardar"
│   ├── Contenido (scroll):
│   │   ├── Campo texto 1
│   │   └── Campo texto 2
│   └── Padding interno

VARIANTE 4: Form Dialog
├── Card Elevada (centrada, ancho medio)
│   ├── Titulo
│   ├── TextField multilinea (textarea, 4 lineas)
│   └── Fila acciones (alineado derecha):
│       ├── TextButton "Cancelar"
│       └── FilledButton "Enviar"
```

## Desktop

Variantes adaptadas al espacio mayor.

```
VARIANTE 1: Alert Dialog
├── Card Elevada [max-width=600dp, centrada]:
│   ├── Icono + Titulo
│   ├── Descripcion
│   └── Fila acciones (alineado derecha):
│       ├── OutlinedButton
│       └── FilledButton

VARIANTE 2: Side Sheet (propio de desktop)
├── Row (full-width, height fijo ~350dp):
│   ├── Area principal [weight=1]:
│   │   └── Contenido detras (oscurecido/dimmed)
│   └── Panel lateral [width=400dp, Surface con elevacion]:
│       ├── Fila titulo:
│       │   ├── Titulo
│       │   └── IconButton cerrar (X)
│       ├── Campos de formulario (stacked)
│       └── Fila acciones

VARIANTE 3: Bottom Sheet (adaptado)
├── Card Elevada [max-width=600dp, centrada]:
│   ├── RadioButtons
│   └── Acciones
```

## Web

Similar a Desktop con restricciones de ancho.

```
Mismas variantes que Desktop pero:
- Cards siempre centradas
- max-width restringido (480-600dp)
- Overlays con backdrop
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "modal",
  "type": "overlay-pattern",
  "variants": [
    {
      "id": "alert",
      "trigger": "confirmation-needed",
      "distribution": "centered-card",
      "maxWidth": 600,
      "slots": [
        {"id": "icon", "controlType": "icon", "alignment": "center", "optional": true},
        {"id": "title", "controlType": "label", "style": "headline", "alignment": "center"},
        {"id": "description", "controlType": "label", "style": "body"},
        {
          "id": "actions",
          "type": "action-row",
          "alignment": "end",
          "slots": [
            {"id": "cancel", "controlType": "outlined-button"},
            {"id": "confirm", "controlType": "filled-button"}
          ]
        }
      ]
    },
    {
      "id": "bottom-sheet",
      "trigger": "selection-needed",
      "distribution": "bottom-card",
      "slots": [
        {"id": "drag_handle", "controlType": "drag-handle"},
        {"id": "options", "type": "radio-group"},
        {"id": "actions", "type": "action-row", "alignment": "end"}
      ]
    },
    {
      "id": "fullscreen",
      "trigger": "complex-form",
      "distribution": "fullscreen-card",
      "slots": [
        {
          "id": "topbar",
          "type": "modal-topbar",
          "slots": [
            {"id": "close", "controlType": "icon-button", "icon": "close"},
            {"id": "title", "controlType": "label"},
            {"id": "save", "controlType": "text-button"}
          ]
        },
        {"id": "form_content", "type": "form-group", "distribution": "stacked"}
      ]
    },
    {
      "id": "side-sheet",
      "trigger": "contextual-edit",
      "platformOnly": ["desktop"],
      "distribution": "side-panel",
      "panelWidth": 400,
      "slots": [
        {"id": "title_row", "type": "container", "distribution": "side-by-side"},
        {"id": "form_content", "type": "form-group"},
        {"id": "actions", "type": "action-row"}
      ]
    }
  ]
}
```
