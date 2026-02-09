# 04 - Catalogo de Modulos Propuestos

## Resumen

| # | Modulo | Tier | Origen | Plataformas | Requiere Compose |
|---|--------|------|--------|-------------|------------------|
| 1 | `kmp-foundation` | 0 | Existente (refactor) | Todas | No |
| 2 | `kmp-logger` | 1 | Existente (refactor) | Todas | No |
| 3 | `kmp-core` | 1 | Existente (refactor) | Todas | No |
| 4 | `kmp-validation` | 1 | Existente (expandir) | Todas | No |
| 5 | `kmp-network` | 2 | Existente (refactor) | Todas | No |
| 6 | `kmp-storage` | 2 | Existente (refactor) | Todas | No |
| 7 | `kmp-persistence` | 2 | Nuevo | Todas | No |
| 8 | `kmp-auth` | 3 | Existente (refactor) | Todas | No |
| 9 | `kmp-cqrs` | 3 | Nuevo (inspirado Swift) | Todas | No |
| 10 | `kmp-state` | 3 | Nuevo (inspirado Swift) | Todas | No |
| 11 | `kmp-designsystem` | 4 | Nuevo | Mobile+Desktop+WASM | Si |
| 12 | `kmp-components` | 4 | Nuevo | Mobile+Desktop+WASM | Si |
| 13 | `kmp-navigation` | 4 | Nuevo | Mobile+Desktop+WASM | Si |

---

## TIER 0 - Foundation

### 1. kmp-foundation

**Proposito:** Tipos base, errores estructurados, contratos fundamentales. Sin NINGUNA dependencia de framework externo (solo stdlib + kotlinx).

**Origen:** Extraido de `test-module/core/` + `test-module/data/models/base/` + `test-module/config/`

**Contenido:**

```
kmp-foundation/src/commonMain/kotlin/com/edugo/kmp/foundation/
├── error/
│   ├── AppError.kt                 # ← De test-module/core/AppError.kt
│   ├── ErrorCode.kt                # ← De test-module/core/ErrorCode.kt
│   ├── DomainError.kt              # Nuevo: errores de dominio (inspirado Swift)
│   └── ErrorExtensions.kt          # ← De test-module/core/ErrorExtensions.kt
├── result/
│   ├── Result.kt                   # ← De test-module/core/Result.kt
│   ├── ResultExtensions.kt         # ← De test-module/extensions/ResultExtensions.kt
│   └── ResultCombinators.kt        # ← De test-module/extensions/ResultCombinators.kt
├── entity/
│   ├── EntityBase.kt               # ← De test-module/data/models/base/EntityBase.kt
│   ├── ValidatableModel.kt         # ← De test-module/data/models/base/
│   ├── AuditableModel.kt           # ← De test-module/data/models/base/
│   ├── SoftDeletable.kt            # ← De test-module/data/models/base/
│   └── Identifiable.kt             # Nuevo: interface minima (solo id)
├── pagination/
│   ├── PagedResult.kt              # ← De test-module/data/models/pagination/
│   └── PaginationExtensions.kt     # ← De test-module/data/models/pagination/
├── serialization/
│   ├── JsonConfig.kt               # ← De test-module/config/JsonConfig.kt
│   ├── SerializationExtensions.kt  # ← De test-module/core/serialization/
│   └── ThrowableSerializer.kt      # ← De test-module/core/serialization/
└── mapper/
    └── DomainMapper.kt             # ← De test-module/mapper/
```

**Dependencias:**
```kotlin
// Solo kotlinx
implementation(libs.kotlinx.coroutines.core)
implementation(libs.kotlinx.serialization.json)
implementation(libs.kotlinx.datetime)
```

**Platform-specific:** Ninguno. 100% commonMain.

**Tests a migrar:** ResultTest, AppErrorTest, ErrorCodeTest, SerializationExtensionsTest, EntityBaseTest, PaginationTest

---

## TIER 1 - Core

### 2. kmp-logger

**Proposito:** Sistema de logging multiplataforma con niveles, categorias y adaptadores por plataforma.

**Origen:** Extraido de `test-module/platform/` (Logger) + inspirado en Swift `EduLogger`

**Contenido:**

