# Sprint 5 - Plan de Implementación: Persistencia de Sesión

## Objetivo del Sprint

Implementar un sistema completo de persistencia de sesión multiplataforma que permita:
- Detectar automáticamente sesiones activas al abrir la app
- Restaurar sesión sin requerir login nuevamente
- Manejar expiración de sesión de manera elegante
- Mantener arquitectura limpia y testeable

---

## Fases de Implementación

### **FASE 1: Session State Management Core** ⭐ CRÍTICO
**Duración estimada**: 2-3 días
**Prioridad**: ALTA

#### Tareas:

##### 1.1. Crear módulo `kmp-session`
**Archivo**: `kmp-session/build.gradle.kts`

**Descripción**: Crear nuevo módulo Gradle para gestión de sesión

**Configuración**:
- Plugin: `kmp.logic.core` (Desktop + WasmJs + iOS on-demand)
- Dependencias:
  - `implementation(project(":modules:foundation"))`
  - `implementation(project(":modules:core"))`
  - `implementation(project(":modules:logger"))`
  - `implementation(project(":modules:auth"))`
  - `implementation(libs.kotlinx.coroutines.core)`
  - `implementation(libs.koin.core)`

**Criterios de aceptación**:
- ✅ Módulo compila en todas las plataformas (Desktop, WasmJs, iOS)
- ✅ Gradle sync exitoso
- ✅ Añadido a `settings.gradle.kts`

---

##### 1.2. Definir `SessionState` sealed class
**Archivo**: `kmp-session/src/commonMain/kotlin/com/edugo/kmp/session/SessionState.kt`

**Descripción**: Modelar todos los estados posibles de la sesión

**Código sugerido**:
```kotlin
package com.edugo.kmp.session

import kotlinx.serialization.Serializable

/**
 * Estados posibles de la sesión de usuario
 */
sealed class SessionState {
    /**
     * Estado inicial antes de verificar la sesión
     */
    data object Unknown : SessionState()
    
    /**
     * Verificando si existe sesión activa
     */
    data object Loading : SessionState()
    
    /**
     * Usuario autenticado con sesión activa
     * @param user Datos del usuario autenticado
     */
    data class LoggedIn(val user: User) : SessionState()
    
    /**
     * No hay sesión activa
     */
    data object LoggedOut : SessionState()
    
    /**
     * Sesión expirada (refresh token inválido)
     */
    data object Expired : SessionState()
    
    /**
     * Error al verificar sesión
     * @param error Error que causó la falla
     */
    data class Error(val error: com.edugo.kmp.foundation.AppError) : SessionState()
}

/**
 * Modelo de usuario para el estado de sesión
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String? = null,
    val roles: List<String> = emptyList()
)
```

**Criterios de aceptación**:
- ✅ Sealed class con todos los estados
- ✅ User data class serializable
- ✅ Compila en todas las plataformas

---

##### 1.3. Implementar `SessionManager`
**Archivo**: `kmp-session/src/commonMain/kotlin/com/edugo/kmp/session/SessionManager.kt`

**Descripción**: Componente principal que gestiona el estado global de sesión

**Responsabilidades**:
- Exponer `StateFlow<SessionState>` observable
- Coordinar con `AuthService` para login/logout
- Verificar sesión al iniciar app
- Manejar eventos de expiración

**API pública**:
```kotlin
interface SessionManager {
    /**
     * Estado actual de la sesión (observable)
     */
    val sessionState: StateFlow<SessionState>
    
    /**
     * Verifica si hay una sesión activa almacenada
     * @return User si sesión válida, null si no
     */
    suspend fun checkSession(): Result<User?>
    
    /**
     * Inicia sesión con credenciales
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * Cierra sesión y limpia datos
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Obtiene usuario actual si hay sesión activa
     */
    fun getCurrentUser(): User?
}
```

**Criterios de aceptación**:
- ✅ Interface `SessionManager` definida
- ✅ Implementación `SessionManagerImpl` con StateFlow
- ✅ Métodos usan `AuthService` internamente
- ✅ Transiciones de estado correctas
- ✅ Thread-safe (uso de Mutex si necesario)

---

