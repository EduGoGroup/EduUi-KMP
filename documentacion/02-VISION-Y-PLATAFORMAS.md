# 02 - Vision y Plataformas Target

## Vision General

Crear un ecosistema de modulos Kotlin Multiplatform **independientes e importables** que cubran las necesidades comunes de cualquier proyecto EduGo (o futuro), siguiendo el mismo patron que los Swift Packages pero para el ecosistema Kotlin.

**Principio fundamental:** Cada modulo es un paquete independiente que puede ser importado por separado, similar a como Go maneja sus modules o como Swift Package Manager organiza dependencias.

---

## Plataformas Target

### 1. Mobile (Prioridad Alta)

**Android + iOS desde una misma base de codigo.**

```
commonMain (logica compartida)
├── androidMain → Android App (Material Design 3)
└── iosMain → iOS App (misma UI adaptada via Compose Multiplatform)
```

**Estrategia:**
- **UI:** Compose Multiplatform para ambas plataformas
- **Android:** Salida nativa con estandares Android, Material Design 3
- **iOS:** Misma app compilada para iOS, minimizando codigo nativo iOS
- **Cero SwiftUI:** La UI es 100% Compose Multiplatform
- **Expect/Actual:** Solo para APIs de plataforma (camara, GPS, notificaciones, etc.)

**Source Set Hierarchy (Mobile):**
```
commonMain
├── androidMain
│   └── Android-specific (permisos, servicios, etc.)
└── iosMain
    └── iOS-specific (solo lo minimo para compilar)
```

**Dependencias clave:**
- Compose Multiplatform (UI compartida)
- Material Design 3 (tema y componentes)
- Ktor (networking)
- kotlinx-serialization (JSON)
- DataStore / multiplatform-settings (almacenamiento)

---

### 2. Desktop (Prioridad Media)

**Misma app para Windows, Mac y Linux.**

```
commonMain (logica compartida)
└── desktopMain → JVM Desktop (Compose for Desktop)
    ├── Windows
    ├── macOS
    └── Linux
```

**Estrategia:**
- **UI:** Compose for Desktop (mismo framework que mobile)
- **Diseno identico** en las 3 plataformas de escritorio
- **Un solo target JVM** que corre en Windows/Mac/Linux
- **Puede compartir modulos** con mobile (network, storage, auth, etc.)

**Source Set Hierarchy (Desktop):**
```
commonMain
└── desktopMain (JVM)
    └── Compose for Desktop UI
```

**Ventaja:** El codigo Compose es practicamente el mismo entre mobile y desktop. La diferencia principal es el tamano de ventana y algunos comportamientos de navegacion.

---

### 3. Web (Prioridad Baja - Secundaria/Terciaria)

**Aplicacion web usando Kotlin/WASM.**

```
commonMain (logica compartida)
└── wasmJsMain → WebAssembly (Compose for Web con WASM)
```

**Estrategia:**
- **Kotlin/WASM** (NO Kotlin/JS como en el proyecto actual)
- **Compose for Web** con target WASM
- **Rendimiento nativo** en el browser via WebAssembly
- **Sin interop JS complejo:** WASM es mas directo

**Source Set Hierarchy (Web):**
```
commonMain
└── wasmJsMain
    └── Compose for Web (WASM target)
```

**Nota:** Kotlin/WASM + Compose for Web aun esta en desarrollo activo (Alpha/Beta). La priorizacion baja permite esperar a que madure mientras se trabaja mobile y desktop.

---

## Source Set Unificado (Todos los targets)

Para un modulo que soporte TODAS las plataformas:

```
commonMain (100% del codigo de logica)
├── androidMain
├── iosMain
├── desktopMain (JVM)
└── wasmJsMain
```

Para modulos de **solo logica** (sin UI), el hierarchy seria:

```
commonMain
├── jvmMain (Android + Desktop comparten JVM)
│   ├── androidMain (especificos Android si los hay)
│   └── desktopMain (especificos Desktop si los hay)
├── nativeMain (iOS + potencialmente otros native targets)
│   └── iosMain
└── wasmJsMain
```

**Nota importante:** Para modulos de logica pura (foundation, core, validation, etc.) casi todo el codigo va en `commonMain` y las implementaciones platform-specific son minimas (logger, dispatchers, etc.).

---

## Compose Multiplatform como Base UI

**Compose Multiplatform** es el framework UI unificador:

| Plataforma | Target Compose | Motor |
|-----------|---------------|-------|
| Android | Compose Android (nativo) | Skia/Android Canvas |
| iOS | Compose iOS | Skia |
| Desktop (Win/Mac/Linux) | Compose Desktop | Skia/AWT |
| Web | Compose Web WASM | Skia/Canvas2D |

**Beneficio:** Un solo lenguaje de UI (Compose) para las 4 plataformas. La misma pantalla de login, el mismo formulario, la misma navegacion.

---

## Convention Plugins Nuevos (Propuesta)

Necesitamos convention plugins mas granulares:

| Plugin | Targets | Para que |
|--------|---------|----------|
| `kmp.logic.mobile` | Android + iOS | Modulos de logica para apps mobile |
| `kmp.logic.full` | Android + iOS + Desktop + WASM | Modulos de logica completa |
| `kmp.logic.core` | Todos (puro Kotlin) | Foundation, validation, etc. |
| `kmp.ui.mobile` | Android + iOS | Compose UI para mobile |
| `kmp.ui.full` | Android + iOS + Desktop + WASM | Compose UI completa |
| `kmp.ui.desktop` | Desktop (JVM) | Compose Desktop |

**Diferencia clave con los actuales:** Separar plugins de logica (sin Compose) de plugins de UI (con Compose).

---

## Estrategia de Importacion de Modulos

### Opcion A: Git Submodules (Recomendada para inicio)
```kotlin
// settings.gradle.kts del proyecto consumidor
includeBuild("../kmp-modules/foundation")
includeBuild("../kmp-modules/network")
```

### Opcion B: Maven Local / GitHub Packages (Recomendada para produccion)
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.edugo.kmp:foundation:1.0.0")
    implementation("com.edugo.kmp:network:1.0.0")
}
```

### Opcion C: Composite Builds (Recomendada para desarrollo)
```kotlin
// settings.gradle.kts
includeBuild("/path/to/kmp-foundation") {
    dependencySubstitution {
        substitute(module("com.edugo.kmp:foundation")).using(project(":"))
    }
}
```

**Recomendacion:** Empezar con **Composite Builds** (Opcion C) durante desarrollo, migrar a **Maven/GitHub Packages** (Opcion B) cuando los modulos esten estables. Esto es lo mas parecido al modelo de Go modules.
