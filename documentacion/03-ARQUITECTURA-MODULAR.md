# 03 - Arquitectura Modular

## Filosofia

Replicar la arquitectura de tiers del proyecto Swift (EduGoModules) adaptada a Kotlin Multiplatform, con modulos verdaderamente independientes que se puedan importar por separado.

**Reglas fundamentales:**
1. **Flujo unidireccional:** Solo se puede depender de tiers inferiores
2. **Independencia:** Cada modulo se publica/importa por separado
3. **commonMain first:** Maximo codigo en common, minimo platform-specific
4. **Sin dependencias circulares**
5. **Cada modulo tiene sus propios tests**

---

## Arquitectura de Tiers

```
TIER-0: Foundation          ← Sin dependencias externas
    ↓
TIER-1: Core                ← Depende de Foundation
    ↓
TIER-2: Infrastructure      ← Depende de Foundation + Core
    ↓
TIER-3: Domain              ← Depende de Foundation + Core + Infrastructure
    ↓
TIER-4: Presentation        ← Depende de Foundation + Core + Domain
    ↓
TIER-5: Features/App        ← Depende de todo (el proyecto final)
```

---

## Comparacion Swift vs Kotlin Propuesto

| Tier | Swift (EduGoModules) | Kotlin (Propuesto) |
|------|---------------------|-------------------|
| 0 | `EduFoundation` | `kmp-foundation` |
| 1 | `EduCore` (Logger, Models, Utils) | `kmp-logger`, `kmp-core` |
| 2 | `EduInfrastructure` (Network, Storage, Persistence) | `kmp-network`, `kmp-storage`, `kmp-persistence` |
| 3 | `EduDomain` (CQRS, State, UseCases) | `kmp-cqrs`, `kmp-state`, `kmp-auth` |
| 4 | `EduPresentation` (DesignSystem, Components, Navigation) | `kmp-designsystem`, `kmp-components`, `kmp-navigation` |
| 5 | `EduFeatures` | Proyectos finales (apps) |

---

## Estructura Fisica de Carpetas

```
kmp_new/
├── documentacion/                    # Esta documentacion
├── modules/                          # Modulos independientes
│   ├── foundation/                   # TIER-0
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/
│   │   ├── src/commonTest/
│   │   └── README.md
│   ├── logger/                       # TIER-1
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/
│   │   ├── src/androidMain/
│   │   ├── src/iosMain/
│   │   ├── src/desktopMain/
│   │   ├── src/wasmJsMain/
│   │   └── src/commonTest/
│   ├── core/                         # TIER-1
│   ├── network/                      # TIER-2
│   ├── storage/                      # TIER-2
│   ├── persistence/                  # TIER-2
│   ├── auth/                         # TIER-3
│   ├── validation/                   # TIER-1
│   ├── cqrs/                         # TIER-3
│   ├── state/                        # TIER-3
│   ├── designsystem/                 # TIER-4
│   ├── components/                   # TIER-4
│   └── navigation/                   # TIER-4
├── build-logic/                      # Convention Plugins compartidos
│   ├── src/main/kotlin/
│   │   ├── kmp.logic.core.gradle.kts
│   │   ├── kmp.logic.mobile.gradle.kts
│   │   ├── kmp.logic.full.gradle.kts
│   │   ├── kmp.ui.mobile.gradle.kts
│   │   ├── kmp.ui.full.gradle.kts
│   │   └── kover.gradle.kts
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml           # Version catalog compartido
├── build.gradle.kts                 # Root build
├── settings.gradle.kts              # Include de modulos
└── gradle.properties
```

---

## Grafo de Dependencias Detallado

