# Fase 2: Build-time Configuration (OPCIONAL)

## Objetivo de la Fase

Mejorar la precisión de la detección de ambiente mediante configuración inyectada en tiempo de compilación, permitiendo diferentes builds con diferentes configuraciones sin modificar código.

## Tiempo Estimado

**3-4 horas** (según experiencia con build systems)

**Desglose**:
- Android BuildConfig: 1 hora
- iOS Info.plist + Xcode schemes: 1.5 horas
- Desktop gradle.properties: 0.5 horas
- WasmJS hostname detection: 1 hora

## Prerequisitos

- ✅ **Fase 1 completada** y funcionando
- ✅ Tests de Fase 1 pasando
- ✅ Familiaridad con Gradle build system
- ✅ Xcode instalado (para iOS)
- ✅ Acceso a configuración de build de cada plataforma

---

## ¿Por Qué Implementar Fase 2?

### Ventajas sobre Fase 1

| Aspecto | Fase 1 (Runtime) | Fase 2 (Build-time) |
|---------|------------------|---------------------|
| **Detección** | Heurística (Platform.isDebug) | Configuración explícita en build |
| **Precisión** | ~90% | 100% |
| **Seguridad** | Posible confusión debug/release | Garantizado por build |
| **Flexibilidad** | Override manual en código | Diferentes builds físicos |
| **Tamaño binario** | Todas las configs empaquetadas | Solo config necesaria |

### Cuándo Implementar Fase 2

✅ **Implementar si**:
- Necesitas 100% certeza de ambiente en cada build
- Quieres separar físicamente configs DEV/STAGING/PROD
- Tienes múltiples entornos de testing
- Necesitas diferentes builds simultáneos

❌ **NO implementar si**:
- Fase 1 es suficiente para tu caso de uso
- Equipo pequeño sin múltiples entornos
- No tienes tiempo para configurar builds

---

## Arquitectura Fase 2

### Flujo Mejorado

```
Build System (Gradle/Xcode)
    │
    ├─► Android: gradle buildTypes + buildConfigField
    ├─► iOS: Xcode schemes + Info.plist
    ├─► Desktop: gradle.properties + System.getProperty()
    └─► WasmJS: NODE_ENV + webpack DefinePlugin
    │
    ▼
Build-time Injection
    │
    ├─► BuildConfig.ENVIRONMENT = "STAGING"  (Android)
    ├─► Info.plist["AppEnvironment"] = "PROD"  (iOS)
    ├─► System.getProperty("app.environment")  (Desktop)
    └─► process.env.NODE_ENV = "production"  (WasmJS)
    │
    ▼
EnvironmentDetector.detectPlatformEnvironment()
    │
    └─► Lee valor inyectado, no usa heurística
```

---

## Tareas de Implementación

### Tarea 2.1: Android - BuildConfig

#### 2.1.1: Configurar buildTypes en build.gradle.kts

**Archivo**: `platforms/mobile/app/build.gradle.kts`

```kotlin
android {
    namespace = "com.edugo.kmp"
    
    buildTypes {
        debug {
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        
        release {
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Opcional: Product flavors para múltiples entornos
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
        }
        
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "ENVIRONMENT", "\"STAGING\"")
        }
        
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
        }
    }
    
    buildFeatures {
        buildConfig = true  // Habilitar BuildConfig
    }
}
```

#### 2.1.2: Actualizar EnvironmentDetector.android.kt

**Archivo**: `modules/config/src/androidMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.android.kt`

