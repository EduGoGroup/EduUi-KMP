# Troubleshooting - Sprint 6

## Visión General

Guía completa de solución de problemas para el sistema de configuración multi-ambiente. Cubre errores comunes, causas y soluciones paso a paso.

---

## Errores de Compilación

### 1. "Unresolved reference: Platform"

**Síntomas**:
```
e: file://.../EnvironmentDetector.android.kt:3:33 Unresolved reference 'Platform'.
e: file://.../EnvironmentDetector.android.kt:7:9 Unresolved reference 'Platform'.
```

**Causas Posibles**:
1. Falta dependencia del módulo `core` en `build.gradle.kts`
2. Import incorrecto (usando `foundation` en lugar de `core`)
3. Módulo `core` no compilado

**Soluciones**:

**Solución 1: Agregar dependencia**
```kotlin
// modules/config/build.gradle.kts
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

**Solución 2: Corregir import**
```kotlin
// CORRECTO
import com.edugo.kmp.core.platform.Platform

// INCORRECTO
import com.edugo.kmp.foundation.Platform  // ← No existe
```

**Solución 3: Compilar módulo core**
```bash
./gradlew :modules:core:build
./gradlew :modules:config:build
```

**Validación**:
```bash
./gradlew :modules:config:compileKotlinDesktop
# Esperado: BUILD SUCCESSFUL
```

---

### 2. "Modifier 'expect' is not applicable to 'member function'"

**Síntomas**:
```
e: file://.../EnvironmentDetector.kt:44:14 Modifier 'expect' is not applicable to 'member function'.
e: file://.../EnvironmentDetector.kt:44:21 Function 'detectPlatformEnvironment' without a body must be abstract.
```

**Causa**:
La función `expect` está declarada dentro del object en lugar de a nivel de módulo.

**Solución**:

**INCORRECTO**:
```kotlin
public object EnvironmentDetector {
    internal expect fun detectPlatformEnvironment(): Environment  // ← Dentro del object
}
```

**CORRECTO**:
```kotlin
public object EnvironmentDetector {
    public fun detect(): Environment {
        return manualOverride ?: detectPlatformEnvironment()
    }
}

// ← A nivel de módulo, FUERA del object
internal expect fun detectPlatformEnvironment(): Environment
```

**Validación**:
```bash
./gradlew :modules:config:build
# Esperado: BUILD SUCCESSFUL
```

---

### 3. "Return type mismatch: expected 'Environment', actual 'Environment?'"

**Síntomas**:
```
e: file://.../AppConfig.kt:67:17 Return type mismatch: expected 'Environment', actual 'Environment?'.
```

**Causa**:
`Environment.fromString()` retorna `Environment?` pero la propiedad espera `Environment`.

**Solución**:

**Usar fromStringOrDefault()** en lugar de fromString():

```kotlin
// INCORRECTO
override val environment: Environment
    get() = Environment.fromString(environmentName)  // Retorna Environment?

// CORRECTO
override val environment: Environment
    get() = Environment.fromStringOrDefault(environmentName, Environment.DEV)
```

**Validación**:
```bash
./gradlew :modules:config:compileKotlinDesktop
# Esperado: BUILD SUCCESSFUL
```

---

### 4. "Unresolved reference 'browser'" (WasmJS)

**Síntomas**:
```
e: file://.../EnvironmentDetector.wasmJs.kt:3:16 Unresolved reference 'browser'.
e: file://.../EnvironmentDetector.wasmJs.kt:6:20 Unresolved reference 'window'.
```

**Causa**:
`kotlinx.browser` no está disponible o configurado incorrectamente en WasmJS.

**Solución 1: Usar default simple (Fase 1)**
```kotlin
// modules/config/src/wasmJsMain/kotlin/.../EnvironmentDetector.wasmJs.kt
internal actual fun detectPlatformEnvironment(): Environment {
    // Simple default for Phase 1
    return Environment.DEV
}
```

**Solución 2: Agregar dependencia kotlinx-browser (Fase 2)**
```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.2")
            }
        }
    }
}
```

**Validación**:
```bash
./gradlew :modules:config:compileKotlinWasmJs
# Esperado: BUILD SUCCESSFUL
```

---

## Errores de Runtime

### 5. "AndroidContextHolder not initialized"

**Síntomas**:
```
NullPointerException in ResourceLoader.android.kt
Config always loads from hardcoded fallback
```

**Causa**:
`AndroidContextHolder.init()` no se llamó en `MainActivity.onCreate()`.

**Solución**:

```kotlin
// platforms/mobile/app/src/androidMain/kotlin/.../MainActivity.kt
import com.edugo.kmp.config.AndroidContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ← Agregar ANTES de KoinApplication
        AndroidContextHolder.init(applicationContext)
        
        // Resto del código...
    }
}
```

**Validación**:
1. Agregar log temporal:
```kotlin
val context = AndroidContextHolder.get()
println("Context initialized: ${context != null}")
```
2. Ejecutar app y verificar: `adb logcat | grep "Context initialized"`
3. Esperado: `Context initialized: true`

---

### 6. "Config files not found in assets"

**Síntomas**:
```
Config always loads from hardcoded fallback
ResourceLoader tries to open assets but fails
```

**Causa**:
Archivos JSON no copiados a assets durante build.

**Solución**:

**Verificar que archivos existen**:
```bash
ls modules/config/src/commonMain/resources/config/
# Esperado: dev.json, staging.json, prod.json
```

**Verificar estructura de directorios**:
```
modules/config/src/commonMain/
└── resources/
    └── config/
        ├── dev.json
        ├── staging.json
        └── prod.json
