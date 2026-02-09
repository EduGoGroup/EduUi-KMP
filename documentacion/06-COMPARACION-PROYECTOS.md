# 06 - Comparacion de Proyectos: Lo Mejor de Cada Uno

## Los Dos Proyectos

| Aspecto | Kmp-Common (Actual) | Template-Kmp-Clean |
|---------|---------------------|-------------------|
| **Kotlin** | 2.1.20 | 2.2.20 |
| **Compose** | No tiene | 1.9.0 |
| **AGP** | 8.7.2 | 8.12.0 |
| **Android SDK** | compile=35, min=29 | compile=36, min=24 |
| **Plataformas** | Android, Desktop, JS | Android, iOS, Desktop, WASM |
| **Archivos Kotlin** | 114 (logica) + 74 (tests) | 65 total + 9 tests |
| **Modulos** | 2 (monolito) | 16 (clean arch) |
| **DI** | No tiene | Koin 4.1.0 |
| **UI** | No tiene | Compose Multiplatform |
| **Navigation** | No tiene | Custom (sin dependencias) |
| **Convention Plugins** | 4 plugins | No tiene (explicito) |
| **iOS** | Parcial | ON-DEMAND (flag) |
| **Web** | Kotlin/JS | Kotlin/WASM |
| **Config** | No tiene | JSON por ambiente (dev/staging/prod) |
| **Strings** | No tiene | expect/actual multiplatform |
| **Design Tokens** | No tiene | Spacing, Sizes, Alpha, Radius |

---

## Que Tomar de Cada Proyecto

### De Template-Kmp-Clean (ESTRUCTURA)

Estas cosas son **superiores** en Template y deben ser la base:

#### 1. Estructura Clean Architecture por Feature
```
features/
├── auth/
│   ├── domain/      # Entidades, interfaces, use cases
│   ├── data/        # Implementaciones, repos
│   └── presentation/ # ViewModel, State, Screen
└── settings/
    ├── domain/
    ├── data/
    └── presentation/
```
**Por que:** Escalable, cada feature es independiente, facil de agregar/remover.

#### 2. Separacion de Platform Apps
```
platforms/
├── mobile/app/      # Android + iOS entry points
├── desktop/app/     # Desktop (JVM) entry point
└── web/app/         # WASM entry point
```
**Por que:** Cada plataforma tiene su propio entry point limpio.

#### 3. iOS ON-DEMAND Pattern
```properties
# gradle.properties
enableIos=false  # Solo compilar iOS cuando se necesite
```
```kotlin
// build.gradle.kts
val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
if (enableIos) {
    iosX64(); iosArm64(); iosSimulatorArm64()
}
```
**Por que:** Evita errores de compilacion en equipos sin macOS, builds mas rapidos.

#### 4. Kotlin/WASM (no JS)
```kotlin
wasmJs {
    browser()
    binaries.library()
}
```
**Por que:** Mejor rendimiento, es el futuro de Kotlin en la web.

#### 5. Koin para DI
```kotlin
val coreModule = module {
    single<ErrorHandler> { ErrorHandlerImpl() }
    single<PreferencesManager> { PreferencesManagerImpl(get()) }
}
expect fun platformModule(): Module
```
**Por que:** Maduro, soporte KMP nativo, modular por features.

#### 6. AppConfig con JSON por Ambiente
```kotlin
// dev.json, staging.json, prod.json
data class AppConfig(
    val environment: Environment,
    val apiUrl: String,
    val apiPort: Int,
    val timeout: Long,
    val debugMode: Boolean
)
```
**Por que:** Configuracion limpia, sin hardcodear URLs.

#### 7. Sistema de Navegacion Custom
```kotlin
class NavigationState(initialRoute: Route) {
    fun navigateTo(route: Route)
    fun back(): Boolean
    fun popTo(route: Route): Boolean
    fun saveState(): String
    fun restoreState(state: String): Boolean
}
```
**Por que:** Sin dependencias externas, backstack funcional, serializable.

#### 8. Design Tokens
```kotlin
object Spacing { val xxs = 2.dp; val xs = 4.dp; val sm = 8.dp; ... }
object Sizes { val iconSmall = 16.dp; val touchTarget = 48.dp; ... }
object Alpha { val disabled = 0.38f; val muted = 0.6f; ... }
```
**Por que:** Consistencia visual centralizada.

#### 9. Strings Multiplatform
```kotlin
expect object Strings {
    val login_title: String
    val error_unknown: String
}
```
**Por que:** I18n ready, type-safe.

#### 10. Versiones Mas Recientes
- Kotlin 2.2.20, AGP 8.12.0, Compose 1.9.0, Koin 4.1.0
- Android compile SDK 36, Coroutines 1.10.2

