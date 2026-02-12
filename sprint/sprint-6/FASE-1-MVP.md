# Fase 1: MVP - Detección Automática y Archivos Externos

## Objetivo de la Fase

Eliminar completamente las URLs hardcodeadas del código fuente e implementar un sistema de detección automática de ambiente que funcione en todas las plataformas (Android, Desktop, iOS, WasmJS).

## Tiempo Estimado

**4-6 horas** (según experiencia con KMP y expect/actual)

**Desglose**:
- Creación de EnvironmentDetector: 1 hora
- Implementaciones platform-specific: 1.5 horas
- Archivos JSON y ResourceLoader: 1 hora
- AndroidContextHolder: 0.5 horas
- Tests: 1 hora
- Validación y troubleshooting: 1 hora

## Prerequisitos

Antes de comenzar, verifica:

- ✅ Gradle 8.0+ instalado
- ✅ Kotlin 1.9.0+ configurado
- ✅ Módulos `foundation` y `core` compilando correctamente
- ✅ Familiaridad con patrón expect/actual de Kotlin Multiplatform
- ✅ Android Studio o IntelliJ IDEA configurado

---

## Tareas de Implementación

### Tarea 1.1: Crear EnvironmentDetector (commonMain)

**Archivo**: `modules/config/src/commonMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.kt`

```kotlin
package com.edugo.kmp.config

/**
 * Detects the current environment (DEV, STAGING, PROD) automatically
 * based on platform-specific heuristics.
 *
 * Supports manual override for testing via [override].
 */
public object EnvironmentDetector {
    private var manualOverride: Environment? = null

    /**
     * Detects the current environment automatically.
     *
     * If [override] was called, returns the overridden value.
     * Otherwise, delegates to platform-specific implementation.
     *
     * @return The detected or overridden environment
     */
    public fun detect(): Environment {
        return manualOverride ?: detectPlatformEnvironment()
    }

    /**
     * Forces a specific environment (useful for testing).
     *
     * @param environment The environment to force
     */
    public fun override(environment: Environment) {
        manualOverride = environment
    }

    /**
     * Clears manual override, restoring automatic detection.
     */
    public fun reset() {
        manualOverride = null
    }
}

/**
 * Platform-specific detection implementation.
 * Implemented via expect/actual pattern.
 */
internal expect fun detectPlatformEnvironment(): Environment
```

**Validación**:
- ✅ Archivo compila sin errores
- ✅ Métodos públicos: `detect()`, `override()`, `reset()`
- ✅ Función `expect` declarada correctamente

---

### Tarea 1.2: Implementar EnvironmentDetector para Android

**Archivo**: `modules/config/src/androidMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.android.kt`

```kotlin
package com.edugo.kmp.config

import com.edugo.kmp.core.platform.Platform

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check if running in debug mode
    if (Platform.isDebug) {
        return Environment.DEV
    }

    // Strategy 2: Check system property (for manual override)
    val envProperty = System.getProperty("app.environment")
    if (envProperty != null) {
        return Environment.fromString(envProperty) ?: Environment.PROD
    }

    // Strategy 3: Default to PROD for release builds
    return Environment.PROD
}
```

**Validación**:
- ✅ Usa `Platform.isDebug` existente
- ✅ Soporta override via `-Dapp.environment=STAGING`
- ✅ Fallback seguro a PROD

**Comando de prueba**:
```bash
./gradlew :modules:config:compileDebugKotlinAndroid
```

---

### Tarea 1.3: Implementar EnvironmentDetector para Desktop

**Archivo**: `modules/config/src/desktopMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.desktop.kt`

```kotlin
package com.edugo.kmp.config

import com.edugo.kmp.core.platform.Platform

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check if debugger is attached (Platform.isDebug already does this)
    if (Platform.isDebug) {
        return Environment.DEV
    }

    // Strategy 2: Check environment variable
    val envVar = System.getenv("APP_ENVIRONMENT")
    if (envVar != null) {
        return Environment.fromString(envVar) ?: Environment.PROD
    }

    // Strategy 3: Default to PROD
    return Environment.PROD
}
```

**Validación**:
- ✅ Debugger detection funciona (ya probado en `Platform.jvm.kt`)
- ✅ Soporta `export APP_ENVIRONMENT=STAGING`
- ✅ Fallback seguro a PROD

