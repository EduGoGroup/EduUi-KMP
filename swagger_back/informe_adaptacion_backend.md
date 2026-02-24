# Informe de Adaptación: Migración al Nuevo Backend (3 APIs)

Este informe detalla los hallazgos y pasos necesarios para adaptar el proyecto KMP a la nueva arquitectura orientada a microservicios/APIs (pasando de 2 APIs base a 3: `Admin`, `Mobile` e `IAM Platform`).

## A) Adaptación de Archivos de Configuración de Entorno

Actualmente, el proyecto KMP almacena las URLs de los backends en la interfaz `AppConfig` (ubicada en `com.edugo.kmp.config.AppConfig`), la cual se pobla a través de su implementación `AppConfigImpl` hidratada desde archivos JSON en `resources/config/` dependiendo del ambiente (local, dev, staging, prod).

**Cambios Necesarios:**
1. **Actualizar `AppConfig` y `AppConfigImpl`:** 
   - Se debe agregar una nueva propiedad, por ejemplo `iamApiBaseUrl: String` (o `authApiBaseUrl`), a nivel de la interfaz y el data class.
   - Esto reflejaría las 3 APIs en lugar de 2: `adminApiBaseUrl`, `mobileApiBaseUrl` e `iamApiBaseUrl`.
2. **Actualizar archivos JSON Ambientales:**
   - Modificar los archivos en `resources/config/` (ej. `dev.json`, `local.json`, `prod.json`) para incluir el campo `"iamApiBaseUrl": "https://[IAM_URL]_O_PUERTO"`.
3. **Inyección de Dependencias (Koin):**
   - En `AuthModule.kt` y `DynamicUiModule.kt` (o donde se inyecte `AuthRepositoryImpl`), se debe asegurar de pasar la nueva `iamApiBaseUrl` en lugar de la genérica `Local` o `adminApiBaseUrl`. En `AuthRepositoryImpl` ya existe el parámetro `baseUrl`, solo hay que alimentarlo con el correcto proveniente de `AppConfig`.

## B) Adaptación a los Nuevos Endpoints y Ubicaciones

Tras analizar los Swagger JSON extraídos (`edugo-api-iam-platform`, `edugo-api-admin-new`, `edugo-api-mobile-new`), se observa la siguiente distribución:

1. **Autenticación (Migración hacia IAM Platform):**
   - El `AuthRepositoryImpl` actual consume las rutas `/v1/auth/login`, `/v1/auth/refresh`, `/v1/auth/contexts`, `/v1/auth/switch-context`, `/v1/auth/verify`, y `/v1/auth/logout`.
   - **Mapeo:** El nuevo `edugo-api-iam-platform` incluye exactamente todas estas rutas (bajo `/auth/login`, `/auth/refresh`, etc.).
   - **Acción:** Dirigir toda petición del `AuthRepositoryImpl` apuntando a `iamApiBaseUrl`. *(Nota: revisar si el nuevo API conserva el prefijo `/v1/` en su `basePath` o si la ruta ahora es directa en la raíz; si no lo tiene, habrá que eliminar el `/v1` de la constante en KMP).*

2. **Navegación y Pantallas Dinámicas:**
   - Anteriormente, las llamadas a `/v1/screens/navigation` y `/v1/screens/{screenKey}` estaban unificadas o agrupadas.
   - **Mapeo actual en Swaggers:**
     - La nueva API `edugo-api-mobile-new` aloja los endpoints orientados al consumidor: `/screens/navigation`, `/screens/{screenKey}`, y `/screens/resource/{resourceKey}`.
     - La nueva API `edugo-api-iam-platform` aloja lo relacionado con la _configuración_ de dichas pantallas (orientado a admin): `/screen-config/instances`, `/screen-config/templates`, etc.
   - **Acción:** En `RemoteScreenLoader.kt`, mapear las llamadas GET de `loadScreen` y `loadNavigation` hacia el `mobileApiBaseUrl`, verificando de nuevo el uso del prefijo `/v1/`.

3. **Inyección Dinámica de Dominio (Resolución SDUI):**
   - En `RemoteDataLoader.kt`, se usa el prefijo en las URNs para saber a qué API ir (`admin:` -> `adminBaseUrl`, `mobile:` -> `mobileBaseUrl`). Se recomienda añadir el prefijo `iam:` para poder consumir recursos cruzados generados desde el backend, tales como la tabla de `/users/{id}/roles` (ahora en IAM).

## C) Informe de Endpoints No Migrados / Aspectos Faltantes

Al cruzar la funcionalidad de la App con los Swaggers, existen las siguientes consideraciones/alertas:

1. **Gestión del Menú Dinámico:**
   - El swagger de `edugo-api-iam-platform` introdujo los nuevos endpoints `/menu` y `/menu/full`, concebidos para gestionar centralmente los menús según el rol.
   - **Faltante/Desfase:** La app KMP actualmente **no usa un servicio de "Menu" específico**, sino que dibuja su drawer de navegación llamando a `/v1/screens/navigation` (ahora en Mobile API). Si el backend delegó la autoridad del menú a IAM, la aplicación de Kotlin debería depreciar el enpoint de "screens/navigation" a favor de los de `/menu` dentro del IAM para reflejar correctamente los permisos de acceso y roles.

2. **Acciones Dinámicas SDUI vs IAM:**
   - La API de IAM ahora concentra permisos (`/permissions`, `/roles`). Sin embargo, en la carga de la UI dinámica no parece haber un contrato estándar para habilitar/deshabilitar botones en frontend evaluando estos permisos (feature flags o capabilities). Por ahora, parece depender de que el backend simplemente no retorne un botón si el usuario no tiene rol.

**Conclusión General:**
El cambio es altamente factible en la App KMP dado el empaquetado y modularidad en `modules/auth`, `modules/config` y `modules/dynamic-ui`. El mayor esfuerzo radicará en actualizar los data classes de Configuración (`AppConfig`) e implementar el prefijo faltante `iam:` en el `RemoteDataLoader` (Data Driven UI) para permitir consultas inter-dominio que provengan desde los JSON dinámicos.