```kotlin
package com.edugo.kmp.config

import com.edugo.kmp.core.platform.Platform

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check BuildConfig (if available)
    try {
        // BuildConfig está en el package de la app, no del módulo
        // Usamos reflection para accederlo
        val buildConfigClass = Class.forName("com.edugo.kmp.BuildConfig")
        val envField = buildConfigClass.getField("ENVIRONMENT")
        val envValue = envField.get(null) as? String
        
        if (envValue != null) {
            return Environment.fromString(envValue) ?: Environment.DEV
        }
    } catch (e: Exception) {
        // BuildConfig no disponible, continuar con estrategias alternativas
    }
    
    // Strategy 2: Check if running in debug mode (fallback)
    if (Platform.isDebug) {
        return Environment.DEV
    }

    // Strategy 3: Check system property (para override manual)
    val envProperty = System.getProperty("app.environment")
    if (envProperty != null) {
        return Environment.fromString(envProperty) ?: Environment.PROD
    }

    // Strategy 4: Default to PROD for release builds
    return Environment.PROD
}
```

**Validación**:
```bash
# Build debug
./gradlew :platforms:mobile:app:assembleDebug
# Instalar y verificar que usa DEV

# Build release
./gradlew :platforms:mobile:app:assembleRelease
# Instalar y verificar que usa PROD

# Build staging (si usas flavors)
./gradlew :platforms:mobile:app:assembleStagingDebug
# Instalar y verificar que usa STAGING
```

---

### Tarea 2.2: iOS - Info.plist + Xcode Schemes

#### 2.2.1: Crear esquemas de Xcode

1. Abrir proyecto en Xcode
2. Product → Scheme → Manage Schemes
3. Duplicar esquema existente 3 veces:
   - `EduGo-Dev`
   - `EduGo-Staging`
   - `EduGo-Prod`

#### 2.2.2: Configurar Info.plist por esquema

**Opción A: Usar User-Defined Settings**

1. En Xcode, seleccionar proyecto
2. Build Settings → Add User-Defined Setting
3. Crear variable `APP_ENVIRONMENT`
4. Configurar por esquema:
   - Debug: `DEV`
   - Staging: `STAGING`
   - Release: `PROD`

**Opción B: Usar archivos .xcconfig**

Crear archivos de configuración:

**`iosApp/Configuration/Dev.xcconfig`**:
```
APP_ENVIRONMENT = DEV
PRODUCT_BUNDLE_IDENTIFIER = com.edugo.kmp.dev
```

**`iosApp/Configuration/Staging.xcconfig`**:
```
APP_ENVIRONMENT = STAGING
PRODUCT_BUNDLE_IDENTIFIER = com.edugo.kmp.staging
```

**`iosApp/Configuration/Prod.xcconfig`**:
```
APP_ENVIRONMENT = PROD
PRODUCT_BUNDLE_IDENTIFIER = com.edugo.kmp
```

#### 2.2.3: Actualizar Info.plist

Agregar clave `AppEnvironment`:

```xml
<key>AppEnvironment</key>
<string>$(APP_ENVIRONMENT)</string>
```

#### 2.2.4: Actualizar EnvironmentDetector.ios.kt

**Archivo**: `modules/config/src/iosMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.ios.kt`

```kotlin
package com.edugo.kmp.config

import com.edugo.kmp.core.platform.Platform
import platform.Foundation.NSBundle

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Read from Info.plist
    try {
        val infoDictionary = NSBundle.mainBundle.infoDictionary
        val envValue = infoDictionary?.get("AppEnvironment") as? String
        
        if (envValue != null) {
            return Environment.fromString(envValue) ?: Environment.DEV
        }
    } catch (e: Exception) {
        // Info.plist key not found, continue with fallback
    }
    
    // Strategy 2: Check if running in debug mode (fallback)
    if (Platform.isDebug) {
        return Environment.DEV
    }
    
    // Strategy 3: Conservative default
    return Environment.DEV
}
```

**Validación**:
```bash
# Build con esquema Dev
xcodebuild -scheme EduGo-Dev -configuration Debug
# Verificar que usa DEV

# Build con esquema Prod
xcodebuild -scheme EduGo-Prod -configuration Release
# Verificar que usa PROD
```

---

### Tarea 2.3: Desktop - gradle.properties

#### 2.3.1: Crear archivo gradle.properties por ambiente

**`gradle.properties`** (default - DEV):
```properties
app.environment=DEV
```

**`gradle-staging.properties`**:
```properties
app.environment=STAGING
```

