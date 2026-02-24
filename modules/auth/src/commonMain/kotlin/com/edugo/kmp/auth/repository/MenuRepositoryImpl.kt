package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.MenuResponse
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient

/**
 * Implementación del repositorio de menú usando EduGoHttpClient.
 * Se conecta al IAM Platform API para obtener la estructura de navegación.
 *
 * IMPORTANTE: Usa el httpClient autenticado (con AuthInterceptor), NO plainHttp.
 *
 * @property httpClient Cliente HTTP con AuthInterceptor para agregar Bearer token
 * @property baseUrl URL base del IAM Platform (ej: "http://localhost:8070")
 */
public class MenuRepositoryImpl(
    private val httpClient: EduGoHttpClient,
    private val baseUrl: String
) : MenuRepository {

    override suspend fun getMenu(): Result<MenuResponse> {
        return httpClient.getSafe(url = "$baseUrl/api/v1/menu")
    }

    override suspend fun getFullMenu(): Result<MenuResponse> {
        return httpClient.getSafe(url = "$baseUrl/api/v1/menu/full")
    }
}