---

### De Kmp-Common (LOGICA/CODIGO)

Estas cosas son **superiores** en Kmp-Common y deben integrarse:

#### 1. Result Monad con Loading + API Funcional
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T)
    data class Failure(val error: String)
    object Loading

    fun map(transform: (T) -> R): Result<R>
    fun flatMap(transform: (T) -> Result<R>): Result<R>
    fun recover(recovery: (String) -> T): Result<T>
    fun zip(other: Result<B>, transform: (A, B) -> R): Result<R>
    fun combine(vararg results: Result<T>): Result<List<T>>
}
```
**vs Template** que usa `kotlin.Result` basico (solo Success/Failure, sin Loading, sin API funcional).
**Por que:** Loading state es esencial en apps, API funcional permite composicion elegante.

#### 2. AppError + ErrorCode con Rangos Numericos
```kotlin
enum class ErrorCode(val code: Int, val description: String, val retryable: Boolean) {
    NETWORK_TIMEOUT(1000, ..., true),      // 1000-1999 = Network
    AUTH_UNAUTHORIZED(2000, ..., false),    // 2000-2999 = Auth
    VALIDATION_INVALID_INPUT(3000, ..., false), // 3000-3999 = Validation
    BUSINESS_RESOURCE_NOT_FOUND(4000, ..., false), // 4000-4999 = Business
    SYSTEM_INTERNAL_ERROR(5000, ..., true), // 5000-5999 = System
}
```
**vs Template** que usa sealed class con code: String (menos estructurado).
**Fusion:** Combinar la sealed class hierarchy de Template (NetworkError, BusinessLogicError, etc.) con los ErrorCode numericos de Kmp-Common.

#### 3. HTTP Client Completo con Interceptores
```kotlin
class EduGoHttpClient(client: HttpClient, interceptorChain: InterceptorChain) {
    suspend inline fun <reified T> get(url: String, config: HttpRequestConfig): T
    suspend inline fun <reified T> post(url: String, body: Any, config: HttpRequestConfig): T
    suspend inline fun <reified T> put(url: String, body: Any, config: HttpRequestConfig): T
    suspend inline fun <reified T> delete(url: String, config: HttpRequestConfig): T
}
// + InterceptorChain pluggable
// + RetryPolicy con exponential backoff
```
**vs Template** que no tiene HTTP client implementado (solo dependencias Ktor sin usar).
**Por que:** Codigo listo para produccion con retry y interceptores.

#### 4. JWT Parser y Validator
```kotlin
interface JwtParser { fun parse(token: String): JwtParseResult }
interface JwtValidator { fun validate(token: String, options: JwtValidationOptions): JwtValidationResult }
```
**vs Template** que no tiene JWT.
**Por que:** Esencial para auth con tokens.

#### 5. Token Refresh Manager
```kotlin
interface TokenRefreshManager {
    suspend fun shouldRefreshToken(): Boolean
    suspend fun refreshToken(): Result<Boolean>
    suspend fun onTokenRefreshSuccess(accessToken: String, refreshToken: String?, expiresAt: Long?)
    suspend fun onTokenRefreshFailure(reason: RefreshFailureReason)
}
```
**Por que:** Manejo automatico de refresh tokens con 401 handling.

#### 6. Storage con 3 Niveles
```kotlin
class EduGoStorage(settings, keyPrefix)           // Sync basico
class AsyncEduGoStorage(storage)                   // Coroutine wrapper
class StateFlowStorage<T>(storage, key, default, serializer)  // Reactivo
```
**vs Template** que tiene `PreferencesManagerImpl` basico con ObservableSettings.
**Fusion:** Usar la estructura de 3 niveles de Kmp-Common con el ObservableSettings+FlowSettings de Template.

#### 7. Validation con AccumulativeValidation
```kotlin
fun String.isValidEmail(): Boolean           // API rapida
fun String.validateEmail(): Result<String>   // API con detalle

