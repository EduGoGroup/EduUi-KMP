# Decisión Arquitectónica: EduGoStorage Factory vs PlatformModule DI

**Fecha:** 9 de febrero de 2026  
**Decisión:** Usar EduGoStorage.create() factory method en lugar de PlatformModule expect/actual  
**Status:** Aprobada e implementada  
**Autor:** Equipo EduGo KMP

---

## Contexto

Durante el Sprint 3, la especificación original (SPRINT-3-DETALLE.md) planteaba implementar un `PlatformModule` con expect/actual para proveer dependencias específicas de plataforma via Koin DI:

```kotlin
// Especificación original:
// di/src/commonMain/kotlin/com/edugo/kmp/di/PlatformModule.kt (expect)
expect fun platformModule(): Module

// di/src/androidMain/kotlin/com/edugo/kmp/di/PlatformModule.android.kt (actual)
actual fun platformModule(): Module = module {
    single<Settings> { Settings() }  // Android-specific
}

// ... similarmente para desktop, iOS, wasmJs
```

**Problema a resolver:** Necesidad de crear instancias de `Settings` (multiplatform-settings) de forma específica por plataforma para el módulo `kmp-storage`.

---

## Decisión

**Se decidió NO implementar PlatformModule** y en su lugar usar el patrón **Factory Method** directamente en `EduGoStorage`:

```kotlin
// modules/storage/src/commonMain/kotlin/com/edugo/kmp/storage/EduGoStorage.kt
class EduGoStorage internal constructor(
    private val settings: Settings,
    private val keyPrefix: String = ""
) {
    companion object {
        fun create(): EduGoStorage = EduGoStorage(createPlatformSettings())
        fun create(name: String): EduGoStorage = EduGoStorage(createPlatformSettings(name), name)
        fun withSettings(settings: Settings, keyPrefix: String = ""): EduGoStorage = 
            EduGoStorage(settings, keyPrefix)
    }
}

// expect/actual en Storage module
internal expect fun createPlatformSettings(name: String? = null): Settings
```

### Implementaciones por plataforma

```kotlin
// androidMain/PlatformSettings.android.kt
internal actual fun createPlatformSettings(name: String?): Settings = Settings()

// desktopMain/PlatformSettings.desktop.kt
internal actual fun createPlatformSettings(name: String?): Settings = Settings()

// wasmJsMain/PlatformSettings.wasmJs.kt
internal actual fun createPlatformSettings(name: String?): Settings = Settings()
```

### Integración con DI (StorageModule)

```kotlin
// di/src/commonMain/kotlin/com/edugo/kmp/di/module/StorageModule.kt
public val storageModule = module {
    single { EduGoStorage.create() }  // ← Factory method
    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
    single { AsyncEduGoStorage(get<EduGoStorage>()) }
}
```

---

## Alternativas Consideradas

### Alternativa 1: PlatformModule expect/actual (Especificación original)

```kotlin
// commonMain/PlatformModule.kt
expect fun platformModule(): Module

// androidMain/PlatformModule.android.kt
actual fun platformModule(): Module = module {
    single<Settings> { Settings() }
}
```

**Pros:**
- Centraliza toda la configuración por plataforma en un solo lugar
- Sigue el patrón típico de Koin para multiplatform
- Explícito sobre qué es específico de plataforma

**Contras:**
- Acopla Storage a Koin DI (Storage es infraestructura base)
- Requiere source sets por plataforma en `kmp-di` (androidMain, iosMain, etc.)
- Complica testing (requiere iniciar Koin en tests)
- Agrega complejidad para casos simples (solo Settings)
- Dependencia circular potencial (storage usado por config/auth que están en DI)

### Alternativa 2: Factory Method en EduGoStorage (Implementada)

```kotlin
class EduGoStorage {
    companion object {
        fun create(): EduGoStorage = EduGoStorage(createPlatformSettings())
        fun withSettings(settings: Settings): EduGoStorage = EduGoStorage(settings)
    }
}
```

**Pros:**
- ✅ Desacopla Storage de Koin DI
- ✅ API más simple y directa (`EduGoStorage.create()`)
- ✅ Testing simplificado (`withSettings(MapSettings())`)
- ✅ Soporta múltiples instancias con prefijos diferentes
- ✅ Lazy initialization (solo se crea cuando se necesita)
- ✅ Control de ciclo de vida por el usuario
- ✅ Evita source sets adicionales en `kmp-di`

**Contras:**
- Menos centralización (expect/actual en Storage, no en DI)
- No sigue el patrón "puro" de DI para plataformas

### Alternativa 3: Híbrida (PlatformModule + Factory)

Ambos enfoques coexistiendo.

**Pros:**
- Máxima flexibilidad

**Contras:**
- Confusión sobre cuál usar
- Duplicación de lógica
- Mayor superficie de API

---

## Justificación

La decisión de usar **Factory Method** se basa en los siguientes principios arquitectónicos:

### 1. Bajo Acoplamiento

**Storage es infraestructura base** que debe ser independiente del framework de DI:

```
Capas de Dependencia:
foundation (base)
  ↓
storage (NO debe depender de DI)
  ↓
config, network, auth (pueden usar DI)
  ↓
di (orquesta todo)
```

Si Storage depende de Koin para inicializarse, creamos acoplamiento innecesario.

### 2. Testabilidad

**Con Factory Method:**
```kotlin
@Test
fun testStorageLogic() {
    val storage = EduGoStorage.withSettings(MapSettings())
    storage.putString("key", "value")
    assertEquals("value", storage.getString("key"))
}
```

