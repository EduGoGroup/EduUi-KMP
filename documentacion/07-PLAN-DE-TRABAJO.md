# 07 - Plan de Trabajo para Iniciar la Migracion

## Resumen Ejecutivo

Crear un monorepo de modulos KMP independientes tomando:
- **Estructura y plataformas** de Template-Kmp-Clean
- **Codigo de logica** de Kmp-Common
- **Versiones actualizadas** de Template-Kmp-Clean

El trabajo se divide en **4 sprints** con entregables concretos.

---

## Sprint 1: Cimientos del Monorepo

**Objetivo:** Tener el monorepo funcional con foundation y logger compilando en las 4 plataformas.

### Task 1.1: Crear estructura base del monorepo
**Tipo:** Setup
**Estimacion:** Pequeña

**Acciones:**
1. Crear `kmp_new/modules/` como raiz de modulos
2. Crear `kmp_new/build.gradle.kts` raiz
3. Crear `kmp_new/settings.gradle.kts` con los primeros modulos
4. Crear `kmp_new/gradle.properties` (basado en Template, con iOS ON-DEMAND)
5. Copiar `gradle/libs.versions.toml` de Template como base y agregar deps de Kmp-Common
6. Crear `kmp_new/gradle/wrapper/` (copiar de Template)

**Resultado:** Proyecto Gradle vacio que configura sin errores.

**Archivos a crear:**
```
kmp_new/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradle/
    ├── wrapper/gradle-wrapper.jar
    ├── wrapper/gradle-wrapper.properties
    └── libs.versions.toml
```

---

### Task 1.2: Crear convention plugins base
**Tipo:** Setup
**Estimacion:** Media

**Acciones:**
1. Crear `kmp_new/build-logic/` con estructura de convention plugins
2. Crear plugin `kmp.logic.core` (Android + iOS on-demand + Desktop + WASM, sin Compose)
3. Crear plugin `kmp.logic.mobile` (Android + iOS on-demand, sin Compose)
4. Crear plugin `kover` para code coverage

**Convention plugin `kmp.logic.core`:**
```kotlin
// Targets: Android, iOS (on-demand), Desktop (JVM), WASM
// Dependencies auto: kotlinx-coroutines, kotlinx-serialization, kotlinx-datetime
// NO incluye: Ktor, Compose, Settings
```

**Archivos a crear:**
```
kmp_new/build-logic/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/kotlin/
    ├── kmp.logic.core.gradle.kts
    ├── kmp.logic.mobile.gradle.kts
    └── kover.gradle.kts
```

---

### Task 1.3: Crear modulo kmp-foundation
**Tipo:** Migracion de codigo
**Estimacion:** Media
**Depende de:** 1.1, 1.2

**Acciones:**
1. Crear estructura de carpetas del modulo
2. Crear `build.gradle.kts` usando plugin `kmp.logic.core`
3. Migrar de Kmp-Common:
   - `core/Result.kt` → `foundation/result/Result.kt`
   - `core/AppError.kt` → `foundation/error/AppError.kt` (fusionado con Template)
   - `core/ErrorCode.kt` → `foundation/error/ErrorCode.kt`
   - `core/ErrorExtensions.kt` → `foundation/error/ErrorExtensions.kt`
   - `core/serialization/` → `foundation/serialization/`
   - `config/JsonConfig.kt` → `foundation/serialization/JsonConfig.kt`
   - `data/models/base/` → `foundation/entity/`
   - `data/models/pagination/` → `foundation/pagination/`
   - `extensions/ResultExtensions.kt` → `foundation/result/ResultExtensions.kt`
   - `mapper/` → `foundation/mapper/`
4. Cambiar packages a `com.edugo.kmp.foundation`
5. Fusionar AppError (sealed hierarchy de Template + ErrorCode numerico de Kmp-Common)
6. Migrar tests correspondientes
7. Verificar compilacion en las 4 plataformas

**Criterio de exito:**
```bash
./gradlew :modules:foundation:test  # Pasa en todas las plataformas
```