class AccumulativeValidation {
    fun <T> validate(value: T, validator: (T) -> Result<T>): AccumulativeValidation
    fun build(): Result<Unit>  // Acumula TODOS los errores
}
```
**vs Template** que tiene validacion inline en UseCase (sin sistema).
**Por que:** Formularios necesitan mostrar todos los errores a la vez.

#### 8. Entity Interfaces
```kotlin
interface EntityBase { val id: String }
interface ValidatableModel { fun validate(): Result<Unit> }
interface AuditableModel { val createdAt, updatedAt, createdBy, updatedBy }
interface SoftDeletable { val deleted, deletedAt, deletedBy }
interface Patchable<T> { fun patch(other: T): T }
```
**vs Template** que no tiene interfaces base.
**Por que:** Contratos consistentes para todos los modelos de dominio.

#### 9. PagedResult
```kotlin
data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)
```
**vs Template** que no tiene paginacion.
**Por que:** Toda API real tiene paginacion.

#### 10. Convention Plugins
```kotlin
// kmp.android.gradle.kts - Configura todo automaticamente
plugins { id("kmp.android") }
// vs Template donde cada modulo repite la configuracion
```
**Por que:** Con 13+ modulos, repetir configuracion es insostenible. Convention plugins son necesarios.

---

## Tabla de Fusion Final

| Componente | Viene de | Modificaciones |
|-----------|----------|----------------|
| **Estructura de carpetas** | Template | Adaptar a monorepo de modulos |
| **Clean Arch (domain/data/presentation)** | Template | Mantener tal cual |
| **Platform apps separation** | Template | Mantener tal cual |
| **Kotlin/WASM** | Template | Mantener tal cual |
| **iOS ON-DEMAND** | Template | Mantener tal cual |
| **Versiones (Kotlin 2.2.20, etc)** | Template | Actualizar a ultimas |
| **Koin DI** | Template | Mantener tal cual |
| **AppConfig + JSON** | Template | Mantener tal cual |
| **Navigation custom** | Template | Mantener tal cual |
| **Design Tokens** | Template | Mantener + expandir |
| **Strings multiplatform** | Template | Mantener tal cual |
| **Result monad** | Kmp-Common | Mejorar con ideas de Template |
| **AppError + ErrorCode** | Kmp-Common | Fusionar con sealed hierarchy de Template |
| **HTTP Client + Interceptores** | Kmp-Common | Desacoplar de auth |
| **Retry Policy** | Kmp-Common | Mantener tal cual |
| **JWT Parser/Validator** | Kmp-Common | Mantener tal cual |
| **TokenRefreshManager** | Kmp-Common | Mantener tal cual |
| **Storage 3 niveles** | Kmp-Common | Usar FlowSettings de Template tambien |
| **Validation + Accumulative** | Kmp-Common | Mantener + expandir |
| **Entity interfaces** | Kmp-Common | Mantener tal cual |
| **PagedResult** | Kmp-Common | Mantener tal cual |
| **Convention Plugins** | Kmp-Common | Reescribir para nuevos targets |
| **DomainMapper** | Kmp-Common | Mantener tal cual |
| **JsonConfig presets** | Kmp-Common | Mantener tal cual |

---

## AppError Fusionado (Propuesta)

Combinar lo mejor de ambos:

```kotlin
// De Template: sealed class hierarchy (claro, ergonomico)
// De Kmp-Common: ErrorCode numerico (estructurado, serializable)

sealed class AppError(
    val errorCode: ErrorCode,        // ← De Kmp-Common (numerico, retryable)
    val userMessage: String,          // ← De Template (mensaje para UI)
    val technicalMessage: String,     // ← De Template (mensaje para logs)
    val details: Map<String, String> = emptyMap(), // ← De Kmp-Common
    cause: Throwable? = null
) : Exception(technicalMessage, cause) {

    // ← De Template: subclases semanticas
    data class Network(
        override val errorCode: ErrorCode,
        // ...
    ) : AppError(errorCode, ...) {
        fun isRetryable(): Boolean = errorCode.retryable
    }

    data class Validation(
        val field: String?,
        // ...
    ) : AppError(ErrorCode.VALIDATION_INVALID_INPUT, ...)

    data class Auth(
        // ...
    ) : AppError(errorCode, ...)

    data class Business(
        // ...
    ) : AppError(errorCode, ...)

    data class System(
        // ...
    ) : AppError(errorCode, ...)

    // ← De Template: recovery actions
    fun getRecoveryActions(): List<RecoveryAction>
}
```

---

## Que NO Tomar

| Componente | Proyecto | Razon |
|-----------|----------|-------|
| Kotlin/JS target | Kmp-Common | Reemplazado por WASM |
| `jvmSharedMain` custom hierarchy | Kmp-Common | No necesario con nueva estructura |
| Nombre `test-module` | Kmp-Common | Era temporal |
| Auth acoplado a Network | Kmp-Common | Desacoplar con interceptor interface |
| Sin convention plugins | Template | Necesarios para 13+ modulos |
| `kotlin.Result` basico | Template | Result custom es superior |
| Validacion inline en UseCase | Template | Sistema de validacion es mejor |
| Mock-only auth | Template | JWT real de Kmp-Common es mejor |
