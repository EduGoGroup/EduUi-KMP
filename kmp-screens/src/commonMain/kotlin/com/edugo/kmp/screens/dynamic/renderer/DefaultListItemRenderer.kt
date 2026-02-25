package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ItemLayout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun DefaultListItemRenderer(
    item: JsonObject,
    itemLayout: ItemLayout?,
    onEvent: (ScreenEvent, JsonObject?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = item.extractDisplayValue("title")
        ?: item.extractDisplayValue("name")
        ?: item.extractDisplayValue("full_name")
        ?: ""
    val subtitle = item.extractDisplayValue("subtitle")
        ?: item.extractDisplayValue("description")
    val status = item.extractDisplayValue("status")
    val createdAt = item.extractDisplayValue("created_at")
        ?: item.extractDisplayValue("date")

    val initials = title.take(2).uppercase().ifEmpty { "?" }

    val formattedStatus = status?.let { DisplayValueFormatter.autoFormat(it) }
    val formattedDate = createdAt?.let { DisplayValueFormatter.autoFormat(it) }

    val supportingParts = listOfNotNull(
        subtitle,
        formattedDate,
    )
    val supportingText = supportingParts.joinToString(" · ").ifEmpty { null }

    DSListItem(
        headlineText = DisplayValueFormatter.autoFormat(title),
        supportingText = supportingText,
        leadingContent = {
            DSAvatar(initials = initials)
        },
        trailingContent = {
            if (formattedStatus != null) {
                DSChip(label = formattedStatus)
            } else {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        },
        onClick = { onEvent(ScreenEvent.SELECT_ITEM, item) },
        modifier = modifier,
    )
}

private fun JsonObject.extractDisplayValue(key: String): String? {
    val element = this[key] ?: return null
    if (element is kotlinx.serialization.json.JsonNull) return null
    return try {
        val content = element.jsonPrimitive.contentOrNull
        content?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}
