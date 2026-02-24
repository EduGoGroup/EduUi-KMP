# Informe de Arquitectura: Auth, Menu y Dynamic UI

## Evaluacion de la distribucion actual y propuestas de mejora

**Fecha**: 2026-02-20
**Autor**: Analisis automatizado sobre el codigo fuente
**Alcance**: edugo-api-administracion, edugo-api-mobile, edugo-shared, KMP frontend, infraestructura

---

## 1. ESTADO ACTUAL: MAPA DE RESPONSABILIDADES

### 1.1 Donde vive cada funcionalidad hoy

```
                    api-admin (Go, :8081)          api-mobile (Go, :9091)
                    ========================       ========================
AUTH LOGIN          POST /v1/auth/login            --- (no tiene) ---
                    POST /v1/auth/refresh
                    POST /v1/auth/logout
                    POST /v1/auth/switch-context

AUTH VERIFY         POST /v1/auth/verify           Validacion LOCAL de JWT
                    (servicio interno)              (misma secret, issuer)
                                                   + fallback remoto a admin

RBAC MGMT           GET/POST roles, permissions    --- (no tiene) ---
                    GET/POST user-roles
                    Grant/Revoke roles

MENU (Admin)        GET /v1/menu                   --- (no tiene) ---
                    GET /v1/menu/full

MENU (Mobile)       --- (no tiene) ---             GET /v1/screens/navigation
                                                   (construye bottomNav/drawer)

SCREEN CONFIG       POST/PUT/DELETE templates      --- (no tiene) ---
(CRUD)              POST/PUT/DELETE instances
                    POST/DELETE resource-screens

SCREEN SERVING      GET /v1/screen-config/         GET /v1/screens/:screenKey
(LECTURA)           resolve/key/:key               GET /v1/screens/resource/:rk
                                                   PUT /v1/screens/:key/preferences

DOMAIN DATA         Schools, Units, Users,         Materials, Assessments,
                    Memberships, Guardians,        Progress, Stats,
                    Subjects                       Summaries (AI)
```

### 1.2 Que comparten ambas APIs

| Recurso compartido | Mecanismo |
|---|---|
| JWT Secret + Issuer | Variable de entorno `AUTH_JWT_SECRET`, issuer: "edugo-central" |
| Tipos RBAC (UserContext, Claims) | `edugo-shared/auth` (Go module) |
| Middleware JWT + Permisos | `edugo-shared/middleware/gin` |
| Tablas PostgreSQL | Misma BD: `auth.resources`, `ui_config.*` |
| Modelos de error | `edugo-shared/common/errors` |
| Screen config types | `edugo-shared/screenconfig` |
| Logging + Config | `edugo-shared/logger`, `edugo-shared/config` |

### 1.3 Como consume el frontend KMP (Android, iOS nativo via KMP, Desktop)

```
KMP App
  |
  |-- Auth Module ---------> api-admin:8081
  |   (login, refresh,        /v1/auth/*
  |    logout, verify)
  |
  |-- Dynamic UI Module
  |   |
  |   |-- ScreenLoader ----> api-admin:8081           <-- PROBLEMA
  |   |   (screen defs)       /v1/screen-config/resolve/key/{key}
  |   |
  |   |-- NavigationLoader -> api-admin:8081           <-- PROBLEMA
  |   |   (menu/nav)           /v1/screens/navigation
  |   |
  |   |-- DataLoader -------> DUAL ROUTING:
  |       - "admin:/v1/..." -> api-admin:8081
  |       - "/v1/..."       -> api-mobile:9091 (default)
```

**Nota**: Tanto screen loading como navigation en KMP apuntan a admin
porque `RemoteScreenLoader` recibe `adminApiBaseUrl` como unico baseUrl.

### 1.4 Como consume el frontend Apple (iOS nativo Swift)

```
Apple App
  |
  |-- Auth (ServiceContainer) -> api-admin:8081
  |   (login, logout)            /v1/auth/*
  |
  |-- Screen Loading ----------> api-mobile:9091       <-- CORRECTO
  |   (screen defs)               /v1/screens/{key}?platform=ios
  |   + ETag caching              If-None-Match / 304
  |   + LRU memory (20 max)
  |
  |-- Data Loading ------------> DUAL ROUTING:
  |   - "admin:/v1/..."  ------> api-admin:8081
  |   - "/v1/..."        ------> api-mobile:9091 (default)
  |
  |-- Domain Data -------------> api-mobile:9091
      /v1/materials, /v1/progress, /v1/stats
```

