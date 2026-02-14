# Registros de Decisiones de Arquitectura (ADRs)

## ADR-001: No crear un nuevo servicio API para configuración de pantallas

### Contexto
Los docs de kmp-samples proponen un sistema de renderizado dinámico de pantallas donde templates, datos y acciones se sirven desde el backend. La pregunta es si esto requiere un nuevo microservicio dedicado.

### Ecosistema Actual
- **api-admin**: Gestiona configuración, RBAC, menús, recursos, colegios, usuarios (operaciones de ESCRITURA)
- **api-mobile**: Sirve datos a clientes mobile/frontend (operaciones de LECTURA)
- **worker**: Procesamiento en segundo plano (NLP, evaluaciones)
- **shared**: Concerns transversales

### Decisión
**Extender las APIs existentes** en lugar de crear un nuevo servicio:
- **api-admin** maneja operaciones CRUD para templates e instancias de pantalla (administrativo)
- **api-mobile** expone endpoints de solo lectura para servir definiciones combinadas de pantalla a los clientes

### Justificación
1. **Simplicidad operacional**: No hay nuevo servicio que desplegar, monitorear, escalar
2. **Consistencia de patrón**: Admin escribe, Mobile lee - coincide con la arquitectura actual
3. **Infraestructura compartida**: Reutiliza auth, middleware, logging, conexiones de BD existentes
4. **Gestión de recursos**: La tabla Resources en api-admin ya define la estructura de menú; la config de pantalla es una extensión natural
5. **Adopción gradual**: Se puede empezar con configs simples y evolucionar sin migración de servicio

### Consecuencias
- api-admin y api-mobile necesitan nuevos módulos (manejable con clean architecture)
- Se necesitan DTOs compartidos en edugo-shared para tipos de definición de pantalla
- Si la carga de config de pantalla se vuelve pesada, se puede extraer a servicio separado después (la ruta de migración existe)

---

## ADR-002: PostgreSQL con schema dedicado para definiciones de pantalla

### Contexto
Los templates de pantalla contienen JSON estructurado (zones, slots, controls) que necesita almacenamiento. Opciones: PostgreSQL JSONB, documentos MongoDB, o una combinación.

### Decisión
**PostgreSQL** con un schema dedicado `ui_config`, usando columnas **JSONB** para definiciones de templates.

### Diseño de Schema
```sql
CREATE SCHEMA ui_config;

-- Separado del schema public para no saturar las tablas del dominio principal
-- Todas las tablas relacionadas con pantallas viven aquí
```

### Justificación
1. **Ventajas de JSONB**: Soporta indexación, consultas parciales, validación de schema vía constraints CHECK
2. **Consistencia transaccional**: Las definiciones de pantalla pueden participar en transacciones PostgreSQL junto con actualizaciones de recursos/permisos
3. **Base de datos única**: No requiere infraestructura adicional; usa la instancia PostgreSQL existente
4. **Flexibilidad de consultas**: Puede consultar dentro de estructuras JSON (ej: encontrar todos los templates que usan patrón "list")
5. **Herramientas existentes**: El proyecto de infraestructura ya tiene sistema de migraciones PostgreSQL
6. **Separación**: El schema dedicado aísla las tablas de config UI de las tablas de dominio

### ¿Por qué no MongoDB?
- MongoDB se usa para datos con alta escritura y orientados a documentos (evaluaciones, resúmenes, eventos)
- Los templates de pantalla son de alta lectura, cambian raramente, se benefician de garantías transaccionales
- Agregar dependencia de MongoDB a la config de pantallas complicaría la API mobile innecesariamente
- PostgreSQL JSONB es suficiente para las necesidades de almacenamiento JSON

---

## ADR-003: Enfoque híbrido de UI dinámica

### Contexto
Los docs de kmp-samples proponen un sistema completamente dinámico de generación de pantallas "sin código" donde CUALQUIER pantalla puede renderizarse puramente desde JSON. Esto es ambicioso pero puede no ser práctico para la Fase 1.

### Decisión
**Enfoque híbrido**: Configuración dirigida por el backend + renderers de patrones pre-construidos en KMP.

### Cómo Funciona
```
El backend sirve:                  KMP renderiza con:
{                                  PatternRenderer pre-construido
  "pattern": "list",               ├── ListPatternRenderer
  "screenId": "materials-list",    ├── DetailPatternRenderer
  "dataEndpoint": "/v1/materials", ├── FormPatternRenderer
  "config": { ... },               ├── DashboardPatternRenderer
  "fields": [ ... ]                ├── SettingsPatternRenderer
}                                  └── LoginPatternRenderer
```

### Lo que controla el Backend
1. **Qué patrón** usar para cada pantalla (list, detail, form, dashboard, settings)
2. **Endpoint de datos** - de dónde obtener el contenido
3. **Definiciones de campos** - qué campos mostrar, su orden, etiquetas, tipos
4. **Definiciones de acciones** - qué pasa con las interacciones del usuario
5. **Overrides por plataforma** - ajustes de layout por plataforma
6. **Reglas de visibilidad** - visualización condicional de campos basada en permisos/datos

### Lo que controla el Frontend
1. **Implementación de patrones** - cada patrón tiene un renderer pre-construido y probado
2. **Sistema de diseño** - componentes nativos Material 3
3. **Adaptación por plataforma** - layouts responsivos por plataforma
4. **Caché y offline** - almacenamiento local de configs de pantalla
5. **Animaciones y transiciones** - transiciones nativas de plataforma