**Comando de prueba**:
```bash
./gradlew :modules:config:compileKotlinDesktop
```

---

### Tarea 1.4: Implementar EnvironmentDetector para iOS

**Archivo**: `modules/config/src/iosMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.ios.kt`

```kotlin
package com.edugo.kmp.config

import com.edugo.kmp.core.platform.Platform

internal actual fun detectPlatformEnvironment(): Environment {
    // Conservative approach: Default to DEV to avoid accidental PROD calls
    // Rationale: iOS debug detection is less reliable from Kotlin
    // Phase 2 will add Info.plist support for proper PROD detection
    
    if (Platform.isDebug) {
        return Environment.DEV
    }
    
    // TODO Phase 2: Read from Info.plist
    // val plistEnv = readFromInfoPlist("AppEnvironment")
    // return Environment.fromString(plistEnv) ?: Environment.DEV
    
    return Environment.DEV // Conservative default
}
```

**Validación**:
- ✅ Conservador: evita llamadas a PROD accidentales
- ✅ TODO claro para Fase 2
- ✅ Compila sin errores

**Comando de prueba**:
```bash
./gradlew :modules:config:compileKotlinIosX64
```

---

### Tarea 1.5: Implementar EnvironmentDetector para WasmJS

**Archivo**: `modules/config/src/wasmJsMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.wasmJs.kt`

```kotlin
package com.edugo.kmp.config

internal actual fun detectPlatformEnvironment(): Environment {
    // WasmJS: Conservative default to DEV
    // In production, detection can be improved by:
    // 1. Reading from environment variables set during build
    // 2. Using window.location.hostname (requires proper browser API access)
    // 3. Loading from external config file via fetch API
    
    // For Phase 1, default to DEV (safe for development)
    // Phase 2 will add proper hostname-based detection
    return Environment.DEV
}
```

**Validación**:
- ✅ Default seguro a DEV
- ✅ Compila sin errores
- ✅ Documentado para Fase 2

**Comando de prueba**:
```bash
./gradlew :modules:config:compileKotlinWasmJs
```

---

### Tarea 1.6: Agregar fromString() a Environment

**Archivo**: `modules/config/src/commonMain/kotlin/com/edugo/kmp/config/Environment.kt`

**Modificación**: Agregar companion object al enum existente

```kotlin
public enum class Environment {
    DEV,
    STAGING,
    PROD;

    public val fileName: String
        get() = name.lowercase()

    public companion object {
        /**
         * Obtiene el ambiente desde un string.
         * Retorna null si el string no corresponde a ningún ambiente válido.
         *
         * @param value String del ambiente (case-insensitive)
         * @return Environment correspondiente o null si no se encuentra
         */
        public fun fromString(value: String?): Environment? {
            if (value == null) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        /**
         * Obtiene el ambiente desde un string con un fallback.
         *
         * @param value String del ambiente (case-insensitive)
         * @param default Ambiente a retornar si value no es válido (por defecto DEV)
         * @return Environment correspondiente o el default
         */
        public fun fromStringOrDefault(value: String?, default: Environment = DEV): Environment {
            return fromString(value) ?: default
        }
    }
}
```

**Validación**:
- ✅ `Environment.fromString("dev")` → `Environment.DEV`
- ✅ `Environment.fromString("STAGING")` → `Environment.STAGING`
- ✅ `Environment.fromString("invalid")` → `null`
- ✅ `Environment.fromStringOrDefault("invalid")` → `Environment.DEV`

---

### Tarea 1.7: Crear archivos JSON externos

**Directorio**: `modules/config/src/commonMain/resources/config/`

**Crear 3 archivos**:

#### dev.json
```json
{
  "environmentName": "DEV",
  "apiUrl": "http://localhost",
  "apiPort": 8080,
  "webPort": 8080,
  "timeout": 30000,
  "debugMode": true
}
```

#### staging.json
```json
{
  "environmentName": "STAGING",
  "apiUrl": "https://api-staging.example.com",
  "apiPort": 443,
  "webPort": 8080,
  "timeout": 60000,
  "debugMode": true
}
```

#### prod.json
```json
{
  "environmentName": "PROD",
  "apiUrl": "https://api.example.com",
  "apiPort": 443,
  "webPort": 80,
  "timeout": 60000,
  "debugMode": false
}
```

