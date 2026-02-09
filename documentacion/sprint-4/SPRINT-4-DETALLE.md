# SPRINT 4: PRESENTACION + APP DE EJEMPLO - Detalle de Implementación

## Resumen Ejecutivo

### Contexto del Sprint

**Prerequisitos**: Sprint 1, 2, 3 completados (todos los módulos de lógica existen)

**Objetivo**: Crear la capa de presentación completa del proyecto EduGo KMP con módulos UI, design system, recursos multilingües, navegación y aplicaciones de ejemplo para todas las plataformas.

### Stack UI Target

```yaml
Compose Multiplatform: 1.9.0
Material Design 3: Incluido en Compose
Compose Resources: Para recursos multiplatform
Koin Compose: 4.1.0 (DI en composables)
Lifecycle ViewModel: Para arquitectura MVVM
Navigation: Custom NavigationState (sin Navigation Compose para evitar dependencia Android)

Plataformas UI:
  - Android: Jetpack Compose
  - iOS: Compose for iOS (on-demand)
  - Desktop: Compose Desktop (JVM)
  - WASM: Compose for Web
```

### Objetivos del Sprint 4

Este sprint crea la **capa de presentación** completa:

1. **Convention Plugin UI**: kmp.ui.full para módulos Compose
2. **Módulo Design**: Design tokens, theme, colores semánticos, componentes
3. **Módulo Resources**: Strings multiplatforma (expect/actual)
4. **Módulo Navigation**: NavigationState, Routes, NavigationHost
5. **App de Ejemplo**: Entry points por plataforma con pantallas funcionales

### Entregables

- 1 convention plugin UI (kmp.ui.full)
- 3 módulos UI (design, resources, navigation incluido en screens)
- 4 aplicaciones de ejemplo (Android, Desktop, WASM, iOS on-demand)
- 4+ pantallas demo (Splash, Login, Home, Settings)
- Design system completo con tokens y componentes

### Duración Estimada

- **Desarrollo**: 4-5 días
- **Testing UI**: 1-2 días
- **Total**: 5-7 días

---

## TASK 4.1: Crear Convention Plugin kmp.ui.full

### Objetivo

Crear el convention plugin `kmp.ui.full` que configura automáticamente Compose Multiplatform para todos los targets (Android, Desktop, WASM, iOS on-demand).

### Archivo a Crear

```
kmp_new/
└── build-logic/
    └── src/main/kotlin/
        └── kmp.ui.full.gradle.kts    # Nuevo plugin UI
```

### Referencia de Código Fuente

**Base**: `/Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common/build-logic/src/main/kotlin/kmp.android.gradle.kts`

**Diferencias con kmp.logic.core**:
- Agrega plugins de Compose (composeMultiplatform, composeCompiler)
- Configura iOS on-demand con flag `enableIos`
- Agrega WASM target con `wasmJs`
- NO incluye Ktor (módulos UI no hacen llamadas de red directas)
- Incluye Compose dependencies (runtime, foundation, material3, ui, resources)
- Incluye Koin Compose para DI en composables

**Referencias de Template**:
- `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/core/design/build.gradle.kts`
- `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/core/resources/build.gradle.kts`
- `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/ui/screens/build.gradle.kts`

### Código Completo del Plugin

```kotlin
// kmp_new/build-logic/src/main/kotlin/kmp.ui.full.gradle.kts

@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")  // Necesario para compose.components.resources
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Constantes de configuración
val VERSION_CATALOG_NAME = "libs"

/**
 * Android SDK version to compile against.
 * SDK 36 = Android 15+ (2025) - Latest stable for Compose UI.
 */
val COMPILE_SDK = 36

/**
 * Minimum Android version required to run the app.
 * SDK 24 = Android 7 - Minimum required for Jetpack Compose.
 */
val MIN_SDK = 24

/**
 * JVM target version for Kotlin compilation.
 * Java 17 is the LTS version required by Android Gradle Plugin 8.x+.
 */
val JVM_TARGET = 17

// Acceso al version catalog
val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named(VERSION_CATALOG_NAME)

kotlin {
    // Android Target
    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.fromTarget(JVM_TARGET.toString()))
            }
        }
    }

    // Desktop (JVM) Target
    jvm("desktop") {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.fromTarget(JVM_TARGET.toString()))
            }
        }
    }

    // WASM Target for Web
    wasmJs {
        browser()
        binaries.library()
    }

    // iOS ON-DEMAND - Solo se compila con flag -PenableIos=true
    // Evita problemas en CI/CD Linux y compilaciones generales
    val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
    if (enableIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform Core
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)

                // Koin for Compose (DI en composables)
                implementation(libs.findLibrary("koin-core")
                    .orElseThrow { IllegalStateException("Library 'koin-core' not found") })
                implementation(libs.findLibrary("koin-compose")
                    .orElseThrow { IllegalStateException("Library 'koin-compose' not found") })
                implementation(libs.findLibrary("koin-compose-viewmodel")
                    .orElseThrow { IllegalStateException("Library 'koin-compose-viewmodel' not found") })

                // Lifecycle ViewModel (MVVM)
                implementation(libs.findLibrary("androidx-lifecycle-viewmodel")
                    .orElseThrow { IllegalStateException("Library 'androidx-lifecycle-viewmodel' not found") })
                implementation(libs.findLibrary("androidx-lifecycle-runtime-compose")
                    .orElseThrow { IllegalStateException("Library 'androidx-lifecycle-runtime-compose' not found") })

                // Coroutines (para LaunchedEffect, rememberCoroutineScope)
                implementation(libs.findLibrary("kotlinx-coroutines-core")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-core' not found") })

                // kotlinx.serialization (para Compose Resources)
                implementation(libs.findLibrary("kotlinx-serialization-json")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-serialization-json' not found") })
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.findLibrary("kotlinx-coroutines-test")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-test' not found") })
            }
        }

        val androidMain by getting {
            dependencies {
                // Compose Preview
                implementation(compose.preview)

                // Activity Compose (para setContent)
                implementation(libs.findLibrary("androidx-activity-compose")
                    .orElseThrow { IllegalStateException("Library 'androidx-activity-compose' not found") })

                // Coroutines Android (Dispatchers.Main)
                implementation(libs.findLibrary("kotlinx-coroutines-android")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-android' not found") })

                // Koin Android
                implementation(libs.findLibrary("koin-android")
                    .orElseThrow { IllegalStateException("Library 'koin-android' not found") })
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.findLibrary("kotlin-test-junit")
                    .orElseThrow { IllegalStateException("Library 'kotlin-test-junit' not found") })
            }
        }

        val desktopMain by getting {
            dependencies {
                // Compose Desktop (incluye swing)
                implementation(compose.desktop.currentOs)

                // Coroutines Swing (Dispatchers.Swing)
                implementation(libs.findLibrary("kotlinx-coroutines-swing")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-swing' not found") })
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.findLibrary("kotlin-test-junit")
                    .orElseThrow { IllegalStateException("Library 'kotlin-test-junit' not found") })
            }
        }

        val wasmJsMain by getting {
            dependencies {
                // Compose for Web incluido automáticamente
                // No requiere dependencias adicionales
            }
        }

        val wasmJsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        if (enableIos) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }

            val iosX64Test by getting
            val iosArm64Test by getting
            val iosSimulatorArm64Test by getting
            val iosTest by creating {
                dependsOn(commonTest)
                iosX64Test.dependsOn(this)
                iosArm64Test.dependsOn(this)
                iosSimulatorArm64Test.dependsOn(this)
            }
        }
    }

    jvmToolchain(JVM_TARGET)
}

// Configuración Android
configure<LibraryExtension> {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // NOTA: Cada módulo debe configurar su namespace en build.gradle.kts
    // Ejemplo: android { namespace = "com.edugo.kmp.design" }
}

// Opciones de compilador comunes
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}
```

### Diferencias Clave con kmp.logic.core

| Aspecto | kmp.logic.core | kmp.ui.full |
|---------|---------------|-------------|
| **Plugins** | NO Compose | SÍ Compose (composeMultiplatform + composeCompiler) |
| **Targets** | Android, Desktop, JS (Browser/Node) | Android, Desktop, WASM, iOS (on-demand) |
| **Ktor** | SÍ (client + engines) | NO (UI no hace network) |
| **Compose** | NO | SÍ (runtime, material3, resources, preview) |
| **Koin** | koin-core | koin-core + koin-compose + koin-compose-viewmodel |
| **Lifecycle** | NO | SÍ (viewmodel + runtime-compose) |
| **Android minSdk** | 29 (Android 10) | 24 (Android 7, mínimo para Compose) |
| **Android compileSdk** | 35 | 36 |

