# 01 - Analisis del Proyecto Actual (KMP-Common)

## Estado General

**Proyecto:** EduGo KMP Shared
**Stack:** Kotlin 2.1.20 + Multiplatform
**Gradle:** 8.11+ con Convention Plugins
**Modulos:** 2 (test-module, test-module-full)

---

## Estructura Actual

```
Kmp-Common/
├── build-logic/                      # Convention Plugins (4 plugins)
│   └── src/main/kotlin/
│       ├── kmp.android.gradle.kts    # Android + Desktop + JS
│       ├── kmp.full.gradle.kts       # Full multiplatform (+ iOS)
│       ├── kmp.library.gradle.kts    # Pure JVM
│       └── kover.gradle.kts          # Code coverage
├── test-module/                      # Modulo principal (114 archivos Kotlin)
│   └── src/
│       ├── commonMain/               # Codigo compartido
│       ├── commonTest/               # Tests compartidos (74 archivos)
│       ├── jvmSharedMain/            # Codigo compartido JVM (Android+Desktop)
│       ├── androidMain/              # Implementaciones Android (9 archivos)
│       ├── desktopMain/              # Implementaciones Desktop (9 archivos)
│       └── jsMain/                   # Implementaciones JS (9 archivos)
├── test-module-full/                 # Modulo de prueba iOS
├── gradle/libs.versions.toml         # Version Catalog centralizado
├── gradle.properties                 # Config Gradle + KMP
└── docs/                             # Documentacion existente
```

---

## Inventario de Codigo por Paquete

| Paquete | Archivos | Que hace |
|---------|----------|----------|
| `core/` | 7 | Result monad, AppError, ErrorCode, serialization |
| `auth/` | 25 | JWT parsing/validation, login, token refresh, AuthState |
| `auth/jwt/` | 7 | JwtParser, JwtValidator, JwtClaims |
| `auth/model/` | 5 | LoginCredentials, AuthUserInfo, LoginResponse |
| `auth/repository/` | 4 | AuthRepository interface + stubs |
| `auth/service/` | 4 | AuthService, AuthState, Factory |
| `auth/token/` | 4 | TokenRefreshManager, Config, FailureReason |
| `network/` | 15 | EduGoHttpClient (Ktor), interceptors, retry |
| `network/interceptor/` | 3 | Auth, Headers, Logging interceptors |
| `network/retry/` | 3 | Retry con exponential backoff |
| `storage/` | 11 | Key-value storage, serialization, Flow reactivo |
| `validation/` | 3 | Email, URL, UUID validators, AccumulativeValidation |
| `data/models/` | 13 | EntityBase, ValidatableModel, AuditableModel, Pagination |
| `mapper/` | 3 | DomainMapper utilities |
| `platform/` | 14 | Logger, Dispatchers, Platform info (expect/actual) |
| `roles/` | 11 | SystemRole, Permission, RoleHierarchy, DSL |
| `config/` | 2 | JsonConfig presets |
| `extensions/` | 3 | Result combinators, Collection helpers |

**Total:** 114 archivos en commonMain, 74 archivos de test

---

## Patrones Implementados (Rescatables)

### 1. Result Monad - RESCATAR
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T)
    data class Failure(val error: String)
    object Loading
}
// API funcional: map, flatMap, recover, zip, combine
```
**Calidad:** Alta. Bien implementado con API funcional completa.

### 2. AppError + ErrorCode - RESCATAR
```kotlin
class AppError(code: ErrorCode, message: String, details: Map<String,String>, cause: Throwable?)

