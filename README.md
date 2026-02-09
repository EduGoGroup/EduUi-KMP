# EduGo KMP Monorepo

A comprehensive Kotlin Multiplatform library providing foundational utilities, core services, logging, and validation for educational applications across multiple platforms.

## Overview

This monorepo contains four essential modules that form the foundation of the EduGo platform's shared code infrastructure. Built with Kotlin Multiplatform, it enables seamless code sharing across Android, Desktop (JVM), and Web (wasmJs) platforms.

## Modules

### 1. Foundation (`modules/foundation`)
**Package**: `com.edugo.kmp.foundation.*`

The foundational layer providing core abstractions and utilities:

- **Result Monad**: Type-safe error handling with `Result<T>` (Success, Failure, Loading)
- **Entity Base Interfaces**: `EntityBase`, `ValidatableModel`, `AuditableModel`, `SoftDeletable`
- **Error Handling**: `AppError` with structured error codes and messages
- **Serialization Extensions**: JSON serialization utilities
- **Date/Time Utilities**: Multiplatform date and time helpers

**Key Features**:
- Zero dependencies on other modules
- Pure Kotlin multiplatform
- Comprehensive test coverage (613 tests)

### 2. Core (`modules/core`)
**Package**: `com.edugo.kmp.core.*`

Core services and infrastructure:

- **Result Extensions**: Advanced functional operations on Result monad
- **Utility Functions**: Common operations and helpers
- **Type Aliases**: Platform-agnostic type definitions

**Dependencies**:
- `foundation` (required)

**Key Features**:
- 72 tests covering all functionality
- Extension functions for Result operations
- Functional programming patterns

### 3. Logger (`modules/logger`)
**Package**: `com.edugo.kmp.logger.*`

Multiplatform logging infrastructure:

- **KmpLogger**: Platform-agnostic logging interface
- **LogLevel**: Configurable log levels (Verbose, Debug, Info, Warning, Error, Assert)
- **Platform Implementations**: Native logging for each platform (Logcat, Console, etc.)
- **Structured Logging**: Tag-based and categorized logging
- **Performance**: Optimized for production use

**Dependencies**:
- `core` → `foundation` (transitive)

**Key Features**:
- 179 comprehensive tests
- Platform-specific implementations
- Zero-cost abstractions

### 4. Validation (`modules/validation`)
**Package**: `com.edugo.kmp.validation.*`

Comprehensive validation framework:

- **Validation Helpers**: Email, UUID, range, length, pattern validators
- **Accumulative Validation**: Collect multiple errors in a single pass
- **Extension Functions**: Fluent validation API (`isValidEmail()`, `validateRange()`, etc.)
- **Result Integration**: Seamless integration with Result monad
- **Custom Validators**: Easy to extend with custom rules

**Dependencies**:
- `foundation` (required)

**Key Features**:
- 139 tests covering all validators
- Functional and imperative APIs
- Custom error messages
- Composable validators

## Supported Platforms

| Platform | Target | Min Version | Status |
|----------|--------|-------------|--------|
| Android | `androidTarget` | API 24 (Android 7.0) | ✅ Stable |
| Desktop | `jvm("desktop")` | JVM 17 | ✅ Stable |
| Web | `wasmJs` | - | ✅ Stable |
| iOS | `iosX64`, `iosArm64`, `iosSimulatorArm64` | - | ⏳ Optional (via `enableIos` flag) |

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.20 | Core language |
| Gradle | 8.14.3 | Build system |
| Android Gradle Plugin | 8.12.0 | Android builds |
| kotlinx.coroutines | 1.10.2 | Async/concurrency |
| kotlinx.serialization | 1.8.0 | JSON serialization |
| kotlinx.datetime | 0.6.1 | Date/time handling |
| Ktor | 3.1.3 | HTTP client (future) |
| Kover | 0.8.3 | Code coverage |
| Turbine | 1.2.0 | Flow testing |

## Quick Start

### Prerequisites

- JDK 17 or higher
- Gradle 8.11+ (included via wrapper)
- Android SDK with API 36 (for Android builds)

