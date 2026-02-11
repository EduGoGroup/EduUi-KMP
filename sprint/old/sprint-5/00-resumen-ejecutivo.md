# Sprint 5 - Resumen Ejecutivo: Persistencia de Sesión

## 🎯 Objetivo del Sprint

Implementar un sistema completo de **persistencia de sesión multiplataforma** que permita a los usuarios mantener su sesión activa entre reinicios de la aplicación, siguiendo los principios de **arquitectura limpia** y **separación de responsabilidades**.

---

## 📊 Situación Actual vs. Objetivo

### ✅ Lo que ya tenemos (Sprint 4):
- **AuthService** funcional con login/logout/refresh
- **Storage multiplataforma** (SharedPreferences, NSUserDefaults, localStorage, etc.)
- **Token refresh automático** con manejo de 401
- **UI estructurada** con navegación (Splash, Login, Home, Settings)
- **DI completo** con Koin

### ❌ Lo que falta:
- **No hay verificación de sesión** al iniciar la app
- **SplashScreen siempre va a Login**, incluso si hay sesión activa
- **No hay gestión de estado de sesión** global (StateFlow)
- **Tokens guardados no se validan** al restaurar sesión
- **No hay manejo UI** de expiración de sesión

### 🎯 Objetivo:
1. Usuario inicia sesión → sesión persiste
2. Usuario cierra y reabre app → **va directo a Home** (sin re-login)
3. Si tokens expiran → muestra mensaje y redirige a Login
4. Todo funciona en **Android + Desktop + Wasm + iOS**

---

## 🏗️ Arquitectura Propuesta

### Nuevo Módulo: `kmp-session`

```
┌──────────────────────────────────────────────────┐
│              UI Layer (kmp-screens)              │
│  • SplashScreen observa sessionState             │
│  • LoginScreen llama SessionManager.login()      │
│  • SettingsScreen llama SessionManager.logout()  │
└──────────────┬───────────────────────────────────┘
               │ observes StateFlow<SessionState>
┌──────────────▼───────────────────────────────────┐
│   NUEVO: kmp-session (Session Management)       │
│  • SessionManager: Coordinador de sesión         │
│  • SessionState: Unknown/Loading/LoggedIn/       │
│                  LoggedOut/Expired/Error         │
│  • Provee sessionState: StateFlow                │
└──────────────┬───────────────────────────────────┘
               │ uses
┌──────────────▼───────────────────────────────────┐
│          auth module (Business Logic)            │
│  • AuthService.restoreSession() (NUEVO)          │
│  • AuthService.login/logout/refresh              │
│  • Valida tokens, intenta refresh si expirado    │
└──────────────┬───────────────────────────────────┘
               │ uses
┌──────────────▼───────────────────────────────────┐
│       storage + network (Data Layer)             │
│  • EduGoStorage: Guarda tokens + user data       │
│  • EduGoHttpClient: Refresh automático           │
└──────────────────────────────────────────────────┘
```

### Responsabilidades por Capa:

| Capa | Responsabilidad | Ejemplo |
|------|-----------------|---------|
| **UI** | Observar estado, disparar acciones | `sessionManager.login(email, pass)` |
| **SessionManager** | Gestionar estado global, coordinar | Emitir `SessionState.LoggedIn(user)` |
| **AuthService** | Lógica de negocio pura | Validar token, refresh, deserializar user |
| **Storage** | Persistencia sin lógica | `putString(KEY_TOKEN, token)` |

---

## 📋 Fases de Implementación

### **FASE 1: Session State Management** ⭐ CRÍTICO (2-3 días)
**Qué se hace**:
- Crear módulo `kmp-session`
- Implementar `SessionState` sealed class (Unknown/Loading/LoggedIn/LoggedOut/Expired)
- Implementar `SessionManager` con `StateFlow<SessionState>`
- Añadir `sessionModule` a DI

**Resultado**: 
- ✅ Componente central que coordina sesión
- ✅ Estado observable desde UI
- ✅ ~20 tests

---

### **FASE 2: Session Restoration Logic** ⭐ CRÍTICO (2-3 días)
**Qué se hace**:
- Añadir `AuthService.restoreSession()` que:
  1. Lee tokens de storage
  2. Valida access token
  3. Si expirado, intenta refresh
  4. Si refresh falla, limpia sesión
  5. Deserializa y retorna User