### Verificación

```bash
# Verificar que el plugin se puede aplicar
./gradlew :kmp-design:tasks

# Compilar un módulo UI de ejemplo
./gradlew :kmp-design:build

# Verificar sourceSets
./gradlew :kmp-design:sourceSets
```

---

## TASK 4.2: Crear Módulo kmp-design (Design System)

### Objetivo

Crear el módulo de design system que centraliza tokens de diseño, theme de Material 3, colores semánticos y componentes reutilizables.

### Archivos a Crear

```
kmp_new/
└── kmp-design/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/com/edugo/kmp/design/
        │   ├── Tokens.kt              # Spacing, Sizes, Alpha, Durations, Radius
        │   ├── SemanticColors.kt      # Success, Warning, Error, Info
        │   ├── Elevation.kt           # Niveles de elevación Material 3
        │   ├── MessageType.kt         # Enum para tipos de mensaje
        │   ├── EduGoTheme.kt          # Theme composable principal
        │   ├── Typography.kt          # Configuración de tipografía
        │   └── components/
        │       ├── DSAlertDialog.kt   # Dialog con estilos semánticos
        │       └── DSSnackbar.kt      # Snackbar con estilos semánticos
        └── commonTest/kotlin/com/edugo/kmp/design/
            └── DesignTokensTest.kt
```

### Referencia de Código Fuente

**Origen**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/core/design/`

**Archivos fuente**:
- `src/commonMain/kotlin/com/sortisplus/template_kmp_clean/core/design/Tokens.kt`
- `src/commonMain/kotlin/com/sortisplus/template_kmp_clean/core/design/SemanticColors.kt`
- `src/commonMain/kotlin/com/sortisplus/template_kmp_clean/core/design/Elevation.kt`
- `src/commonMain/kotlin/com/sortisplus/template_kmp_clean/core/design/MessageType.kt`
- `src/commonMain/kotlin/com/sortisplus/template_kmp_clean/core/design/components/DSAlertDialog.kt`
- `src/commonMain/kotlin/com/sortisplus/template_kmp_clean/core/design/components/DSSnackbar.kt`
- `build.gradle.kts`

**Adaptaciones**:
- Cambiar package de `com.sortisplus.template_kmp_clean.core.design` a `com.edugo.kmp.design`
- Mantener toda la lógica de tokens y componentes
- Agregar EduGoTheme.kt con colores personalizados

### Código Crítico

#### 1. kmp-design/build.gradle.kts

```kotlin
plugins {
    id("kmp.ui.full")
}

