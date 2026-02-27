package com.edugo.kmp.dynamicui.viewmodel

import com.edugo.kmp.dynamicui.cache.RecentScreenTracker
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContractRegistry
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.data.CachedDataLoader
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.event.ScreenDataEvent
import com.edugo.kmp.dynamicui.event.ScreenEventBus
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ZoneType
import com.edugo.kmp.dynamicui.offline.MutationQueue
import com.edugo.kmp.dynamicui.orchestrator.EventOrchestrator
import com.edugo.kmp.dynamicui.resolver.FormFieldsResolver
import com.edugo.kmp.dynamicui.resolver.PlaceholderResolver
import com.edugo.kmp.dynamicui.resolver.SlotBindingResolver
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.connectivity.NetworkObserver
import com.edugo.kmp.network.connectivity.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class DynamicScreenViewModel(
    private val screenLoader: ScreenLoader,
    private val dataLoader: DataLoader,
    private val orchestrator: EventOrchestrator,
    private val contractRegistry: ScreenContractRegistry,
    private val networkObserver: NetworkObserver? = null,
    private val recentScreenTracker: RecentScreenTracker? = null,
    private val mutationQueue: MutationQueue? = null,
    private val screenEventBus: ScreenEventBus? = null,
) {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _dataState = MutableStateFlow<DataState>(DataState.Idle)
    val dataState: StateFlow<DataState> = _dataState.asStateFlow()

    private val _fieldValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldValues: StateFlow<Map<String, String>> = _fieldValues.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi::class)
    val isOnline: StateFlow<Boolean> = networkObserver?.let { observer ->
        object : StateFlow<Boolean> {
            override val value: Boolean get() = observer.isOnline
            override val replayCache: List<Boolean> get() = listOf(value)
            override suspend fun collect(collector: FlowCollector<Boolean>): Nothing {
                observer.status.map { it == NetworkStatus.AVAILABLE }
                    .distinctUntilChanged()
                    .collect(collector)
                error("StateFlow collection should never complete")
            }
        }
    } ?: MutableStateFlow(true)

    val pendingMutationCount: StateFlow<Int> = mutationQueue?.pendingCount
        ?: MutableStateFlow(0)

    sealed class ScreenState {
        data object Loading : ScreenState()
        data class Ready(val screen: ScreenDefinition) : ScreenState()
        data class Error(val message: String) : ScreenState()
    }

    companion object {
        const val OFFLINE_NOT_AVAILABLE = "OFFLINE_NOT_AVAILABLE"
    }

    sealed class DataState {
        data object Idle : DataState()
        data object Loading : DataState()
        data class Success(
            val items: List<JsonObject>,
            val hasMore: Boolean,
            val loadingMore: Boolean = false,
            val isOfflineFiltered: Boolean = false,
            val isStale: Boolean = false,
        ) : DataState()
        data class Error(val message: String) : DataState()
    }

    suspend fun loadScreen(
        screenKey: String,
        platform: String? = null,
        placeholders: Map<String, String> = emptyMap(),
    ) {
        _screenState.value = ScreenState.Loading
        _screenParams = placeholders

        // Track recent screen access
        recentScreenTracker?.recordAccess(screenKey)

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
                    params = placeholders,
                )
                val endpoint = contract?.endpointFor(ScreenEvent.LOAD_DATA, context)
                if (endpoint != null) {
                    val config = contract.dataConfig()
                    // For forms in edit mode (single object), pre-fill field values
                    if (resolved.pattern == com.edugo.kmp.dynamicui.model.ScreenPattern.FORM && placeholders.containsKey("id")) {
                        loadFormData(endpoint, config)
                    } else {
                        loadDataWithStaleness(endpoint, config, screenKey = screenKey, screenPattern = resolved.pattern)
                    }
                }

                // Subscribe to event bus for auto-reload on data changes
                subscribeToEventBus(screenKey, placeholders)
            }
            is Result.Failure -> {
                val isOffline = networkObserver != null && !networkObserver.isOnline
                _screenState.value = if (isOffline) {
                    ScreenState.Error(OFFLINE_NOT_AVAILABLE)
                } else {
                    ScreenState.Error(result.error)
                }
            }
            is Result.Loading -> {
                // Already in loading state
            }
        }
    }

    suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        extraParams: Map<String, String> = emptyMap(),
    ) {
        _dataState.value = DataState.Loading
        when (val result = dataLoader.loadData(endpoint, config, extraParams)) {
            is Result.Success -> {
                val items = applyFieldMapping(result.data.items, config.fieldMapping)
                _allItems = items
                _dataState.value = DataState.Success(
                    items = items,
                    hasMore = result.data.hasMore,
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

    private suspend fun loadDataWithStaleness(
        endpoint: String,
        config: DataConfig,
        extraParams: Map<String, String> = emptyMap(),
        screenKey: String? = null,
        screenPattern: com.edugo.kmp.dynamicui.model.ScreenPattern? = null,
    ) {
        _dataState.value = DataState.Loading

        val cachedLoader = dataLoader as? CachedDataLoader
        if (cachedLoader != null) {
            when (val result = cachedLoader.loadDataWithStaleness(endpoint, config, extraParams, screenPattern, screenKey)) {
                is Result.Success -> {
                    val items = applyFieldMapping(result.data.data.items, config.fieldMapping)
                    _allItems = items
                    _dataState.value = DataState.Success(
                        items = items,
                        hasMore = result.data.data.hasMore,
                        isStale = result.data.isStale,
                    )
                }
                is Result.Failure -> {
                    _dataState.value = DataState.Error(result.error)
                }
                is Result.Loading -> { }
            }
        } else {
            loadData(endpoint, config, extraParams)
        }
    }

    suspend fun loadMore() {
        val currentState = _dataState.value
        if (currentState !is DataState.Success || !currentState.hasMore || currentState.loadingMore) return

        val screen = (screenState.value as? ScreenState.Ready)?.screen ?: return
        val contract = contractRegistry.find(screen.screenKey) ?: return
        val context = EventContext(screenKey = screen.screenKey, params = _screenParams)
        val endpoint = contract.endpointFor(ScreenEvent.LOAD_MORE, context) ?: return
        val config = contract.dataConfig()
        val pagination = config.pagination ?: return

        _dataState.value = currentState.copy(loadingMore = true)

        val offset = currentState.items.size
        val extraParams = mapOf(pagination.offsetParam to offset.toString())

        when (val result = dataLoader.loadData(endpoint, config, extraParams)) {
            is Result.Success -> {
                val items = applyFieldMapping(result.data.items, config.fieldMapping)
                val combined = currentState.items + items
                _allItems = combined
                _dataState.value = DataState.Success(
                    items = combined,
                    hasMore = result.data.hasMore,
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
        context: EventContext,
    ): EventResult {
        return orchestrator.execute(screenKey, event, context)
    }

    suspend fun executeCustomEvent(
        screenKey: String,
        eventId: String,
        context: EventContext,
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

    private fun validateFormFields(): Map<String, String> {
        val screen = (screenState.value as? ScreenState.Ready)?.screen ?: return emptyMap()
        val errors = mutableMapOf<String, String>()

        screen.template.zones
            .filter { it.type == ZoneType.FORM_SECTION }
            .flatMap { it.slots }
            .filter { it.required && !it.readOnly }
            .forEach { slot ->
                val value = _fieldValues.value[slot.id]
                if (value.isNullOrBlank()) {
                    errors[slot.id] = "Este campo es obligatorio"
                }
            }

        return errors
    }

    suspend fun submitForm(
        endpoint: String,
        method: String,
        fieldValues: Map<String, String>,
    ): EventResult {
        // Client-side validation: check required fields before API call
        val validationErrors = validateFormFields()
        if (validationErrors.isNotEmpty()) {
            _fieldErrors.value = validationErrors
            return EventResult.Error("Corrige los campos marcados")
        }

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
            is Result.Success -> {
                if (result.data == null) {
                    // null signals "queued offline"
                    EventResult.Success(message = "Guardado localmente, se sincronizará al reconectar")
                } else {
                    // Emit data changed event so related screens auto-reload
                    val screenKey = (screenState.value as? ScreenState.Ready)?.screen?.screenKey
                    if (screenKey != null) {
                        val resource = contractRegistry.find(screenKey)?.resource
                        if (resource != null) {
                            screenEventBus?.emit(ScreenDataEvent.DataChanged(resource))
                        }
                    }
                    EventResult.Success(message = "Guardado exitosamente")
                }
            }
            is Result.Failure -> EventResult.Error(result.error)
            is Result.Loading -> EventResult.Error("Unexpected loading state")
        }
    }

    // Stores the navigation params so search/loadMore can build correct contexts
    private var _screenParams: Map<String, String> = emptyMap()

    // Stores the full unfiltered dataset for client-side fallback filtering
    private var _allItems: List<JsonObject> = emptyList()

    // Pending delete support: delays actual DELETE API call to allow undo
    private val pendingDeleteScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pendingDeleteJob: Job? = null

    fun schedulePendingDelete(itemId: String, endpoint: String, method: String, delayMs: Long = 5000) {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = pendingDeleteScope.launch {
            delay(delayMs)
            executePendingDelete(itemId, endpoint, method)
        }
    }

    fun cancelPendingDelete() {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
    }

    suspend fun executePendingDelete(itemId: String, endpoint: String, method: String) {
        val body = JsonObject(emptyMap())
        val result = dataLoader.submitData(endpoint, body, method)
        // Only emit DataDeleted when the delete was actually executed (data != null).
        // Result.Success(null) means the mutation was queued offline, not yet executed.
        if (result is Result.Success && result.data != null) {
            val screenKey = (screenState.value as? ScreenState.Ready)?.screen?.screenKey
            if (screenKey != null) {
                val resource = contractRegistry.find(screenKey)?.resource
                if (resource != null) {
                    screenEventBus?.emit(ScreenDataEvent.DataDeleted(resource, itemId))
                }
            }
        }
    }

    private var eventBusJob: Job? = null

    private fun subscribeToEventBus(screenKey: String, placeholders: Map<String, String>) {
        if (screenEventBus == null) return
        val contract = contractRegistry.find(screenKey) ?: return
        val myResource = contract.resource

        // Cancel previous subscription when screenKey changes
        eventBusJob?.cancel()
        eventBusJob = pendingDeleteScope.launch {
            screenEventBus.events.collect { event ->
                val matchesResource = when (event) {
                    is ScreenDataEvent.DataChanged -> event.resource == myResource
                    is ScreenDataEvent.DataDeleted -> event.resource == myResource
                }
                if (matchesResource) {
                    val context = EventContext(screenKey = screenKey, params = placeholders)
                    val endpoint = contract.endpointFor(ScreenEvent.LOAD_DATA, context)
                    if (endpoint != null) {
                        loadDataWithStaleness(
                            endpoint,
                            contract.dataConfig(),
                            screenKey = screenKey,
                            screenPattern = (screenState.value as? ScreenState.Ready)?.screen?.pattern,
                        )
                    }
                }
            }
        }
    }

    suspend fun search(query: String) {
        val screen = (screenState.value as? ScreenState.Ready)?.screen ?: return
        val contract = contractRegistry.find(screen.screenKey) ?: return
        val context = EventContext(screenKey = screen.screenKey, params = _screenParams)
        val endpoint = contract.endpointFor(ScreenEvent.SEARCH, context) ?: return
        val config = contract.dataConfig()

        // Empty query → reload all data, restore full dataset
        if (query.isBlank()) {
            if (_allItems.isNotEmpty()) {
                _dataState.value = DataState.Success(
                    items = _allItems,
                    hasMore = false,
                    isOfflineFiltered = false,
                )
            } else {
                loadData(endpoint, config)
            }
            return
        }

        // Step 1: Immediately show client-side filtered results (instant feedback)
        val baseline = _allItems.ifEmpty {
            (_dataState.value as? DataState.Success)?.items ?: emptyList()
        }
        if (baseline.isNotEmpty()) {
            val lowerQuery = query.lowercase()
            val localFiltered = baseline.filter { item ->
                item.values.any { value ->
                    value is kotlinx.serialization.json.JsonPrimitive &&
                        value.isString &&
                        value.content.lowercase().contains(lowerQuery)
                }
            }
            _dataState.value = DataState.Success(
                items = localFiltered,
                hasMore = false,
                isOfflineFiltered = true,
            )
        }

        // Step 2: Try server-side search (upgrades results if successful)
        val searchFields = config.searchFields
            ?: config.fieldMapping.values.toList().ifEmpty { null }

        val extraParams = if (searchFields.isNullOrEmpty()) {
            emptyMap()
        } else {
            mapOf(
                "search" to query,
                "search_fields" to searchFields.joinToString(","),
            )
        }

        val result = dataLoader.loadData(endpoint, config, extraParams)
        if (result is Result.Success) {
            val items = applyFieldMapping(result.data.items, config.fieldMapping)
            _dataState.value = DataState.Success(
                items = items,
                hasMore = result.data.hasMore,
                isOfflineFiltered = false,
            )
        }
        // If server fails → the local-filtered results from Step 1 remain visible (orange indicator)
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
        mapping: Map<String, String>,
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

    // ── Remote select options support ────────────────────────────────────

    sealed class SelectOptionsState {
        data object Loading : SelectOptionsState()
        data class Success(val options: List<com.edugo.kmp.dynamicui.model.SlotOption>) : SelectOptionsState()
        data class Error(val message: String) : SelectOptionsState()
    }

    private val _selectOptions = MutableStateFlow<Map<String, SelectOptionsState>>(emptyMap())
    val selectOptions: StateFlow<Map<String, SelectOptionsState>> = _selectOptions.asStateFlow()

    fun loadSelectOptions(fieldKey: String, endpoint: String, labelField: String, valueField: String) {
        if (_selectOptions.value.containsKey(fieldKey)) return
        _selectOptions.update { it + (fieldKey to SelectOptionsState.Loading) }
        pendingDeleteScope.launch {
            val result = dataLoader.loadData(endpoint, DataConfig(), emptyMap())
            if (result is Result.Success) {
                val options = result.data.items.mapNotNull { item ->
                    val label = item[labelField]?.let { el ->
                        (el as? JsonPrimitive)?.contentOrNull
                    } ?: return@mapNotNull null
                    val value = item[valueField]?.let { el ->
                        (el as? JsonPrimitive)?.contentOrNull
                    } ?: return@mapNotNull null
                    com.edugo.kmp.dynamicui.model.SlotOption(label = label, value = value)
                }
                _selectOptions.update { it + (fieldKey to SelectOptionsState.Success(options)) }
            } else if (result is Result.Failure) {
                _selectOptions.update { it + (fieldKey to SelectOptionsState.Error(result.error)) }
            }
        }
    }
}
