# Sprint 5: Persistencia de Sesión Multiplataforma

> **Status**: 📋 PLANIFICADO + ANÁLISIS COMPARATIVO  
> **Fecha de Planificación**: 2026-02-10  
> **Fecha de Análisis**: 2026-02-10  
> **Objetivo**: Implementar persistencia de sesión que permita a usuarios mantener su login entre reinicios de la aplicación

---

## 🚨 IMPORTANTE: Lee Primero la Recomendación

**NUEVO**: Se ha realizado un análisis comparativo de los patrones de Kmp-Common y Template-Kmp-Clean.

### Para tomar decisión rápida:
👉 **LEE PRIMERO**: [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md) (5 minutos)

Este documento presenta:
- Enfoque híbrido recomendado en 3 fases
- Diferencias con el plan original
- Decisión que debe tomarse
- Tiempo estimado: 7-9 días vs 8-13 días original

---

## 📚 Documentación del Sprint

Este sprint contiene **documentación de planificación** y **análisis comparativo**.

### Documentos Disponibles (Orden de Lectura):

#### 1. [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md) ⭐ LEER PRIMERO
**Para**: Product Owner, Tech Lead (toma de decisiones)  
**Contenido**:
- Decisión propuesta: Enfoque híbrido en 3 fases
- Por qué cambiar el plan original
- Comparación Plan Original vs Recomendación
- Arquitectura final recomendada
- Estimación de tiempo por fase
- Checklist de decisión

**Tiempo de lectura**: 5 minutos  
**Acción requerida**: Aprobar enfoque

---

#### 2. [`03-analisis-comparativo.md`](./03-analisis-comparativo.md) 📊 ANÁLISIS TÉCNICO DETALLADO
**Para**: Tech Lead, Arquitecto, Desarrollador Senior  
**Contenido**:
- Comparación exhaustiva de 3 enfoques:
  - Plan original Sprint 5
  - Kmp-Common (proyecto de referencia interno)
  - Template-Kmp-Clean (template de arquitectura limpia)
- Mejores patrones identificados
- Anti-patrones a evitar
- Código de ejemplo completo (Fase 1, 2, 3)
- Decisiones de diseño explicadas
- Testing strategy completa

**Tiempo de lectura**: 45-60 minutos  
**Valor**: Entender profundamente por qué se recomienda el enfoque híbrido

---

#### 3. [`00-resumen-ejecutivo.md`](./00-resumen-ejecutivo.md) 📋 PLAN ORIGINAL
**Para**: Product Owners, Tech Leads, Stakeholders  
**Contenido**: (Plan original antes del análisis)
- Objetivo del sprint
- Situación actual vs. objetivo
- Arquitectura propuesta
- Fases resumidas
- Métricas de éxito
- Estimación de tiempo: 8-13 días
- Criterios de aceptación

**Tiempo de lectura**: 5-10 minutos  
**Nota**: Este plan ha sido mejorado. Ver `04-recomendacion-ejecutiva.md`

---

#### 4. [`01-modulos-actuales.md`](./01-modulos-actuales.md) 🔍 ANÁLISIS DE ESTADO ACTUAL
**Para**: Desarrolladores, Arquitectos  
**Contenido**:
- Análisis detallado de los 12 módulos existentes
- Flujo actual de login (diagramas de secuencia)
- **Gaps identificados**: 8 problemas específicos a resolver
- Arquitectura propuesta original
- Ventajas y consideraciones

**Tiempo de lectura**: 20-30 minutos

---

#### 5. [`02-plan-implementacion.md`](./02-plan-implementacion.md) 🛠️ PLAN ORIGINAL DETALLADO
**Para**: Desarrolladores (el más técnico del plan original)  
**Contenido**: (Plan original antes del análisis)
- 6 Fases con tareas paso a paso
- Código de ejemplo
- Criterios de aceptación granulares
- Tests mínimos requeridos
- Configuración de DI

