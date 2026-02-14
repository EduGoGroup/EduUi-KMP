# Navigation - Distribucion Estructural

Este patron documenta los **mecanismos de navegacion** segun plataforma, no una pantalla especifica sino los patrones de navegacion que se aplican a cualquier pantalla.

## Mobile (Android/iOS)

Cuatro tipos de navegacion disponibles:

```
TIPO 1: Bottom Navigation
├── Contenido principal (weight=1, fillMaxSize)
└── BottomNavigationBar (parte inferior):
    ├── NavItem: icono + label "Inicio"
    ├── NavItem: "Cursos" (selected)
    ├── NavItem: "Buscar"
    └── NavItem: "Perfil"

TIPO 2: Tabs
├── TabRow (parte superior, debajo del TopAppBar):
│   ├── Tab "General" (selected)
│   ├── Tab "Actividad"
│   └── Tab "Estadisticas"
└── Contenido del tab activo (weight=1)

TIPO 3: Navigation Rail (tablets)
├── Distribucion: side-by-side
├── NavigationRail [width=auto, izquierda]:
│   ├── NavRailItem: "Inicio"
│   ├── NavRailItem: "Cursos" (selected)
│   └── ...
└── Contenido [weight=1]

TIPO 4: Navigation Drawer (hamburger)
├── Card lateral con items:
│   ├── DrawerItem "Inicio"
│   ├── DrawerItem "Mis Cursos"
│   ├── Divider
│   └── DrawerItem "Configuracion"
└── Contenido principal
```

## Desktop

Navegacion permanente visible siempre.

```
TIPO 1: Navigation Rail (comun)
├── Distribucion: side-by-side
├── NavigationRail [width=auto, izquierda, fillMaxHeight]:
│   ├── NavRailItem: icono + label
│   └── ...
└── Contenido [weight=1]

TIPO 2: Permanent Drawer (pantallas con mucho menu)
├── PermanentNavigationDrawer [width=280dp, izquierda]:
│   ├── DrawerHeader (logo/titulo)
│   ├── DrawerItem (selected)
│   ├── DrawerItem
│   ├── Divider
│   └── DrawerItem
│
└── Column [weight=1]:
    ├── TopAppBar
    └── Contenido
```

## Web

Navegacion responsiva que cambia segun viewport.

```
CASO compact (< 600dp):
├── Layout: igual que Mobile Bottom Navigation
└── BottomNav abajo

CASO medium (600-840dp):
├── Layout: Navigation Rail a la izquierda
├── Rail [width=auto]
└── Contenido [weight=1]

CASO expanded (> 840dp):
├── Layout: Drawer permanente
├── Drawer [width=280dp, izquierda]
│   ├── Items navegacion
│   └── Divider entre grupos
└── Contenido [weight=1]
```

## Reglas de Seleccion de Navegacion

| Plataforma | Ancho < 600dp | 600-840dp | > 840dp |
|------------|---------------|-----------|---------|
| Mobile | Bottom Nav | Rail | N/A |
| Desktop | N/A | Rail | Rail o Drawer |
| Web | Bottom Nav | Rail | Drawer |

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "navigation",
  "type": "structural-pattern",
  "description": "Define como se navega segun plataforma y viewport",
  "rules": [
    {
      "platform": "mobile",
      "navigationType": "bottom-navigation",
      "position": "bottom",
      "maxItems": 5,
      "itemLayout": {
        "slots": [
          {"id": "icon", "controlType": "icon"},
          {"id": "label", "controlType": "label"}
        ]
      },
      "secondaryNavigation": {
        "type": "tabs",
        "position": "top-below-appbar"
      }
    },
    {
      "platform": "desktop",
      "navigationType": "navigation-rail",
      "position": "left",
      "itemLayout": {
        "slots": [
          {"id": "icon", "controlType": "icon"},
          {"id": "label", "controlType": "label"}
        ]
      },
      "alternativeType": {
        "type": "permanent-drawer",
        "condition": "menuItems > 6",
        "width": 280
      }
    },
    {
      "platform": "web",
      "responsive": {
        "compact": {"type": "bottom-navigation"},
        "medium": {"type": "navigation-rail"},
        "expanded": {"type": "permanent-drawer", "width": 280}
      }
    }
  ]
}
```