```
kmp-logger/src/commonMain/kotlin/com/edugo/kmp/logger/
├── api/
│   ├── Logger.kt                   # Interface principal
│   ├── LogLevel.kt                 # TRACE, DEBUG, INFO, WARNING, ERROR
│   ├── LogCategory.kt              # Categorias tipadas
│   └── LoggerFactory.kt            # Factory para crear loggers
├── impl/
│   ├── DefaultLogger.kt            # Implementacion base
│   └── TaggedLogger.kt             # Logger con tag fijo
└── config/
    └── LoggerConfiguration.kt      # Configuracion global

// Platform-specific:
androidMain/ → LogcatAdapter.kt (android.util.Log)
iosMain/     → OSLogAdapter.kt (NSLog / os_log)
desktopMain/ → Slf4jAdapter.kt o PrintAdapter.kt
wasmJsMain/  → ConsoleAdapter.kt (console.log/warn/error)
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
// Opcional: Kermit como backend (o implementacion propia)
```

**Por que separado de core:** El logger es cross-cutting. Todo modulo puede necesitarlo sin traer el resto de core.

---

### 3. kmp-core

**Proposito:** Utilidades generales, helpers, extension functions compartidas. El "toolbox" general.

**Origen:** Extraido de `test-module/extensions/` + `test-module/data/models/helpers/` + `test-module/platform/`

**Contenido:**

```
kmp-core/src/commonMain/kotlin/com/edugo/kmp/core/
├── extensions/
│   ├── StringExtensions.kt         # Helpers de strings
│   ├── CollectionExtensions.kt     # ← De test-module/extensions/
│   ├── DateExtensions.kt           # Helpers de fechas
│   └── FlowExtensions.kt           # Helpers de Flow
├── model/
│   ├── ModelPatch.kt               # ← De test-module/data/models/helpers/
│   ├── ModelMerge.kt               # ← De test-module/data/models/helpers/
│   └── Patchable.kt                # ← De test-module/data/models/helpers/
├── platform/
│   ├── Platform.kt                 # expect/actual platform info
│   └── Dispatchers.kt              # expect/actual dispatchers
└── concurrency/
    └── Mutex.kt                    # Helpers de concurrencia
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation(libs.kotlinx.coroutines.core)
```

---

### 4. kmp-validation

**Proposito:** Sistema de validacion extensible con API booleana y Result-based.

**Origen:** Extraido de `test-module/validation/` + expandido

**Contenido:**

```
kmp-validation/src/commonMain/kotlin/com/edugo/kmp/validation/
├── api/
│   ├── Validator.kt                # Interface base: (T) -> ValidationResult
│   ├── ValidationResult.kt         # Resultado tipado
│   └── ValidationRule.kt           # Regla reutilizable
├── rules/
│   ├── StringRules.kt              # Email, URL, UUID, pattern, length
│   ├── NumberRules.kt              # Range, min, max, positive
│   ├── CollectionRules.kt          # NotEmpty, size, unique
│   └── DateRules.kt                # Past, future, range
├── composite/
│   ├── AccumulativeValidation.kt   # ← De test-module/validation/
│   ├── CompositeValidator.kt       # Combinar validadores
│   └── ConditionalValidator.kt     # Validacion condicional
├── extensions/
│   ├── StringValidationExt.kt      # ← De test-module/validation/ (isValidEmail, etc.)
│   └── ResultValidationExt.kt      # ← validateEmail(), etc.
└── dsl/
    └── ValidationDsl.kt            # DSL fluido: validate { field("email") { isNotEmpty(); isEmail() } }
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
```

---

## TIER 2 - Infrastructure

### 5. kmp-network

**Proposito:** Cliente HTTP multiplataforma basado en Ktor, con interceptores pluggables y retry automatico. **SIN conocimiento de auth.**

**Origen:** Extraido de `test-module/network/` con desacoplamiento de auth

**Contenido:**

```
kmp-network/src/commonMain/kotlin/com/edugo/kmp/network/
├── api/
│   ├── HttpClient.kt               # Interface principal (wrapper Ktor)
│   ├── HttpRequestConfig.kt        # ← De test-module/network/
│   ├── HttpResponse.kt             # Response wrapper
│   └── NetworkError.kt             # Errores de red tipados
├── interceptor/
│   ├── Interceptor.kt              # Interface base
│   ├── InterceptorChain.kt         # ← De test-module/network/
│   ├── HeaderInterceptor.kt        # ← De test-module/network/interceptor/
│   └── LoggingInterceptor.kt       # ← De test-module/network/interceptor/
├── retry/
│   ├── RetryPolicy.kt              # ← De test-module/network/retry/
│   ├── RetryInterceptor.kt         # ← De test-module/network/retry/
│   └── BackoffStrategy.kt          # ← De test-module/network/retry/
├── builder/
│   └── HttpClientBuilder.kt        # Builder pattern para config
└── serialization/
    └── NetworkSerializer.kt        # JSON serialization para requests

// Platform-specific engines:
androidMain/ → OkHttpEngine.kt
iosMain/     → DarwinEngine.kt
desktopMain/ → CIOEngine.kt
wasmJsMain/  → WasmFetchEngine.kt (Fetch API via WASM)
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:logger:$version")  // Opcional
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
```

