# Arquitectura: Multi-Environment Config Management

## Visión General

Sistema de configuración multi-ambiente que detecta automáticamente el entorno de ejecución (DEV/STAGING/PROD) basado en heurísticas específicas de cada plataforma, elimina URLs hardcodeadas y centraliza la configuración en archivos JSON externos.

---

## Diagrama de Capas

```
┌────────────────────────────────────────────────────────────┐
│ CAPA 5: Applications (Android, iOS, Desktop, Web)         │
│  - MainActivity.kt (Android)                               │
│  - App.kt (Compose Multiplatform)                         │
└─────────────────────┬──────────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────────┐
│ CAPA 4: Dependency Injection                               │
│  - KoinInitializer                                         │
│  - ConfigModule ← EnvironmentDetector.detect()             │
└─────────────────────┬──────────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────────┐
│ CAPA 3: Domain (Auth, Business Logic)                     │
│  - AuthModule ← Recibe AppConfig                           │
│  - AuthRepositoryImpl(baseUrl = config.getFullApiUrl())   │
└─────────────────────┬──────────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────────┐
│ CAPA 2: Infrastructure (Config, Network, Storage)         │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ EnvironmentDetector (expect/actual)                  │ │
│  │  - detect(): Environment                             │ │
│  │  - override(env): Unit                               │ │
│  │  - reset(): Unit                                     │ │
│  └──────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ ConfigLoader                                         │ │
│  │  - load(environment): AppConfig                      │ │
│  └──────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ ResourceLoader (expect/actual)                       │ │
│  │  - loadResourceAsString(path): String?               │ │
│  └──────────────────────────────────────────────────────┘ │
└─────────────────────┬──────────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────────┐
│ CAPA 1: Core + Foundation                                 │
│  - Platform (isDebug, name, osVersion)                    │
│  - Result<T>, ErrorCode                                   │
│  - JSON Serialization                                     │
└────────────────────────────────────────────────────────────┘
```

---

## Diagrama de Flujo de Datos

### Flujo 1: Inicialización de Configuración

```
App Startup (onCreate/main)
    │
    ├─► [Android] AndroidContextHolder.init(applicationContext)
    │
    ▼
KoinApplication.modules(allModules())
    │
    ▼
ConfigModule ejecutado
    │
    ├─► single<Environment> { EnvironmentDetector.detect() }
    │       │
    │       ├─► detectPlatformEnvironment() ← expect/actual
    │       │       ├─ Android: Platform.isDebug?
    │       │       ├─ Desktop: Debugger attached?
    │       │       ├─ iOS: Platform.isDebug? (conservador)
    │       │       └─ WasmJS: Default DEV
    │       │
    │       └─► Retorna Environment.DEV | STAGING | PROD
    │
    ├─► single<AppConfig> { ConfigLoader.load(get<Environment>()) }
    │       │
    │       ├─► path = "config/${environment.fileName}.json"
    │       │
    │       ├─► ResourceLoader.loadResourceAsString(path)
    │       │       ├─ Android: Context.assets.open(path)
    │       │       ├─ Desktop: ClassLoader.getResourceAsStream(path)
    │       │       ├─ iOS: NSBundle.mainBundle.path()
    │       │       └─ Fallback: Hardcoded JSON
    │       │
    │       ├─► JSON.decodeFromString<AppConfigImpl>(jsonContent)
    │       │
    │       └─► Retorna AppConfig
    │
    └─► Config inyectado en toda la app
```

### Flujo 2: Uso en AuthModule

```
AuthModule ejecutado
    │
    ├─► single { AuthConfig.forEnvironment(get<AppConfig>().environment) }
    │       └─► Retorna AuthConfig con timeouts/retries según ambiente
    │
    ├─► single<AuthRepository> {
    │       AuthRepositoryImpl(
    │           baseUrl = get<AppConfig>().getFullApiUrl()  ← "http://localhost:8080"
    │       )
    │   }
    │
    └─► AuthService.login(credentials)
            │
            ├─► AuthRepository.login()
            │       │
            │       └─► POST {baseUrl}/v1/auth/login
            │               └─► Usa URL correcta según ambiente!
            │
            └─► Token almacenado, AuthInterceptor configurado
```

### Flujo 3: Override Manual (Testing)

