package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.buttons.DSTextButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.inputs.DSPasswordField
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSCheckbox
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.design.components.selection.DSChipVariant
import com.edugo.kmp.design.components.selection.DSSwitch
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ControlType
import com.edugo.kmp.dynamicui.model.Slot
import kotlinx.serialization.json.JsonObject

@Composable
fun SlotRenderer(
    slot: Slot,
    actions: List<ActionDefinition>,
    fieldValues: Map<String, String>,
    fieldErrors: Map<String, String>,
    onFieldChanged: (String, String) -> Unit,
    onAction: (ActionDefinition, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
    itemData: JsonObject? = null,
) {
    val displayValue = resolveSlotValue(slot, fieldValues, itemData)

    when (slot.controlType) {
        ControlType.LABEL -> {
            Text(
                text = displayValue,
                style = mapTextStyle(slot.style),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = modifier,
            )
        }

        ControlType.TEXT_INPUT -> {
            DSOutlinedTextField(
                value = fieldValues[slot.id] ?: "",
                onValueChange = { onFieldChanged(slot.id, it) },
                label = slot.label,
                placeholder = slot.placeholder,
                supportingText = fieldErrors[slot.id],
                isError = fieldErrors.containsKey(slot.id),
                readOnly = slot.readOnly,
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.EMAIL_INPUT -> {
            DSOutlinedTextField(
                value = fieldValues[slot.id] ?: "",
                onValueChange = { onFieldChanged(slot.id, it) },
                label = slot.label,
                placeholder = slot.placeholder,
                supportingText = fieldErrors[slot.id],
                isError = fieldErrors.containsKey(slot.id),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.PASSWORD_INPUT -> {
            DSPasswordField(
                value = fieldValues[slot.id] ?: "",
                onValueChange = { onFieldChanged(slot.id, it) },
                label = slot.label,
                placeholder = slot.placeholder,
                supportingText = fieldErrors[slot.id],
                isError = fieldErrors.containsKey(slot.id),
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.NUMBER_INPUT -> {
            DSOutlinedTextField(
                value = fieldValues[slot.id] ?: "",
                onValueChange = { onFieldChanged(slot.id, it) },
                label = slot.label,
                placeholder = slot.placeholder,
                supportingText = fieldErrors[slot.id],
                isError = fieldErrors.containsKey(slot.id),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.SEARCH_BAR -> {
            DSOutlinedTextField(
                value = fieldValues[slot.id] ?: "",
                onValueChange = { onFieldChanged(slot.id, it) },
                placeholder = slot.placeholder,
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.FILLED_BUTTON -> {
            val action = findActionForSlot(slot.id, actions)
            DSFilledButton(
                text = displayValue,
                onClick = { action?.let { onAction(it, itemData) } },
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.OUTLINED_BUTTON -> {
            val action = findActionForSlot(slot.id, actions)
            DSOutlinedButton(
                text = displayValue,
                onClick = { action?.let { onAction(it, itemData) } },
                modifier = modifier,
            )
        }

        ControlType.TEXT_BUTTON -> {
            val action = findActionForSlot(slot.id, actions)
            DSTextButton(
                text = displayValue,
                onClick = { action?.let { onAction(it, itemData) } },
                modifier = modifier,
            )
        }

        ControlType.ICON_BUTTON -> {
            val action = findActionForSlot(slot.id, actions)
            DSIconButton(
                icon = Icons.Default.Star,
                contentDescription = slot.label,
                onClick = { action?.let { onAction(it, itemData) } },
                modifier = modifier,
            )
        }

        ControlType.ICON -> {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = displayValue,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier,
            )
        }

        ControlType.AVATAR -> {
            DSAvatar(
                initials = displayValue.take(2),
                modifier = modifier,
            )
        }

        ControlType.SWITCH -> {
            DSSwitch(
                checked = fieldValues[slot.id]?.toBoolean() ?: false,
                onCheckedChange = { onFieldChanged(slot.id, it.toString()) },
                label = slot.label,
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.CHECKBOX -> {
            DSCheckbox(
                checked = fieldValues[slot.id]?.toBoolean() ?: false,
                onCheckedChange = { onFieldChanged(slot.id, it.toString()) },
                label = slot.label,
                modifier = modifier.fillMaxWidth(),
            )
        }

        ControlType.LIST_ITEM -> {
            DSListItem(
                headlineText = displayValue,
                supportingText = slot.label,
                modifier = modifier,
            )
        }

        ControlType.LIST_ITEM_NAVIGATION -> {
            val action = findActionForSlot(slot.id, actions)
            DSListItem(
                headlineText = slot.label ?: displayValue,
                supportingText = if (slot.label != null && displayValue.isNotEmpty()) displayValue else null,
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = { action?.let { onAction(it, itemData) } },
                modifier = modifier,
            )
        }

        ControlType.METRIC_CARD -> {
            DSElevatedCard(modifier = modifier) {
                Text(
                    text = slot.label ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        ControlType.CHIP -> {
            DSChip(
                label = displayValue,
                variant = DSChipVariant.FILTER,
                selected = fieldValues[slot.id]?.toBoolean() ?: false,
                onClick = {
                    val current = fieldValues[slot.id]?.toBoolean() ?: false
                    onFieldChanged(slot.id, (!current).toString())
                },
                modifier = modifier,
            )
        }

        ControlType.DIVIDER -> {
            DSDivider(modifier = modifier)
        }

        ControlType.IMAGE -> {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = displayValue,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier,
            )
        }

        ControlType.RADIO_GROUP, ControlType.SELECT, ControlType.RATING -> {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun mapTextStyle(style: String?) = when (style) {
    "headline-large" -> MaterialTheme.typography.headlineLarge
    "headline-medium" -> MaterialTheme.typography.headlineMedium
    "headline-small" -> MaterialTheme.typography.headlineSmall
    "headline" -> MaterialTheme.typography.headlineSmall
    "title-large" -> MaterialTheme.typography.titleLarge
    "title-medium" -> MaterialTheme.typography.titleMedium
    "title-small" -> MaterialTheme.typography.titleSmall
    "body-large" -> MaterialTheme.typography.bodyLarge
    "body" -> MaterialTheme.typography.bodyMedium
    "body-small" -> MaterialTheme.typography.bodySmall
    "label-large" -> MaterialTheme.typography.labelLarge
    "label" -> MaterialTheme.typography.labelMedium
    "caption" -> MaterialTheme.typography.labelSmall
    else -> MaterialTheme.typography.bodyMedium
}

private fun resolveSlotValue(
    slot: Slot,
    fieldValues: Map<String, String>,
    itemData: JsonObject?,
): String {
    val fieldBinding = slot.field

    // 1. If there is a field binding and item data, resolve from item data
    if (fieldBinding != null && itemData != null) {
        val fieldValue = resolveFieldFromJson(fieldBinding, itemData)
        if (fieldValue != null) return fieldValue
    }

    // 2. If there is a field binding and field values, resolve from field values
    if (fieldBinding != null && fieldValues.containsKey(fieldBinding)) {
        return fieldValues[fieldBinding] ?: ""
    }

    // 3. Static value (no fallback to label to avoid duplication)
    return slot.value ?: ""
}

private fun resolveFieldFromJson(field: String, data: JsonObject): String? {
    val parts = field.split(".")
    var current: kotlinx.serialization.json.JsonElement = data
    for (part in parts) {
        when (current) {
            is JsonObject -> {
                current = (current as JsonObject)[part] ?: return null
            }
            else -> return null
        }
    }
    return when (current) {
        is kotlinx.serialization.json.JsonPrimitive -> {
            val primitive = current as kotlinx.serialization.json.JsonPrimitive
            if (primitive.isString) primitive.content else primitive.toString()
        }
        else -> current.toString()
    }
}

private fun findActionForSlot(
    slotId: String,
    actions: List<ActionDefinition>,
): ActionDefinition? {
    return actions.find { it.triggerSlotId == slotId }
}