android {
    namespace = "com.edugo.kmp.design"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose incluido por kmp.ui.full
                // Material3 incluido por kmp.ui.full
                // materialIconsExtended incluido por kmp.ui.full
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

#### 2. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/Tokens.kt

```kotlin
package com.edugo.kmp.design

import androidx.compose.ui.unit.dp

/**
 * Design Tokens centralizados para mantener consistencia visual
 * en toda la aplicación EduGo multiplataforma.
 *
 * Estos tokens eliminan valores hardcodeados y facilitan:
 * - Mantenimiento centralizado
 * - Consistencia visual
 * - Cambios de diseño escalables
 */

/**
 * Espaciado estándar para padding, margins y gaps
 *
 * Uso:
 * ```
 * Modifier.padding(Spacing.m)
 * Spacer(modifier = Modifier.height(Spacing.l))
 * ```
 */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

/**
 * Tamaños de componentes específicos
 *
 * Uso:
 * ```
 * Icon(modifier = Modifier.size(Sizes.iconMedium))
 * CircularProgressIndicator(modifier = Modifier.size(Sizes.progressLarge))
 * ```
 */
object Sizes {
    // Iconos - Tamaños estándar
    val iconSmall = 16.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp

    // Iconos - Tamaños grandes (para emojis, ilustraciones)
    val iconXLarge = 32.dp
    val iconXXLarge = 48.dp
    val iconMassive = 64.dp

    // Progress indicators
    val progressSmall = 24.dp
    val progressLarge = 48.dp

    // Botones
    val buttonHeight = 48.dp

    /**
     * Tamaños de avatares
     */
    object Avatar {
        val small = 24.dp
        val medium = 32.dp
        val large = 40.dp
        val xlarge = 48.dp
        val xxlarge = 64.dp
    }

    /**
     * Tamaños de touch targets
     * Basados en Material Design guidelines
     */
    object TouchTarget {
        /**
         * Tamaño mínimo de touch target según Material Design (48dp)
         */
        val minimum = 48.dp

        /**
         * Tamaño cómodo de touch target para mejor usabilidad (56dp)
         */
        val comfortable = 56.dp
    }
}

/**
 * Valores de opacidad/alpha para estados visuales
 *
 * Uso:
 * ```
 * color = MaterialTheme.colorScheme.onBackground.copy(alpha = Alpha.muted)
 * ```
 */
object Alpha {
    const val disabled = 0.4f
    const val muted = 0.6f
    const val subtle = 0.7f
    const val surfaceVariant = 0.8f
}

/**
 * Duraciones de animaciones y delays (en milisegundos)
 *
 * Uso:
 * ```
 * LaunchedEffect(Unit) {
 *     delay(Durations.splash)
 *     onNavigate()
 * }
 * ```
 */
object Durations {
    const val splash = 2000L
    const val short = 200L
    const val medium = 500L
    const val long = 1000L
}

/**
 * Radios de esquinas (border radius)
 *
 * Uso:
 * ```
 * Card(shape = RoundedCornerShape(Radius.medium))
 * ```
 */
object Radius {
    val small = 4.dp
    val medium = 8.dp
    val large = 16.dp
}
```

#### 3. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/SemanticColors.kt

```kotlin
package com.edugo.kmp.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colores semánticos para mensajes y estados.
 * Usa MaterialTheme.colorScheme como base para mantener consistencia con el tema.
 */
object SemanticColors {

    /**
     * Color para mensajes de éxito (verde)
     */
    @Composable
    fun success(): Color = MaterialTheme.colorScheme.tertiary

    @Composable
    fun onSuccess(): Color = MaterialTheme.colorScheme.onTertiary

    @Composable
    fun successContainer(): Color = MaterialTheme.colorScheme.tertiaryContainer

    @Composable
    fun onSuccessContainer(): Color = MaterialTheme.colorScheme.onTertiaryContainer

    /**
     * Color para mensajes de advertencia (amarillo/naranja)
     */
    @Composable
    fun warning(): Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)

    @Composable
    fun onWarning(): Color = MaterialTheme.colorScheme.onSecondary

    @Composable
    fun warningContainer(): Color = MaterialTheme.colorScheme.secondaryContainer

    @Composable
    fun onWarningContainer(): Color = MaterialTheme.colorScheme.onSecondaryContainer

    /**
     * Color para mensajes de error (rojo)
     */
    @Composable
    fun error(): Color = MaterialTheme.colorScheme.error

    @Composable
    fun onError(): Color = MaterialTheme.colorScheme.onError

    @Composable
    fun errorContainer(): Color = MaterialTheme.colorScheme.errorContainer

    @Composable
    fun onErrorContainer(): Color = MaterialTheme.colorScheme.onErrorContainer

    /**
     * Color para mensajes informativos (azul/primary)
     */
    @Composable
    fun info(): Color = MaterialTheme.colorScheme.primary

    @Composable
    fun onInfo(): Color = MaterialTheme.colorScheme.onPrimary

    @Composable
    fun infoContainer(): Color = MaterialTheme.colorScheme.primaryContainer

    @Composable
    fun onInfoContainer(): Color = MaterialTheme.colorScheme.onPrimaryContainer
}
```

#### 4. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/Elevation.kt

```kotlin
package com.edugo.kmp.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Valores de elevación para componentes.
 * Basados en Material Design 3 elevation levels.
 */
object Elevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp

    // Aliases semánticos
    val card: Dp = level1
    val cardHover: Dp = level2
    val floatingButton: Dp = level2
    val modal: Dp = level3
    val drawer: Dp = level4
}
```

#### 5. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/MessageType.kt

```kotlin
package com.edugo.kmp.design

/**
 * Tipos de mensajes soportados por el sistema de mensajería.
 */
enum class MessageType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
}
```

#### 6. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/EduGoTheme.kt

```kotlin
package com.edugo.kmp.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Color scheme para EduGo
 */
private val EduGoLightColors = lightColorScheme(
    primary = Color(0xFF1976D2),        // Azul EduGo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = Color(0xFFFFA726),      // Naranja acento
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFFE65100),

    tertiary = Color(0xFF66BB6A),       // Verde éxito
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Color(0xFF2E7D32),

    error = Color(0xFFE53935),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFC62828),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
)

private val EduGoDarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFFE65100),
    secondaryContainer = Color(0xFFF57C00),
    onSecondaryContainer = Color(0xFFFFE0B2),

    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF388E3C),
    onTertiaryContainer = Color(0xFFC8E6C9),

    error = Color(0xFFEF5350),
    onError = Color(0xFFFFEBEE),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFCDD2),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
)

/**
 * Theme principal de EduGo.
 *
 * Aplica Material 3 con colores personalizados y tipografía.
 * Soporta modo oscuro automático.
 *
 * @param darkTheme Si debe usar tema oscuro (default: detecta sistema)
 * @param content Contenido de la app
 */
@Composable
fun EduGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        EduGoDarkColors
    } else {
        EduGoLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduGoTypography,
        content = content
    )
}
```

#### 7. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/Typography.kt

```kotlin
package com.edugo.kmp.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía de EduGo basada en Material 3.
 *
 * Usa la fuente del sistema por defecto.
 * Para fuentes personalizadas, importar con Compose Resources.
 */
val EduGoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)
```

#### 8. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/components/DSAlertDialog.kt

```kotlin
package com.edugo.kmp.design.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors

/**
 * Dialog de alerta con estilos consistentes según el tipo de mensaje.
 */
@Composable
fun DSAlertDialog(
    title: String,
    message: String,
    type: MessageType = MessageType.INFO,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (type) {
        MessageType.INFO -> Icons.Default.Info
        MessageType.SUCCESS -> Icons.Default.CheckCircle
        MessageType.WARNING -> Icons.Default.Warning
        MessageType.ERROR -> Icons.Default.Error
    }

    val iconColor = when (type) {
        MessageType.INFO -> SemanticColors.info()
        MessageType.SUCCESS -> SemanticColors.success()
        MessageType.WARNING -> SemanticColors.warning()
        MessageType.ERROR -> SemanticColors.error()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = if (dismissText != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        } else {
            null
        },
        modifier = modifier,
    )
}
```

#### 9. kmp-design/src/commonMain/kotlin/com/edugo/kmp/design/components/DSSnackbar.kt

```kotlin
package com.edugo.kmp.design.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors

/**
 * Snackbar con estilos consistentes según el tipo de mensaje.
 */
@Composable
fun DSSnackbar(
    message: String,
    type: MessageType = MessageType.INFO,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (type) {
        MessageType.INFO -> SemanticColors.infoContainer()
        MessageType.SUCCESS -> SemanticColors.successContainer()
        MessageType.WARNING -> SemanticColors.warningContainer()
        MessageType.ERROR -> SemanticColors.errorContainer()
    }

    val contentColor = when (type) {
        MessageType.INFO -> SemanticColors.onInfoContainer()
        MessageType.SUCCESS -> SemanticColors.onSuccessContainer()
        MessageType.WARNING -> SemanticColors.onWarningContainer()
        MessageType.ERROR -> SemanticColors.onErrorContainer()
    }

    Snackbar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        action = if (actionLabel != null && onActionClick != null) {
            {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel, color = contentColor)
                }
            }
        } else {
            null
        },
    ) {
        Text(message)
    }
}

/**
 * Host para mostrar snackbars con el estilo del design system.
 */
@Composable
fun DSSnackbarHost(
    hostState: SnackbarHostState,
    messageType: MessageType = MessageType.INFO,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        DSSnackbar(
            message = snackbarData.visuals.message,
            type = messageType,
            actionLabel = snackbarData.visuals.actionLabel,
            onActionClick = { snackbarData.performAction() },
            duration = snackbarData.visuals.duration,
        )
    }
}
```

### Verificación

```bash
# Compilar el módulo
./gradlew :kmp-design:build

# Ejecutar tests
./gradlew :kmp-design:test

# Verificar que compile para todos los targets
./gradlew :kmp-design:compileKotlinAndroid
./gradlew :kmp-design:compileKotlinDesktop
./gradlew :kmp-design:compileKotlinWasmJs

# Con iOS habilitado
./gradlew :kmp-design:compileKotlinIosArm64 -PenableIos=true
```

---

## TASK 4.3: Crear Módulo kmp-resources (Strings Multiplatforma)

### Objetivo

Crear el módulo de recursos multilingües usando expect/actual para strings localizados en cada plataforma.

### Archivos a Crear

```
kmp_new/
└── kmp-resources/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/com/edugo/kmp/resources/
        │   └── Strings.kt              # expect object Strings
        ├── androidMain/
        │   ├── kotlin/com/edugo/kmp/resources/
        │   │   └── Strings.android.kt  # actual object Strings
        │   └── res/values/
        │       └── strings.xml         # Recursos Android nativos
        ├── desktopMain/kotlin/com/edugo/kmp/resources/
        │   └── Strings.desktop.kt      # actual object Strings (hardcoded)
        ├── wasmJsMain/kotlin/com/edugo/kmp/resources/
        │   └── Strings.wasm.kt         # actual object Strings (hardcoded)
        └── iosMain/kotlin/com/edugo/kmp/resources/
            └── Strings.ios.kt          # actual object Strings (hardcoded)
```

### Referencia de Código Fuente

**Origen**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/core/resources/`

**Archivos fuente**:
- `src/commonMain/kotlin/.../Strings.kt`
- `src/androidMain/kotlin/.../Strings.android.kt`
- `src/androidMain/res/values/strings.xml`
- `src/desktopMain/kotlin/.../Strings.desktop.kt`
- `src/wasmJsMain/kotlin/.../Strings.wasm.kt`
- `src/iosMain/kotlin/.../Strings.ios.kt`
- `build.gradle.kts`

**Adaptaciones**:
- Cambiar package a `com.edugo.kmp.resources`
- Ajustar strings a la temática de EduGo
- Mantener la estructura expect/actual

### Código Crítico

#### 1. kmp-resources/build.gradle.kts

```kotlin
plugins {
    id("kmp.ui.full")
}

android {
    namespace = "com.edugo.kmp.resources"

    // Configurar directorio de recursos Android
    sourceSets["main"].res.srcDirs("src/androidMain/res")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose runtime para @Composable
                implementation(compose.runtime)
            }
        }

        val androidMain by getting {
            dependencies {
                // Compose UI para LocalContext en Android
                implementation(compose.ui)
            }
        }
    }
}
```

#### 2. kmp-resources/src/commonMain/kotlin/com/edugo/kmp/resources/Strings.kt

```kotlin
package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

/**
 * Interface para acceso a strings localizados multiplataforma
 *
 * Implementación:
 * - Android: usa strings.xml con recursos nativos
 * - iOS/Desktop/Wasm: usa mapas de strings hardcoded (simplificado)
 */
expect object Strings {
    // Splash
    val splash_title: String
    val splash_subtitle: String
    val splash_loading: String

    // Login
    val login_title: String
    val login_email_label: String
    val login_password_label: String
    val login_button: String
    val login_error_empty_fields: String

    // Home
    val home_welcome: String
    val home_subtitle: String
    val home_card_title: String
    val home_card_description: String
    val home_settings_button: String
    val home_logout_button: String

    // Settings
    val settings_title: String
    val settings_theme_section: String
    val settings_theme_light: String
    val settings_theme_dark: String
    val settings_theme_system: String
    val settings_reset_button: String
    val settings_logout_button: String

    // Messaging System
    val message_error_title: String
    val message_error_retry: String
    val message_error_dismiss: String
    val message_success_title: String
    val message_success_ok: String
    val message_warning_title: String
    val message_warning_understood: String
    val message_info_title: String
    val message_info_ok: String

    // Common
    val app_name: String
    val back_button: String
    val error_unknown: String
}

/**
 * Helper composable para obtener strings por clave
 * Útil para casos dinámicos
 */
