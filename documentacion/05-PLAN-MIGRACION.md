# 05 - Plan de Migracion y Fases

## Resumen de Fases

| Fase | Modulos | Objetivo |
|------|---------|----------|
| **1 - Cimientos** | foundation, logger, core, validation | Base sin UI, 100% commonMain |
| **2 - Infraestructura** | network, storage | Comunicacion y persistencia |
| **3 - Dominio** | auth, cqrs, state | Logica de negocio |
| **4 - Presentacion** | designsystem, components, navigation | UI con Compose Multiplatform |
| **5 - Integracion** | Proyecto ejemplo | App de ejemplo con todos los modulos |

---

## Fase 1 - Cimientos (Empezar aqui)

### Paso 1.1: Crear estructura base del monorepo

```
kmp_new/
├── modules/
│   └── foundation/
│       ├── build.gradle.kts
│       └── src/commonMain/kotlin/com/edugo/kmp/foundation/
├── build-logic/
│   └── src/main/kotlin/
│       └── kmp.logic.core.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### Paso 1.2: Convention Plugin base (kmp.logic.core)

```kotlin
// kmp.logic.core.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    // Targets para TODAS las plataformas de logica
    androidTarget { compilations.all { kotlinOptions.jvmTarget = "17" } }
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop") { compilations.all { kotlinOptions.jvmTarget = "17" } }
    wasmJs { browser { testTask { enabled = true } } }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

### Paso 1.3: Migrar foundation

**Archivos a copiar del proyecto actual:**
```
test-module/core/Result.kt           → foundation/result/Result.kt
test-module/core/AppError.kt         → foundation/error/AppError.kt
test-module/core/ErrorCode.kt        → foundation/error/ErrorCode.kt
test-module/core/ErrorExtensions.kt  → foundation/error/ErrorExtensions.kt
test-module/core/serialization/      → foundation/serialization/
test-module/config/JsonConfig.kt     → foundation/serialization/JsonConfig.kt
test-module/data/models/base/        → foundation/entity/
test-module/data/models/pagination/  → foundation/pagination/
test-module/extensions/Result*.kt    → foundation/result/
test-module/mapper/                  → foundation/mapper/
```

**Cambios necesarios:**
1. Cambiar package de `com.edugo.test.module.core` → `com.edugo.kmp.foundation`
2. Eliminar dependencias a otros paquetes del monolito
3. Verificar que compila solo con kotlinx

### Paso 1.4: Migrar logger, core, validation

Mismo proceso. Cada modulo en su carpeta, cada uno compila independiente.

**Criterio de exito de Fase 1:**
- Cada modulo compila independientemente
- Tests pasan en todas las plataformas (Android, iOS, Desktop, WASM)
- Cero dependencias entre modulos excepto foundation

---

## Fase 2 - Infraestructura

### Paso 2.1: Migrar network

**Refactor principal:** Separar AuthInterceptor del modulo de network.

```kotlin
// ANTES (acoplado)
class AuthInterceptor(private val authService: AuthService) : Interceptor

// DESPUES (desacoplado)
// En kmp-network:
interface Interceptor {
    suspend fun intercept(chain: Chain): Response
}

// En kmp-auth (futuro):
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor
```

### Paso 2.2: Migrar storage

Relativamente directo. Solo cambiar packages y separar de auth.

**Criterio de exito de Fase 2:**
- network compila sin auth
- storage compila sin network
- Tests de HTTP funcionan con mock server
- Tests de storage funcionan en todas las plataformas

---

## Fase 3 - Dominio

### Paso 3.1: Migrar auth

Ahora auth DEPENDE de network y storage (no al reves).

```kotlin
// kmp-auth/build.gradle.kts
dependencies {
    implementation("com.edugo.kmp:foundation:$version")
    implementation("com.edugo.kmp:network:$version")
    implementation("com.edugo.kmp:storage:$version")
}
```

### Paso 3.2: Crear cqrs (nuevo)

Implementar interfaces base inspiradas en el modulo Swift.

### Paso 3.3: Crear state (nuevo)

StatePublisher como wrapper enriquecido de StateFlow.

---

## Fase 4 - Presentacion

### Paso 4.1: Agregar Compose Multiplatform

Necesita un convention plugin nuevo con Compose:

```kotlin
// kmp.ui.full.gradle.kts
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
    }
}
```

### Paso 4.2: Crear designsystem, components, navigation

Estos son modulos nuevos, no hay migracion.

---

## Fase 5 - Integracion

### Crear proyecto de ejemplo

```
kmp_new/
├── modules/           # Los 13 modulos
├── examples/
│   └── demo-app/      # App de ejemplo
│       ├── composeApp/
│       │   ├── src/commonMain/    # UI compartida
│       │   ├── src/androidMain/   # Entry point Android
│       │   ├── src/iosMain/       # Entry point iOS
│       │   ├── src/desktopMain/   # Entry point Desktop
│       │   └── src/wasmJsMain/    # Entry point Web
│       └── build.gradle.kts
└── settings.gradle.kts
```

---

## Prioridades de Implementacion

```
INMEDIATO (Sprint 1-2):
├── kmp-foundation     ← Base de todo
├── kmp-logger         ← Cross-cutting
└── kmp-validation     ← Independiente

SIGUIENTE (Sprint 3-4):
├── kmp-core           ← Utilities
├── kmp-network        ← HTTP client
└── kmp-storage        ← Key-value

DESPUES (Sprint 5-6):
├── kmp-auth           ← Depende de network + storage
└── kmp-cqrs           ← Patron nuevo

CUANDO SE NECESITE UI (Sprint 7+):
├── kmp-designsystem
├── kmp-components
└── kmp-navigation
```

---

## Checklist por Modulo

Para considerar un modulo "completo":

- [ ] Compila en todas las plataformas target (Android, iOS, Desktop, WASM)
- [ ] Tests unitarios en commonTest
- [ ] Tests platform-specific donde aplique
- [ ] README.md con uso basico
- [ ] API publica documentada (KDoc)
- [ ] Cero dependencias circulares
- [ ] Se puede importar independientemente
- [ ] Version catalog actualizado

---

## Decisiones Tecnicas Pendientes

| Decision | Opciones | Recomendacion |
|----------|----------|---------------|
| **Persistence engine** | SQLDelight vs Room KMP | SQLDelight (mas maduro en KMP) |
| **Navigation library** | Compose Navigation vs Voyager vs Decompose | Evaluar cuando se necesite |
| **DI framework** | Koin vs Kodein vs manual | Koin (popular en KMP) |
| **Image loading** | Coil vs Kamel | Coil 3 (KMP support) |
| **Kotlin/WASM readiness** | Estable vs experimental | Empezar con experimental, estabilizar con el tiempo |
| **iOS UI strategy** | Compose iOS vs SwiftUI bridge | Compose iOS (alineado con la vision) |

---

## Riesgos y Mitigaciones

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| Kotlin/WASM no maduro | Web target puede fallar | Priorizacion baja, empezar con mobile/desktop |
| Compose iOS rendimiento | UX inferior en iOS | Monitorear, tener plan B con UIKit bridge |
| Demasiados modulos | Complejidad de mantenimiento | Empezar con los esenciales, agregar bajo demanda |
| Breaking changes en KMP | Refactors forzados | Pinear versiones, actualizar gradualmente |
| Acoplamiento accidental | Volver al monolito | CI que valide independencia de cada modulo |
