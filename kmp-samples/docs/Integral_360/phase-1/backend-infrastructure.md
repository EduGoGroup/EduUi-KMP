# Fase 1: Cambios de Infraestructura (edugo-infrastructure)

## Descripción General

Agregar el schema PostgreSQL `ui_config` y todos los archivos de migración relacionados al proyecto de infraestructura. También actualizar el migrador en `edugo-dev-environment` para manejar el nuevo schema.

## Proyecto: `edugo-infrastructure`

### Estructura de Migraciones

El proyecto usa 4 capas de migración embebidas en Go (no up/down):

```
postgres/migrations/
├── structure/    → CREATE TABLE sin FK (embebido, ejecutado por ApplyStructure)
├── constraints/  → FK, índices, triggers (embebido, ejecutado por ApplyConstraints)
├── seeds/        → Datos del sistema: roles, permisos (embebido, ejecutado por ApplySeeds)
└── testing/      → Datos demo para desarrollo (embebido, ejecutado por ApplyMockData)
```

Las migraciones existentes van de 000 a 015 en structure/constraints, 001-004 en seeds, y 001-005 en testing.

---

### 1. Capa Structure (CREATE TABLE sin FK)

#### `structure/016_create_screen_templates.sql`
```sql
-- Schema para configuración de UI
CREATE SCHEMA IF NOT EXISTS ui_config;

-- Templates de pantalla
CREATE TABLE ui_config.screen_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    version INT NOT NULL DEFAULT 1,
    definition JSONB NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(name, version)
);
```

#### `structure/017_create_screen_instances.sql`
```sql
CREATE TABLE ui_config.screen_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_key VARCHAR(100) NOT NULL UNIQUE,
    template_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    slot_data JSONB NOT NULL DEFAULT '{}',
    actions JSONB NOT NULL DEFAULT '[]',
    data_endpoint VARCHAR(500),
    data_config JSONB DEFAULT '{}',
    scope VARCHAR(20) DEFAULT 'school',
    required_permission VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

#### `structure/018_create_resource_screens.sql`
```sql
CREATE TABLE ui_config.resource_screens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID NOT NULL,
    screen_instance_id UUID NOT NULL,
    screen_type VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(resource_id, screen_type)
);
```

#### `structure/019_create_screen_user_preferences.sql`
```sql
CREATE TABLE ui_config.screen_user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_instance_id UUID NOT NULL,
    user_id UUID NOT NULL,
    preferences JSONB NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(screen_instance_id, user_id)
);
```

---

### 2. Capa Constraints (FK, índices, triggers)

#### `constraints/016_screen_templates_constraints.sql`
```sql
-- FK
ALTER TABLE ui_config.screen_templates
    ADD CONSTRAINT fk_screen_templates_created_by
    FOREIGN KEY (created_by) REFERENCES public.users(id);

-- Índices
CREATE INDEX idx_screen_templates_pattern ON ui_config.screen_templates(pattern);
CREATE INDEX idx_screen_templates_active ON ui_config.screen_templates(is_active) WHERE is_active = true;
CREATE INDEX idx_screen_templates_definition ON ui_config.screen_templates USING GIN (definition);

-- Trigger updated_at
CREATE TRIGGER update_screen_templates_updated_at
    BEFORE UPDATE ON ui_config.screen_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

#### `constraints/017_screen_instances_constraints.sql`
```sql
-- FK
ALTER TABLE ui_config.screen_instances
    ADD CONSTRAINT fk_screen_instances_template
    FOREIGN KEY (template_id) REFERENCES ui_config.screen_templates(id);

ALTER TABLE ui_config.screen_instances
    ADD CONSTRAINT fk_screen_instances_created_by
    FOREIGN KEY (created_by) REFERENCES public.users(id);

-- Índices
CREATE INDEX idx_screen_instances_template ON ui_config.screen_instances(template_id);
CREATE INDEX idx_screen_instances_scope ON ui_config.screen_instances(scope);
CREATE INDEX idx_screen_instances_active ON ui_config.screen_instances(is_active) WHERE is_active = true;
CREATE INDEX idx_screen_instances_slot_data ON ui_config.screen_instances USING GIN (slot_data);

-- Trigger updated_at
CREATE TRIGGER update_screen_instances_updated_at
    BEFORE UPDATE ON ui_config.screen_instances
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

#### `constraints/018_resource_screens_constraints.sql`
```sql
-- FK
ALTER TABLE ui_config.resource_screens
    ADD CONSTRAINT fk_resource_screens_resource
    FOREIGN KEY (resource_id) REFERENCES public.resources(id);

ALTER TABLE ui_config.resource_screens
    ADD CONSTRAINT fk_resource_screens_instance
    FOREIGN KEY (screen_instance_id) REFERENCES ui_config.screen_instances(id);

-- Índices
CREATE INDEX idx_resource_screens_resource ON ui_config.resource_screens(resource_id);
CREATE INDEX idx_resource_screens_instance ON ui_config.resource_screens(screen_instance_id);

-- Trigger updated_at
CREATE TRIGGER update_resource_screens_updated_at
    BEFORE UPDATE ON ui_config.resource_screens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

