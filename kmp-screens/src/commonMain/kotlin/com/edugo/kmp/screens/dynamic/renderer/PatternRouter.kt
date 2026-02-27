package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel.SelectOptionsState
import kotlinx.serialization.json.JsonObject

@Composable
fun PatternRouter(
    screen: ScreenDefinition,
    dataState: DynamicScreenViewModel.DataState,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    isStale: Boolean = false,
    selectOptionsMap: Map<String, SelectOptionsState> = emptyMap(),
    onLoadSelectOptions: ((String, String, String, String) -> Unit)? = null,
) {
    when (screen.pattern) {
        ScreenPattern.LOGIN -> LoginPatternRenderer(
            screen = screen,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onEvent = onEvent,
            onCustomEvent = onCustomEvent,
            modifier = modifier,
        )

        ScreenPattern.DASHBOARD -> DashboardPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onEvent = onEvent,
            onCustomEvent = onCustomEvent,
            modifier = modifier,
        )

        ScreenPattern.LIST -> ListPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onEvent = onEvent,
            onCustomEvent = onCustomEvent,
            modifier = modifier,
            isStale = isStale,
        )

        ScreenPattern.DETAIL -> DetailPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onEvent = onEvent,
            onCustomEvent = onCustomEvent,
            modifier = modifier,
        )

        ScreenPattern.SETTINGS -> SettingsPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onEvent = onEvent,
            onCustomEvent = onCustomEvent,
            modifier = modifier,
        )

        ScreenPattern.FORM -> FormPatternRenderer(
            screen = screen,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onEvent = onEvent,
            onCustomEvent = onCustomEvent,
            modifier = modifier,
            selectOptionsMap = selectOptionsMap,
            onLoadSelectOptions = onLoadSelectOptions,
        )

        else -> UnsupportedPatternFallback(
            pattern = screen.pattern,
            modifier = modifier,
        )
    }
}

@Composable
private fun UnsupportedPatternFallback(
    pattern: ScreenPattern,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(Spacing.spacing4),
        contentAlignment = Alignment.Center,
    ) {
        DSEmptyState(
            icon = Icons.Default.Warning,
            title = "Unsupported pattern",
            description = "The pattern '${pattern.name}' is not yet supported.",
        )
    }
}
