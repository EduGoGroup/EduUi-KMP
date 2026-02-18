package com.edugo.kmp.dynamicui.resolver

import com.edugo.kmp.dynamicui.model.ControlType
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.Distribution
import com.edugo.kmp.dynamicui.model.ItemLayout
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.model.Slot
import com.edugo.kmp.dynamicui.model.Zone
import com.edugo.kmp.dynamicui.model.ZoneType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SlotBindingResolverTest {

    private fun buildScreen(
        zones: List<Zone>,
        slotData: JsonObject? = null
    ) = ScreenDefinition(
        screenId = "test-id",
        screenKey = "test-screen",
        screenName = "Test Screen",
        pattern = ScreenPattern.SETTINGS,
        version = 1,
        template = ScreenTemplate(zones = zones),
        slotData = slotData,
        updatedAt = "2026-01-01T00:00:00Z"
    )

    @Test
    fun resolves_switch_label_from_slotData() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "dark_mode",
                            controlType = ControlType.SWITCH,
                            bind = "slot:dark_mode_label"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("dark_mode_label" to JsonPrimitive("Dark Mode")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Dark Mode", slot.label)
    }

    @Test
    fun resolves_label_control_to_value() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "greeting",
                            controlType = ControlType.LABEL,
                            bind = "slot:greeting_text"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("greeting_text" to JsonPrimitive("Hello World")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Hello World", slot.value)
        assertNull(slot.label)
    }

    @Test
    fun resolves_button_to_value() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "logout_btn",
                            controlType = ControlType.FILLED_BUTTON,
                            bind = "slot:logout_label"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("logout_label" to JsonPrimitive("Sign Out")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Sign Out", slot.value)
    }

    @Test
    fun does_not_overwrite_explicit_label() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "my_switch",
                            controlType = ControlType.SWITCH,
                            bind = "slot:switch_label",
                            label = "Explicit Label"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("switch_label" to JsonPrimitive("From SlotData")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Explicit Label", slot.label)
    }

    @Test
    fun returns_unchanged_when_no_slotData() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "s1",
                            controlType = ControlType.SWITCH,
                            bind = "slot:some_label"
                        )
                    )
                )
            ),
            slotData = null
        )

        val resolved = SlotBindingResolver.resolve(screen)
        assertNull(resolved.template.zones[0].slots[0].label)
    }

    @Test
    fun resolves_nested_zones() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "outer",
                    type = ZoneType.CONTAINER,
                    zones = listOf(
                        Zone(
                            id = "inner",
                            type = ZoneType.FORM_SECTION,
                            slots = listOf(
                                Slot(
                                    id = "nested_switch",
                                    controlType = ControlType.CHECKBOX,
                                    bind = "slot:nested_label"
                                )
                            )
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("nested_label" to JsonPrimitive("Nested!")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].zones[0].slots[0]

        assertEquals("Nested!", slot.label)
    }

    @Test
    fun resolves_itemLayout_slots() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "list",
                    type = ZoneType.SIMPLE_LIST,
                    itemLayout = ItemLayout(
                        slots = listOf(
                            Slot(
                                id = "item_label",
                                controlType = ControlType.LABEL,
                                bind = "slot:item_text"
                            )
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("item_text" to JsonPrimitive("Item Value")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].itemLayout!!.slots[0]

        assertEquals("Item Value", slot.value)
    }

    @Test
    fun ignores_bind_without_slot_prefix() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "s1",
                            controlType = ControlType.SWITCH,
                            bind = "unknown:some_key"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("some_key" to JsonPrimitive("Value")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        assertNull(resolved.template.zones[0].slots[0].label)
    }

    @Test
    fun resolves_metric_card_label() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "kpis",
                    type = ZoneType.METRIC_GRID,
                    slots = listOf(
                        Slot(
                            id = "total_students",
                            controlType = ControlType.METRIC_CARD,
                            bind = "slot:kpi_students_label",
                            field = "total_students"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("kpi_students_label" to JsonPrimitive("Students")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Students", slot.label)
        assertEquals("total_students", slot.field)
    }

    @Test
    fun resolves_list_item_navigation_label() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.FORM_SECTION,
                    slots = listOf(
                        Slot(
                            id = "theme_color",
                            controlType = ControlType.LIST_ITEM_NAVIGATION,
                            bind = "slot:theme_label"
                        )
                    )
                )
            ),
            slotData = JsonObject(mapOf("theme_label" to JsonPrimitive("Theme Color")))
        )

        val resolved = SlotBindingResolver.resolve(screen)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Theme Color", slot.label)
    }
}