```
Test Setup
    │
    ├─► EnvironmentDetector.override(Environment.STAGING)
    │       └─► manualOverride = STAGING
    │
    ├─► EnvironmentDetector.detect()
    │       └─► Retorna STAGING (ignora detección automática)
    │
    ├─► ConfigLoader.load(EnvironmentDetector.detect())
    │       └─► Carga config/staging.json
    │
    ├─► Test ejecutado con configuración STAGING
    │
    └─► EnvironmentDetector.reset()
            └─► manualOverride = null (restaura auto-detección)
```

---

## Diagrama de Módulos

```
modules/config/
├── src/commonMain/
│   ├── kotlin/com/edugo/kmp/config/
│   │   ├── Environment.kt               # Enum: DEV, STAGING, PROD
│   │   │   └─ fromString(name): Environment?
│   │   │   └─ fromStringOrDefault(name, default): Environment
│   │   │
│   │   ├── EnvironmentDetector.kt       # expect/actual
│   │   │   └─ detect(): Environment     # API pública
│   │   │   └─ override(env): Unit
│   │   │   └─ reset(): Unit
│   │   │   └─ detectPlatformEnvironment(): Environment [expect]
│   │   │
│   │   ├── AppConfig.kt                 # Interface + Impl
│   │   │   └─ getFullApiUrl(): String   # "http://localhost:8080"
│   │   │
│   │   ├── ConfigLoader.kt              # Cargador
│   │   │   └─ load(environment): AppConfig
│   │   │
│   │   └── ResourceLoader.kt            # expect/actual
│   │       └─ loadResourceAsString(path): String? [expect]
│   │
│   └── resources/config/
│       ├── dev.json                     # Configuración DEV
│       ├── staging.json                 # Configuración STAGING
│       └── prod.json                    # Configuración PROD
│
├── src/androidMain/kotlin/.../config/
│   ├── EnvironmentDetector.android.kt   # Android detection
│   ├── AndroidContextHolder.kt          # Context holder
│   └── ResourceLoader.android.kt        # Lee desde assets
│
├── src/desktopMain/kotlin/.../config/
│   ├── EnvironmentDetector.desktop.kt   # Desktop detection
│   └── ResourceLoader.desktop.kt        # Lee desde classpath
│
├── src/iosMain/kotlin/.../config/
│   ├── EnvironmentDetector.ios.kt       # iOS detection
│   └── ResourceLoader.ios.kt            # Lee desde Bundle
│
├── src/wasmJsMain/kotlin/.../config/
│   ├── EnvironmentDetector.wasmJs.kt    # WasmJS detection
│   └── ResourceLoader.wasmJs.kt         # Hardcoded (Fase 1)
│
└── src/commonTest/kotlin/.../config/
    ├── EnvironmentDetectorTest.kt       # Tests de detección
    ├── EnvironmentTest.kt               # Tests de fromString()
    └── ConfigLoaderTest.kt              # Tests de integración
```

---

## Responsabilidades de Componentes

### EnvironmentDetector

**Responsabilidad**: Detectar el ambiente de ejecución actual

**Funciones**:
- `detect()`: Retorna ambiente detectado o overrideado
- `override(env)`: Fuerza un ambiente específico
- `reset()`: Limpia override, restaura auto-detección
- `detectPlatformEnvironment()`: Implementación platform-specific

**Dependencias**:
- `Platform.isDebug` (de modules/core)
- System properties (Android, Desktop)
- Variables de entorno (Desktop)

**No debe**:
- Cargar configuración (responsabilidad de ConfigLoader)
- Leer archivos JSON (responsabilidad de ResourceLoader)
- Mantener estado más allá del override manual

---

### ConfigLoader

**Responsabilidad**: Cargar configuración desde archivos JSON

**Funciones**:
- `load(environment)`: Carga y parsea config del ambiente
- `loadFromString(json)`: Carga desde string (testing)

**Dependencias**:
- `ResourceLoader` para leer archivos
- JSON serialization (Kotlinx Serialization)
- `Environment` para determinar archivo

**No debe**:
- Detectar ambiente (responsabilidad de EnvironmentDetector)
- Mantener estado (siempre retorna nueva instancia)
- Validar valores de configuración (se confía en JSON)

---

### ResourceLoader (expect/actual)

**Responsabilidad**: Cargar archivos de recursos según plataforma

**Funciones**:
- `loadResourceAsString(path)`: Lee archivo y retorna String