##### 1.4. Tests para `SessionManager`
**Archivo**: `kmp-session/src/commonTest/kotlin/com/edugo/kmp/session/SessionManagerTest.kt`

**Descripción**: Tests exhaustivos de state management

**Casos de test mínimos**:
1. ✅ Estado inicial es `Unknown`
2. ✅ `checkSession()` con sesión válida → `LoggedIn`
3. ✅ `checkSession()` sin sesión → `LoggedOut`
4. ✅ `checkSession()` con token expirado → `Expired`
5. ✅ `login()` exitoso → `LoggedIn` + StateFlow actualizado
6. ✅ `login()` fallido → `Error` + StateFlow con error
7. ✅ `logout()` → `LoggedOut` + StateFlow actualizado
8. ✅ `getCurrentUser()` retorna null cuando `LoggedOut`
9. ✅ `getCurrentUser()` retorna User cuando `LoggedIn`
10. ✅ Race condition: múltiples `checkSession()` simultáneos

**Criterios de aceptación**:
- ✅ Mínimo 20 tests
- ✅ Coverage > 80%
- ✅ Tests pasan en Desktop + Wasm

---

##### 1.5. Añadir `sessionModule` a DI
**Archivo**: `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/SessionModule.kt`

**Descripción**: Configurar inyección de dependencias para SessionManager

**Código sugerido**:
```kotlin
package com.edugo.kmp.di

import com.edugo.kmp.session.SessionManager
import com.edugo.kmp.session.SessionManagerImpl
import org.koin.dsl.module

val sessionModule = module {
    single<SessionManager> { 
        SessionManagerImpl(
            authService = get(),
            logger = get()
        )
    }
}
```

**Actualizar**: `modules/di/src/commonMain/kotlin/com/edugo/kmp/di/KoinModules.kt`
```kotlin
fun getAllModules() = listOf(
    foundationModule,
    loggerModule,
    storageModule,
    networkModule,
    configModule,
    authModule,
    sessionModule // NUEVO
)
```

**Criterios de aceptación**:
- ✅ `SessionManager` es singleton
- ✅ DI tests pasan
- ✅ No hay ciclos de dependencias

---

### **FASE 2: Session Restoration Logic** ⭐ CRÍTICO
**Duración estimada**: 2-3 días
**Prioridad**: ALTA

#### Tareas:

##### 2.1. Añadir `restoreSession()` a `AuthService`
**Archivo**: `modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/AuthService.kt`

**Descripción**: Implementar lógica de restauración de sesión desde storage

**Código sugerido**:
```kotlin
/**
 * Restaura sesión desde tokens almacenados
 * @return User si sesión válida, null si no hay sesión o está expirada
 */
suspend fun restoreSession(): Result<User?> = withContext(ioDispatcher) {
    try {
        // 1. Obtener tokens de storage
        val accessToken = storage.getString(KEY_ACCESS_TOKEN, "")
        val refreshToken = storage.getString(KEY_REFRESH_TOKEN, "")
        
        if (accessToken.isEmpty() || refreshToken.isEmpty()) {
            return@withContext Result.success(null)
        }
        
        // 2. Validar access token
        val tokenValidation = jwtTokenParser.parse(accessToken)
        
        when {
            tokenValidation.isFailure -> {
                // Token inválido, intentar refresh
                val refreshResult = tokenRefreshManager.refreshToken(refreshToken)
                if (refreshResult.isFailure) {
                    // Refresh también falló, limpiar sesión
                    clearSession()
                    return@withContext Result.success(null)
                }
            }
            tokenValidation.getOrNull()?.isExpired() == true -> {
                // Token expirado, intentar refresh
                val refreshResult = tokenRefreshManager.refreshToken(refreshToken)
                if (refreshResult.isFailure) {
                    clearSession()
                    return@withContext Result.success(null)
                }
            }
        }
        
        // 3. Obtener datos de usuario de storage
        val userData = storage.getString(KEY_USER_DATA, "")
        if (userData.isEmpty()) {
            // No hay datos de usuario, hacer logout
            clearSession()
            return@withContext Result.success(null)
        }
        
        // 4. Deserializar usuario
        val user = Json.decodeFromString<User>(userData)
        
        Result.success(user)
    } catch (e: Exception) {
        logger.e("Error restoring session", e)
        Result.failure(AppError.unexpected(e))
    }
}

private suspend fun clearSession() {
    storage.remove(KEY_ACCESS_TOKEN)
    storage.remove(KEY_REFRESH_TOKEN)
    storage.remove(KEY_USER_DATA)
}

companion object {
    const val KEY_ACCESS_TOKEN = "auth_access_token"
    const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    const val KEY_USER_DATA = "auth_user_data" // NUEVO
}
```