### 1.5 Discrepancia entre frontends

| Aspecto | KMP | Apple |
|---|---|---|
| **Screen loading** | `admin:8081/v1/screen-config/resolve/key/{key}` | `mobile:9091/v1/screens/{key}` |
| **Navigation** | `admin:8081/v1/screens/navigation` | No implementado aun |
| **Endpoint mobile** | Endpoint admin basico (sin ETag, sin platform overrides, sin user prefs) | Endpoint mobile completo (ETag, platform overrides, user prefs) |
| **Platform param** | Opcional, pasado como parametro | Hardcoded `platform=ios` |
| **HTTP caching** | Solo cache local (storage + LRU) | ETag + 304 Not Modified + LRU |
| **handlerKey en modelo** | NO existe en ScreenDefinition | SI existe en ScreenDefinition |

**Conclusion**: Apple usa el endpoint correcto (mobile API, mas completo). KMP usa el endpoint equivocado (admin API, mas basico). Hay que alinear KMP con Apple.

---

## 2. ANALISIS: PROBLEMAS Y OBSERVACIONES

### 2.1 Duplicacion de logica de menu

**Problema**: La construccion de menus basada en permisos existe en AMBAS APIs.

- **api-admin** (`menu_service.go`): Carga `auth.resources`, filtra por permisos del usuario, construye arbol jerarquico, mapea `resource_screens`.
- **api-mobile** (`screen_service.go`): Hace EXACTAMENTE lo mismo - carga `auth.resources`, filtra por permisos, construye arbol, pero ademas distribuye en `bottomNav` vs `drawerItems` segun plataforma.

**Impacto**: Si cambia la logica de filtrado de permisos o la estructura del menu, hay que actualizar en DOS lugares. Ambos leen las mismas tablas (`auth.resources`, `ui_config.resource_screens`).

**Severidad**: Media. Ambas implementaciones son relativamente simples (~200 lineas cada una), pero es una fuente de bugs por inconsistencia.

### 2.2 Doble resolucion de pantallas

**Problema**: Screen resolution existe en ambas APIs.

- **api-admin**: `GET /v1/screen-config/resolve/key/:key` - JOIN template + instance, devuelve CombinedScreenDTO.
- **api-mobile**: `GET /v1/screens/:screenKey` - Misma logica de JOIN + resolucion de slots + platform overrides + user preferences + ETag caching.

**Impacto**: La version mobile es MAS completa (tiene platform overrides, user preferences, ETag). La version admin es mas basica. El frontend KMP actualmente usa la version ADMIN para cargar screens.

**Severidad**: Baja-Media. La duplicacion existe pero cada version sirve un proposito distinto (admin = preview/config, mobile = runtime optimizado).

### 2.3 Auth centralizado pero con acoplamiento

**Observacion positiva**: El login esta correctamente centralizado en api-admin. Api-mobile valida tokens localmente con la misma secret (patron correcto para microservicios).

**Problema menor**: Api-mobile tiene un `AuthClient` con circuit breaker para validar remotamente contra api-admin. Esta capacidad esta **deshabilitada por defecto** (`remote_enabled: false`), pero el codigo y la complejidad estan ahi.

**Severidad**: Baja. El acoplamiento es minimo gracias al JWT compartido.

### 2.4 Redis: configurado pero NO utilizado

**Estado actual**:
- api-admin: `RedisConfig` existe en config, pero cache esta **deshabilitado** (`TokenValidation.Enabled: false`).
- api-mobile: Usa cache **in-memory** con `sync.RWMutex` (screen cache 1h, token cache 60s).
- Docker Compose: Redis es un servicio **opcional** (profile: `with-redis`).

**Impacto**: No hay problema real de Redis hoy. El caching in-memory funciona bien para una sola instancia de cada API. Se volveria problema al escalar horizontalmente (multiples instancias = caches inconsistentes).

### 2.5 Tablas compartidas sin ownership claro

**Problema**: Ambas APIs leen/escriben en las mismas tablas de PostgreSQL:

```
auth.resources          -> admin ESCRIBE, mobile LEE
ui_config.screen_*      -> admin ESCRIBE, mobile LEE + ESCRIBE (preferences)
auth.users              -> admin ESCRIBE, mobile LEE
auth.user_roles         -> admin ESCRIBE, mobile LEE (indirectamente via JWT)
```

**Impacto**: Esto no es un anti-patron per se (shared database en microservicios es comun en etapas tempranas), pero dificulta la independencia de deploy y crea riesgo de migraciones conflictivas.

**Severidad**: Baja ahora, Alta en el futuro si se escala el equipo.

---

## 3. OPCIONES EVALUADAS

### OPCION A: Mantener como esta (Status Quo)

**Descripcion**: No cambiar nada. Aceptar la duplicacion como costo del diseno actual.

**A favor**:
- Cero costo de migracion
- El sistema funciona y es estable
- La duplicacion es manejable (2 APIs, equipo pequeno)
- Cada API es independiente para deployar
- El shared library (`edugo-shared`) ya mitiga mucha duplicacion de tipos/logica

**En contra**:
- Menu y screen resolution duplicados divergeran con el tiempo
- No es reutilizable para otros proyectos sin copiar ambas APIs
- Al escalar el equipo, la coordinacion entre APIs aumenta

**Esfuerzo**: 0 (nada que hacer)

**Veredicto**: Viable a corto plazo (6-12 meses). No es sostenible si se planea reutilizar o escalar significativamente.

---

### OPCION B: Extraer un "Platform API" (Auth + Menu + Dynamic UI)

**Descripcion**: Crear una tercera API (`edugo-api-platform`) que centralice:
- Auth (login, refresh, logout, switch-context, verify)
- RBAC (roles, permisos, user-roles)
- Menu/Navigation (construccion filtrada por permisos)
- Screen Config (CRUD + resolution + serving)
- Resources (catalogo de recursos del sistema)

**Arquitectura resultante**:

```
                    api-platform (NUEVO)
                    =====================
                    /v1/auth/*
                    /v1/menu/*
                    /v1/screens/*
                    /v1/screen-config/*
                    /v1/roles/*
                    /v1/permissions/*
                    /v1/resources/*

                    api-admin (REDUCIDO)            api-mobile (REDUCIDO)
                    ====================            ====================
                    /v1/schools/*                   /v1/materials/*
                    /v1/units/*                     /v1/assessments/*
                    /v1/memberships/*               /v1/progress/*
                    /v1/users/* (CRUD basico)       /v1/stats/*
                    /v1/guardians/*                 /v1/users/me/*
                    /v1/subjects/*
```

**A favor**:
- Eliminacion total de duplicacion de menu y screen resolution
- Un unico punto para auth, RBAC y UI config
- **Altamente reutilizable**: cualquier proyecto puede usar `api-platform` para auth + menus dinamicos
- Separacion clara: platform = cross-cutting, admin = gestion escolar, mobile = runtime educativo
- Facilita agregar nuevos clientes (web admin, otra app) sin duplicar auth/menu

**En contra**:
- **Esfuerzo significativo**: migrar endpoints, actualizar frontend, CI/CD, infra
- Agrega un servicio mas al ecosistema (mas complejidad operativa)
- Las APIs existentes quedan dependientes de platform para auth
- Potencial punto unico de fallo (si platform cae, nada funciona)

**Esfuerzo**: Alto (3-5 sprints estimados)

**Mitigacion del SPOF**: api-mobile ya sabe validar JWT localmente. Si platform cae, las requests con token valido siguen funcionando. Solo login/refresh se verian afectados.

---

### OPCION C: Extraer solo Auth/RBAC como "Identity Service"

**Descripcion**: Crear un microservicio dedicado solo a identidad y acceso. Menu y Dynamic UI quedan donde estan.

**Alcance**:
- Login, refresh, logout, switch-context
- Roles, permissions, user-roles
- Token verify (inter-service)
- NO incluye menu, NO incluye screen config

**A favor**:
- Menor esfuerzo que Opcion B
- Auth/IAM es el candidato mas obvio para servicio independiente
- Patron muy probado en la industria (Keycloak, Auth0, etc.)
- Reutilizable para otros proyectos EduGo

