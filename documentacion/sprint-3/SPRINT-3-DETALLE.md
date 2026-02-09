# Sprint 3: Dominio - Detalle de Implementacion

**Version:** 1.0
**Fecha:** 2026-02-08
**Stack Target:** Kotlin 2.2.20, Compose 1.9.0, AGP 8.12.0, Koin 4.1, Ktor 3.1.3
**Plataformas:** Android (compile=36, min=24), iOS (on-demand), Desktop (JVM), WASM
**Package Base:** `com.edugo.kmp.{modulo}`

## Prerequisites

- Sprint 1 completado: kmp-foundation, kmp-logger, kmp-core existen
- Sprint 2 completado: kmp-validation, kmp-network, kmp-storage, kmp-config existen

## Indice

1. [Task 3.1: Modulo kmp-auth](#task-31-modulo-kmp-auth)
2. [Task 3.2: Modulo kmp-di](#task-32-modulo-kmp-di)
3. [Diagrama de Dependencias](#diagrama-de-dependencias)
4. [Orden de Ejecucion](#orden-de-ejecucion)
5. [Checklist de Verificacion](#checklist-de-verificacion)
6. [Problemas Potenciales](#problemas-potenciales)

---

## Task 3.1: Modulo kmp-auth

### Objetivo

Crear modulo de autenticacion con gestion de JWT, refresh automatico de tokens, AuthService con estado reactivo, AuthInterceptor que implementa network.Interceptor, y interfaces genericas de Role/Permission (SIN enums especificos de EduGo).

### Estructura de archivos completa

Ver documento completo para estructura detallada de ~44 archivos a crear.

### Puntos criticos de refactoring

#### 1. AuthInterceptor implementa network.Interceptor

AuthInterceptor Y TokenProvider SE MUEVEN de kmp-network a kmp-auth para evitar dependencia circular.

```kotlin
// kmp-auth/interceptor/AuthInterceptor.kt
class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {  // IMPLEMENTA com.edugo.kmp.network.interceptor.Interceptor
    override val order: Int = 20
    override suspend fun interceptRequest(request: HttpRequestBuilder) {
        // Agrega token al request
    }
}
```

#### 2. Interfaces genericas Role/Permission

Se crean interfaces genericas en kmp-auth. Los enums especificos (SystemRole, AppPermission) quedan en la app.

```kotlin
// kmp-auth/roles/Role.kt - INTERFACE GENERICA
interface Role {
    val name: String
    val displayName: String
    val level: Int
}

// EN LA APP - Enum especifico
enum class SystemRole : Role {
    ADMIN, TEACHER, STUDENT;
    // Implementa interface Role
}
```

#### 3. JwtClaims sin campos especificos

Eliminar convenience accessors especificos de EduGo. Solo claims estandar JWT + customClaims map.


### Cambios de package principales

| Viejo Package | Nuevo Package |
|---------------|---------------|
| `com.edugo.test.module.auth.*` | `com.edugo.kmp.auth.*` |
| `com.edugo.test.module.network.interceptor.AuthInterceptor` | `com.edugo.kmp.auth.interceptor.AuthInterceptor` |
| `com.edugo.test.module.roles.*` | `com.edugo.kmp.auth.roles.*` |
| `com.edugo.test.module.data.models.AuthToken` | `com.edugo.kmp.auth.model.AuthToken` |

### Dependencias del modulo kmp-auth

```kotlin
dependencies {
    implementation(project(":kmp-foundation"))
    implementation(project(":kmp-logger"))
    implementation(project(":kmp-core"))
    implementation(project(":kmp-validation"))
    implementation(project(":kmp-network"))
    implementation(project(":kmp-storage"))
    
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.ktor.client.common)
}
```

### Codigo de build.gradle.kts para kmp-auth

```kotlin
plugins {
    id("kmp.android")
    id("kover")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.edugo.kmp.auth"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kmp-foundation"))
                implementation(project(":kmp-logger"))
                implementation(project(":kmp-core"))
                implementation(project(":kmp-validation"))
                implementation(project(":kmp-network"))
                implementation(project(":kmp-storage"))
                
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.bundles.ktor.client.common)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
```

### Tests a migrar

- `jwt/JwtParserTest.kt` - Test parsing de JWT
- `jwt/JwtValidatorImplTest.kt` - Test validacion contra backend
- `service/AuthServiceImplTest.kt` - Test AuthService completo
- `service/AuthStateTest.kt` - Test estados de autenticacion
- `service/LogoutTest.kt` - Test logout con idempotencia
- `service/AuthServiceRefreshIntegrationTest.kt` - Test refresh automatico
- `repository/StubAuthRepositoryTest.kt` - Test repository stub
- `model/LoginCredentialsTest.kt` - Test validacion de credenciales
- `model/LoginResponseTest.kt` - Test deserializacion

### Verificacion kmp-auth

```bash
./gradlew :kmp-auth:build
./gradlew :kmp-auth:test
./gradlew :kmp-auth:koverHtmlReport
./gradlew :kmp-auth:compileKotlinJvm  # Verificar AuthInterceptor implementa Interceptor
```


---

## Task 3.2: Modulo kmp-di

### Objetivo

Crear modulo de inyeccion de dependencias con Koin 4.1. Incluye modules para todos los modulos existentes y platformModule expect/actual para 4 plataformas.

### Estructura de archivos

```
kmp-di/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/com/edugo/kmp/di/
│   │   ├── KoinInitializer.kt
│   │   ├── FoundationModule.kt
│   │   ├── LoggerModule.kt
│   │   ├── CoreModule.kt
│   │   ├── ValidationModule.kt
│   │   ├── NetworkModule.kt
│   │   ├── StorageModule.kt
│   │   ├── ConfigModule.kt
│   │   ├── AuthModule.kt
│   │   └── PlatformModule.kt          # expect
│   ├── androidMain/kotlin/com/edugo/kmp/di/
│   │   └── PlatformModule.android.kt  # actual
│   ├── iosMain/kotlin/com/edugo/kmp/di/
│   │   └── PlatformModule.ios.kt      # actual
│   ├── desktopMain/kotlin/com/edugo/kmp/di/
│   │   └── PlatformModule.desktop.kt  # actual
│   ├── wasmJsMain/kotlin/com/edugo/kmp/di/
│   │   └── PlatformModule.wasmJs.kt   # actual
│   └── commonTest/kotlin/com/edugo/kmp/di/
│       └── KoinModulesTest.kt
```

### Modules principales

#### KoinInitializer.kt

```kotlin
package com.edugo.kmp.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun coreKoinModules(): List<Module> = listOf(
    foundationModule,
    loggerModule,
    coreModule,
    platformModule()
)

fun allModules(): List<Module> = coreKoinModules() + listOf(
    validationModule,
    networkModule,
    storageModule,
    configModule,
    authModule
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(allModules())
    }
```

#### AuthModule.kt (ejemplo completo)

```kotlin
package com.edugo.kmp.di

import com.edugo.kmp.auth.interceptor.AuthInterceptor
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.repository.AuthRepositoryImpl
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthServiceImpl
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.auth.token.TokenRefreshManagerImpl
import com.edugo.kmp.auth.token.TokenRefreshConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule: Module = module {
    single<CoroutineScope>(qualifier = named("AuthScope")) {
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get(),
            baseUrl = get<AppConfig>().apiBaseUrl
        )
    }

    single<TokenRefreshManager> {
        TokenRefreshManagerImpl(
            repository = get(),
            storage = get(),
            config = TokenRefreshConfig.DEFAULT,
            scope = get(named("AuthScope")),
            json = get()
        )
    }

    single<AuthService> {
        AuthServiceImpl(
            repository = get(),
            storage = get(),
            json = get(),
            scope = get(named("AuthScope")),
            refreshConfig = TokenRefreshConfig.DEFAULT
        )
    }

    single<AuthInterceptor> {
        AuthInterceptor(
            tokenProvider = get<AuthService>(),
            autoRefresh = true
        )
    }
}
```

### PlatformModule expect/actual

#### commonMain/PlatformModule.kt

```kotlin
package com.edugo.kmp.di

import org.koin.core.module.Module

expect fun platformModule(): Module
```

#### androidMain/PlatformModule.android.kt

```kotlin
package com.edugo.kmp.di

import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        Settings()  // SharedPreferences en Android
    }
}
```

#### desktopMain/PlatformModule.desktop.kt

```kotlin
package com.edugo.kmp.di

import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        Settings()  // java.util.prefs.Preferences en Desktop
    }
}
```

#### iosMain/PlatformModule.ios.kt

```kotlin
package com.edugo.kmp.di

import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        Settings()  // NSUserDefaults en iOS
    }
}
```

#### wasmJsMain/PlatformModule.wasmJs.kt

```kotlin
package com.edugo.kmp.di

import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        Settings()  // localStorage en WASM
    }
}
```


### Dependencias del modulo kmp-di

```kotlin
dependencies {
    // Todos los modulos internos
    implementation(project(":kmp-foundation"))
    implementation(project(":kmp-logger"))
    implementation(project(":kmp-core"))
    implementation(project(":kmp-validation"))
    implementation(project(":kmp-network"))
    implementation(project(":kmp-storage"))
    implementation(project(":kmp-config"))
    implementation(project(":kmp-auth"))
    
    // Koin
    implementation(libs.koin.core)
    
    // multiplatform-settings (para platformModule)
    implementation(libs.multiplatform.settings)
    
    // kotlinx
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
```

### Codigo de build.gradle.kts para kmp-di

```kotlin
@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp.android")
    id("kover")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.edugo.kmp.di"
    
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

kotlin {
    val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
    if (enableIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }
    
    jvm("desktop")
    
    wasmJs {
        browser()
        binaries.library()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kmp-foundation"))
                implementation(project(":kmp-logger"))
                implementation(project(":kmp-core"))
                implementation(project(":kmp-validation"))
                implementation(project(":kmp-network"))
                implementation(project(":kmp-storage"))
                implementation(project(":kmp-config"))
                implementation(project(":kmp-auth"))
                
                implementation(libs.koin.core)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.koin.test)
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
```

### Tests para kmp-di

#### KoinModulesTest.kt

```kotlin
package com.edugo.kmp.di

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.logger.EduGoLogger
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.storage.EduGoStorage
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.get
import kotlin.test.*

class KoinModulesTest : KoinTest {

    @BeforeTest
    fun setup() {
        startKoin {
            modules(allModules())
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify all modules load correctly`() {
        getKoin().checkModules()
    }

    @Test
    fun `verify core dependencies`() {
        assertNotNull(get<Json>())
        assertNotNull(get<EduGoLogger>())
    }

    @Test
    fun `verify auth dependencies`() {
        assertNotNull(get<AuthService>())
    }
}
```

### Ejemplo de uso

#### Android Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidContext(this@MyApplication)
        }
    }
}
```

#### iOS

```kotlin
fun initKoinIos() = initKoin {}
```

#### Desktop

```kotlin
fun main() {
    initKoin {}
    
    application {
        // Compose Desktop app
    }
}
```

### Verificacion kmp-di

```bash
./gradlew :kmp-di:build
./gradlew :kmp-di:test
./gradlew :kmp-di:compileKotlinJvm
./gradlew :kmp-di:dependencies --configuration commonMainImplementation
```


---

## Diagrama de Dependencias

```
kmp-di
├─> kmp-auth
│   ├─> kmp-network (para Interceptor interface)
│   ├─> kmp-storage (para EduGoStorage)
│   ├─> kmp-core (para Result, ErrorCode, ValidatableModel)
│   ├─> kmp-validation (para validators)
│   └─> kmp-logger
│
├─> kmp-config
│   └─> kmp-foundation
│
├─> kmp-storage
│   ├─> kmp-core (para serialization)
│   └─> kmp-logger
│
├─> kmp-network
│   ├─> kmp-core (para Result)
│   └─> kmp-logger
│
├─> kmp-validation
│   ├─> kmp-core (para Result, ErrorCode)
│   └─> kmp-logger
│
├─> kmp-core
│   └─> kmp-foundation
│
├─> kmp-logger
│   └─> kmp-foundation
│
└─> kmp-foundation (base - sin dependencias)
```

**CRITICO:** NO hay dependencias circulares. kmp-auth depende de kmp-network (para Interceptor), pero AuthInterceptor VIVE en kmp-auth (no en kmp-network).

---

## Orden de Ejecucion

### Task 3.1: kmp-auth (8-12 horas)

1. Crear estructura de directorios `kmp-auth/`
2. Configurar `build.gradle.kts` con namespace y dependencias
3. Migrar archivos JWT con package change
4. **REFACTOR:** Eliminar campos especificos en JwtClaims y JwtValidationResult
5. Migrar TokenRefreshManager y configuracion
6. Migrar AuthService y AuthServiceImpl
7. Migrar AuthRepository (interface + impl + stub)
8. Migrar modelos (LoginCredentials, AuthToken, etc.)
9. **REFACTOR:** Mover AuthInterceptor de network a auth/interceptor/
10. **REFACTOR:** Crear interfaces genericas Role/Permission
11. Migrar tests
12. Build y verificacion

### Task 3.2: kmp-di (4-6 horas)

1. Crear estructura de directorios `kmp-di/`
2. Configurar `build.gradle.kts` con todas las dependencias
3. Crear KoinInitializer.kt
4. Crear modules individuales (Foundation, Logger, Core, etc.)
5. Crear AuthModule con todas las dependencias auth
6. Crear PlatformModule expect en commonMain
7. Crear actual implementations para Android, iOS, Desktop, WASM
8. Crear KoinModulesTest
9. Build y verificacion

---

## Checklist de Verificacion

### Task 3.1: kmp-auth

- [ ] Modulo creado con namespace `com.edugo.kmp.auth`
- [ ] Todos los archivos JWT migrados (6 archivos)
- [ ] JwtClaims SIN convenience accessors especificos
- [ ] AuthInterceptor en `kmp-auth/interceptor/` (NO en kmp-network)
- [ ] AuthInterceptor IMPLEMENTA `com.edugo.kmp.network.interceptor.Interceptor`
- [ ] Interfaces genericas Role/Permission/RoleHierarchy creadas
- [ ] PermissionChecker generico con tipos `<R : Role, P : Permission>`
- [ ] TokenRefreshManager con Mutex para thread-safety
- [ ] AuthService implementa TokenProvider
- [ ] Todos los tests migrados (9 archivos)
- [ ] `./gradlew :kmp-auth:build` exitoso
- [ ] `./gradlew :kmp-auth:test` exitoso
- [ ] Coverage > 80%

### Task 3.2: kmp-di

- [ ] Modulo creado con namespace `com.edugo.kmp.di`
- [ ] Dependencias a TODOS los modulos configuradas
- [ ] KoinInitializer con initKoin(), allModules()
- [ ] 8 modules creados (Foundation, Logger, Core, Validation, Network, Storage, Config, Auth)
- [ ] PlatformModule expect/actual para 4 plataformas
- [ ] Android: Settings con SharedPreferences
- [ ] iOS: Settings con NSUserDefaults
- [ ] Desktop: Settings con Preferences
- [ ] WASM: Settings con localStorage
- [ ] KoinModulesTest verifica carga de modules
- [ ] `./gradlew :kmp-di:build` exitoso
- [ ] `./gradlew :kmp-di:test` exitoso
- [ ] `getKoin().checkModules()` pasa

---

## Problemas Potenciales

### Task 3.1: kmp-auth

#### Problema 1: Dependencia circular kmp-auth ↔ kmp-network

**Sintoma:** Error "circular dependency detected"

**Solucion:** Mover AuthInterceptor Y TokenProvider de kmp-network a kmp-auth/interceptor/. AuthInterceptor IMPLEMENTA Interceptor (de kmp-network) pero VIVE en kmp-auth.

#### Problema 2: JwtClaims con logica especifica de EduGo

**Sintoma:** kmp-auth tiene campos de negocio (userId, role, schoolId)

**Solucion:** Eliminar convenience accessors. Solo claims estandar JWT + customClaims map. La app puede agregar extensions propias.

#### Problema 3: Thread-safety en TokenRefreshManager

**Sintoma:** Race conditions en refreshes concurrentes

**Solucion:** Usar `Mutex` de kotlinx.coroutines (KMP-compatible), NO `synchronized`.

### Task 3.2: kmp-di

#### Problema 1: Settings no se provee en platformModule

**Sintoma:** Runtime error "No definition found for type Settings"

**Solucion:** Verificar que cada plataforma tiene su `actual fun platformModule()` en el source set correcto.

#### Problema 2: Modules en orden incorrecto

**Sintoma:** "No definition found for dependency X"

**Solucion:** Cargar modules en orden de dependencias en allModules().

#### Problema 3: iOS no compila por falta de iosMain

**Solucion:** Usar flag `-PenableIos=true` para habilitar iOS solo cuando se necesita.

---

## Resumen Ejecutivo

**Sprint 3 completa la capa de dominio:**

### Entregables

1. **kmp-auth:** ~44 archivos
   - JWT parsing y validation
   - Token refresh automatico con retry y thread-safety
   - AuthService con estado reactivo (StateFlow)
   - AuthInterceptor que implementa Interceptor
   - Interfaces genericas Role/Permission
   - Tests > 80% coverage

2. **kmp-di:** ~13 archivos
   - 8 Koin modules para todos los modulos
   - PlatformModule expect/actual para 4 plataformas
   - KoinInitializer con initKoin()
   - Tests verificando carga de modules

### Tiempo Estimado

- Task 3.1 (kmp-auth): 8-12 horas
- Task 3.2 (kmp-di): 4-6 horas
- **Total Sprint 3:** 12-18 horas (2-3 dias)

### Puntos Criticos

1. AuthInterceptor VIVE en kmp-auth (evita circular dependency)
2. Interfaces genericas Role/Permission (enums especificos en la app)
3. PlatformModule expect/actual provee Settings por plataforma
4. TokenRefreshManager usa Mutex (KMP-compatible)

### Referencias de Codigo

- **kmp-auth origen:** `/Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common/test-module/src/commonMain/kotlin/com/edugo/test/module/auth/`
- **kmp-di referencia:** `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/core/dependency-injection/`

### Proximo Sprint

Sprint 4 (Presentacion): ViewModel, State Management, Navigation