- Modificar `AuthService.login()` para guardar User en storage

**Resultado**:
- ✅ Lógica de restauración robusta
- ✅ Validación de tokens
- ✅ ~10 tests nuevos

---

### **FASE 3: UI Integration** ⭐ CRÍTICO (1-2 días)
**Qué se hace**:
- **SplashScreen**: Llamar `SessionManager.checkSession()` y navegar según resultado
- **LoginScreen**: Usar `SessionManager.login()` y navegar a Home si exitoso
- **SettingsScreen**: Implementar logout funcional
- **HomeScreen**: Mostrar info de usuario actual

**Resultado**:
- ✅ Flujo completo funcional
- ✅ Navegación automática según estado
- ✅ ~5 tests de integración

---

### **FASE 4: Session Expiration Handling** 🔶 IMPORTANTE (1-2 días)
**Qué se hace**:
- SessionManager detecta tokens expirados y emite `SessionState.Expired`
- UI muestra Snackbar "Sesión expirada"
- Navegación automática a Login

**Resultado**:
- ✅ UX mejorada en expiración
- ✅ No crashes por sesión inválida

---

### **FASE 5: Secure Storage** 🔵 OPCIONAL (3-5 días)
**Qué se hace**:
- Crear módulo `kmp-security`
- Implementar encriptación platform-specific:
  - Android: EncryptedSharedPreferences
  - iOS: Keychain
  - Desktop: OS keyring
  - Wasm: Web Crypto API
- Integrar en AuthService

**Resultado**:
- ✅ Tokens encriptados (mayor seguridad)
- ⚠️ Puede dejarse para Sprint 6

---

### **FASE 6: Testing & QA** ⭐ CRÍTICO (2-3 días)
**Qué se hace**:
- Tests E2E de flujos completos
- QA manual en Android + Desktop + Wasm + iOS
- Performance testing (`checkSession()` < 500ms)
- Security testing (tokens no en logs)

**Resultado**:
- ✅ ~100 nuevos tests totales
- ✅ QA passed en todas las plataformas

---

## 📈 Métricas de Éxito

### Funcionales:
- ✅ **Persistencia**: Usuario reabre app y está logueado
- ✅ **Expiración**: Sesión expirada muestra mensaje y redirige
- ✅ **Logout**: Limpia sesión completamente
- ✅ **Multiplataforma**: Funciona en Android/Desktop/Wasm/iOS

### Técnicas:
- ✅ **Tests**: +100 tests nuevos (1694 → ~1800)
- ✅ **Coverage**: >80% en módulos nuevos
- ✅ **Performance**: `checkSession()` < 500ms
- ✅ **Arquitectura**: Separación clara de capas

### No Funcionales:
- ✅ **UX**: Loading states claros, errores informativos
- ✅ **Security**: Tokens no en logs (encriptación opcional)
- ✅ **Mantenibilidad**: Código modular y documentado

---

## ⏱️ Estimación de Tiempo

| Fase | Duración |
|------|----------|
| **Fase 1**: Session State Management | 2-3 días |
| **Fase 2**: Session Restoration | 2-3 días |
| **Fase 3**: UI Integration | 1-2 días |
| **Fase 4**: Expiration Handling | 1-2 días |
| **Fase 5**: Secure Storage (OPCIONAL) | 3-5 días |
| **Fase 6**: Testing & QA | 2-3 días |
| **TOTAL (sin Secure Storage)** | **8-13 días** |
| **TOTAL (con Secure Storage)** | **11-18 días** |

**Recomendación**: Hacer Fases 1-4 + 6 primero (MVP). Dejar Fase 5 para Sprint 6.

---

## 🎯 Criterios de Aceptación Globales

### Para considerar el sprint COMPLETADO:

#### Funcionales:
- [ ] Usuario puede iniciar sesión y la sesión persiste entre reinicios
- [ ] Al reabrir la app con sesión activa, va directo a Home (no a Login)
- [ ] Usuario puede cerrar sesión y volver a Login
- [ ] Si tokens expiran, usuario ve mensaje claro y va a Login
- [ ] Funciona en **Android + Desktop + Wasm** (iOS opcional)