**CLAVE:** El `AuthInterceptor` NO esta aqui. El modulo `kmp-auth` crea su propio interceptor e lo inyecta al cliente.

---

### 6. kmp-storage

**Proposito:** Almacenamiento key-value multiplataforma con soporte tipado y reactivo.

**Origen:** Extraido de `test-module/storage/`

**Contenido:**

```
kmp-storage/src/commonMain/kotlin/com/edugo/kmp/storage/
├── api/
│   ├── Storage.kt                  # Interface principal
│   ├── TypedStorage.kt             # Generics con serializers
│   └── ReactiveStorage.kt          # Flow-based (StateFlow)
├── impl/
│   ├── DefaultStorage.kt           # ← De test-module/storage/EduGoStorage.kt
│   ├── AsyncStorage.kt             # ← De test-module/storage/AsyncEduGoStorage.kt
│   └── FlowStorage.kt              # ← De test-module/storage/StateFlowStorage.kt
├── serialization/
│   └── StorageSerializer.kt        # ← De test-module/storage/ serialization ext
└── encryption/
    └── EncryptedStorage.kt         # Nuevo: wrapper con encriptacion

// Platform-specific:
androidMain/ → SharedPreferencesStorage.kt o DataStoreStorage.kt
iosMain/     → NSUserDefaultsStorage.kt
desktopMain/ → JavaPreferencesStorage.kt
wasmJsMain/  → LocalStorageAdapter.kt (window.localStorage)
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation(libs.multiplatform.settings)       // O implementacion propia
```

---

### 7. kmp-persistence

**Proposito:** Persistencia de datos estructurados (base de datos local). Equivalente a SwiftData/Room.

**Origen:** Nuevo (no existia en el proyecto KMP actual). Inspirado en Swift `EduPersistence`.

**Contenido:**

```
kmp-persistence/src/commonMain/kotlin/com/edugo/kmp/persistence/
├── api/
│   ├── Repository.kt               # Interface base de repositorio
│   ├── CrudRepository.kt           # CRUD generico
│   └── QueryBuilder.kt             # Builder para queries
├── config/
│   └── DatabaseConfig.kt           # Configuracion de BD
├── migration/
│   └── Migration.kt                # Sistema de migraciones
└── cache/
    ├── CachePolicy.kt              # Politicas de cache
    └── InMemoryCache.kt            # Cache en memoria
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:core:$version")
// SQLDelight o Room KMP (cuando este estable)
```

**Nota:** Este modulo puede empezar simple (in-memory + cache) y agregar SQLDelight/Room cuando se necesite.

---

## TIER 3 - Domain

### 8. kmp-auth

**Proposito:** Autenticacion, gestion de tokens JWT, refresh automatico. Usa `kmp-network` y `kmp-storage` como dependencias.

**Origen:** Extraido de `test-module/auth/` + `test-module/roles/` (parte generica)

**Contenido:**

```
kmp-auth/src/commonMain/kotlin/com/edugo/kmp/auth/
├── api/
│   ├── AuthService.kt              # ← De test-module/auth/service/
│   ├── AuthState.kt                # ← De test-module/auth/service/
│   └── Credentials.kt              # ← De test-module/auth/model/
├── jwt/
│   ├── JwtParser.kt                # ← De test-module/auth/jwt/
│   ├── JwtValidator.kt             # ← De test-module/auth/jwt/
│   ├── JwtClaims.kt                # ← De test-module/auth/jwt/
│   └── JwtConfig.kt                # ← De test-module/auth/jwt/
├── token/
│   ├── TokenManager.kt             # ← De test-module/auth/token/TokenRefreshManager.kt
│   ├── TokenConfig.kt              # ← De test-module/auth/token/
│   └── RefreshStrategy.kt          # ← De test-module/auth/token/
├── interceptor/
│   └── AuthInterceptor.kt          # Implementa network.Interceptor
├── roles/
│   ├── Role.kt                     # Interface generica de rol
│   ├── Permission.kt               # Interface generica de permiso
│   ├── RoleProvider.kt             # Proveedor de roles (pluggable)
│   └── PermissionChecker.kt        # ← De test-module/roles/checker/
└── repository/
    ├── AuthRepository.kt           # ← De test-module/auth/repository/
    └── TokenRepository.kt          # Storage de tokens
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:network:$version")
implementation("com.edugo.kmp:storage:$version")
```

