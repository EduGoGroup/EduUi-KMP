# Sprint 5 - Análisis de Módulos Actuales

## Estado Actual del Sistema

### Módulos Existentes (kmp_new/)

#### 1. **foundation** (Base de todo)
- **Propósito**: Tipos fundamentales y utilidades compartidas
- **Componentes clave**:
  - `Result<T>` - Manejo de errores type-safe
  - `AppError` - Errores estructurados con códigos
  - Extensiones de serialización JSON
  - Dispatchers multiplataforma
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 613 tests ✓

#### 2. **core** (Lógica central)
- **Propósito**: Abstracciones core y platform-specific
- **Componentes clave**:
  - `Platform` - Info de plataforma (expect/actual)
  - Anotaciones compartidas
  - Utilitarios de sincronización
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 72 tests ✓

#### 3. **logger** (Logging)
- **Propósito**: Sistema de logging multiplataforma (Kermit)
- **Componentes clave**:
  - `EduGoLogger` - Logger estructurado
  - Configuración por severidad
  - Writers platform-specific
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 179 tests ✓

#### 4. **validation** (Validación de datos)
- **Propósito**: Validadores reutilizables
- **Componentes clave**:
  - Email, UUID, Password validators
  - Validación acumulativa
  - API booleana y Result-based
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 168 tests ✓

#### 5. **network** (Networking)
- **Propósito**: Cliente HTTP multiplataforma (Ktor)
- **Componentes clave**:
  - `EduGoHttpClient` - Wrapper de Ktor
  - `TokenProvider` interface (para auth)
  - Interceptores (Auth, Headers, Logging)
  - Retry logic con backoff exponencial
  - Platform engines (OkHttp/Android, CIO/Desktop, Ktor-JS/Wasm, Darwin/iOS)
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 185 tests ✓

#### 6. **storage** (Almacenamiento K-V)
- **Propósito**: Key-value storage multiplataforma
- **Tecnología**: multiplatform-settings (Shared Preferences/Android, UserDefaults/iOS, etc.)
- **Componentes clave**:
  - `EduGoStorage` - API síncrona básica
  - `AsyncEduGoStorage` - API async con coroutines
  - `StateFlowStorage` - Storage reactivo con StateFlow
  - Serialización automática JSON
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 231 tests ✓

#### 7. **config** (Configuración de entornos)
- **Propósito**: Gestión de configuración por entorno (DEV/STAGING/PROD)
- **Componentes clave**:
  - `EduGoConfig` - Config data class
  - `ConfigLoader` - Carga desde JSON (expect/actual)
  - `AppEnvironment` enum
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 21 tests ✓

#### 8. **auth** (Autenticación y autorización)
- **Propósito**: Sistema completo de autenticación JWT
- **Componentes clave**:
  - `AuthService` - Login/Logout/Refresh
  - `TokenRefreshManager` - Refresh automático con Mutex
  - `JwtTokenParser` - Parsing y validación JWT
  - `AuthorizationService` - Roles y permisos genéricos
  - `TokenProvider` implementation (usado por network)
- **Dependencias**: foundation, logger, core, validation, network, storage
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 171 tests ✓

#### 9. **di** (Inyección de dependencias)
- **Propósito**: Configuración de Koin 4.1.0
- **Módulos Koin**:
  - `foundationModule` - Dispatchers, serializers
  - `loggerModule` - EduGoLogger singleton
  - `storageModule` - Storage instances (sync/async/reactive)
  - `networkModule` - HttpClient con TokenProvider
  - `configModule` - EduGoConfig
  - `authModule` - AuthService, TokenRefreshManager, AuthorizationService
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 20 tests ✓

#### 10. **kmp-design** (Design System)
- **Propósito**: Sistema de diseño Compose Multiplatform
- **Componentes clave**:
  - Tokens (colors, spacing, etc.)
  - `EduGoTheme` - Tema principal
  - `SemanticColors` - Colores semánticos
  - `DSAlertDialog`, `DSSnackbar` - Componentes
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 13 tests ✓

#### 11. **kmp-resources** (Recursos multiplatforma)
- **Propósito**: Strings localizados expect/actual
- **Implementación**:
  - Android: R.string resources
  - Desktop/Wasm/iOS: Hardcoded strings
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 0 (pure data)

