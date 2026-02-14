# Fase 1: Cambios de Infraestructura (edugo-infrastructure)

## Descripcion General

Agregar el schema PostgreSQL `ui_config` y todos los archivos de migracion relacionados al proyecto de infraestructura.

## Proyecto: `edugo-infrastructure`

### 1. Nuevos Archivos de Migracion

Ubicacion: `postgres/migrations/`

#### Migracion 0018: Crear Schema
```sql
-- 0018_create_ui_config_schema.up.sql
CREATE SCHEMA IF NOT EXISTS ui_config;

-- 0018_create_ui_config_schema.down.sql
DROP SCHEMA IF EXISTS ui_config CASCADE;
```

#### Migracion 0019: Tabla de Templates de Pantalla
```sql
-- 0019_create_screen_templates.up.sql
CREATE TABLE ui_config.screen_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    version INT NOT NULL DEFAULT 1,
    definition JSONB NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_by UUID REFERENCES public.users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(name, version)
);

CREATE INDEX idx_screen_templates_pattern ON ui_config.screen_templates(pattern);
CREATE INDEX idx_screen_templates_active ON ui_config.screen_templates(is_active) WHERE is_active = true;
CREATE INDEX idx_screen_templates_definition ON ui_config.screen_templates USING GIN (definition);
```

#### Migracion 0020: Tabla de Instancias de Pantalla
```sql
-- 0020_create_screen_instances.up.sql
CREATE TABLE ui_config.screen_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_key VARCHAR(100) NOT NULL UNIQUE,
    template_id UUID NOT NULL REFERENCES ui_config.screen_templates(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    slot_data JSONB NOT NULL DEFAULT '{}',
    actions JSONB NOT NULL DEFAULT '[]',
    data_endpoint VARCHAR(500),
    data_config JSONB DEFAULT '{}',
    scope VARCHAR(20) DEFAULT 'school',
    required_permission VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_by UUID REFERENCES public.users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_screen_instances_template ON ui_config.screen_instances(template_id);
CREATE INDEX idx_screen_instances_scope ON ui_config.screen_instances(scope);
CREATE INDEX idx_screen_instances_active ON ui_config.screen_instances(is_active) WHERE is_active = true;
CREATE INDEX idx_screen_instances_slot_data ON ui_config.screen_instances USING GIN (slot_data);
```

#### Migracion 0021: Tabla de Asociacion Recurso-Pantalla
```sql
-- 0021_create_resource_screens.up.sql
CREATE TABLE ui_config.resource_screens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID NOT NULL REFERENCES public.resources(id),
    screen_instance_id UUID NOT NULL REFERENCES ui_config.screen_instances(id),
    screen_type VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(resource_id, screen_type)
);

CREATE INDEX idx_resource_screens_resource ON ui_config.resource_screens(resource_id);
```

#### Migracion 0022: Tabla de Preferencias de Usuario
```sql
-- 0022_create_screen_user_preferences.up.sql
CREATE TABLE ui_config.screen_user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_instance_id UUID NOT NULL REFERENCES ui_config.screen_instances(id),
    user_id UUID NOT NULL REFERENCES public.users(id),
    preferences JSONB NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(screen_instance_id, user_id)
);

CREATE INDEX idx_screen_user_prefs_user ON ui_config.screen_user_preferences(user_id);
CREATE INDEX idx_screen_user_prefs_screen ON ui_config.screen_user_preferences(screen_instance_id);
```

### 2. Datos Semilla

#### Migracion 0023: Templates Base de Pantalla
Templates semilla para cada pattern usado en la Fase 1:
- `list-basic-v1` - Lista basica con busqueda, estado vacio, elementos
- `detail-basic-v1` - Detalle con hero, secciones de contenido, acciones
- `dashboard-basic-v1` - Dashboard con saludo, KPIs, actividad, acciones rapidas
- `settings-basic-v1` - Configuracion con secciones agrupadas
- `login-basic-v1` - Login con marca, formulario, autenticacion social

#### Migracion 0024: Instancias de Pantalla para Pantallas Principales
Instancias semilla para las pantallas de la Fase 1:
- `materials-list` → vinculada a `list-basic-v1`
- `material-detail` → vinculada a `detail-basic-v1`
- `dashboard-teacher` → vinculada a `dashboard-basic-v1`
- `dashboard-student` → vinculada a `dashboard-basic-v1`
- `app-settings` → vinculada a `settings-basic-v1`
- `app-login` → vinculada a `login-basic-v1`

### 3. Datos de Prueba

Ubicacion: `postgres/testing/`

Agregar datos de prueba para configuraciones de pantalla que se puedan cargar en desarrollo:
- Templates de ejemplo con todos los tipos de control soportados
- Instancias de ejemplo con datos de slot realistas
- Mapeos de ejemplo recurso-pantalla

### 4. Docker Compose

No se necesitan cambios - el contenedor PostgreSQL existente soporta multiples schemas.

### 5. Actualizaciones del Makefile

Agregar targets:
```makefile
# Ejecutar migraciones de UI config especificamente
migrate-ui-config:
	@$(POSTGRES_CLI) up --schema ui_config

# Cargar datos semilla de UI config
seed-ui-config:
	@$(POSTGRES_CLI) seed --schema ui_config
```

## Criterios de Aceptacion

- [ ] Schema `ui_config` creado exitosamente
- [ ] Las 4 tablas creadas con restricciones e indices apropiados
- [ ] Los datos semilla se cargan sin errores
- [ ] Las migraciones up/down funcionan correctamente
- [ ] Datos de prueba disponibles para desarrollo
- [ ] Indices GIN en columnas JSONB para rendimiento de consultas
