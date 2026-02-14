package com.edugo.kmp.screens.dynamic.screens

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
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.renderer.PatternRouter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import org.koin.compose.koinInject

/**
 * Detalle de un material educativo.
 *
 * Usa DynamicScreen con screenKey="material-detail".
 * Recibe el ID del material como parametro y carga datos desde /v1/materials/{id}.
 * Soporta acciones: download, take quiz, go back.
 */
@Composable
fun DynamicMaterialDetailScreen(
    materialId: String,
    onNavigate: (String, Map<String, String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<DynamicScreenViewModel>()
    val scope = rememberCoroutineScope()

    val screenState by viewModel.screenState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val fieldValues by viewModel.fieldValues.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    LaunchedEffect(materialId) {
        viewModel.loadScreen("material-detail")
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
                onFieldChanged = viewModel::onFieldChanged,
                onAction = { action: ActionDefinition, item: JsonObject? ->
                    when (action.type) {
                        ActionType.NAVIGATE_BACK -> onBack()
                        else -> {
                            scope.launch {
                                val result = viewModel.executeAction(action, item)
                                when (result) {
                                    is ActionResult.NavigateTo -> onNavigate(result.screenKey, result.params)
                                    else -> { /* handled by viewModel */ }
                                }
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
                    title = "Error loading material",
                    description = state.message,
                    actionLabel = "Retry",
                    onAction = {
                        scope.launch { viewModel.loadScreen("material-detail") }
                    },
                )
            }
        }
    }
}
