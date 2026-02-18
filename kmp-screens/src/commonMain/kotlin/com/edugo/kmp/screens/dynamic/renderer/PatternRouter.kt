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
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import kotlinx.serialization.json.JsonObject

@Composable
fun PatternRouter(
    screen: ScreenDefinition,
    dataState: DynamicScreenViewModel.DataState,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onAction: (ActionDefinition, JsonObject?) -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (screen.pattern) {
        ScreenPattern.LOGIN -> LoginPatternRenderer(
            screen = screen,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onAction = onAction,
            modifier = modifier,
        )

        ScreenPattern.DASHBOARD -> DashboardPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onAction = onAction,
            modifier = modifier,
        )

        ScreenPattern.LIST -> ListPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onAction = onAction,
            modifier = modifier,
        )

        ScreenPattern.DETAIL -> DetailPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onAction = onAction,
            modifier = modifier,
        )

        ScreenPattern.SETTINGS -> SettingsPatternRenderer(
            screen = screen,
            dataState = dataState,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onAction = onAction,
            modifier = modifier,
        )

        ScreenPattern.FORM -> FormPatternRenderer(
            screen = screen,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onAction = onAction,
            modifier = modifier,
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
