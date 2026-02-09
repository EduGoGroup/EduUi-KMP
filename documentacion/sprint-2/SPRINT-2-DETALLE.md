# SPRINT 2: Infraestructura - Documento Detallado

## Índice
- [Visión General](#visión-general)
- [Prerequisitos](#prerequisitos)
- [Task 2.1: Módulo kmp-network](#task-21-módulo-kmp-network)
- [Task 2.2: Módulo kmp-storage](#task-22-módulo-kmp-storage)
- [Task 2.3: Módulo kmp-config](#task-23-módulo-kmp-config)
- [Diagrama de Dependencias](#diagrama-de-dependencias)
- [Orden de Ejecución](#orden-de-ejecución)
- [Checklist de Verificación Global](#checklist-de-verificación-global)
- [Problemas Potenciales y Soluciones](#problemas-potenciales-y-soluciones)

---

## Visión General

Este Sprint construye la capa de infraestructura de los módulos KMP de EduGo:
- **kmp-network**: Cliente HTTP basado en Ktor con interceptores y retry
- **kmp-storage**: Almacenamiento key-value con 3 niveles (sync, async, reactive)
- **kmp-config**: Configuración por ambiente con JSON

**Stack target:**
- Kotlin: 2.2.20
- AGP: 8.12.0
- Ktor: 3.1.3
- multiplatform-settings: 1.3.0

**Plataformas:**
- Android (compileSdk=36, minSdk=24)
- Desktop (JVM)
- WASM

**Package base:** `com.edugo.kmp.{modulo}`

---

## Prerequisitos

**IMPORTANTE:** Sprint 1 DEBE estar completado antes de iniciar Sprint 2.

Sprint 1 debe haber creado:
- ✅ `kmp-foundation` - Result, AppError, ErrorCode
- ✅ `kmp-logger` - Sistema de logging multiplataforma
- ✅ `kmp-core` - Extensiones comunes
- ✅ `kmp-validation` - Validadores de entrada

Sprint 2 depende de estos módulos.

---

## Task 2.1: Módulo kmp-network

### Objetivo

Crear módulo HTTP client basado en Ktor con soporte para interceptores, retry automático y serialización JSON. **SIN autenticación** (AuthInterceptor se moverá a Sprint 3: kmp-auth).

### Archivos a crear

**Estructura del módulo:**
```
kmp_new/modules/kmp-network/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/com/edugo/kmp/network/
│   │   ├── EduGoHttpClient.kt
│   │   ├── EduGoHttpClientBuilder.kt
│   │   ├── HttpClientFactory.kt
│   │   ├── HttpRequestConfig.kt
│   │   ├── HttpResponseExtensions.kt
│   │   ├── NetworkException.kt
│   │   ├── ExceptionMapper.kt
│   │   ├── NetworkLogger.kt
│   │   ├── LogSanitizer.kt
│   │   ├── PlatformEngine.kt (expect)
│   │   ├── interceptor/
│   │   │   ├── Interceptor.kt
│   │   │   ├── HeadersInterceptor.kt
│   │   │   ├── LoggingInterceptor.kt
│   │   │   └── TokenProvider.kt (interface para Sprint 3)
│   │   └── retry/
│   │       └── RetryConfig.kt
│   ├── androidMain/kotlin/com/edugo/kmp/network/
│   │   └── PlatformEngine.android.kt (actual)
│   ├── desktopMain/kotlin/com/edugo/kmp/network/
│   │   └── PlatformEngine.jvm.kt (actual)
│   ├── wasmJsMain/kotlin/com/edugo/kmp/network/
│   │   └── PlatformEngine.wasm.kt (actual)
│   └── commonTest/kotlin/com/edugo/kmp/network/
│       ├── EduGoHttpClientTest.kt
│       ├── EduGoHttpClientGetTest.kt
│       ├── EduGoHttpClientPostPutPatchTest.kt
│       ├── EduGoHttpClientDeleteTest.kt
│       ├── EduGoHttpClientBuilderTest.kt
│       ├── HttpClientFactoryTest.kt
│       ├── HttpResponseExtensionsTest.kt
│       ├── LogSanitizerTest.kt
│       ├── NetworkExceptionTest.kt
│       ├── interceptor/
│       │   └── InterceptorsTest.kt
│       └── retry/
│           └── RetryConfigTest.kt
```

### Referencia de código fuente

| Archivo Destino | Archivo Origen | Cambios |
|----------------|----------------|---------|
| `EduGoHttpClient.kt` | `/test-module/src/commonMain/.../network/EduGoHttpClient.kt` | Cambiar package |
| `EduGoHttpClientBuilder.kt` | `/test-module/src/commonMain/.../network/EduGoHttpClientBuilder.kt` | Cambiar package |
| `HttpClientFactory.kt` | `/test-module/src/commonMain/.../network/HttpClientFactory.kt` | **ELIMINAR** método `createWithAutoRefresh()` (va a kmp-auth Sprint 3) |
| `HttpRequestConfig.kt` | `/test-module/src/commonMain/.../network/HttpRequestConfig.kt` | Cambiar package |
| `HttpResponseExtensions.kt` | `/test-module/src/commonMain/.../network/HttpResponseExtensions.kt` | Cambiar package |
| `NetworkException.kt` | `/test-module/src/commonMain/.../network/NetworkException.kt` | Cambiar package, depende de ErrorCode de kmp-foundation |
| `ExceptionMapper.kt` | `/test-module/src/commonMain/.../network/ExceptionMapper.kt` | Cambiar package |
| `NetworkLogger.kt` | `/test-module/src/commonMain/.../network/NetworkLogger.kt` | Cambiar package, usar TaggedLogger de kmp-logger |
| `LogSanitizer.kt` | `/test-module/src/commonMain/.../network/LogSanitizer.kt` | Cambiar package |
| `PlatformEngine.kt` (expect) | `/test-module/src/commonMain/.../network/PlatformEngine.kt` | Cambiar package |
| `PlatformEngine.android.kt` | `/test-module/src/androidMain/.../network/PlatformEngine.android.kt` | Cambiar package |
| `PlatformEngine.jvm.kt` | `/test-module/src/desktopMain/.../network/PlatformEngine.jvm.kt` | Cambiar package |
| `PlatformEngine.wasm.kt` | **NUEVO** (basado en js) | Usar `Fetch` engine para WASM |
| `interceptor/Interceptor.kt` | `/test-module/src/commonMain/.../interceptor/Interceptor.kt` | Cambiar package |
| `interceptor/HeadersInterceptor.kt` | `/test-module/src/commonMain/.../interceptor/HeadersInterceptor.kt` | Cambiar package |
| `interceptor/LoggingInterceptor.kt` | `/test-module/src/commonMain/.../interceptor/LoggingInterceptor.kt` | Cambiar package |
| `interceptor/TokenProvider.kt` | `/test-module/src/commonMain/.../interceptor/AuthInterceptor.kt` | **EXTRAER SOLO** la interface `TokenProvider`, **NO** migrar `AuthInterceptor` |
| `retry/RetryConfig.kt` | `/test-module/src/commonMain/.../retry/RetryConfig.kt` | Cambiar package |

**TESTS:**
| Archivo Test Destino | Archivo Test Origen |
|---------------------|---------------------|
| `EduGoHttpClientGetTest.kt` | `/test-module/src/commonTest/.../network/EduGoHttpClientGetTest.kt` |
| `EduGoHttpClientPostPutPatchTest.kt` | `/test-module/src/commonTest/.../network/EduGoHttpClientPostPutPatchTest.kt` |
| `EduGoHttpClientDeleteTest.kt` | `/test-module/src/commonTest/.../network/EduGoHttpClientDeleteTest.kt` |
| `EduGoHttpClientBuilderTest.kt` | `/test-module/src/commonTest/.../network/EduGoHttpClientBuilderTest.kt` |
| `HttpClientFactoryTest.kt` | `/test-module/src/commonTest/.../network/HttpClientFactoryTest.kt` |
| `HttpResponseExtensionsTest.kt` | `/test-module/src/commonTest/.../network/HttpResponseExtensionsTest.kt` |
| `LogSanitizerTest.kt` | `/test-module/src/commonTest/.../network/LogSanitizerTest.kt` |
| `NetworkExceptionTest.kt` | `/test-module/src/commonTest/.../network/NetworkExceptionTest.kt` |
| `InterceptorsTest.kt` | `/test-module/src/commonTest/.../interceptor/InterceptorsTest.kt` |
| `RetryConfigTest.kt` | `/test-module/src/commonTest/.../retry/RetryConfigTest.kt` |

### Cambios de package necesarios

| Package Viejo | Package Nuevo |
|---------------|---------------|
| `com.edugo.test.module.network` | `com.edugo.kmp.network` |
| `com.edugo.test.module.network.interceptor` | `com.edugo.kmp.network.interceptor` |
| `com.edugo.test.module.network.retry` | `com.edugo.kmp.network.retry` |

**Imports a actualizar:**
- `com.edugo.test.module.core.Result` → `com.edugo.kmp.foundation.Result`
- `com.edugo.test.module.core.AppError` → `com.edugo.kmp.foundation.AppError`
- `com.edugo.test.module.core.ErrorCode` → `com.edugo.kmp.foundation.ErrorCode`
- `com.edugo.test.module.platform.TaggedLogger` → `com.edugo.kmp.logger.TaggedLogger`
- `com.edugo.test.module.platform.DefaultLogger` → `com.edugo.kmp.logger.DefaultLogger`
- `com.edugo.test.module.config.JsonConfig` → **CREAR NUEVO** `JsonConfig.kt` en kmp-network

### Refactoring crítico: Desacoplamiento de AuthInterceptor

**PROBLEMA:** El código actual tiene `AuthInterceptor` en el módulo network, pero depende del sistema de autenticación que pertenece a Sprint 3.

**SOLUCIÓN:**

1. **EXTRAER** solo la interface `TokenProvider` de `AuthInterceptor.kt`:
```kotlin
// kmp-network/src/commonMain/kotlin/com/edugo/kmp/network/interceptor/TokenProvider.kt
package com.edugo.kmp.network.interceptor

/**
 * Proveedor de tokens de autenticación.
 *
 * Esta interface permite al módulo kmp-network ser agnóstico
 * de la implementación de autenticación. El módulo kmp-auth
 * (Sprint 3) proporcionará la implementación concreta.
 */
interface TokenProvider {
    /**
     * Obtiene el token actual. Retorna null si no hay sesión.
     */
    suspend fun getToken(): String?

    /**
     * Refresca el token. Retorna nuevo token o null si falla.
     */
    suspend fun refreshToken(): String?

    /**
     * Indica si el token actual ha expirado.
     */
    suspend fun isTokenExpired(): Boolean
}
```

2. **NO MIGRAR** la clase `AuthInterceptor` completa - esta se creará en Sprint 3 (kmp-auth)

3. **ELIMINAR** de `HttpClientFactory.kt` el método `createWithAutoRefresh()` - este se creará en Sprint 3 junto con `TokenRefreshManager`

4. **DOCUMENTAR** en KDoc de `TokenProvider`:
```kotlin
/**
 * @see com.edugo.kmp.auth.interceptor.AuthInterceptor Implementación en kmp-auth
 */
```

**Interceptores que QUEDAN en kmp-network:**
- ✅ `Interceptor` (base interface)
- ✅ `InterceptorChain`
- ✅ `HeadersInterceptor`
- ✅ `LoggingInterceptor`
- ✅ `TokenProvider` (solo interface)

**Interceptores que VAN a kmp-auth (Sprint 3):**
- ❌ `AuthInterceptor` (implementación completa)

### Dependencias del módulo

```kotlin
// kmp-network/build.gradle.kts - dependencies
dependencies {
    // Módulos internos (Sprint 1)
    api(project(":modules:kmp-foundation"))
    api(project(":modules:kmp-logger"))
    implementation(project(":modules:kmp-core"))

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    // Serialización
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // DateTime (para NetworkLogger)
    implementation(libs.kotlinx.datetime)
}
```

**Platform-specific dependencies:**
```kotlin
// androidMain
implementation(libs.ktor.client.okhttp)

// desktopMain
implementation(libs.ktor.client.cio)

// wasmJsMain
implementation(libs.ktor.client.js)  // Usa Fetch API
```

**Testing dependencies:**
```kotlin
// commonTest
implementation(libs.kotlin.test)
implementation(libs.kotlinx.coroutines.test)
implementation(libs.ktor.client.mock)
```

### Código de build.gradle.kts

```kotlin
/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

plugins {
    id("kmp.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.edugo.kmp.network"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Módulos internos
                api(project(":modules:kmp-foundation"))
                api(project(":modules:kmp-logger"))
                implementation(project(":modules:kmp-core"))

                // Ktor Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                // Serialización
                implementation(libs.kotlinx.serialization.json)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // DateTime (para NetworkLogger)
                implementation(libs.kotlinx.datetime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }
    }
}
```

### Platform-specific files

**expect/actual para `PlatformEngine`:**

| Plataforma | Archivo | Engine |
|------------|---------|--------|
| expect (common) | `PlatformEngine.kt` | - |
| Android | `PlatformEngine.android.kt` | `OkHttp` |
| Desktop | `PlatformEngine.jvm.kt` | `CIO` |
| WASM | `PlatformEngine.wasm.kt` | `Js` (Fetch) |

**Código PlatformEngine.wasm.kt (NUEVO):**
```kotlin
package com.edugo.kmp.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

/**
 * WASM implementation using Js engine (Fetch API).
 *
 * Js engine provides:
 * - Browser Fetch API integration
 * - Native async/await compatibility
 * - Zero additional dependencies in browser
 */
public actual fun createPlatformEngine(): HttpClientEngine = Js.create()
```

### Archivo adicional: JsonConfig.kt

**NUEVO archivo necesario** (reemplaza import desde test.module.config):

```kotlin
// kmp-network/src/commonMain/kotlin/com/edugo/kmp/network/JsonConfig.kt
package com.edugo.kmp.network

import kotlinx.serialization.json.Json

/**
 * Configuración JSON para serialización HTTP.
 */
object JsonConfig {
    val Default = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }
}
```

### Tests a migrar/crear

**Tests a migrar (cambiar package):**
- `EduGoHttpClientGetTest.kt`
- `EduGoHttpClientPostPutPatchTest.kt`
- `EduGoHttpClientDeleteTest.kt`
- `EduGoHttpClientBuilderTest.kt`
- `HttpClientFactoryTest.kt` - **ELIMINAR** tests de `createWithAutoRefresh()`
- `HttpResponseExtensionsTest.kt`
- `LogSanitizerTest.kt`
- `NetworkExceptionTest.kt`
- `InterceptorsTest.kt` - **ELIMINAR** tests de `AuthInterceptor`
- `RetryConfigTest.kt`

**Platform-specific tests** (si existen en test-module):
- `PlatformEngineAndroidTest.kt`
- `PlatformEngineJvmTest.kt`

### Verificación

```bash
# Compilación
./gradlew :modules:kmp-network:compileKotlinAndroid
./gradlew :modules:kmp-network:compileKotlinDesktop
./gradlew :modules:kmp-network:compileKotlinWasmJs

# Tests
./gradlew :modules:kmp-network:test
./gradlew :modules:kmp-network:androidUnitTest
./gradlew :modules:kmp-network:desktopTest

# Verificar exports
./gradlew :modules:kmp-network:build
```

**Checklist funcional:**
- [ ] EduGoHttpClient realiza GET/POST/PUT/PATCH/DELETE
- [ ] Interceptores se ejecutan en orden correcto
- [ ] RetryConfig aplica backoff exponencial
- [ ] NetworkLogger sanitiza tokens y passwords
- [ ] Cada plataforma usa su engine correcto (OkHttp/CIO/Js)
- [ ] TokenProvider interface está disponible para Sprint 3

---

## Task 2.2: Modulo kmp-storage

### Objetivo
Migrar almacenamiento key-value multiplataforma con 3 niveles (sync, async, reactive). Total: ~2,299 lineas.

### build.gradle.kts
```kotlin
plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.storage"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
                implementation(project(":modules:logger"))
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.coroutines)
                implementation(libs.multiplatform.settings.make.observable)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
```

### Mapa de archivos

**Origen:** `test-module/src/commonMain/kotlin/com/edugo/test/module/storage/`
**Destino:** `modules/storage/src/commonMain/kotlin/com/edugo/kmp/storage/`

| Origen | Destino | Lineas |
|--------|---------|--------|
| `EduGoStorage.kt` | `EduGoStorage.kt` | 170 |
| `AsyncEduGoStorage.kt` | `AsyncEduGoStorage.kt` | 173 |
| `StateFlowStorage.kt` | `StateFlowStorage.kt` | 213 |
| `SafeEduGoStorage.kt` | `SafeEduGoStorage.kt` | 404 |
| `StorageDelegate.kt` | `StorageDelegate.kt` | 244 |
| `StorageFlow.kt` | `StorageFlow.kt` | 269 |
| `StorageSerializationExtensions.kt` | `StorageSerializationExtensions.kt` | 332 |
| `AsyncStorageSerializationExtensions.kt` | `AsyncStorageSerializationExtensions.kt` | 159 |
| `StorageKeyValidator.kt` | `StorageKeyValidator.kt` | 114 |
| `StorageException.kt` | `StorageException.kt` | 199 |
| `PlatformSettings.kt` | `PlatformSettings.kt` | 22 |

### Cambios de package
| Buscar | Reemplazar |
|--------|-----------|
| `package com.edugo.test.module.storage` | `package com.edugo.kmp.storage` |
| `import com.edugo.test.module.core.*` | `import com.edugo.kmp.foundation.*` (ajustar subpackages) |
| `import com.edugo.test.module.platform.*` | `import com.edugo.kmp.logger.*` |
| `import com.edugo.test.module.config.JsonConfig` | `import com.edugo.kmp.foundation.serialization.JsonConfig` |

### Platform-specific
PlatformSettings.kt tiene `expect fun createSettings()`. Buscar actuals:
```bash
find test-module/src/*/kotlin -path "*/storage/*" -name "*.kt"
```

### Tests a migrar

| Test origen | Test destino |
|-------------|-------------|
| `storage/EduGoStorageTest.kt` | `EduGoStorageTest.kt` |
| `storage/AsyncEduGoStorageTest.kt` | `AsyncEduGoStorageTest.kt` |
| `storage/StateFlowStorageTest.kt` | `StateFlowStorageTest.kt` |
| `storage/SafeEduGoStorageTest.kt` | `SafeEduGoStorageTest.kt` |
| `storage/StorageDelegateTest.kt` | `StorageDelegateTest.kt` |
| `storage/StorageFlowTest.kt` | `StorageFlowTest.kt` |
| `storage/StorageSerializationTest.kt` | `StorageSerializationTest.kt` |
| `storage/StorageCollectionsTest.kt` | `StorageCollectionsTest.kt` |
| `storage/StorageKeyValidatorTest.kt` | `StorageKeyValidatorTest.kt` |

### Verificacion
```bash
./gradlew :modules:storage:desktopTest
./gradlew :modules:storage:androidUnitTest
./gradlew :modules:storage:wasmJsTest
```

---

## Task 2.3: Modulo kmp-config

### Objetivo
Migrar configuracion por ambiente (dev/staging/prod) desde Template-Kmp-Clean.

### build.gradle.kts
```kotlin
plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.config"
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

### Referencia de codigo fuente

**Origen:** `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/`
Buscar archivos con:
```bash
find /Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean -path "*/config/*" -name "*.kt"
```

**Archivos a migrar de Template:**

| Archivo Template | Destino | Descripcion |
|-----------------|---------|-------------|
| `core/config/.../AppConfig.kt` | `config/AppConfig.kt` | Data class con apiUrl, port, timeout, debugMode |
| `core/config/.../Environment.kt` | `config/Environment.kt` | Enum DEV, STAGING, PROD |
| `core/config/.../ConfigLoader.kt` | `config/ConfigLoader.kt` | Carga JSON por ambiente |
| `core/config/.../ResourceLoader.kt` (expect) | `config/ResourceLoader.kt` | expect fun para leer JSON |
| Platform actuals de ResourceLoader | `androidMain/`, `desktopMain/`, `wasmJsMain/` | Cargar JSON por plataforma |

**Destino base:** `modules/config/src/commonMain/kotlin/com/edugo/kmp/config/`

### Cambios de package
| Buscar | Reemplazar |
|--------|-----------|
| Package de Template (buscar en archivos) | `package com.edugo.kmp.config` |

### JSON configs a crear
Crear en `modules/config/src/commonMain/resources/`:
```
config/
├── dev.json
├── staging.json
└── prod.json
```

Ejemplo `dev.json`:
```json
{
  "environment": "DEV",
  "apiUrl": "http://localhost",
  "apiPort": 8080,
  "timeout": 30000,
  "debugMode": true
}
```

### Verificacion
```bash
./gradlew :modules:config:desktopTest
./gradlew :modules:config:androidUnitTest
```

---

## Actualizar settings.gradle.kts

Agregar los nuevos modulos:
```kotlin
// Sprint 2 modules
include(":modules:network")
include(":modules:storage")
include(":modules:config")
```

---

## Diagrama de Dependencias Sprint 2

```
Sprint 1:  [foundation] [logger] [core] [validation]
               ↓           ↓       ↓
Sprint 2:  [network] ←─────┘───────┘
           [storage] ←── [foundation] + [logger]
           [config]  ←── [foundation]
```

Los 3 modulos de Sprint 2 son INDEPENDIENTES entre si (se pueden hacer en paralelo).

---

## Orden de Ejecucion

1. Actualizar settings.gradle.kts con nuevos modulos
2. **En paralelo:**
   - Task 2.1 - Network (~4h) - el mas grande
   - Task 2.2 - Storage (~3h)
   - Task 2.3 - Config (~1h)

**Total estimado:** 4-5 horas (paralelo), 8h (secuencial)

---

## Checklist de Verificacion Global Sprint 2

```bash
# Sprint 1 + 2 completos
./gradlew test

# Cada modulo independiente
./gradlew :modules:network:build
./gradlew :modules:storage:build
./gradlew :modules:config:build

# Verificar que network NO depende de auth
./gradlew :modules:network:dependencies --configuration commonMainImplementation
# NO debe aparecer "kmp-auth" en la salida
```

### Criterios
- [ ] Network compila SIN AuthInterceptor
- [ ] TokenProvider interface existe en network
- [ ] Storage funciona con multiplatform-settings
- [ ] Config carga JSON por ambiente
- [ ] Tests pasan en todas las plataformas
- [ ] Cero dependencias circulares

---

## Problemas Potenciales

### 1. AuthInterceptor importado accidentalmente en network
**Sintoma:** Import error a clases de auth
**Solucion:** Verificar que NO se copien archivos de auth. Solo copiar TokenProvider interface.

### 2. multiplatform-settings no compatible con WASM
**Sintoma:** Compilation error en wasmJsMain
**Solucion:** Verificar version 1.3.0+ de multiplatform-settings que tiene soporte WASM.

### 3. Ktor engine incorrecto por plataforma
**Sintoma:** Runtime crash en una plataforma especifica
**Solucion:** Usar expect/actual para PlatformEngine. Android=OkHttp, Desktop=CIO, WASM=Js.

### 4. Tests de network necesitan MockEngine
**Sintoma:** Tests fallan por conexion real
**Solucion:** Usar `ktor-client-mock` en commonTest. NO hacer requests reales en tests.