### Clone and Build

```bash
# Navigate to project directory
cd /Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common/kmp_new

# Build all modules
./gradlew build

# Build specific module
./gradlew :modules:foundation:build
./gradlew :modules:core:build
./gradlew :modules:logger:build
./gradlew :modules:validation:build
```

### Run Tests

```bash
# Run all tests (all platforms)
./gradlew test

# Run tests for specific platform
./gradlew desktopTest          # Desktop JVM tests
./gradlew androidUnitTest      # Android unit tests
./gradlew wasmJsTest           # WebAssembly tests

# Run tests for specific module
./gradlew :modules:foundation:test
./gradlew :modules:logger:desktopTest
./gradlew :modules:validation:androidUnitTest
```

### Assemble

```bash
# Compile all modules without running tests
./gradlew assemble

# Assemble specific module
./gradlew :modules:foundation:assemble
```

### Code Coverage

```bash
# Generate coverage report (Kover)
./gradlew koverHtmlReport

# View report
open build/reports/kover/html/index.html
```

## Project Structure

```
kmp_new/
├── build-logic/                          # Convention plugins
│   └── src/main/kotlin/
│       ├── kmp.android.gradle.kts        # Android + Desktop + wasmJs
│       └── kmp.logic.core.gradle.kts     # Desktop + wasmJs (no Android)
├── modules/
│   ├── foundation/                       # Foundation module
│   │   └── src/
│   │       ├── commonMain/kotlin/com/edugo/kmp/foundation/
│   │       └── commonTest/kotlin/com/edugo/kmp/foundation/
│   ├── core/                             # Core module
│   │   └── src/
│   │       ├── commonMain/kotlin/com/edugo/kmp/core/
│   │       └── commonTest/kotlin/com/edugo/kmp/core/
│   ├── logger/                           # Logger module
│   │   └── src/
│   │       ├── commonMain/kotlin/com/edugo/kmp/logger/
│   │       ├── commonTest/kotlin/com/edugo/kmp/logger/
│   │       ├── androidMain/kotlin/       # Android implementation
│   │       ├── desktopMain/kotlin/       # Desktop implementation
│   │       └── wasmJsMain/kotlin/        # WASM implementation
│   └── validation/                       # Validation module
│       └── src/
│           ├── commonMain/kotlin/com/edugo/kmp/validation/
│           └── commonTest/kotlin/com/edugo/kmp/validation/
├── gradle/
│   └── libs.versions.toml                # Version catalog
├── build.gradle.kts                      # Root build configuration
├── settings.gradle.kts                   # Module declarations
└── gradle.properties                     # Gradle settings
```

## Module Dependencies

```
foundation (no dependencies)
    ↓
    ├── core (depends on foundation)
    │   ↓
    │   └── logger (depends on core → foundation)
    │
    └── validation (depends on foundation)
```

## Convention Plugins

The project uses Gradle convention plugins for consistent configuration:

### `kmp.android`
For modules targeting Android + Desktop + wasmJs (e.g., logger)

```kotlin
plugins {
    id("kmp.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.edugo.kmp.yourmodule"
}
```

**Includes**:
- Android library configuration (compileSdk 36, minSdk 24)
- JVM target (Java 17)
- wasmJs target with browser support
- Optional iOS targets (via `enableIos` flag)
- Common dependencies (coroutines, serialization, datetime)

### `kmp.logic.core`
For modules without Android (e.g., foundation, core, validation)

```kotlin
plugins {
    id("kmp.logic.core")
}
```

**Includes**:
- Desktop JVM target (Java 17)
- wasmJs target with browser support
- Optional iOS targets (via `enableIos` flag)
- Common dependencies (coroutines, serialization, datetime)

## Version Catalog

All dependency versions are centralized in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.2.20"
kotlinx-coroutines = "1.10.2"
kotlinx-serialization = "1.8.0"
kover = "0.8.3"

[libraries]
kotlinx-coroutines-core = { module = "...", version.ref = "kotlinx-coroutines" }
kotlinx-serialization-json = { module = "...", version.ref = "kotlinx-serialization" }

