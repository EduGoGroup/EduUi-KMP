package com.edugo.kmp.dynamicui.resolver

import com.edugo.kmp.dynamicui.model.ItemLayout
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.model.Slot
import com.edugo.kmp.dynamicui.model.Zone

/**
 * Resuelve placeholders de tipo {key} en los campos de texto de los slots.
 *
 * Los valores de slotData pueden contener placeholders como:
 * - {user.firstName} -> nombre del usuario autenticado
 * - {today_date} -> fecha actual formateada
 * - {context.roleName} -> rol activo del usuario
 *
 * Este resolver recorre todos los slots y reemplaza los placeholders
 * con los valores reales proporcionados en el mapa de placeholders.
 */
object PlaceholderResolver {

    private val PLACEHOLDER_REGEX = Regex("""\{([^}]+)\}""")

    /**
     * Resuelve una ScreenDefinition completa, reemplazando todos los
     * placeholders {key} en label y value de los slots.
     *
     * @param screen La pantalla con slots ya resueltos por SlotBindingResolver
     * @param placeholders Mapa de clave -> valor (ej: "user.firstName" -> "John")
     */
    fun resolve(screen: ScreenDefinition, placeholders: Map<String, String>): ScreenDefinition {
        if (placeholders.isEmpty()) return screen
        val resolvedTemplate = resolveTemplate(screen.template, placeholders)
        return screen.copy(template = resolvedTemplate)
    }

    private fun resolveTemplate(template: ScreenTemplate, placeholders: Map<String, String>): ScreenTemplate {
        return template.copy(
            zones = template.zones.map { resolveZone(it, placeholders) }
        )
    }

    private fun resolveZone(zone: Zone, placeholders: Map<String, String>): Zone {
        return zone.copy(
            slots = zone.slots.map { resolveSlot(it, placeholders) },
            zones = zone.zones.map { resolveZone(it, placeholders) },
            itemLayout = zone.itemLayout?.let { resolveItemLayout(it, placeholders) }
        )
    }

    private fun resolveItemLayout(itemLayout: ItemLayout, placeholders: Map<String, String>): ItemLayout {
        return itemLayout.copy(
            slots = itemLayout.slots.map { resolveSlot(it, placeholders) }
        )
    }

    private fun resolveSlot(slot: Slot, placeholders: Map<String, String>): Slot {
        val newLabel = slot.label?.let { replacePlaceholders(it, placeholders) }
        val newValue = slot.value?.let { replacePlaceholders(it, placeholders) }

        if (newLabel == slot.label && newValue == slot.value) return slot
        return slot.copy(label = newLabel, value = newValue)
    }

    internal fun replacePlaceholders(text: String, placeholders: Map<String, String>): String {
        if (!text.contains('{')) return text
        return PLACEHOLDER_REGEX.replace(text) { match ->
            val key = match.groupValues[1]
            placeholders[key] ?: match.value
        }
    }
}