**`gradle-prod.properties`**:
```properties
app.environment=PROD
```

#### 2.3.2: Configurar build.gradle.kts para leer property

**Archivo**: `platforms/desktop/app/build.gradle.kts`

```kotlin
val appEnvironment: String by project  // Lee de gradle.properties

application {
    mainClass.set("com.edugo.kmp.MainKt")
    
    applicationDefaultJvmArgs = listOf(
        "-Dapp.environment=$appEnvironment"
    )
}

tasks.named<JavaExec>("run") {
    systemProperty("app.environment", appEnvironment)
}
```

#### 2.3.3: EnvironmentDetector.desktop.kt ya está listo

El código actual ya lee `System.getProperty("app.environment")`, no requiere cambios.

**Validación**:
```bash
# Run con DEV (default)
./gradlew :platforms:desktop:app:run

# Run con STAGING
./gradlew :platforms:desktop:app:run -Papp.environment=STAGING

# Run con PROD
./gradlew :platforms:desktop:app:run -Papp.environment=PROD
```

---

### Tarea 2.4: WasmJS - Hostname Detection

#### 2.4.1: Actualizar EnvironmentDetector.wasmJs.kt

**Archivo**: `modules/config/src/wasmJsMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.wasmJs.kt`

```kotlin
package com.edugo.kmp.config

import kotlinx.browser.window

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check NODE_ENV (if running in Node.js context)
    try {
        val nodeEnv = js("process.env.NODE_ENV") as? String
        if (nodeEnv != null) {
            return when (nodeEnv.lowercase()) {
                "development" -> Environment.DEV
                "staging" -> Environment.STAGING
                "production" -> Environment.PROD
                else -> Environment.DEV
            }
        }
    } catch (e: Exception) {
        // Not in Node.js, continue with browser detection
    }
    
    // Strategy 2: Check hostname (browser environment)
    val hostname = window.location.hostname
    
    return when {
        hostname == "localhost" || hostname == "127.0.0.1" -> Environment.DEV
        hostname.contains("staging", ignoreCase = true) -> Environment.STAGING
        hostname.contains("dev", ignoreCase = true) -> Environment.DEV
        else -> Environment.PROD
    }
}
```

#### 2.4.2: Configurar webpack (si aplica)

Si usas webpack para build:

**`webpack.config.js`**:
```javascript
const webpack = require('webpack');

module.exports = {
  plugins: [
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development')
    })
  ]
};
```

**Validación**:
```bash
# Development
NODE_ENV=development ./gradlew :platforms:web:app:wasmJsBrowserDevelopmentRun

# Production
NODE_ENV=production ./gradlew :platforms:web:app:wasmJsBrowserProductionRun
```

---

## Ventajas de Cada Plataforma

### Android

**Con buildTypes**:
- ✅ `debugImplementation` vs `releaseImplementation` para diferentes dependencias
- ✅ ProGuard solo en release
- ✅ Logging diferente por build
- ✅ Íconos diferentes por flavor

**Con productFlavors**:
- ✅ Múltiples apps instaladas simultáneamente (dev, staging, prod)
- ✅ Bundle IDs diferentes
- ✅ Configuraciones completamente separadas

---

### iOS

**Con Xcode Schemes**:
- ✅ Run/Test/Profile con diferentes configuraciones
- ✅ Bundle IDs diferentes (dev, staging, prod)
- ✅ Certificados diferentes por ambiente
- ✅ Push notifications diferentes

---

### Desktop

**Con gradle.properties**:
- ✅ Diferentes distribuciones empaquetadas
- ✅ CI/CD fácil: `-Papp.environment=PROD`
- ✅ Desarrollo local siempre DEV

---

### WasmJS

**Con hostname detection**:
- ✅ Detección automática según dominio
- ✅ No requiere rebuild para cambiar ambiente
- ✅ localhost → DEV automático
- ✅ Deploy a staging.example.com → STAGING automático

---

## Testing Fase 2

