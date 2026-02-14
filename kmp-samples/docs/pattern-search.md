# Search - Distribucion Estructural

## Mobile (Android/iOS)

Single-column con barra de busqueda arriba, filtros horizontales, resultados abajo.

```
PANTALLA
├── Contenido (scroll vertical)
│   │
│   ├── ZONA: Barra de Busqueda
│   │   └── SearchBar (full-width, padding horizontal)
│   │
│   ├── ZONA: Filtros
│   │   └── LazyRow (scroll horizontal, sin limite):
│   │       ├── FilterChip "Todos"
│   │       ├── FilterChip "Matematicas"
│   │       ├── FilterChip "Ciencias"
│   │       └── ...
│   │
│   ├── ZONA: Busquedas Recientes
│   │   ├── Fila: titulo "Recientes" + text-button "Limpiar" (side-by-side)
│   │   ├── ListItem (leading=icono-history, headline="Matematicas")
│   │   ├── ListItem ...
│   │   └── Divider
│   │
│   └── ZONA: Resultados
│       ├── CASO: Sin resultados
│       │   └── EmptyState
│       └── CASO: Con resultados
│           ├── ElevatedCard resultado:
│           │   ├── Titulo
│           │   ├── Descripcion
│           │   └── Rating (estrellas)
│           └── ElevatedCard ...
```

## Desktop

Split: filtros/recientes a la izquierda, resultados en grid a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Panel Filtros [weight=0.3]
│   ├── Scroll: vertical
│   ├── Padding: medio
│   │
│   ├── SearchBar (full-width)
│   ├── Espaciado
│   ├── Titulo "Filtros"
│   ├── FilterChips apilados (full-width cada uno, vertical):
│   │   ├── Chip "Todos"
│   │   ├── Chip "Matematicas"
│   │   └── ...
│   ├── Divider
│   ├── Titulo "Recientes"
│   └── Lista recientes (ListItems)
│
└── ZONA: Resultados [weight=0.7]
    ├── Scroll: vertical
    ├── Padding: medio
    │
    ├── Texto "N resultados"
    └── Grid 2 columnas via chunked(2):
        ├── Fila: ElevatedCard + ElevatedCard
        └── Fila: ...
```

## Web

Card contenedora con layout responsivo.

```
PANTALLA
├── Scroll: vertical
├── Padding: medio
│
├── CASO compact:
│   └── OutlinedCard (full-width)
│       ├── SearchBar (full-width)
│       ├── Resultados apilados (1 columna)
│       └── Cards resultado stacked
│
└── CASO expanded:
    └── OutlinedCard (full-width)
        ├── SearchBar (full-width)
        └── Row (side-by-side):
            ├── Filtros [weight=0.25]
            │   ├── FilterChips vertical
            │   └── Recientes
            └── Resultados [weight=0.75]
                └── Grid 2 columnas
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "search",
  "zones": [
    {
      "id": "search_bar",
      "type": "container",
      "slots": [
        {"id": "search_input", "controlType": "search-bar", "width": "full"}
      ]
    },
    {
      "id": "filters",
      "type": "chip-group",
      "distribution": "horizontal-scroll",
      "selectable": true
    },
    {
      "id": "recent",
      "type": "simple-list",
      "title": "slot:recent_title",
      "titleAction": {"controlType": "text-button", "label": "slot:clear_label"},
      "itemLayout": {
        "type": "list-item",
        "slots": [
          {"id": "history_icon", "controlType": "icon", "position": "leading", "icon": "history"},
          {"id": "recent_text", "controlType": "label", "style": "headline"}
        ]
      }
    },
    {
      "id": "results",
      "type": "card-list",
      "emptyState": {"icon": "search", "title": "slot:no_results"},
      "itemLayout": {
        "type": "elevated-card",
        "slots": [
          {"id": "result_title", "controlType": "label", "style": "title"},
          {"id": "result_desc", "controlType": "label", "style": "body"},
          {"id": "result_rating", "controlType": "rating"}
        ]
      }
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "zones": {
        "search_bar": {"panel": "left", "weight": 0.3},
        "filters": {"panel": "left", "distribution": "stacked"},
        "recent": {"panel": "left"},
        "results": {"panel": "right", "weight": 0.7, "columns": 2}
      }
    },
    "web": {
      "zoneWrapper": "outlined-card",
      "responsive": {
        "compact": {"distribution": "stacked", "results": {"columns": 1}},
        "expanded": {
          "distribution": "side-by-side",
          "panels": {"left": 0.25, "right": 0.75},
          "results": {"columns": 2}
        }
      }
    }
  }
}
```