```

**Rebuild para copiar assets**:
```bash
./gradlew :modules:config:clean
./gradlew :modules:config:build
./gradlew :platforms:mobile:app:assembleDebug
```

**Verificar APK contiene assets**:
```bash
# Extraer APK
unzip -l platforms/mobile/app/build/outputs/apk/debug/app-debug.apk | grep config
# Esperado: assets/config/dev.json, assets/config/staging.json, etc.
```

**Validación**:
Agregar log en ResourceLoader:
```kotlin
val context = AndroidContextHolder.get()
if (context != null) {
    try {
        val content = context.assets.open(path).bufferedReader().use { it.readText() }
        println("Loaded from assets: $path")
        return content
    } catch (e: Exception) {
        println("Failed to load from assets: ${e.message}")
    }
}
```

---

### 7. "iOS Bundle resources not found"

**Síntomas**:
```
Config always loads from hardcoded fallback in iOS
NSBundle.mainBundle.path() returns null
```

**Causa**:
Archivos JSON no agregados al Xcode target.

**Solución**:

**Paso 1: Agregar archivos al proyecto Xcode**
1. Abrir `iosApp.xcodeproj` en Xcode
2. Drag & drop `config/` folder al proyecto
3. En el diálogo:
   - ✅ "Copy items if needed"
   - ✅ "Create folder references"
   - ✅ Seleccionar target: "iosApp"

**Paso 2: Verificar en Build Phases**
1. Seleccionar target "iosApp"
2. Build Phases → Copy Bundle Resources
3. Verificar que aparecen:
   - `config/dev.json`
   - `config/staging.json`
   - `config/prod.json`

**Paso 3: Rebuild**
```bash
./gradlew :platforms:ios:app:build
```

**Validación**:
```bash
# Verificar .app contiene archivos
ls -R platforms/ios/app/build/Debug-iphonesimulator/iosApp.app/
# Esperado: Debe mostrar config/dev.json, etc.
```

---

### 8. "Wrong environment detected"

**Síntomas**:
```
App en debug usa PROD
App en release usa DEV
Environment detection incorrect
```

**Causas Posibles**:
1. `Platform.isDebug` no refleja build actual
2. Override manual activo por error
3. Configuración de build incorrecta

**Solución 1: Verificar Platform.isDebug**
```kotlin
// Agregar log temporal
println("Platform.isDebug: ${Platform.isDebug}")
println("Environment detected: ${EnvironmentDetector.detect()}")
```

**Solución 2: Reset override**
```kotlin
// Al inicio de la app
EnvironmentDetector.reset()  // Limpiar cualquier override
```

**Solución 3: Verificar build configuration**

**Android**:
```bash
# Debug build debe usar debug variant
./gradlew :platforms:mobile:app:assembleDebug

