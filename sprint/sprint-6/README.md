# Sprint 6: Multi-Environment Config Management

## Resumen Ejecutivo

Este sprint implementa un **sistema profesional de configuración multi-ambiente** que elimina las URLs hardcodeadas del backend y detecta automáticamente el ambiente de ejecución (DEV, STAGING, PROD) en cada plataforma.

### Arquitectura Seleccionada

**Hybrid Runtime Detection + External Config Files**

- Detección automática basada en heurísticas específicas por plataforma
- Archivos de configuración externos en `resources/config/`
- Fallback seguro a configuración hardcodeada para tests
- Override manual para testing (`EnvironmentDetector.override()`)

### Características Clave

- ✅ **Auto-detección de ambiente** en Android, Desktop, iOS, WasmJS
- ✅ **Archivos JSON externos** (`dev.json`, `staging.json`, `prod.json`)
- ✅ **Código reutilizable** con patrón expect/actual
- ✅ **Testing-friendly** con override manual
- ✅ **Zero breaking changes** - Backward compatible al 100%
- ✅ **Cross-platform** - Mismo API en todas las plataformas

---

## Orden de Implementación

### Fase 1: MVP - Detección Automática (COMPLETADA) ✅

**Objetivo**: Eliminar hardcoding de URLs y detectar ambiente automáticamente

**Tiempo estimado**: 4-6 horas

**Entregable**:
- Sistema de detección automática funcional en todas las plataformas
- Archivos de configuración externos
- Tests con >70% cobertura

**Ver**: [FASE-1-MVP.md](./FASE-1-MVP.md)

---

### Fase 2: Build-time Config (OPCIONAL) 🔄

**Objetivo**: Mejorar detección con configuración de build-time

**Tiempo estimado**: 3-4 horas

**Entregable**:
- BuildConfig para Android (gradle buildTypes)
- Info.plist para iOS (Xcode schemes)
- gradle.properties integration
- Detección por hostname en WasmJS

**Ver**: [FASE-2-BUILD-TIME.md](./FASE-2-BUILD-TIME.md)

---

### Fase 3: Remote Config (FUTURO) 📅

**Objetivo**: Configuración dinámica remota

**Tiempo estimado**: 8-12 horas

**Entregable**:
- Firebase Remote Config o API custom
- Feature flags remotos
- A/B testing capabilities

**Estado**: No en scope para este sprint

---

## Tiempo Total Estimado

| Fase | Tiempo | Acumulado | Estado |
|------|--------|-----------|--------|
| Fase 1 - MVP | 4-6 horas | 4-6 horas | ✅ Completada |
| Fase 2 - Build-time | 3-4 horas | 7-10 horas | 🔄 Opcional |
| Fase 3 - Remote Config | 8-12 horas | 15-22 horas | 📅 Futuro |

**Recomendación**: Completar Fase 1 primero, evaluar si Fase 2 es necesaria según feedback.

---

## Criterios de Aceptación Generales

### Funcionalidad

- ✅ Auto-detección funciona en Android, Desktop, iOS, WasmJS
- ✅ Android debug → DEV, release → PROD
- ✅ Desktop con debugger → DEV, sin debugger → PROD
- ✅ iOS → DEV (conservador para evitar llamadas PROD accidentales)
- ✅ WasmJS → DEV (Fase 2 agregará detección por hostname)
- ✅ Override manual funciona: `EnvironmentDetector.override(Environment.STAGING)`
- ✅ Reset funciona: `EnvironmentDetector.reset()`
- ✅ ConfigLoader carga configuración correcta según ambiente
- ✅ URLs NO están hardcodeadas (archivos externos + fallback)
- ✅ AuthModule recibe baseUrl correcta automáticamente

### Calidad

- ✅ Sin breaking changes en código existente
- ✅ Backward compatible al 100%
- ✅ Tests unitarios con >70% cobertura
- ✅ Documentación completa (README, ARQUITECTURA, TESTING)

### Rendimiento

- ✅ Detección de ambiente se ejecuta una sola vez (singleton)
- ✅ Carga de configuración lazy (solo cuando se necesita)
- ✅ Archivos JSON <1KB cada uno (~600 bytes overhead total)

### Seguridad

- ✅ Defaults conservadores (iOS siempre DEV)
- ✅ Validación de valores con `fromStringOrDefault()`
- ✅ Fallback seguro si archivos no existen

---

## Cómo Leer esta Documentación

### Para Implementadores

1. Lee [ARQUITECTURA.md](./ARQUITECTURA.md) para entender el diseño
2. Sigue [FASE-1-MVP.md](./FASE-1-MVP.md) paso a paso
3. Ejecuta tests según [TESTING.md](./TESTING.md)
4. Consulta [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) si encuentras errores
5. Valida con comandos en sección "Validación Rápida"

### Para Revisores

1. Lee "Resumen Ejecutivo" y "Arquitectura Seleccionada"
2. Revisa [ARQUITECTURA.md](./ARQUITECTURA.md) - Diagramas de flujo
3. Verifica criterios de aceptación cumplidos
4. Ejecuta validación rápida (sección abajo)

