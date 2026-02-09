# Sprint 1: Cimientos del Monorepo - Detalle de Implementacion

**Stack:** Kotlin 2.2.20, AGP 8.12.0, Compose 1.9.0, Koin 4.1, Ktor 3.1.3
**Plataformas:** Android (compile=36, min=24), iOS (on-demand), Desktop (JVM), WASM
**Package base:** `com.edugo.kmp.{modulo}`

**Fuentes:**
- Kmp-Common: `/Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common`
- Template-Kmp-Clean: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean`

---

## TASK 1.1: Estructura Base del Monorepo

### Objetivo
Crear proyecto Gradle vacio que configura sin errores con las versiones target.

### Archivos a crear

#### kmp_new/gradle.properties
```properties
# Kotlin
kotlin.code.style=official
kotlin.incremental=true

# Gradle Performance
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

# Kotlin Daemon
kotlin.daemon.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m

# Android
android.useAndroidX=true
android.nonTransitiveRClass=true

# KMP
kotlin.mpp.androidSourceSetLayoutVersion=2
kotlin.mpp.stability.nowarn=true

# iOS ON-DEMAND
enableIos=false

# Compose
org.jetbrains.compose.experimental.uikit.enabled=true
```

#### kmp_new/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}
```

#### kmp_new/settings.gradle.kts
```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EduGoKmpModules"

// Sprint 1 modules
include(":modules:foundation")
include(":modules:logger")
include(":modules:core")
include(":modules:validation")
```

#### kmp_new/gradle/libs.versions.toml
Copiar de Template (`/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/gradle/libs.versions.toml`) y agregar:

```toml
[versions]
agp = "8.12.0"
android-compileSdk = "36"
android-minSdk = "24"
android-targetSdk = "36"
androidx-activity = "1.11.0"
androidx-appcompat = "1.7.1"
androidx-core = "1.17.0"
androidx-lifecycle = "2.9.4"
composeMultiplatform = "1.9.0"
koin = "4.1.0"
kotlin = "2.2.20"
kotlinx-coroutines = "1.10.2"
kotlinx-datetime = "0.6.1"
kotlinx-serialization = "1.8.0"
ktor = "3.1.3"
multiplatform-settings = "1.3.0"
kermit = "2.0.4"
kover = "0.8.3"
turbine = "1.2.0"

[libraries]
# Kotlin
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }

# AndroidX
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidx-core" }
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "androidx-appcompat" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "androidx-lifecycle" }
androidx-lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }

# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }

# Coroutines
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-swing = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-swing", version.ref = "kotlinx-coroutines" }

# KotlinX
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinx-serialization" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }

# Settings
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatform-settings" }
multiplatform-settings-no-arg = { module = "com.russhwolf:multiplatform-settings-no-arg", version.ref = "multiplatform-settings" }
multiplatform-settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "multiplatform-settings" }
multiplatform-settings-make-observable = { module = "com.russhwolf:multiplatform-settings-make-observable", version.ref = "multiplatform-settings" }
multiplatform-settings-test = { module = "com.russhwolf:multiplatform-settings-test", version.ref = "multiplatform-settings" }

# Logging
kermit = { module = "co.touchlab:kermit", version.ref = "kermit" }

# Testing
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlinxSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

#### Gradle Wrapper
Copiar de Template:
```bash
cp -r /Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/gradle/wrapper kmp_new/gradle/wrapper/
cp /Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/gradlew kmp_new/
cp /Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/gradlew.bat kmp_new/
```

### Verificacion
```bash
cd kmp_new && ./gradlew tasks --no-configuration-cache
```

---

## TASK 1.2: Convention Plugins Base

### Objetivo
Crear build-logic con plugins que configuren automaticamente todos los targets KMP.

### Archivos a crear

#### kmp_new/build-logic/settings.gradle.kts
```kotlin
dependencyResolution {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"
```

#### kmp_new/build-logic/build.gradle.kts
```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.androidLibrary.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.composeMultiplatform.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.composeCompiler.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.kotlinxSerialization.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}
```

**NOTA:** Si la sintaxis `libs.plugins.X.map` no funciona, usar IDs directos:
```kotlin
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    compileOnly("com.android.library:com.android.library.gradle.plugin:8.12.0")
    compileOnly("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:1.9.0")
    compileOnly("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.20")
    compileOnly("org.jetbrains.kotlin:kotlin-serialization:2.2.20")
}
```

#### kmp_new/build-logic/src/main/kotlin/kmp.logic.core.gradle.kts
Plugin para modulos de logica SIN Compose, SIN Android:
```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask { enabled = true }
        }
        binaries.library()
    }

    val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
    if (enableIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                implementation(libs.findLibrary("kotlinx-serialization-json").get())
                implementation(libs.findLibrary("kotlinx-datetime").get())
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
```

#### kmp_new/build-logic/src/main/kotlin/kmp.android.gradle.kts
Plugin para modulos con Android + Desktop + WASM + iOS (on-demand):
```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask { enabled = true }
        }
        binaries.library()
    }

    val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
    if (enableIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                implementation(libs.findLibrary("kotlinx-serialization-json").get())
                implementation(libs.findLibrary("kotlinx-datetime").get())
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
```

#### kmp_new/build-logic/src/main/kotlin/kover.gradle.kts
```kotlin
plugins {
    id("org.jetbrains.kotlinx.kover")
}
```

### Verificacion
```bash
cd kmp_new && ./gradlew tasks
```

---

## TASK 1.3: Modulo kmp-foundation

### Objetivo
Migrar Result monad, AppError+ErrorCode, entity interfaces, pagination, serialization, mapper.
Total: ~4,833 lineas de codigo fuente.

### build.gradle.kts
```kotlin
plugins {
    id("kmp.logic.core")
}
```

### Mapa de archivos fuente → destino

**Prefijo origen:** `test-module/src/commonMain/kotlin/com/edugo/test/module/`
**Prefijo destino:** `modules/foundation/src/commonMain/kotlin/com/edugo/kmp/foundation/`

| Origen | Destino | Lineas | Cambios |
|--------|---------|--------|---------|
| `core/Result.kt` | `result/Result.kt` | 442 | Package, quitar @JsExport |
| `core/AppError.kt` | `error/AppError.kt` | 696 | Package, quitar @JsExport, js→wasm |
| `core/ErrorCode.kt` | `error/ErrorCode.kt` | 544 | Package, quitar @JsExport |
| `core/serialization/ThrowableSerializer.kt` | `serialization/ThrowableSerializer.kt` | 119 | Package |
| `config/JsonConfig.kt` | `serialization/JsonConfig.kt` | 202 | Package |
| `extensions/ResultCombinators.kt` | `result/ResultCombinators.kt` | 314 | Package |
| `extensions/CollectionResultExtensions.kt` | `result/CollectionResultExtensions.kt` | 465 | Package |
| `mapper/DomainMapper.kt` | `mapper/DomainMapper.kt` | 327 | Package |
| `data/models/base/EntityBase.kt` | `entity/EntityBase.kt` | 133 | Package |
| `data/models/base/ValidatableModel.kt` | `entity/ValidatableModel.kt` | 270 | Package |
| `data/models/base/AuditableModel.kt` | `entity/AuditableModel.kt` | 295 | Package |
| `data/models/base/SoftDeletable.kt` | `entity/SoftDeletable.kt` | 339 | Package |
| `data/models/pagination/PagedResult.kt` | `pagination/PagedResult.kt` | 344 | Package |
| `data/models/pagination/PaginationExtensions.kt` | `pagination/PaginationExtensions.kt` | 343 | Package |

### Cambios de package (buscar/reemplazar en todos los archivos)

| Buscar | Reemplazar por |
|--------|---------------|
| `package com.edugo.test.module.core` | `package com.edugo.kmp.foundation` (o subpackage segun carpeta) |
| `package com.edugo.test.module.config` | `package com.edugo.kmp.foundation.serialization` |
| `package com.edugo.test.module.extensions` | `package com.edugo.kmp.foundation.result` |
| `package com.edugo.test.module.mapper` | `package com.edugo.kmp.foundation.mapper` |
| `package com.edugo.test.module.data.models.base` | `package com.edugo.kmp.foundation.entity` |
| `package com.edugo.test.module.data.models.pagination` | `package com.edugo.kmp.foundation.pagination` |

### Imports a actualizar en TODOS los archivos

| Import viejo | Import nuevo |
|-------------|-------------|
| `com.edugo.test.module.core.Result` | `com.edugo.kmp.foundation.result.Result` |
| `com.edugo.test.module.core.AppError` | `com.edugo.kmp.foundation.error.AppError` |
| `com.edugo.test.module.core.ErrorCode` | `com.edugo.kmp.foundation.error.ErrorCode` |
| `com.edugo.test.module.core.serialization.*` | `com.edugo.kmp.foundation.serialization.*` |
| `com.edugo.test.module.config.JsonConfig` | `com.edugo.kmp.foundation.serialization.JsonConfig` |
| `com.edugo.test.module.data.models.base.*` | `com.edugo.kmp.foundation.entity.*` |
| `com.edugo.test.module.data.models.pagination.*` | `com.edugo.kmp.foundation.pagination.*` |
| `com.edugo.test.module.extensions.*` | `com.edugo.kmp.foundation.result.*` |
| `com.edugo.test.module.mapper.*` | `com.edugo.kmp.foundation.mapper.*` |

### Cambios adicionales
1. **Quitar `@JsExport`** de Result.kt, AppError.kt, ErrorCode.kt (WASM no usa JsExport)
2. **Quitar `kotlin.js.JsExport`** imports
3. **Reemplazar `@OptIn(ExperimentalJsExport::class)`** si existe
4. ValidatableModel referencia `Result` → ajustar import a `com.edugo.kmp.foundation.result.Result`

### Tests a migrar

**Prefijo test origen:** `test-module/src/commonTest/kotlin/com/edugo/test/module/`
**Prefijo test destino:** `modules/foundation/src/commonTest/kotlin/com/edugo/kmp/foundation/`

| Test origen | Test destino |
|-------------|-------------|
| `core/ResultTest.kt` | `result/ResultTest.kt` |
| `core/ResultAppErrorExtensionsTest.kt` | `result/ResultAppErrorExtensionsTest.kt` |
| `core/AppErrorTest.kt` | `error/AppErrorTest.kt` |
| `core/ErrorCodeTest.kt` | `error/ErrorCodeTest.kt` |
| `core/SerializationExtensionsTest.kt` | `serialization/SerializationExtensionsTest.kt` |
| `config/JsonConfigTest.kt` | `serialization/JsonConfigTest.kt` |
| `data/models/base/EntityBaseTest.kt` | `entity/EntityBaseTest.kt` |
| `data/models/base/ValidatableModelTest.kt` | `entity/ValidatableModelTest.kt` |
| `data/models/base/AuditableModelTest.kt` | `entity/AuditableModelTest.kt` |
| `data/models/base/SoftDeletableTest.kt` | `entity/SoftDeletableTest.kt` |
| `data/models/pagination/PagedResultTest.kt` | `pagination/PagedResultTest.kt` |
| `data/models/pagination/PaginationExtensionsTest.kt` | `pagination/PaginationExtensionsTest.kt` |
| `extensions/ResultCombinatorsTest.kt` | `result/ResultCombinatorsTest.kt` |
| `extensions/CollectionResultExtensionsTest.kt` | `result/CollectionResultExtensionsTest.kt` |
| `mapper/DomainMapperTest.kt` | `mapper/DomainMapperTest.kt` |
| `data/models/helpers/ModelPatchTest.kt` | `entity/ModelPatchTest.kt` |
| `data/models/helpers/ModelMergeTest.kt` | `entity/ModelMergeTest.kt` |

### Verificacion
```bash
./gradlew :modules:foundation:desktopTest
./gradlew :modules:foundation:wasmJsTest
```

---

## TASK 1.4: Modulo kmp-logger

### Objetivo
Migrar sistema de logging multiplataforma con Kermit. ~485 lineas + platform-specific.

### build.gradle.kts
```kotlin
plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.logger"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
                implementation(libs.kermit)
            }
        }
    }
}
```

### Mapa de archivos

**Origen:** `test-module/src/commonMain/kotlin/com/edugo/test/module/platform/`
**Destino:** `modules/logger/src/commonMain/kotlin/com/edugo/kmp/logger/`

| Origen | Destino | Lineas |
|--------|---------|--------|
| `Logger.kt` | `Logger.kt` | 316 |
| `KermitLogger.kt` | `KermitLogger.kt` | 169 |

### Cambios de package
| Buscar | Reemplazar |
|--------|-----------|
| `package com.edugo.test.module.platform` | `package com.edugo.kmp.logger` |
| `import com.edugo.test.module.core.*` | `import com.edugo.kmp.foundation.*` |

### Platform-specific (expect/actual)
El Logger actual tiene `expect fun createDefaultLogger()` y `expect class KermitConfig`.

Archivos platform-specific a migrar/crear:
- `androidMain/` → copiar de `test-module/src/androidMain/.../platform/` (buscar con Glob)
- `desktopMain/` → copiar de `test-module/src/desktopMain/.../platform/`
- `wasmJsMain/` → CREAR NUEVO basado en jsMain (reemplazar JS APIs por WASM)
- `iosMain/` → CREAR NUEVO (NSLog-based, solo si enableIos=true)

**Buscar archivos platform-specific:**
```bash
find test-module/src/*/kotlin -path "*/platform/*" -name "*.kt"
```

### Tests a migrar

| Test origen | Test destino |
|-------------|-------------|
| `platform/TaggedLoggerTest.kt` | `TaggedLoggerTest.kt` |
| `platform/TagParserTest.kt` | `TagParserTest.kt` |
| `platform/LoggerConfigTest.kt` | `LoggerConfigTest.kt` |
| `platform/LoggerTest.kt` | `LoggerTest.kt` |
| `platform/LogFilterTest.kt` | `LogFilterTest.kt` |
| `platform/LoggerFactoryTest.kt` | `LoggerFactoryTest.kt` |
| `platform/KermitLoggerCommonTest.kt` | `KermitLoggerCommonTest.kt` |
| `platform/LoggerExtensionsTest.kt` | `LoggerExtensionsTest.kt` |

### Verificacion
```bash
./gradlew :modules:logger:desktopTest
./gradlew :modules:logger:androidUnitTest
```

---

## TASK 1.5: Modulo kmp-core

### Objetivo
Migrar Platform info, Dispatchers, y extension functions. ~192 lineas + platform-specific.

### build.gradle.kts
```kotlin
plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.core"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
            }
        }
    }
}
```

### Mapa de archivos

| Origen | Destino | Lineas |
|--------|---------|--------|
| `platform/Platform.kt` | `platform/Platform.kt` | 76 |
| `platform/Dispatchers.kt` | `platform/Dispatchers.kt` | 116 |
| `data/models/helpers/ModelPatch.kt` | `model/ModelPatch.kt` | buscar |
| `data/models/helpers/ModelMerge.kt` | `model/ModelMerge.kt` | buscar |
| `data/models/helpers/Patchable.kt` | `model/Patchable.kt` | buscar |

**Destino base:** `modules/core/src/commonMain/kotlin/com/edugo/kmp/core/`

### Cambios de package
| Buscar | Reemplazar |
|--------|-----------|
| `package com.edugo.test.module.platform` | `package com.edugo.kmp.core.platform` |
| `package com.edugo.test.module.data.models.helpers` | `package com.edugo.kmp.core.model` |

### Platform-specific (expect/actual)
Platform.kt tiene `expect object Platform` y Dispatchers.kt tiene `expect object AppDispatchers`.
Migrar actuals de androidMain, desktopMain, jsMain→wasmJsMain.

### Tests
| Test origen | Test destino |
|-------------|-------------|
| `PlatformTest.kt` | `platform/PlatformTest.kt` |
| `data/models/helpers/ModelPatchTest.kt` | `model/ModelPatchTest.kt` |
| `data/models/helpers/ModelMergeTest.kt` | `model/ModelMergeTest.kt` |

### Verificacion
```bash
./gradlew :modules:core:desktopTest
./gradlew :modules:core:androidUnitTest
```

---

## TASK 1.6: Modulo kmp-validation

### Objetivo
Migrar validadores y AccumulativeValidation. ~1,163 lineas.

### build.gradle.kts
```kotlin
plugins {
    id("kmp.logic.core")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
            }
        }
    }
}
```

### Mapa de archivos

| Origen | Destino | Lineas |
|--------|---------|--------|
| `validation/ValidationHelpers.kt` | `ValidationHelpers.kt` | 764 |
| `validation/AccumulativeValidation.kt` | `AccumulativeValidation.kt` | 399 |

**Destino base:** `modules/validation/src/commonMain/kotlin/com/edugo/kmp/validation/`

### Cambios de package
| Buscar | Reemplazar |
|--------|-----------|
| `package com.edugo.test.module.validation` | `package com.edugo.kmp.validation` |
| `import com.edugo.test.module.core.Result` | `import com.edugo.kmp.foundation.result.Result` |
| `import com.edugo.test.module.core.AppError` | `import com.edugo.kmp.foundation.error.AppError` |
| `import com.edugo.test.module.core.ErrorCode` | `import com.edugo.kmp.foundation.error.ErrorCode` |

### Tests
| Test origen | Test destino |
|-------------|-------------|
| `validation/ValidationHelpersTest.kt` | `ValidationHelpersTest.kt` |
| `validation/AccumulativeValidationTest.kt` | `AccumulativeValidationTest.kt` |
| `integration/ValidationIntegrationTest.kt` | `integration/ValidationIntegrationTest.kt` |

### Verificacion
```bash
./gradlew :modules:validation:desktopTest
./gradlew :modules:validation:wasmJsTest
```

---

## Diagrama de Dependencias

```
Task 1.1 (estructura) ──→ Task 1.2 (plugins) ──→ Task 1.3 (foundation)
                                                       ↓
                                        ┌──────────────┼──────────────┐
                                        ↓              ↓              ↓
                                   Task 1.4       Task 1.5       Task 1.6
                                   (logger)       (core)         (validation)
```

Tasks 1.4, 1.5, 1.6 son PARALELAS (independientes entre si, solo dependen de 1.3).

---

## Orden de Ejecucion

1. **Task 1.1** - Estructura base (~30 min)
2. **Task 1.2** - Convention plugins (~1h)
3. **Task 1.3** - Foundation (~3h) - el mas grande
4. **En paralelo:**
   - Task 1.4 - Logger (~2h)
   - Task 1.5 - Core (~1h)
   - Task 1.6 - Validation (~1h)

**Total estimado:** 6-8 horas

---

## Checklist de Verificacion Global

```bash
# Compilacion de todos los modulos
./gradlew assemble

# Tests de todos los modulos
./gradlew test

# Tests por plataforma
./gradlew desktopTest
./gradlew androidUnitTest
./gradlew wasmJsTest

# Verificar independencia (cada modulo compila solo)
./gradlew :modules:foundation:build
./gradlew :modules:logger:build
./gradlew :modules:core:build
./gradlew :modules:validation:build
```

### Criterios de exito
- [ ] Todos los modulos compilan en Desktop y WASM
- [ ] Modulos con Android (logger, core) compilan en Android
- [ ] Tests pasan en todas las plataformas
- [ ] Cero dependencias circulares
- [ ] Cada modulo se importa independientemente
- [ ] Packages correctos: `com.edugo.kmp.{modulo}`

---

## Problemas Potenciales

### 1. @JsExport incompatible con WASM
**Sintoma:** Error de compilacion en wasmJs por `@JsExport`
**Solucion:** Eliminar `@JsExport` y `@OptIn(ExperimentalJsExport::class)` de todos los archivos. WASM no necesita `@JsExport`.

### 2. Convention plugin no encuentra libs
**Sintoma:** `Unresolved reference: libs` en convention plugins
**Solucion:** Usar `libs.findLibrary("nombre").get()` en convention plugins (no `libs.nombre` directo).

### 3. Source sets jsMain no existen (ahora es wasmJsMain)
**Sintoma:** `Source set 'jsMain' not found`
**Solucion:** Renombrar todos los `jsMain` a `wasmJsMain`. Para platform-specific files, crear nuevos en `wasmJsMain/` basados en los de `jsMain/`.

### 4. jvmSharedMain ya no existe
**Sintoma:** Error por `jvmSharedMain` no encontrado
**Solucion:** En la nueva estructura NO usamos `jvmSharedMain`. Android y Desktop son source sets separados. Si hay codigo compartido JVM, duplicar o crear un intermediate source set.

### 5. enableIos=false pero tests requieren iOS
**Sintoma:** Tests de iOS fallan
**Solucion:** Los tests de iOS solo corren con `-PenableIos=true`. En CI, configurar un job separado para iOS.

### 6. Gradle configuration cache incompatible
**Sintoma:** Error con `--configuration-cache`
**Solucion:** Para la primera ejecucion, usar `--no-configuration-cache`. Luego ajustar convention plugins para ser CC-compatible.
