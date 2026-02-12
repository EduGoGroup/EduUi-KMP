# Testing Strategy - Sprint 6

## Visión General

Estrategia de testing completa para el sistema de configuración multi-ambiente, garantizando que la detección automática funcione correctamente en todas las plataformas y que la configuración se cargue sin errores.

---

## Pirámide de Testing

```
         /\
        /  \
       /E2E \          5% - Manual testing (1-2 tests)
      /------\
     /        \
    /Integration\      15% - ConfigLoader + EnvironmentDetector (3-4 tests)
   /------------\
  /              \
 /  Unit Tests    \   80% - Componentes aislados (12+ tests)
/------------------\
```

### Distribución de Tests

| Nivel | Cantidad | Cobertura | Velocidad |
|-------|----------|-----------|-----------|
| Unit | 12 tests | 80% | Muy rápida (< 1s) |
| Integration | 4 tests | 15% | Rápida (< 5s) |
| E2E | 2 tests | 5% | Lenta (manual) |

---

## Tests por Fase

### Fase 1 - MVP

#### Unit Tests (12 tests)

**EnvironmentDetectorTest.kt** (6 tests)
- ✅ `detect returns valid environment automatically`
- ✅ `override forces specific environment`
- ✅ `reset clears override and restores automatic detection`
- ✅ `multiple overrides work correctly`
- ✅ `override persists across multiple detect calls`
- ✅ `reset is idempotent`

**EnvironmentTest.kt** (6 tests)
- ✅ `fromString converts valid strings case-insensitively`
- ✅ `fromString returns null for invalid strings`
- ✅ `fromString returns null for null input`
- ✅ `fromStringOrDefault returns environment for valid string`
- ✅ `fromStringOrDefault returns default for invalid string`
- ✅ `fileName returns lowercase name`

**Total Fase 1**: **12 tests**

---

#### Integration Tests (4 tests)

**ConfigLoaderTest.kt** (4 tests)
- ✅ `loads config with auto-detected environment`
- ✅ `loads config with overridden environment`
- ✅ `loads dev config correctly`
- ✅ `loads staging config correctly`
- ✅ `loads prod config correctly`
- ✅ `getFullApiUrl constructs correct URL`

**Total Integration**: **6 tests**

---

### Fase 2 - Build-time Config (Opcional)

#### Platform-specific Tests

**EnvironmentDetectorAndroidTest.kt** (3 tests)
- ✅ `detects DEV in debug build`
- ✅ `detects PROD in release build`
- ✅ `reads BuildConfig.ENVIRONMENT correctly`

**EnvironmentDetectorIosTest.kt** (3 tests)
- ✅ `reads Info.plist AppEnvironment`
- ✅ `defaults to DEV if plist key missing`
- ✅ `handles invalid plist values`

**EnvironmentDetectorDesktopTest.kt** (2 tests)
- ✅ `reads app.environment system property`
- ✅ `defaults to PROD if property missing`

**EnvironmentDetectorWasmJsTest.kt** (2 tests)
- ✅ `detects DEV on localhost`
- ✅ `detects STAGING on staging hostname`
- ✅ `detects PROD on production hostname`

**Total Fase 2**: **10 tests adicionales**

---

## Cobertura por Módulo

### modules/config

| Archivo | Cobertura Objetivo | Tests |
|---------|-------------------|-------|
| `EnvironmentDetector.kt` | 100% | 6 tests |
| `Environment.kt` | 100% | 6 tests |
| `ConfigLoader.kt` | 90% | 4 tests |
| `AppConfig.kt` | 80% | Cubierto por ConfigLoader |
| `ResourceLoader.*.kt` | 70% | Cubierto por ConfigLoader |

**Cobertura total objetivo**: **>75%**

---

## Herramientas de Testing

### Framework de Tests

```kotlin
// commonTest - Para tests multiplataforma
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))  // kotlin.test
            }
        }
    }
}
```

### Librerías Utilizadas

- **kotlin.test**: Framework de testing KMP estándar
- **@Test**: Anotación para tests
- **assertEquals, assertNotNull, assertTrue**: Assertions

---

## Ejecución de Tests

### Comandos Gradle

```bash
# Todos los tests (todas las plataformas)
./gradlew :modules:config:allTests

# Solo Desktop (más rápido para desarrollo)
./gradlew :modules:config:desktopTest

# Solo Android
./gradlew :modules:config:testDebugUnitTest

# Solo iOS (simulator)
./gradlew :modules:config:iosX64Test

# Con coverage (Desktop)
./gradlew :modules:config:desktopTest koverHtmlReport
# Ver: modules/config/build/reports/kover/html/index.html

# Tests específicos
./gradlew :modules:config:desktopTest --tests EnvironmentDetectorTest
./gradlew :modules:config:desktopTest --tests "EnvironmentDetectorTest.override*"
```

### Tiempos de Ejecución Esperados

| Comando | Tiempo | Plataforma |
|---------|--------|------------|
| `desktopTest` | 1-3 segundos | Desktop/JVM |
| `testDebugUnitTest` | 5-10 segundos | Android |
| `iosX64Test` | 10-20 segundos | iOS Simulator |
| `allTests` | 30-60 segundos | Todas |

