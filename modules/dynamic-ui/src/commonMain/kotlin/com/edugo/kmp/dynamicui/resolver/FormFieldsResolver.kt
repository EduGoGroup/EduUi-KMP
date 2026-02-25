package com.edugo.kmp.dynamicui.resolver

import com.edugo.kmp.dynamicui.model.ControlType
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.Slot
import com.edugo.kmp.dynamicui.model.ZoneType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Converts slot_data.fields into dynamic Slot entries for form-section zones.
 *
 * The form template has an empty form-section zone. The actual fields come
 * from slot_data.fields as an array of field definitions:
 *   { "key": "name", "type": "text", "label": "Nombre", "placeholder": "...", "required": true }
 *
 * This resolver generates Slot objects from those definitions and injects
 * them into the form-section zone.
 */
object FormFieldsResolver {

    fun resolve(screen: ScreenDefinition): ScreenDefinition {
        val slotData = screen.slotData ?: return screen
        val fieldsElement = slotData["fields"] ?: return screen
        if (fieldsElement !is JsonArray) return screen

        val dynamicSlots = fieldsElement.mapNotNull { element ->
            if (element !is JsonObject) return@mapNotNull null
            val obj = element.jsonObject
            val key = obj["key"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "text"
            val label = obj["label"]?.jsonPrimitive?.contentOrNull
            val placeholder = obj["placeholder"]?.jsonPrimitive?.contentOrNull
            val required = obj["required"]?.jsonPrimitive?.booleanOrNull ?: false

            val controlType = when (type) {
                "text", "textarea" -> ControlType.TEXT_INPUT
                "email" -> ControlType.EMAIL_INPUT
                "password" -> ControlType.PASSWORD_INPUT
                "number" -> ControlType.NUMBER_INPUT
                "toggle", "boolean" -> ControlType.SWITCH
                "checkbox" -> ControlType.CHECKBOX
                "select" -> ControlType.SELECT
                else -> ControlType.TEXT_INPUT
            }

            Slot(
                id = key,
                controlType = controlType,
                label = label,
                placeholder = placeholder,
                required = required,
                field = key
            )
        }

        if (dynamicSlots.isEmpty()) return screen

        val newZones = screen.template.zones.map { zone ->
            if (zone.type == ZoneType.FORM_SECTION && zone.slots.isEmpty()) {
                zone.copy(slots = dynamicSlots)
            } else {
                zone
            }
        }

        return screen.copy(template = screen.template.copy(zones = newZones))
    }
}
