# List - Distribucion Estructural

## Mobile (Android/iOS)

Lista agrupada con detalle por navegacion (push).

```
PANTALLA
├── Scaffold
│   ├── TopAppBar:
│   │   ├── Centro: titulo "Contactos"
│   │   └── Derecha: iconos [buscar, agregar]
│   │
│   └── Contenido (scroll vertical)
│       │
│       ├── CASO: Lista vacia
│       │   └── EmptyState (icono + titulo + descripcion + boton accion)
│       │
│       └── CASO: Con datos
│           ├── ZONA: Grupo "A"
│           │   ├── Header de grupo (fondo=surfaceVariant, padding, label bold)
│           │   ├── ListItem (avatar + nombre + email + trailing-icon)
│           │   ├── InsetDivider
│           │   └── ListItem ...
│           │
│           ├── ZONA: Grupo "B"
│           │   ├── Header de grupo
│           │   └── ListItem ...
│           │
│           └── (grupos sucesivos por letra inicial)
```

## Desktop

Master-detail split: lista a la izquierda, detalle a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Lista [weight=0.4]
│   ├── TopAppBar: titulo "Contactos"
│   ├── Scroll: vertical
│   ├── Seleccion: item seleccionado resalta (fondo=secondaryContainer)
│   │
│   ├── Grupo "A":
│   │   ├── Header
│   │   ├── ListItem (clickable, selectable)
│   │   └── Divider
│   ├── Grupo "B": ...
│   └── ...
│
├── VerticalDivider
│
└── ZONA: Detalle [weight=0.6]
    ├── Alineacion: centrado
    ├── Padding: grande
    │
    ├── Avatar grande (xxlarge)
    ├── Nombre (headline)
    ├── Espaciado
    ├── Info fila: icono-email + texto
    ├── Info fila: icono-telefono + texto
    ├── Espaciado
    └── Fila acciones:
        ├── Boton "Enviar mensaje" (filled)
        └── Boton "Editar" (outlined)
```

## Web

Grid responsivo de cards.

```
PANTALLA
├── Scroll: vertical
├── Alineacion: centrado horizontal
│
├── CASO compact (< 600dp):
│   └── Grid 1 columna
│       ├── ContactCard (outlined-card, centrado)
│       │   ├── Avatar
│       │   ├── Nombre
│       │   ├── Email
│       │   └── Telefono
│       ├── ContactCard ...
│       └── ...
│
├── CASO medium (600-840dp):
│   └── Grid 2 columnas [weight=1 cada una]
│       ├── Fila: Card + Card
│       └── ...
│
└── CASO expanded (> 840dp):
    └── Grid 3 columnas [weight=1 cada una]
        ├── Fila: Card + Card + Card
        └── ...
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "list",
  "navigation": {
    "topBar": {
      "title": "slot:list_title",
      "trailingActions": ["search", "add"]
    }
  },
  "zones": [
    {
      "id": "empty_state",
      "type": "empty-state",
      "condition": "data.isEmpty",
      "slots": [
        {"id": "empty_icon", "controlType": "icon"},
        {"id": "empty_title", "controlType": "label"},
        {"id": "empty_description", "controlType": "label"},
        {"id": "empty_action", "controlType": "filled-button"}
      ]
    },
    {
      "id": "list_content",
      "type": "grouped-list",
      "condition": "data.isNotEmpty",
      "groupBy": "firstLetter",
      "itemLayout": {
        "type": "list-item",
        "slots": [
          {"id": "avatar", "controlType": "avatar", "position": "leading"},
          {"id": "title", "controlType": "label", "style": "headline"},
          {"id": "subtitle", "controlType": "label", "style": "supporting"},
          {"id": "action_icon", "controlType": "icon", "position": "trailing"}
        ]
      },
      "separatorType": "inset-divider"
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "zones": {
        "list_content": {"weight": 0.4, "selectable": true},
        "detail_panel": {
          "weight": 0.6,
          "inject": true,
          "distribution": "stacked",
          "alignment": "center",
          "slots": [
            {"id": "detail_avatar", "controlType": "avatar", "size": "xxlarge"},
            {"id": "detail_name", "controlType": "label", "style": "headline"},
            {"id": "detail_info", "controlType": "info-rows"},
            {"id": "detail_actions", "controlType": "action-buttons", "distribution": "side-by-side"}
          ]
        }
      }
    },
    "web": {
      "listStyle": "card-grid",
      "responsiveColumns": {"compact": 1, "medium": 2, "expanded": 3},
      "itemWrapper": "outlined-card"
    }
  }
}
```