**CLAVE:** Los roles/permisos ESPECIFICOS de EduGo (ADMIN, TEACHER, STUDENT, etc.) NO van aqui. Van en el proyecto EduGo que consume este modulo. Aqui solo van las interfaces genericas.

---

### 9. kmp-cqrs

**Proposito:** Patron Command Query Responsibility Segregation. Inspirado directamente en el modulo Swift `CQRS`.

**Origen:** Nuevo. Inspirado en Swift `Packages/Domain/Sources/CQRS/`

**Contenido:**

```
kmp-cqrs/src/commonMain/kotlin/com/edugo/kmp/cqrs/
├── api/
│   ├── Command.kt                  # interface Command<R> { suspend fun validate() }
│   ├── Query.kt                    # interface Query<R>
│   ├── CommandHandler.kt           # interface CommandHandler<C, R> { suspend fun handle(c): Result<R> }
│   ├── QueryHandler.kt             # interface QueryHandler<Q, R> { suspend fun handle(q): Result<R> }
│   └── UseCase.kt                  # interface UseCase<I, O> { suspend fun execute(input: I): Result<O> }
├── result/
│   └── CommandResult.kt            # Wrapper con metadata (timestamp, correlationId)
├── mediator/
│   ├── Mediator.kt                 # Dispatch commands/queries a handlers
│   └── MediatorRegistry.kt         # Registro de handlers
├── events/
│   ├── DomainEvent.kt              # Base event
│   ├── EventBus.kt                 # Publicacion/suscripcion
│   └── EventHandler.kt             # Handler de eventos
└── middleware/
    ├── Middleware.kt                # Pipeline de middlewares
    ├── LoggingMiddleware.kt         # Log de commands/queries
    └── ValidationMiddleware.kt      # Validacion automatica
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:core:$version")
implementation(libs.kotlinx.coroutines.core)
```

---

### 10. kmp-state

**Proposito:** State management avanzado con state machines, operators funcionales y persistencia de estado. Inspirado en Swift `StateManagement`.

**Origen:** Nuevo. Inspirado en Swift `Packages/Domain/Sources/StateManagement/`

**Contenido:**

```
kmp-state/src/commonMain/kotlin/com/edugo/kmp/state/
├── api/
│   ├── StatePublisher.kt           # Wrapper sobre MutableStateFlow con operators
│   ├── StateStream.kt              # Read-only state stream
│   └── AsyncState.kt               # Loading/Success/Error state wrapper
├── operators/
│   ├── MapOperator.kt              # .map { }
│   ├── FilterOperator.kt           # .filter { }
│   ├── ScanOperator.kt             # .scan(initial) { acc, value -> }
│   ├── CombineOperator.kt          # .combine(other) { a, b -> }
│   ├── DebounceOperator.kt         # .debounce(duration)
│   └── DistinctOperator.kt         # .distinctUntilChanged()
├── machine/
│   ├── StateMachine.kt             # Maquina de estados tipada
│   ├── StateTransition.kt          # Transicion: (State, Event) -> State
│   └── SideEffect.kt               # Efectos secundarios en transiciones
└── persistence/
    └── StatePersistence.kt          # Persistir estado en storage
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:core:$version")
implementation(libs.kotlinx.coroutines.core)
```

---

## TIER 4 - Presentation (Compose Multiplatform)

### 11. kmp-designsystem

**Proposito:** Design tokens, temas, tipografia, colores, espaciado. Base visual de todas las apps.

**Origen:** Nuevo. Inspirado en Swift `DesignSystemSDK`.

**Contenido:**

```
kmp-designsystem/src/commonMain/kotlin/com/edugo/kmp/designsystem/
├── theme/
│   ├── EduGoTheme.kt               # MaterialTheme wrapper
│   ├── Colors.kt                   # Paleta de colores (light/dark)
│   ├── Typography.kt               # Tipografia (Material Design 3)
│   ├── Spacing.kt                  # Sistema de espaciado
│   ├── Shapes.kt                   # Formas (rounded corners, etc.)
│   └── Elevation.kt                # Sombras y elevaciones
├── tokens/
│   ├── DesignTokens.kt             # Tokens centralizados
│   └── Dimensions.kt               # Dimensiones responsive
└── accessibility/
    └── AccessibilityHelpers.kt      # Content descriptions, semantics
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation(compose.material3)
implementation(compose.runtime)
```

---

### 12. kmp-components

**Proposito:** Componentes UI reutilizables basados en Compose + Material Design 3.

**Origen:** Nuevo. Inspirado en Swift `UIComponentsSDK` + `FormsSDK`.

**Contenido:**