#### `constraints/019_screen_user_preferences_constraints.sql`
```sql
-- FK
ALTER TABLE ui_config.screen_user_preferences
    ADD CONSTRAINT fk_screen_user_prefs_instance
    FOREIGN KEY (screen_instance_id) REFERENCES ui_config.screen_instances(id);

ALTER TABLE ui_config.screen_user_preferences
    ADD CONSTRAINT fk_screen_user_prefs_user
    FOREIGN KEY (user_id) REFERENCES public.users(id);

-- Índices
CREATE INDEX idx_screen_user_prefs_user ON ui_config.screen_user_preferences(user_id);
CREATE INDEX idx_screen_user_prefs_screen ON ui_config.screen_user_preferences(screen_instance_id);

-- Trigger updated_at
CREATE TRIGGER update_screen_user_prefs_updated_at
    BEFORE UPDATE ON ui_config.screen_user_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

### 3. Capa Seeds (datos del sistema)

#### `seeds/005_seed_screen_config_permissions.sql`
```sql
-- Recurso para configuración de pantalla
INSERT INTO public.resources (id, key, display_name, icon, is_menu_visible, scope)
VALUES ('20000000-0000-0000-0000-000000000020', 'screen_config', 'Configuración de Pantallas', 'settings_applications', false, 'system');

-- Permisos de configuración de pantalla
INSERT INTO public.permissions (name, display_name, resource_id, resource_key, action, scope) VALUES
('screen_templates:read', 'Ver Templates de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'read', 'system'),
('screen_templates:create', 'Crear Templates de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'create', 'system'),
('screen_templates:update', 'Actualizar Templates de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'update', 'system'),
('screen_templates:delete', 'Eliminar Templates de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'delete', 'system'),
('screen_instances:read', 'Ver Instancias de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'read', 'system'),
('screen_instances:create', 'Crear Instancias de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'create', 'system'),
('screen_instances:update', 'Actualizar Instancias de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'update', 'system'),
('screen_instances:delete', 'Eliminar Instancias de Pantalla', '20000000-0000-0000-0000-000000000020', 'screen_config', 'delete', 'system'),
('screens:read', 'Leer Pantallas (Mobile)', '20000000-0000-0000-0000-000000000020', 'screen_config', 'read', 'system');

-- Asignar todos los permisos de screen_config al rol super_admin
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM public.roles r, public.permissions p
WHERE r.name = 'super_admin' AND p.resource_key = 'screen_config';
```

#### `seeds/006_seed_screen_templates.sql`
5 templates base para los patterns de la Fase 1:

| Template | Pattern | Descripción |
|----------|---------|-------------|
| `login-basic-v1` | login | Login con marca, formulario, autenticación social |
| `dashboard-basic-v1` | dashboard | Dashboard con saludo, KPIs, actividad, acciones rápidas |
| `list-basic-v1` | list | Lista con búsqueda, filtros, estado vacío, elementos |
| `detail-basic-v1` | detail | Detalle con hero, secciones de contenido, acciones |
| `settings-basic-v1` | settings | Configuración con secciones agrupadas |

Cada template contiene el JSON de `definition` completo con las zones, slots, y controlTypes definidos en [main-screens.md](./main-screens.md).

#### `seeds/007_seed_screen_instances.sql`
6 instancias para las pantallas de la Fase 1:

| Instancia | Screen Key | Template | Data Endpoint |
|-----------|-----------|----------|---------------|
| Login | `app-login` | login-basic-v1 | null |
| Dashboard Profesor | `dashboard-teacher` | dashboard-basic-v1 | `/v1/stats/global` |
| Dashboard Estudiante | `dashboard-student` | dashboard-basic-v1 | personalizado |
| Lista de Materiales | `materials-list` | list-basic-v1 | `/v1/materials` |
| Detalle de Material | `material-detail` | detail-basic-v1 | `/v1/materials/{id}` |
| Configuración | `app-settings` | settings-basic-v1 | null |

Cada instancia incluye `slot_data`, `actions`, `data_config` y `required_permission` completos.

#### `seeds/008_seed_resource_screens.sql`
Mapeos recurso-pantalla que vinculan los recursos RBAC existentes con las instancias de pantalla:

| Recurso | Screen Key | Screen Type |
|---------|-----------|-------------|
| materials | materials-list | list |
| materials | material-detail | detail |
| dashboard | dashboard-teacher | dashboard |
| settings | app-settings | settings |

---

### 4. Capa Testing (datos demo)

#### `testing/006_demo_screen_config.sql`
Datos adicionales de prueba para desarrollo:
- Templates de ejemplo con todos los tipos de control soportados
- Instancias adicionales para probar variantes
- Preferencias de usuario de ejemplo

---

### 5. Actualización del Migrador (edugo-dev-environment)

El migrador en `edugo-dev-environment/migrator/cmd/main.go` necesita actualizarse para manejar el schema `ui_config` en el modo FORCE_MIGRATION:

```go
// En dropPostgresSchema(), agregar antes de DROP public:
_, err := db.Exec("DROP SCHEMA IF EXISTS ui_config CASCADE")
if err != nil {
    return fmt.Errorf("error eliminando schema ui_config: %w", err)
}
```

Esto garantiza que `FORCE_MIGRATION=true` recree limpiamente todas las tablas incluyendo el nuevo schema.

**Nota**: Para desarrollo local, agregar `./edugo-dev-environment/migrator` al `go.work` para que use la versión local de `edugo-infrastructure/postgres` sin necesidad de releases.

---

## Criterios de Aceptación

- [ ] Schema `ui_config` creado exitosamente
- [ ] Las 4 tablas creadas con constraints e índices apropiados
- [ ] Los datos semilla se cargan sin errores (ApplySeeds)
- [ ] Los datos de testing se cargan sin errores (ApplyMockData)
- [ ] Índices GIN en columnas JSONB para rendimiento de consultas
- [ ] Triggers de `updated_at` funcionando en todas las tablas
- [ ] FK referencian correctamente a `public.users` y `public.resources`
- [ ] El migrador con FORCE_MIGRATION=true elimina y recrea el schema `ui_config`
- [ ] La compilación del módulo `postgres` de infrastructure pasa sin errores