**Tiempo de lectura**: 45-60 minutos  
**Nota**: Este plan ha sido refinado. Ver `03-analisis-comparativo.md` sección 9

---

## 🎯 Guía de Lectura por Rol

### Si eres Product Owner:
1. ✅ **PRIMERO**: [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md) (5 min)
2. Aprobar o rechazar enfoque propuesto
3. (Opcional) Leer [`00-resumen-ejecutivo.md`](./00-resumen-ejecutivo.md) para contexto del plan original

### Si eres Tech Lead:
1. ✅ **PRIMERO**: [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md) (5 min)
2. ✅ **SEGUNDO**: [`03-analisis-comparativo.md`](./03-analisis-comparativo.md) (45 min)
3. Validar decisiones de diseño
4. Aprobar arquitectura final
5. (Opcional) Comparar con [`00-resumen-ejecutivo.md`](./00-resumen-ejecutivo.md) y [`01-modulos-actuales.md`](./01-modulos-actuales.md)

### Si eres Desarrollador que Implementará:
1. ✅ **PRIMERO**: [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md) (5 min)
2. ✅ **SEGUNDO**: [`03-analisis-comparativo.md`](./03-analisis-comparativo.md) secciones 9-12 (30 min) - Código de ejemplo
3. Revisar checklist de implementación (sección 12)
4. Empezar con Fase 1
5. (Referencia) Consultar [`02-plan-implementacion.md`](./02-plan-implementacion.md) si necesitas más detalle

### Si eres QA / Tester:
1. Leer [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md) sección "Métricas de Éxito"
2. Leer [`03-analisis-comparativo.md`](./03-analisis-comparativo.md) sección 10 (Testing Strategy)
3. Preparar escenarios de test por fase

---

## 📋 Resumen Ultra-Rápido

### ¿Qué problema resolvemos?
Usuarios deben iniciar sesión **cada vez** que abren la app. Mala UX.

### ¿Qué vamos a hacer?
Sistema de persistencia de sesión con:
1. **Guardar sesión** al login
2. **Restaurar sesión** al reabrir app
3. **Manejar expiración** elegantemente
4. **Funciona en todas las plataformas**

### ¿Cómo? (Enfoque Recomendado)

**FASE 1 (3-5 días)**: MVP simple
- SessionManager con persistencia real
- AuthRepository.restoreSession()
- SplashScreen verifica sesión

**FASE 2 (3-5 días)**: Estado reactivo
- AuthState sealed class + extension properties
- StateFlow + Flows de eventos
- UI observa estado reactivamente

**FASE 3 (4-6 días)**: Robustez (opcional)
- SafeEduGoStorage con validación
- logoutWithDetails con soporte offline
- StateFlowStorage para preferencias

### ¿Cuánto tiempo?
- **Fases 1+2 (recomendado para Sprint 5)**: 7-9 días
- **Fase 3 (opcional, puede ir a Sprint 6)**: 4-6 días
- **Total completo**: 11-15 días

---

## 🏗️ Arquitectura Recomendada (Final)

```
┌──────────────────────────────────────────┐
│         UI (Compose Screens)             │
│  Observa authState y eventos             │
└─────────────┬────────────────────────────┘
              │ StateFlow + Flows
┌─────────────▼────────────────────────────┐
│      ViewModel + UseCases                │
│  LoginUseCase, LogoutUseCase             │
└─────────────┬────────────────────────────┘
              │
┌─────────────▼────────────────────────────┐
│         AuthService                      │
│  authState: StateFlow<AuthState>         │
│  onSessionExpired: Flow<Unit>            │
└─────────────┬────────────────────────────┘
              │
┌─────────────▼────────────────────────────┐
│       AuthRepository (Clean Arch)        │
│  Interface en Domain                     │
└─────────────┬────────────────────────────┘
              │
┌─────────────▼────────────────────────────┐
│  SessionManager + SafeEduGoStorage       │
└──────────────────────────────────────────┘
```

