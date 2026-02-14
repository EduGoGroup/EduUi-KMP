# Fase 1: Fundamentos + Pantallas Principales

## Objetivo

Establecer el pipeline de extremo a extremo para renderizado de UI dirigido por backend y entregar las 5 pantallas principales que forman la navegacion central de la app EduGo.

## Alcance

### Backend
1. **Infraestructura**: Nuevo schema PostgreSQL `ui_config` con archivos de migracion
2. **Shared**: Nuevos DTOs y tipos para definiciones de pantalla en `edugo-shared`
3. **API Admin**: Endpoints CRUD para gestionar templates e instancias de pantalla
4. **API Mobile**: Endpoints de solo lectura para servir definiciones de pantalla combinadas a los clientes

### Frontend (KMP)
5. **Modulo de UI Dinamica**: Nuevo `modules/dynamic-ui` con modelos, loader, renderers
6. **Integracion de Pantallas**: Conectar las pantallas principales a la configuracion dirigida por backend

### Pantallas Principales (Entregables)
7. **Login** - Ya existe; mejorar con configuracion de campos dirigida por backend
8. **Dashboard** - Nuevo; dashboard de profesor/estudiante con KPIs y actividad
9. **Lista de Materiales** - Nuevo; primera pantalla de lista conectada a datos reales
10. **Detalle de Material** - Nuevo; primera pantalla de detalle con contenido y acciones
11. **Configuracion** - Ya existe; mejorar con secciones dirigidas por backend

## Grafo de Dependencias

```
[1] Infraestructura (schema + migraciones)
    ↓
[2] Shared (DTOs)    →    [3] API Admin (CRUD)    →    [4] API Mobile (endpoints de lectura)
    ↓                                                          ↓
[5] Modulo dynamic-ui KMP ←──────────────────────────────────────┘
    ↓
[6] Pantallas Principales (Login, Dashboard, Lista de Materiales, Detalle de Material, Configuracion)
```

## Sub-Documentos

| Documento | Enfoque |
|-----------|---------|
| [backend-infrastructure.md](./backend-infrastructure.md) | Schema PostgreSQL, migraciones, Docker |
| [backend-shared.md](./backend-shared.md) | DTOs compartidos, validacion, tipos |
| [backend-api-admin.md](./backend-api-admin.md) | Endpoints de API para gestion de pantallas |
| [backend-api-mobile.md](./backend-api-mobile.md) | Endpoints de API para servir pantallas |
| [frontend-kmp.md](./frontend-kmp.md) | Arquitectura del modulo de UI dinamica |
| [main-screens.md](./main-screens.md) | Especificacion de cada pantalla principal |

## Criterios de Aceptacion

- [ ] El admin puede crear/editar templates de pantalla via API
- [ ] El admin puede crear/editar instancias de pantalla y vincularlas a recursos
- [ ] El frontend puede cargar la configuracion de pantalla desde api-mobile
- [ ] El frontend renderiza la pantalla de Login con configuracion de campos dirigida por backend
- [ ] El frontend renderiza el Dashboard con datos de KPI reales
- [ ] El frontend renderiza la Lista de Materiales con paginacion desde api-mobile
- [ ] El frontend renderiza el Detalle de Material con contenido completo
- [ ] El frontend renderiza Configuracion con secciones dirigidas por backend
- [ ] Todas las pantallas funcionan en Android, Desktop y WasmJS
- [ ] Las configuraciones de pantalla se almacenan en cache local para rendimiento
- [ ] Los renderers de pattern pasan las pruebas unitarias
