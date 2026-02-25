package com.edugo.kmp.dynamicui.viewmodel

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContractRegistry
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.orchestrator.EventOrchestrator
import com.edugo.kmp.dynamicui.resolver.FormFieldsResolver
import com.edugo.kmp.dynamicui.resolver.PlaceholderResolver
import com.edugo.kmp.dynamicui.resolver.SlotBindingResolver
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class DynamicScreenViewModel(
    private val screenLoader: ScreenLoader,
    private val dataLoader: DataLoader,
    private val orchestrator: EventOrchestrator,
    private val contractRegistry: ScreenContractRegistry
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
        data class Success(
            val items: List<JsonObject>,
            val hasMore: Boolean,
            val loadingMore: Boolean = false
        ) : DataState()
        data class Error(val message: String) : DataState()
    }

    suspend fun loadScreen(
        screenKey: String,
        platform: String? = null,
        placeholders: Map<String, String> = emptyMap()
    ) {
        _screenState.value = ScreenState.Loading
        when (val result = screenLoader.loadScreen(screenKey, platform)) {
            is Result.Success -> {
                val formResolved = FormFieldsResolver.resolve(result.data)
                val slotResolved = SlotBindingResolver.resolve(formResolved)
                val resolved = PlaceholderResolver.resolve(slotResolved, placeholders)
                _screenState.value = ScreenState.Ready(resolved)

                // Use contract to determine data loading
                val contract = contractRegistry.find(screenKey)
                val context = EventContext(
                    screenKey = screenKey,
                    params = placeholders
                )
                val endpoint = contract?.endpointFor(ScreenEvent.LOAD_DATA, context)
                if (endpoint != null) {
                    val config = contract.dataConfig()
                    // For forms in edit mode (single object), pre-fill field values
                    if (resolved.pattern == com.edugo.kmp.dynamicui.model.ScreenPattern.FORM && placeholders.containsKey("id")) {
                        loadFormData(endpoint, config)
                    } else {
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
                val items = applyFieldMapping(result.data.items, config.fieldMapping)
                _dataState.value = DataState.Success(
                    items = items,
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

    suspend fun loadMore() {
        val currentState = _dataState.value
        if (currentState !is DataState.Success || !currentState.hasMore || currentState.loadingMore) return

        val screen = (screenState.value as? ScreenState.Ready)?.screen ?: return
        val contract = contractRegistry.find(screen.screenKey) ?: return
        val context = EventContext(screenKey = screen.screenKey)
        val endpoint = contract.endpointFor(ScreenEvent.LOAD_MORE, context) ?: return
        val config = contract.dataConfig()
        val pagination = config.pagination ?: return

        _dataState.value = currentState.copy(loadingMore = true)

        val offset = currentState.items.size
        val extraParams = mapOf(pagination.offsetParam to offset.toString())

        when (val result = dataLoader.loadData(endpoint, config, extraParams)) {
            is Result.Success -> {
                val items = applyFieldMapping(result.data.items, config.fieldMapping)
                _dataState.value = DataState.Success(
                    items = currentState.items + items,
                    hasMore = result.data.hasMore
                )
            }
            is Result.Failure -> {
                _dataState.value = currentState.copy(loadingMore = false)
            }
            is Result.Loading -> { /* no-op */ }
        }
    }

    suspend fun executeEvent(
        screenKey: String,
        event: ScreenEvent,
        context: EventContext
    ): EventResult {
        return orchestrator.execute(screenKey, event, context)
    }

    suspend fun executeCustomEvent(
        screenKey: String,
        eventId: String,
        context: EventContext
    ): EventResult {
        return orchestrator.executeCustom(screenKey, eventId, context)
    }

    fun onFieldChanged(fieldId: String, value: String) {
        _fieldValues.update { it + (fieldId to value) }
        _fieldErrors.update { it - fieldId }
    }

    private suspend fun loadFormData(endpoint: String, config: DataConfig) {
        when (val result = dataLoader.loadData(endpoint, config)) {
            is Result.Success -> {
                // Single-object response: first item is the entity
                val item = result.data.items.firstOrNull()
                if (item != null) {
                    val values = mutableMapOf<String, String>()
                    for ((key, value) in item) {
                        val str = when (value) {
                            is kotlinx.serialization.json.JsonPrimitive -> {
                                if (value.isString) value.content else value.toString()
                            }
                            else -> continue
                        }
                        values[key] = str
                    }
                    _fieldValues.value = values
                }
            }
            is Result.Failure -> { /* silently ignore, form stays empty */ }
            is Result.Loading -> { }
        }
    }

    suspend fun submitForm(
        endpoint: String,
        method: String,
        fieldValues: Map<String, String>
    ): EventResult {
        // Get form field keys from the screen definition to filter only editable fields
        val screen = (_screenState.value as? ScreenState.Ready)?.screen
        val formFieldKeys = screen?.template?.zones
            ?.filter { it.type == com.edugo.kmp.dynamicui.model.ZoneType.FORM_SECTION }
            ?.flatMap { it.slots }
            ?.map { it.id }
            ?.toSet()

        // Only include fields that are in the form definition
        val editableValues = if (formFieldKeys != null) {
            fieldValues.filterKeys { it in formFieldKeys }
        } else {
            fieldValues
        }

        // Convert types: numbers → JsonPrimitive(int/double), booleans → JsonPrimitive(bool)
        val body = JsonObject(editableValues.mapValues { (_, v) ->
            when {
                v == "true" || v == "false" -> kotlinx.serialization.json.JsonPrimitive(v.toBoolean())
                v.toLongOrNull() != null -> kotlinx.serialization.json.JsonPrimitive(v.toLong())
                v.toDoubleOrNull() != null -> kotlinx.serialization.json.JsonPrimitive(v.toDouble())
                else -> kotlinx.serialization.json.JsonPrimitive(v)
            }
        })

        return when (val result = dataLoader.submitData(endpoint, body, method)) {
            is Result.Success -> EventResult.Success(message = "Guardado exitosamente")
            is Result.Failure -> EventResult.Error(result.error)
            is Result.Loading -> EventResult.Error("Unexpected loading state")
        }
    }

    fun canExecute(screenKey: String, event: ScreenEvent): Boolean {
        return orchestrator.canExecute(screenKey, event)
    }

    fun resetFields() {
        _fieldValues.value = emptyMap()
        _fieldErrors.value = emptyMap()
    }

    /**
     * Applies field mapping to transform API response field names to template-expected field names.
     * Mapping is "templateField" -> "apiField", e.g., "title" -> "full_name".
     * Original fields are preserved alongside mapped aliases.
     */
    private fun applyFieldMapping(
        items: List<JsonObject>,
        mapping: Map<String, String>
    ): List<JsonObject> {
        if (mapping.isEmpty()) return items
        return items.map { item ->
            val extra = mutableMapOf<String, JsonElement>()
            for ((templateField, apiField) in mapping) {
                val value = item[apiField]
                if (value != null) {
                    extra[templateField] = value
                }
            }
            if (extra.isEmpty()) item
            else JsonObject(item + extra)
        }
    }
}