```
foundation (TIER-0)
├── Solo: kotlinx-serialization, kotlinx-datetime, kotlinx-coroutines
├── NO depende de ningun otro modulo
└── Contiene: Result, AppError, ErrorCode, EntityBase, JsonConfig

logger (TIER-1)
├── Depende de: foundation
├── Platform-specific: Android(Logcat), iOS(OSLog), Desktop(SLF4J), WASM(console)
└── Contiene: Logger interface, LogLevel, LogCategory

core (TIER-1)
├── Depende de: foundation
└── Contiene: Extensions, ModelHelpers, Platform abstractions

validation (TIER-1)
├── Depende de: foundation
└── Contiene: Validators, AccumulativeValidation, ValidationRules

network (TIER-2)
├── Depende de: foundation, logger
├── Platform-specific: Android(OkHttp), iOS(Darwin), Desktop(CIO), WASM(Fetch)
├── NO depende de auth (interceptors son pluggables)
└── Contiene: HttpClient, Interceptors, Retry, RequestConfig

storage (TIER-2)
├── Depende de: foundation
├── Platform-specific: Android(DataStore), iOS(NSUserDefaults), Desktop(Preferences), WASM(localStorage)
└── Contiene: KeyValueStorage, TypedStorage, FlowStorage

persistence (TIER-2)
├── Depende de: foundation, core
├── Platform-specific: SQLDelight o Room KMP
└── Contiene: Database abstractions, Repositories, Migrations

auth (TIER-3)
├── Depende de: foundation, network, storage
├── Usa network con interceptor pluggable (no integrado)
└── Contiene: AuthService, TokenManager, JwtParser, JwtValidator

cqrs (TIER-3)
├── Depende de: foundation, core
└── Contiene: Command, Query, Handler, Mediator, Events

state (TIER-3)
├── Depende de: foundation, core
└── Contiene: StatePublisher, StateMachine, Operators

designsystem (TIER-4)
├── Depende de: foundation (solo tokens y temas)
├── Requiere: Compose Multiplatform
└── Contiene: Theme, Colors, Typography, Spacing, Effects

components (TIER-4)
├── Depende de: foundation, designsystem
├── Requiere: Compose Multiplatform
└── Contiene: Forms, Input, Lists, Loading, Feedback

navigation (TIER-4)
├── Depende de: foundation, core
├── Requiere: Compose Multiplatform
└── Contiene: Navigator, Routes, DeepLinking, Transitions
```

---

## Principio de Desacoplamiento

### Antes (Proyecto Actual - Acoplado)
```
test-module/
└── network/
    └── interceptor/
        └── AuthInterceptor.kt  ← CONOCE AuthService directamente
```

### Despues (Propuesto - Desacoplado)
```
kmp-network/
└── interceptor/
    └── Interceptor.kt          ← Interface generica
    └── HeaderInterceptor.kt    ← Solo agrega headers
    └── LoggingInterceptor.kt   ← Solo logea

kmp-auth/
└── AuthInterceptor.kt          ← Implementa Interceptor interface
└── AuthService.kt              ← Usa kmp-network como dependencia
```

**El modulo `network` NO sabe nada de `auth`.** Es el modulo `auth` quien crea un interceptor que inyecta al `network`. Inversion de dependencias.

---

## Patron de Modulo Tipo

Cada modulo sigue esta estructura interna:

```
kmp-{nombre}/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/com/edugo/kmp/{nombre}/
│   │   ├── api/                  # Interfaces publicas (contratos)
│   │   ├── impl/                 # Implementaciones internas
│   │   ├── model/                # Modelos de datos
│   │   └── ext/                  # Extension functions
│   ├── commonTest/kotlin/com/edugo/kmp/{nombre}/
│   │   └── ...Tests.kt
│   ├── androidMain/              # Si aplica
│   ├── iosMain/                  # Si aplica
│   ├── desktopMain/              # Si aplica
│   └── wasmJsMain/               # Si aplica
└── README.md
```

**Reglas de visibilidad:**
- `api/` → Todo `public`, exportado
- `impl/` → Todo `internal`, no visible fuera del modulo
- `model/` → Data classes `public`
- `ext/` → Extension functions `public`

---

## Como Consume un Proyecto Final

```kotlin
// settings.gradle.kts de una app mobile
pluginManagement {
    includeBuild("path/to/kmp_new/build-logic")
}

// Opcion A: Local (desarrollo)
includeBuild("path/to/kmp_new/modules/foundation")
includeBuild("path/to/kmp_new/modules/network")
includeBuild("path/to/kmp_new/modules/auth")

// Opcion B: Remoto (produccion)
// Solo agregar al repositories + dependencies
```

```kotlin
// build.gradle.kts del modulo app
dependencies {
    // Solo importas lo que necesitas
    implementation("com.edugo.kmp:foundation:1.0.0")
    implementation("com.edugo.kmp:network:1.0.0")
    implementation("com.edugo.kmp:auth:1.0.0")
    // NO necesitas traer storage si no lo usas
}
```
