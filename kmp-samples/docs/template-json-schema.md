# Template JSON Schema - Esquema de Plantillas de Distribucion

Este documento define el esquema JSON para describir **plantillas de distribucion** (layouts) independientes del dato.

## Concepto de Dos Capas

```
┌─────────────────────┐     ┌─────────────────────┐
│   TEMPLATE (Layout)  │     │   DATA (Campos)      │
│                     │     │                     │
│ - Zonas             │     │ - Labels            │
│ - Distribucion      │  +  │ - Valores           │  →  UI Renderizada
│ - Controles         │     │ - Placeholders      │
│ - Platform rules    │     │ - Validaciones      │
│                     │     │ - Opciones           │
└─────────────────────┘     └─────────────────────┘
```

## Schema del Template

### Raiz

```json
{
  "templateId": "string (unico)",
  "pattern": "string (login|form|list|dashboard|settings|detail|search|profile|modal|notification|onboarding|empty-state)",
  "version": "number",
  "navigation": { "...NavigationConfig" },
  "zones": [ "...Zone[]" ],
  "platformOverrides": { "...PlatformOverrides" }
}
```

### NavigationConfig

```json
{
  "topBar": {
    "title": "slot:nombre_slot",
    "leadingAction": "back | menu | none",
    "trailingActions": ["search", "add", "notifications", "more-options"]
  },
  "bottomNav": {
    "items": "slot:nav_items"
  }
}
```

### Zone

Una zona es un contenedor logico de UI. Puede contener slots (campos) u otras zonas anidadas.

```json
{
  "id": "string (unico dentro del template)",
  "type": "container | form-section | form-group | simple-list | grouped-list | card-list | metric-grid | action-group | action | divider | feedback-group | paged-container | settings-section | media | chip-group | banner | empty-state",
  "title": "string | slot:nombre",
  "distribution": "stacked | side-by-side | flow-row | horizontal-scroll | grid",
  "alignment": "start | center | end | center-both",
  "condition": "string (expresion de visibilidad, ej: 'data.isEmpty')",
  "slots": [ "...Slot[]" ],
  "itemLayout": { "...ItemLayout (para listas/grids)" },
  "separatorType": "divider | inset-divider | none"
}
```

### Slot

Un slot es una posicion donde va un control. El nombre del slot mapea al dato.

```json
{
  "id": "string (clave para mapear al dato)",
  "controlType": "text-input | email-input | password-input | number-input | textarea | checkbox | switch | radio-group | select | date-input | search-bar | label | icon | avatar | image | filled-button | outlined-button | text-button | icon-button | text-button-link | divider-with-text | drag-handle | rating | info-rows | action-buttons | list-item | list-item-navigation | metric-card | icon-circle",
  "width": "full | auto | fixed",
  "weight": "number (0-1, para layouts side-by-side)",
  "position": "leading | trailing | center",
  "style": "headline | title | body | body-small | body-large | label | supporting",
  "size": "small | medium | large | xlarge | xxlarge",
  "icon": "string (nombre del icono)",
  "color": "string (token semantico: primary, onSurfaceVariant, error)",
  "alignment": "start | center | end",
  "required": "boolean",
  "optional": "boolean",
  "keyboard": "text | email | number | phone | password",
  "background": "string (token: primaryContainer, surfaceVariant)",
  "maxLines": "number",
  "readOnly": "boolean"
}
```

### ItemLayout (para listas y grids)

Define la estructura de cada item repetido.

```json
{
  "type": "list-item | elevated-card | outlined-card | metric-card",
  "distribution": "side-by-side | stacked",
  "alignment": "center | start",
  "slots": [ "...Slot[]" ]
}
```

### PlatformOverrides

Modificaciones por plataforma. Solo se especifica lo que cambia.

```json
{
  "desktop": {
    "rootDistribution": "side-by-side | stacked",
    "injectNavigation": {
      "type": "navigation-rail | permanent-drawer",
      "position": "leading"
    },
    "zones": {
      "zone_id": {
        "panel": "left | center | right",
        "weight": "number",
        "distribution": "side-by-side | stacked",
        "background": "string",
        "columns": "number (para grids)",
        "selectable": "boolean",
        "mergeWith": "string (id de otra zona para fusionar)"
      }
    },
    "panels": {
      "left": { "weight": 0.4 },
      "center": { "weight": 0.35 },
      "right": { "weight": 0.25 }
    }
  },
  "web": {
    "rootMaxWidth": "number (dp)",
    "zoneWrapper": "outlined-card | elevated-card | none",
    "responsive": {
      "compact": {
        "distribution": "stacked",
        "columns": 1
      },
      "medium": {
        "columns": 2
      },
      "expanded": {
        "distribution": "side-by-side",
        "panels": { "left": 0.3, "right": 0.7 },
        "columns": 3
      }
    }
  }
}
```

