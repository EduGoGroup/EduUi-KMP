# Form - Distribucion Estructural

## Mobile (Android/iOS)

Single-column con secciones separadas por dividers.

```
PANTALLA
├── Scaffold
│   ├── TopAppBar:
│   │   ├── Izquierda: boton back (icon-button)
│   │   └── Centro: titulo "Registro"
│   │
│   └── Contenido (scroll vertical, padding horizontal)
│       │
│       ├── ZONA: Seccion 1 - Datos Personales
│       │   ├── Titulo seccion (label, color=primary)
│       │   ├── Campo "Nombre completo" (text-input, full-width)
│       │   ├── Fila side-by-side:
│       │   │   ├── Campo "Fecha nacimiento" [weight=1]
│       │   │   └── Campo "Genero" [weight=1, readOnly]
│       │   └── Espaciado
│       │
│       ├── Divider (full-width)
│       │
│       ├── ZONA: Seccion 2 - Contacto
│       │   ├── Titulo seccion
│       │   ├── Campo "Correo" (email-input, full-width, con texto de soporte)
│       │   └── Campo "Telefono" (text-input, full-width)
│       │
│       ├── Divider
│       │
│       ├── ZONA: Seccion 3 - Preferencias
│       │   ├── Titulo seccion
│       │   ├── Checkbox "Recibir notificaciones"
│       │   ├── Checkbox "Acepto terminos"
│       │   └── Texto error condicional (si aplica)
│       │
│       └── ZONA: Accion
│           └── Boton "Enviar" (filled, full-width)
```

## Desktop

Dos columnas de campos, preferencias en fila horizontal.

```
PANTALLA
├── Scroll: vertical
├── Padding: grande
│
├── ZONA: Campos (side-by-side)
│   ├── Columna Izquierda [weight=1]
│   │   ├── Titulo "Datos Personales" (label, primary)
│   │   ├── Campo "Nombre completo" (full-width)
│   │   └── Fila:
│   │       ├── Campo "Fecha nacimiento" [weight=1]
│   │       └── Campo "Genero" [weight=1]
│   │
│   └── Columna Derecha [weight=1]
│       ├── Titulo "Contacto"
│       ├── Campo "Correo" (full-width)
│       ├── Campo "Telefono" (full-width)
│       └── Campo "Direccion" (full-width)
│
├── Divider
│
├── ZONA: Preferencias (side-by-side, espaciado grande)
│   ├── Checkbox "Recibir notificaciones"
│   └── Checkbox "Acepto terminos"
│
└── ZONA: Accion
    └── Boton "Enviar" (filled, full-width)
```

## Web

Cards apiladas centradas con ancho maximo.

```
PANTALLA
├── Scroll: vertical
├── Alineacion: centrado horizontal
│
└── Contenedor [max-width=600dp, centrado]
    │
    ├── Card Elevada: Seccion 1 - Datos Personales
    │   ├── Titulo seccion
    │   ├── Campo "Nombre" (full-width)
    │   ├── Fila: Fecha [w=1] + Genero [w=1]
    │   └── Padding interno
    │
    ├── Espaciado
    │
    ├── Card Elevada: Seccion 2 - Contacto
    │   ├── Titulo seccion
    │   ├── Campo "Correo" (full-width)
    │   └── Campo "Telefono" (full-width)
    │
    ├── Espaciado
    │
    ├── Card Elevada: Seccion 3 - Preferencias
    │   ├── Checkboxes apilados
    │   └── Padding interno
    │
    └── Boton "Enviar" (full-width)
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "form",
  "navigation": {
    "topBar": {"title": "slot:form_title", "leadingAction": "back"}
  },
  "zones": [
    {
      "id": "section_personal",
      "type": "form-section",
      "title": "slot:section_1_title",
      "distribution": "stacked",
      "slots": [
        {"id": "field_1", "controlType": "text-input", "width": "full"},
        {
          "id": "row_1",
          "type": "container",
          "distribution": "side-by-side",
          "slots": [
            {"id": "field_2", "controlType": "text-input", "weight": 1},
            {"id": "field_3", "controlType": "select", "weight": 1}
          ]
        }
      ]
    },
    {
      "id": "divider_1",
      "type": "divider"
    },
    {
      "id": "section_contact",
      "type": "form-section",
      "title": "slot:section_2_title",
      "distribution": "stacked",
      "slots": [
        {"id": "field_4", "controlType": "email-input", "width": "full", "supportingText": "slot:email_hint"},
        {"id": "field_5", "controlType": "text-input", "width": "full"}
      ]
    },
    {
      "id": "divider_2",
      "type": "divider"
    },
    {
      "id": "section_prefs",
      "type": "form-section",
      "title": "slot:section_3_title",
      "distribution": "stacked",
      "slots": [
        {"id": "field_6", "controlType": "checkbox"},
        {"id": "field_7", "controlType": "checkbox", "required": true}
      ]
    },
    {
      "id": "submit",
      "type": "action",
      "slots": [
        {"id": "submit_btn", "controlType": "filled-button", "width": "full"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "zones": {
        "section_personal": {"distribution": "side-by-side", "weight": 1},
        "section_contact": {"distribution": "side-by-side", "weight": 1, "mergeWith": "section_personal"},
        "section_prefs": {"distribution": "side-by-side"}
      }
    },
    "web": {
      "rootMaxWidth": 600,
      "zoneWrapper": "elevated-card"
    }
  }
}
```
