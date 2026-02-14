# Profile - Distribucion Estructural

## Mobile (Android/iOS)

Single-column centrado con avatar grande, stats en fila, acciones.

```
PANTALLA
├── Scaffold
│   ├── TopAppBar (SMALL):
│   │   ├── Izquierda: boton back
│   │   └── Centro: titulo "Perfil"
│   │
│   └── Contenido (scroll vertical, padding horizontal, centrado)
│       │
│       ├── ZONA: Identidad
│       │   ├── Avatar (xxlarge, centrado)
│       │   ├── Nombre (headline, centrado)
│       │   ├── Email (body, color=onSurfaceVariant, centrado)
│       │   └── Bio (body, centrado)
│       │
│       ├── Espaciado
│       │
│       ├── ZONA: Estadisticas
│       │   └── Fila (side-by-side, espaciado):
│       │       ├── StatCard [weight=1] (outlined-card, centrado):
│       │       │   ├── Valor (headline, bold)
│       │       │   └── Label (body-small)
│       │       ├── StatCard [weight=1]
│       │       ├── StatCard [weight=1]
│       │       └── StatCard [weight=1]
│       │
│       ├── Espaciado
│       │
│       ├── ZONA: Acciones
│       │   ├── Boton "Editar perfil" (filled, full-width)
│       │   └── Boton "Compartir" (outlined, full-width)
│       │
│       ├── Divider
│       │
│       └── ZONA: Atajos
│           ├── ListItem "Mis cursos" (trailing=flecha)
│           ├── ListItem "Mis certificados"
│           └── ListItem "Configuracion"
```

## Desktop

Split: identidad a la izquierda, stats y contenido a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Panel Identidad [weight=0.35, fondo=surfaceContainerLow]
│   ├── Alineacion: centrado horizontal
│   ├── Padding: grande
│   │
│   ├── Avatar (xxlarge)
│   ├── Nombre (headline, centrado)
│   ├── Email (centrado)
│   ├── Bio (centrado)
│   ├── Espaciado
│   ├── Boton "Editar perfil" (filled, full-width)
│   └── Boton "Compartir" (outlined, full-width)
│
└── ZONA: Contenido [weight=0.65]
    ├── Scroll: vertical
    ├── Padding: grande
    │
    ├── Stats grid 2x2:
    │   ├── Fila: StatCard [w=1] + StatCard [w=1]
    │   └── Fila: StatCard [w=1] + StatCard [w=1]
    │
    ├── Seccion Actividad
    │   └── Lista de actividades recientes
    │
    └── Seccion Atajos
        └── ListItems navegacion
```

## Web

Card responsiva.

```
PANTALLA
├── Scroll: vertical
├── Padding: medio
│
├── CASO compact:
│   └── OutlinedCard (full-width, centrado)
│       ├── Avatar (xlarge)
│       ├── Nombre, email, bio (centrados)
│       ├── Stats en fila (4 cards side-by-side)
│       ├── Boton editar (full-width)
│       └── Boton compartir (full-width)
│
└── CASO expanded:
    └── OutlinedCard (full-width)
        └── Row (side-by-side):
            ├── Identidad [weight=0.4, centrado]:
            │   ├── Avatar (xxlarge)
            │   ├── Nombre, email, bio
            │   └── Botones acciones
            └── Contenido [weight=0.6]:
                ├── Stats grid 2x2
                └── Atajos navegacion
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "profile",
  "navigation": {
    "topBar": {"title": "slot:profile_title", "leadingAction": "back"}
  },
  "zones": [
    {
      "id": "identity",
      "type": "container",
      "alignment": "center",
      "slots": [
        {"id": "avatar", "controlType": "avatar", "size": "xxlarge"},
        {"id": "name", "controlType": "label", "style": "headline"},
        {"id": "email", "controlType": "label", "style": "body", "color": "onSurfaceVariant"},
        {"id": "bio", "controlType": "label", "style": "body"}
      ]
    },
    {
      "id": "stats",
      "type": "metric-grid",
      "distribution": "side-by-side",
      "itemLayout": {
        "type": "outlined-card",
        "alignment": "center",
        "slots": [
          {"id": "stat_value", "controlType": "label", "style": "headline"},
          {"id": "stat_label", "controlType": "label", "style": "body-small"}
        ]
      }
    },
    {
      "id": "actions",
      "type": "action-group",
      "distribution": "stacked",
      "slots": [
        {"id": "edit", "controlType": "filled-button", "width": "full"},
        {"id": "share", "controlType": "outlined-button", "width": "full"}
      ]
    },
    {
      "id": "shortcuts",
      "type": "simple-list",
      "itemLayout": {
        "type": "list-item",
        "slots": [
          {"id": "shortcut_label", "controlType": "label", "style": "headline"},
          {"id": "shortcut_arrow", "controlType": "icon", "position": "trailing", "icon": "arrow-right"}
        ]
      }
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "zones": {
        "identity": {"panel": "left", "weight": 0.35, "background": "surfaceContainerLow"},
        "actions": {"panel": "left"},
        "stats": {"panel": "right", "weight": 0.65, "columns": 2},
        "shortcuts": {"panel": "right"}
      }
    },
    "web": {
      "zoneWrapper": "outlined-card",
      "responsive": {
        "compact": {"distribution": "stacked"},
        "expanded": {"distribution": "side-by-side", "panels": {"left": 0.4, "right": 0.6}}
      }
    }
  }
}
```
