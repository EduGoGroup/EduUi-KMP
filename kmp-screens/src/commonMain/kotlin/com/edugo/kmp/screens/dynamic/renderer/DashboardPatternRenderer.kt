package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import kotlinx.serialization.json.JsonObject

@Composable
fun DashboardPatternRenderer(
    screen: ScreenDefinition,
    dataState: DynamicScreenViewModel.DataState,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zones = screen.template.zones

    val items = when (dataState) {
        is DynamicScreenViewModel.DataState.Success -> dataState.items
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing4),
    ) {
        if (dataState is DynamicScreenViewModel.DataState.Loading) {
            DSLinearProgress(modifier = Modifier.fillMaxWidth())
        }

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
            )
        }
    }
}