#### 12. **kmp-screens** (UI y navegación)
- **Propósito**: Pantallas y navegación de la app
- **Componentes clave**:
  - `NavigationState` - Estado de navegación con backstack
  - `Routes` - Rutas sealed class
  - Pantallas: Splash, Login, Home, Settings
  - `App.kt` - Composable principal
- **Targets**: Android, Desktop, WasmJs, iOS (on-demand)
- **Tests**: 21 tests ✓

---

## Flujo Actual de Login (Sprint 4)

### 1. UI Layer (`kmp-screens/LoginScreen.kt`)
```
Usuario ingresa credenciales
    ↓
LoginScreen muestra UI
    ↓
[TODO] Llamar AuthService (aún no conectado)
```

### 2. Auth Layer (`auth/AuthService.kt`)
```
AuthService.login(email, password)
    ↓
NetworkClient POST /auth/login
    ↓
Recibe AuthResponse(accessToken, refreshToken, user)
    ↓
Guarda tokens en storage:
    - KEY_ACCESS_TOKEN
    - KEY_REFRESH_TOKEN
    ↓
Retorna Result<AuthResponse>
```

### 3. Storage Layer (`storage/EduGoStorage.kt`)
```
storage.putString(key, value)
    ↓
Platform-specific:
    - Android: SharedPreferences
    - Desktop: Preferences (java.util.prefs)
    - Wasm: localStorage
    - iOS: NSUserDefaults
```

### 4. Network Layer (`network/EduGoHttpClient.kt`)
```
TokenProvider provee tokens
    ↓
AuthInterceptor añade "Authorization: Bearer {token}"
    ↓
Si 401: TokenRefreshManager refresca automáticamente
    ↓
Retry request con nuevo token
```

---

## Gaps Identificados para Persistencia de Sesión

### ✅ Lo que ya tenemos:
1. **Storage multiplataforma** - `storage` module con 3 APIs (sync/async/reactive)
2. **Auth service** - Login/logout con guardado de tokens
3. **Token refresh automático** - `TokenRefreshManager` con Mutex
4. **DI configurado** - Koin con todos los módulos
5. **UI estructurada** - Navegación y pantallas base

### ❌ Lo que falta implementar:

#### 1. **Session State Management** (CRÍTICO)
- **Problema**: No hay un componente que maneje el estado de sesión global
- **Necesidad**: Un `SessionManager` que:
  - Detecte si hay sesión activa al inicio
  - Emita estado reactivo (Flow/StateFlow)
  - Maneje transiciones de estado (LoggedOut → LoggedIn → Expired)
  - Integre con AuthService

#### 2. **Session Persistence Logic** (CRÍTICO)
- **Problema**: AuthService guarda tokens pero no hay lógica de restauración
- **Necesidad**:
  - Método `AuthService.restoreSession()` que valide tokens guardados
  - Verificación de expiración de tokens
  - Auto-refresh si token válido pero cerca de expirar
  - Limpieza de sesión expirada

#### 3. **App Initialization Flow** (CRÍTICO)
- **Problema**: SplashScreen no verifica sesión, solo navega a Login
- **Necesidad**:
  - Lógica en SplashScreen para verificar sesión
  - Navegación condicional (Home si sesión activa, Login si no)
  - Loading state durante verificación

#### 4. **Logout Completo** (IMPORTANTE)
- **Problema**: `AuthService.logout()` limpia storage pero no hay integración UI
- **Necesidad**:
  - Conectar logout de SettingsScreen con AuthService
  - Navegación a Login tras logout
  - Limpieza de estado en navegación

#### 5. **Session Expiration Handling** (IMPORTANTE)
- **Problema**: Si refresh token expira, no hay manejo UI
- **Necesidad**:
  - Listener global para eventos 401 sin refresh posible
  - Navegación automática a Login
  - Mensaje al usuario ("Sesión expirada")

#### 6. **User State in UI** (IMPORTANTE)
- **Problema**: No hay estado de usuario en UI layer
- **Necesidad**:
  - ViewModel/StateHolder con user info
  - Integración con SessionManager
  - Display de info de usuario en Settings/Home

#### 7. **Secure Storage (Opcional pero recomendado)**
- **Problema**: Tokens en plain text en storage
- **Necesidad**:
  - Wrapper de storage con encriptación platform-specific
  - Android: EncryptedSharedPreferences
  - iOS: Keychain
  - Desktop: OS keyring
  - Wasm: Web Crypto API