enum class ErrorCode(val code: Int, val description: String, val retryable: Boolean) {
    NETWORK_TIMEOUT(1000, ..., true),
    AUTH_UNAUTHORIZED(2000, ..., false),
    VALIDATION_INVALID_INPUT(3000, ..., false),
    // Rangos: Network=1000, Auth=2000, Validation=3000, Business=4000, System=5000, Storage=6000
}
```
**Calidad:** Alta. Sistema de errores estructurado por rangos.

### 3. Network Layer (Ktor) - RESCATAR CON REFACTOR
```kotlin
class EduGoHttpClient(client: HttpClient, interceptorChain: InterceptorChain) {
    suspend inline fun <reified T> get(url: String, config: HttpRequestConfig): T
    suspend inline fun <reified T> post(url: String, body: Any, config: HttpRequestConfig): T
}
// Interceptors: Auth, Headers, Logging
// Retry: Exponential backoff con RetryPolicy
```
**Calidad:** Media-Alta. Funcional pero acoplado al auth system.

### 4. Storage Layer - RESCATAR
```kotlin
class EduGoStorage(settings: Settings, keyPrefix: String)
class AsyncEduGoStorage(storage: EduGoStorage)    // Coroutine wrapper
class StateFlowStorage<T>(storage, key, default, serializer)  // Reactivo
```
**Calidad:** Alta. Bien estratificado con 3 niveles de API.

### 5. Auth System - RESCATAR PARCIALMENTE
```kotlin
interface AuthService { val authState: StateFlow<AuthState> }
interface TokenRefreshManager { suspend fun refreshToken(): Result<Boolean> }
interface JwtParser { fun parse(token: String): JwtParseResult }
interface JwtValidator { fun validate(token: String): JwtValidationResult }
```
**Calidad:** Media. Demasiados archivos (25) para un modulo de auth. JWT parser/validator son rescatables por separado.

### 6. Validation - RESCATAR
```kotlin
fun String.isValidEmail(): Boolean      // API booleana
fun String.validateEmail(): Result<String>  // API Result
class AccumulativeValidation { ... }    // Multiples errores
```
**Calidad:** Media. Funcional pero basica.

### 7. Domain Models - RESCATAR CON SIMPLIFICACION
```kotlin
interface EntityBase { val id: String }
interface ValidatableModel { fun validate(): Result<Unit> }
interface AuditableModel { val createdAt, updatedAt, createdBy, updatedBy }
interface SoftDeletable { val deleted, deletedAt, deletedBy }
interface Patchable<T> { fun patch(other: T): T }
data class PagedResult<T>(items, page, pageSize, totalItems, totalPages)
```
**Calidad:** Media-Alta. Buenos contratos base.

### 8. Roles & Permissions - RESCATAR
```kotlin
enum class SystemRole { ADMIN(100), OWNER(90), TEACHER(50), STUDENT(30)... }
enum class Permission { VIEW_MATERIALS, UPLOAD_MATERIALS... }
// Extension functions para User
```
**Calidad:** Alta para el dominio EduGo. Separar parte generica de parte especifica.

---

## Plataformas Configuradas

| Plataforma | Engine | Implementaciones | Estado |
|-----------|--------|-------------------|--------|
| Android (API 29+) | OkHttp | 9 archivos | Completo |
| Desktop (JVM 17) | CIO | 9 archivos | Completo |
| JavaScript | JS Fetch/Node | 9 archivos | Completo |
| iOS | Darwin | Solo en test-module-full | Parcial |

**Nota:** Custom hierarchy con `jvmSharedMain` (no usa defaultHierarchyTemplate).

---

## Convention Plugins Existentes

| Plugin | Targets | Uso |
|--------|---------|-----|
| `kmp.android` | Android + Desktop + JS | Modulo principal |
| `kmp.full` | Desktop + JS + iOS | Modulo completo |
| `kmp.library` | JVM only | Librerias puras |
| `kover` | N/A | Code coverage |

**Dependencias auto-incluidas por plugins:**
- kotlinx-coroutines, kotlinx-serialization, kotlinx-datetime
- Ktor (client + engines por plataforma)
- multiplatform-settings (storage)
- Kermit (logging)
- Turbine (testing)

---

## Problemas Identificados

### 1. Modulo Monolitico
Todo esta en `test-module`. No hay separacion real en modulos independientes. Si quieres usar solo `network`, debes traer todo incluyendo `auth`, `roles`, `storage`, etc.

### 2. Nombre Temporal
`test-module` es un nombre de prueba que se quedo como principal.

### 3. Acoplamiento Excesivo
- Network depende de Auth (interceptor de auth integrado)
- Auth depende de Storage (tokens)
- Roles depende de Auth (user extensions)
- No se pueden usar por separado

### 4. JS en vez de WASM
Configurado para JavaScript (browser+Node) pero el objetivo es Kotlin/WASM.

### 5. iOS Incompleto
Solo test-module-full tiene iOS y de forma basica.

### 6. Sin Capa de Presentacion
No hay componentes UI, design system, ni navigation (a diferencia de los modulos Swift).

### 7. Sin CQRS/State Management Avanzado
Los modulos Swift tienen CQRS y StatePublisher. Aca solo se usa StateFlow basico.

### 8. Convention Plugins Acoplados
Los plugins auto-incluyen demasiadas dependencias. Un modulo de `core` no necesita Ktor.

---

## Que Rescatar (Resumen)

| Componente | Accion | Destino Propuesto |
|-----------|--------|-------------------|
| Result monad | Mover tal cual | `foundation` module |
| AppError + ErrorCode | Mover tal cual | `foundation` module |
| EduGoHttpClient | Refactorizar | `network` module |
| Interceptors | Separar de auth | `network` module |
| EduGoStorage | Mover tal cual | `storage` module |
| StateFlowStorage | Mover tal cual | `storage` module |
| JwtParser/Validator | Extraer de auth | `auth` o `security` module |
| TokenRefreshManager | Extraer de auth | `auth` module |
| Validation | Mover + expandir | `validation` module |
| EntityBase interfaces | Mover | `foundation` module |
| PagedResult | Mover | `foundation` module |
| Platform abstractions | Mover | `platform` module |
| Logger (Kermit) | Refactorizar | `logger` module |
| SystemRole/Permission | Parte generica | `auth` module |
| JsonConfig | Mover | `foundation` module |
