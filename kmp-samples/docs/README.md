# Patrones de UI - Guia de Distribucion Estructural

Este directorio contiene la documentacion de **distribucion de layout** (plantillas estructurales) para cada patron de UI, separado del dato concreto que se muestra.

## Concepto

La idea es separar dos capas:

| Capa | Que define | Ejemplo |
|------|-----------|---------|
| **Template (Plantilla)** | Donde va cada zona, que tipo de control, como se distribuyen | "Seccion titulo arriba, luego grupo de campos en 2 columnas, boton abajo" |
| **Data (Dato)** | Que campos concretos mostrar, sus valores, labels | "Nombre: text, Email: email, Tema oscuro: switch" |

Al combinar ambas en runtime, se genera la UI dinamicamente.

## Indice de Patrones

| # | Patron | Archivo | Descripcion |
|---|--------|---------|-------------|
| 1 | [Login](./pattern-login.md) | `login/` | Autenticacion con brand, formulario, social auth |
| 2 | [Form](./pattern-form.md) | `form/` | Formulario de registro/edicion con secciones |
| 3 | [List](./pattern-list.md) | `list/` | Lista de items con agrupacion y detalle |
| 4 | [Dashboard](./pattern-dashboard.md) | `dashboard/` | Panel con KPIs, actividad reciente, acciones rapidas |
| 5 | [Settings](./pattern-settings.md) | `settings/` | Configuracion con categorias, toggles, navegacion |
| 6 | [Detail](./pattern-detail.md) | `detail/` | Vista de detalle de un item con hero image |
| 7 | [Search](./pattern-search.md) | `search/` | Busqueda con filtros, recientes, resultados |
| 8 | [Profile](./pattern-profile.md) | `profile/` | Perfil de usuario con stats y acciones |
| 9 | [Navigation](./pattern-navigation.md) | `navigation/` | Patrones de navegacion por plataforma |
| 10 | [Modal](./pattern-modal.md) | `modal/` | Dialogos, bottom sheets, side sheets |
| 11 | [Notification](./pattern-notification.md) | `notification/` | Notificaciones, snackbars, toasts, banners |
| 12 | [Onboarding](./pattern-onboarding.md) | `onboarding/` | Pantallas de bienvenida paso a paso |
| 13 | [Empty State](./pattern-empty-state.md) | `empty/` | Estados vacios y feedback |

## Documentos Complementarios

| Documento | Descripcion |
|-----------|-------------|
| [Template JSON Schema](./template-json-schema.md) | Esquema JSON para definir plantillas de distribucion |
| [Estrategia de Persistencia](./persistence-strategy.md) | Como guardar plantilla + dato en base de datos |
| [Action Binding Strategy](./action-binding-strategy.md) | Como enlazar eventos y acciones (save, validate, delete, navigate) |
| [Roadmap de Implementacion](./implementation-roadmap.md) | Plan de trabajo en 6 fases para implementar el sistema completo |

## Convenciones de Lectura

Cada documento de patron usa esta estructura:

```
ZONA: [nombre]
├── Posicion: [donde va espacialmente]
├── Tipo: [container / control / group]
├── Distribucion: [column / row / grid]
└── Contenido:
    ├── Elemento 1
    └── Elemento 2
```

Las distribuciones se describen de forma **abstracta** (no pixeles), usando:
- `full-width`: ocupa todo el ancho
- `weight(N)`: proporcion relativa (ej: 0.4 = 40%)
- `centered(max-width)`: centrado con ancho maximo
- `stacked`: elementos apilados verticalmente
- `side-by-side`: elementos lado a lado horizontalmente
