# Plan de Correccion - Dynamic UI Navigation & Menu

**Estado: COMPLETADO** (2026-02-20)

---

## Bugs Identificados y Resueltos

### Bug 1: API Mobile - Schema mismatch en resources (CRITICO) - RESUELTO
- **Archivo:** `edugo-api-mobile/internal/infrastructure/persistence/postgres/repository/resource_repository.go:26`
- **Problema:** Query usa `FROM auth.resources` pero la tabla esta en `public.resources` (no existe esquema `auth`)
- **Efecto:** `GET /v1/screens/navigation` fallaba con `{"error":"database error during get menu resources","code":"DATABASE_ERROR"}`
- **Fix aplicado:** Cambiar `auth.resources` -> `resources`
- **Rama:** `fix/navigation-schema` en edugo-api-mobile (desde dev)
- **Commit:** `4828cc0 fix: remove auth schema prefix from resources query`

### Bug 2: API Admin - Permissions no se extraen del JWT (MEDIO) - RESUELTO
- **Archivo afectado:** `edugo-api-administracion/internal/infrastructure/http/handler/menu_handler.go`
- **Problema:** El handler usaba `c.Get("permissions")` pero el middleware JWT solo setea `user_id`, `email`, `role`, `jwt_claims`
- **Efecto:** `GET /v1/menu` retornaba `{"items":[]}` para todos los usuarios
- **Fix aplicado:** Cambiar handler para usar `ginmiddleware.GetClaims(c)` y extraer `claims.ActiveContext.Permissions`
- **Decision arquitectonica:** Se modifico el handler (no el middleware compartido) para no afectar otros servicios
- **Rama:** `fix/menu-permissions` en edugo-api-administracion (desde dev)
- **Commit:** `e1cbafc fix: extract menu permissions from JWT claims instead of missing context key`

## Resultados de Testing (contra Neon)

### Antes del fix:
| Endpoint | Resultado |
|----------|-----------|
| `GET /v1/menu` | `{"items":[]}` (vacio) |
| `GET /v1/screens/navigation?platform=desktop` | `{"error":"database error","code":"DATABASE_ERROR"}` |

### Despues del fix:
| Endpoint | Resultado |
|----------|-----------|
| `GET /v1/menu` | 4 secciones (Admin, Academico, Contenido, Reportes) con 8 hijos y 29 screens |
| `GET /v1/screens/navigation?platform=desktop` | NavigationConfigDTO con drawerItems poblados |

## Hallazgo adicional: Filtrado de navegacion incompleto

La navegacion funciona pero el filtrado es mas agresivo de lo esperado:
- Solo muestra resources donde el usuario tiene permiso `{key}:read`
- Resources como `dashboard`, `admin`, `academic`, `content`, `reports` no tienen permisos `{key}:read` directos
- El menu full tiene 5 secciones raiz + 10 hijos, pero la navegacion solo muestra un subconjunto

**Esto es un tema pre-existente** en la logica del servicio de navegacion (`screen_service.go`), no de los bugs que corregimos. Requiere revision futura del filtrado de permisos para la navegacion.

## Infraestructura configurada

1. **psql instalado:** `/opt/homebrew/opt/libpq/bin/psql` (agregado a PATH en ~/.zshrc)
2. **Credenciales centralizadas:** `/Users/jhoanmedina/source/EduGo/repos-separados/.db-credentials.env`
3. **Memoria actualizada:** MEMORY.md con datos de DB, APIs, y procedimientos

## Proximos pasos sugeridos

1. Hacer PR de ambas ramas a `dev`
2. Revisar logica de filtrado de navegacion en `screen_service.go`
3. Verificar que el KMP Desktop client funcione con la navegacion real (no fallback)
4. Repetir analisis con otros roles (teacher, student, school_admin)