**Implementaciones**:
- **Android**: `Context.assets.open(path)` → Fallback hardcoded
- **Desktop**: `ClassLoader.getResourceAsStream(path)` → Fallback hardcoded
- **iOS**: `NSBundle.mainBundle.path()` → Fallback hardcoded
- **WasmJS**: Hardcoded (Fase 1), Fetch API (Fase 2)

**Dependencias**:
- Android: `AndroidContextHolder.get()` para Context
- Desktop: Java ClassLoader
- iOS: Foundation NSBundle
- WasmJS: Ninguna (Fase 1)

**No debe**:
- Parsear JSON (responsabilidad de ConfigLoader)
- Validar contenido de archivos
- Lanzar excepciones (retorna null si falla)

---

### AndroidContextHolder

**Responsabilidad**: Proporcionar Context de Android para acceso a assets

**Funciones**:
- `init(context)`: Inicializa con application context
- `get()`: Retorna context almacenado o null

**Dependencias**:
- Android Context

**No debe**:
- Ser usado fuera del módulo config
- Almacenar Activity context (solo ApplicationContext)
- Ser inicializado múltiples veces (idempotente)

---

### AppConfig / AppConfigImpl

**Responsabilidad**: Representar configuración de la aplicación

**Propiedades**:
- `environment`: Environment
- `apiUrl`, `apiPort`: URL del backend
- `timeout`: Timeout HTTP
- `debugMode`: Flag de debug

**Funciones**:
- `getFullApiUrl()`: Construye URL completa

**Dependencias**:
- Kotlinx Serialization para parsing
- `Environment.fromStringOrDefault()` para parsing seguro

**No debe**:
- Cargar archivos (responsabilidad de ConfigLoader)
- Validar URLs (se confía en JSON)
- Contener lógica de negocio

---

## Inyección de Dependencias

```kotlin
// modules/di/src/commonMain/kotlin/.../ConfigModule.kt

public val configModule = module {
    // Auto-detección de ambiente
    single<Environment> { 
        EnvironmentDetector.detect()  // ← Inyectado automáticamente
    }
    
    // Configuración basada en ambiente
    single<AppConfig> { 
        ConfigLoader.load(get())  // ← Recibe Environment del DI
    }
}

// modules/di/src/commonMain/kotlin/.../AuthModule.kt

public val authModule = module {
    // AuthConfig adaptado al ambiente
    single {
        AuthConfig.forEnvironment(
            get<AppConfig>().environment  // ← Recibe AppConfig del DI
        )
    }
    
    // AuthRepository con URL correcta
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get(),
            baseUrl = get<AppConfig>().getFullApiUrl(),  // ← URL automática!
            circuitBreaker = get(),
            retryPolicy = get<AuthConfig>().retryPolicy
        )
    }
}
```

### Orden de Inicialización

1. **KoinApplication** se inicia
2. **ConfigModule** se ejecuta:
   - `EnvironmentDetector.detect()` → Retorna DEV/STAGING/PROD
   - `ConfigLoader.load(environment)` → Carga config JSON
3. **AuthModule** se ejecuta:
   - Recibe `AppConfig` del DI
   - Extrae `baseUrl` de AppConfig
   - Crea `AuthRepositoryImpl` con URL correcta
4. **App** arranca con configuración correcta

---

## Flujos de Detección Completos

### Flujo Android

```
App.onCreate()
    │
    ├─► AndroidContextHolder.init(applicationContext)
    │
    ├─► KoinApplication { modules(...) }
    │       │
    │       └─► ConfigModule
    │               │
    │               └─► EnvironmentDetector.detect()
    │                       │
    │                       └─► detectPlatformEnvironment() [Android]
    │                               │
    │                               ├─► Platform.isDebug?
    │                               │   ├─ true → Environment.DEV
    │                               │   └─ false ↓
    │                               │
    │                               ├─► System.getProperty("app.environment")?
    │                               │   ├─ "STAGING" → Environment.STAGING
    │                               │   ├─ "PROD" → Environment.PROD
    │                               │   └─ null ↓
    │                               │
    │                               └─► Environment.PROD (default release)
    │
    └─► App ejecutándose con ambiente correcto
```

### Flujo Desktop