**Con PlatformModule:**
```kotlin
@Test
fun testStorageLogic() {
    startKoin {
        modules(platformModule(), storageModule)
    }
    val storage: EduGoStorage = get()
    // ... más setup ...
    stopKoin()
}
```

Factory Method reduce boilerplate en tests.

### 3. Flexibilidad de Uso

EduGoStorage.create() permite:

```kotlin
// Uso 1: Instancia global default
val storage = EduGoStorage.create()

// Uso 2: Instancia nombrada (para multi-tenant)
val userStorage = EduGoStorage.create("user_123")
val appStorage = EduGoStorage.create("app_config")

// Uso 3: Testing con mocks
val testStorage = EduGoStorage.withSettings(MapSettings())

// Uso 4: Via DI (si se necesita)
val storageModule = module {
    single { EduGoStorage.create() }
}
```

PlatformModule solo soporta escenario 4.

### 4. Simplicidad

**Líneas de código requeridas:**

| Enfoque | LOC | Archivos |
|---------|-----|----------|
| Factory Method | ~30 | 4 (1 expect + 3 actuals) |
| PlatformModule | ~80 | 8 (1 expect + 3 actuals + integración DI) |

Factory Method es 60% menos código.

### 5. Evita Source Sets Adicionales en kmp-di

Con Factory Method, `kmp-di` solo necesita `commonMain`:

```
di/
├── src/commonMain/kotlin/
│   └── com/edugo/kmp/di/
│       ├── KoinInitializer.kt
│       └── module/
│           ├── FoundationModule.kt
│           ├── StorageModule.kt  ← Usa EduGoStorage.create()
│           └── ...
```

Con PlatformModule, necesitaría:

```
di/
├── src/commonMain/kotlin/  (expect)
├── src/androidMain/kotlin/ (actual)
├── src/iosMain/kotlin/     (actual)
├── src/desktopMain/kotlin/ (actual)
└── src/wasmJsMain/kotlin/  (actual)
```

Esto complica la estructura del módulo DI.

---

## Consecuencias

### Positivas

1. **Storage es completamente independiente de Koin**
   - Puede usarse sin inicializar DI
   - Útil para herramientas CLI, scripts, testing unitario

2. **API más clara y directa**
   - `EduGoStorage.create()` es autoexplicativo
   - No requiere entender Koin para usar Storage

3. **Testing simplificado**
   - `withSettings(MapSettings())` en una línea
   - Sin necesidad de `startKoin()` / `stopKoin()`

4. **Soporta múltiples instancias**
   - `create("user")`, `create("app")`, `create("cache")`
   - Útil para escenarios multi-tenant o segmentación de datos

5. **kmp-di mantiene estructura simple**
   - Solo `commonMain`, sin source sets por plataforma
   - Menos complejidad en el módulo orquestador

### Negativas (Mitigadas)

1. ⚠️ **Menos centralización de lógica por plataforma**
   - Mitigado: Storage es el único caso actual que necesita expect/actual
   - Si surgen más casos, se puede introducir PlatformModule en ese momento

2. ⚠️ **No sigue patrón "puro" de DI**
   - Mitigado: Storage es infraestructura, no lógica de negocio
   - Las capas superiores (auth, config) SÍ usan DI puro

---

## Cuándo Reconsiderar

Esta decisión debería revisarse si:

1. **Aparecen 3+ componentes** que necesitan expect/actual por plataforma
   - Ejemplo: FileSystem, Clipboard, Sensors, etc.
   - En ese caso, PlatformModule centralizaría mejor

2. **Se requiere inyección de Settings** desde DI en múltiples lugares
   - Actualmente solo Storage usa Settings directamente
   - Si Config, Auth, etc. lo necesitan, considerar PlatformModule

3. **Política de proyecto** requiere que TODO sea inyectado via DI
   - Decisión organizacional sobre pureza arquitectónica

---

## Referencias

### Código Implementado

- **Factory Method:** `/modules/storage/src/commonMain/kotlin/com/edugo/kmp/storage/EduGoStorage.kt`
- **Expect/Actual:** `/modules/storage/src/{platform}Main/kotlin/.../PlatformSettings.kt`
- **StorageModule:** `/modules/di/src/commonMain/kotlin/com/edugo/kmp/di/module/StorageModule.kt`

### Especificación Original

- **SPRINT-3-DETALLE.md:** Sección "PlatformModule expect/actual" (líneas 303-380)

### Documentos Relacionados

- **CLAUDE.md:** Sección "Architecture" sobre multiplatform patterns
- **VALIDACION-SPRINT-3.md:** Discrepancia #2 sobre PlatformModule

---

## Conclusión

**La decisión de usar EduGoStorage Factory Method es arquitectónicamente superior** para el caso de uso actual:

- ✅ Mantiene bajo acoplamiento (Storage independiente de DI)
- ✅ Simplifica testing (sin iniciar Koin)
- ✅ API más clara (`create()` vs `get<Storage>()`)
- ✅ Soporta múltiples instancias con prefijos
- ✅ Reduce complejidad en `kmp-di` (sin source sets por plataforma)

**Esta decisión NO invalida PlatformModule** como patrón, sino que reconoce que para el caso específico de Storage, un Factory Method es más apropiado.

Si en el futuro aparecen múltiples componentes que requieren configuración por plataforma, se puede introducir PlatformModule sin refactorizar Storage (ambos pueden coexistir).

---

**Aprobación:**
- [x] Decisión documentada
- [x] Implementación verificada
- [x] Tests pasando (BUILD SUCCESSFUL)
- [x] Validación arquitectónica completa

**Próxima revisión:** Sprint 4 (si aparecen nuevos requerimientos multiplataforma)
