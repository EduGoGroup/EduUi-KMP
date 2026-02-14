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
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ZoneType
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import kotlinx.serialization.json.JsonObject

@Composable
fun SettingsPatternRenderer(
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
            .padding(vertical = Spacing.spacing2),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing1),
    ) {
        zones.forEachIndexed { index, zone ->
            ZoneRenderer(
                zone = zone,
                actions = actions,
                data = items,
                fieldValues = fieldValues,
                fieldErrors = fieldErrors,
                onFieldChanged = onFieldChanged,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.spacing4),
            )

            // Add divider between form sections
            if (zone.type == ZoneType.FORM_SECTION && index < zones.lastIndex) {
                DSDivider(modifier = Modifier.padding(vertical = Spacing.spacing2))
            }
        }
    }
}
