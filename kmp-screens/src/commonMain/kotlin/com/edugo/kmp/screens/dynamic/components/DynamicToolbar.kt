package com.edugo.kmp.screens.dynamic.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Standard toolbar for SDUI dynamic screens.
 *
 * Renders a Material3 TopAppBar with contextual actions based on the screen pattern:
 * - LIST: title + "Nuevo" create button
 * - FORM: back arrow + title (edit/create) + "Guardar" save button
 * - DETAIL: back arrow + title
 * - DASHBOARD/SETTINGS: title only
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicToolbar(
    screen: ScreenDefinition,
    isEditMode: Boolean,
    canCreate: Boolean,
    onBack: (() -> Unit)?,
    onEvent: (ScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val slotData = screen.slotData
    val pattern = screen.pattern

    val title = resolveTitle(slotData, pattern, isEditMode)

    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            when (pattern) {
                ScreenPattern.FORM, ScreenPattern.DETAIL -> {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                            )
                        }
                    }
                }
                else -> { /* No navigation icon for list/dashboard/settings */ }
            }
        },
        actions = {
            when (pattern) {
                ScreenPattern.LIST -> {
                    if (canCreate) {
                        IconButton(onClick = { onEvent(ScreenEvent.CREATE) }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Nuevo",
                            )
                        }
                    }
                }
                ScreenPattern.FORM -> {
                    FilledTonalButton(
                        onClick = {
                            onEvent(
                                if (isEditMode) ScreenEvent.SAVE_EXISTING
                                else ScreenEvent.SAVE_NEW
                            )
                        },
                    ) {
                        Text("Guardar")
                    }
                }
                else -> { /* No actions for detail/dashboard/settings */ }
            }
        },
    )
}

/**
 * Resolves the toolbar title from slot_data based on screen pattern and edit mode.
 */
private fun resolveTitle(
    slotData: JsonObject?,
    pattern: ScreenPattern,
    isEditMode: Boolean,
): String {
    if (slotData == null) return ""

    return when (pattern) {
        ScreenPattern.FORM -> {
            if (isEditMode) {
                slotData["edit_title"]?.jsonPrimitive?.content
                    ?: slotData["page_title"]?.let { "Editar ${it.jsonPrimitive.content}" }
                    ?: "Editar"
            } else {
                slotData["page_title"]?.jsonPrimitive?.content ?: "Nuevo"
            }
        }
        else -> slotData["page_title"]?.jsonPrimitive?.content ?: ""
    }
}