**Criterios de aceptación**:
- ✅ `restoreSession()` implementado
- ✅ Valida access token antes de retornar
- ✅ Intenta refresh si token expirado
- ✅ Limpia storage si refresh falla
- ✅ Retorna User deserializado si válido
- ✅ Maneja errores gracefully

---

##### 2.2. Modificar `login()` para guardar User
**Archivo**: `modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/AuthService.kt`

**Descripción**: Guardar datos de usuario en storage al hacer login

**Modificación**:
```kotlin
suspend fun login(email: String, password: String): Result<AuthResponse> {
    // ... código existente ...
    
    // Después de guardar tokens:
    storage.putString(KEY_ACCESS_TOKEN, response.accessToken)
    storage.putString(KEY_REFRESH_TOKEN, response.refreshToken)
    
    // NUEVO: Guardar datos de usuario
    val userJson = Json.encodeToString(response.user)
    storage.putString(KEY_USER_DATA, userJson)
    
    Result.success(response)
}
```

**Criterios de aceptación**:
- ✅ User se serializa y guarda en storage
- ✅ Tests de login actualizados
- ✅ No rompe tests existentes

---

##### 2.3. Tests para `restoreSession()`
**Archivo**: `modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/AuthServiceTest.kt`

**Descripción**: Tests de restauración de sesión

**Casos de test nuevos**:
1. ✅ `restoreSession()` con tokens válidos → retorna User
2. ✅ `restoreSession()` sin tokens → retorna null
3. ✅ `restoreSession()` con access token expirado + refresh válido → refresh + retorna User
4. ✅ `restoreSession()` con ambos tokens expirados → limpia storage + retorna null
5. ✅ `restoreSession()` con user data corrupto → retorna error
6. ✅ `restoreSession()` con token inválido → limpia storage + retorna null

**Criterios de aceptación**:
- ✅ Mínimo 10 nuevos tests
- ✅ Coverage de `restoreSession()` > 90%
- ✅ Tests pasan en todas las plataformas

---

### **FASE 3: UI Integration** ⭐ CRÍTICO
**Duración estimada**: 1-2 días
**Prioridad**: ALTA

#### Tareas:

##### 3.1. Integrar SessionManager en SplashScreen
**Archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/splash/SplashScreen.kt`

**Descripción**: Verificar sesión al iniciar y navegar según resultado

**Código sugerido**:
```kotlin
@Composable
fun SplashScreen(
    navigationState: NavigationState,
    sessionManager: SessionManager = koinInject()
) {
    var isCheckingSession by remember { mutableStateOf(true) }
    
    // Observar estado de sesión
    val sessionState by sessionManager.sessionState.collectAsState()
    
    LaunchedEffect(Unit) {
        // Verificar sesión al entrar
        sessionManager.checkSession()
    }
    
    // Navegar según estado
    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.LoggedIn -> {
                // Sesión activa, ir a Home
                navigationState.navigate(Routes.Home)
            }
            is SessionState.LoggedOut,
            is SessionState.Expired -> {
                // Sin sesión, ir a Login
                navigationState.navigate(Routes.Login)
            }
            SessionState.Loading -> {
                // Mostrar splash mientras verifica
                isCheckingSession = true
            }
            is SessionState.Error -> {
                // Error al verificar, ir a Login
                navigationState.navigate(Routes.Login)
            }
            SessionState.Unknown -> {
                // Estado inicial, esperar
            }
        }
    }
    
    // UI del Splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isCheckingSession) {
            CircularProgressIndicator()
        }
        // Logo, etc.
    }
}
```

**Criterios de aceptación**:
- ✅ SplashScreen verifica sesión al iniciar
- ✅ Navega a Home si sesión activa
- ✅ Navega a Login si sin sesión
- ✅ Muestra loading mientras verifica
- ✅ Maneja errores correctamente

---

##### 3.2. Integrar SessionManager en LoginScreen
**Archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/login/LoginScreen.kt`

