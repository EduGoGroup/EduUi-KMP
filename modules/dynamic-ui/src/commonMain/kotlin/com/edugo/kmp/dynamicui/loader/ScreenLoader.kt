package com.edugo.kmp.dynamicui.loader

import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.foundation.result.Result

interface ScreenLoader {
    suspend fun loadScreen(screenKey: String, platform: String? = null): Result<ScreenDefinition>
    suspend fun loadNavigation(): Result<NavigationDefinition>
}
