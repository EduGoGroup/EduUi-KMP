package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.progress.DSCircularProgress
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import kotlinx.serialization.json.JsonObject

@Composable
fun ListPatternRenderer(
    screen: ScreenDefinition,
    dataState: DynamicScreenViewModel.DataState,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onAction: (ActionDefinition, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zones = screen.template.zones
    val actions = screen.actions

    val items = when (dataState) {
        is DynamicScreenViewModel.DataState.Success -> dataState.items
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
    ) {
        if (dataState is DynamicScreenViewModel.DataState.Loading) {
            DSLinearProgress(modifier = Modifier.fillMaxWidth())
        }

        if (dataState is DynamicScreenViewModel.DataState.Error) {
            DSEmptyState(
                icon = Icons.Default.Warning,
                title = "Error loading data",
                description = dataState.message,
            )
        }

        zones.forEach { zone ->
            ZoneRenderer(
                zone = zone,
                actions = actions,
                data = items,
                fieldValues = fieldValues,
                fieldErrors = fieldErrors,
                onFieldChanged = onFieldChanged,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