**Descripción**: Usar SessionManager para login en lugar de llamar AuthService directamente

**Modificación**:
```kotlin
@Composable
fun LoginScreen(
    navigationState: NavigationState,
    sessionManager: SessionManager = koinInject()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Observar estado de sesión
    val sessionState by sessionManager.sessionState.collectAsState()
    
    // Navegar si login exitoso
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedIn) {
            navigationState.navigate(Routes.Home)
        }
    }
    
    fun onLoginClick() {
        isLoading = true
        errorMessage = null
        
        scope.launch {
            val result = sessionManager.login(email, password)
            isLoading = false
            
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message 
                    ?: "Error al iniciar sesión"
            }
        }
    }
    
    // UI...
}
```

**Criterios de aceptación**:
- ✅ LoginScreen usa `SessionManager.login()`
- ✅ Navega a Home tras login exitoso
- ✅ Muestra errores correctamente
- ✅ Loading state funciona

---

##### 3.3. Integrar SessionManager en SettingsScreen
**Archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/settings/SettingsScreen.kt`

**Descripción**: Implementar logout funcional

**Modificación**:
```kotlin
@Composable
fun SettingsScreen(
    navigationState: NavigationState,
    sessionManager: SessionManager = koinInject()
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    val currentUser = (sessionState as? SessionState.LoggedIn)?.user
    
    // Navegar a Login tras logout
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedOut) {
            navigationState.navigate(Routes.Login)
        }
    }
    
    Column {
        // Mostrar info de usuario
        currentUser?.let { user ->
            Text("Usuario: ${user.email}")
            Text("Nombre: ${user.name ?: "N/A"}")
        }
        
        // Botón de logout
        Button(
            onClick = {
                scope.launch {
                    sessionManager.logout()
                }
            }
        ) {
            Text("Cerrar Sesión")
        }
    }
}
```

**Criterios de aceptación**:
- ✅ Muestra info de usuario actual
- ✅ Botón de logout funcional
- ✅ Navega a Login tras logout
- ✅ Limpia datos correctamente

---

##### 3.4. Actualizar HomeScreen para mostrar usuario
**Archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/home/HomeScreen.kt`

**Descripción**: Mostrar información del usuario autenticado

**Modificación**:
```kotlin
@Composable
fun HomeScreen(
    navigationState: NavigationState,
    sessionManager: SessionManager = koinInject()
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    val currentUser = (sessionState as? SessionState.LoggedIn)?.user
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Bienvenido ${currentUser?.name ?: currentUser?.email ?: ""}")
                }
            )
        }
    ) { padding ->
        // Contenido...
    }
}
```

**Criterios de aceptación**:
- ✅ Muestra nombre/email de usuario
- ✅ Reactivo a cambios de sesión
- ✅ Maneja caso sin usuario

---

##### 3.5. Tests de UI (opcional pero recomendado)
**Archivos**: `kmp-screens/src/commonTest/kotlin/...`

**Descripción**: Tests de integración para flujos de UI

**Casos de test sugeridos**:
1. ✅ SplashScreen navega a Home si sesión activa
2. ✅ SplashScreen navega a Login si sin sesión
3. ✅ LoginScreen navega a Home tras login exitoso
4. ✅ SettingsScreen navega a Login tras logout

**Criterios de aceptación**:
- ✅ Mínimo 5 tests de navegación
- ✅ Usan mocks de SessionManager
- ✅ Pasan en Desktop + Wasm

---

### **FASE 4: Session Expiration Handling** 🔶 IMPORTANTE
**Duración estimada**: 1-2 días
**Prioridad**: MEDIA

#### Tareas:

##### 4.1. Listener de expiración en SessionManager
**Archivo**: `kmp-session/src/commonMain/kotlin/com/edugo/kmp/session/SessionManager.kt`

**Descripción**: Detectar cuando refresh token expira y notificar UI

