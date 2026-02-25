package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.renderer.PatternRouter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

@Composable
fun DynamicScreen(
    screenKey: String,
    viewModel: DynamicScreenViewModel,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    placeholders: Map<String, String> = emptyMap(),
    onFieldChanged: ((String, String) -> Unit)? = null,
) {
    val screenState by viewModel.screenState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val fieldValues by viewModel.fieldValues.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(screenKey, placeholders) {
        viewModel.loadScreen(screenKey, placeholders = placeholders)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = screenState) {
            is DynamicScreenViewModel.ScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                    onEvent = { event: ScreenEvent, item: JsonObject? ->
                        scope.launch {
                            val context = EventContext(
                                screenKey = screenKey,
                                fieldValues = viewModel.fieldValues.value,
                                selectedItem = item,
                                params = placeholders
                            )
                            when (val result = viewModel.executeEvent(screenKey, event, context)) {
                                is EventResult.NavigateTo -> onNavigate(result.screenKey, result.params)
                                is EventResult.Success -> result.message?.let { snackbarHostState.showSnackbar(it) }
                                is EventResult.Error -> snackbarHostState.showSnackbar(result.message)
                                is EventResult.PermissionDenied -> snackbarHostState.showSnackbar("Sin permisos")
                                is EventResult.Logout -> { /* handled by auth observer */ }
                                is EventResult.Cancelled -> { }
                                is EventResult.NoOp -> { }
                                is EventResult.SubmitTo -> { }
                            }
                        }
                    },
                    onCustomEvent = { eventId: String, item: JsonObject? ->
                        scope.launch {
                            val context = EventContext(
                                screenKey = screenKey,
                                fieldValues = viewModel.fieldValues.value,
                                selectedItem = item,
                                params = placeholders
                            )
                            when (val result = viewModel.executeCustomEvent(screenKey, eventId, context)) {
                                is EventResult.NavigateTo -> onNavigate(result.screenKey, result.params)
                                is EventResult.Success -> result.message?.let { snackbarHostState.showSnackbar(it) }
                                is EventResult.Error -> snackbarHostState.showSnackbar(result.message)
                                is EventResult.PermissionDenied -> snackbarHostState.showSnackbar("Sin permisos")
                                is EventResult.Logout -> { /* handled by auth observer */ }
                                is EventResult.Cancelled -> { }
                                is EventResult.NoOp -> { }
                                is EventResult.SubmitTo -> {
                                    val submitResult = viewModel.submitForm(result.endpoint, result.method, result.fieldValues)
                                    when (submitResult) {
                                        is EventResult.Success -> {
                                            snackbarHostState.showSnackbar(submitResult.message ?: "Guardado")
                                            onNavigate("schools-list", emptyMap())
                                        }
                                        is EventResult.Error -> snackbarHostState.showSnackbar(submitResult.message)
                                        else -> {}
                                    }
                                }
                            }
                        }
                    },
                    onNavigate = onNavigate,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is DynamicScreenViewModel.ScreenState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(Spacing.spacing4),
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

        // Snackbar overlay - positioned at bottom, no nested Scaffold
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.spacing4),
        )
    }
}