**En contra**:
- No resuelve la duplicacion de menu y screen resolution
- Menor impacto arquitectonico
- Aun queda la pregunta de donde vive el menu

**Esfuerzo**: Medio (2-3 sprints)

---

### OPCION D: Consolidar en api-admin y hacer api-mobile "thin"

**Descripcion**: Mover TODA la logica de menu y screen resolution a api-admin. Api-mobile se vuelve un servicio "thin" que solo sirve datos de dominio (materials, assessments, progress) y proxea a admin para screens.

**A favor**:
- Elimina duplicacion sin crear un nuevo servicio
- api-admin ya tiene toda la logica de RBAC y screen config
- Menor complejidad operativa

**En contra**:
- api-admin se vuelve aun MAS pesado (ya tiene mucho)
- El frontend tendria que hablar con admin para TODO (auth + menu + screens + gestion)
- api-mobile pierde la optimizacion de platform overrides y user preferences
- No mejora la reutilizacion

**Esfuerzo**: Medio (2-3 sprints)

**Veredicto**: Concentra el problema en vez de resolverlo.

---

### OPCION E: Refactorizar `edugo-shared` para eliminar duplicacion sin nuevo servicio

**Descripcion**: En vez de crear un nuevo servicio, mover la logica duplicada (menu building, screen resolution) al shared library. Ambas APIs llaman la misma funcion de `edugo-shared`.

**Arquitectura**:

```
edugo-shared/menu/
  - BuildUserMenu(permissions, resources) -> MenuDTO
  - BuildNavigation(permissions, platform) -> NavigationDTO

edugo-shared/screenconfig/
  - ResolveScreen(key, platform, userPrefs) -> CombinedScreenDTO
  - ApplyPlatformOverrides(...)
  - MergeUserPreferences(...)
```

**A favor**:
- **Minimo esfuerzo** (mover logica que ya existe a shared)
- Cero nuevos servicios, cero nueva infra
- Garantiza consistencia: misma funcion en ambas APIs
- Mantiene la ventaja de que cada API puede servir localmente (sin llamadas inter-service)
- Incrementalmente adoptable

**En contra**:
- No resuelve el problema de endpoints duplicados (aun hay 2 URLs para lo mismo)
- No mejora la reutilizacion para otros proyectos (siguen necesitando montar api-admin o api-mobile)
- Actualizaciones del shared requieren redeploy de AMBAS APIs

**Esfuerzo**: Bajo (1 sprint)

---

## 4. TABLA COMPARATIVA

| Criterio | A (Status Quo) | B (Platform API) | C (Identity Svc) | D (Consolidar) | E (Shared Lib) |
|---|---|---|---|---|---|
| Elimina duplicacion menu | No | Si | No | Parcial | Parcial* |
| Elimina duplicacion screens | No | Si | No | Si | Parcial* |
| Reutilizable otros proyectos | No | **Si** | Parcial | No | No |
| Esfuerzo | Nulo | Alto | Medio | Medio | **Bajo** |
| Complejidad operativa | Igual | +1 servicio | +1 servicio | Igual | Igual |
| Riesgo SPOF | N/A | Mitigable | Mitigable | N/A | N/A |
| Escala con equipo | Mal | **Bien** | Bien | Mal | Medio |
| Alineado con crecimiento | No | **Si** | Parcial | No | Parcial |

*Parcial = la logica esta unificada en la libreria, pero los endpoints siguen duplicados.

---

## 5. RECOMENDACION

### Corto plazo (ahora): OPCION E - Unificar logica en `edugo-shared`

**Por que**: Es el paso mas pragmatico. Cuesta 1 sprint, elimina el riesgo de divergencia de logica, y no agrega complejidad operativa. El equipo es pequeno y no necesita la ceremonia de un nuevo servicio HOY.

**Acciones concretas**:
1. Mover `BuildUserMenu()` y `BuildNavigation()` a `edugo-shared/menu/`
2. Mover `ResolveScreen()` con platform overrides y user preferences a `edugo-shared/screenconfig/`
3. Ambas APIs importan y usan las funciones del shared
4. Tests unitarios en el shared garantizan consistencia

### Mediano plazo (cuando se necesite reutilizar): OPCION B - Platform API