**Código sugerido**:
```kotlin
class SessionManagerImpl(
    private val authService: AuthService,
    private val logger: EduGoLogger
) : SessionManager {
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    init {
        // Escuchar eventos de expiración de AuthService
        listenToAuthEvents()
    }
    
    private fun listenToAuthEvents() {
        // TODO: Implementar si AuthService emite eventos
        // Por ahora, manejar en checkSession() y login()
    }
    
    override suspend fun checkSession(): Result<User?> {
        _sessionState.value = SessionState.Loading
        
        val result = authService.restoreSession()
        
        _sessionState.value = when {
            result.isSuccess && result.getOrNull() != null -> {
                SessionState.LoggedIn(result.getOrNull()!!)
            }
            result.isSuccess && result.getOrNull() == null -> {
                SessionState.LoggedOut
            }
            else -> {
                val error = result.exceptionOrNull() as? AppError 
                    ?: AppError.unexpected(result.exceptionOrNull())
                
                // Si es error de autenticación, marcar como expirado
                if (error.code == ErrorCode.UNAUTHORIZED) {
                    SessionState.Expired
                } else {
                    SessionState.Error(error)
                }
            }
        }
        
        return result
    }
}
```

**Criterios de aceptación**:
- ✅ SessionManager detecta tokens expirados
- ✅ Emite `SessionState.Expired`
- ✅ UI puede reaccionar a expiración

---

##### 4.2. Manejo de 401 en Network Interceptor
**Archivo**: `modules/network/src/commonMain/kotlin/com/edugo/kmp/network/interceptors/AuthInterceptor.kt`

**Descripción**: Si refresh falla con 401, notificar que sesión expiró

**Modificación**:
```kotlin
// En AuthInterceptor o TokenRefreshManager
private suspend fun refreshAccessToken(): String? {
    val refreshToken = tokenProvider.getRefreshToken() ?: return null
    
    return try {
        val response = httpClient.post("/auth/refresh") {
            setBody(RefreshTokenRequest(refreshToken))
        }
        
        val newAccessToken = response.body<RefreshTokenResponse>().accessToken
        
        // Guardar nuevo token
        tokenProvider.saveAccessToken(newAccessToken)
        
        newAccessToken
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.Unauthorized) {
            // Refresh token expirado
            logger.w("Refresh token expired")
            // TODO: Emitir evento de sesión expirada
            // Por ahora, AuthService.restoreSession() detectará esto
        }
        null
    }
}
```

**Criterios de aceptación**:
- ✅ Detecta 401 en refresh
- ✅ Loggea evento
- ✅ No crashea la app

---

##### 4.3. UI feedback de sesión expirada
**Archivo**: `kmp-screens/src/commonMain/kotlin/com/edugo/kmp/screens/App.kt`

**Descripción**: Mostrar Snackbar/Dialog cuando sesión expira

**Código sugerido**:
```kotlin
@Composable
fun App(
    sessionManager: SessionManager = koinInject()
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar mensaje si sesión expiró
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Expired) {
            snackbarHostState.showSnackbar(
                message = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.",
                duration = SnackbarDuration.Long
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        // Navegación y pantallas...
    }
}
```

**Criterios de aceptación**:
- ✅ Muestra mensaje cuando sesión expira
- ✅ Navega a Login automáticamente
- ✅ No muestra mensaje múltiples veces

---

### **FASE 5: Secure Storage (OPCIONAL)** 🔵 BAJA PRIORIDAD
**Duración estimada**: 3-5 días
**Prioridad**: BAJA (puede dejarse para Sprint 6)

#### Tareas:

##### 5.1. Crear módulo `kmp-security`
**Archivo**: `kmp-security/build.gradle.kts`

**Descripción**: Módulo para almacenamiento encriptado

**Configuración**:
- Plugin: `kmp.android` (Android + Desktop + WasmJs + iOS)
- Dependencias platform-specific:
  - Android: Jetpack Security Crypto
  - iOS: Keychain (native)
  - Desktop: java-keyring
  - Wasm: Web Crypto API

**Criterios de aceptación**:
- ✅ Módulo compila en todas las plataformas
- ✅ Dependencias platform-specific correctas

---

##### 5.2. Implementar `SecureStorage` interface
**Archivo**: `kmp-security/src/commonMain/kotlin/com/edugo/kmp/security/SecureStorage.kt`

**Descripción**: Interface para storage encriptado

