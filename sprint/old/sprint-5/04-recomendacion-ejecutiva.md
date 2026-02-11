# Sprint 5 - Recomendación Ejecutiva

## Decisión Propuesta

**Implementar enfoque HÍBRIDO en 3 fases**, combinando:
- Base simple de **Template-Kmp-Clean**
- Patrones robustos de **Kmp-Common**
- Arquitectura Clean Architecture completa

---

## Por Qué Cambiar el Plan Original

El Sprint 5 planificado tiene buenas ideas pero:

1. **Duplica responsabilidades**: SessionManager + AuthService hacen cosas similares
2. **Complejidad innecesaria**: Crear módulo completo para componente simple
3. **Falta robustez de producción**: No valida keys de storage, manejo de errores básico

---

## Enfoque Recomendado: 3 Fases

### FASE 1: MVP Simple (3-5 días) - DEBE HACERSE

**Qué se implementa**:
- SessionManager con persistencia REAL (no in-memory)
- AuthRepository con `restoreSession()`
- SplashScreen verifica sesión → navega a Home o Login
- UseCases siguiendo Clean Architecture

**Por qué esta fase**:
- Cumple objetivo principal: sesión persiste entre reinicios
- Simple y rápido
- Base sólida para fases siguientes

**Entregables**:
```
Usuario inicia sesión → cierra app → reabre app → va directo a Home
```

---

### FASE 2: Estado Reactivo (3-5 días) - RECOMENDADO

**Qué se implementa**:
- `AuthState` sealed class (3 estados: Authenticated, Unauthenticated, Loading)
- Extension properties poderosas (`authState.currentUser`, `authState.isAuthenticated`)
- Flows de eventos (`onSessionExpired: Flow<Unit>`)
- UI reactiva que observa estado

**Por qué esta fase**:
- UX profesional con loading states
- Manejo elegante de expiración
- Patrón usado en Kmp-Common (probado en producción)

**Entregables**:
```
Token expira → Flow emite → UI muestra "Sesión expirada" → navega a Login
```

---

### FASE 3: Robustez (4-6 días) - OPCIONAL

**Qué se implementa**:
- `SafeEduGoStorage`: Valida keys, nunca crashea, logs automáticos
- `logoutWithDetails`: Soporte offline, mensajes granulares
- `StateFlowStorage`: Preferencias observables (opcional)

**Por qué esta fase**:
- Previene bugs de producción (keys inválidas, storage corrupto)
- Logout funciona sin internet
- Código production-ready

**Entregables**:
```
Usuario sin internet → presiona "Cerrar sesión" → logout local exitoso
```

---

## Diferencias con Plan Original

| Aspecto | Plan Original | Recomendación |
|---------|---------------|---------------|
| **Módulo kmp-session** | Sí (nuevo módulo completo) | No (solo SessionManager en data layer) |
| **Estados de sesión** | 6 estados (Unknown, Loading, LoggedIn, LoggedOut, Expired, Error) | 3 estados (Authenticated, Unauthenticated, Loading) |
| **Manejo de errores** | Estado Error | Result<T> + Flows de eventos |
| **Validación de storage** | No mencionado | SafeEduGoStorage (Fase 3) |
| **Soporte offline** | No especificado | logoutWithDetails con forceLocal |
| **Arquitectura** | Session layer + Auth layer | Clean Architecture (Domain/Data/Presentation) |
| **Tiempo total** | 8-13 días | 10-16 días (más robusto) |

---

## Arquitectura Final

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
│  login/logout/restoreSession             │
└─────────────┬────────────────────────────┘
              │
┌─────────────▼────────────────────────────┐
│       AuthRepository                     │
│  Interface en Domain                     │
│  Impl en Data layer                      │
└─────────────┬────────────────────────────┘
              │