**Trigger para migrar**: Cuando ocurra CUALQUIERA de estos:
- Se necesite reutilizar auth/menu/dynamic-ui en otro producto del ecosistema EduGo
- El equipo crezca a 2+ equipos trabajando en paralelo en las APIs
- Se necesite escalar horizontalmente y el cache in-memory no sea suficiente (Redis se vuelve necesario de verdad)
- Se planee un web admin panel que necesite los mismos endpoints de menu/screens

**Por que B y no C**: Auth sin menu no tiene sentido separarlos cuando el menu DEPENDE de los permisos de auth. Son una unidad cohesiva. Separar auth solo crea un servicio incompleto que no se puede usar sin el otro.

---

## 6. SOBRE LA PREOCUPACION DE REDIS

**Estado**: Redis NO se esta usando realmente. El caching actual es in-memory.

**Cuando si necesitaras Redis**:
- Multiples instancias de api-mobile (horizontal scaling) -> caches in-memory divergen
- Blacklist de tokens (logout) que necesite ser compartida entre instancias
- Session storage si se implementa switch-context frecuente
- Rate limiting distribuido

**Recomendacion**: No agregar Redis hasta que se escale horizontalmente. El cache in-memory actual es correcto y eficiente para una sola instancia.

---

## 7. SOBRE LA REUTILIZACION PARA OTROS PROYECTOS

**Que es potencialmente reutilizable hoy** (con esfuerzo minimo):
- `edugo-shared/auth` - JWT + RBAC generico (ya es un Go module independiente)
- `edugo-shared/middleware/gin` - middleware de auth para cualquier API Gin
- `edugo-shared/screenconfig` - tipos de screen config

**Que seria reutilizable con Opcion B**:
- API completa de auth + RBAC + menu dinamico + screen config
- Cualquier app (no solo EduGo) podria conectarse
- Solo necesitaria configurar sus propios resources, roles y screen templates

**Veredicto**: Si la reutilizacion es una prioridad estrategica, la Opcion B es el camino correcto. Si es solo una posibilidad futura, la Opcion E te prepara sin sobreinvertir.

---

## 8. DIAGRAMA: ESTADO ACTUAL vs PROPUESTA B

### Estado Actual
```
┌─────────────┐     ┌──────────────────────────────────┐
│  KMP App    │────>│  api-admin (:8081)               │
│  (Android,  │     │  - Auth (login/refresh/logout)    │
│   iOS,      │     │  - RBAC (roles/perms/user-roles)  │
│   Desktop)  │     │  - Menu (admin panel)              │
│             │     │  - Screen Config (CRUD + resolve)  │
│             │     │  - Schools/Units/Users/Subjects    │
│             │     │  - Guardians/Memberships           │
│             │────>├──────────────────────────────────┤
│             │     │  api-mobile (:9091)               │
│             │     │  - Screen serving (resolve + prefs)│
│             │     │  - Navigation (bottomNav/drawer)   │
│             │     │  - Materials/Assessments/Progress   │
│             │     │  - Stats/Summaries                  │
└─────────────┘     └──────────────────────────────────┘
```

### Propuesta B (Mediano Plazo)
```
┌─────────────┐     ┌──────────────────────────────────┐
│  KMP App    │────>│  api-platform (NUEVO)            │
│  (Android,  │     │  - Auth (login/refresh/logout)    │
│   iOS,      │     │  - RBAC (roles/perms/user-roles)  │
│   Desktop)  │     │  - Menu + Navigation               │
│             │     │  - Screen Config (CRUD + resolve)  │
│ Otro App    │────>│  - Resources                       │
│ (futuro)    │     │  ** REUTILIZABLE **                │
│             │     ├──────────────────────────────────┤
│             │────>│  api-admin (:8081) REDUCIDO      │
│             │     │  - Schools/Units/Users CRUD        │
│             │     │  - Guardians/Memberships/Subjects  │
│             │     ├──────────────────────────────────┤
│             │────>│  api-mobile (:9091) REDUCIDO     │
│             │     │  - Materials/Assessments/Progress   │
│             │     │  - Stats/Summaries                  │
└─────────────┘     └──────────────────────────────────┘
```

---

## 9. CONCLUSION

