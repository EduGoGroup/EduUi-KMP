# Dynamic UI - Guia de Implementacion

Sistema de pantallas dinamicas de EduGo. Permite definir interfaces de usuario completas desde el backend, sin necesidad de compilar nuevas versiones de la app.

## Indice

| # | Documento | Descripcion |
|---|-----------|-------------|
| 1 | [Arquitectura General](01-arquitectura.md) | Vision general del sistema: capas, flujo de datos, conceptos clave |
| 2 | [Base de Datos](02-base-de-datos.md) | Tablas, relaciones y estructura JSONB |
| 3 | [API Endpoints](03-api-endpoints.md) | Endpoints REST para CRUD y resolucion de pantallas |
| 4 | [Modelos KMP (Frontend)](04-modelos-frontend.md) | Data classes Kotlin que mapean la respuesta del API |
| 5 | [Pipeline de Renderizado](05-pipeline-renderizado.md) | Desde la llamada HTTP hasta el Composable en pantalla |
| 6 | [Ejemplo Paso a Paso](06-ejemplo-paso-a-paso.md) | Crear una nueva pantalla de cero (settings como ejemplo) |

## Flujo Rapido

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│   Template   │     │   Instance   │     │  CombinedScreen  │
│  (layout)    │────>│  (contenido) │────>│    (resolve)     │
│  zones/slots │     │  slot_data   │     │  template+data   │
└──────────────┘     │  actions     │     └────────┬─────────┘
                     │  dataEndpoint│              │
                     └──────────────┘              v
                                          ┌──────────────────┐
                                          │   KMP Frontend   │
                                          ├──────────────────┤
                                          │ SlotBindingResolver
                                          │ PlaceholderResolver
                                          │ PatternRouter     │
                                          │ ZoneRenderer      │
                                          │ SlotRenderer      │
                                          └──────────────────┘
```

## Conceptos Clave

- **Template**: Define la ESTRUCTURA visual (zonas, slots, controles). Es reutilizable.
- **Instance**: Define el CONTENIDO de una pantalla especifica (textos, acciones, endpoint de datos).
- **SlotData**: Valores clave-valor que llenan los slots del template (`bind: "slot:key"`).
- **Resolve**: El endpoint que combina template + instance en un solo JSON para el frontend.
- **Pattern**: Tipo de pantalla (login, dashboard, list, detail, settings, form...) que determina el layout Composable.
