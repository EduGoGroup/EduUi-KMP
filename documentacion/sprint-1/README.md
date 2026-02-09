# Documentación Sprint 1: Cimientos del Monorepo

## Documentos Disponibles

### 1. SPRINT-1-DETALLE.md
**Documento maestro** con todos los detalles técnicos del sprint.

**Contenido**:
- Visión general del sprint
- Objetivos y entregables
- Tasks 1.1 a 1.6 completas
- Diagramas de dependencias
- Orden de ejecución
- Checklist de verificación
- Problemas potenciales y soluciones

**Cuándo usarlo**: Como guía de referencia completa durante la implementación.

---

### 2. RESUMEN-EJECUTIVO.md
**Vista rápida** del sprint con información clave.

**Contenido**:
- Tabla de tasks con duración
- Lista de código a migrar (archivo por archivo)
- Cambios de packages
- Stack tecnológico
- Estructura final
- Comandos quick start
- Criterios de éxito

**Cuándo usarlo**: Para tener una vista panorámica antes de comenzar.

---

### 3. TASK-1.1-ESTRUCTURA-BASE.md
**Guía específica** para la Task 1.1.

**Contenido**:
- Lista de archivos a crear
- Referencias a código fuente
- Comandos paso a paso
- Checklist de verificación

**Cuándo usarlo**: Durante la implementación de la Task 1.1.

---

### 4. TROUBLESHOOTING.md
**Guía de resolución** de problemas comunes.

**Contenido**:
- Problemas de Gradle
- Problemas de Convention Plugins
- Problemas de Compilación
- Problemas de Tests
- Problemas de Plataformas
- Problemas de Performance
- Comandos de diagnóstico

**Cuándo usarlo**: Cuando algo falla durante la implementación.

---

## Orden de Lectura Recomendado

### Para Implementadores

1. **RESUMEN-EJECUTIVO.md** → Vista panorámica
2. **SPRINT-1-DETALLE.md** → Referencia técnica
3. **TASK-1.X-*.md** → Guías específicas (según task actual)
4. **TROUBLESHOOTING.md** → Cuando surjan problemas

### Para Revisores

1. **RESUMEN-EJECUTIVO.md** → Entender alcance
2. **Sección de Criterios de Éxito** en SPRINT-1-DETALLE.md
3. **Checklist de Verificación** en SPRINT-1-DETALLE.md

---

## Información Clave

### Ubicación del Proyecto

```
Kmp-Common (origen):
/Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common

Nuevo monorepo (destino):
/Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common/kmp_new
```

### Stack Tecnológico

- Kotlin: 2.2.20
- Gradle: 8.11.1
- AGP: 8.12.0
- Ktor: 3.1.3
- Koin: 4.1.0
- Compose: 1.9.0

### Plataformas

- ✅ Android (SDK 24-36)
- ✅ Desktop (JVM 17)
- ✅ JavaScript (Browser + Node.js)
- ⏳ WASM (experimental)
- ⏳ iOS (on-demand)

### Duración Estimada

**Total**: 15 horas (2 días de trabajo)

- Task 1.1: 2 horas
- Task 1.2: 3 horas
- Task 1.3: 4 horas
- Task 1.4: 2 horas
- Task 1.5: 2 horas
- Task 1.6: 2 horas

---

## Quick Start

```bash
# 1. Ir al directorio del nuevo proyecto
cd /Users/jhoanmedina/source/EduGo/EduUI/Modules/Kmp-Common/kmp_new

# 2. Leer resumen ejecutivo
cat documentacion/sprint-1/RESUMEN-EJECUTIVO.md

# 3. Comenzar con Task 1.1
cat documentacion/sprint-1/TASK-1.1-ESTRUCTURA-BASE.md

# 4. Si hay problemas
cat documentacion/sprint-1/TROUBLESHOOTING.md
```

---

## Archivos de Código Completos

Los archivos de configuración completos (build.gradle.kts, settings.gradle.kts, etc.) están referenciados en **SPRINT-1-DETALLE.md** dentro de cada task.

Para copiar código rápidamente, buscar la sección "### Contenido Completo de los Archivos" en cada task.

---

## Notas Importantes

1. **No implementar código aún**: Este sprint solo documenta. La implementación será en sesiones futuras.

2. **Paths absolutos**: Todos los paths en la documentación son absolutos desde la raíz del sistema para evitar confusión.

3. **Código fuente completo**: Los archivos de configuración están incluidos completos en SPRINT-1-DETALLE.md.

4. **Tests incluidos**: Cada módulo tiene referencias a tests a migrar desde Kmp-Common.

5. **Verificación obligatoria**: Cada task tiene sección de "Verificación" con comandos exactos.

---

**Siguiente paso**: Leer RESUMEN-EJECUTIVO.md para tener contexto completo antes de comenzar la implementación.