### Justificación
1. **Velocidad de desarrollo**: Renderers pre-construidos son más rápidos de desarrollar y probar que renderizado completamente dinámico
2. **Seguridad de tipos**: Sealed classes de Kotlin > interpretación JSON en runtime para confiabilidad
3. **Rendimiento**: Renderers compilados son más rápidos que interpretación dinámica de JSON
4. **Testabilidad**: Cada renderer de patrón puede probarse unitariamente de forma independiente
5. **Ruta de migración**: Se puede evolucionar hacia renderizado completamente dinámico en fases futuras
6. **Calidad de diseño**: Renderers pre-construidos aseguran adherencia consistente al sistema de diseño

### Ruta de Evolución
```
Fase 1: Híbrido (config + renderers pre-construidos)
    → Backend define QUÉ mostrar
    → Frontend decide CÓMO mostrarlo

Fase 2+: Dinamismo progresivo
    → Backend controla más detalles de layout
    → Frontend renderiza más desde JSON
    → Renderers custom solo para lógica de negocio compleja
```

---

## ADR-004: Modelo de relación Recurso-Pantalla

### Contexto
La tabla existente `resources` define ítems de menú con: key, displayName, icon, parentID, sortOrder, isMenuVisible, scope. Necesitamos asociar definiciones de pantalla con recursos.

### Decisión
Crear una tabla de unión **`resource_screens`** que mapee un recurso a una o más configuraciones de pantalla (vista lista, vista detalle, formulario de creación, etc.).

### Diseño
```sql
-- Un recurso puede tener múltiples vistas de pantalla
-- ej: recurso "materials" tiene:
--   - materials-list (patrón lista)
--   - material-detail (patrón detalle)
--   - material-create (patrón formulario)

CREATE TABLE ui_config.resource_screens (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES public.resources(id),
    screen_instance_id UUID NOT NULL REFERENCES ui_config.screen_instances(id),
    screen_type VARCHAR(50) NOT NULL, -- 'list', 'detail', 'create', 'edit'
    is_default BOOLEAN DEFAULT false,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(resource_id, screen_type)
);
```

### Justificación
1. **Extensión natural**: Los recursos ya representan "cosas a las que el usuario puede acceder" - las pantallas son cómo acceden
2. **Reutilización de permisos**: Los permisos de recursos controlan automáticamente el acceso a pantallas
3. **Integración con menú**: El menú ya filtra por permisos de recurso; ahora cada ítem de menú enlaza a sus pantallas
4. **Múltiples vistas**: Un recurso puede tener vistas de lista, detalle, creación, edición
5. **Flexibilidad**: Fácil intercambiar templates de pantalla para pruebas A/B

### Flujo
```
Usuario se autentica → JWT con permisos
    → GET /v1/menu → Recursos filtrados por permisos
    → Cada recurso tiene configs de pantalla vinculadas
    → Frontend carga config de pantalla del recurso seleccionado
    → Renderiza usando pattern renderer con datos del backend
```

---

## ADR-005: Estrategia de caché para configuración de pantallas

### Contexto
Las configuraciones de pantalla son relativamente estáticas (cambian cuando el admin las actualiza) pero se solicitan frecuentemente por cada sesión de cliente.

### Decisión
**Caché de tres capas**:

1. **Backend (api-mobile)**: Caché Redis con TTL de 1 hora para definiciones de pantalla
2. **Frontend (KMP)**: Caché en memoria con vida de sesión + caché persistente en SafeEduGoStorage
3. **Versionado**: Headers ETag/Last-Modified para solicitudes condicionales

### Invalidación de Caché
```
Admin actualiza template en api-admin
    → Incrementa número de versión
    → Publica evento "screen.updated" a RabbitMQ (opcional)
    → Caché de api-mobile expira naturalmente por TTL (1 hora)
    → Frontend detecta cambio de versión en siguiente solicitud
    → Refresca caché local
```

### Justificación
1. **Baja latencia**: Las pantallas cacheadas cargan instantáneamente
2. **Soporte offline**: Caché persistente permite renderizado sin conexión
3. **Eficiencia de ancho de banda**: ETag previene transferencia innecesaria de datos
4. **Simplicidad**: Expiración basada en TTL es simple y predecible
5. **Consistencia eventual**: Máximo 1 hora de datos desactualizados es aceptable para config de UI

---

## ADR-006: Arquitectura del sistema de acciones

### Contexto
Los docs de kmp-samples definen un sistema de acciones de 4 niveles (enum StandardAction → JSON ActionDefinition → interfaz ActionHandler → ActionRegistry). Necesitamos decidir cuánto implementar en la Fase 1.

### Decisión
**Fase 1: Sistema de acciones simplificado** con solo handlers estándar.

### Alcance Fase 1
```kotlin
// Acciones estándar que cubren el 90% de los casos de uso
enum class StandardAction {
    NAVIGATE,           // Navegar a otra pantalla
    NAVIGATE_BACK,      // Ir atrás
    API_CALL,           // Hacer petición HTTP (GET, POST, PUT, DELETE)
    SUBMIT_FORM,        // Validar + llamada API
    REFRESH,            // Recargar datos de pantalla actual
    CONFIRM,            // Mostrar diálogo de confirmación antes de la acción
    LOGOUT,             // Limpiar sesión
}
```

### Alcance Fase 2 (diferido)
- Handlers de acción custom por dominio de negocio
- Encadenamiento de acciones (onSuccess → siguiente acción)
- Acciones solo-UI (mostrar snackbar, toggle de campo)
- Acciones condicionales
- Reglas de validación complejas

### Justificación
1. **Regla 80/20**: Las acciones estándar cubren la gran mayoría de interacciones de pantalla
2. **JSON más simple**: Las definiciones de acciones permanecen simples y predecibles
3. **Desarrollo más rápido**: No se necesita un registry de acciones complejo en la Fase 1
4. **Ruta de evolución**: Se pueden agregar handlers custom incrementalmente según surjan necesidades de negocio