### Para Product Owners

1. Lee "Características Clave" y "Tiempo Total Estimado"
2. Revisa criterios de aceptación (arriba)
3. Pregunta al equipo sobre progreso de cada fase
4. Prioriza Fase 2 según necesidad de negocio

---

## Estructura de Documentos

```
sprint-6/
├── README.md                  ← Este archivo (punto de entrada)
├── ARQUITECTURA.md            ← Diseño del sistema, diagramas
├── FASE-1-MVP.md              ← Implementación paso a paso
├── FASE-2-BUILD-TIME.md       ← Mejoras opcionales
├── TESTING.md                 ← Estrategia de testing
└── TROUBLESHOOTING.md         ← Solución de problemas comunes
```

---

## Módulos Afectados

- **`modules/config`** - Módulo principal de configuración
  - Agregado: `EnvironmentDetector` (expect/actual)
  - Modificado: `Environment`, `AppConfig`, `ResourceLoader`
  - Agregado: `AndroidContextHolder`

- **`modules/di`** - Inyección de dependencias
  - Modificado: `ConfigModule` (1 línea: usa `EnvironmentDetector.detect()`)

- **`modules/auth`** - Autenticación (consumidor de config)
  - Sin cambios (recibe `baseUrl` automáticamente)

---

## Dependencias Requeridas

### Ya Incluidas

```kotlin
// modules/config/build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
                implementation(project(":modules:core")) // ← Agregado en Sprint 6
            }
        }
    }
}
```

### Nuevas Dependencias Externas

**Ninguna** - Solo usa dependencias existentes del proyecto.

---

## Comandos Útiles

```bash
# Compilar módulo config
./gradlew :modules:config:build

# Ejecutar tests (Desktop - más rápido)
./gradlew :modules:config:desktopTest

# Ejecutar todos los tests
./gradlew :modules:config:allTests

# Verificar que no hay errores de compilación en todas las plataformas
./gradlew :modules:config:compileKotlinAndroid
./gradlew :modules:config:compileKotlinDesktop
./gradlew :modules:config:compileKotlinIosX64
./gradlew :modules:config:compileKotlinWasmJs

# Limpiar y rebuild
./gradlew :modules:config:clean :modules:config:build
```

---

## Validación Rápida

### 1. Compilación

```bash
./gradlew :modules:config:build
# Esperado: BUILD SUCCESSFUL
```

### 2. Tests

```bash
./gradlew :modules:config:desktopTest
# Esperado: BUILD SUCCESSFUL, 12 tests passed
```

### 3. Verificar Auto-detección

```kotlin
// En cualquier parte del código después de KoinApplication
val env = EnvironmentDetector.detect()
println("Ambiente detectado: $env") // DEV, STAGING, o PROD
```

### 4. Verificar ConfigLoader

```kotlin
val config = ConfigLoader.load(EnvironmentDetector.detect())
println("API URL: ${config.getFullApiUrl()}") 
// Esperado: http://localhost:8080 (si estás en DEV)
```

---

## Siguientes Pasos

1. **Completar Fase 1** (si no está completa)
   - Seguir [FASE-1-MVP.md](./FASE-1-MVP.md)
   - Ejecutar todos los tests
   - Validar en cada plataforma

2. **Inicializar AndroidContextHolder** (Android)
   - Editar `MainActivity.kt`
   - Agregar `AndroidContextHolder.init(applicationContext)` en `onCreate()`

3. **Validar en app real**
   - Ejecutar app en Android debug → Verificar que usa DEV
   - Ejecutar app en Desktop → Verificar detección de debugger
   - Ejecutar tests → Verificar override funciona

4. **Decidir si implementar Fase 2**
   - ¿Necesitas diferentes builds con diferentes configs?
   - ¿Quieres separar completamente DEV de PROD en build?
   - Ver [FASE-2-BUILD-TIME.md](./FASE-2-BUILD-TIME.md)

5. **Documentar URLs reales**
   - Actualizar `config/staging.json` con URL real de staging
   - Actualizar `config/prod.json` con URL real de producción
   - Commit y push cambios

6. **Monitorear en producción**
   - Verificar que apps en release usan PROD
   - Agregar logging de ambiente en startup
   - Alertar si se detecta ambiente incorrecto

---

## Soporte

### Si encuentras problemas:

1. Revisa [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
2. Verifica logs de compilación
3. Ejecuta `./gradlew clean build`
4. Consulta sección "Validación Rápida" arriba
5. Contacta al equipo de desarrollo

### Errores Comunes

- **"Unresolved reference Platform"** → Ver TROUBLESHOOTING.md #1
- **"AndroidContextHolder not initialized"** → Ver TROUBLESHOOTING.md #2
- **"Config files not found"** → Ver TROUBLESHOOTING.md #3

---

**Última actualización**: 2026-02-11  
**Versión**: 1.0.0  
**Módulo**: `modules/config`  
**Proyecto**: EduGo KMP