**Tu intuicion es correcta**: hay responsabilidades (auth, menu, dynamic UI) que estan acopladas entre las dos APIs y que conceptualmente son una unidad. Sin embargo, **el momento de separarlas no es necesariamente ahora**.

**La buena noticia**: La arquitectura actual NO esta mal disenada. La separacion admin/mobile tiene sentido como management-plane vs runtime-plane. La duplicacion es real pero contenida, y el shared library ya hace un buen trabajo unificando tipos y logica comun.

**La estrategia pragmatica**:
1. **Ahora**: Unificar la logica duplicada en `edugo-shared` (Opcion E). Costo bajo, beneficio inmediato.
2. **Cuando haya trigger real**: Extraer el Platform API (Opcion B). No antes.
3. **No hacer**: Consolidar todo en api-admin (Opcion D) ni crear un Identity Service solo (Opcion C).

**Puedes sacarte esto de la cabeza**: El sistema esta bien para su etapa actual. La duplicacion tiene un plan de mitigacion claro (Opcion E ahora, Opcion B despues). No necesitas actuar urgentemente.

---

## 10. HALLAZGO ADICIONAL: FRONTEND APPLE vs KMP

Al analizar el frontend Apple (`apple_new`), se descubrio que **ya usa el endpoint correcto** de api-mobile para cargar screens (`/v1/screens/{key}`), mientras que KMP sigue usando el endpoint de api-admin (`/v1/screen-config/resolve/key/{key}`).

Esto significa que:
1. api-mobile ya sirve screens con todas las features (ETag, platform overrides, user preferences)
2. Apple ya esta aprovechando estas features
3. KMP esta usando un endpoint inferior innecesariamente
4. Una vez que KMP migre, el endpoint de screen resolution en api-admin puede ser evaluado para remocion

---

## 11. PLAN DE MIGRACION: KMP Screen Loading (admin -> mobile)

### Objetivo

Migrar el `ScreenLoader` de KMP para que use `api-mobile` (`/v1/screens/{key}`) en vez de `api-admin` (`/v1/screen-config/resolve/key/{key}`), alineandolo con el frontend Apple.

### Beneficios de la migracion

1. **Consistencia**: Ambos frontends (KMP y Apple) usaran el mismo endpoint
2. **Features superiores**: Platform overrides, user preferences, ETag/304 del endpoint mobile
3. **Preparacion para remocion**: Permite eliminar el endpoint duplicado de admin
4. **Menos carga en admin**: Las requests de screen serving se mueven a mobile (que esta optimizado para eso)

### Pre-requisitos

- Verificar que api-mobile esta corriendo y sirviendo `/v1/screens/{key}` correctamente
- Verificar que el JSON de respuesta de `/v1/screens/{key}` es compatible con `ScreenDefinition` de KMP
- Verificar que api-mobile sirve `/v1/screens/navigation` (ya confirmado)

### Archivos a modificar

```
1. modules/dynamic-ui/src/commonMain/.../model/ScreenDefinition.kt     [MODELO]
2. modules/dynamic-ui/src/commonMain/.../loader/RemoteScreenLoader.kt  [ENDPOINT]
3. modules/di/src/commonMain/.../module/DynamicUiModule.kt             [DI: baseUrl]
4. modules/dynamic-ui/src/commonTest/.../loader/CachedScreenLoaderTest.kt [TESTS]
```

### Paso 1: Agregar `handlerKey` a ScreenDefinition

**Archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/model/ScreenDefinition.kt`

**Razon**: El endpoint de mobile devuelve `handlerKey` (campo opcional). Apple lo tiene, KMP no. Gracias a `ignoreUnknownKeys = true` en el JSON parser no falla, pero lo necesitamos para que los handlers funcionen correctamente con screens que declaran `handlerKey`.

**Cambio**:
```kotlin
@Serializable
data class ScreenDefinition(
    val screenId: String,
    val screenKey: String,
    val screenName: String,
    val pattern: ScreenPattern,
    val version: Int,
    val template: ScreenTemplate,
    val slotData: JsonObject? = null,
    val dataEndpoint: String? = null,
    val dataConfig: DataConfig? = null,
    val actions: List<ActionDefinition> = emptyList(),
    val handlerKey: String? = null,          // <-- AGREGAR
    val userPreferences: JsonObject? = null,
    val updatedAt: String
)
```

**Riesgo**: Ninguno. Campo nullable con default null. No rompe la serializacion existente.

### Paso 2: Cambiar endpoint en RemoteScreenLoader

**Archivo**: `modules/dynamic-ui/src/commonMain/kotlin/com/edugo/kmp/dynamicui/loader/RemoteScreenLoader.kt`

**Cambio**:
```kotlin
// ANTES (admin endpoint):
url = "$baseUrl/v1/screen-config/resolve/key/$screenKey"