**Validación**:
- ✅ Archivos válidos JSON (validar con jsonlint.com)
- ✅ Estructura coincide con `AppConfigImpl`
- ✅ Ubicación correcta en `resources/`

**Comandos**:
```bash
# Crear directorio
mkdir -p modules/config/src/commonMain/resources/config

# Validar JSON
cat modules/config/src/commonMain/resources/config/dev.json | python -m json.tool
```

---

### Tarea 1.8: Crear AndroidContextHolder

**Archivo**: `modules/config/src/androidMain/kotlin/com/edugo/kmp/config/AndroidContextHolder.kt`

```kotlin
package com.edugo.kmp.config

import android.content.Context

/**
 * Holds the Android application context for accessing resources.
 * Must be initialized before loading config.
 *
 * Usage:
 * ```
 * // In MainActivity.onCreate() or Application.onCreate()
 * AndroidContextHolder.init(applicationContext)
 * ```
 */
public object AndroidContextHolder {
    private var context: Context? = null

    /**
     * Initializes the holder with the application context.
     * This should be called as early as possible in the app lifecycle.
     *
     * @param appContext The application context (use applicationContext, not activity context)
     */
    public fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    /**
     * Gets the stored context.
     * Returns null if not initialized yet.
     */
    internal fun get(): Context? = context
}
```

**Validación**:
- ✅ Object es singleton
- ✅ Almacena ApplicationContext (no Activity)
- ✅ Método `get()` es internal

---

### Tarea 1.9: Refactorizar ResourceLoader.android.kt

**Archivo**: `modules/config/src/androidMain/kotlin/com/edugo/kmp/config/ResourceLoader.android.kt`

**Reemplazar contenido completo**:

```kotlin
package com.edugo.kmp.config

/**
 * Implementación Android para cargar recursos.
 * Primero intenta cargar desde Android assets, si falla usa configuración hardcodeada como fallback.
 */
internal actual fun loadResourceAsString(path: String): String? {
    // Try to load from Android assets first
    val context = AndroidContextHolder.get()
    if (context != null) {
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // File not found in assets, fall back to hardcoded
            null
        }
    }

    // Fallback to hardcoded config (for unit tests or uninitialized context)
    return getHardcodedConfig(path)
}

/**
 * Configuraciones hardcodeadas como fallback.
 * Se usan cuando AndroidContextHolder no está inicializado (ej: unit tests)
 */
private fun getHardcodedConfig(path: String): String? {
    return when (path) {
        "config/dev.json" -> """
            {
              "environmentName": "DEV",
              "apiUrl": "http://localhost",
              "apiPort": 8080,
              "webPort": 8080,
              "timeout": 30000,
              "debugMode": true
            }
        """.trimIndent()
        "config/staging.json" -> """
            {
              "environmentName": "STAGING",
              "apiUrl": "https://api-staging.example.com",
              "apiPort": 443,
              "webPort": 8080,
              "timeout": 60000,
              "debugMode": true
            }
        """.trimIndent()
        "config/prod.json" -> """
            {
              "environmentName": "PROD",
              "apiUrl": "https://api.example.com",
              "apiPort": 443,
              "webPort": 80,
              "timeout": 60000,
              "debugMode": false
            }
        """.trimIndent()
        else -> null
    }
}
```

**Validación**:
- ✅ Carga desde assets si Context disponible
- ✅ Fallback a hardcoded para tests
- ✅ Sin errores de compilación

---

### Tarea 1.10: Refactorizar ResourceLoader.desktop.kt

**Archivo**: `modules/config/src/desktopMain/kotlin/com/edugo/kmp/config/ResourceLoader.desktop.kt`

**Reemplazar contenido completo**:

```kotlin
package com.edugo.kmp.config

/**
 * Implementación Desktop para cargar recursos.
 * Primero intenta cargar desde classpath resources, si falla usa configuración hardcodeada como fallback.
 */
internal actual fun loadResourceAsString(path: String): String? {
    // Try to load from classpath resources first
    return try {
        val stream = object {}.javaClass.classLoader?.getResourceAsStream(path)
        stream?.bufferedReader()?.use { it.readText() }
    } catch (e: Exception) {
        null
    } ?: getHardcodedConfig(path)
}

/**
 * Configuraciones hardcodeadas como fallback.
 * Se usan cuando los archivos no están disponibles en el classpath (ej: unit tests)
 */
private fun getHardcodedConfig(path: String): String? {
    return when (path) {
        "config/dev.json" -> """
            {
              "environmentName": "DEV",
              "apiUrl": "http://localhost",
              "apiPort": 8080,
              "webPort": 8080,
              "timeout": 30000,
              "debugMode": true
            }
        """.trimIndent()
        "config/staging.json" -> """
            {
              "environmentName": "STAGING",
              "apiUrl": "https://api-staging.example.com",
              "apiPort": 443,
              "webPort": 8080,
              "timeout": 60000,
              "debugMode": true
            }
        """.trimIndent()
        "config/prod.json" -> """
            {
              "environmentName": "PROD",
              "apiUrl": "https://api.example.com",
              "apiPort": 443,
              "webPort": 80,
              "timeout": 60000,
              "debugMode": false
            }
        """.trimIndent()
        else -> null
    }
}
```

**Validación**:
- ✅ Carga desde resources si disponible
- ✅ Fallback a hardcoded
- ✅ Sin errores de compilación

---

### Tarea 1.11: Refactorizar ResourceLoader.ios.kt

**Archivo**: `modules/config/src/iosMain/kotlin/com/edugo/kmp/config/ResourceLoader.ios.kt`

**Reemplazar contenido completo**:

```kotlin
package com.edugo.kmp.config

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * Implementación iOS para cargar recursos de configuración.
 * Primero intenta cargar desde Bundle resources, si falla usa configuración hardcodeada como fallback.
 */
internal actual fun loadResourceAsString(path: String): String? {
    // Try to load from Bundle resources first
    return try {
        // Split path into directory and filename
        // "config/dev.json" -> resource: "dev", type: "json", directory: "config"
        val parts = path.split("/")
        val fileName = parts.lastOrNull()?.substringBeforeLast(".")
        val fileExt = parts.lastOrNull()?.substringAfterLast(".")
        val directory = parts.dropLast(1).joinToString("/").takeIf { it.isNotEmpty() }
        
        if (fileName != null && fileExt != null) {
            val resourcePath = NSBundle.mainBundle.pathForResource(
                name = fileName,
                ofType = fileExt,
                inDirectory = directory
            )
            
            resourcePath?.let {
                NSString.stringWithContentsOfFile(
                    path = it,
                    encoding = NSUTF8StringEncoding,
                    error = null
                ) as? String
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    } ?: getHardcodedConfig(path)
}

/**
 * Configuraciones hardcodeadas como fallback.
 * Se usan cuando los archivos no están disponibles en el Bundle (ej: unit tests, archivos no copiados)
 */
private fun getHardcodedConfig(path: String): String? {
    return when (path) {
        "config/dev.json" -> """
            {
              "environmentName": "DEV",
              "apiUrl": "http://localhost",
              "apiPort": 8080,
              "webPort": 8080,
              "timeout": 30000,
              "debugMode": true
            }
        """.trimIndent()
        "config/staging.json" -> """
            {
              "environmentName": "STAGING",
              "apiUrl": "https://api-staging.example.com",
              "apiPort": 443,
              "webPort": 8080,
              "timeout": 60000,
              "debugMode": true
            }
        """.trimIndent()
        "config/prod.json" -> """
            {
              "environmentName": "PROD",
              "apiUrl": "https://api.example.com",
              "apiPort": 443,
              "webPort": 80,
              "timeout": 60000,
              "debugMode": false
            }
        """.trimIndent()
        else -> null
    }
}
```

**Nota**: Los archivos JSON deben copiarse al Bundle de iOS. Esto se configura en Xcode o build.gradle.kts.

**Validación**:
- ✅ Carga desde Bundle si disponible
- ✅ Fallback a hardcoded
- ✅ Sin errores de compilación

---

### Tarea 1.12: Actualizar ConfigModule

**Archivo**: `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/ConfigModule.kt`

**Cambio crítico**: Solo 1 línea cambia

**ANTES**:
```kotlin
public val configModule = module {
    single<Environment> { Environment.DEV }
    single<AppConfig> { ConfigLoader.load(get()) }
}
```

