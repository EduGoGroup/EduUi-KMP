# 1. Arquitectura General

## Filosofia

El sistema separa **estructura** de **contenido**:

- Un **Template** define COMO se ve la pantalla (zonas, slots, tipos de control).
- Una **Instance** define QUE muestra la pantalla (textos, acciones, de donde cargar datos).

Esto permite:
- Reutilizar un mismo template para multiples pantallas (ej: el template `list-basic-v1` sirve para materials, assessments, courses...).
- Cambiar textos o endpoints sin modificar el template.
- Localizar la app cambiando solo el `slot_data` de la instancia.

## Capas del Sistema

```
┌─────────────────────────────────────────────────────┐
│                    PostgreSQL                        │
│  ui_config.screen_templates   (estructura)           │
│  ui_config.screen_instances   (contenido)            │
│  ui_config.resource_screens   (mapeo recurso→screen) │
│  ui_config.screen_user_preferences (personalización) │
└─────────────────────┬───────────────────────────────┘
                      │
                      v
┌─────────────────────────────────────────────────────┐
│              API Admin (Go) - :8081                   │
│  GET /v1/screen-config/resolve/key/{screenKey}       │
│  → Combina template.definition + instance.slot_data  │
│  → Retorna CombinedScreenDTO                         │
└─────────────────────┬───────────────────────────────┘
                      │ JSON
                      v
┌─────────────────────────────────────────────────────┐
│             KMP Frontend (Kotlin)                     │
│                                                       │
│  1. RemoteScreenLoader → HTTP GET al resolve endpoint │
│  2. CachedScreenLoader → Cache L1 memoria + L2 disco │
│  3. SlotBindingResolver → Resuelve bind:"slot:xxx"    │
│  4. PlaceholderResolver → Resuelve {user.firstName}   │
│  5. PatternRouter → Selecciona renderer por pattern   │
│  6. ZoneRenderer → Renderiza zonas con distribución   │
│  7. SlotRenderer → Renderiza cada control Composable  │
└─────────────────────────────────────────────────────┘
```

## Repositorios Involucrados

| Repo | Rol | Archivos clave |
|------|-----|----------------|
| `edugo-infrastructure` | Migraciones SQL, seeds | `postgres/migrations/structure/016-019*.sql` |
| `edugo-shared` | DTOs compartidos Go | `screenconfig/dto.go`, `types.go`, `validation.go` |
| `edugo-api-administracion` | API REST, servicio de resolucion | `internal/application/service/screen_config_service.go` |
| `kmp_new/modules/dynamic-ui` | Modelos, resolvers, ViewModel KMP | `model/`, `resolver/`, `viewmodel/`, `loader/` |
| `kmp_new/kmp-screens` | Composables de renderizado | `dynamic/renderer/`, `dynamic/screens/` |

## Modelo de Datos (Relaciones)

```
screen_templates (1) ←──── (N) screen_instances
         │                          │
         │                          │ screen_key
         │                          │
         │                    resource_screens
         │                   (mapea recurso RBAC
         │                    a screen_key)
         │
         └── pattern determina qué
             PatternRenderer usar
             en el frontend
```

## Patrones Soportados

| Pattern | Frontend Renderer | Uso |
|---------|-------------------|-----|
| `login` | `LoginPatternRenderer` | Pantalla de autenticacion |
| `dashboard` | `DashboardPatternRenderer` | Panel principal con KPIs |
| `list` | `ListPatternRenderer` | Listas con busqueda y paginacion |
| `detail` | `DetailPatternRenderer` | Detalle de un recurso |
| `settings` | `SettingsPatternRenderer` | Configuracion con secciones |
| `form` | `FormPatternRenderer` | Formularios de entrada |
| `search` | (pendiente) | Pantalla de busqueda |
| `profile` | (pendiente) | Perfil de usuario |
| `modal` | (pendiente) | Dialogo modal |
| `notification` | (pendiente) | Lista de notificaciones |
| `onboarding` | (pendiente) | Flujo de onboarding |
| `empty-state` | (pendiente) | Estado vacio |
