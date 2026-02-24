Aquí tienes el flujo de endpoints durante el arranque de la aplicación:

Secuencia de endpoints al iniciar la app
Fase 1: Inicialización DI (sin llamadas HTTP)
Koin carga todos los módulos (core, config, network, auth, dynamicui, etc.)
Fase 2: Splash Screen - Restauración de sesión
authService.restoreSession() — Lee tokens del storage local
Si el token expiró y hay refresh token:
POST /v1/auth/refresh
Fase 3: Si NO hay sesión → Login
POST /v1/auth/login — Envía email + password
GET /v1/auth/contexts — Obtiene contextos disponibles del usuario
Fase 4: Si es Super Admin sin schoolId
GET /v1/schools — Lista de colegios activos
POST /v1/auth/switch-context — Selecciona colegio y obtiene nuevo token
Fase 5: Carga de navegación y pantallas (Dynamic UI)
GET /v1/screens/navigation?platform=mobile — Definición de navegación (drawer/bottom nav)
GET /v1/screens/{screenKey}?platform=mobile — Configuración de la pantalla inicial (ej: dashboard-teacher)
Fase 6: Carga de datos dinámicos
GET endpoints dinámicos según la config de pantalla (ej: /v1/stats/global, /v1/materials, etc.)
Resumen del flujo completo

App() → Koin init → SplashScreen
  ↓
restoreSession() → [POST /v1/auth/refresh si token expirado]
  ↓
Si no autenticado → LoginScreen
  → POST /v1/auth/login
  → GET /v1/auth/contexts
  ↓
Si super_admin sin school:
  → GET /v1/schools
  → POST /v1/auth/switch-context
  ↓
MainScreen
  → GET /v1/screens/navigation
  → GET /v1/screens/{screenKey}
  → GET /v1/{datos dinámicos según config}
APIs involucradas
Admin API (puerto 8081): auth, schools, switch-context
Mobile API (puerto 9091): screens/navigation, screens/{key}, datos dinámicos