**Archivos:**
```
kmp_new/modules/foundation/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/edugo/kmp/foundation/
    │   ├── error/AppError.kt, ErrorCode.kt, ErrorExtensions.kt
    │   ├── result/Result.kt, ResultExtensions.kt, ResultCombinators.kt
    │   ├── entity/EntityBase.kt, ValidatableModel.kt, AuditableModel.kt, SoftDeletable.kt
    │   ├── pagination/PagedResult.kt, PaginationExtensions.kt
    │   ├── serialization/JsonConfig.kt, SerializationExtensions.kt, ThrowableSerializer.kt
    │   └── mapper/DomainMapper.kt
    └── commonTest/kotlin/com/edugo/kmp/foundation/
        ├── ResultTest.kt
        ├── AppErrorTest.kt
        ├── ErrorCodeTest.kt
        ├── EntityBaseTest.kt
        └── PaginationTest.kt
```

---

### Task 1.4: Crear modulo kmp-logger
**Tipo:** Migracion + nuevo codigo
**Estimacion:** Media
**Depende de:** 1.3

**Acciones:**
1. Crear estructura del modulo
2. Crear `build.gradle.kts` usando `kmp.logic.core`
3. Migrar logger de Kmp-Common (platform-specific)
4. Agregar expect/actual para cada plataforma:
   - Android: android.util.Log (Logcat)
   - iOS: NSLog / os_log
   - Desktop: println o SLF4J
   - WASM: console.log/warn/error
5. Definir `LogLevel`, `LogCategory`, `LoggerFactory`
6. Tests basicos

**Archivos:**
```
kmp_new/modules/logger/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/edugo/kmp/logger/
    │   ├── Logger.kt (interface)
    │   ├── LogLevel.kt
    │   ├── LogCategory.kt
    │   └── LoggerFactory.kt
    ├── androidMain/kotlin/.../LogcatLogger.kt
    ├── iosMain/kotlin/.../NSLogLogger.kt
    ├── desktopMain/kotlin/.../JvmLogger.kt
    ├── wasmJsMain/kotlin/.../ConsoleLogger.kt
    └── commonTest/kotlin/.../LoggerTest.kt
```

---

### Task 1.5: Crear modulo kmp-validation
**Tipo:** Migracion + expansion
**Estimacion:** Pequeña
**Depende de:** 1.3

**Acciones:**
1. Migrar de Kmp-Common: validators + AccumulativeValidation
2. Agregar reglas adicionales: NumberRules, CollectionRules
3. Crear DSL de validacion fluido
4. Tests

---

### Task 1.6: Crear modulo kmp-core
**Tipo:** Migracion
**Estimacion:** Pequeña
**Depende de:** 1.3

**Acciones:**
1. Migrar extensions, helpers, platform abstractions
2. Agregar Platform.kt con expect/actual
3. Agregar Dispatchers.kt con expect/actual
4. Tests

---

### Entregable Sprint 1:
```
kmp_new/
├── build-logic/          # Convention plugins
├── modules/
│   ├── foundation/       # Result, AppError, ErrorCode, Entity interfaces
│   ├── logger/           # Logger multiplatform
│   ├── core/             # Extensions, Platform, Dispatchers
│   └── validation/       # Validators, AccumulativeValidation
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

**Verificacion:**
```bash
./gradlew test                        # Todos los tests pasan
./gradlew :modules:foundation:test    # Foundation independiente
./gradlew :modules:logger:test        # Logger independiente
./gradlew :modules:validation:test    # Validation independiente
```

---

## Sprint 2: Infraestructura

**Objetivo:** Network y Storage funcionando independientemente.

### Task 2.1: Crear modulo kmp-network
**Tipo:** Migracion con refactor
**Estimacion:** Grande
**Depende de:** Sprint 1

**Acciones:**
1. Migrar HTTP client de Kmp-Common
2. **CRITICO:** Remover AuthInterceptor del modulo network
3. Mantener Interceptor interface generica
4. Migrar InterceptorChain, HeaderInterceptor, LoggingInterceptor
5. Migrar RetryPolicy con BackoffStrategy
6. Agregar platform engines:
   - Android: OkHttp (de Kmp-Common)
   - iOS: Darwin (nuevo)
   - Desktop: CIO (de Kmp-Common)
   - WASM: Fetch/CIO (de Template)
7. Integrar con kmp-logger para logging
8. Tests con mock HTTP responses

**Archivos criticos:**
```kotlin
// network/interceptor/Interceptor.kt
interface Interceptor {
    suspend fun intercept(chain: Chain): Response
}