@Composable
expect fun stringResource(key: String): String
```

#### 3. kmp-resources/src/androidMain/kotlin/com/edugo/kmp/resources/Strings.android.kt

```kotlin
package com.edugo.kmp.resources

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Implementación Android de Strings usando recursos nativos (strings.xml)
 */
actual object Strings {
    private lateinit var appContext: Context

    /**
     * Debe ser inicializado desde Application.onCreate()
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getString(resId: Int): String {
        return if (::appContext.isInitialized) {
            appContext.getString(resId)
        } else {
            "String not initialized"
        }
    }

    // Splash
    actual val splash_title: String get() = getString(R.string.splash_title)
    actual val splash_subtitle: String get() = getString(R.string.splash_subtitle)
    actual val splash_loading: String get() = getString(R.string.splash_loading)

    // Login
    actual val login_title: String get() = getString(R.string.login_title)
    actual val login_email_label: String get() = getString(R.string.login_email_label)
    actual val login_password_label: String get() = getString(R.string.login_password_label)
    actual val login_button: String get() = getString(R.string.login_button)
    actual val login_error_empty_fields: String get() = getString(R.string.login_error_empty_fields)

    // Home
    actual val home_welcome: String get() = getString(R.string.home_welcome)
    actual val home_subtitle: String get() = getString(R.string.home_subtitle)
    actual val home_card_title: String get() = getString(R.string.home_card_title)
    actual val home_card_description: String get() = getString(R.string.home_card_description)
    actual val home_settings_button: String get() = getString(R.string.home_settings_button)
    actual val home_logout_button: String get() = getString(R.string.home_logout_button)

    // Settings
    actual val settings_title: String get() = getString(R.string.settings_title)
    actual val settings_theme_section: String get() = getString(R.string.settings_theme_section)
    actual val settings_theme_light: String get() = getString(R.string.settings_theme_light)
    actual val settings_theme_dark: String get() = getString(R.string.settings_theme_dark)
    actual val settings_theme_system: String get() = getString(R.string.settings_theme_system)
    actual val settings_reset_button: String get() = getString(R.string.settings_reset_button)
    actual val settings_logout_button: String get() = getString(R.string.settings_logout_button)

    // Messaging System
    actual val message_error_title: String get() = getString(R.string.message_error_title)
    actual val message_error_retry: String get() = getString(R.string.message_error_retry)
    actual val message_error_dismiss: String get() = getString(R.string.message_error_dismiss)
    actual val message_success_title: String get() = getString(R.string.message_success_title)
    actual val message_success_ok: String get() = getString(R.string.message_success_ok)
    actual val message_warning_title: String get() = getString(R.string.message_warning_title)
    actual val message_warning_understood: String get() = getString(R.string.message_warning_understood)
    actual val message_info_title: String get() = getString(R.string.message_info_title)
    actual val message_info_ok: String get() = getString(R.string.message_info_ok)

    // Common
    actual val app_name: String get() = getString(R.string.app_name)
    actual val back_button: String get() = getString(R.string.back_button)
    actual val error_unknown: String get() = getString(R.string.error_unknown)
}

@Composable
actual fun stringResource(key: String): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else key
}
```

#### 4. kmp-resources/src/androidMain/res/values/strings.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">EduGo</string>

    <!-- Splash -->
    <string name="splash_title">EduGo</string>
    <string name="splash_subtitle">Plataforma Educativa</string>
    <string name="splash_loading">Cargando…</string>

    <!-- Login -->
    <string name="login_title">Inicio de sesión</string>
    <string name="login_email_label">Email</string>
    <string name="login_password_label">Contraseña</string>
    <string name="login_button">Iniciar sesión</string>
    <string name="login_error_empty_fields">Email y contraseña son requeridos</string>

    <!-- Home -->
    <string name="home_welcome">¡Bienvenido!</string>
    <string name="home_subtitle">Has iniciado sesión exitosamente</string>
    <string name="home_card_title">EduGo KMP</string>
    <string name="home_card_description">Plataforma educativa multiplataforma con Kotlin</string>
    <string name="home_settings_button">Configuración</string>
    <string name="home_logout_button">Cerrar Sesión</string>

    <!-- Settings -->
    <string name="settings_title">Configuración</string>
    <string name="settings_theme_section">Tema</string>
    <string name="settings_theme_light">Claro</string>
    <string name="settings_theme_dark">Oscuro</string>
    <string name="settings_theme_system">Sistema</string>
    <string name="settings_reset_button">Restablecer configuración</string>
    <string name="settings_logout_button">Cerrar Sesión</string>

    <!-- Messaging System -->
    <string name="message_error_title">Error</string>
    <string name="message_error_retry">Reintentar</string>
    <string name="message_error_dismiss">Cerrar</string>
    <string name="message_success_title">Éxito</string>
    <string name="message_success_ok">Aceptar</string>
    <string name="message_warning_title">Advertencia</string>
    <string name="message_warning_understood">Entendido</string>
    <string name="message_info_title">Información</string>
    <string name="message_info_ok">Aceptar</string>

    <!-- Common -->
    <string name="back_button">Volver</string>
    <string name="error_unknown">Ocurrió un error inesperado</string>
</resources>
```

#### 5. kmp-resources/src/desktopMain/kotlin/com/edugo/kmp/resources/Strings.desktop.kt

```kotlin
package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

/**
 * Implementación Desktop de Strings usando mapa hardcoded
 */
actual object Strings {
    actual val splash_title: String = "EduGo"
    actual val splash_subtitle: String = "Plataforma Educativa"
    actual val splash_loading: String = "Cargando…"

    actual val login_title: String = "Inicio de sesión"
    actual val login_email_label: String = "Email"
    actual val login_password_label: String = "Contraseña"
    actual val login_button: String = "Iniciar sesión"
    actual val login_error_empty_fields: String = "Email y contraseña son requeridos"

    actual val home_welcome: String = "¡Bienvenido!"
    actual val home_subtitle: String = "Has iniciado sesión exitosamente"
    actual val home_card_title: String = "EduGo KMP"
    actual val home_card_description: String = "Plataforma educativa multiplataforma con Kotlin"
    actual val home_settings_button: String = "Configuración"
    actual val home_logout_button: String = "Cerrar Sesión"

    actual val settings_title: String = "Configuración"
    actual val settings_theme_section: String = "Tema"
    actual val settings_theme_light: String = "Claro"
    actual val settings_theme_dark: String = "Oscuro"
    actual val settings_theme_system: String = "Sistema"
    actual val settings_reset_button: String = "Restablecer configuración"
    actual val settings_logout_button: String = "Cerrar Sesión"

    actual val message_error_title: String = "Error"
    actual val message_error_retry: String = "Reintentar"
    actual val message_error_dismiss: String = "Cerrar"
    actual val message_success_title: String = "Éxito"
    actual val message_success_ok: String = "Aceptar"
    actual val message_warning_title: String = "Advertencia"
    actual val message_warning_understood: String = "Entendido"
    actual val message_info_title: String = "Información"
    actual val message_info_ok: String = "Aceptar"

    actual val app_name: String = "EduGo"
    actual val back_button: String = "Volver"
    actual val error_unknown: String = "Ocurrió un error inesperado"
}

@Composable
actual fun stringResource(key: String): String = key
```

#### 6. kmp-resources/src/wasmJsMain/kotlin/com/edugo/kmp/resources/Strings.wasm.kt

```kotlin
package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

/**
 * Implementación Wasm de Strings usando mapa hardcoded
 */
actual object Strings {
    actual val splash_title: String = "EduGo"
    actual val splash_subtitle: String = "Plataforma Educativa"
    actual val splash_loading: String = "Cargando…"

    actual val login_title: String = "Inicio de sesión"
    actual val login_email_label: String = "Email"
    actual val login_password_label: String = "Contraseña"
    actual val login_button: String = "Iniciar sesión"
    actual val login_error_empty_fields: String = "Email y contraseña son requeridos"

    actual val home_welcome: String = "¡Bienvenido!"
    actual val home_subtitle: String = "Has iniciado sesión exitosamente"
    actual val home_card_title: String = "EduGo KMP"
    actual val home_card_description: String = "Plataforma educativa multiplataforma con Kotlin"
    actual val home_settings_button: String = "Configuración"
    actual val home_logout_button: String = "Cerrar Sesión"

    actual val settings_title: String = "Configuración"
    actual val settings_theme_section: String = "Tema"
    actual val settings_theme_light: String = "Claro"
    actual val settings_theme_dark: String = "Oscuro"
    actual val settings_theme_system: String = "Sistema"
    actual val settings_reset_button: String = "Restablecer configuración"
    actual val settings_logout_button: String = "Cerrar Sesión"

    actual val message_error_title: String = "Error"
    actual val message_error_retry: String = "Reintentar"
    actual val message_error_dismiss: String = "Cerrar"
    actual val message_success_title: String = "Éxito"
    actual val message_success_ok: String = "Aceptar"
    actual val message_warning_title: String = "Advertencia"
    actual val message_warning_understood: String = "Entendido"
    actual val message_info_title: String = "Información"
    actual val message_info_ok: String = "Aceptar"

    actual val app_name: String = "EduGo"
    actual val back_button: String = "Volver"
    actual val error_unknown: String = "Ocurrió un error inesperado"
}

@Composable
actual fun stringResource(key: String): String = key
```

#### 7. kmp-resources/src/iosMain/kotlin/com/edugo/kmp/resources/Strings.ios.kt

```kotlin
package com.edugo.kmp.resources

import androidx.compose.runtime.Composable

/**
 * Implementación iOS de Strings usando mapa hardcoded
 */
actual object Strings {
    actual val splash_title: String = "EduGo"
    actual val splash_subtitle: String = "Plataforma Educativa"
    actual val splash_loading: String = "Cargando…"

    actual val login_title: String = "Inicio de sesión"
    actual val login_email_label: String = "Email"
    actual val login_password_label: String = "Contraseña"
    actual val login_button: String = "Iniciar sesión"
    actual val login_error_empty_fields: String = "Email y contraseña son requeridos"

    actual val home_welcome: String = "¡Bienvenido!"
    actual val home_subtitle: String = "Has iniciado sesión exitosamente"
    actual val home_card_title: String = "EduGo KMP"
    actual val home_card_description: String = "Plataforma educativa multiplataforma con Kotlin"
    actual val home_settings_button: String = "Configuración"
    actual val home_logout_button: String = "Cerrar Sesión"

    actual val settings_title: String = "Configuración"
    actual val settings_theme_section: String = "Tema"
    actual val settings_theme_light: String = "Claro"
    actual val settings_theme_dark: String = "Oscuro"
    actual val settings_theme_system: String = "Sistema"
    actual val settings_reset_button: String = "Restablecer configuración"
    actual val settings_logout_button: String = "Cerrar Sesión"

    actual val message_error_title: String = "Error"
    actual val message_error_retry: String = "Reintentar"
    actual val message_error_dismiss: String = "Cerrar"
    actual val message_success_title: String = "Éxito"
    actual val message_success_ok: String = "Aceptar"
    actual val message_warning_title: String = "Advertencia"
    actual val message_warning_understood: String = "Entendido"
    actual val message_info_title: String = "Información"
    actual val message_info_ok: String = "Aceptar"

    actual val app_name: String = "EduGo"
    actual val back_button: String = "Volver"
    actual val error_unknown: String = "Ocurrió un error inesperado"
}

@Composable
actual fun stringResource(key: String): String = key
```

### Inicialización Android

Para Android, agregar en la clase Application:

```kotlin
// En platforms/mobile/app/src/androidMain/kotlin/.../EduGoApplication.kt
class EduGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar strings con contexto
        Strings.init(this)

        // Inicializar Koin...
    }
}
```

### Verificación

```bash
# Compilar el módulo
./gradlew :kmp-resources:build