### Test Android con Flavors

```bash
# Install dev flavor
./gradlew :platforms:mobile:app:installDevDebug
adb shell am start -n com.edugo.kmp.dev/.MainActivity
adb logcat | grep "Environment"
# Esperado: Environment detected: DEV

# Install staging flavor
./gradlew :platforms:mobile:app:installStagingDebug
adb shell am start -n com.edugo.kmp.staging/.MainActivity
adb logcat | grep "Environment"
# Esperado: Environment detected: STAGING

# Install prod flavor
./gradlew :platforms:mobile:app:installProdRelease
adb shell am start -n com.edugo.kmp/.MainActivity
adb logcat | grep "Environment"
# Esperado: Environment detected: PROD
```

### Test iOS con Schemes

```bash
# Build y run con Xcode
# Seleccionar scheme "EduGo-Dev" → Run
# Verificar logs: Environment detected: DEV

# Seleccionar scheme "EduGo-Staging" → Run
# Verificar logs: Environment detected: STAGING

# Seleccionar scheme "EduGo-Prod" → Run
# Verificar logs: Environment detected: PROD
```

### Test Desktop con Properties

```bash
# DEV
./gradlew :platforms:desktop:app:run
# Verificar: Environment detected: DEV

# STAGING
./gradlew :platforms:desktop:app:run -Papp.environment=STAGING
# Verificar: Environment detected: STAGING

# PROD
./gradlew :platforms:desktop:app:run -Papp.environment=PROD
# Verificar: Environment detected: PROD
```

---

## Criterios de Aceptación - Fase 2

### Android

- ✅ BuildConfig.ENVIRONMENT refleja build actual
- ✅ Debug builds → DEV automático
- ✅ Release builds → PROD automático
- ✅ Flavors permiten instalar múltiples versiones
- ✅ Sin hardcoding de ambiente

### iOS

- ✅ Info.plist contiene AppEnvironment
- ✅ Cada scheme usa ambiente correcto
- ✅ Bundle IDs diferentes por ambiente
- ✅ Sin confusión entre builds

### Desktop

- ✅ gradle.properties controla ambiente
- ✅ CI/CD puede pasar `-Papp.environment=PROD`
- ✅ Default a DEV para desarrollo local

### WasmJS

- ✅ Hostname detection funciona
- ✅ localhost → DEV
- ✅ staging.example.com → STAGING
- ✅ example.com → PROD

---

## Problemas Comunes

### Android: BuildConfig no generado

**Síntomas**: `Class.forName("com.edugo.kmp.BuildConfig")` falla

**Solución**:
```kotlin
android {
    buildFeatures {
        buildConfig = true  // ← Asegurar que esté habilitado
    }
}
```

### iOS: Info.plist no encuentra AppEnvironment

**Síntomas**: `infoDictionary?.get("AppEnvironment")` retorna null

**Solución**: Verificar que la clave exista en Info.plist y que use `$(APP_ENVIRONMENT)`

### Desktop: Property no se pasa a JVM

**Síntomas**: `System.getProperty("app.environment")` retorna null

**Solución**: Verificar `applicationDefaultJvmArgs` en `build.gradle.kts`

---

## Migración desde Fase 1

### Cambios Necesarios

1. **Android**: Agregar buildTypes/flavors en `build.gradle.kts`
2. **iOS**: Crear schemes y configurar Info.plist
3. **Desktop**: Agregar gradle.properties
4. **WasmJS**: Actualizar detection logic

### Compatibilidad

✅ **Fase 2 es 100% compatible con Fase 1**:
- Si build-time config no está disponible, usa runtime detection (Fase 1)
- Override manual sigue funcionando
- Tests no requieren cambios

---

## Siguiente Paso

Fase 2 completa el sistema de configuración multi-ambiente. **Fase 3** (Remote Config) es completamente opcional y solo necesaria si requieres:
- Actualización de configuración sin rebuild
- Feature flags remotos
- A/B testing

---

**Fin de Fase 2** ✅