┌─────────────▼────────────────────────────┐
│    SessionManager + Storage              │
│  Persistencia con SafeEduGoStorage       │
└──────────────────────────────────────────┘
```

---

## Mejores Patrones Adoptados

### De Kmp-Common

1. **Extension properties sobre AuthState**:
```kotlin
val user = authState.currentUser  // En vez de casting
if (authState.isAuthenticated) { ... }
```

2. **Flows de eventos**:
```kotlin
LaunchedEffect(Unit) {
    authService.onSessionExpired.collect {
        showSnackbar("Sesión expirada")
        navigateToLogin()
    }
}
```

3. **Pattern matching con fold()**:
```kotlin
val message = authState.fold(
    onAuthenticated = { user, token -> "Bienvenido ${user.name}" },
    onUnauthenticated = { "Inicia sesión" },
    onLoading = { "Cargando..." }
)
```

### De Template-Kmp-Clean

1. **Clean Architecture pura**: Domain/Data/Presentation bien separados
2. **UseCases**: `LoginUseCase`, `LogoutUseCase` encapsulan lógica
3. **Repository en domain**: Interface en domain, implementación en data

---

## Código de Ejemplo: Antes vs Después

### ANTES (Plan Original - Complejo)

```kotlin
// SessionManager en módulo separado
class SessionManager(authService: AuthService) {
    val sessionState: StateFlow<SessionState>
    suspend fun checkSession(): Result<User?>
    suspend fun login(...): Result<User>
    suspend fun logout(): Result<Unit>
}

// AuthService también tiene estado
class AuthService {
    suspend fun restoreSession(): Result<User?>
    suspend fun login(...): Result<AuthResponse>
}

// Duplicación: ¿Quién es la fuente de verdad?
```

### DESPUÉS (Recomendación - Simple)

```kotlin
// SessionManager simple en data layer
class SessionManager(storage: EduGoStorage) {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveUser(user: User)
    fun getUser(): User?
}

// AuthService coordina todo
class AuthServiceImpl(repository: AuthRepository) {
    val authState: StateFlow<AuthState>  // Única fuente de verdad
    val onSessionExpired: Flow<Unit>
    
