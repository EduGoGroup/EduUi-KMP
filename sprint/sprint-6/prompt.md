# Sprint 6: Prompt para Sesion Limpia

## Objetivo
Verificar, corregir y compilar el modulo `config` del Sprint 6 (Multi-Environment Config Management). El codigo fuente ya esta escrito. Falta: arreglar tests, compilar todas las plataformas, ejecutar tests y ajustar lo que falle.

---

## Estado Actual del Codigo (Verificado en disco)

### Archivos Fuente - COMPLETOS Y CORRECTOS

Todos estos archivos existen y tienen la API correcta:

#### commonMain (5 archivos)

**`modules/config/src/commonMain/kotlin/com/edugo/kmp/config/Environment.kt`**
```kotlin
public enum class Environment {
    DEV, STAGING, PROD;
    public val fileName: String get() = name.lowercase()
    public companion object {
        public fun fromString(value: String?): Environment?
        public fun fromStringOrDefault(value: String?, default: Environment = DEV): Environment
    }
}
```

**`modules/config/src/commonMain/kotlin/com/edugo/kmp/config/EnvironmentDetector.kt`**
```kotlin
public object EnvironmentDetector {
    @Volatile private var manualOverride: Environment? = null
    public fun detect(): Environment  // retorna override o detectPlatformEnvironment()
    public fun forceEnvironment(environment: Environment)
    public fun reset()
}
internal expect fun detectPlatformEnvironment(): Environment  // top-level expect fun
```

**`modules/config/src/commonMain/kotlin/com/edugo/kmp/config/AppConfig.kt`**
```kotlin
public interface AppConfig {
    val environment: Environment
    val apiUrl: String
    val apiPort: Int
    val webPort: Int
    val timeout: Long
    val debugMode: Boolean
    fun getFullApiUrl(): String = "$apiUrl:$apiPort"
}

@Serializable
public data class AppConfigImpl(
    private val environmentName: String,  // se mapea a Environment via fromStringOrDefault
    override val apiUrl: String,
    override val apiPort: Int,
    override val webPort: Int,
    override val timeout: Long,
    override val debugMode: Boolean
) : AppConfig
```

**`modules/config/src/commonMain/kotlin/com/edugo/kmp/config/ConfigLoader.kt`**
```kotlin
public object ConfigLoader {
    public fun load(environment: Environment): AppConfig  // lee "config/{env.fileName}.json"
    public fun loadFromString(jsonString: String): AppConfig
}
internal expect fun loadResourceAsString(path: String): String?  // top-level expect fun
```

**`modules/config/src/commonMain/kotlin/com/edugo/kmp/config/DefaultConfigs.kt`**
```kotlin
internal object DefaultConfigs {
    fun get(path: String): String?  // fallback con JSON embebido para dev/staging/prod
}
```

#### Platform-Specific Detectors (4 archivos)

**`modules/config/src/androidMain/.../EnvironmentDetector.android.kt`**
- System.getProperty("app.environment") -> Debug.isDebuggerConnected() -> default PROD

**`modules/config/src/desktopMain/.../EnvironmentDetector.desktop.kt`**
- System.getProperty("app.environment") -> System.getenv("APP_ENVIRONMENT") -> JDWP debugger check -> default PROD

**`modules/config/src/iosMain/.../EnvironmentDetector.ios.kt`**
- NSBundle.mainBundle.infoDictionary["AppEnvironment"] -> default DEV (conservador)

**`modules/config/src/wasmJsMain/.../EnvironmentDetector.wasmJs.kt`**
- @JsFun para obtener hostname -> localhost=DEV, staging=STAGING, otro=PROD, fallback DEV

#### Platform-Specific ResourceLoaders (4 archivos)

**Android**: `AndroidContextHolder.get()?.assets.open(path)` -> fallback `DefaultConfigs.get(path)`
**Desktop**: `javaClass.classLoader?.getResourceAsStream(path)` -> fallback `DefaultConfigs.get(path)`
**iOS**: `NSBundle.mainBundle.pathForResource()` + `NSString.stringWithContentsOfFile()` -> fallback `DefaultConfigs.get(path)`
**WasmJS**: Solo `DefaultConfigs.get(path)` (no hay classpath en wasm)

**`modules/config/src/androidMain/.../AndroidContextHolder.kt`**
```kotlin
public object AndroidContextHolder {
    public fun init(appContext: Context)  // llamar en MainActivity/Application
    internal fun get(): Context?
}
```

#### Config JSON (3 archivos en `modules/config/src/commonMain/resources/config/`)
- `dev.json`: localhost:8080, debugMode:true
- `staging.json`: api-staging.example.com:443, debugMode:true
- `prod.json`: api.example.com:443, debugMode:false

---

### Modulo DI - CORRECTO, usa API Sprint 6

**`modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/ConfigModule.kt`**
```kotlin
val configModule = module {
    single<Environment> { EnvironmentDetector.detect() }
    single<AppConfig> { ConfigLoader.load(get()) }
}
```

**`modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/AuthModule.kt`**
- Usa `get<AppConfig>().environment` para AuthConfig.forEnvironment()
- Usa `get<AppConfig>().getFullApiUrl()` para AuthRepositoryImpl baseUrl

**`modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/config/AuthConfig.kt`**
- `forEnvironment(env: Environment)` con when sobre DEV, STAGING, PROD - correcto

---

### build.gradle.kts - INCOMPLETO

**`modules/config/build.gradle.kts`** actual:
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

