# EduGo KMP - Sprints

Este directorio contiene la planificación y documentación de todos los sprints del proyecto EduGo KMP Multiplatform.

---

## 📊 Resumen de Sprints

| Sprint | Nombre | Status | Módulos | Tests | Duración |
|--------|--------|--------|---------|-------|----------|
| [Sprint 1](../MEMORY.md#sprint-1-status-completed) | Foundation & Core | ✅ COMPLETADO | foundation, core, logger, validation | 1032 | ~2 semanas |
| [Sprint 2](../MEMORY.md#sprint-2-status-completed) | Network & Storage | ✅ COMPLETADO | network, storage, config | +437 (1469 total) | ~2 semanas |
| [Sprint 3](../MEMORY.md#sprint-3-status-completed) | Auth & DI | ✅ COMPLETADO | auth, di | +191 (1660 total) | ~2 semanas |
| [Sprint 4](../MEMORY.md#sprint-4-status-completed) | UI & Design System | ✅ COMPLETADO | kmp-design, kmp-resources, kmp-screens, platforms | +34 (1694 total) | ~2 semanas |
| [Sprint 5](./sprint-5/) | Session Persistence | 📋 PLANIFICADO | kmp-session, kmp-security (opcional) | +100-150 (~1800 total) | 8-18 días |
| Sprint 6 | TBD | 🔜 PRÓXIMO | TBD | TBD | TBD |

---

## 📈 Evolución del Proyecto

### Tests por Sprint:
```
Sprint 1:  1,032 tests ✓ (foundation, core, logger, validation)
Sprint 2:  1,469 tests ✓ (+437: network, storage, config)
Sprint 3:  1,660 tests ✓ (+191: auth, di)
Sprint 4:  1,694 tests ✓ (+34: kmp-design, kmp-resources, kmp-screens)
Sprint 5:  ~1,800 tests (estimado, +100-150: kmp-session, kmp-security)
```

### Módulos por Sprint:
```
Sprint 1:  4 módulos (foundation, core, logger, validation)
Sprint 2:  7 módulos (+3: network, storage, config)
Sprint 3:  9 módulos (+2: auth, di)
Sprint 4: 12 módulos (+3: kmp-design, kmp-resources, kmp-screens)
Sprint 5: 13-14 módulos (+1-2: kmp-session, kmp-security opcional)
```

---

## 🎯 Sprints Completados

### Sprint 1: Foundation & Core ✅
**Objetivo**: Establecer la base del proyecto con tipos fundamentales y utilidades compartidas.

**Módulos creados**:
- `foundation` - Result monad, AppError, JSON utils, Dispatchers
- `core` - Platform info, annotations, synchronization
- `logger` - Logging system con Kermit
- `validation` - Email, UUID, password validators

**Logros**:
- ✅ 1,032 tests passing
- ✅ 4 plataformas soportadas (Android, Desktop, WasmJs, iOS on-demand)
- ✅ Convention plugins configurados
- ✅ Arquitectura limpia establecida

---

### Sprint 2: Network & Storage ✅
**Objetivo**: Implementar capas de comunicación y persistencia multiplataforma.

**Módulos creados**:
- `network` - HTTP client (Ktor), interceptors, retry logic, TokenProvider
- `storage` - Key-value storage (multiplatform-settings) con 3 niveles (sync/async/reactive)
- `config` - Environment config (DEV/STAGING/PROD)

**Logros**:
- ✅ +437 tests (1,469 total)
- ✅ Ktor 3.1.3 integrado
- ✅ Platform-specific engines (OkHttp, CIO, Darwin, Ktor-JS)
- ✅ Storage reactivo con StateFlow

---

### Sprint 3: Auth & DI ✅
**Objetivo**: Sistema completo de autenticación y gestión de dependencias.

**Módulos creados**:
- `auth` - AuthService, TokenRefreshManager, JWT parsing, Authorization
- `di` - Koin 4.1.0 modules para todos los módulos

**Logros**:
- ✅ +191 tests (1,660 total)
- ✅ Login/Logout/Refresh funcional
- ✅ Token refresh automático con Mutex
- ✅ Roles y permisos genéricos
- ✅ DI configurado para todo el proyecto

---

### Sprint 4: UI & Design System ✅
**Objetivo**: Crear sistema de diseño y pantallas base con Compose Multiplatform.

**Módulos creados**:
- `kmp-design` - Design system (tokens, theme, semantic colors, components)
- `kmp-resources` - Strings multiplatform (expect/actual)
- `kmp-screens` - Navigation y 4 pantallas (Splash, Login, Home, Settings)
- Platforms: Android (MainActivity), Desktop (Main.kt), WASM (index.html), iOS (Xcode project)

**Logros**:
- ✅ +34 tests (1,694 total)
- ✅ Compose Multiplatform funcionando en 4 plataformas
- ✅ Sistema de diseño coherente
- ✅ Navegación con backstack
- ✅ 4 pantallas funcionales (sin lógica de negocio aún)

---

## 📋 Sprints Planificados

### Sprint 5: Session Persistence 📋
**Objetivo**: Implementar persistencia de sesión para mantener usuario logueado entre reinicios.

**Documentación**: [`sprint-5/README.md`](./sprint-5/README.md)

**Módulos a crear**:
- `kmp-session` - SessionManager, SessionState, coordinación UI-Auth
- `kmp-security` (opcional) - Secure Storage con encriptación platform-specific

**Modificaciones**:
- `auth` - Añadir `restoreSession()`, guardar User en storage
- `kmp-screens` - Integrar SessionManager en Splash/Login/Settings/Home
- `di` - Añadir `sessionModule`

**Objetivos**:
- ✅ Usuario mantiene sesión entre reinicios
- ✅ Verificación automática en SplashScreen
- ✅ Manejo de expiración elegante
- ✅ +100-150 tests (~1,800 total)

**Duración estimada**: 8-18 días (MVP: 8-13 días sin secure storage)

**Documentos**:
- [`00-resumen-ejecutivo.md`](./sprint-5/00-resumen-ejecutivo.md) - Resumen para stakeholders
- [`01-modulos-actuales.md`](./sprint-5/01-modulos-actuales.md) - Análisis de gaps
- [`02-plan-implementacion.md`](./sprint-5/02-plan-implementacion.md) - Fases detalladas

---

### Sprint 6: TBD 🔜
**Ideas**:
- Completar Secure Storage (si no se hizo en Sprint 5)
- Token rotation y refresh token expiry
- Session analytics
- Error boundary y crash reporting

---

## 🏗️ Arquitectura Evolutiva

### Sprint 1-3: Backend Foundation
```
foundation (Result, AppError)
    ↓
core (Platform, Sync)
    ↓
logger (EduGoLogger) + validation (Validators)
    ↓
network (HTTP) + storage (K-V) + config (Env)
    ↓
auth (Login/Logout/Refresh)
    ↓
di (Koin modules)
```

### Sprint 4: UI Layer
```
kmp-design (Theme, Components)
    ↓
kmp-resources (Strings)
    ↓
kmp-screens (Navigation, Pantallas)
    ↓
platforms (Android, Desktop, Wasm, iOS)
```

### Sprint 5: Session Management
```
kmp-session (SessionManager, SessionState)
    ↓
Integra: kmp-screens + auth + storage
    ↓
+ kmp-security (Secure Storage) [opcional]
```

---

## 📚 Documentación General

### Archivos Principales:
- [`/CLAUDE.md`](../CLAUDE.md) - Guía para Claude Code (arquitectura, convenciones)
- [`/MEMORY.md`](../.claude/memory/MEMORY.md) - Historia del proyecto, patrones, gotchas
- [`/README.md`](../README.md) - README del proyecto
- [`/docs/`](../docs/) - Documentación técnica detallada

### Convenciones:
- [`/build-logic/`](../build-logic/) - Convention plugins (kmp.android, kmp.logic.core, kmp.ui.full)
- [`/gradle/libs.versions.toml`](../gradle/libs.versions.toml) - Version catalog

---

## 🎯 Roadmap de Alto Nivel

### Q1 2026 (Sprint 1-4) ✅
- ✅ Foundation completa
- ✅ Network y Storage
- ✅ Auth y DI
- ✅ UI básico

### Q2 2026 (Sprint 5-8)
- 📋 Session Persistence (Sprint 5)
- 🔜 Security Hardening (Sprint 6)
- 🔜 Biometric Auth (Sprint 7)
- 🔜 SSO / OAuth (Sprint 8)

### Q3 2026 (Sprint 9-12)
- Features avanzadas
- Performance optimization
- Analytics e instrumentación
- A/B testing framework

---

## 📏 Métricas del Proyecto

### Tests:
- **Total actual**: 1,694 tests ✓
- **Promedio por sprint**: ~400 tests
- **Coverage target**: >80%

### Módulos:
- **Total actual**: 12 módulos
- **Promedio por sprint**: 2-3 módulos
- **Targets**: Android + Desktop + WasmJs + iOS (on-demand)

### Código:
- **Lenguaje**: Kotlin 2.1.20
- **Paradigma**: Multiplatform (expect/actual)
- **Arquitectura**: Clean Architecture + Repository Pattern

---

## 🚀 Cómo Usar Esta Documentación

### Para Product Owners:
1. Lee la tabla de resumen de sprints
2. Revisa "Objetivos" de cada sprint
3. Para sprint actual, lee `sprint-X/00-resumen-ejecutivo.md`

### Para Tech Leads:
1. Lee todos los `README.md` de sprints
2. Revisa arquitectura evolutiva
3. Para planificar nuevo sprint, usa Sprint 5 como template

### Para Desarrolladores:
1. Lee `sprint-X/README.md` del sprint actual
2. Sigue `sprint-X/02-plan-implementacion.md` paso a paso
3. Escribe tests según `02-plan-implementacion.md`
4. Actualiza `MEMORY.md` al completar

### Para QA:
1. Lee sección "Fase 6: Testing & QA" de cada sprint
2. Ejecuta `./gradlew test` antes y después
3. QA manual en 4 plataformas
4. Reporta en sprint retrospective

---

## 📞 Soporte

### ¿Dudas sobre un sprint?
- Lee primero el `README.md` del sprint
- Si es planificado, lee `00-resumen-ejecutivo.md`
- Si es técnico, lee `02-plan-implementacion.md`

### ¿Quieres crear un nuevo sprint?
1. Copia estructura de `sprint-5/`
2. Crea: `README.md`, `00-resumen-ejecutivo.md`, `01-modulos-actuales.md`, `02-plan-implementacion.md`
3. Actualiza este archivo (`sprint/README.md`) con nueva entrada

---

**Última actualización**: 2026-02-10  
**Sprint actual**: 5 (planificado)  
**Versión de proyecto**: 0.5.0 (pre-release)