#### 8. **Biometric Auth (Futuro)**
- **Problema**: No hay soporte para biométricos
- **Necesidad**: (Dejar para Sprint futuro)
  - Wrapper multiplataforma para biométricos
  - Integración con session restoration

---

## Arquitectura Propuesta para Session Management

### Capas y Responsabilidades

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  (kmp-screens)                                              │
│  - SplashScreen: Verifica sesión y navega                   │
│  - LoginScreen: Llama AuthService.login                     │
│  - SettingsScreen: Llama SessionManager.logout              │
│  - HomeScreen: Muestra info de usuario                      │
└────────────────────┬────────────────────────────────────────┘
                     │ observes SessionState (StateFlow)
                     │ calls SessionManager methods
┌────────────────────▼────────────────────────────────────────┐
│                  Presentation Layer                          │
│  (NUEVO: kmp-session - UI State Management)                 │
│  - SessionManager: Estado global de sesión                   │
│    · sessionState: StateFlow<SessionState>                   │
│    · checkSession(): Result<User?>                           │
│    · login(email, pass): Result<User>                        │
│    · logout(): Result<Unit>                                  │
│  - SessionState: sealed class                                │
│    · Unknown (inicial)                                       │
│    · Loading (verificando)                                   │
│    · LoggedIn(user)                                          │
│    · LoggedOut                                               │
│    · Expired                                                 │
└────────────────────┬────────────────────────────────────────┘
                     │ uses
┌────────────────────▼────────────────────────────────────────┐
│                   Domain Layer                               │
│  (auth module - Business Logic)                             │
│  - AuthService: Login/Logout/Refresh                         │
│    · login(email, pass): Result<AuthResponse>                │
│    · logout(): Result<Unit>                                  │
│    · refreshToken(): Result<String>                          │
│    · restoreSession(): Result<User?> (NUEVO)                 │
│  - TokenRefreshManager: Auto-refresh                         │
│  - JwtTokenParser: Token parsing y validación                │
│  - AuthorizationService: Roles y permisos                    │
└────────────────────┬────────────────────────────────────────┘
                     │ uses
