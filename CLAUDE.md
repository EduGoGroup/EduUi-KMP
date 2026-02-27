# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Desktop app
make desktop-run             # Ejecutar en ambiente DEV
make desktop-stage           # Ejecutar en ambiente STAGING
make desktop-debug           # Ejecutar con debugger en puerto 5005
make desktop-build           # Compilar sin ejecutar
make desktop-clear-session   # Eliminar sesión guardada (fuerza pantalla de login)
make clean                   # Limpiar build

# Módulo específico (más rápido)
./gradlew :modules:{name}:compileKotlinDesktop  # Compilar un módulo
./gradlew :modules:{name}:desktopTest           # Tests solo Desktop (más rápido)
./gradlew :modules:{name}:allTests              # Tests en todas las plataformas
./gradlew :modules:{name}:androidUnitTest       # Tests Android
```

## Arquitectura del Proyecto

Monorepo KMP con soporte para **Android, Desktop (JVM), Web (wasmJs)** e iOS (opcional vía `enableIos=true` en `gradle.properties`).

```
modules/         # Módulos de lógica compartida (capas de abajo hacia arriba)
kmp-design/      # Design system (componentes Compose)
kmp-resources/   # Strings, colores, imágenes
kmp-screens/     # Implementaciones de pantallas y contratos de pantalla
kmp-samples/     # Showcase del design system
platforms/       # Aplicaciones por plataforma (desktop/app, mobile/app, web/app)
iosApp/          # App iOS
build-logic/     # Convention plugins de Gradle
gradle/          # Version catalog (libs.versions.toml)
sprint/          # Documentación activa del sprint
docs/            # Documentación de arquitectura
```

### Capas de módulos (dependencia ascendente)

```
foundation → core → logger / validation / config / storage / network → auth → dynamic-ui → di
```

`di` es el único módulo que importa todos los demás para registrarlos en Koin. `kmp-screens` depende de `di` y de las librerías de UI.

### Convention plugins (`build-logic/`)

| Plugin | Uso |
|--------|-----|
| `kmp.android` | Módulos con soporte Android + Desktop + wasmJs |
| `kmp.logic.core` | Módulos de lógica pura sin Android |
| `kmp.ui.full` | Módulos con Compose Multiplatform |

## Patrones clave

### Result monad
```kotlin
// Result.Failure NO tiene parámetro de tipo
Result.Success(data)
Result.Failure(errorMessage)   // No: Result.Failure<T>(...)
```

### Logger
```kotlin
logger.d(tag, msg)   // debug
logger.i(tag, msg)   // info
logger.w(tag, msg)   // warn
logger.e(tag, msg)   // error
```
En tests de Android (`androidUnitTest`): `DefaultLogger` falla porque no hay `android.util.Log`. Usar logger nullable o inyectar uno de prueba.

### Storage
Siempre usar `SafeEduGoStorage`, no el raw `EduGoStorage`. Para tests: `EduGoStorage.withSettings(MapSettings())`.

### Thread safety
Usar `Mutex` de `kotlinx.coroutines` (compatible con KMP). No usar `synchronized` ni `java.util.concurrent`.

### Tests en iOS (Kotlin/Native)
No usar comas en nombres de funciones de test (restricción de Kotlin/Native).

### Nombres de paquetes
Todos los módulos siguen `com.edugo.kmp.{nombre-modulo}`.

## Dependency Injection (Koin)

Los módulos Koin viven en `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/`. Hay un archivo por módulo de negocio (`AuthModule.kt`, `NetworkModule.kt`, etc.).

**Regla crítica**: `modules/di` NO debe depender de `kmp-screens` (evitar dependencia circular). Los contratos de pantalla se registran en un módulo Koin separado dentro de `kmp-screens/` y se añaden manualmente en `App.kt`:

```kotlin
startKoin {
    modules(KoinInitializer.modules + screenContractsModule)
}
```

## SDUI (Server-Driven UI)

Las pantallas se describen con JSON desde el backend (`ScreenDefinition`) y se renderizan dinámicamente. El flujo es:

1. **Backend** → devuelve `ScreenDefinition` con zonas (`slots`) y datos
2. **ScreenContract** → define los eventos que puede emitir una pantalla
3. **EventOrchestrator** → enruta `ScreenEvent` al handler correspondiente
4. **EventResult** → resultado del evento: `Success`, `NavigateTo`, `Error`, `SubmitTo`, etc.

Los contratos de pantalla viven en `kmp-screens/src/.../dynamic/contracts/`. El orquestador está en `modules/dynamic-ui/.../orchestrator/EventOrchestrator.kt`.

`ScreenDefinition` **no tiene** `dataEndpoint` ni `actions` (fueron eliminados del backend).

## Auth y RBAC

`AuthState.Authenticated` contiene `activeContext: UserContext`. Para acceder al rol/permisos:
```kotlin
authState.activeContext?.roleName      // NO usar user.role (campo eliminado)
authState.activeContext?.permissions
```

El storage guarda 3 claves: `auth_token`, `auth_user`, `auth_context`.

`LoginResponse` tiene `activeContext: UserContext` (obligatorio, no nullable).

## Estructura de módulo

```
modules/{name}/src/
├── commonMain/kotlin/com/edugo/kmp/{name}/
├── commonTest/kotlin/com/edugo/kmp/{name}/
├── androidMain/kotlin/   (sobreescrituras Android)
├── desktopMain/kotlin/   (sobreescrituras Desktop)
└── wasmJsMain/kotlin/    (sobreescrituras Web)
```

Los módulos usan `api(project(":modules:..."))` para dependencias que deben ser visibles al consumidor, e `implementation()` para dependencias internas.
