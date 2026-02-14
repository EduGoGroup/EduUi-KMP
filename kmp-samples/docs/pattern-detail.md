# Detail - Distribucion Estructural

## Mobile (Android/iOS)

Single-column con hero image arriba y contenido scrolleable.

```
PANTALLA
├── Scaffold
│   ├── TopAppBar:
│   │   ├── Izquierda: boton back
│   │   ├── Centro: titulo "Detalle"
│   │   └── Derecha: icono opciones (more-vert)
│   │
│   └── Contenido (scroll vertical)
│       │
│       ├── ZONA: Hero Image
│       │   └── Placeholder imagen (full-width, 200dp height, fondo=surfaceVariant)
│       │
│       └── Contenido con padding horizontal:
│           │
│           ├── ZONA: Encabezado
│           │   ├── Titulo (headline-medium)
│           │   └── Subtitulo (body-medium, color=onSurfaceVariant)
│           │
│           ├── ZONA: Tags
│           │   └── FlowRow (horizontal-wrap, espaciado):
│           │       ├── Chip "tag1"
│           │       ├── Chip "tag2"
│           │       └── ...
│           │
│           ├── ZONA: Descripcion
│           │   └── Texto largo (body-large)
│           │
│           ├── Divider
│           │
│           ├── ZONA: Detalles (pares clave-valor)
│           │   ├── Titulo seccion "Detalles"
│           │   ├── Fila: label "Duracion" + valor "8 semanas" (side-by-side)
│           │   ├── Fila: label "Nivel" + valor "Intermedio"
│           │   ├── Fila: label "Idioma" + valor "Espanol"
│           │   └── Fila: label "Certificado" + valor "Si"
│           │
│           └── ZONA: Accion
│               └── Boton "Inscribirse" (filled, full-width)
```

## Desktop

Split horizontal: imagen/media a la izquierda, contenido a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Media [weight=0.4]
│   ├── Scroll: vertical
│   │
│   ├── Hero Image (flexible height, fondo=surfaceVariant)
│   │
│   └── Tags (FlowRow, padding, espaciado)
│       ├── Chip ...
│       └── Chip ...
│
└── ZONA: Contenido [weight=0.6]
    ├── Scroll: vertical
    ├── Padding: grande
    │
    ├── Titulo (headline)
    ├── Subtitulo
    ├── Espaciado
    ├── Descripcion (body-large)
    ├── Divider
    ├── Detalles en grid 2 columnas:
    │   └── Filas via chunked(2):
    │       ├── [Duracion: 8 sem] [Nivel: Intermedio]
    │       └── [Idioma: Espanol] [Certificado: Si]
    ├── Espaciado
    └── Boton CTA (filled, full-width)
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
│       ├── Hero (full-width, 200dp)
│       ├── Titulo + subtitulo
│       ├── Tags (FlowRow)
│       ├── Descripcion
│       └── Boton CTA (full-width)
│
└── CASO expanded:
    └── OutlinedCard (full-width)
        └── Row (side-by-side, espaciado):
            ├── Media [weight=0.4]:
            │   ├── Hero
            │   └── Tags
            └── Contenido [weight=0.6]:
                ├── Titulo + subtitulo
                ├── Descripcion
                ├── Detalles (key-value rows)
                └── Boton CTA
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "detail",
  "navigation": {
    "topBar": {
      "title": "slot:detail_title",
      "leadingAction": "back",
      "trailingActions": ["more-options"]
    }
  },
  "zones": [
    {
      "id": "hero",
      "type": "media",
      "slots": [
        {"id": "hero_image", "controlType": "image", "width": "full", "height": 200}
      ]
    },
    {
      "id": "header",
      "type": "container",
      "distribution": "stacked",
      "slots": [
        {"id": "title", "controlType": "label", "style": "headline"},
        {"id": "subtitle", "controlType": "label", "style": "body", "color": "onSurfaceVariant"}
      ]
    },
    {
      "id": "tags",
      "type": "chip-group",
      "distribution": "flow-row"
    },
    {
      "id": "description",
      "type": "container",
      "slots": [
        {"id": "body", "controlType": "label", "style": "body-large"}
      ]
    },
    {"id": "divider_1", "type": "divider"},
    {
      "id": "details",
      "type": "key-value-list",
      "title": "slot:details_title",
      "itemLayout": {
        "distribution": "side-by-side",
        "slots": [
          {"id": "key", "controlType": "label", "style": "body", "color": "onSurfaceVariant"},
          {"id": "value", "controlType": "label", "style": "body", "alignment": "end"}
        ]
      }
    },
    {
      "id": "action",
      "type": "action",
      "slots": [
        {"id": "cta", "controlType": "filled-button", "width": "full"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "zones": {
        "hero": {"panel": "left", "weight": 0.4},
        "tags": {"panel": "left"},
        "header": {"panel": "right", "weight": 0.6},
        "description": {"panel": "right"},
        "details": {"panel": "right", "columns": 2},
        "action": {"panel": "right"}
      }
    },
    "web": {
      "zoneWrapper": "outlined-card",
      "responsive": {
        "compact": {"distribution": "stacked"},
        "expanded": {
          "distribution": "side-by-side",
          "panels": {"left": 0.4, "right": 0.6}
        }
      }
    }
  }
}
```
