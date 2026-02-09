# EduGo KMP Modules - Documentacion

## Indice

| # | Documento | Contenido |
|---|-----------|-----------|
| 01 | [Analisis Proyecto Actual](01-ANALISIS-PROYECTO-ACTUAL.md) | Inventario de Kmp-Common: 114 archivos, que rescatar, problemas |
| 02 | [Vision y Plataformas](02-VISION-Y-PLATAFORMAS.md) | Targets (Mobile/Desktop/Web), Compose Multiplatform, source sets |
| 03 | [Arquitectura Modular](03-ARQUITECTURA-MODULAR.md) | Tiers, grafo de dependencias, desacoplamiento |
| 04 | [Catalogo de Modulos](04-CATALOGO-MODULOS.md) | Los 15 modulos propuestos con contenido detallado |
| 05 | [Plan de Migracion](05-PLAN-MIGRACION.md) | Fases generales, checklist, decisiones pendientes |
| 06 | [Comparacion Proyectos](06-COMPARACION-PROYECTOS.md) | Kmp-Common vs Template-Kmp-Clean: lo mejor de cada uno |
| 07 | [Plan de Trabajo](07-PLAN-DE-TRABAJO.md) | 4 sprints con tasks concretas, orden de ejecucion |

### Planos de Construccion por Sprint

| Sprint | Documento | Contenido |
|--------|-----------|-----------|
| **1** | [Sprint 1 - Cimientos](sprint-1/SPRINT-1-DETALLE.md) | build-logic, foundation, logger, core, validation (754 lineas) |
| **2** | [Sprint 2 - Infraestructura](sprint-2/SPRINT-2-DETALLE.md) | network, storage, config (682 lineas) |
| **3** | [Sprint 3 - Dominio](sprint-3/SPRINT-3-DETALLE.md) | auth, di con Koin (762 lineas) |
| **4** | [Sprint 4 - Presentacion](sprint-4/SPRINT-4-DETALLE.md) | design, resources, navigation, app ejemplo (2881 lineas) |

---

## Contexto Rapido

**Que es esto:** Plan para crear un ecosistema de modulos KMP independientes fusionando lo mejor de dos proyectos:
- **Kmp-Common** → Logica robusta (Result, AppError, Network, Storage, Auth, Validation)
- **Template-Kmp-Clean** → Estructura ejemplar (Clean Arch, WASM, iOS on-demand, Koin, Compose)

**3 Plataformas target:**
- **Mobile:** Android + iOS via Compose Multiplatform (Material Design 3)
- **Desktop:** Windows + Mac + Linux via Compose Desktop
- **Web:** Kotlin/WASM via Compose Web

**Stack:** Kotlin 2.2.20, Compose 1.9.0, Koin 4.1, Ktor 3.1.3

**Arquitectura:** 5 tiers (Foundation → Core → Infrastructure → Domain → Presentation)

**Inspirado en:** Modulos Swift de EduGoModules + Template-Kmp-Clean

---

## Modulos Propuestos (Resumen Visual)

```
TIER-0  [ foundation ]
             ↓
TIER-1  [ logger ] [ core ] [ validation ]
             ↓
TIER-2  [ network ] [ storage ] [ config ]
             ↓
TIER-3  [ auth ] [ di ]
             ↓
TIER-4  [ design ] [ resources ] [ navigation ]
             ↓
TIER-5  [ platforms/mobile ] [ platforms/desktop ] [ platforms/web ]
```

## Proximo Paso

**Implementar Sprint 1** usando [sprint-1/SPRINT-1-DETALLE.md](sprint-1/SPRINT-1-DETALLE.md) como plano de construccion.

Cada documento de sprint contiene: archivos a crear, codigo fuente de referencia, cambios de package, build.gradle.kts completos, tests a migrar, y comandos de verificacion.