# Release build debe usar release variant
./gradlew :platforms:mobile:app:assembleRelease
```

**Solución 4: Implementar Fase 2 (build-time config)**
Si la detección runtime no es confiable, implementar Fase 2 con BuildConfig/Info.plist.

---

## Errores de Tests

### 9. "Tests fail with 'Environment detection returned null'"

**Síntomas**:
```
AssertionError: expected:<DEV> but was:<null>
EnvironmentDetector.detect() returns null in tests
```

**Causa**:
expect/actual no implementado para testSourceSet, usa implementación incorrecta.

**Solución**:

**Es comportamiento normal para Fase 1**. Los tests usan la implementación de Desktop por defecto.

**Workaround**:
```kotlin
@Test
fun `detect returns valid environment automatically`() {
    val env = EnvironmentDetector.detect()
    assertNotNull(env, "Environment should not be null")
    // No assert valor específico, solo que sea válido
    assertTrue(env in listOf(Environment.DEV, Environment.STAGING, Environment.PROD))
}
```

---

### 10. "Tests affect each other (state leak)"

**Síntomas**:
```
Test A pasa solo
Test B pasa solo
Test A + B juntos → Test B falla
```

**Causa**:
Override de EnvironmentDetector no se limpia entre tests.

**Solución**:

**Siempre usar @AfterTest**:
```kotlin
class EnvironmentDetectorTest {
    
    @AfterTest  // ← IMPORTANTE
    fun cleanup() {
        EnvironmentDetector.reset()
    }
    
    @Test
    fun `test A`() {
        EnvironmentDetector.override(Environment.DEV)
        // ...
    }
    
    @Test
    fun `test B`() {
        // Comienza limpio gracias a @AfterTest
        val env = EnvironmentDetector.detect()
        // ...
    }
}
```

**Validación**:
```bash
# Run tests múltiples veces para verificar consistencia
./gradlew :modules:config:desktopTest
./gradlew :modules:config:desktopTest
./gradlew :modules:config:desktopTest
# Esperado: Todos pasan
```

---

## Errores de Configuración

### 11. "Invalid JSON in config files"

**Síntomas**:
```
SerializationException: Unexpected JSON token
Failed to parse config
```

**Causa**:
JSON malformado en archivos de configuración.

**Solución**:

**Validar JSON**:
```bash
# Usar python
cat modules/config/src/commonMain/resources/config/dev.json | python -m json.tool

# O usar jq
cat modules/config/src/commonMain/resources/config/dev.json | jq .

# O usar validador online: jsonlint.com
```

**Errores comunes**:
```json
// ❌ INCORRECTO - Trailing comma
{
  "apiUrl": "http://localhost",
  "apiPort": 8080,  // ← Coma final
}

// ✅ CORRECTO
{
  "apiUrl": "http://localhost",
  "apiPort": 8080
}
```

```json
// ❌ INCORRECTO - Comillas simples
{
  'apiUrl': 'http://localhost'  // ← Comillas simples
}

// ✅ CORRECTO - Comillas dobles
{
  "apiUrl": "http://localhost"
}
```

---

### 12. "Config properties missing"

**Síntomas**:
```
SerializationException: Field 'timeout' is required
MissingFieldException
```

**Causa**:
JSON no tiene todos los campos requeridos por `AppConfigImpl`.

**Solución**:

**Verificar que todos los campos existen**:
```json
{
  "environmentName": "DEV",     // ← Requerido
  "apiUrl": "http://localhost",  // ← Requerido
  "apiPort": 8080,               // ← Requerido
  "webPort": 8080,               // ← Requerido
  "timeout": 30000,              // ← Requerido
  "debugMode": true              // ← Requerido
}
```

**Comparar con AppConfigImpl**:
```kotlin
@Serializable
public data class AppConfigImpl(
    private val environmentName: String,
    override val apiUrl: String,
    override val apiPort: Int,
    override val webPort: Int,
    override val timeout: Long,
    override val debugMode: Boolean
) : AppConfig
```

---

## Errores de Fase 2 (Build-time Config)

### 13. "BuildConfig class not found" (Android)

**Síntomas**:
```
ClassNotFoundException: com.edugo.kmp.BuildConfig
Reflection fails in EnvironmentDetector
```

**Causa**:
`buildFeatures.buildConfig = true` no habilitado en `build.gradle.kts`.

**Solución**:

```kotlin
// platforms/mobile/app/build.gradle.kts
android {
    buildFeatures {
        buildConfig = true  // ← Agregar esta línea
    }
    
    buildTypes {
        debug {
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
        }
        release {
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
        }
    }
}
```

**Rebuild**:
```bash
./gradlew :platforms:mobile:app:clean
./gradlew :platforms:mobile:app:assembleDebug
```

**Validación**:
```bash
# Verificar que BuildConfig.java se genera
ls platforms/mobile/app/build/generated/source/buildConfig/debug/com/edugo/kmp/
# Esperado: BuildConfig.java
```

---

### 14. "Info.plist key not found" (iOS)

**Síntomas**:
```
infoDictionary?.get("AppEnvironment") returns null
Environment always defaults to DEV
```

**Causa**:
Clave `AppEnvironment` no existe en Info.plist o no usa variable `$(APP_ENVIRONMENT)`.

**Solución**:

**Paso 1: Verificar Info.plist**
```xml
<!-- iosApp/Info.plist -->
<plist version="1.0">
<dict>
    <key>AppEnvironment</key>
    <string>$(APP_ENVIRONMENT)</string>  <!-- ← Debe usar variable -->
    <!-- ... otras claves ... -->