**Diferencias clave con plan original**:
- ❌ NO módulo `kmp-session` completo
- ✅ SessionManager simple en `features/auth/data`
- ✅ AuthState con 3 estados (vs 6 original)
- ✅ Flows de eventos separados
- ✅ Clean Architecture completa
- ✅ Validación de storage (Fase 3)

---

## 📊 Comparación: Original vs Recomendado

| Aspecto | Plan Original | Recomendación Híbrida |
|---------|---------------|----------------------|
| **Módulo kmp-session** | Sí (nuevo) | No (SessionManager en data) |
| **Estados** | 6 estados | 3 estados + eventos |
| **Arquitectura** | Session + Auth layers | Clean Architecture |
| **Storage validation** | No | SafeEduGoStorage (Fase 3) |
| **Logout offline** | No especificado | logoutWithDetails (Fase 3) |
| **Tiempo (MVP)** | 8-13 días | 7-9 días (Fases 1+2) |
| **Inspiración** | Desde cero | Kmp-Common + Template-Kmp-Clean |

---

## 🧪 Estado de Tests

### Tests Actuales (Sprint 4):
- **TOTAL**: **1694 tests** ✓

### Tests Post-Sprint 5 (Estimado):

#### Con Recomendación (Fases 1+2):
- **Fase 1**: +30 tests
- **Fase 2**: +40 tests
- **TOTAL Fases 1+2**: **~1764 tests** ✓

#### Si se hace Fase 3:
- **Fase 3**: +40 tests
- **TOTAL Completo**: **~1804 tests** ✓

---

## ✅ Decisión Requerida

### Opciones:

#### Opción A: Enfoque Híbrido (Fases 1+2 en Sprint 5) ⭐ RECOMENDADO
- **Tiempo**: 7-9 días
- **Entregables**: Persistencia + Estado reactivo + Eventos
- **Tests**: ~70 nuevos
- **Fase 3**: Postponer a Sprint 6

#### Opción B: Enfoque Híbrido Completo (Fases 1+2+3 en Sprint 5)
- **Tiempo**: 11-15 días
- **Entregables**: Todo + Validación storage + Logout offline
- **Tests**: ~110 nuevos

#### Opción C: Plan Original
- **Tiempo**: 8-13 días
- **Entregables**: Módulo kmp-session + SessionManager completo
- **Tests**: ~100 nuevos
- **Nota**: No incluye mejoras de Kmp-Common

#### Opción D: Solo MVP (Fase 1)
- **Tiempo**: 3-5 días
- **Entregables**: Persistencia básica funcional
- **Tests**: ~30 nuevos
- **Nota**: Sin estado reactivo, para prototipo rápido

---

## 🚀 Próximos Pasos

### Inmediatos (Hoy/Mañana):
1. **Product Owner + Tech Lead**: Leer `04-recomendacion-ejecutiva.md`
2. **Aprobar enfoque**: Opción A (recomendado), B, C o D
3. **Asignar desarrollador(es)** al sprint
4. **Comunicar decisión** al equipo

### Si se aprueba Opción A (Recomendado):
1. **Día 1**: Desarrollador lee `03-analisis-comparativo.md` secciones 9-12
2. **Día 2**: Kickoff Fase 1, crear branch `feature/auth-persistence-phase1`
3. **Día 3-5**: Implementar Fase 1
4. **Día 6**: Code review + merge
5. **Día 7**: Kickoff Fase 2
6. **Día 8-10**: Implementar Fase 2
7. **Día 11**: QA + merge

---

## 📞 Contacto y Soporte

### ¿Dudas sobre la recomendación?
1. Lee [`04-recomendacion-ejecutiva.md`](./04-recomendacion-ejecutiva.md)
2. Lee [`03-analisis-comparativo.md`](./03-analisis-comparativo.md) secciones relevantes
3. Pregunta a Tech Lead o Claude

