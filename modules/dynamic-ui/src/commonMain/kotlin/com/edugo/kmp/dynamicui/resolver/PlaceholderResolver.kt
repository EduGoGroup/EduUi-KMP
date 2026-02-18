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
        if (resolvedTemplate === screen.template) return screen
        return screen.copy(template = resolvedTemplate)
    }

    private fun resolveTemplate(template: ScreenTemplate, placeholders: Map<String, String>): ScreenTemplate {
        val newZones = template.zones.mapIfChanged { resolveZone(it, placeholders) }
        if (newZones === template.zones) return template
        return template.copy(zones = newZones)
    }

    private fun resolveZone(zone: Zone, placeholders: Map<String, String>): Zone {
        val newSlots = zone.slots.mapIfChanged { resolveSlot(it, placeholders) }
        val newZones = zone.zones.mapIfChanged { resolveZone(it, placeholders) }
        val newItemLayout = zone.itemLayout?.let { resolveItemLayout(it, placeholders) }
        if (newSlots === zone.slots && newZones === zone.zones && newItemLayout === zone.itemLayout) return zone
        return zone.copy(slots = newSlots, zones = newZones, itemLayout = newItemLayout)
    }

    private fun resolveItemLayout(itemLayout: ItemLayout, placeholders: Map<String, String>): ItemLayout {
        val newSlots = itemLayout.slots.mapIfChanged { resolveSlot(it, placeholders) }
        if (newSlots === itemLayout.slots) return itemLayout
        return itemLayout.copy(slots = newSlots)
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

    private inline fun <T> List<T>.mapIfChanged(transform: (T) -> T): List<T> {
        var changed = false
        val result = map { item ->
            val newItem = transform(item)
            if (newItem !== item) changed = true
            newItem
        }
        return if (changed) result else this
    }
}