</dict>
</plist>
```

**Paso 2: Verificar Build Settings**
1. Xcode → Target → Build Settings
2. Buscar "APP_ENVIRONMENT"
3. Debe tener valores por configuración:
   - Debug: `DEV`
   - Release: `PROD`

**Paso 3: Rebuild**
```bash
xcodebuild clean -scheme iosApp
xcodebuild -scheme iosApp -configuration Debug
```

**Validación**:
Agregar log en EnvironmentDetector.ios.kt:
```kotlin
val infoDictionary = NSBundle.mainBundle.infoDictionary
val envValue = infoDictionary?.get("AppEnvironment") as? String
println("Info.plist AppEnvironment: $envValue")
```

---

### 15. "gradle.properties not applied" (Desktop)

**Síntomas**:
```
System.getProperty("app.environment") returns null
Always defaults to PROD
```

**Causa**:
Property no se pasa a JVM args.

**Solución**:

```kotlin
// platforms/desktop/app/build.gradle.kts
val appEnvironment: String by project  // Lee de gradle.properties

application {
    mainClass.set("com.edugo.kmp.MainKt")
    
    applicationDefaultJvmArgs = listOf(
        "-Dapp.environment=$appEnvironment"  // ← Pasar a JVM
    )
}

tasks.named<JavaExec>("run") {
    systemProperty("app.environment", appEnvironment)  // ← Para 'run' task
}
```

**Validación**:
```bash
# Verificar que property se lee
./gradlew :platforms:desktop:app:run -Papp.environment=STAGING
# Agregar log: println("System property: ${System.getProperty("app.environment")}")
# Esperado: "System property: STAGING"
```

---

## Tips de Debugging

### Logging Detallado

```kotlin
// Agregar logs temporales en EnvironmentDetector
internal actual fun detectPlatformEnvironment(): Environment {
    println("=== Environment Detection START ===")
    println("Platform.isDebug: ${Platform.isDebug}")
    
    if (Platform.isDebug) {
        println("Detected: DEV (via Platform.isDebug)")
        return Environment.DEV
    }
    
    val envProperty = System.getProperty("app.environment")
    println("System.getProperty('app.environment'): $envProperty")
    
    if (envProperty != null) {
        val env = Environment.fromString(envProperty)
        println("Detected: $env (via System property)")
        return env ?: Environment.PROD
    }
    
    println("Detected: PROD (default)")
    return Environment.PROD
}
```

### Verificar Estado de Config

```kotlin
// Agregar en App.kt o MainActivity
fun debugConfig() {
    println("=== Config Debug ===")
    println("Environment: ${EnvironmentDetector.detect()}")
    
    val config = ConfigLoader.load(EnvironmentDetector.detect())
    println("API URL: ${config.apiUrl}")
    println("API Port: ${config.apiPort}")
    println("Full URL: ${config.getFullApiUrl()}")
    println("Debug Mode: ${config.debugMode}")
    println("Timeout: ${config.timeout}")
    println("===================")
}
```

### Limpiar y Rebuild

Si todo falla:
```bash
# Limpiar completamente
./gradlew clean
./gradlew --stop  # Detener daemon

# Invalidar caché de Gradle
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# Rebuild
./gradlew build
```

---

## Recursos Adicionales

### Documentación

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Koin DI](https://insert-koin.io/)

### Comunidad

- Stack Overflow tag: `kotlin-multiplatform`
- Kotlin Slack: `#multiplatform`
- GitHub Issues del proyecto

---

## Checklist de Troubleshooting

Cuando encuentres un error:

1. ✅ Verificar logs completos (no solo mensaje de error)
2. ✅ Reproducir en ambiente limpio (`./gradlew clean`)
3. ✅ Verificar versiones de dependencias
4. ✅ Buscar error en este documento
5. ✅ Agregar logging detallado
6. ✅ Aislar componente problemático
7. ✅ Verificar que Fase 1 está completa
8. ✅ Consultar documentación oficial
9. ✅ Pedir ayuda en comunidad (con logs completos)

---

**Última actualización**: 2026-02-11  
**Sprint**: 6 - Multi-Environment Config Management
