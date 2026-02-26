# Flujos de Arquitectura — EduGo KMP

> **Actualizado en febrero 2026** para reflejar la implementacion del Sprint 8:
> Sync Bundle System, Arquitectura Offline-First y DynamicToolbar.

> Documentacion detallada de los procesos internos del proyecto multiplataforma EduGo.
> Incluye diagramas secuenciales, de componentes, de estados y comparativas por plataforma.

---

## Indice de Documentos

| # | Documento | Descripcion |
|---|-----------|-------------|
| 01 | [Menu y Navegacion](./01-menu-navegacion.md) | Flujo de construccion del menu, navegacion adaptativa y routing por rol |
| 02 | [Permisos y RBAC](./02-permisos-rbac.md) | Sistema RBAC con ActiveContext, verificacion de permisos y switch de escuela |
| 03 | [Carga de UI Dinamica (SDUI)](./03-carga-sdui.md) | Pipeline completo desde backend hasta renderizado de pantallas dinamicas |
| 04 | [Acciones de la UI](./04-acciones-ui.md) | ScreenEvent → EventOrchestrator → EventResult, contratos y handlers |
| 05 | [Almacenamiento Local](./05-storage-local.md) | SafeEduGoStorage, estrategia de cache multinivel y diferencias por plataforma |
| 06 | [Conexion a Endpoints](./06-network-endpoints.md) | Ktor, interceptores, multi-API routing y resiliencia |
| 07 | [Conectado y Offline](./07-offline-conectividad.md) | Estrategias de cache, stale-while-revalidate y busqueda offline |
| 08 | [Arquitectura Global y DI](./08-arquitectura-global.md) | Capas, modulos Koin, ciclo de vida y flujo completo de datos |
| 09 | [Mejoras Propuestas](./09-mejoras-propuestas.md) | Analisis de gaps y propuestas de mejora tecnica |
| 10 | [Multiidioma (i18n)](./10-multiidioma-i18n.md) | Arquitectura hibrida: composeResources + strings del servidor via sync bundle |
| 11 | [Conceptos y Terminologia](./11-conceptos-terminologia.md) | Sistema de terminologia dinamica por institucion (concept_types + school_concepts) |
| 12 | [SDUI Remote Select](./12-sdui-remote-select.md) | Dropdown dinamico para formularios: select (fijo) y remote_select (desde endpoint) |

---

## Convenciones de los Diagramas

Los diagramas usan sintaxis **Mermaid** (compatible con GitHub, GitLab, Obsidian, VSCode con extension).

```
flowchart      → Flujo de proceso y decisiones
sequenceDiagram → Interacciones entre componentes en orden temporal
stateDiagram   → Maquinas de estados
classDiagram   → Relaciones entre clases/interfaces
graph LR/TD    → Grafos de dependencias
```

---

## Capas del Proyecto

```
┌─────────────────────────────────────────────────────────┐
│  PLATAFORMAS: Android · iOS · Desktop · WasmJS          │
├─────────────────────────────────────────────────────────┤
│  kmp-screens: Composables + Navegacion + Contratos      │
│               + DynamicToolbar + ConnectivityBanner      │
├─────────────────────────────────────────────────────────┤
│  modules/dynamic-ui: SDUI ViewModel + Orchestrator      │
│                      + DataSyncService + MutationQueue   │
│                      + SyncEngine + CacheConfig          │
├─────────────────────────────────────────────────────────┤
│  modules/auth: RBAC + JWT + Refresh (con rotacion)      │
├─────────────────────────────────────────────────────────┤
│  modules/network: Ktor + Interceptors + CircuitBreaker  │
│                   + NetworkObserver multiplataforma      │
├─────────────────────────────────────────────────────────┤
│  modules/storage: SafeStorage + multiplatform-settings  │
│                   + LocalSyncStore                       │
└─────────────────────────────────────────────────────────┘
```

---

## Sprint 8 - Resumen de cambios

### Sync Bundle System

Se centralizo la carga de datos del usuario en un unico bundle de sincronizacion. Los componentes principales son:

- **DataSyncService**: Orquesta la descarga del bundle completo (menu, permisos, pantallas, contextos) en una sola llamada al endpoint `/api/v1/sync/bundle`. Tambien soporta delta sync via `/api/v1/sync/delta` enviando hashes para recibir solo los datos que cambiaron.
- **LocalSyncStore**: Persiste el bundle en storage local para restauracion inmediata al abrir la app.
- **SyncRepository**: Capa intermedia entre el servicio remoto y el store local, maneja la logica de cuando usar datos locales vs remotos.

### Arquitectura Offline-First

Se implemento soporte completo para operacion sin conexion en todas las plataformas:

- **NetworkObserver**: Observador de conectividad multiplataforma (Android, iOS, Desktop, WasmJS) que emite un `StateFlow<Boolean>` con el estado de red en tiempo real.
- **MutationQueue**: Cola persistente de operaciones de escritura (crear, editar, eliminar) que se ejecutaron offline. Las mutaciones se encolan y se envian automaticamente cuando se recupera la conexion.
- **SyncEngine**: Motor de sincronizacion que coordina la reconciliacion de datos locales con el servidor al reconectarse. Ejecuta delta sync y procesa la cola de mutaciones pendientes.
- **ConflictResolver**: Estrategia de resolucion de conflictos cuando los datos locales y remotos divergen (por defecto: server-wins con notificacion al usuario).

### DynamicToolbar

Toolbar dinamico que se adapta automaticamente segun el patron de la pantalla SDUI:

- **LIST**: Titulo de la pantalla + boton "+" para crear (visible solo si el usuario tiene permiso CREATE).
- **FORM**: Flecha de retroceso + titulo (edit_title o page_title) + boton "Guardar" que dispara el evento custom `submit-form`.
- **DETAIL**: Flecha de retroceso + titulo de la pantalla.
- **DASHBOARD / SETTINGS**: Solo titulo.
- **LOGIN**: Sin toolbar.

### ConnectivityBanner y StaleDataIndicator

- **ConnectivityBanner**: Banner visual que aparece cuando la app pierde conexion a internet, informando al usuario que esta operando en modo offline.
- **StaleDataIndicator**: Indicador que se muestra cuando los datos en pantalla provienen de cache local y pueden estar desactualizados, junto con la fecha de la ultima sincronizacion exitosa.

### CacheConfig con TTLs por patron

Configuracion de cache diferenciada segun el tipo de pantalla:

- Las pantallas de tipo DASHBOARD tienen TTLs mas cortos (datos que cambian frecuentemente).
- Las pantallas de tipo LIST y DETAIL tienen TTLs intermedios.
- Las pantallas de tipo SETTINGS tienen TTLs mas largos (datos que cambian raramente).
- La configuracion es extensible y se puede ajustar por screen key individual.

### Rotacion de JWT Refresh Token

Se migraron los refresh tokens de formato random base64 a JWT firmados. Cada vez que se usa un refresh token, el servidor emite uno nuevo (rotacion), invalidando el anterior. Esto mejora la seguridad y permite detectar reutilizacion de tokens comprometidos. El `RefreshResponse` ahora incluye `refreshToken` y `activeContext`.

### SchoolSelectionScreen

Nueva pantalla post-login para usuarios que pertenecen a mas de una escuela. El flujo es:

1. Login exitoso retorna la lista de escuelas del usuario.
2. Si tiene mas de una escuela, se muestra `SchoolSelectionScreen` para que elija.
3. Al seleccionar una escuela, se ejecuta `fullSync()` para cargar los datos del contexto seleccionado.
4. Si tiene una sola escuela, se salta directo al dashboard con sync automatico.

### Versionado de pantallas

Endpoint de versionado que permite al cliente verificar si las definiciones de pantallas SDUI han cambiado desde la ultima sincronizacion, evitando descargas innecesarias del bundle completo.

---

## Estado de la Documentacion

| Documento | Diagramas | Plataformas | Mejoras |
|-----------|-----------|-------------|---------|
| 01 Menu   | si        | si          | si      |
| 02 RBAC   | si        | si          | si      |
| 03 SDUI   | si        | si          | si      |
| 04 Acciones | si      | si          | si      |
| 05 Storage | si       | si          | si      |
| 06 Network | si       | si          | si      |
| 07 Offline | si       | si          | si      |
| 08 Arquitectura | si  | si          | si      |
| 09 Mejoras | si       | —           | si      |