**API**:
```kotlin
interface SecureStorage {
    suspend fun saveSecure(key: String, value: String): Result<Unit>
    suspend fun getSecure(key: String): Result<String?>
    suspend fun removeSecure(key: String): Result<Unit>
    suspend fun clearAll(): Result<Unit>
}
```

**Criterios de aceptación**:
- ✅ Interface definida
- ✅ Documentación clara

---

##### 5.3-5.6. Implementaciones platform-specific
**Archivos**: 
- `androidMain/.../SecureStorageAndroid.kt`
- `iosMain/.../SecureStorageIos.kt`
- `desktopMain/.../SecureStorageDesktop.kt`
- `wasmJsMain/.../SecureStorageWasm.kt`

**Descripción**: Implementar encriptación en cada plataforma

**Criterios de aceptación**:
- ✅ Android: EncryptedSharedPreferences
- ✅ iOS: Keychain Services
- ✅ Desktop: OS keyring o fallback
- ✅ Wasm: Web Crypto API (SubtleCrypto)
- ✅ Tests para cada implementación

---

##### 5.7. Integrar SecureStorage en AuthService
**Archivo**: `modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/AuthService.kt`

**Descripción**: Usar SecureStorage para tokens en vez de storage plano

**Modificación**:
```kotlin
class AuthServiceImpl(
    private val storage: EduGoStorage,
    private val secureStorage: SecureStorage?, // Opcional
    // ...
) {
    private suspend fun saveTokens(access: String, refresh: String) {
        if (secureStorage != null) {
            secureStorage.saveSecure(KEY_ACCESS_TOKEN, access)
            secureStorage.saveSecure(KEY_REFRESH_TOKEN, refresh)
        } else {
            storage.putString(KEY_ACCESS_TOKEN, access)
            storage.putString(KEY_REFRESH_TOKEN, refresh)
        }
    }
}
```

**Criterios de aceptación**:
- ✅ Usa SecureStorage si disponible
- ✅ Fallback a storage plano si no
- ✅ No rompe funcionalidad existente

---

### **FASE 6: Testing y QA** ⭐ CRÍTICO
**Duración estimada**: 2-3 días
**Prioridad**: ALTA

#### Tareas:

##### 6.1. Tests de integración end-to-end
**Archivo**: `kmp-screens/src/commonTest/kotlin/integration/SessionFlowTest.kt`

**Descripción**: Test del flujo completo de sesión

**Escenarios**:
1. ✅ App inicia sin sesión → Splash → Login
2. ✅ Login exitoso → Home
3. ✅ Logout → Login
4. ✅ App reinicia con sesión activa → Splash → Home
5. ✅ App reinicia con sesión expirada → Splash → Login

**Criterios de aceptación**:
- ✅ Mínimo 5 tests E2E
- ✅ Usan SessionManager real (no mock)
- ✅ Pasan en todas las plataformas

---

##### 6.2. QA Manual en todas las plataformas

**Android**:
- [ ] Login y ver que Home muestra usuario
- [ ] Cerrar app y reabrir → va directo a Home
- [ ] Logout y verificar que va a Login
- [ ] Login con credenciales incorrectas → muestra error

**Desktop**:
- [ ] Mismo flujo que Android
- [ ] Verificar que SharedPreferences persiste entre ejecuciones

**WasmJs**:
- [ ] Mismo flujo que Android
- [ ] Verificar que localStorage persiste tokens
- [ ] Probar en Chrome + Firefox

**iOS** (si habilitado):
- [ ] Mismo flujo que Android
- [ ] Verificar NSUserDefaults

**Criterios de aceptación**:
- ✅ Todos los flujos funcionan en todas las plataformas
- ✅ Sin crashes
- ✅ Persistencia funciona correctamente

---

##### 6.3. Performance Testing

**Métricas a medir**:
- ⏱️ Tiempo de `checkSession()` en Splash
- ⏱️ Tiempo de `login()` 
- ⏱️ Tiempo de `logout()`
- 💾 Tamaño de datos en storage

**Objetivos**:
- ✅ `checkSession()` < 500ms
- ✅ Storage < 10KB

**Criterios de aceptación**:
- ✅ Métricas dentro de objetivos
- ✅ No memory leaks
- ✅ No blocking en UI thread

