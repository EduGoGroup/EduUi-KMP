# Settings - Distribucion Estructural

## Mobile (Android/iOS)

Lista de secciones con controles mixtos (toggles, navegacion, texto).

```
PANTALLA
├── Scaffold
│   ├── TopAppBar:
│   │   ├── Izquierda: boton back
│   │   └── Centro: titulo "Configuracion"
│   │
│   └── Contenido (scroll vertical)
│       │
│       ├── ZONA: Seccion "Cuenta"
│       │   ├── Label seccion (color=primary, padding izq)
│       │   ├── ListItem especial:
│       │   │   ├── Leading: avatar (large)
│       │   │   ├── Headline: nombre usuario
│       │   │   ├── Supporting: email
│       │   │   └── Trailing: icono flecha derecha
│       │   └── Divider (full)
│       │
│       ├── ZONA: Seccion "Apariencia"
│       │   ├── Label seccion
│       │   ├── ListItem: "Tema" + trailing texto "Sistema" (clickable)
│       │   ├── InsetDivider
│       │   ├── ListItem: "Tamano de texto" + trailing texto "Mediano" (clickable)
│       │   └── Divider (full)
│       │
│       ├── ZONA: Seccion "Notificaciones"
│       │   ├── Label seccion
│       │   ├── Switch: "Notificaciones push" (con padding horizontal)
│       │   ├── InsetDivider
│       │   ├── Switch: "Notificaciones email"
│       │   ├── InsetDivider
│       │   ├── Switch: "Sonido"
│       │   └── Divider (full)
│       │
│       ├── ZONA: Seccion "General"
│       │   ├── ListItem: "Privacidad" (leading=icono-lock, trailing=flecha)
│       │   ├── InsetDivider
│       │   ├── ListItem: "Idioma" (leading=icono, supporting="Espanol", trailing=flecha)
│       │   ├── InsetDivider
│       │   ├── ListItem: "Acerca de" (leading=icono-info, supporting="v1.0.0", trailing=flecha)
│       │   └── Divider (full)
│       │
│       └── ZONA: Accion
│           └── Boton "Cerrar Sesion" (filled, full-width, leading-icon=exit, padding horizontal)
```

## Desktop

Split: categorias a la izquierda, contenido dinamico a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Categorias [weight=0.3]
│   ├── Scroll: vertical
│   ├── Seleccion: categoria activa resaltada
│   │
│   ├── ListItem "Cuenta" (clickable, selectedIndex=0)
│   ├── ListItem "Apariencia" (clickable, selectedIndex=1)
│   ├── ListItem "Notificaciones" (clickable, selectedIndex=2)
│   └── ListItem "General" (clickable, selectedIndex=3)
│
├── VerticalDivider (implicito)
│
└── ZONA: Contenido [weight=0.7]
    ├── Scroll: vertical
    ├── Padding: medio
    │
    └── Contenido dinamico segun selectedIndex:
        ├── index=0: Seccion Cuenta (avatar, nombre, email, botones editar)
        ├── index=1: Seccion Apariencia (tema, tamano texto)
        ├── index=2: Seccion Notificaciones (switches)
        └── index=3: Seccion General (lista navegacion + logout)
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
│       ├── Switches apilados (stacked)
│       ├── Divider
│       └── ListItems navegacion apilados
│
└── CASO expanded:
    └── OutlinedCard (full-width)
        └── Row (side-by-side):
            ├── Categorias [weight=0.3]
            │   └── Lista clickable
            └── Contenido [weight=0.7]
                └── (igual que desktop content zone)
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "settings",
  "navigation": {
    "topBar": {"title": "slot:settings_title", "leadingAction": "back"}
  },
  "zones": [
    {
      "id": "section_account",
      "type": "settings-section",
      "title": "slot:section_account",
      "slots": [
        {
          "id": "user_profile",
          "controlType": "list-item",
          "variant": "profile",
          "slots": [
            {"id": "avatar", "controlType": "avatar", "position": "leading", "size": "large"},
            {"id": "user_name", "controlType": "label", "style": "headline"},
            {"id": "user_email", "controlType": "label", "style": "supporting"},
            {"id": "nav_arrow", "controlType": "icon", "position": "trailing", "icon": "arrow-right"}
          ]
        }
      ]
    },
    {
      "id": "section_appearance",
      "type": "settings-section",
      "title": "slot:section_appearance",
      "slots": [
        {"id": "theme", "controlType": "list-item-navigation", "trailing": "slot:theme_value"},
        {"id": "text_size", "controlType": "list-item-navigation", "trailing": "slot:text_size_value"}
      ]
    },
    {
      "id": "section_notifications",
      "type": "settings-section",
      "title": "slot:section_notifications",
      "slots": [
        {"id": "push_notif", "controlType": "switch"},
        {"id": "email_notif", "controlType": "switch"},
        {"id": "sound", "controlType": "switch"}
      ]
    },
    {
      "id": "section_general",
      "type": "settings-section",
      "title": "slot:section_general",
      "slots": [
        {"id": "privacy", "controlType": "list-item-navigation", "leadingIcon": "lock"},
        {"id": "language", "controlType": "list-item-navigation", "leadingIcon": "language", "trailing": "slot:lang_value"},
        {"id": "about", "controlType": "list-item-navigation", "leadingIcon": "info", "trailing": "slot:version"}
      ]
    },
    {
      "id": "logout",
      "type": "action",
      "slots": [
        {"id": "logout_btn", "controlType": "filled-button", "width": "full", "leadingIcon": "exit"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "extractCategories": true,
      "categoryPanel": {"weight": 0.3, "selectable": true},
      "contentPanel": {"weight": 0.7, "showsByCategory": true}
    },
    "web": {
      "zoneWrapper": "outlined-card",
      "responsive": {
        "compact": {"distribution": "stacked"},
        "expanded": {"distribution": "side-by-side", "extractCategories": true}
      }
    }
  }
}
```
