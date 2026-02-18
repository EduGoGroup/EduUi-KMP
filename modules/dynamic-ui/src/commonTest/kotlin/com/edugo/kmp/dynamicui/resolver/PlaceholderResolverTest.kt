package com.edugo.kmp.dynamicui.resolver

import com.edugo.kmp.dynamicui.model.ControlType
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.model.Slot
import com.edugo.kmp.dynamicui.model.Zone
import com.edugo.kmp.dynamicui.model.ZoneType
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderResolverTest {

    private fun buildScreen(
        zones: List<Zone>
    ) = ScreenDefinition(
        screenId = "test-id",
        screenKey = "test-screen",
        screenName = "Test Screen",
        pattern = ScreenPattern.SETTINGS,
        version = 1,
        template = ScreenTemplate(zones = zones),
        updatedAt = "2026-01-01T00:00:00Z"
    )

    @Test
    fun resolves_user_placeholder_in_label() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "greeting",
                            controlType = ControlType.LABEL,
                            label = "Welcome back {user.firstName}"
                        )
                    )
                )
            )
        )

        val placeholders = mapOf("user.firstName" to "John")
        val resolved = PlaceholderResolver.resolve(screen, placeholders)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("Welcome back John", slot.label)
    }

    @Test
    fun resolves_placeholder_in_value() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "date",
                            controlType = ControlType.LABEL,
                            value = "{today_date}"
                        )
                    )
                )
            )
        )

        val placeholders = mapOf("today_date" to "February 15 2026")
        val resolved = PlaceholderResolver.resolve(screen, placeholders)
        val slot = resolved.template.zones[0].slots[0]

        assertEquals("February 15 2026", slot.value)
    }

    @Test
    fun resolves_multiple_placeholders_in_same_text() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "info",
                            controlType = ControlType.LABEL,
                            value = "Hello {user.firstName} {user.lastName}"
                        )
                    )
                )
            )
        )

        val placeholders = mapOf(
            "user.firstName" to "John",
            "user.lastName" to "Doe"
        )
        val resolved = PlaceholderResolver.resolve(screen, placeholders)

        assertEquals("Hello John Doe", resolved.template.zones[0].slots[0].value)
    }

    @Test
    fun leaves_unknown_placeholders_unchanged() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "s1",
                            controlType = ControlType.LABEL,
                            value = "Hi {unknown_key}"
                        )
                    )
                )
            )
        )

        val resolved = PlaceholderResolver.resolve(screen, mapOf("user.firstName" to "John"))
        assertEquals("Hi {unknown_key}", resolved.template.zones[0].slots[0].value)
    }

    @Test
    fun returns_unchanged_when_no_placeholders_map() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "s1",
                            controlType = ControlType.LABEL,
                            value = "{user.firstName}"
                        )
                    )
                )
            )
        )

        val resolved = PlaceholderResolver.resolve(screen, emptyMap())
        assertEquals("{user.firstName}", resolved.template.zones[0].slots[0].value)
    }

    @Test
    fun does_not_modify_text_without_placeholders() {
        val screen = buildScreen(
            zones = listOf(
                Zone(
                    id = "z1",
                    type = ZoneType.CONTAINER,
                    slots = listOf(
                        Slot(
                            id = "s1",
                            controlType = ControlType.LABEL,
                            label = "Normal text without braces"
                        )
                    )
                )
            )
        )

        val resolved = PlaceholderResolver.resolve(screen, mapOf("user.firstName" to "John"))
        assertEquals("Normal text without braces", resolved.template.zones[0].slots[0].label)
    }

    @Test
    fun replacePlaceholders_handles_basic_cases() {
        assertEquals("Hello John", PlaceholderResolver.replacePlaceholders("Hello {name}", mapOf("name" to "John")))
        assertEquals("No placeholders", PlaceholderResolver.replacePlaceholders("No placeholders", mapOf("name" to "John")))
        assertEquals("{missing}", PlaceholderResolver.replacePlaceholders("{missing}", emptyMap()))
    }
}
