package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.screens.dynamic.components.ListSkeleton
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ControlType
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.components.StaleDataIndicator
import kotlinx.serialization.json.JsonObject
import org.koin.compose.getKoin

@Composable
fun ListPatternRenderer(
    screen: ScreenDefinition,
    dataState: DynamicScreenViewModel.DataState,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
    isStale: Boolean = false,
) {
    val zones = screen.template.zones
    val koin = getKoin()
    val listItemRenderer = remember(screen.screenKey) {
        koin.getOrNull<ListItemRendererRegistry>()?.find(screen.screenKey)
    }

    val rawItems = when (dataState) {
        is DynamicScreenViewModel.DataState.Success -> dataState.items
        else -> emptyList()
    }

    // Find search bar slot id to get the current query from fieldValues
    val searchSlotId = remember(zones) {
        zones.flatMap { it.slots }
            .firstOrNull { it.controlType == ControlType.SEARCH_BAR }
            ?.id
    }

    // Debounce search query and trigger server-side search
    val searchQuery = searchSlotId?.let { fieldValues[it] } ?: ""
    var debouncedQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        delay(300) // 300ms debounce
        if (debouncedQuery != searchQuery) {
            debouncedQuery = searchQuery
            onEvent(ScreenEvent.SEARCH, null)
        }
    }
    val isOfflineFiltered = (dataState as? DynamicScreenViewModel.DataState.Success)?.isOfflineFiltered == true
    val items = rawItems

    val scrollState = rememberScrollState()

    // Infinite scroll: trigger loadMore when near bottom
    val hasMore = (dataState as? DynamicScreenViewModel.DataState.Success)?.hasMore == true
    val isLoadingMore = (dataState as? DynamicScreenViewModel.DataState.Success)?.loadingMore == true
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (hasMore && !isLoadingMore
            && scrollState.maxValue > 0
            && scrollState.value >= scrollState.maxValue - 200
        ) {
            onEvent(ScreenEvent.LOAD_MORE, null)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(Spacing.spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
    ) {
        if (dataState is DynamicScreenViewModel.DataState.Loading) {
            ListSkeleton()
        }

        if (dataState is DynamicScreenViewModel.DataState.Error) {
            DSEmptyState(
                icon = Icons.Default.Warning,
                title = "Error al cargar datos",
                description = dataState.message,
            )
        }

        // Search mode indicator
        if (debouncedQuery.isNotBlank()) {
            val indicatorColor = if (isOfflineFiltered) Color(0xFFFF9800) else Color(0xFF4CAF50)
            val indicatorText = if (isOfflineFiltered) "Offline - datos locales" else "Online"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing1),
                modifier = Modifier.padding(horizontal = Spacing.spacing2),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = indicatorText,
                    style = MaterialTheme.typography.labelSmall,
                    color = indicatorColor,
                )
            }
        }

        // Stale data indicator
        StaleDataIndicator(isStale = isStale)

        zones.forEach { zone ->
            ZoneRenderer(
                zone = zone,
                data = items,
                fieldValues = fieldValues,
                fieldErrors = fieldErrors,
                onFieldChanged = onFieldChanged,
                onEvent = onEvent,
                onCustomEvent = onCustomEvent,
                modifier = Modifier.fillMaxWidth(),
                listItemRenderer = listItemRenderer,
            )
        }

        if (isLoadingMore) {
            DSLinearProgress(modifier = Modifier.fillMaxWidth())
        }
    }
}