---

##### 6.4. Security Testing

**Verificaciones**:
- 🔒 Tokens no se loggean en producción
- 🔒 Storage no es accesible sin root/jailbreak
- 🔒 Tokens se limpian tras logout
- 🔒 Refresh token no se expone en network logs

**Criterios de aceptación**:
- ✅ No se encuentra tokens en logs
- ✅ Storage encriptado en Android (si SecureStorage implementado)
- ✅ HTTPS only en network calls

---

## Resumen de Criterios de Aceptación Globales

### Funcionales:
1. ✅ Usuario puede iniciar sesión y la sesión persiste
2. ✅ Al reabrir la app, usuario va directo a Home si sesión activa
3. ✅ Usuario puede cerrar sesión y volver a Login
4. ✅ Si tokens expiran, usuario ve mensaje y va a Login
5. ✅ Errores de red se manejan gracefully

### Técnicos:
1. ✅ Arquitectura limpia (SessionManager → AuthService → Storage)
2. ✅ Tests: mínimo 100 nuevos tests (80% coverage)
3. ✅ Sin memory leaks ni race conditions
4. ✅ Funciona en Android + Desktop + Wasm (+ iOS opcional)
5. ✅ DI configurado correctamente

### No Funcionales:
1. ✅ Performance: `checkSession()` < 500ms
2. ✅ Security: Tokens no en logs, storage seguro
3. ✅ UX: Loading states claros, errores informativos
4. ✅ Mantenibilidad: Código documentado, fácil de extender

---

## Estimación de Esfuerzo

| Fase | Duración | Desarrolladores |
|------|----------|-----------------|
| Fase 1: Session State Management | 2-3 días | 1 dev |
| Fase 2: Session Restoration | 2-3 días | 1 dev |
| Fase 3: UI Integration | 1-2 días | 1 dev |
| Fase 4: Expiration Handling | 1-2 días | 1 dev |
| Fase 5: Secure Storage (OPCIONAL) | 3-5 días | 1 dev |
| Fase 6: Testing & QA | 2-3 días | 1-2 devs |

**Total sin Secure Storage**: 8-13 días (1.5-2.5 semanas)  
**Total con Secure Storage**: 11-18 días (2-3.5 semanas)

---

## Riesgos y Contingencias

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Token refresh race conditions | Baja | Alto | Ya mitigado con Mutex en TokenRefreshManager |
| Storage corruption en Wasm | Media | Medio | Try-catch + fallback a logout |
| Performance en devices viejos | Media | Bajo | Optimizar checkSession(), lazy loading |
| Secure Storage complejo en iOS | Alta | Bajo | Dejar para Sprint 6, usar storage plano inicialmente |
| Tests flaky en Wasm | Media | Medio | Aumentar timeouts, mockar storage |

---

## Dependencias Externas

- ✅ Koin 4.1.0 (ya integrado)
- ✅ Ktor 3.1.3 (ya integrado)
- ✅ multiplatform-settings (ya integrado)
- ⚠️ Jetpack Security Crypto (solo si Secure Storage)
- ⚠️ java-keyring (solo si Secure Storage Desktop)

---

## Notas Adicionales

### Escalabilidad Futura:
- **Sprint 6**: Secure Storage completo
- **Sprint 7**: Autenticación biométrica
- **Sprint 8**: SSO / OAuth
- **Sprint 9**: Refresh token rotation
- **Sprint 10**: Session analytics

### Compatibilidad con Backend:
- Asegurar que `/auth/login`, `/auth/refresh`, `/auth/logout` existan
- Validar formato de JWT (claims: `sub`, `exp`, `roles`)
- Configurar CORS para Wasm

---

## Checklist Pre-Sprint

Antes de empezar el sprint, verificar:
- [ ] Backend de auth está funcional (login/refresh/logout)
- [ ] Tokens JWT tienen claims estándar
- [ ] Equipo alineado en arquitectura propuesta
- [ ] Prioridades claras (Fases 1-4 críticas, Fase 5 opcional)
- [ ] Ambiente de QA listo para todas las plataformas

---

**Última actualización**: 2026-02-10  
**Autor**: Claude (AI Assistant)  
**Versión**: 1.0