```
main()
    │
    ├─► KoinApplication { modules(...) }
    │       │
    │       └─► ConfigModule
    │               │
    │               └─► EnvironmentDetector.detect()
    │                       │
    │                       └─► detectPlatformEnvironment() [Desktop]
    │                               │
    │                               ├─► Platform.isDebug? (debugger attached?)
    │                               │   ├─ true → Environment.DEV
    │                               │   └─ false ↓
    │                               │
    │                               ├─► System.getenv("APP_ENVIRONMENT")?
    │                               │   ├─ "STAGING" → Environment.STAGING
    │                               │   ├─ "PROD" → Environment.PROD
    │                               │   └─ null ↓
    │                               │
    │                               └─► Environment.PROD (default)
    │
    └─► App ejecutándose con ambiente correcto
```

### Flujo iOS

```
App.main()
    │
    ├─► KoinApplication { modules(...) }
    │       │
    │       └─► ConfigModule
    │               │
    │               └─► EnvironmentDetector.detect()
    │                       │
    │                       └─► detectPlatformEnvironment() [iOS]
    │                               │
    │                               ├─► Platform.isDebug?
    │                               │   ├─ true → Environment.DEV
    │                               │   └─ false ↓
    │                               │
    │                               └─► Environment.DEV (conservador)
    │                                   └─ Evita llamadas PROD accidentales
    │
    └─► App ejecutándose en DEV (Fase 2 agregará Info.plist support)
```

### Flujo WasmJS

```
App start
    │
    ├─► KoinApplication { modules(...) }
    │       │
    │       └─► ConfigModule
    │               │
    │               └─► EnvironmentDetector.detect()
    │                       │
    │                       └─► detectPlatformEnvironment() [WasmJS]
    │                               │
    │                               └─► Environment.DEV (Fase 1)
    │                                   └─ Fase 2 agregará hostname detection
    │
    └─► App ejecutándose en DEV
```

---

## Configuración por Ambiente

### Development (dev.json)

```json
{
  "environmentName": "DEV",
  "apiUrl": "http://localhost",
  "apiPort": 8080,
  "webPort": 8080,
  "timeout": 30000,
  "debugMode": true
}
```

**Características**:
- URL local (`localhost:8080`)
- Timeout corto (30s) para debugging rápido
- Debug mode activado
- Ideal para desarrollo local

---

### Staging (staging.json)

```json
{
  "environmentName": "STAGING",
  "apiUrl": "https://api-staging.example.com",
  "apiPort": 443,
  "webPort": 8080,
  "timeout": 60000,
  "debugMode": true
}
```

**Características**:
- URL de staging con HTTPS
- Timeout medio (60s)
- Debug mode activado (para troubleshooting)
- Ideal para QA y testing pre-producción

---

### Production (prod.json)

```json
{
  "environmentName": "PROD",
  "apiUrl": "https://api.example.com",
  "apiPort": 443,
  "webPort": 80,
  "timeout": 60000,
  "debugMode": false
}
```

**Características**:
- URL de producción con HTTPS
- Timeout medio (60s)
- Debug mode **desactivado**
- Logging mínimo
- Ideal para usuarios finales

---

## Patrones Utilizados

1. **Expect/Actual Pattern**
   - Permite código común con implementaciones platform-specific
   - Usado en `EnvironmentDetector`, `ResourceLoader`

2. **Singleton Pattern**
   - `EnvironmentDetector` como object
   - `AndroidContextHolder` como object

3. **Factory Pattern**
   - `ConfigLoader.load()` retorna AppConfig
   - `Environment.fromString()` retorna Environment

4. **Dependency Injection**
   - Koin modules para proporcionar Environment y AppConfig
   - Lazy initialization de configuración

5. **Fallback Pattern**
   - ResourceLoader intenta leer archivo, fallback a hardcoded
   - `fromStringOrDefault()` usa default si parsing falla

6. **Strategy Pattern**
   - Diferentes estrategias de detección por plataforma
   - Android: isDebug + System properties
   - Desktop: Debugger + Env vars
   - iOS: Conservador
   - WasmJS: Default (Fase 1)

---

## Resumen

La arquitectura implementada es:

✅ **Modular**: Cada componente tiene una responsabilidad clara  
✅ **Extensible**: Fácil agregar nuevas plataformas o estrategias  
✅ **Testeable**: Override manual permite testing completo  
✅ **Segura**: Defaults conservadores, fallbacks robustos  
✅ **Performante**: Singleton, lazy loading, archivos pequeños  
✅ **Mantenible**: Código simple, bien documentado  

**Próxima evolución**: Fase 2 agregará build-time configuration para mayor precisión en detección de ambiente.
