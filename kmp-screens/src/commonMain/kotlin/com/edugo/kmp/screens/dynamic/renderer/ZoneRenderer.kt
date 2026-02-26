package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.Distribution
import com.edugo.kmp.dynamicui.model.Zone
import com.edugo.kmp.dynamicui.model.ZoneType
import kotlinx.serialization.json.JsonObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZoneRenderer(
    zone: Zone,
    data: List<JsonObject>,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
    listItemRenderer: ListItemRenderer? = null,
    maxDepth: Int = 10,
) {
    // Guard against excessively deep zone nesting
    if (maxDepth <= 0) return

    // Evaluate condition
    val condition = zone.condition
    if (condition != null && !evaluateCondition(condition, data)) return

    Column(modifier = modifier.padding(vertical = Spacing.spacing1)) {
        when {
            // List zones render items with itemLayout
            zone.type == ZoneType.SIMPLE_LIST && zone.itemLayout != null -> {
                // Render zone header slots first
                zone.slots.forEach { slot ->
                    SlotRenderer(
                        slot = slot,
                        fieldValues = fieldValues,
                        fieldErrors = fieldErrors,
                        onFieldChanged = onFieldChanged,
                        onCustomEvent = onCustomEvent,
                    )
                }
                // Render list items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    data.forEachIndexed { index, item ->
                        if (listItemRenderer != null) {
                            listItemRenderer(item, onEvent, onCustomEvent, Modifier.fillMaxWidth())
                        } else {
                            DefaultListItemRenderer(
                                item = item,
                                itemLayout = zone.itemLayout,
                                onEvent = onEvent,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (index < data.lastIndex) {
                            DSDivider()
                        }
                    }
                }
            }

            zone.type == ZoneType.GROUPED_LIST && zone.itemLayout != null -> {
                zone.slots.forEach { slot ->
                    SlotRenderer(
                        slot = slot,
                        fieldValues = fieldValues,
                        fieldErrors = fieldErrors,
                        onFieldChanged = onFieldChanged,
                        onCustomEvent = onCustomEvent,
                    )
                }
                data.forEach { item ->
                    Column(modifier = Modifier.fillMaxWidth().clickable { onEvent(ScreenEvent.SELECT_ITEM, item) }) {
                        zone.itemLayout!!.slots.forEach { slot ->
                            SlotRenderer(
                                slot = slot,
                                fieldValues = fieldValues,
                                fieldErrors = fieldErrors,
                                onFieldChanged = onFieldChanged,
                                onCustomEvent = onCustomEvent,
                                itemData = item,
                            )
                        }
                    }
                }
            }

            // Standard distribution-based rendering
            else -> {
                when (zone.distribution) {
                    Distribution.STACKED -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                        ) {
                            renderZoneContent(
                                zone = zone,
                                data = data,
                                fieldValues = fieldValues,
                                fieldErrors = fieldErrors,
                                onFieldChanged = onFieldChanged,
                                onCustomEvent = onCustomEvent,
                            )
                        }
                    }

                    Distribution.SIDE_BY_SIDE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                        ) {
                            zone.slots.forEach { slot ->
                                Box(modifier = Modifier.weight(slot.weight ?: 1f)) {
                                    SlotRenderer(
                                        slot = slot,
                                        fieldValues = fieldValues,
                                        fieldErrors = fieldErrors,
                                        onFieldChanged = onFieldChanged,
                                        onCustomEvent = onCustomEvent,
                                    )
                                }
                            }
                            zone.zones.forEach { childZone ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ZoneRenderer(
                                        zone = childZone,
                                        data = data,
                                        fieldValues = fieldValues,
                                        fieldErrors = fieldErrors,
                                        onFieldChanged = onFieldChanged,
                                        onEvent = onEvent,
                                        onCustomEvent = onCustomEvent,
                                        listItemRenderer = listItemRenderer,
                                        maxDepth = maxDepth - 1,
                                    )
                                }
                            }
                        }
                    }

                    Distribution.GRID -> {
                        val columns = if (zone.type == ZoneType.METRIC_GRID) 2 else 2
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val chunked = zone.slots.chunked(columns)
                            chunked.forEach { rowSlots ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                                ) {
                                    rowSlots.forEach { slot ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            SlotRenderer(
                                                slot = slot,
                                                fieldValues = fieldValues,
                                                fieldErrors = fieldErrors,
                                                onFieldChanged = onFieldChanged,
                                                onCustomEvent = onCustomEvent,
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    }
                                    // Fill remaining space if odd number of items
                                    if (rowSlots.size < columns) {
                                        repeat(columns - rowSlots.size) {
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Distribution.FLOW_ROW -> {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                            verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                        ) {
                            zone.slots.forEach { slot ->
                                SlotRenderer(
                                    slot = slot,
                                    fieldValues = fieldValues,
                                    fieldErrors = fieldErrors,
                                    onFieldChanged = onFieldChanged,
                                    onCustomEvent = onCustomEvent,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Render nested zones
        if (zone.type != ZoneType.SIMPLE_LIST && zone.type != ZoneType.GROUPED_LIST) {
            zone.zones.forEach { childZone ->
                ZoneRenderer(
                    zone = childZone,
                    data = data,
                    fieldValues = fieldValues,
                    fieldErrors = fieldErrors,
                    onFieldChanged = onFieldChanged,
                    onEvent = onEvent,
                    onCustomEvent = onCustomEvent,
                    listItemRenderer = listItemRenderer,
                    maxDepth = maxDepth - 1,
                )
            }
        }
    }
}

@Composable
private fun renderZoneContent(
    zone: Zone,
    data: List<JsonObject>,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onCustomEvent: (String, JsonObject?) -> Unit,
) {
    zone.slots.forEach { slot ->
        SlotRenderer(
            slot = slot,
            fieldValues = fieldValues,
            fieldErrors = fieldErrors,
            onFieldChanged = onFieldChanged,
            onCustomEvent = onCustomEvent,
        )
    }
}

internal fun evaluateCondition(condition: String, data: List<JsonObject>): Boolean {
    return when (condition) {
        "data.isEmpty" -> data.isEmpty()
        "!data.isEmpty" -> data.isNotEmpty()
        "data.isNotEmpty" -> data.isNotEmpty()
        else -> {
            // For conditions like "data.summary != null", check if field exists
            if (condition.contains("!= null")) {
                val field = condition.substringBefore("!=").trim()
                    .removePrefix("data.")
                if (data.isNotEmpty()) {
                    resolveFieldExists(field, data.first())
                } else {
                    false
                }
            } else if (condition.contains("== null")) {
                val field = condition.substringBefore("==").trim()
                    .removePrefix("data.")
                if (data.isNotEmpty()) {
                    !resolveFieldExists(field, data.first())
                } else {
                    true
                }
            } else {
                true // Default: show zone
            }
        }
    }
}

private fun resolveFieldExists(field: String, data: JsonObject): Boolean {
    val parts = field.split(".")
    var current: kotlinx.serialization.json.JsonElement = data
    for (part in parts) {
        when (current) {
            is JsonObject -> {
                current = (current as JsonObject)[part] ?: return false
            }
            else -> return false
        }
    }
    return current !is kotlinx.serialization.json.JsonNull
}