# Verificar targets
./gradlew :kmp-resources:compileKotlinAndroid
./gradlew :kmp-resources:compileKotlinDesktop
./gradlew :kmp-resources:compileKotlinWasmJs
./gradlew :kmp-resources:compileKotlinIosArm64 -PenableIos=true
```

---

## TASK 4.4: Crear Módulo kmp-navigation (dentro de kmp-screens)

### Objetivo

Crear el sistema de navegación multiplataforma personalizado usando NavigationState con backstack y soporte de serialización.

### Archivos a Crear

```
kmp_new/
└── kmp-screens/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/com/edugo/kmp/screens/
        │   ├── navigation/
        │   │   ├── Navigation.kt      # NavigationState + NavigationHost
        │   │   └── Routes.kt           # sealed class Route
        │   └── App.kt                  # Composable principal
        └── commonTest/kotlin/com/edugo/kmp/screens/
            └── navigation/
                └── NavigationStateTest.kt
```

### Referencia de Código Fuente

**Origen**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/ui/screens/src/commonMain/kotlin/.../ui/`

**Archivos fuente**:
- `navigation/Navigation.kt`
- `navigation/Routes.kt`
- `App.kt`
- `../build.gradle.kts`

**Adaptaciones**:
- Cambiar package a `com.edugo.kmp.screens`
- Mantener NavigationState con backstack funcional
- Routes adaptadas a la app (Splash, Login, Home, Settings)

### Código Crítico

#### 1. kmp-screens/build.gradle.kts

```kotlin
plugins {
    id("kmp.ui.full")
}

android {
    namespace = "com.edugo.kmp.screens"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose incluido por kmp.ui.full

                // Módulos del proyecto
                implementation(project(":kmp-design"))
                implementation(project(":kmp-resources"))
                implementation(project(":kmp-di"))  // Dependency Injection

                // Koin incluido por kmp.ui.full
                // Lifecycle ViewModel incluido por kmp.ui.full
                // Coroutines incluido por kmp.ui.full
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.findLibrary("kotlinx-coroutines-test")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-test' not found") })
            }
        }
    }
}
```

#### 2. kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/navigation/Routes.kt

```kotlin
package com.edugo.kmp.screens.navigation

/**
 * Rutas de navegación de la aplicación.
 *
 * Sealed class que define todas las pantallas navegables.
 * Cada ruta tiene un path único para serialización.
 */
sealed class Route(val path: String) {
    /**
     * Pantalla de splash - Primera pantalla al iniciar la app.
     * Auto-navega a Login o Home después de verificar sesión.
     */
    data object Splash : Route("splash")

    /**
     * Pantalla de login - Autenticación de usuario.
     */
    data object Login : Route("login")

    /**
     * Pantalla principal - Home después de login exitoso.
     */
    data object Home : Route("home")

    /**
     * Pantalla de configuración - Settings de la aplicación.
     */
    data object Settings : Route("settings")

    companion object {
        /**
         * Convierte un path string a Route.
         *
         * @param path Path de la ruta (ej: "splash", "login", "home")
         * @return Route correspondiente o null si el path es inválido
         */
        fun fromPath(path: String): Route? =
            when (path.trim()) {
                "splash" -> Splash
                "login" -> Login
                "home" -> Home
                "settings" -> Settings
                else -> null
            }
    }
}
```

#### 3. kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/navigation/Navigation.kt

