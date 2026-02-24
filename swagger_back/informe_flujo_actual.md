# Informe de Evaluación: Flujo de Inicio Actual

Este informe presenta el análisis del flujo de inicio actual en la aplicación cliente (KMP), detallando cómo se abordan la autenticación, la carga del menú y la renderización de la interfaz de usuario (UI) dinámica.

## A) Autenticación y Login

El flujo de autenticación está centralizado en el módulo `auth`, específicamente en la implementación `AuthRepositoryImpl`.

*   **Endpoint Principal**: El cliente realiza una petición `POST` a `{baseUrl}/v1/auth/login` enviando las credenciales (`LoginCredentials`).
*   **Cliente HTTP**: Se utiliza `EduGoHttpClient`, el cual envuelve el cliente de Ktor y provee mecanismos de resiliencia como **Circuit Breaker** (cortocircuito) y **Retry Policy** (reintentos).
*   **Manejo de Errores**: Los errores HTTP son mapeados a códigos de error propios del dominio (ej. `ErrorCode.AUTH_UNAUTHORIZED` para 401, `ErrorCode.AUTH_FORBIDDEN` para 403, `ErrorCode.AUTH_ACCOUNT_LOCKED` para 423), lo que permite a la aplicación reaccionar de forma estandarizada y mostrar mensajes amigables al usuario.
*   **Gestión de Sesión**: Existen endpoints adicionales cubiertos en el mismo módulo para renovar el token (`/v1/auth/refresh`), verificar sesión (`/v1/auth/verify`) y cerrar sesión (`/v1/auth/logout`), manejando la inyección del token Bearer en las cabeceras.

## B) Carga del Menú según Autenticación/Autorización

La aplicación adapta el menú basado en el contexto del usuario autenticado:

*   **Componente Visual (`UserMenuHeader`)**: Este componente de Jetpack Compose muestra la información del contexto actual del usuario (iniciales, nombre, rol y escuela activa).
*   **Navegación Dinámica`: El menú principal de la aplicación no está estático. A través del `RemoteScreenLoader` (módulo `dynamic-ui`), el cliente hace un `GET` al endpoint `{baseUrl}/v1/screens/navigation?platform={mobile/web}`. Esta respuesta asume proveer la definición del esquema de navegación (`NavigationDefinition`), lo cual permite que el backend dicte qué módulos y opciones de menú están habilitados según el rol y autorización del perfil activo.
*   **Cambio de Contexto**: El cliente permite cambiar entre diferentes colegios/roles a través del endpoint `/v1/auth/contexts` y `/v1/auth/switch-context`, actualizando el token y, asumo, recargando el esquema de navegación.

## C) Carga de la UI Dinámica

El modelo Server-Driven UI (SDUI) permite a la aplicación construir las pantallas desde el backend:

*   **Carga de Pantalla**: Al navegar a una ruta o clave de pantalla específica, el `RemoteScreenLoader` lanza una petición `GET` a `{baseUrl}/v1/screens/{screenKey}?platform={platform}`.
*   **Definición (ScreenDefinition)**: El backend devuelve un esquema JSON que representa los componentes visuales a renderizar. Esto se maneja usando Koin para inyectar los handlers correctos desde `DynamicUiModule`.
*   **Manejadores (Screen Handlers)**: Diferentes `ActionHandlers` (como `NavigateHandler`, `ApiCallHandler`, `SubmitFormHandler`) traducen las acciones descritas en el JSON enviado desde el backend a comportamientos reales dentro de la app multiplataforma.

---
**Conclusión de la Evaluación actual:**
Actualmente la aplicación tiene un fuerte acoplamiento a una sola `baseUrl` para consumir tanto la estructura de Server-Driven UI y de Navegación (`/v1/screens/...`) como las utilidades de Auth (`/v1/auth/...`). Debido a los próximos cambios en el backend (división y consolidación de APIs), será necesario modificar la forma en la que se inyectan las URLs y cómo los clientes HTTP dirigen el tráfico a los dominios correctos.
