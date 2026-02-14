# Notification - Distribucion Estructural

## Mobile (Android/iOS)

Lista de notificaciones con variantes de feedback.

```
PANTALLA
├── Scaffold
│   ├── TopAppBar (SMALL):
│   │   └── Centro: titulo "Notificaciones"
│   │
│   └── Contenido (scroll vertical, padding horizontal)
│       │
│       ├── ZONA: Lista de Notificaciones
│       │   ├── ListItem notificacion:
│       │   │   ├── Leading: icono circular (fondo=primaryContainer)
│       │   │   ├── Headline: titulo notificacion
│       │   │   ├── Supporting: descripcion
│       │   │   └── Trailing: timestamp (body-small)
│       │   ├── InsetDivider
│       │   └── ListItem ...
│       │
│       ├── Divider
│       │
│       ├── ZONA: Snackbar Ejemplos
│       │   ├── Card con fondo inverseSurface:
│       │   │   └── Fila: texto + TextButton "Deshacer"
│       │   └── Card snackbar con icono error
│       │
│       ├── ZONA: Toast
│       │   └── Area placeholder (200dp, fondo=surfaceVariant)
│       │       └── Card pequena centrada (elevated, rounded):
│       │           └── Fila: icono-check + texto "Guardado"
│       │
│       └── ZONA: Banner
│           └── Card (fondo=infoContainer):
│               └── Fila:
│                   ├── Icono info
│                   ├── Texto explicativo [weight=1]
│                   └── TextButton "Ver"
```

## Desktop

Split: contenido principal a la izquierda, panel de notificaciones a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Contenido Principal [weight=0.7]
│   └── Placeholder centrado (texto "Area de contenido")
│
└── ZONA: Panel Notificaciones [weight=0.3, fondo=surfaceContainerLow]
    ├── Scroll: vertical
    ├── Padding: medio
    │
    ├── Banner info (fondo=infoContainer)
    │   └── icono + texto + accion
    │
    ├── Lista notificaciones (stacked)
    │   ├── Card notificacion
    │   └── ...
    │
    ├── Snackbar stack
    │   ├── Snackbar con accion
    │   └── Snackbar error
    │
    └── Toast preview
        └── Card flotante pequena
```

## Web

Card responsiva.

```
PANTALLA
├── Scroll: vertical
├── Padding: medio
│
├── CASO compact:
│   └── OutlinedCard (full-width)
│       ├── Notificaciones apiladas
│       ├── Divider
│       ├── Snackbar full-width
│       └── Toast centrado
│
└── CASO expanded:
    └── OutlinedCard (full-width)
        └── Row (side-by-side):
            ├── Notificaciones [weight=0.6]
            └── Snackbars + Toasts [weight=0.4, max-width=400dp, alineado derecha]
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "notification",
  "navigation": {
    "topBar": {"title": "slot:notif_title"}
  },
  "zones": [
    {
      "id": "notification_list",
      "type": "simple-list",
      "itemLayout": {
        "type": "list-item",
        "slots": [
          {"id": "icon", "controlType": "icon-circle", "position": "leading", "background": "primaryContainer"},
          {"id": "title", "controlType": "label", "style": "headline"},
          {"id": "body", "controlType": "label", "style": "supporting"},
          {"id": "time", "controlType": "label", "position": "trailing", "style": "body-small"}
        ]
      }
    },
    {
      "id": "snackbars",
      "type": "feedback-group",
      "variants": [
        {"type": "snackbar", "slots": [{"id": "text"}, {"id": "action", "controlType": "text-button"}]},
        {"type": "snackbar-error", "slots": [{"id": "icon"}, {"id": "text"}]}
      ]
    },
    {
      "id": "toast",
      "type": "feedback-overlay",
      "slots": [
        {"id": "icon", "controlType": "icon"},
        {"id": "text", "controlType": "label"}
      ]
    },
    {
      "id": "banner",
      "type": "banner",
      "background": "infoContainer",
      "slots": [
        {"id": "icon", "controlType": "icon"},
        {"id": "text", "controlType": "label"},
        {"id": "action", "controlType": "text-button"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "zones": {
        "notification_list": {"panel": "right", "weight": 0.3, "background": "surfaceContainerLow"},
        "snackbars": {"panel": "right"},
        "toast": {"panel": "right"},
        "banner": {"panel": "right"}
      },
      "mainContent": {"panel": "left", "weight": 0.7}
    }
  }
}
```