## Tipos de Control Soportados

### Inputs (capturan dato del usuario)

| controlType | Componente DS | Descripcion |
|------------|---------------|-------------|
| `text-input` | DSOutlinedTextField | Texto libre, una linea |
| `email-input` | DSOutlinedTextField + keyboard=email | Email con teclado email |
| `password-input` | DSPasswordField | Password con toggle visibility |
| `number-input` | DSOutlinedTextField + keyboard=number | Solo numeros |
| `textarea` | DSOutlinedTextField singleLine=false | Texto multilinea |
| `date-input` | DSOutlinedTextField (+ DatePicker futuro) | Fecha |
| `search-bar` | DSSearchBar | Barra de busqueda |

### Seleccion (opciones)

| controlType | Componente DS | Descripcion |
|------------|---------------|-------------|
| `checkbox` | DSCheckbox | Opcion booleana individual |
| `switch` | DSSwitch | Toggle on/off con label |
| `radio-group` | DSRadioButton (N veces) | Seleccion unica entre opciones |
| `select` | DropdownMenu / RadioButtons | Seleccion con opciones predefinidas |

### Display (muestran informacion)

| controlType | Componente DS | Descripcion |
|------------|---------------|-------------|
| `label` | Text | Texto con estilo variable |
| `icon` | Icon | Icono Material |
| `avatar` | DSAvatar | Avatar con iniciales o imagen |
| `image` | Placeholder/Image | Imagen o hero |
| `rating` | Estrellas custom | Puntuacion |

### Acciones (botones)

| controlType | Componente DS | Descripcion |
|------------|---------------|-------------|
| `filled-button` | DSFilledButton | Boton primario |
| `outlined-button` | DSOutlinedButton | Boton secundario |
| `text-button` | DSTextButton | Boton terciario |
| `icon-button` | DSIconButton | Boton solo icono |

### Estructurales

| controlType | Componente DS | Descripcion |
|------------|---------------|-------------|
| `divider` | DSDivider | Separador full-width |
| `inset-divider` | DSInsetDivider | Separador con margen |
| `list-item` | DSListItem | Item de lista con leading/trailing |
| `list-item-navigation` | DSListItem + flecha | Item navegable |

## Ejemplo Completo: Settings para EduGo

```json
{
  "templateId": "settings-v1",
  "pattern": "settings",
  "version": 1,
  "navigation": {
    "topBar": {
      "title": "slot:page_title",
      "leadingAction": "back"
    }
  },
  "zones": [
    {
      "id": "account",
      "type": "settings-section",
      "title": "slot:section_account",
      "slots": [
        {
          "id": "user_card",
          "controlType": "list-item",
          "slots": [
            {"id": "avatar", "controlType": "avatar", "position": "leading", "size": "large"},
            {"id": "name", "controlType": "label", "style": "headline"},
            {"id": "email", "controlType": "label", "style": "supporting"}
          ]
        }
      ]
    },
    {
      "id": "appearance",
      "type": "settings-section",
      "title": "slot:section_appearance",
      "slots": [
        {"id": "theme", "controlType": "list-item-navigation"},
        {"id": "font_size", "controlType": "list-item-navigation"}
      ]
    },
    {
      "id": "notifications",
      "type": "settings-section",
      "title": "slot:section_notifications",
      "slots": [
        {"id": "push", "controlType": "switch"},
        {"id": "email_notif", "controlType": "switch"},
        {"id": "sound", "controlType": "switch"}
      ]
    },
    {
      "id": "general",
      "type": "settings-section",
      "title": "slot:section_general",
      "slots": [
        {"id": "privacy", "controlType": "list-item-navigation", "icon": "lock"},
        {"id": "language", "controlType": "list-item-navigation", "icon": "language"},
        {"id": "about", "controlType": "list-item-navigation", "icon": "info"}
      ]
    },
    {
      "id": "logout_action",
      "type": "action",
      "slots": [
        {"id": "logout", "controlType": "filled-button", "width": "full", "icon": "exit"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "extractCategories": true,
      "zones": {
        "account": {"panel": "right"},
        "appearance": {"panel": "right"},
        "notifications": {"panel": "right"},
        "general": {"panel": "right"},
        "logout_action": {"panel": "right"}
      },
      "panels": {
        "left": {"weight": 0.3, "role": "category-selector"},
        "right": {"weight": 0.7, "role": "content"}
      }
    }
  }
}
```