    suspend fun login(...): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun restoreSession()
}
```

---

## Testing

### Tests por Fase

| Fase | Tests Nuevos | Coverage |
|------|--------------|----------|
| Fase 1: MVP | ~30 | >75% |
| Fase 2: Reactivo | ~40 | >80% |
| Fase 3: Robustez | ~40 | >85% |
| **TOTAL** | **~110** | **>80%** |

---

## Estimación de Tiempo

| Fase | Desarrollo | Tests | QA | Total |
|------|------------|-------|-----|-------|
| **Fase 1** | 2-3 días | 1 día | 0.5 días | **3.5-4.5 días** |
| **Fase 2** | 2-3 días | 1 día | 0.5 días | **3.5-4.5 días** |
| **Fase 3** | 2-3 días | 1-2 días | 1 día | **4-6 días** |
| **TOTAL** | 6-9 días | 3-4 días | 2 días | **11-15 días** |

**Nota**: Fases 1+2 pueden completarse en 1 sprint (2 semanas). Fase 3 puede ir a Sprint 6.

---

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Complejidad mayor que plan original | Media | Bajo | Fase 1 es más simple que plan original |
| Tiempo excede estimación | Media | Medio | Fase 3 es opcional, puede posponerse |
| Equipo no familiarizado con Clean Architecture | Alta | Bajo | Documentación completa con ejemplos |
| Tests toman más tiempo | Baja | Bajo | Tests por fase, incrementales |

---

## Métricas de Éxito

### Must Have (Fase 1)
- [ ] Usuario mantiene sesión entre reinicios
- [ ] SplashScreen navega correctamente
- [ ] Logout limpia sesión
- [ ] Mínimo 30 tests passing

### Should Have (Fase 2)
- [ ] Estado reactivo con StateFlow
- [ ] Expiración muestra mensaje
- [ ] UI observa authState
- [ ] Mínimo 70 tests total

### Nice to Have (Fase 3)
- [ ] Logout funciona offline
- [ ] Storage valida keys
- [ ] Logs de errores automáticos
- [ ] Mínimo 110 tests total

---

## Decisión Requerida

### Opción A: Implementar Recomendación Híbrida (RECOMENDADO)

**Pros**:
- Combina lo mejor de ambos proyectos
- Base simple que crece incrementalmente
- Patrones probados en producción
- Arquitectura más limpia

**Contras**:
- 2-3 días más que plan original (si se hace Fase 3)
- Requiere entender dos proyectos de referencia

**Tiempo**: 11-15 días (Fases 1+2+3) o 7-9 días (solo Fases 1+2)

---

### Opción B: Seguir Plan Original Sprint 5

**Pros**:
- Ya está documentado
- Equipo ya lo leyó

**Contras**:
- Duplicación SessionManager + AuthService
- Módulo completo para componente simple
- Falta validación de storage
- Falta soporte offline en logout

**Tiempo**: 8-13 días

---

### Opción C: Solo MVP (Fase 1)

**Pros**:
- Más rápido (3-5 días)
- Cumple objetivo mínimo

**Contras**:
- Sin estado reactivo
- Sin manejo elegante de expiración
- Código menos profesional

**Tiempo**: 3-5 días

---

## Recomendación Final

### RECOMENDADO: Opción A (Fases 1+2 en Sprint 5, Fase 3 en Sprint 6)

**Razones**:

1. **Cumple objetivo**: Sesión persiste (Fase 1)
2. **UX profesional**: Estado reactivo y eventos (Fase 2)
3. **Flexible**: Fase 3 puede posponerse
4. **Arquitectura limpia**: Clean Architecture completa
5. **Patrones probados**: De Kmp-Common y Template-Kmp-Clean
6. **Testeable**: 70 tests en Fases 1+2

**Tiempo**: 7-9 días para Fases 1+2 (cabe en 1 sprint de 2 semanas)

---

## Checklist de Decisión

### Para Product Owner

- [ ] Leer sección "Enfoque Recomendado: 3 Fases"
- [ ] Leer "Métricas de Éxito"
- [ ] Decidir: ¿Fases 1+2 en Sprint 5, Fase 3 en Sprint 6?
- [ ] Aprobar tiempo estimado: 7-9 días

### Para Tech Lead

- [ ] Leer documento completo `/sprint/sprint-5/03-analisis-comparativo.md`
- [ ] Revisar "Arquitectura Final Recomendada"
- [ ] Validar patrones adoptados
- [ ] Asignar desarrollador a Fase 1

### Para Desarrollador

- [ ] Leer `03-analisis-comparativo.md` secciones 9-10 (Código de ejemplo)
- [ ] Entender diferencias con plan original
- [ ] Revisar checklist de implementación (sección 12)
- [ ] Hacer preguntas si algo no está claro

---

## Próximos Pasos Inmediatos

1. **Hoy**: Product Owner + Tech Lead revisan y aprueban enfoque
2. **Mañana**: Desarrollador lee documentación completa
3. **Día 2**: Kickoff de Fase 1, crear branch `feature/auth-persistence-phase1`
4. **Día 3-5**: Implementar Fase 1
5. **Día 6**: Code review + merge Fase 1
6. **Día 7**: Kickoff de Fase 2
7. **Día 8-10**: Implementar Fase 2
8. **Día 11**: QA final + merge

---

## Contacto para Dudas

- **Documentación completa**: `/sprint/sprint-5/03-analisis-comparativo.md`
- **Plan original**: `/sprint/sprint-5/02-plan-implementacion.md`
- **Ejemplos de código**: Sección 9 del documento comparativo

---

**Última actualización**: 2026-02-10  
**Para**: Product Owner, Tech Lead  
**Decisión requerida**: Aprobar Opción A (Recomendado)  
**Tiempo estimado**: 7-9 días (Fases 1+2)