**DESPUÉS**:
```kotlin
public val configModule = module {
    single<Environment> { EnvironmentDetector.detect() }  // ← Cambio aquí
    single<AppConfig> { ConfigLoader.load(get()) }
}
```

**Agregar comentario**:
```kotlin
/**
 * Módulo Koin para dependencias del módulo config.
 *
 * El ambiente se detecta automáticamente según la plataforma:
 * - Android: Platform.isDebug → DEV, release → PROD
 * - Desktop: Debugger attached → DEV, production → PROD
 * - iOS: Siempre DEV (conservador)
 * - WasmJS: Default DEV
 *
 * Para override manual en tests: EnvironmentDetector.override(Environment.STAGING)
 */
```

**Validación**:
- ✅ Una sola línea cambiada
- ✅ Backward compatible (EnvironmentDetector.override() permite testing)
- ✅ Sin breaking changes

---

### Tarea 1.13: Actualizar AppConfig.kt

**Archivo**: `modules/config/src/commonMain/kotlin/com/edugo/kmp/config/AppConfig.kt`

**Cambio**: En la propiedad `environment` de `AppConfigImpl`

**ANTES**:
```kotlin
override val environment: Environment
    get() = Environment.fromString(environmentName)
```

**DESPUÉS**:
```kotlin
override val environment: Environment
    get() = Environment.fromStringOrDefault(environmentName, Environment.DEV)
```

**Validación**:
- ✅ Usa `fromStringOrDefault()` para fallback seguro
- ✅ No lanza excepciones si environmentName es inválido

---

### Tarea 1.14: Agregar dependencia de core en build.gradle.kts

**Archivo**: `modules/config/build.gradle.kts`

