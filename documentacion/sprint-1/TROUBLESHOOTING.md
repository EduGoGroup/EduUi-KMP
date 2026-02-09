# Sprint 1: Troubleshooting Guide

## Tabla de Contenidos

1. [Problemas de Gradle](#problemas-de-gradle)
2. [Problemas de Convention Plugins](#problemas-de-convention-plugins)
3. [Problemas de Compilación](#problemas-de-compilación)
4. [Problemas de Tests](#problemas-de-tests)
5. [Problemas de Plataformas](#problemas-de-plataformas)
6. [Problemas de Performance](#problemas-de-performance)

---

## Problemas de Gradle

### Error: Plugin not found

**Síntoma**:
\`\`\`
Plugin [id: 'kmp.logic.core'] was not found
\`\`\`

**Causa**: build-logic no compilado o no en classpath.

**Solución**:
\`\`\`bash
# Limpiar y recompilar build-logic
rm -rf build-logic/build
./gradlew :build-logic:build

# Sync Gradle
./gradlew --stop
./gradlew build
\`\`\`

### Error: Version catalog not resolved

**Síntoma**:
\`\`\`
Could not find libs
\`\`\`

**Causa**: Path incorrecto en build-logic/settings.gradle.kts.

**Solución**:
Verificar que `build-logic/settings.gradle.kts` contenga:
\`\`\`kotlin
versionCatalogs {
    create("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}
\`\`\`

### Error: Gradle daemon timeout

**Síntoma**:
\`\`\`
Gradle build daemon disappeared unexpectedly
\`\`\`

**Causa**: Memoria insuficiente.

**Solución**:
Aumentar memoria en `gradle.properties`:
\`\`\`properties
org.gradle.jvmargs=-Xmx6144m -XX:MaxMetaspaceSize=1536m
kotlin.daemon.jvmargs=-Xmx6144m
\`\`\`

---

## Problemas de Convention Plugins

### Error: Namespace not specified (Android)

**Síntoma**:
\`\`\`
Namespace not specified. Specify a namespace in the module's build file.
\`\`\`

**Causa**: Módulo con Android target no define namespace.

**Solución**:
Agregar en `build.gradle.kts` del módulo:
\`\`\`kotlin
plugins {
    id("kmp.logic.mobile")
}

android {
    namespace = "com.edugo.kmp.your.module"  // REQUERIDO
}
\`\`\`

### Error: JVM target mismatch

**Síntoma**:
\`\`\`
Inconsistent JVM-target compatibility
\`\`\`

**Causa**: Versiones de JVM inconsistentes entre módulos.

**Solución**:
Verificar que TODOS los convention plugins usen:
\`\`\`kotlin
val JVM_TARGET = 17
jvmToolchain(JVM_TARGET)
\`\`\`

---

## Problemas de Compilación

### Error: Unresolved reference Result

**Síntoma**:
\`\`\`
Unresolved reference: Result
\`\`\`

**Causa**: Módulo no tiene dependency en kmp-foundation.

**Solución**:
Agregar dependency en `build.gradle.kts`:
\`\`\`kotlin
val commonMain by getting {
    dependencies {
        api(projects.kmpFoundation)  // o implementation()
    }
}
\`\`\`

### Error: Package does not exist

**Síntoma**:
\`\`\`
Package 'com.edugo.test.module' does not exist
\`\`\`

**Causa**: Imports no actualizados después de migration.

**Solución**:
Búsqueda y reemplazo global:
\`\`\`bash
find kmp-foundation -name "*.kt" -exec sed -i '' 's/com\\.edugo\\.test\\.module/com.edugo.kmp.foundation/g' {} +
find kmp-logger -name "*.kt" -exec sed -i '' 's/com\\.edugo\\.test\\.module/com.edugo.kmp.logger/g' {} +
find kmp-validation -name "*.kt" -exec sed -i '' 's/com\\.edugo\\.test\\.module/com.edugo.kmp.validation/g' {} +
find kmp-core -name "*.kt" -exec sed -i '' 's/com\\.edugo\\.test\\.module/com.edugo.kmp.core/g' {} +
\`\`\`

### Error: Circular dependency

**Síntoma**:
\`\`\`
Circular dependency between :kmp-validation and :kmp-foundation
\`\`\`

**Causa**: Imports bidireccionales.

**Solución**:
Verificar dependencias:
- kmp-foundation → no depende de nadie
- kmp-validation → solo depende de kmp-foundation
- kmp-logger → standalone
- kmp-core → standalone

---

## Problemas de Tests

### Error: No tests found

**Síntoma**:
\`\`\`
No tests found for given includes
\`\`\`

**Causa**: Tests no en `commonTest/` o nombre incorrecto.

**Solución**:
Verificar estructura:
\`\`\`
src/
└── commonTest/
    └── kotlin/
        └── com/edugo/kmp/module/
            └── SomeTest.kt  // DEBE terminar en "Test"
\`\`\`

### Error: Test failed with exception

**Síntoma**:
\`\`\`
java.lang.AssertionError: expected:<Success> but was:<Failure>
\`\`\`

**Causa**: Lógica de test incorrecta o código migrado tiene bugs.

**Solución**:
1. Ejecutar test individual:
\`\`\`bash
./gradlew :kmp-foundation:test --tests "*ResultTest"
\`\`\`

2. Habilitar stack traces:
\`\`\`bash
./gradlew test --stacktrace
\`\`\`

3. Comparar con test original en Kmp-Common

### Error: Coroutine test timeout

**Síntoma**:
\`\`\`
Test timed out after 60 seconds
\`\`\`

**Causa**: Test usa coroutines sin `runTest`.

**Solución**:
Usar `runTest` de kotlinx-coroutines-test:
\`\`\`kotlin
@Test
fun testSuspendFunction() = runTest {
    val result = suspendingFunction()
    assertEquals(expected, result)
}
\`\`\`

---

## Problemas de Plataformas

### iOS Build Falla

**Síntoma**:
\`\`\`
Could not determine the dependencies of task ':kmp-foundation:compileKotlinIosX64'
\`\`\`

**Causa**: XCode no instalado o enableIos=true sin configuración.

**Solución A** (deshabilitar iOS temporalmente):
\`\`\`bash
./gradlew build -PenableIos=false
\`\`\`

**Solución B** (habilitar iOS correctamente):
\`\`\`bash
# 1. Instalar XCode
xcode-select --install

# 2. Verificar instalación
xcodebuild -version

# 3. Build con iOS
./gradlew build -PenableIos=true
\`\`\`

### JavaScript Tests Fallan

**Síntoma**:
\`\`\`
Chrome driver not found
\`\`\`

**Causa**: Chrome no instalado o no en PATH.

**Solución**:
\`\`\`bash
# Instalar Chrome (macOS)
brew install --cask google-chrome

# Verificar
which google-chrome

# Ejecutar tests JS
./gradlew :kmp-foundation:jsTest
\`\`\`

### WASM Build Falla

**Síntoma**:
\`\`\`
WASM compilation is not supported
\`\`\`

**Causa**: Kotlin version < 2.0 o WASM no habilitado.

**Solución**:
Verificar `gradle.properties`:
\`\`\`properties
org.jetbrains.compose.experimental.wasm.enabled=true
\`\`\`

Y convention plugin usa:
\`\`\`kotlin
@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
wasmJs { ... }
\`\`\`

---

## Problemas de Performance

### Build Muy Lento (> 5 minutos)

**Causa**: Caches deshabilitados.

**Solución**:
Habilitar en `gradle.properties`:
\`\`\`properties
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true
\`\`\`

### OutOfMemoryError

**Síntoma**:
\`\`\`
java.lang.OutOfMemoryError: Java heap space
\`\`\`

**Causa**: Heap insuficiente para WASM + multiple targets.

**Solución**:
Aumentar memoria:
\`\`\`properties
# gradle.properties
org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=2048m
kotlin.daemon.jvmargs=-Xmx8192m
\`\`\`

### Incremental Build No Funciona

**Síntoma**:
Cada build toma el mismo tiempo.

**Causa**: Configuration cache no habilitado.

**Solución**:
\`\`\`bash
# Habilitar
./gradlew build --configuration-cache

# Verificar que funciona
./gradlew build --configuration-cache
# Segunda vez debe ser mucho más rápido
\`\`\`

---

## Comandos de Diagnóstico Útiles

\`\`\`bash
# Ver dependencias de un módulo
./gradlew :kmp-foundation:dependencies

# Ver tasks disponibles
./gradlew :kmp-foundation:tasks --all

# Limpiar todo
./gradlew clean
rm -rf .gradle build */build

# Rebuild completo (fresh)
./gradlew clean build

# Tests con detalles
./gradlew test --info

# Ver versiones
./gradlew --version

# Verificar configuration cache
./gradlew help --configuration-cache

# Ver qué archivos cambiaron en build
./gradlew build --dry-run
\`\`\`

---

## Contacto y Soporte

Si ninguna solución funciona:

1. Revisar logs en `build/reports/`
2. Comparar con Kmp-Common original
3. Verificar versiones en `gradle/libs.versions.toml`
4. Consultar CLAUDE.md en la raíz del proyecto

