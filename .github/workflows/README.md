# GitHub Actions Workflows

Este directorio contiene los workflows de CI/CD para el proyecto EduUI-KMP.

## Workflows Disponibles

### 🔄 Sync Main to Dev (`sync-main-to-dev.yml`)

**Propósito:** Sincroniza automáticamente los cambios de `main` a `dev` después de un merge.

**Disparador:**
- Push a la rama `main` (generalmente después de un merge de PR)

**Funcionamiento:**
1. Verifica que la rama `dev` existe (la crea si no existe)
2. Detecta si hay commits en `main` que `dev` no tiene
3. Hace un merge automático de `main` a `dev`
4. Si hay conflictos, el workflow falla y requiere resolución manual

**Características:**
- ✅ Previene loops infinitos (ignora commits que contienen "chore: sync")
- ✅ Crea la rama `dev` automáticamente si no existe
- ✅ Solo se ejecuta cuando hay diferencias reales entre ramas
- ✅ Proporciona resumen detallado del proceso
- ⚠️ Requiere resolución manual si hay conflictos

**Permisos requeridos:**
- `contents: write` - Para hacer push a la rama `dev`

## Próximos Workflows (Pendientes)

Los siguientes workflows se agregarán en futuras iteraciones:

- **Build & Test:** Compilación y pruebas para cada plataforma (Android, iOS, Desktop, Web)
- **PR Checks:** Validación de PRs antes de merge
- **Release:** Generación automática de releases y artifacts
- **Code Coverage:** Reportes de cobertura de código

## Uso

Los workflows se ejecutan automáticamente según sus disparadores configurados. No requieren intervención manual en condiciones normales.

Para desactivar temporalmente un workflow, puedes:
1. Comentar el trigger `on:` en el archivo
2. Usar la interfaz de GitHub Actions para deshabilitarlo

## Troubleshooting

### El workflow de sync falla por conflictos

Si `sync-main-to-dev.yml` falla por conflictos:

1. Resolver conflictos manualmente:
```bash
git checkout dev
git pull origin dev
git merge origin/main
# Resolver conflictos manualmente
git add .
git commit -m "chore: sync main to dev (conflictos resueltos manualmente)"
git push origin dev
```

2. El próximo push a `main` se sincronizará normalmente.

### El workflow no se ejecuta

Verifica que:
- El workflow está en `.github/workflows/`
- El archivo tiene extensión `.yml` o `.yaml`
- La rama tiene los permisos correctos configurados en GitHub
- No hay errores de sintaxis YAML
