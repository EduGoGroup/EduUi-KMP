package com.edugo.kmp.dynamicui.loader

import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.HttpRequestConfig

class RemoteScreenLoader(
    private val httpClient: EduGoHttpClient,
    private val baseUrl: String
) : ScreenLoader {

    override suspend fun loadScreen(
        screenKey: String,
        platform: String?
    ): Result<ScreenDefinition> {
        if (!VALID_KEY_REGEX.matches(screenKey)) {
            return Result.Failure("Invalid screenKey: only alphanumeric, hyphens and underscores allowed")
        }
        val configBuilder = HttpRequestConfig.builder()
        platform?.let { configBuilder.queryParam("platform", it) }
        return httpClient.getSafe(
            url = "$baseUrl/v1/screen-config/resolve/key/$screenKey",
            config = configBuilder.build()
        )
    }

    companion object {
        private val VALID_KEY_REGEX = Regex("^[a-zA-Z0-9_-]+$")
    }

    override suspend fun loadNavigation(): Result<NavigationDefinition> {
        // TODO: Backend aún no tiene endpoint de navegación
        return httpClient.getSafe(
            url = "$baseUrl/v1/screen-config/navigation"
        )
    }
}