┌────────────────────▼────────────────────────────────────────┐
│                   Data Layer                                 │
│  (storage + network modules)                                │
│  - EduGoStorage: Key-value persistence                       │
│    · Guarda: access_token, refresh_token, user_data          │
│  - EduGoHttpClient: HTTP requests                            │
│    · TokenProvider: Provee tokens                            │
│    · AuthInterceptor: Añade Authorization header            │
│  - SecureStorage (NUEVO - Opcional): Encrypted storage       │
└──────────────────────────────────────────────────────────────┘
```

---

## Nuevos Módulos a Crear

### 1. **kmp-session** (Session Management)
- **Propósito**: Gestión de estado de sesión global (UI-aware)
- **Responsabilidades**:
  - Proveer `SessionManager` (singleton via DI)
  - Exponer `sessionState: StateFlow<SessionState>`
  - Coordinar entre AuthService y UI
  - Manejar eventos de sesión expirada
- **Dependencias**: auth, di, core, foundation
- **Tests**: ~50-70 tests (state transitions, edge cases)

### 2. **kmp-security** (Secure Storage - Opcional)
- **Propósito**: Almacenamiento encriptado multiplataforma
- **Responsabilidades**:
  - `SecureStorage` interface con encrypt/decrypt
  - Platform-specific implementations:
    - Android: EncryptedSharedPreferences (Jetpack Security)
    - iOS: Keychain Services
    - Desktop: OS keyring (java-keyring library)
    - Wasm: Web Crypto API (SubtleCrypto)
  - Wrapper para tokens sensibles
- **Dependencias**: storage, core, foundation
- **Tests**: ~40-60 tests (encryption/decryption, fallbacks)

---

## Modificaciones a Módulos Existentes

### 1. **auth** (AuthService)
**Añadir**:
- `suspend fun restoreSession(): Result<User?>` - Valida tokens guardados
- `suspend fun getCurrentUser(): Result<User?>` - Obtiene usuario actual
- Método privado `validateAccessToken(token: String): Boolean`
- Constantes para keys de storage (`KEY_USER_DATA`)

### 2. **storage** (EduGoStorage)
**Añadir** (si no existe):
- `fun <T> getObject(key: String, defaultValue: T, serializer: KSerializer<T>): T`
- `fun <T> putObject(key: String, value: T?, serializer: KSerializer<T>)`
- Métodos para limpiar grupos de keys (`clearAuthData()`)

### 3. **kmp-screens** (UI)
**Modificar**:
- **SplashScreen**: Integrar `SessionManager.checkSession()`
- **LoginScreen**: Integrar `SessionManager.login()`
- **SettingsScreen**: Integrar `SessionManager.logout()`
- **HomeScreen**: Observar `sessionState` y mostrar user info

### 4. **di** (Dependency Injection)
**Añadir**:
- `sessionModule` - Provee `SessionManager` singleton
- `securityModule` (si se implementa kmp-security) - Provee `SecureStorage`

---

## Ventajas de esta Arquitectura

### ✅ Separación de Responsabilidades
- **AuthService** (auth): Lógica de negocio pura (login/logout/refresh)
- **SessionManager** (kmp-session): Estado UI y coordinación
- **Storage**: Persistencia sin lógica de negocio
- **Network**: Comunicación HTTP sin awareness de sesión

### ✅ Testabilidad
- Cada capa se puede testear independientemente
- Mocks fáciles (interfaces bien definidas)
- Tests unitarios para SessionManager sin UI
- Tests de integración para flujo completo

### ✅ Reusabilidad
- `AuthService` puede usarse en otros contextos (CLI, backend, etc.)
- `SessionManager` puede reemplazarse por otra implementación
- `SecureStorage` es módulo independiente (puede usarse para otros datos)

### ✅ Escalabilidad
- Fácil añadir features (biométricos, SSO, etc.)
- Cambiar storage backend sin tocar lógica
- A/B testing de flujos de login

### ✅ Mantenibilidad
- Código organizado por dominio
- Dependencias claras y explícitas
- Fácil de entender para nuevos devs

---

## Consideraciones de Implementación

### Platform-Specific Challenges

#### Android
- **Storage**: SharedPreferences es síncrono (OK)
- **Secure Storage**: EncryptedSharedPreferences (Jetpack Security Crypto)
- **Lifecycle**: Verificar sesión en `Application.onCreate()` o primer Activity

#### iOS
- **Storage**: NSUserDefaults es síncrono (OK)
- **Secure Storage**: Keychain Services (requiere actual implementation)
- **Lifecycle**: Verificar sesión en `MainViewController.viewDidLoad()` o AppDelegate

#### Desktop (JVM)
- **Storage**: java.util.prefs.Preferences
- **Secure Storage**: java-keyring o fallback a obfuscación
- **Lifecycle**: Verificar en main() antes de window.show()

#### WasmJs (Browser)
- **Storage**: localStorage (síncrono)
- **Secure Storage**: Web Crypto API (SubtleCrypto) - asíncrono
- **Lifecycle**: Verificar en composable App() init
- **Limitación**: localStorage es plain text (secure storage crítico aquí)

---

## Riesgos y Mitigaciones

### Riesgo 1: Token Refresh Race Conditions
**Mitigación**: TokenRefreshManager ya usa Mutex ✓

### Riesgo 2: Storage Corruption
**Mitigación**:
- Try-catch en restore operations
- Fallback a logout si datos corruptos
- Logging de errores

### Riesgo 3: Session Hijacking (Wasm)
**Mitigación**:
- Implementar SecureStorage con Web Crypto API
- Rotación frecuente de refresh tokens
- HTTPS only

### Riesgo 4: Complejidad de Testing en Wasm
**Mitigación**:
- Abstraer dependencies (fácil de mockear)
- Tests en JVM/Android primero
- Wasm tests focalizados en platform-specific code

---

## Próximos Pasos (Ver `02-plan-implementacion.md`)

1. Crear módulo `kmp-session` con `SessionManager`
2. Añadir `AuthService.restoreSession()`
3. Integrar en SplashScreen
4. Añadir tests de integración
5. (Opcional) Implementar `kmp-security` para secure storage
6. QA en todas las plataformas

---

**Total Tests Estimados Sprint 5**: ~100-150 nuevos tests
**Tests Actuales**: 1694 tests ✓
**Tests Post-Sprint 5**: ~1800-1850 tests
