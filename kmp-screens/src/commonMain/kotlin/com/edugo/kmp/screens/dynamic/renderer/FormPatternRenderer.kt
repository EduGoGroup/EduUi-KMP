package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import kotlinx.serialization.json.JsonObject

@Composable
fun FormPatternRenderer(
    screen: ScreenDefinition,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zones = screen.template.zones

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing4),
        ) {
            zones.forEach { zone ->
                ZoneRenderer(
                    zone = zone,
                    data = emptyList(),
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
}
