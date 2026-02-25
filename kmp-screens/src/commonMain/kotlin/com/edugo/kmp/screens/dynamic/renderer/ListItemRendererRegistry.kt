package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import kotlinx.serialization.json.JsonObject

typealias ListItemRenderer = @Composable (
    item: JsonObject,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
    modifier: Modifier,
) -> Unit

class ListItemRendererRegistry(
    private val renderers: Map<String, ListItemRenderer> = emptyMap(),
) {
    fun find(screenKey: String): ListItemRenderer? = renderers[screenKey]
}