**Problemas conocidos:**
1. NO tiene `implementation(kotlin("stdlib"))` en commonMain - necesario para WasmJS
2. NO tiene `kotlinx-serialization-json` explicitamente (puede venir via foundation, hay que verificar)
3. NO tiene dependencia de `kotlin-test` en commonTest

---

### Tests - EXISTEN PERO TIENEN PROBLEMAS

5 archivos de test en `modules/config/src/commonTest/kotlin/com/edugo/kmp/config/`:

**`EnvironmentTest.kt`** - PROBLEMA: usa backtick en nombres de funcion
```kotlin
fun `fromString converts valid strings case-insensitively`()  // FALLA en iOS/Native
fun `fromString returns null for invalid strings`()
fun `fromString returns null for null input`()
fun `fromStringOrDefault returns environment for valid string`()
fun `fromStringOrDefault returns default for invalid string`()
fun `fileName returns lowercase name`()
```
Hay que renombrar a camelCase sin backticks.

**`EnvironmentDetectorTest.kt`** - OK, ya usa camelCase sin backticks
```kotlin
fun detectReturnsValidEnvironment()
fun forceEnvironmentOverridesDetection()
fun resetRestoresAutomaticDetection()
fun multipleForceCallsUsesLatest()
fun forcedEnvironmentPersistsAcrossMultipleDetectCalls()
fun resetIsIdempotent()
fun detectWithoutForceUsesAutoDetection()
```

**`ConfigLoaderTest.kt`** - PROBLEMA: usa underscores (revisar si compila en iOS)
```kotlin
fun load_dev_config_contains_correct_values()
fun load_staging_config_contains_correct_values()
fun load_prod_config_contains_correct_values()
fun getFullApiUrl_concatenates_url_and_port()
fun getFullApiUrl_works_for_prod()
fun loadFromString_parses_json_correctly()
fun loadFromString_ignores_unknown_keys()
```
Los underscores SIN backticks SI compilan en Kotlin/Native, solo los backticks fallan.

**`AppConfigTest.kt`** - OK, ya usa camelCase/underscores sin backticks
```kotlin
fun appConfigImpl_serializes_and_deserializes()
fun appConfigImpl_environment_maps_from_name()
fun appConfigImpl_environment_defaults_to_dev_for_unknown()
fun getFullApiUrl_concatenates_correctly()
fun getFullApiUrl_works_with_https_and_443()
fun appConfigImpl_data_class_equality()
fun debugMode_is_true_for_dev()
fun debugMode_is_false_for_prod()
```

**`ConfigLoaderIntegrationTest.kt`** - OK, usa camelCase sin backticks
```kotlin
fun loadDevConfigHasCorrectValues()
fun loadStagingConfigHasCorrectValues()
fun loadProdConfigHasCorrectValues()
fun getFullApiUrlConstructsCorrectUrl()
fun forceEnvironmentAffectsConfigLoading()
fun loadFromStringParsesValidJson()
```

---

## Tareas Pendientes (En Orden)

### 1. Arreglar EnvironmentTest.kt - Quitar backticks
Renombrar las 6 funciones de test que usan backticks a camelCase:
- `fromString converts valid strings case-insensitively` -> `fromStringConvertsCaseInsensitively`
- `fromString returns null for invalid strings` -> `fromStringReturnsNullForInvalidStrings`
- `fromString returns null for null input` -> `fromStringReturnsNullForNullInput`
- `fromStringOrDefault returns environment for valid string` -> `fromStringOrDefaultReturnsEnvironmentForValidString`
- `fromStringOrDefault returns default for invalid string` -> `fromStringOrDefaultReturnsDefaultForInvalidString`
- `fileName returns lowercase name` -> `fileNameReturnsLowercaseName`

### 2. Verificar/Actualizar build.gradle.kts
Agregar dependencias faltantes si las pruebas o compilacion fallan:
```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
                implementation(kotlin("stdlib"))  // necesario para WasmJS
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
```

### 3. Compilar Desktop (mas rapido)
```bash
./gradlew :modules:config:compileKotlinDesktop
```

### 4. Compilar WasmJS
```bash
./gradlew :modules:config:compileKotlinWasmJs
```
Si falla con "unresolved reference: JsFun", verificar que el plugin `kmp.android` ya incluye wasmJs target y que kotlin-stdlib esta en dependencias.

### 5. Compilar Android
```bash
./gradlew :modules:config:compileDebugKotlinAndroid
```

### 6. Ejecutar tests Desktop (mas rapido)
```bash
./gradlew :modules:config:desktopTest
```

### 7. Ejecutar todos los tests
```bash
./gradlew :modules:config:allTests
```

### 8. Verificar integracion con DI
```bash
./gradlew :modules:di:compileKotlinDesktop
```

---

## Informacion del Proyecto

- **Kotlin**: 2.2.20
- **Compose**: 1.9.0
- **Build system**: Convention plugins en `build-logic/`
- **Plugin base**: `kmp.android` (configura Android, Desktop, iOS, WasmJS targets)
- **Package convention**: `com.edugo.kmp.{moduleName}`
- **Modulos**: foundation, config, logger, storage, network, auth, di, ui, app
- **Test restriction iOS**: NO usar backticks en nombres de funciones de test (Kotlin/Native)
- **Logger en tests**: NO usar `DefaultLogger` en Android unit tests (no hay android.util.Log)

---

## Notas Importantes

1. **Compilacion rapida de un modulo**: `./gradlew :modules:config:compileKotlinDesktop`
2. **Tests rapidos**: `./gradlew :modules:config:desktopTest`
3. **El codigo fuente (src) esta COMPLETO** - Solo faltan ajustes menores en tests y build.