#### Técnicos:
- [ ] Módulo `kmp-session` creado con SessionManager
- [ ] `AuthService.restoreSession()` implementado
- [ ] UI integrada (Splash/Login/Settings/Home)
- [ ] **Mínimo 100 nuevos tests** con >80% coverage
- [ ] DI configurado con `sessionModule`
- [ ] Sin memory leaks ni race conditions

#### No Funcionales:
- [ ] `checkSession()` toma menos de 500ms
- [ ] Tokens no aparecen en logs de producción
- [ ] Loading states claros en UI
- [ ] Errores con mensajes informativos
- [ ] Código documentado (KDoc en APIs públicas)

---

## 🚧 Riesgos Identificados

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Token refresh race conditions | Baja | Alto | ✅ Ya mitigado con Mutex |
| Storage corruption en Wasm | Media | Medio | Try-catch + fallback a logout |
| Secure Storage complejo en iOS | Alta | Bajo | Dejar para Sprint 6 |
| Tests flaky en Wasm | Media | Medio | Aumentar timeouts, mockar storage |

---

## 📦 Entregables del Sprint

1. **Código**:
   - Módulo `kmp-session/` funcional
   - Modificaciones en `auth/`, `kmp-screens/`, `di/`
   - (Opcional) Módulo `kmp-security/`

2. **Tests**:
   - ~50-70 tests en `kmp-session`
   - ~10 tests en `auth`
   - ~5 tests E2E en `kmp-screens`
   - ~40-60 tests en `kmp-security` (si se implementa)

3. **Documentación**:
   - ✅ `01-modulos-actuales.md` - Análisis de estado actual
   - ✅ `02-plan-implementacion.md` - Fases detalladas
   - ✅ `00-resumen-ejecutivo.md` - Este documento
   - (Crear) KDoc en APIs públicas de SessionManager

4. **QA**:
   - Reporte de QA manual en 4 plataformas
   - Screenshots de flujos funcionando
   - Métricas de performance

---

## 🔄 Próximos Sprints (Roadmap)

### Sprint 6: Security Hardening
- Implementar Secure Storage completo
- Rotación de refresh tokens
- Rate limiting en login

### Sprint 7: Biometric Authentication
- Wrapper multiplataforma para biométricos
- Integración con SessionManager
- Fallback a password

### Sprint 8: SSO / OAuth
- Soporte para Google/Apple/Microsoft login
- OAuth flow multiplataforma
- Token exchange

### Sprint 9: Session Analytics
- Tracking de eventos de sesión
- Métricas de engagement
- Alertas de seguridad

---

## 📚 Documentos Relacionados

- **`01-modulos-actuales.md`**: Análisis detallado de módulos existentes y gaps
- **`02-plan-implementacion.md`**: Plan fase por fase con código de ejemplo
- **`/modules/auth/README.md`**: Documentación de AuthService
- **`/modules/storage/README.md`**: Documentación de EduGoStorage
- **`MEMORY.md`**: Historial de sprints anteriores

---

## ✅ Checklist Pre-Sprint

Antes de empezar, verificar:
- [ ] Backend de auth está funcional (`/auth/login`, `/auth/refresh`, `/auth/logout`)
- [ ] Tokens JWT tienen claims estándar (`sub`, `exp`, `roles`)
- [ ] Equipo alineado en arquitectura propuesta
- [ ] Prioridades claras (Fases 1-4+6 críticas, Fase 5 opcional)
- [ ] Ambiente de QA listo para Android/Desktop/Wasm/iOS
- [ ] Sprint 4 completado (1694 tests passing)

---

## 🎉 Valor de Negocio

### Para el Usuario:
- ✅ **Conveniencia**: No necesita re-login cada vez
- ✅ **Seguridad**: Sesión expira automáticamente si inactiva
- ✅ **Transparencia**: Mensajes claros de estado de sesión

### Para el Equipo:
- ✅ **Arquitectura limpia**: Fácil de mantener y extender
- ✅ **Reusabilidad**: SessionManager se puede usar en otros proyectos
- ✅ **Escalabilidad**: Base sólida para biométricos, SSO, etc.
- ✅ **Multiplataforma**: Un código, 4 plataformas

---

**Última actualización**: 2026-02-10  
**Sprint**: 5  
**Versión**: 1.0  
**Status**: 📋 PLANIFICADO (No implementado)