**Agregar dependencia**:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
                implementation(project(":modules:core"))  // ← Agregar esta línea
            }
        }
    }
}
```

**Validación**:
- ✅ Sync gradle exitoso
- ✅ `Platform` ahora se puede importar

---

## Tests de Fase 1

### Test 1.1: EnvironmentDetectorTest

**Archivo**: `modules/config/src/commonTest/kotlin/com/edugo/kmp/config/EnvironmentDetectorTest.kt`

```kotlin
package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EnvironmentDetectorTest {

    @AfterTest
    fun cleanup() {
        // Always reset after each test to avoid side effects
        EnvironmentDetector.reset()
    }

    @Test
    fun `detect returns valid environment automatically`() {
        val env = EnvironmentDetector.detect()
        assertNotNull(env, "Detected environment should not be null")
        assertTrue(
            env in listOf(Environment.DEV, Environment.STAGING, Environment.PROD),
            "Detected environment should be one of DEV STAGING or PROD but was $env"
        )
    }

    @Test
    fun `override forces specific environment`() {
        // Given: Override to STAGING
        EnvironmentDetector.override(Environment.STAGING)
        
        // When: Detect environment
        val env = EnvironmentDetector.detect()
        
        // Then: Returns STAGING
        assertEquals(Environment.STAGING, env, "Should return overridden environment")
    }

    @Test
    fun `reset clears override and restores automatic detection`() {
        // Given: Override active
        EnvironmentDetector.override(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect(), "Override should be active")
        
        // When: Reset
        EnvironmentDetector.reset()
        
        // Then: Returns auto-detected value (not necessarily PROD)
        val env = EnvironmentDetector.detect()
        assertNotNull(env, "After reset should still return valid environment")
    }

    @Test
    fun `multiple overrides work correctly`() {
        EnvironmentDetector.override(Environment.DEV)
        assertEquals(Environment.DEV, EnvironmentDetector.detect())
        
        EnvironmentDetector.override(Environment.STAGING)
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())
        
        EnvironmentDetector.override(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect())
    }

    @Test
    fun `override persists across multiple detect calls`() {
        EnvironmentDetector.override(Environment.STAGING)
        
        // Call detect multiple times
        repeat(5) {
            assertEquals(Environment.STAGING, EnvironmentDetector.detect())
        }
    }

    @Test
    fun `reset is idempotent`() {
        EnvironmentDetector.override(Environment.PROD)
        
        // Reset multiple times
        EnvironmentDetector.reset()
        val env1 = EnvironmentDetector.detect()
        
        EnvironmentDetector.reset()
        val env2 = EnvironmentDetector.detect()
        
        assertEquals(env1, env2, "Multiple resets should produce same result")
    }
}
```

**Ejecutar**:
```bash
./gradlew :modules:config:desktopTest --tests EnvironmentDetectorTest
```

---

### Test 1.2: EnvironmentTest

**Archivo**: `modules/config/src/commonTest/kotlin/com/edugo/kmp/config/EnvironmentTest.kt`

```kotlin
package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnvironmentTest {

    @Test
    fun `fromString converts valid strings case-insensitively`() {
        assertEquals(Environment.DEV, Environment.fromString("DEV"))
        assertEquals(Environment.DEV, Environment.fromString("dev"))
        assertEquals(Environment.DEV, Environment.fromString("Dev"))
        assertEquals(Environment.DEV, Environment.fromString("dEv"))
        
        assertEquals(Environment.STAGING, Environment.fromString("STAGING"))
        assertEquals(Environment.STAGING, Environment.fromString("staging"))
        assertEquals(Environment.STAGING, Environment.fromString("Staging"))
        
        assertEquals(Environment.PROD, Environment.fromString("PROD"))
        assertEquals(Environment.PROD, Environment.fromString("prod"))
        assertEquals(Environment.PROD, Environment.fromString("Prod"))
    }

    @Test
    fun `fromString returns null for invalid strings`() {
        assertNull(Environment.fromString("invalid"))
        assertNull(Environment.fromString(""))
        assertNull(Environment.fromString("development"))
        assertNull(Environment.fromString("production"))
        assertNull(Environment.fromString("test"))
        assertNull(Environment.fromString("local"))
    }

    @Test
    fun `fromString returns null for null input`() {
        assertNull(Environment.fromString(null))
    }

    @Test
    fun `fromStringOrDefault returns environment for valid string`() {
        assertEquals(Environment.DEV, Environment.fromStringOrDefault("dev"))
        assertEquals(Environment.STAGING, Environment.fromStringOrDefault("staging"))
        assertEquals(Environment.PROD, Environment.fromStringOrDefault("prod"))
    }

    @Test
    fun `fromStringOrDefault returns default for invalid string`() {
        assertEquals(Environment.DEV, Environment.fromStringOrDefault("invalid"))
        assertEquals(Environment.DEV, Environment.fromStringOrDefault(null))
        assertEquals(Environment.STAGING, Environment.fromStringOrDefault("invalid", Environment.STAGING))
        assertEquals(Environment.PROD, Environment.fromStringOrDefault(null, Environment.PROD))
    }

    @Test
    fun `fileName returns lowercase name`() {
        assertEquals("dev", Environment.DEV.fileName)
        assertEquals("staging", Environment.STAGING.fileName)
        assertEquals("prod", Environment.PROD.fileName)
    }
}
```

**Ejecutar**:
```bash
./gradlew :modules:config:desktopTest --tests EnvironmentTest
```

---

## Criterios de Aceptación - Fase 1

### Funcionalidad

- ✅ `EnvironmentDetector.detect()` retorna ambiente válido en Android/Desktop/iOS/WasmJS
- ✅ Android debug → DEV, release → PROD
- ✅ Desktop con debugger → DEV, sin debugger → PROD
- ✅ iOS → DEV (conservador)
- ✅ WasmJS → DEV
- ✅ `EnvironmentDetector.override()` permite forzar ambiente
- ✅ `EnvironmentDetector.reset()` restaura auto-detección
- ✅ ConfigLoader carga configuración correcta según ambiente detectado
- ✅ URLs NO están hardcodeadas en ResourceLoader (usan archivos o fallback)

### Testing

- ✅ Tests de EnvironmentDetector pasan en commonTest (6 tests)
- ✅ Tests de Environment.fromString pasan (6 tests)
- ✅ Tests de integración ConfigLoader + EnvironmentDetector pasan
- ✅ Cobertura > 70% en módulo config

### Compilación

- ✅ `./gradlew :modules:config:build` sin errores
- ✅ `./gradlew :modules:config:allTests` todos pasan
- ✅ Android app compila y corre
- ✅ Desktop app compila y corre

---

## Checklist de Validación

```bash
# 1. Compilar módulo config
./gradlew :modules:config:compileKotlinDesktop
# Esperado: BUILD SUCCESSFUL

# 2. Ejecutar tests
./gradlew :modules:config:desktopTest
# Esperado: BUILD SUCCESSFUL, 12 tests passed

