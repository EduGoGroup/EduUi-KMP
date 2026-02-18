package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.renderer.PatternRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

@Composable
fun DynamicScreen(
    screenKey: String,
    viewModel: DynamicScreenViewModel,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    placeholders: Map<String, String> = emptyMap(),
    onAction: ((ActionDefinition, JsonObject?, CoroutineScope) -> Unit)? = null,
    onFieldChanged: ((String, String) -> Unit)? = null,
) {
    val screenState by viewModel.screenState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val fieldValues by viewModel.fieldValues.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(screenKey, placeholders) {
        viewModel.loadScreen(screenKey, placeholders = placeholders)
    }

    when (val state = screenState) {
        is DynamicScreenViewModel.ScreenState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                DSLinearProgress(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.spacing8))
            }
        }

        is DynamicScreenViewModel.ScreenState.Ready -> {
            PatternRouter(
                screen = state.screen,
                dataState = dataState,
                fieldValues = fieldValues,
                fieldErrors = fieldErrors,
                onFieldChanged = onFieldChanged ?: viewModel::onFieldChanged,
                onAction = { action: ActionDefinition, item: JsonObject? ->
                    if (onAction != null) {
                        onAction(action, item, scope)
                    } else {
                        scope.launch {
                            val result = viewModel.executeAction(action, item)
                            if (result is ActionResult.NavigateTo) {
                                onNavigate(result.screenKey, result.params)
                            }
                        }
                    }
                },
                onNavigate = onNavigate,
                modifier = modifier,
            )
        }

        is DynamicScreenViewModel.ScreenState.Error -> {
            Box(
                modifier = modifier.fillMaxSize().padding(Spacing.spacing4),
                contentAlignment = Alignment.Center,
            ) {
                DSEmptyState(
                    icon = Icons.Default.Warning,
                    title = "Error loading screen",
                    description = state.message,
                    actionLabel = "Retry",
                    onAction = {
                        scope.launch {
                            viewModel.loadScreen(screenKey)
                        }
                    },
                )
            }
        }
    }
}