// network/interceptor/InterceptorChain.kt
class InterceptorChain(private val interceptors: List<Interceptor>) {
    suspend fun proceed(request: Request): Response
}

// NO hay AuthInterceptor aqui - eso va en kmp-auth
```

---

### Task 2.2: Crear modulo kmp-storage
**Tipo:** Migracion + fusion
**Estimacion:** Media
**Depende de:** Sprint 1

**Acciones:**
1. Migrar 3 niveles de storage de Kmp-Common:
   - Storage basico (sync)
   - AsyncStorage (coroutines)
   - FlowStorage (reactive)
2. Integrar multiplatform-settings de Template:
   - Android: SharedPreferences
   - iOS: NSUserDefaults
   - Desktop: java.util.prefs
   - WASM: localStorage
3. Usar ObservableSettings + FlowSettings de Template
4. Agregar EncryptedStorage wrapper (placeholder)
5. Tests

---

### Task 2.3: Crear modulo kmp-config
**Tipo:** Migracion desde Template
**Estimacion:** Pequeña
**Depende de:** Sprint 1

**Acciones:**
1. Migrar AppConfig, Environment, ConfigLoader de Template
2. Migrar JSON configs (dev.json, staging.json, prod.json)
3. Migrar ResourceLoader expect/actual por plataforma
4. Tests

---

### Entregable Sprint 2:
```
modules/
├── (sprint 1)
├── network/         # HTTP client, interceptors, retry
├── storage/         # Key-value storage, 3 niveles
└── config/          # AppConfig, Environment, JSON configs
```

---

## Sprint 3: Dominio

**Objetivo:** Auth completo, DI configurado.

### Task 3.1: Crear modulo kmp-auth
**Tipo:** Migracion con refactor
**Estimacion:** Grande
**Depende de:** Sprint 2

**Acciones:**
1. Migrar de Kmp-Common:
   - AuthService interface + impl
   - JwtParser, JwtValidator
   - TokenRefreshManager
   - AuthState
2. **Crear AuthInterceptor aqui** (NO en network)
3. Migrar de Template:
   - SessionManager concept
   - LoginUseCase pattern
   - LoginState sealed class
4. Crear Role/Permission interfaces genericas
5. Tests

---

### Task 3.2: Crear modulo kmp-di
**Tipo:** Migracion desde Template
**Estimacion:** Media
**Depende de:** 3.1

**Acciones:**
1. Migrar estructura de Koin modules de Template
2. Adaptar CoreModules para nuevos modulos
3. Crear platformModule expect/actual por plataforma
4. Agregar modulos para cada feature (network, storage, auth, config)
5. Crear `allModules()` helper
6. Tests con Koin check

---

### Entregable Sprint 3:
```
modules/
├── (sprint 1 & 2)
├── auth/            # AuthService, JWT, TokenManager, AuthInterceptor
└── di/              # Koin modules, platform modules
```

---

## Sprint 4: Presentacion + App de Ejemplo

**Objetivo:** UI funcional con Compose Multiplatform.

### Task 4.1: Crear convention plugin kmp.ui.full
**Tipo:** Setup
**Estimacion:** Media

**Acciones:**
1. Crear plugin que agrega Compose Multiplatform
2. Configurar todos los targets con Compose
3. Agregar Material3 dependency

---

### Task 4.2: Crear modulo kmp-design
**Tipo:** Migracion desde Template + expansion
**Estimacion:** Media
**Depende de:** 4.1

**Acciones:**
1. Migrar Design Tokens de Template (Spacing, Sizes, Alpha, Radius)
2. Migrar SemanticColors
3. Crear EduGoTheme (MaterialTheme wrapper)
4. Crear Typography con Material3
5. Migrar DSAlertDialog, DSSnackbar de Template

---

### Task 4.3: Crear modulo kmp-resources
**Tipo:** Migracion desde Template
**Estimacion:** Pequeña
**Depende de:** 4.1

**Acciones:**
1. Migrar Strings expect/actual de Template
2. Adaptar para las 4 plataformas

---

### Task 4.4: Crear modulo kmp-navigation
**Tipo:** Migracion desde Template
**Estimacion:** Pequeña
**Depende de:** 4.1

**Acciones:**
1. Migrar NavigationState de Template
2. Migrar Routes sealed class
3. Migrar NavigationHost composable
4. Tests de navegacion

---

### Task 4.5: Crear app de ejemplo
**Tipo:** Integracion
**Estimacion:** Grande
**Depende de:** Todo

**Acciones:**
1. Crear estructura `platforms/` (mobile, desktop, web)
2. Crear App.kt con Koin + Navigation + Theme
3. Crear pantallas basicas (Splash, Login, Home, Settings)
4. Migrar ViewModels y States de Template
5. Configurar entry points por plataforma
6. Verificar que corre en Android, Desktop y WASM
7. Verificar iOS con flag `enableIos=true`

**Archivos:**
```
kmp_new/
├── platforms/
│   ├── mobile/app/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── androidMain/.../MainActivity.kt
│   │       └── iosMain/.../MainIos.kt
│   ├── desktop/app/
│   │   ├── build.gradle.kts
│   │   └── src/desktopMain/.../Main.kt
│   └── web/app/
│       ├── build.gradle.kts
│       └── src/wasmJsMain/.../Main.kt
├── ui/
│   └── screens/
│       ├── build.gradle.kts
│       └── src/commonMain/.../
│           ├── App.kt
│           └── screens/
│               ├── SplashScreen.kt
│               ├── LoginScreen.kt
│               ├── HomeScreen.kt
│               └── SettingsScreen.kt
```

---

## Orden de Ejecucion (Secuencial)

```
Sprint 1 (Cimientos):
  1.1 Estructura base ──→ 1.2 Convention plugins ──→ 1.3 Foundation
                                                         ↓
                                                    1.4 Logger (paralelo con 1.5, 1.6)
                                                    1.5 Validation
                                                    1.6 Core