# 3. Verificar detección automática en Android debug
./gradlew :platforms:mobile:app:installDebug
adb logcat | grep "Environment"
# Esperado: "Environment detected: DEV"

# 4. Verificar detección automática en Desktop
./gradlew :platforms:desktop:app:run
# Buscar en consola: "Environment detected: DEV/PROD"

# 5. Verificar archivos JSON existen
ls modules/config/src/commonMain/resources/config/
# Debe mostrar: dev.json, staging.json, prod.json
```

---

## Cómo Probar Manualmente

### Setup inicial

Agregar logging temporal en `App.kt` o `MainActivity.kt`:

```kotlin
println("Environment detected: ${EnvironmentDetector.detect()}")
val config = ConfigLoader.load(EnvironmentDetector.detect())
println("Config loaded: apiUrl=${config.apiUrl}, apiPort=${config.apiPort}")
println("Full API URL: ${config.getFullApiUrl()}")
```

### Test Case 1: Android Debug → DEV

```bash
./gradlew :platforms:mobile:app:installDebug
adb logcat | grep "Environment detected"
# Esperado: "Environment detected: DEV"
```

### Test Case 2: Desktop con Debugger → DEV

```bash
# En IntelliJ/Android Studio: Run con debugger attachado
./gradlew :platforms:desktop:app:run --debug-jvm
# Esperado: "Environment detected: DEV"
```

### Test Case 3: Override Manual

```kotlin
@Test
fun testOverride() {
    EnvironmentDetector.override(Environment.STAGING)
    val config = ConfigLoader.load(EnvironmentDetector.detect())
    assertEquals("https://api-staging.example.com", config.apiUrl)
    EnvironmentDetector.reset()
}
```

### Test Case 4: Desktop sin Debugger → PROD

```bash
# Build release y ejecutar sin debugger
./gradlew :platforms:desktop:app:installDist
./platforms/desktop/app/build/install/app/bin/app
# Esperado: "Environment detected: PROD"
```

---

## Problemas Comunes

### Problema 1: AndroidContextHolder no inicializado

**Síntomas**: 
```
NullPointerException al cargar config en Android
```

**Causa**: 
`AndroidContextHolder.init()` no llamado en `MainActivity.onCreate()`

**Solución**:
```kotlin
// En MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidContextHolder.init(applicationContext) // ← Agregar esto PRIMERO
    // ... resto del código
}
```

---

### Problema 2: Archivos JSON no encontrados en Desktop

**Síntomas**: 
```
Carga fallback hardcoded siempre
```

**Causa**: 
Archivos no copiados a classpath en build

**Solución**: 
Verificar que `src/commonMain/resources/` esté configurado correctamente. Los archivos deberían copiarse automáticamente.

---

### Problema 3: iOS Bundle resources no copiados

**Síntomas**: 
```
Carga fallback hardcoded siempre en iOS
```

**Causa**: 
JSON files no agregados al Xcode project

**Solución**: 
En Xcode, agregar archivos `config/*.json` al target:
1. Abrir proyecto en Xcode
2. Drag & drop archivos JSON
3. Asegurar que estén en "Copy Bundle Resources"

---

### Problema 4: "Unresolved reference Platform"

**Síntomas**:
```
e: Unresolved reference 'Platform'
```

**Causa**:
Falta dependencia de `modules:core` en `build.gradle.kts`

**Solución**:
```kotlin
// modules/config/build.gradle.kts
dependencies {
    implementation(project(":modules:core"))  // ← Agregar
}
```

---

### Problema 5: Tests fallan con "Environment detection returned null"

**Síntomas**: 
```
Tests de EnvironmentDetector fallan
```

**Causa**: 
expect/actual no implementado para testSourceSet

**Solución**: 
Los tests usan la implementación de Desktop por defecto. Esto es OK para Fase 1. No requiere acción.

---

## Siguiente Fase

Una vez completada Fase 1, proceder a **Fase 2: Build-time Config** (opcional):
- BuildConfig para Android con gradle flavors
- Info.plist para iOS con Xcode schemes
- gradle.properties integration
- NODE_ENV para WasmJS

Ver: [FASE-2-BUILD-TIME.md](./FASE-2-BUILD-TIME.md)

---

**Fin de Fase 1** ✅