```kotlin
package com.edugo.kmp.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Estado de navegación unificado para todas las plataformas con soporte de backstack.
 *
 * Features:
 * - Backstack funcional: mantiene historial de navegación
 * - Back navigation: navega hacia atrás en el backstack
 * - State persistence: serialización para restauración tras background
 *
 * Ejemplo:
 * ```
 * val navState = NavigationState()
 * navState.navigateTo(Route.Login)      // Backstack: [Splash, Login]
 * navState.navigateTo(Route.Home)       // Backstack: [Splash, Login, Home]
 * navState.back()                       // Backstack: [Splash, Login], retorna true
 * navState.back()                       // Backstack: [Splash], retorna true
 * navState.back()                       // Backstack: [Splash], retorna false (root)
 * ```
 */
class NavigationState(private val initialRoute: Route = Route.Splash) {
    private val _backstack: MutableState<List<Route>> = mutableStateOf(listOf(initialRoute))
    private val _currentRoute: MutableState<Route> = mutableStateOf(initialRoute)

    /**
     * Ruta actual (última en el backstack) como State observable.
     * Compose recompondrá automáticamente cuando este valor cambie.
     */
    val currentRoute: Route
        get() = _currentRoute.value

    /**
     * Tamaño del backstack (útil para debugging y tests)
     */
    val backstackSize: Int
        get() = _backstack.value.size

    /**
     * Backstack completo (solo lectura para tests/debugging)
     */
    val backstack: List<Route>
        get() = _backstack.value.toList()

    /**
     * Navega a una nueva ruta agregándola al backstack.
     */
    fun navigateTo(route: Route) {
        _backstack.value = _backstack.value + route
        _currentRoute.value = route
    }

    /**
     * Navega hacia atrás eliminando la ruta actual del backstack.
     *
     * @return true si se navegó hacia atrás, false si ya estamos en el root
     */
    fun back(): Boolean {
        return if (_backstack.value.size > 1) {
            _backstack.value = _backstack.value.dropLast(1)
            _currentRoute.value = _backstack.value.lastOrNull() ?: initialRoute
            true
        } else {
            false
        }
    }

    /**
     * Navega hacia atrás hasta una ruta específica, eliminando todas las rutas
     * intermedias del backstack.
     *
     * @param route Ruta de destino
     * @return true si se navegó, false si la ruta no existe en el backstack
     */
    fun popTo(route: Route): Boolean {
        val index = _backstack.value.indexOfLast { it.path == route.path }
        return if (index >= 0) {
            _backstack.value = _backstack.value.take(index + 1)
            _currentRoute.value = route
            true
        } else {
            false
        }
    }

    /**
     * Serializa el estado del backstack para restauración tras background.
     *
     * Formato: rutas separadas por coma (ej: "splash,login,home")
     */
    fun saveState(): String {
        return _backstack.value.joinToString(",") { it.path }
    }

    /**
     * Restaura el estado del backstack desde una cadena serializada.
     *
     * @return true si el backstack fue actualizado, false si se ignoró
     */
    fun restoreState(state: String): Boolean {
        if (state.isBlank()) return false

        val routes = state.split(",").mapNotNull { Route.fromPath(it) }

        return if (routes.isNotEmpty()) {
            _backstack.value = routes
            _currentRoute.value = routes.lastOrNull() ?: Route.Splash
            true
        } else {
            false
        }
    }
}

/**
 * Navigator composable para manejo de navegación multiplataforma.
 *
 * @param navigationState Estado de navegación
 * @param startRoute Ruta inicial (ignorada si navigationState ya tiene una ruta)
 * @param content Contenido que recibe el navigationState
 */
@Composable
fun NavigationHost(
    navigationState: NavigationState = remember { NavigationState() },
    startRoute: Route = Route.Splash,
    content: @Composable (NavigationState) -> Unit,
) {
    content(navigationState)
}
```

#### 4. kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/App.kt

```kotlin
package com.edugo.kmp.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.di.allModules
import com.edugo.kmp.screens.navigation.NavigationState
import com.edugo.kmp.screens.navigation.Route
import com.edugo.kmp.screens.ui.HomeScreen
import com.edugo.kmp.screens.ui.LoginScreen
import com.edugo.kmp.screens.ui.SettingsScreen
import com.edugo.kmp.screens.ui.SplashScreen
import org.koin.compose.KoinApplication

/**
 * Componente principal de la aplicación compartido entre plataformas.
 *
 * Inicializa:
 * - Koin con módulos de DI
 * - EduGoTheme (Material 3)
 * - Navegación entre pantallas
 *
 * Flujo de navegación:
 * - Splash (2s) → Login o Home (según sesión)
 * - Login → Home
 * - Home → Settings o Logout
 * - Settings → Back → Home
 *
 * Compatible con Android, iOS, Desktop y WASM.
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(allModules())
    }) {
        EduGoTheme {
            val navState = remember { NavigationState() }

            when (navState.currentRoute) {
                Route.Splash -> SplashScreen(
                    onNavigateToLogin = { navState.navigateTo(Route.Login) },
                    onNavigateToHome = { navState.navigateTo(Route.Home) }
                )

                Route.Login -> LoginScreen(
                    onLoginSuccess = { navState.navigateTo(Route.Home) }
                )

                Route.Home -> HomeScreen(
                    onNavigateToSettings = { navState.navigateTo(Route.Settings) },
                    onLogout = { navState.popTo(Route.Login) }
                )

                Route.Settings -> SettingsScreen(
                    onBack = { navState.back() },
                    onLogout = { navState.popTo(Route.Login) }
                )
            }
        }
    }
}
```

### Verificación

```bash
# Compilar navegación
./gradlew :kmp-screens:build

# Ejecutar tests de navegación
./gradlew :kmp-screens:test

# Verificar que compile para todos los targets
./gradlew :kmp-screens:compileKotlinAndroid
./gradlew :kmp-screens:compileKotlinDesktop
./gradlew :kmp-screens:compileKotlinWasmJs
```

---

## TASK 4.5: Crear App de Ejemplo (Platforms)

### Objetivo

Crear aplicaciones de ejemplo para cada plataforma (Android, Desktop, WASM, iOS) con entry points y pantallas básicas.

### Estructura de Archivos

```
kmp_new/
├── platforms/
│   ├── mobile/
│   │   └── app/
│   │       ├── build.gradle.kts         # Android App
│   │       └── src/
│   │           ├── androidMain/
│   │           │   ├── kotlin/com/edugo/android/
│   │           │   │   ├── EduGoApplication.kt
│   │           │   │   └── MainActivity.kt
│   │           │   ├── res/
│   │           │   │   └── values/
│   │           │   │       └── strings.xml
│   │           │   └── AndroidManifest.xml
│   │           └── iosMain/kotlin/com/edugo/
│   │               └── Main.kt          # iOS entry point
│   ├── desktop/
│   │   └── app/
│   │       ├── build.gradle.kts         # Desktop App
│   │       └── src/
│   │           └── desktopMain/kotlin/com/edugo/desktop/
│   │               └── Main.kt
│   └── web/
│       └── app/
│           ├── build.gradle.kts         # WASM App
│           └── src/
│               └── wasmJsMain/
│                   ├── kotlin/com/edugo/web/
│                   │   └── Main.kt
│                   └── resources/
│                       └── index.html
└── kmp-screens/
    └── src/commonMain/kotlin/com/edugo/kmp/screens/ui/
        ├── SplashScreen.kt
        ├── LoginScreen.kt
        ├── HomeScreen.kt
        └── SettingsScreen.kt
```

### Referencia de Código Fuente

**Origen Android**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/platforms/mobile/app/`
**Origen Desktop**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/platforms/desktop/app/`
**Origen WASM**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/platforms/web/app/`
**Origen iOS**: `/Users/jhoanmedina/source/ProjectAndroid/Template-Kmp-Clean/platforms/mobile/app/src/iosMain/`

**Archivos fuente de pantallas**:
- `ui/screens/src/commonMain/kotlin/.../ui/screens/SplashScreen.kt`
- `ui/screens/src/commonMain/kotlin/.../ui/screens/LoginScreen.kt`
- `ui/screens/src/commonMain/kotlin/.../ui/screens/HomeScreen.kt`
- `features/settings/presentation/src/commonMain/kotlin/.../screens/SettingsScreen.kt`

### Código Crítico

#### 1. platforms/mobile/app/build.gradle.kts (Android + iOS)

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }

    // iOS ON-DEMAND
    val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
    if (enableIos) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "EduGoApp"
                isStatic = true
                binaryOption("bundleId", "com.edugo.app")
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.findLibrary("androidx-activity-compose")
                .orElseThrow { IllegalStateException("Library not found") })
            implementation(libs.findLibrary("koin-android")
                .orElseThrow { IllegalStateException("Library not found") })
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Módulos del proyecto
            implementation(project(":kmp-screens"))
            implementation(project(":kmp-design"))
            implementation(project(":kmp-resources"))
            implementation(project(":kmp-di"))

            // Koin
            implementation(libs.findLibrary("koin-core")
                .orElseThrow { IllegalStateException("Library not found") })
            implementation(libs.findLibrary("koin-compose")
                .orElseThrow { IllegalStateException("Library not found") })
        }

        if (enableIos) {
            iosMain.dependencies {
                // iOS-specific si es necesario
            }
        }
    }
}

android {
    namespace = "com.edugo.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.edugo.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}
```

#### 2. platforms/mobile/app/src/androidMain/kotlin/com/edugo/android/MainActivity.kt

```kotlin
package com.edugo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edugo.kmp.screens.App

/**
 * MainActivity - Punto de entrada de la aplicación Android.
 *
 * Usa el componente App compartido que gestiona:
 * - Koin DI
 * - EduGoTheme
 * - Navegación: Splash → Login → Home → Settings
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}
```

#### 3. platforms/mobile/app/src/androidMain/kotlin/com/edugo/android/EduGoApplication.kt

```kotlin
package com.edugo.android

import android.app.Application
import com.edugo.kmp.resources.Strings

/**
 * Application class para Android.
 *
 * Inicializa:
 * - Strings con contexto Android
 * - Koin (se inicializa en App composable, pero se puede hacer aquí también)
 */
class EduGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar strings con contexto
        Strings.init(this)
    }
}
```