### ¿Quieres entender el plan original?
1. Lee [`00-resumen-ejecutivo.md`](./00-resumen-ejecutivo.md)
2. Lee [`01-modulos-actuales.md`](./01-modulos-actuales.md)
3. Lee [`02-plan-implementacion.md`](./02-plan-implementacion.md)

---

## 🏆 Criterios de Éxito

### Must Have (Fases 1+2):
- ✅ Usuario mantiene sesión entre reinicios
- ✅ Estado reactivo con StateFlow
- ✅ Expiración muestra mensaje y navega a Login
- ✅ ~70 tests nuevos passing
- ✅ Performance: `checkSession()` < 500ms

### Nice to Have (Fase 3):
- ✅ Logout funciona offline
- ✅ Storage valida keys
- ✅ ~40 tests adicionales
- ✅ Logs de errores automáticos

---

## 📝 Changelog del Sprint

### 2026-02-10 - Análisis Comparativo
- ✅ Añadido `03-analisis-comparativo.md` - Análisis exhaustivo de 3 enfoques
- ✅ Añadido `04-recomendacion-ejecutiva.md` - Decisión propuesta
- ✅ Identificados patrones de Kmp-Common y Template-Kmp-Clean
- ✅ Propuesto enfoque híbrido en 3 fases
- ✅ Documentado código de ejemplo completo
- ✅ Actualizado README con nueva guía de lectura

### 2026-02-10 - Planificación Inicial
- ✅ Creado `00-resumen-ejecutivo.md`
- ✅ Creado `01-modulos-actuales.md`
- ✅ Creado `02-plan-implementacion.md`
- ✅ Creado README.md

---

## 📦 Estructura de Archivos Post-Sprint

```
kmp_new/
├── features/                        # NUEVO (Clean Architecture)
│   └── auth/
│       ├── domain/
│       │   ├── models/
│       │   │   ├── User.kt
│       │   │   ├── AuthState.kt         # Fase 2
│       │   │   └── LogoutResult.kt      # Fase 3
│       │   ├── repositories/
│       │   │   └── AuthRepository.kt    # Fase 1
│       │   └── usecases/
│       │       ├── LoginUseCase.kt      # Fase 1
│       │       └── RestoreSessionUseCase.kt  # Fase 1
│       ├── data/
│       │   ├── repositories/
│       │   │   └── AuthRepositoryImpl.kt  # Fase 1
│       │   └── services/
│       │       └── SessionManager.kt      # Fase 1
│       └── presentation/
│           └── viewmodels/
│               └── LoginViewModel.kt    # Fase 1
├── modules/
│   ├── auth/                        # MODIFICADO
│   │   └── AuthService.kt           # Fase 2: + StateFlow + eventos
│   ├── storage/                     # MODIFICADO
│   │   ├── EduGoStorage.kt          # Ya existe
│   │   ├── SafeEduGoStorage.kt      # Fase 3: NUEVO
│   │   └── StateFlowStorage.kt      # Fase 3: OPCIONAL
│   └── di/                          # MODIFICADO
│       └── AuthModule.kt            # Actualizar DI
└── kmp-screens/                     # MODIFICADO
    ├── splash/SplashScreen.kt       # Fase 1: Verificar sesión
    ├── login/LoginScreen.kt         # Fase 1: Integrar ViewModel
    ├── home/HomeScreen.kt           # Fase 2: Observar authState
    └── settings/SettingsScreen.kt   # Fase 2: Logout reactivo
```

---

**Happy Coding!** 🚀

---

**Última actualización**: 2026-02-10  
**Versión de Documentación**: 2.0 (con análisis comparativo)  
**Status**: 📋 ANÁLISIS COMPLETO - DECISIÓN PENDIENTE  
**Decisión recomendada**: Opción A (Fases 1+2 en Sprint 5, Fase 3 en Sprint 6)