[plugins]
kotlinMultiplatform = { id = "...", version.ref = "kotlin" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

## Usage Examples

### Foundation - Result Monad

```kotlin
import com.edugo.kmp.foundation.result.*

fun validateUser(email: String): Result<User> {
    return if (email.contains("@")) {
        success(User(email))
    } else {
        failure("Invalid email")
    }
}

// Usage
val result = validateUser("user@example.com")
when (result) {
    is Result.Success -> println("User: ${result.data}")
    is Result.Failure -> println("Error: ${result.error}")
    is Result.Loading -> println("Loading...")
}
```

### Logger - Multiplatform Logging

```kotlin
import com.edugo.kmp.logger.*

val logger = KmpLogger.getLogger("MyApp")

logger.debug("Debug message")
logger.info("Info message")
logger.warning("Warning message")
logger.error("Error occurred", throwable)
```

### Validation - Input Validation

```kotlin
import com.edugo.kmp.validation.*

// Boolean API
if ("user@example.com".isValidEmail()) {
    println("Valid email")
}

// Result API
val result = "user@example.com".validateEmail()
when (result) {
    is Result.Success -> println("Valid: ${result.data}")
    is Result.Failure -> println("Error: ${result.error}")
}

// Accumulative validation
val errors = accumulateValidationErrors {
    add(validateEmail("user@example.com"))
    add(validateRange(25, 18, 120, "Age"))
    add(validateLengthRange("username", 3, 30, "Username"))
}
```

## Sprint 1 Completion Status

**Status**: ✅ **100% COMPLETE**

| Task | Description | Status | Tests |
|------|-------------|--------|-------|
| 1.1 | Foundation module | ✅ Complete | 613 passing |
| 1.2 | Core module | ✅ Complete | 72 passing |
| 1.3 | Logger module | ✅ Complete | 179 passing |
| 1.4 | Validation module | ✅ Complete | 139 passing |
| 1.5 | Convention plugins | ✅ Complete | - |
| 1.6 | Documentation | ✅ Complete | - |

**Total Tests**: 1,003 tests passing, 0 failures

**Build Status**: All modules build successfully on all platforms

## Documentation

Comprehensive documentation is available in the `documentacion/` directory:

- **[Sprint 1 Guide](documentacion/sprint-1/README.md)** - Complete Sprint 1 documentation
- **[Sprint 1 Details](documentacion/sprint-1/SPRINT-1-DETALLE.md)** - Technical specifications
- **[Executive Summary](documentacion/sprint-1/RESUMEN-EJECUTIVO.md)** - Quick overview
- **[Troubleshooting](documentacion/sprint-1/TROUBLESHOOTING.md)** - Common issues and solutions
- **Task-specific guides** - Step-by-step implementation guides

## Development

### Adding a New Module

1. Create module directory:
```bash
mkdir -p modules/my-module/src/commonMain/kotlin/com/edugo/kmp/mymodule
mkdir -p modules/my-module/src/commonTest/kotlin/com/edugo/kmp/mymodule
```

2. Create `modules/my-module/build.gradle.kts`:
```kotlin
plugins {
    id("kmp.logic.core")  // or kmp.android
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

3. Add to `settings.gradle.kts`:
```kotlin
include(":modules:my-module")
```

4. Sync and build:
```bash
./gradlew :modules:my-module:build
```

### Code Style

- Kotlin 2.2.20 with explicit types where appropriate
- Functional programming patterns preferred
- Immutable data structures by default
- Comprehensive KDoc comments for public APIs
- 100% test coverage target

### Testing

- Use `kotlin("test")` for multiplatform tests
- Use `kotlinx-coroutines-test` with `runTest {}` for async code
- Use `turbine` for Flow testing
- Platform-specific tests go in `{platform}Test/` source sets

## Contributing

This is an internal EduGo project. For contribution guidelines, see the team documentation.

## License

Proprietary - EduGo Platform © 2025

## Contact

For questions or support, contact the EduGo Platform team.

---

**Project Location**: `/Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common/kmp_new`

**Last Updated**: Sprint 1 Completion - February 2026