#### 4. platforms/mobile/app/src/androidMain/AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".EduGoApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

#### 5. platforms/mobile/app/src/iosMain/kotlin/com/edugo/Main.kt

```kotlin
package com.edugo

import androidx.compose.ui.window.ComposeUIViewController
import com.edugo.kmp.screens.App
import platform.UIKit.UIViewController

/**
 * Punto de entrada para iOS.
 * Esta función es llamada desde Swift para crear el UIViewController principal.
 */
fun MainViewController(): UIViewController {
    return ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        }
    ) {
        App()
    }
}
```

#### 6. platforms/desktop/app/build.gradle.kts

```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                // Módulos del proyecto
                implementation(project(":kmp-screens"))
                implementation(project(":kmp-design"))
                implementation(project(":kmp-resources"))
                implementation(project(":kmp-di"))

                // Compose Desktop
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                // Koin
                implementation(libs.findLibrary("koin-core")
                    .orElseThrow { IllegalStateException("Library not found") })

                // Coroutines
                implementation(libs.findLibrary("kotlinx-coroutines-core")
                    .orElseThrow { IllegalStateException("Library not found") })
                implementation(libs.findLibrary("kotlinx-coroutines-swing")
                    .orElseThrow { IllegalStateException("Library not found") })
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.edugo.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EduGo"
            packageVersion = "1.0.0"
            description = "EduGo - Plataforma Educativa Multiplataforma"
            vendor = "EduGo Team"

            macOS {
                iconFile.set(project.file("icon.icns"))
            }
            windows {
                iconFile.set(project.file("icon.ico"))
            }
            linux {
                iconFile.set(project.file("icon.png"))
            }
        }
    }
}
```

#### 7. platforms/desktop/app/src/desktopMain/kotlin/com/edugo/desktop/Main.kt

```kotlin
package com.edugo.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.edugo.kmp.resources.Strings
import com.edugo.kmp.screens.App

/**
 * Punto de entrada de la aplicación Desktop.
 *
 * Inicializa:
 * - Window con título
 * - App composable compartido
 */
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = Strings.app_name
        ) {
            App()
        }
    }
}
```

#### 8. platforms/web/app/build.gradle.kts

```kotlin
@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                // Módulos del proyecto
                implementation(project(":kmp-screens"))
                implementation(project(":kmp-design"))
                implementation(project(":kmp-resources"))
                implementation(project(":kmp-di"))

                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Koin
                implementation(libs.findLibrary("koin-core")
                    .orElseThrow { IllegalStateException("Library not found") })
                implementation(libs.findLibrary("koin-compose")
                    .orElseThrow { IllegalStateException("Library not found") })

                // Coroutines
                implementation(libs.findLibrary("kotlinx-coroutines-core")
                    .orElseThrow { IllegalStateException("Library not found") })
            }
        }
    }
}
```

#### 9. platforms/web/app/src/wasmJsMain/kotlin/com/edugo/web/Main.kt

```kotlin
package com.edugo.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.edugo.kmp.screens.App
import kotlinx.browser.document

/**
 * Punto de entrada de la aplicación Web (Wasm).
 *
 * Renderiza el componente App compartido en el DOM.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
```

#### 10. platforms/web/app/src/wasmJsMain/resources/index.html

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduGo - Plataforma Educativa</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background-color: #fafafa;
        }
        #root {
            width: 100%;
            height: 100vh;
        }
    </style>
</head>
<body>
    <div id="root"></div>
    <script src="app.js"></script>
</body>
</html>
```

### Pantallas Básicas (Ejemplo: SplashScreen)

#### kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/ui/SplashScreen.kt

```kotlin
package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Alpha
import com.edugo.kmp.design.Durations
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.resources.Strings
import kotlinx.coroutines.delay

