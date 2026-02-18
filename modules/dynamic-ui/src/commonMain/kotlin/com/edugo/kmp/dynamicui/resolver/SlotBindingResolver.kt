package com.edugo.kmp.dynamicui.resolver

import com.edugo.kmp.dynamicui.model.ItemLayout
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.model.Slot
import com.edugo.kmp.dynamicui.model.Zone
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Resuelve bindings de slot_data en una ScreenDefinition.
 *
 * Los slots del template pueden tener un campo `bind` con formato "slot:key_name".
 * El `slotData` del instance contiene los valores reales: { "key_name": "Dark Mode" }.
 * Este resolver recorre todos los slots y popula `label`/`value` segun corresponda.
 */
object SlotBindingResolver {

    private const val SLOT_PREFIX = "slot:"

    /**
     * Resuelve una ScreenDefinition completa, retornando una copia con todos los
     * bindings de slotData aplicados a los slots del template.
     */
    fun resolve(screen: ScreenDefinition): ScreenDefinition {
        val slotData = screen.slotData ?: return screen
        val resolvedTemplate = resolveTemplate(screen.template, slotData)
        if (resolvedTemplate === screen.template) return screen
        return screen.copy(template = resolvedTemplate)
    }

    private fun resolveTemplate(template: ScreenTemplate, slotData: JsonObject): ScreenTemplate {
        val newZones = template.zones.mapIfChanged { resolveZone(it, slotData) }
        if (newZones === template.zones) return template
        return template.copy(zones = newZones)
    }

    private fun resolveZone(zone: Zone, slotData: JsonObject): Zone {
        val newSlots = zone.slots.mapIfChanged { resolveSlot(it, slotData) }
        val newZones = zone.zones.mapIfChanged { resolveZone(it, slotData) }
        val newItemLayout = zone.itemLayout?.let { resolveItemLayout(it, slotData) }
        if (newSlots === zone.slots && newZones === zone.zones && newItemLayout === zone.itemLayout) return zone
        return zone.copy(slots = newSlots, zones = newZones, itemLayout = newItemLayout)
    }

    private fun resolveItemLayout(itemLayout: ItemLayout, slotData: JsonObject): ItemLayout {
        val newSlots = itemLayout.slots.mapIfChanged { resolveSlot(it, slotData) }
        if (newSlots === itemLayout.slots) return itemLayout
        return itemLayout.copy(slots = newSlots)
    }

    private fun resolveSlot(slot: Slot, slotData: JsonObject): Slot {
        val bindKey = slot.bind ?: return slot
        if (!bindKey.startsWith(SLOT_PREFIX)) return slot

        val dataKey = bindKey.removePrefix(SLOT_PREFIX)
        val resolvedValue = lookupSlotData(dataKey, slotData) ?: return slot

        // Para controles que usan label (switch, checkbox, list-item, metric-card, etc.)
        // el binding resuelve a label. Para controles de texto/boton resuelve a value.
        return when {
            slot.label != null -> slot // Ya tiene label explicito, no sobreescribir
            slot.controlType.usesLabel -> slot.copy(label = resolvedValue)
            slot.value != null -> slot // Ya tiene value explicito
            else -> slot.copy(value = resolvedValue)
        }
    }

    private fun lookupSlotData(key: String, slotData: JsonObject): String? {
        val element = slotData[key] ?: return null
        return when (element) {
            is JsonPrimitive -> element.contentOrNull ?: element.toString()
            else -> element.toString()
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
