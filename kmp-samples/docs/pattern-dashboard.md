# Dashboard - Distribucion Estructural

## Mobile (Android/iOS)

Single-column con KPIs en fila y secciones apiladas.

```
PANTALLA
├── Scaffold
│   ├── TopAppBar (SMALL):
│   │   ├── Centro: titulo "Dashboard"
│   │   └── Derecha: icono notificaciones
│   │
│   └── Contenido (scroll vertical, padding horizontal)
│       │
│       ├── ZONA: Saludo
│       │   └── Texto "Hola, [nombre]" (headline)
│       │
│       ├── ZONA: KPIs
│       │   └── Fila (side-by-side, espaciado):
│       │       ├── MetricCard [weight=1] (elevated-card)
│       │       │   ├── Label (body-small)
│       │       │   ├── Valor (headline)
│       │       │   └── Cambio % (body-small, color segun +/-)
│       │       ├── MetricCard [weight=1]
│       │       └── MetricCard [weight=1]
│       │
│       ├── Espaciado
│       │
│       ├── ZONA: Actividad Reciente
│       │   ├── Titulo seccion
│       │   ├── ListItem (icono + titulo + descripcion + timestamp)
│       │   ├── InsetDivider
│       │   └── ListItem ...
│       │
│       └── ZONA: Acciones Rapidas
│           ├── Titulo seccion
│           └── Fila:
│               ├── Boton accion 1 (outlined)
│               └── Boton accion 2 (outlined)
```

## Desktop

Tres paneles: rail de navegacion + contenido central + panel lateral.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Navegacion [width=auto]
│   └── NavigationRail (vertical, fillMaxHeight)
│       ├── NavItem: Inicio
│       ├── NavItem: Dashboard (selected)
│       ├── NavItem: Usuarios
│       └── NavItem: Config
│
├── ZONA: Contenido Principal [weight=0.6]
│   ├── Scroll: vertical
│   ├── Padding: medio
│   │
│   ├── Texto saludo
│   │
│   ├── KPIs en fila (side-by-side, 3 cards):
│   │   ├── MetricCard [weight=1]
│   │   ├── MetricCard [weight=1]
│   │   └── MetricCard [weight=1]
│   │
│   └── Placeholder para graficos (200dp height)
│
└── ZONA: Panel Lateral [weight=0.25]
    ├── Scroll: vertical
    ├── Padding: medio
    │
    ├── Seccion: Actividad Reciente
    │   └── Lista de actividades
    │
    └── Seccion: Acciones Rapidas
        ├── Boton accion 1 (full-width)
        └── Boton accion 2 (full-width)
```

## Web

Card contenedora con layout responsivo interno.

```
PANTALLA
├── Scroll: vertical
├── Padding: medio
│
├── CASO compact:
│   └── OutlinedCard (full-width)
│       ├── KPI cards apiladas (stacked, 1 columna)
│       │   └── Card: fila [label | valor | cambio]
│       ├── Divider
│       └── Actividad apilada (stacked)
│
└── CASO expanded:
    └── OutlinedCard (full-width)
        ├── KPIs en fila (3 columnas, weight=1 cada una)
        ├── Divider
        └── Dos columnas:
            ├── Actividad [weight=0.6]
            └── Acciones rapidas [weight=0.4]
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "dashboard",
  "navigation": {
    "topBar": {"title": "slot:dashboard_title", "trailingActions": ["notifications"]}
  },
  "zones": [
    {
      "id": "greeting",
      "type": "container",
      "slots": [
        {"id": "greeting_text", "controlType": "label", "style": "headline", "template": "Hola, {user_name}"}
      ]
    },
    {
      "id": "kpis",
      "type": "metric-grid",
      "distribution": "side-by-side",
      "itemLayout": {
        "type": "metric-card",
        "slots": [
          {"id": "metric_label", "controlType": "label", "style": "body-small"},
          {"id": "metric_value", "controlType": "label", "style": "headline"},
          {"id": "metric_change", "controlType": "label", "style": "body-small", "colorRule": "positive-negative"}
        ]
      }
    },
    {
      "id": "activity",
      "type": "simple-list",
      "title": "slot:activity_title",
      "itemLayout": {
        "type": "list-item",
        "slots": [
          {"id": "activity_icon", "controlType": "icon", "position": "leading"},
          {"id": "activity_title", "controlType": "label", "style": "headline"},
          {"id": "activity_desc", "controlType": "label", "style": "supporting"},
          {"id": "activity_time", "controlType": "label", "position": "trailing"}
        ]
      }
    },
    {
      "id": "quick_actions",
      "type": "action-group",
      "title": "slot:actions_title",
      "distribution": "side-by-side",
      "slots": [
        {"id": "action_1", "controlType": "outlined-button"},
        {"id": "action_2", "controlType": "outlined-button"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "injectNavigation": {"type": "navigation-rail", "position": "leading"},
      "zones": {
        "greeting": {"panel": "center"},
        "kpis": {"panel": "center"},
        "activity": {"panel": "right", "weight": 0.25},
        "quick_actions": {"panel": "right"}
      },
      "panels": {
        "center": {"weight": 0.6},
        "right": {"weight": 0.25}
      }
    },
    "web": {
      "zoneWrapper": "outlined-card",
      "responsive": {
        "compact": {"kpis": "stacked"},
        "expanded": {
          "kpis": "side-by-side",
          "activity+quick_actions": {"distribution": "side-by-side", "weights": [0.6, 0.4]}
        }
      }
    }
  }
}
```