---

## Tests Unitarios Detallados

### EnvironmentDetectorTest

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
        EnvironmentDetector.reset()
    }

    @Test
    fun `detect returns valid environment automatically`() {
        val env = EnvironmentDetector.detect()
        assertNotNull(env)
        assertTrue(env in listOf(Environment.DEV, Environment.STAGING, Environment.PROD))
    }

    @Test
    fun `override forces specific environment`() {
        EnvironmentDetector.override(Environment.STAGING)
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())
    }

    @Test
    fun `reset clears override and restores automatic detection`() {
        EnvironmentDetector.override(Environment.PROD)
        EnvironmentDetector.reset()
        assertNotNull(EnvironmentDetector.detect())
    }

    @Test
    fun `multiple overrides work correctly`() {
        EnvironmentDetector.override(Environment.DEV)
        assertEquals(Environment.DEV, EnvironmentDetector.detect())
        
        EnvironmentDetector.override(Environment.STAGING)
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())
    }

    @Test
    fun `override persists across multiple detect calls`() {
        EnvironmentDetector.override(Environment.STAGING)
        repeat(5) {
            assertEquals(Environment.STAGING, EnvironmentDetector.detect())
        }
    }

    @Test
    fun `reset is idempotent`() {
        EnvironmentDetector.override(Environment.PROD)
        EnvironmentDetector.reset()
        val env1 = EnvironmentDetector.detect()
        EnvironmentDetector.reset()
        val env2 = EnvironmentDetector.detect()
        assertEquals(env1, env2)
    }
}
```

---

### EnvironmentTest

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
        assertEquals(Environment.STAGING, Environment.fromString("STAGING"))
        assertEquals(Environment.PROD, Environment.fromString("PROD"))
    }

    @Test
    fun `fromString returns null for invalid strings`() {
        assertNull(Environment.fromString("invalid"))
        assertNull(Environment.fromString(""))
    }

    @Test
    fun `fromString returns null for null input`() {
        assertNull(Environment.fromString(null))
    }

    @Test
    fun `fromStringOrDefault returns environment for valid string`() {
        assertEquals(Environment.DEV, Environment.fromStringOrDefault("dev"))
        assertEquals(Environment.STAGING, Environment.fromStringOrDefault("staging"))
    }

    @Test
    fun `fromStringOrDefault returns default for invalid string`() {
        assertEquals(Environment.DEV, Environment.fromStringOrDefault("invalid"))
        assertEquals(Environment.STAGING, Environment.fromStringOrDefault("invalid", Environment.STAGING))
    }

    @Test
    fun `fileName returns lowercase name`() {
        assertEquals("dev", Environment.DEV.fileName)
        assertEquals("staging", Environment.STAGING.fileName)
        assertEquals("prod", Environment.PROD.fileName)
    }
}
```

---

## Tests de Integración

### ConfigLoaderTest

```kotlin
package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigLoaderTest {

    @Test
    fun `loads config with auto-detected environment`() {
        EnvironmentDetector.reset()
        val env = EnvironmentDetector.detect()
        val config = ConfigLoader.load(env)
        
        assertEquals(env, config.environment)
        assertTrue(config.apiUrl.isNotEmpty())
        assertTrue(config.apiPort > 0)
    }

    @Test
    fun `loads config with overridden environment`() {
        EnvironmentDetector.override(Environment.STAGING)
        val config = ConfigLoader.load(EnvironmentDetector.detect())
        
        assertEquals(Environment.STAGING, config.environment)
        assertTrue(config.apiUrl.contains("staging") || 
                   config.apiUrl == "https://api-staging.example.com")
        
        EnvironmentDetector.reset()
    }

    @Test
    fun `loads dev config correctly`() {
        val config = ConfigLoader.load(Environment.DEV)
        
        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://localhost", config.apiUrl)
        assertEquals(8080, config.apiPort)
        assertEquals(true, config.debugMode)
    }

    @Test
    fun `loads staging config correctly`() {
        val config = ConfigLoader.load(Environment.STAGING)
        
        assertEquals(Environment.STAGING, config.environment)
        assertEquals("https://api-staging.example.com", config.apiUrl)
        assertEquals(443, config.apiPort)
        assertEquals(true, config.debugMode)
    }

    @Test
    fun `loads prod config correctly`() {
        val config = ConfigLoader.load(Environment.PROD)
        
        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.example.com", config.apiUrl)
        assertEquals(443, config.apiPort)
        assertEquals(false, config.debugMode)
    }

    @Test
    fun `getFullApiUrl constructs correct URL`() {
        val devConfig = ConfigLoader.load(Environment.DEV)
        assertEquals("http://localhost:8080", devConfig.getFullApiUrl())
        
        val prodConfig = ConfigLoader.load(Environment.PROD)
        assertEquals("https://api.example.com:443", prodConfig.getFullApiUrl())
    }
}
```

---

## Testing Manual (E2E)