// DESPUES (mobile endpoint, igual que Apple):
url = "$baseUrl/v1/screens/$screenKey"
```

**Nota**: El navigation endpoint (`/v1/screens/navigation`) ya usa el patron correcto de mobile. No necesita cambios, pero ahora apuntara a mobile (que es donde realmente existe este endpoint).

**Riesgo**: Bajo. El formato de respuesta es el mismo `CombinedScreenDTO` -> `ScreenDefinition`.

### Paso 3: Cambiar baseUrl en DI Module

**Archivo**: `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/DynamicUiModule.kt`

**Cambio**:
```kotlin
// ANTES:
single<ScreenLoader> {
    val appConfig = get<AppConfig>()
    // screen-config endpoints están en API Admin
    CachedScreenLoader(
        remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl),
        storage = get<SafeEduGoStorage>()
    )
}

// DESPUES:
single<ScreenLoader> {
    val appConfig = get<AppConfig>()
    // screen endpoints en API Mobile (alineado con frontend Apple)
    CachedScreenLoader(
        remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.mobileApiBaseUrl),
        storage = get<SafeEduGoStorage>()
    )
}
```

**Riesgo**: Este es el cambio critico. Todas las llamadas de screen loading y navigation ahora iran a mobile API.

### Paso 4: Actualizar tests

**Archivo**: `modules/dynamic-ui/src/commonTest/kotlin/com/edugo/kmp/dynamicui/loader/CachedScreenLoaderTest.kt`

Los tests existentes usan un `ScreenLoader` fake (mock), por lo que **no necesitan cambios** en CachedScreenLoaderTest. El cambio de URL es transparente porque CachedScreenLoader depende de la interface `ScreenLoader`, no de la URL.

**Si existen tests de integracion** que verifican la URL real, habria que actualizar la URL esperada.

### Paso 5 (Opcional/Futuro): Agregar soporte ETag

**Descripcion**: Apple usa ETag + If-None-Match para reducir transferencia de datos. KMP podria beneficiarse de esto.

**Archivos**: `RemoteScreenLoader.kt`, `CachedScreenLoader.kt`, posiblemente `EduGoHttpClient.kt`

**Cambios necesarios**:
- Guardar el ETag de la respuesta junto al cache entry
- Enviar `If-None-Match: {etag}` en requests subsecuentes
- Manejar respuesta 304 (devolver cache sin deserializar)

**Prioridad**: Baja. El cache por tiempo (1 hora) ya funciona bien. ETag es una optimizacion de red, no un requisito.

**Recomendacion**: Hacerlo en un sprint separado, no mezclarlo con la migracion de endpoint.

### Orden de ejecucion

```
Paso 1 → Paso 2 → Paso 3 → Paso 4 → Compilar → Tests → Verificar manualmente
```

**Tiempo estimado**: 30-60 minutos de cambios de codigo. + Tiempo de QA manual.

### Validacion post-migracion

1. [ ] `./gradlew :modules:dynamic-ui:desktopTest` pasa
2. [ ] La app carga la pantalla de login correctamente
3. [ ] La navegacion (bottomNav/drawer) se construye bien
4. [ ] Los screens de dashboard, materiales, etc. cargan datos
5. [ ] Las acciones de los screens (submit form, navigate, etc.) funcionan

### Post-migracion: Evaluar remocion del endpoint admin

Una vez que KMP este migrado y funcionando con mobile API:
- `GET /v1/screen-config/resolve/key/:key` en api-admin **podria removerse**
- Verificar que ningun otro cliente lo usa (web admin panel, scripts, etc.)
- Si api-admin necesita preview de screens para su panel de administracion, puede mantener el endpoint solo para uso interno del admin panel
