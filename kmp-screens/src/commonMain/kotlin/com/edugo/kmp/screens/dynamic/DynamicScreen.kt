package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.progress.DSLinearProgress
import androidx.compose.material.icons.filled.CloudOff
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.offline.SyncEngine
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.components.ConnectivityBanner
import com.edugo.kmp.screens.dynamic.components.DynamicToolbar
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
    onBack: (() -> Unit)? = null,
    syncState: SyncEngine.SyncState = SyncEngine.SyncState.Idle,
) {
    val screenState by viewModel.screenState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val fieldValues by viewModel.fieldValues.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pendingCount by viewModel.pendingMutationCount.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isProcessingEvent by remember { mutableStateOf(false) }

    LaunchedEffect(screenKey, placeholders) {
        viewModel.loadScreen(screenKey, placeholders = placeholders)
    }

    val isEditMode = placeholders.containsKey("id")

    /** Shared handler for EventResult from both standard and custom events. */
    fun handleEventResult(result: EventResult) {
        scope.launch {
            when (result) {
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
                            if (onBack != null) onBack() else onNavigate(screenKey, emptyMap())
                        }
                        is EventResult.Error -> snackbarHostState.showSnackbar(submitResult.message)
                        else -> {}
                    }
                }
            }
        }
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
                val screen = state.screen
                val showToolbar = screen.pattern != ScreenPattern.LOGIN

                Column(modifier = Modifier.fillMaxSize()) {
                    // Toolbar
                    if (showToolbar) {
                        DynamicToolbar(
                            screen = screen,
                            isEditMode = isEditMode,
                            canCreate = viewModel.canExecute(screenKey, ScreenEvent.CREATE),
                            onBack = onBack,
                            onEvent = { event ->
                                scope.launch {
                                    if (isProcessingEvent) return@launch
                                    isProcessingEvent = true
                                    try {
                                        val context = EventContext(
                                            screenKey = screenKey,
                                            fieldValues = viewModel.fieldValues.value,
                                            params = placeholders,
                                        )
                                        // For form save events, route through "submit-form" custom handler
                                        // which returns SubmitTo with proper type conversion
                                        val result = if (
                                            (event == ScreenEvent.SAVE_NEW || event == ScreenEvent.SAVE_EXISTING) &&
                                            screen.pattern == ScreenPattern.FORM
                                        ) {
                                            viewModel.executeCustomEvent(screenKey, "submit-form", context)
                                        } else {
                                            viewModel.executeEvent(screenKey, event, context)
                                        }
                                        handleEventResult(result)
                                    } finally {
                                        isProcessingEvent = false
                                    }
                                }
                            },
                        )
                    }

                    // Connectivity banner between toolbar and content
                    ConnectivityBanner(
                        isOnline = isOnline,
                        pendingMutationCount = pendingCount,
                        syncState = syncState,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Content
                    val isStale = (dataState as? DynamicScreenViewModel.DataState.Success)?.isStale == true
                    PatternRouter(
                        screen = screen,
                        dataState = dataState,
                        fieldValues = fieldValues,
                        fieldErrors = fieldErrors,
                        onFieldChanged = onFieldChanged ?: viewModel::onFieldChanged,
                        onEvent = { event: ScreenEvent, item: JsonObject? ->
                            if (event == ScreenEvent.LOAD_MORE) {
                                scope.launch { viewModel.loadMore() }
                                return@PatternRouter
                            }
                            if (event == ScreenEvent.SEARCH) {
                                scope.launch {
                                    val readyScreen = (viewModel.screenState.value as? DynamicScreenViewModel.ScreenState.Ready)?.screen
                                    val searchSlotId = readyScreen?.template?.zones
                                        ?.flatMap { it.slots }
                                        ?.firstOrNull { it.controlType == com.edugo.kmp.dynamicui.model.ControlType.SEARCH_BAR }
                                        ?.id
                                    val query = searchSlotId?.let { viewModel.fieldValues.value[it] } ?: ""
                                    viewModel.search(query)
                                }
                                return@PatternRouter
                            }
                            scope.launch {
                                if (isProcessingEvent) return@launch
                                isProcessingEvent = true
                                try {
                                    val context = EventContext(
                                        screenKey = screenKey,
                                        fieldValues = viewModel.fieldValues.value,
                                        selectedItem = item,
                                        params = placeholders,
                                    )
                                    val result = viewModel.executeEvent(screenKey, event, context)
                                    handleEventResult(result)
                                } finally {
                                    isProcessingEvent = false
                                }
                            }
                        },
                        onCustomEvent = { eventId: String, item: JsonObject? ->
                            scope.launch {
                                if (isProcessingEvent) return@launch
                                isProcessingEvent = true
                                try {
                                    val context = EventContext(
                                        screenKey = screenKey,
                                        fieldValues = viewModel.fieldValues.value,
                                        selectedItem = item,
                                        params = placeholders,
                                    )
                                    val result = viewModel.executeCustomEvent(screenKey, eventId, context)
                                    handleEventResult(result)
                                } finally {
                                    isProcessingEvent = false
                                }
                            }
                        },
                        onNavigate = onNavigate,
                        modifier = Modifier.weight(1f),
                        isStale = isStale,
                    )
                }
            }

            is DynamicScreenViewModel.ScreenState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(Spacing.spacing4),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.message == DynamicScreenViewModel.OFFLINE_NOT_AVAILABLE) {
                        DSEmptyState(
                            icon = Icons.Default.CloudOff,
                            title = "No disponible sin conexión",
                            description = "Esta pantalla no está disponible sin conexión. Conéctate a internet para cargarla.",
                            actionLabel = "Reintentar",
                            onAction = {
                                scope.launch {
                                    viewModel.loadScreen(screenKey)
                                }
                            },
                        )
                    } else {
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

        // Snackbar overlay - positioned at bottom, no nested Scaffold
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.spacing4),
        )
    }
}