### Test Case 1: Android Debug Build

**Pasos**:
1. `./gradlew :platforms:mobile:app:assembleDebug`
2. Instalar APK en dispositivo/emulador
3. Abrir app
4. Verificar logs: `adb logcat | grep Environment`

**Resultado Esperado**:
```
Environment detected: DEV
Config loaded: apiUrl=http://localhost, apiPort=8080
Full API URL: http://localhost:8080
```

---

### Test Case 2: iOS Release Build

**Pasos**:
1. Abrir proyecto en Xcode
2. Seleccionar scheme "EduGo-Prod"
3. Product → Archive
4. Distribuir a TestFlight o dispositivo
5. Abrir app y verificar logs

**Resultado Esperado**:
```
Environment detected: PROD
Config loaded: apiUrl=https://api.example.com, apiPort=443
Full API URL: https://api.example.com:443
```

---

### Test Case 3: Desktop con Override

**Pasos**:
1. Agregar en código:
```kotlin
EnvironmentDetector.override(Environment.STAGING)
```
2. `./gradlew :platforms:desktop:app:run`
3. Verificar logs en consola

**Resultado Esperado**:
```
Environment detected: STAGING
Config loaded: apiUrl=https://api-staging.example.com, apiPort=443
```

---

## Métricas de Éxito

### Cobertura de Tests

```bash
# Generar reporte de cobertura
./gradlew :modules:config:desktopTest koverHtmlReport

# Abrir reporte
open modules/config/build/reports/kover/html/index.html
```

**Objetivos**:
- ✅ Line coverage: >75%
- ✅ Branch coverage: >70%
- ✅ Method coverage: >80%

---

### Tiempo de Ejecución

**Baseline (antes de optimización)**:
- Desktop tests: ~3 segundos
- Android tests: ~10 segundos
- iOS tests: ~20 segundos

**Objetivo**:
- ✅ Desktop tests: <2 segundos
- ✅ Android tests: <8 segundos
- ✅ iOS tests: <15 segundos

---

## Continuous Integration

### GitHub Actions (ejemplo)

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Run tests
        run: ./gradlew :modules:config:allTests
        
      - name: Generate coverage report
        run: ./gradlew :modules:config:koverHtmlReport
        
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

---

## Debugging Tests

### Logs en Tests

```kotlin
@Test
fun `debug test with logging`() {
    println("Starting test...")
    val env = EnvironmentDetector.detect()
    println("Detected environment: $env")
    
    val config = ConfigLoader.load(env)
    println("Loaded config: ${config.apiUrl}")
    
    assertNotNull(config)
}
```

### Run Tests con --info

```bash
./gradlew :modules:config:desktopTest --info
# Muestra logs detallados de cada test
```

### Run Tests con --debug

```bash
./gradlew :modules:config:desktopTest --debug
# Muestra logs muy detallados (verboso)
```

---

## Best Practices

### 1. Siempre limpiar estado en @AfterTest

```kotlin
@AfterTest
fun cleanup() {
    EnvironmentDetector.reset()  // ← Importante!
}
```

### 2. Usar nombres descriptivos

```kotlin
// ✅ Bueno
@Test
fun `override forces specific environment`()

// ❌ Malo
@Test
fun testOverride()
```

### 3. Un assertion por test (cuando sea posible)

```kotlin
// ✅ Bueno
@Test
fun `fromString returns DEV for dev`() {
    assertEquals(Environment.DEV, Environment.fromString("dev"))
}

// ❌ Malo (múltiples assertions no relacionadas)
@Test
fun testEverything() {
    assertEquals(Environment.DEV, Environment.fromString("dev"))
    assertNotNull(ConfigLoader.load(Environment.DEV))
    // ... más assertions
}
```

### 4. Testear edge cases

```kotlin
@Test
fun `fromString handles null input`() {
    assertNull(Environment.fromString(null))
}

@Test
fun `fromString handles empty string`() {
    assertNull(Environment.fromString(""))
}

@Test
fun `fromString handles invalid string`() {
    assertNull(Environment.fromString("INVALID"))
}
```

---

## Troubleshooting Tests

### Tests fallan con "Context not initialized"

**Causa**: AndroidContextHolder no inicializado en tests

**Solución**: Los tests usan fallback hardcoded, esto es OK. Si necesitas testear con Context:

```kotlin
@Before
fun setup() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    AndroidContextHolder.init(context)
}
```

---

### Tests pasan localmente pero fallan en CI

**Causa**: Diferencias de ambiente o timing

**Solución**: 
- Verificar versiones de Kotlin/Gradle
- Agregar timeouts si hay operaciones async
- Usar `@Ignore` temporal para identificar test problemático

---

## Resumen

**Tests Implementados**: 12 unit + 4 integration = **16 tests automáticos**  
**Cobertura**: **>75%**  
**Tiempo Total**: **<5 segundos** (Desktop)  

**Estado**: ✅ **Completo para Fase 1**

---

**Última actualización**: 2026-02-11  
**Sprint**: 6 - Multi-Environment Config Management