/**
 * Pantalla de splash - Primera pantalla al iniciar la aplicación.
 *
 * Verifica si hay sesión activa:
 * - Si hay sesión → navega a Home
 * - Si no hay sesión → navega a Login
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    delayMs: Long = Durations.splash,
) {
    // TODO: Inyectar PreferencesManager con Koin para verificar sesión
    val isLoggedIn = false  // Placeholder

    LaunchedEffect(Unit) {
        delay(delayMs)

        if (isLoggedIn) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = Strings.splash_title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.m)
            )

            Text(
                text = Strings.splash_subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Alpha.subtle),
                modifier = Modifier.padding(bottom = Spacing.xxl)
            )

            CircularProgressIndicator(
                modifier = Modifier.size(Sizes.progressLarge),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = Strings.splash_loading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Alpha.muted)
            )
        }
    }
}
```

**Nota**: Las otras pantallas (LoginScreen, HomeScreen, SettingsScreen) siguen un patrón similar. Copiar de Template y adaptar packages y strings.

### Verificación

```bash
# Android
./gradlew :platforms:mobile:app:assembleDebug
./gradlew :platforms:mobile:app:installDebug

# Desktop
./gradlew :platforms:desktop:app:run

# WASM
./gradlew :platforms:web:app:wasmJsBrowserDevelopmentRun

# iOS (requiere macOS)
./gradlew :platforms:mobile:app:iosSimulatorArm64Binaries -PenableIos=true
```

---

## Diagrama de Dependencias Completo (Sprint 1-4)

```
┌─────────────────────────────────────────────────────────────┐
│                    PLATFORMS (Sprint 4)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ mobile/app   │  │ desktop/app  │  │   web/app    │      │
│  │  (Android+   │  │    (JVM)     │  │   (WASM)     │      │
│  │    iOS)      │  │              │  │              │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                  UI LAYER (Sprint 4)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ kmp-screens  │  │ kmp-design   │  │kmp-resources │      │
│  │(Navigation,  │  │(Theme,       │  │(Strings      │      │
│  │ App, UI)     │  │ Tokens)      │  │ i18n)        │      │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘      │
└─────────┼──────────────────┼──────────────────────────────┘
          │                  │
          └──────────────────┘
                   ▼
┌─────────────────────────────────────────────────────────────┐
│              FEATURES LAYER (Sprint 3)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  kmp-auth    │  │kmp-settings  │  │ kmp-users    │      │
│  │ (Login, JWT, │  │ (Prefs,      │  │ (Profiles,   │      │
│  │  Session)    │  │  Theme)      │  │  Roles)      │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────┐
│            BUSINESS LOGIC LAYER (Sprint 2)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ kmp-network  │  │ kmp-storage  │  │ kmp-security │      │
│  │ (Ktor, API,  │  │ (KV Store,   │  │ (JWT, Hash,  │      │
│  │  Retry)      │  │  Reactive)   │  │  Encrypt)    │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              FOUNDATION LAYER (Sprint 1)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │kmp-foundation│  │  kmp-logger  │  │  kmp-core    │      │
│  │ (Result,     │  │  (Kermit,    │  │ (Platform,   │      │
│  │ AppError)    │  │   Tagged)    │  │ Dispatchers) │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    SHARED (Sprint 1)                        │
│         ┌──────────────┐  ┌──────────────┐                 │
│         │kmp-validation│  │   kmp-di     │                 │
│         │ (Email, UUID,│  │   (Koin      │                 │
│         │  Patterns)   │  │   Modules)   │                 │
│         └──────────────┘  └──────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

### Leyenda de Dependencias

- **→** : Depende de
- **Plataformas** dependen de **UI Layer**
- **UI Layer** depende de **Features** + **Foundation**
- **Features** dependen de **Business Logic** + **Foundation**
- **Business Logic** depende de **Foundation**
- **Foundation** es la base (no depende de nadie)

---

## Orden de Ejecución Sprint 4

### Prerequisitos

- ✅ Sprint 1 completado (foundation, logger, core, validation)
- ✅ Sprint 2 completado (network, storage, security)
- ✅ Sprint 3 completado (auth, settings, users)

### Orden de Tareas

1. **Task 4.1**: Convention Plugin kmp.ui.full
   - Crear `build-logic/src/main/kotlin/kmp.ui.full.gradle.kts`
   - Verificar que compila
   - Duración: 2-3 horas

2. **Task 4.2**: Módulo kmp-design
   - Crear design tokens (Spacing, Sizes, Alpha, etc.)
   - Crear SemanticColors
   - Crear EduGoTheme
   - Crear componentes (DSAlertDialog, DSSnackbar)
   - Duración: 4-6 horas

3. **Task 4.3**: Módulo kmp-resources
   - Crear Strings expect/actual
   - Crear strings.xml para Android
   - Duplicar strings en Desktop/WASM/iOS
   - Duración: 3-4 horas

4. **Task 4.4**: Navegación (dentro de kmp-screens)
   - Crear Routes sealed class
   - Crear NavigationState con backstack
   - Crear NavigationHost
   - Crear App composable
   - Duración: 3-4 horas

5. **Task 4.5**: Aplicaciones de ejemplo
   - Crear entry points por plataforma
   - Crear pantallas básicas (Splash, Login, Home, Settings)
   - Configurar AndroidManifest, build configs
   - Duración: 6-8 horas

### Verificación Final

```bash
# Compilar todo el proyecto
./gradlew build

# Ejecutar tests
./gradlew test

# Android
./gradlew :platforms:mobile:app:assembleDebug
adb install platforms/mobile/app/build/outputs/apk/debug/app-debug.apk

# Desktop
./gradlew :platforms:desktop:app:run

# WASM
./gradlew :platforms:web:app:wasmJsBrowserDevelopmentRun
# Abrir http://localhost:8080

# iOS (macOS)
./gradlew :platforms:mobile:app:iosSimulatorArm64Binaries -PenableIos=true
```

---

## Checklist Global Sprint 4

### Convention Plugin UI
- [ ] kmp.ui.full.gradle.kts creado
- [ ] Configura Compose Multiplatform
- [ ] Soporta Android, Desktop, WASM, iOS on-demand
- [ ] Incluye Koin Compose y Lifecycle ViewModel
- [ ] Compila sin errores

### Módulo kmp-design
- [ ] Tokens.kt (Spacing, Sizes, Alpha, Durations, Radius)
- [ ] SemanticColors.kt (Success, Warning, Error, Info)
- [ ] Elevation.kt (Niveles Material 3)
- [ ] MessageType.kt (Enum)
- [ ] EduGoTheme.kt (Light/Dark schemes)
- [ ] Typography.kt (Material 3 typography)
- [ ] DSAlertDialog.kt (Componente)
- [ ] DSSnackbar.kt (Componente)
- [ ] Tests básicos

### Módulo kmp-resources
- [ ] Strings.kt (expect object)
- [ ] Strings.android.kt (actual con R.string)
- [ ] strings.xml para Android
- [ ] Strings.desktop.kt (actual hardcoded)
- [ ] Strings.wasm.kt (actual hardcoded)
- [ ] Strings.ios.kt (actual hardcoded)
- [ ] stringResource() expect/actual
- [ ] Inicialización en EduGoApplication (Android)

### Módulo kmp-screens (Navegación)
- [ ] Routes.kt (sealed class)
- [ ] Navigation.kt (NavigationState + NavigationHost)
- [ ] App.kt (Composable principal)
- [ ] Tests de NavigationState
- [ ] SplashScreen.kt
- [ ] LoginScreen.kt
- [ ] HomeScreen.kt
- [ ] SettingsScreen.kt

### Plataforma Android
- [ ] build.gradle.kts configurado
- [ ] MainActivity.kt
- [ ] EduGoApplication.kt
- [ ] AndroidManifest.xml
- [ ] Compila y se instala
- [ ] Navegación funciona

### Plataforma Desktop
- [ ] build.gradle.kts configurado
- [ ] Main.kt entry point
- [ ] Compila y ejecuta
- [ ] Window se abre correctamente
- [ ] Navegación funciona

### Plataforma WASM
- [ ] build.gradle.kts configurado
- [ ] Main.kt entry point
- [ ] index.html
- [ ] Compila y se sirve en browser
- [ ] Navegación funciona

### Plataforma iOS (on-demand)
- [ ] Main.kt para iOS
- [ ] Compila con -PenableIos=true
- [ ] Framework se genera

---

## Problemas Potenciales y Soluciones

### Problema 1: Compose Compiler Version Mismatch

**Síntoma**: Error de compilación sobre versión de Compose Compiler incompatible con Kotlin.

**Solución**:
```kotlin
// En gradle/libs.versions.toml
[versions]
kotlin = "2.2.20"
compose = "1.9.0"
compose-compiler = "1.5.15"  # Usar versión compatible
```

### Problema 2: iOS No Compila en Linux/Windows

**Síntoma**: Error al intentar compilar targets iOS sin macOS.

**Solución**:
- Usar flag `-PenableIos=false` por defecto
- Solo habilitar iOS en macOS
- CI/CD debe detectar plataforma antes de compilar iOS

### Problema 3: WASM Index.html No se Encuentra

**Síntoma**: Navegador muestra 404 al abrir app WASM.

**Solución**:
```bash
# Verificar que index.html está en resources
ls platforms/web/app/src/wasmJsMain/resources/index.html

# Limpiar y rebuild
./gradlew :platforms:web:app:clean
./gradlew :platforms:web:app:wasmJsBrowserDevelopmentRun
```

### Problema 4: Strings No Inicializados en Android

**Síntoma**: App crash con "String not initialized".

**Solución**:
```kotlin
// Verificar que EduGoApplication.onCreate() llama a:
Strings.init(this)

// Verificar que Application está en AndroidManifest:
<application
    android:name=".EduGoApplication"
    ...>
```

### Problema 5: Navigation Backstack No Funciona

**Síntoma**: Botón back no navega correctamente.

**Solución**:
- Verificar que NavigationState usa `remember { NavigationState() }`
- No crear nuevo NavigationState en cada recomposición
- Usar `navState.back()` correctamente

### Problema 6: Theme No se Aplica

**Síntoma**: Colores y estilos no coinciden con EduGoTheme.

**Solución**:
```kotlin
// Verificar que App.kt envuelve todo en EduGoTheme:
@Composable
fun App() {
    KoinApplication(...) {
        EduGoTheme {  // ← IMPORTANTE
            // Navegación...
        }
    }
}
```

### Problema 7: Koin No Encuentra Módulos

**Síntoma**: Error "No definition found for class X".

**Solución**:
```kotlin
// Verificar que kmp-di/allModules() incluye todos los módulos:
fun allModules() = listOf(
    foundationModule(),
    loggerModule(),
    networkModule(),
    authModule(),
    settingsModule(),
    // ... etc
)
```

---

## Notas Finales

### Buenas Prácticas UI

1. **Siempre usar Design Tokens**: No hardcodear valores (16.dp → Spacing.m)
2. **Preview Composables**: Agregar `@Preview` en composables para Desktop/Android
3. **Test Tags**: Usar `Modifier.testTag()` para UI testing
4. **Semantic Colors**: Usar SemanticColors para mensajes (no colores directos)
5. **Navigation State**: Siempre usar `remember` para NavigationState

### Optimizaciones Futuras

1. **Compose Resources**: Migrar strings a Compose Resources en vez de expect/actual
2. **Navigation Args**: Agregar soporte para argumentos en Routes (sealed class con data)
3. **Deep Links**: Implementar deep linking para web/mobile
4. **Animations**: Agregar transiciones entre pantallas
5. **State Management**: Considerar ViewModel para estados complejos

### Testing UI

```bash
# Android UI Tests
./gradlew :kmp-screens:connectedAndroidTest

# Screenshot tests (Paparazzi/Shot)
./gradlew :kmp-screens:verifyPaparazziDebug

# Accessibility tests
./gradlew :kmp-screens:lintDebug
```

---

## Resumen Ejecutivo Final

Sprint 4 completa la capa de presentación del proyecto EduGo KMP con:

- **1 convention plugin UI** (kmp.ui.full) para reutilizar configuración Compose
- **3 módulos UI** (design, resources, screens/navigation) con 100% código compartido
- **4 plataformas funcionales** (Android, Desktop, WASM, iOS on-demand)
- **Sistema de navegación custom** sin dependencias de Navigation Compose
- **Design system completo** con Material 3 y tokens consistentes

**Total de archivos**: ~40 archivos
**Líneas de código**: ~3500 líneas
**Plataformas soportadas**: 4 (5 con iOS)
**Reutilización de código UI**: 95%+ (solo entry points son específicos)

Este sprint permite **ejecutar la misma app en 4 plataformas** sin cambios en la lógica UI, usando el poder de Compose Multiplatform y la arquitectura modular establecida en Sprints 1-3.
