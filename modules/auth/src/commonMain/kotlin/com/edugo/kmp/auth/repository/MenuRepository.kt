package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.MenuResponse
import com.edugo.kmp.foundation.result.Result

/**
 * Repositorio para obtener el menú de navegación desde IAM Platform.
 */
public interface MenuRepository {
    /**
     * Obtiene el menú filtrado por permisos del usuario autenticado.
     * Requiere token Bearer en el header (inyectado por AuthInterceptor).
     */
    public suspend fun getMenu(): Result<MenuResponse>

    /**
     * Obtiene el menú completo sin filtrar (solo admin/super_admin).
     */
    public suspend fun getFullMenu(): Result<MenuResponse>
}