```
kmp-components/src/commonMain/kotlin/com/edugo/kmp/components/
├── input/
│   ├── TextField.kt                # Text input con validacion
│   ├── PasswordField.kt            # Password con toggle visibility
│   ├── SearchField.kt              # Barra de busqueda
│   └── DatePicker.kt               # Selector de fecha
├── buttons/
│   ├── PrimaryButton.kt
│   ├── SecondaryButton.kt
│   ├── IconButton.kt
│   └── LoadingButton.kt            # Boton con estado loading
├── lists/
│   ├── LazyList.kt                 # Lista con paginacion
│   ├── ListItem.kt                 # Item estandar
│   └── SwipeToAction.kt            # Swipe to delete/archive
├── feedback/
│   ├── Snackbar.kt
│   ├── Dialog.kt
│   ├── BottomSheet.kt
│   └── EmptyState.kt               # Estado vacio con ilustracion
├── loading/
│   ├── Skeleton.kt                 # Skeleton loading
│   ├── ShimmerEffect.kt
│   └── ProgressIndicator.kt
├── cards/
│   ├── ContentCard.kt
│   └── InfoCard.kt
└── forms/
    ├── Form.kt                     # Container de formulario
    ├── FormField.kt                # Campo con label + error
    └── FormState.kt                # Estado del formulario
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:designsystem:$version")
implementation("com.edugo.kmp:validation:$version")  // Para validacion de forms
implementation(compose.material3)
```

---

### 13. kmp-navigation

**Proposito:** Sistema de navegacion multiplataforma con deep linking.

**Origen:** Nuevo. Inspirado en Swift Navigation + Compose Navigation.

**Contenido:**

```
kmp-navigation/src/commonMain/kotlin/com/edugo/kmp/navigation/
├── api/
│   ├── Navigator.kt                # Interface principal
│   ├── Route.kt                    # Definicion de rutas tipadas
│   ├── NavGraph.kt                 # Grafo de navegacion
│   └── DeepLink.kt                 # Deep linking
├── impl/
│   ├── ComposeNavigator.kt         # Implementacion Compose
│   └── NavHostContainer.kt         # Container principal
├── transitions/
│   ├── SlideTransition.kt
│   ├── FadeTransition.kt
│   └── SharedElementTransition.kt
└── backstack/
    ├── BackStack.kt                # Pila de navegacion
    └── BackHandler.kt              # Manejo del boton back
```

**Dependencias:**
```kotlin
implementation("com.edugo.kmp:foundation:$version")
implementation("com.edugo.kmp:core:$version")
implementation(compose.runtime)
implementation(compose.navigation)  // O voyager/decompose
```

---

## Modulos Futuros (No Prioritarios)

| Modulo | Tier | Descripcion | Cuando |
|--------|------|-------------|--------|
| `kmp-analytics` | 2 | Tracking de eventos multiplataforma | Cuando haya app en produccion |
| `kmp-notifications` | 2 | Push notifications (FCM/APNs) | Cuando se necesite |
| `kmp-media` | 2 | Camara, galeria, archivos | Cuando se necesite |
| `kmp-sync` | 3 | Sincronizacion offline-first | Cuando se necesite |
| `kmp-testing` | - | Test utilities compartidas (fakes, builders) | Inmediato (util para todos) |
| `kmp-forms-advanced` | 4 | Formularios dinamicos con schema | Cuando se necesite |
| `kmp-charts` | 4 | Graficas y visualizacion de datos | Cuando se necesite |

---

## Equivalencia Swift ↔ Kotlin

| Swift Module (EduGoModules) | Kotlin Module (Propuesto) |
|----------------------------|--------------------------|
| `FoundationToolkit` | `kmp-foundation` |
| `LoggerSDK` | `kmp-logger` |
| `NetworkSDK` | `kmp-network` |
| `CQRSKit` | `kmp-cqrs` |
| `DesignSystemSDK` | `kmp-designsystem` |
| `FormsSDK` | `kmp-components` (incluye forms) |
| `UIComponentsSDK` | `kmp-components` |
| `EduFoundation` | `kmp-foundation` |
| `EduCore` (Logger+Models+Utils) | `kmp-logger` + `kmp-core` |
| `EduNetwork` | `kmp-network` |
| `EduStorage` | `kmp-storage` |
| `EduPersistence` | `kmp-persistence` |
| `EduDomain` (CQRS+State+UseCases) | `kmp-cqrs` + `kmp-state` + `kmp-auth` |
| `EduPresentation` (Design+Components+Nav) | `kmp-designsystem` + `kmp-components` + `kmp-navigation` |
