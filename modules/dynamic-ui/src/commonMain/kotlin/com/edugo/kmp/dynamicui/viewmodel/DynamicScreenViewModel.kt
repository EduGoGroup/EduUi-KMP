package com.edugo.kmp.dynamicui.viewmodel

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionRegistry
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonObject

class DynamicScreenViewModel(
    private val screenLoader: ScreenLoader,
    private val dataLoader: DataLoader,
    private val actionRegistry: ActionRegistry
) {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _dataState = MutableStateFlow<DataState>(DataState.Idle)
    val dataState: StateFlow<DataState> = _dataState.asStateFlow()

    private val _fieldValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldValues: StateFlow<Map<String, String>> = _fieldValues.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    sealed class ScreenState {
        data object Loading : ScreenState()
        data class Ready(val screen: ScreenDefinition) : ScreenState()
        data class Error(val message: String) : ScreenState()
    }

    sealed class DataState {
        data object Idle : DataState()
        data object Loading : DataState()
        data class Success(val items: List<JsonObject>, val hasMore: Boolean) : DataState()
        data class Error(val message: String) : DataState()
    }

    suspend fun loadScreen(screenKey: String, platform: String? = null) {
        _screenState.value = ScreenState.Loading
        when (val result = screenLoader.loadScreen(screenKey, platform)) {
            is Result.Success -> {
                _screenState.value = ScreenState.Ready(result.data)
                result.data.dataEndpoint?.let { endpoint ->
                    result.data.dataConfig?.let { config ->
                        loadData(endpoint, config)
                    }
                }
            }
            is Result.Failure -> {
                _screenState.value = ScreenState.Error(result.error)
            }
            is Result.Loading -> {
                // Already in loading state
            }
        }
    }

    suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        extraParams: Map<String, String> = emptyMap()
    ) {
        _dataState.value = DataState.Loading
        when (val result = dataLoader.loadData(endpoint, config, extraParams)) {
            is Result.Success -> {
                _dataState.value = DataState.Success(
                    items = result.data.items,
                    hasMore = result.data.hasMore
                )
            }
            is Result.Failure -> {
                _dataState.value = DataState.Error(result.error)
            }
            is Result.Loading -> {
                // Already in loading state
            }
        }
    }

    suspend fun executeAction(
        actionDef: ActionDefinition,
        itemData: JsonObject? = null
    ): ActionResult {
        val handler = actionRegistry.resolve(actionDef.type)
        val context = ActionContext(
            screenKey = (screenState.value as? ScreenState.Ready)?.screen?.screenKey ?: "",
            actionId = actionDef.id,
            config = actionDef.config,
            fieldValues = _fieldValues.value,
            selectedItem = itemData
        )
        return handler.execute(context)
    }

    fun onFieldChanged(fieldId: String, value: String) {
        _fieldValues.update { it + (fieldId to value) }
        _fieldErrors.update { it - fieldId }
    }

    fun resetFields() {
        _fieldValues.value = emptyMap()
        _fieldErrors.value = emptyMap()
    }
}