Sprint 2 (Infra):
  2.1 Network ──→ 2.2 Storage ──→ 2.3 Config

Sprint 3 (Dominio):
  3.1 Auth ──→ 3.2 DI

Sprint 4 (UI):
  4.1 Plugin UI ──→ 4.2 Design ──→ 4.3 Resources ──→ 4.4 Navigation ──→ 4.5 App ejemplo
```

---

## Criterios de Aceptacion Globales

Cada modulo debe cumplir:

1. **Compila en 4 plataformas:** Android, iOS (on-demand), Desktop, WASM
2. **Tests pasan** en `commonTest` + platform tests si aplican
3. **Se importa independientemente** sin traer modulos no deseados
4. **Cero dependencias circulares**
5. **Package correcto:** `com.edugo.kmp.{modulo}`
6. **API publica documentada** con KDoc

---

## Version Catalog Consolidado (libs.versions.toml)

```toml
[versions]
# Kotlin & Build
kotlin = "2.2.20"
agp = "8.12.0"
composeMultiplatform = "1.9.0"

# Android
android-compileSdk = "36"
android-minSdk = "24"
android-targetSdk = "36"

# KotlinX
kotlinx-coroutines = "1.10.2"
kotlinx-serialization = "1.8.0"
kotlinx-datetime = "0.6.1"

# Network
ktor = "3.1.3"

# DI
koin = "4.1.0"

# Storage
multiplatform-settings = "1.3.0"

# Testing
turbine = "1.2.0"
kover = "0.8.3"

# Logging
kermit = "2.0.4"

[libraries]
# Se detallan todas las dependencias necesarias
# (combinar catalogs de ambos proyectos, tomar version mas reciente)

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
kotlinxSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

---

## Siguiente Paso Inmediato

**Ejecutar Task 1.1:** Crear la estructura base del monorepo. Es el primer paso concreto que desbloquea todo lo demas.

```bash
# Verificar que funciona:
cd kmp_new && ./gradlew tasks
```
