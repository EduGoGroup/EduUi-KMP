package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScreenDefinitionTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun screenPattern_serializes_with_serial_name() {
        val encoded = json.encodeToString(ScreenPattern.LOGIN)
        assertEquals("\"login\"", encoded)
    }

    @Test
    fun screenPattern_deserializes_from_serial_name() {
        val decoded = json.decodeFromString<ScreenPattern>("\"dashboard\"")
        assertEquals(ScreenPattern.DASHBOARD, decoded)
    }

    @Test
    fun screenDefinition_serializes_and_deserializes_correctly() {
        val screen = ScreenDefinition(
            screenId = "scr-001",
            screenKey = "login",
            screenName = "Login Screen",
            pattern = ScreenPattern.LOGIN,
            version = 1,
            template = ScreenTemplate(
                zones = listOf(
                    Zone(
                        id = "zone-1",
                        type = ZoneType.FORM_SECTION,
                        slots = listOf(
                            Slot(
                                id = "email",
                                controlType = ControlType.EMAIL_INPUT,
                                label = "Email",
                                required = true
                            )
                        )
                    )
                )
            ),
            updatedAt = "2026-01-01T00:00:00Z"
        )

        val encoded = json.encodeToString(screen)
        val decoded = json.decodeFromString<ScreenDefinition>(encoded)

        assertEquals(screen.screenId, decoded.screenId)
        assertEquals(screen.screenKey, decoded.screenKey)
        assertEquals(screen.pattern, decoded.pattern)
        assertEquals(screen.version, decoded.version)
        assertEquals(1, decoded.template.zones.size)
        assertEquals(1, decoded.template.zones[0].slots.size)
        assertEquals(ControlType.EMAIL_INPUT, decoded.template.zones[0].slots[0].controlType)
    }

    @Test
    fun screenDefinition_with_optional_fields_defaults_correctly() {
        val screen = ScreenDefinition(
            screenId = "scr-002",
            screenKey = "settings",
            screenName = "Settings",
            pattern = ScreenPattern.SETTINGS,
            version = 1,
            template = ScreenTemplate(zones = emptyList()),
            updatedAt = "2026-01-01T00:00:00Z"
        )

        assertNull(screen.dataConfig)
        assertNull(screen.userPreferences)
    }

    @Test
    fun controlType_enum_covers_all_expected_types() {
        val allTypes = ControlType.entries
        assertEquals(23, allTypes.size)
    }

    @Test
    fun zoneType_enum_serializes_correctly() {
        val encoded = json.encodeToString(ZoneType.FORM_SECTION)
        assertEquals("\"form-section\"", encoded)
    }

    @Test
    fun distribution_enum_serializes_correctly() {
        val encoded = json.encodeToString(Distribution.SIDE_BY_SIDE)
        assertEquals("\"side-by-side\"", encoded)
    }

    @Test
    fun dataConfig_with_pagination_serializes() {
        val config = DataConfig(
            defaultParams = mapOf("status" to "active"),
            pagination = PaginationConfig(pageSize = 10)
        )

        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<DataConfig>(encoded)

        assertEquals("active", decoded.defaultParams["status"])
        assertEquals(10, decoded.pagination?.pageSize)
    }

    @Test
    fun navigationConfig_serializes_correctly() {
        val nav = NavigationConfig(
            type = NavigationType.BOTTOM_NAV,
            title = "Home",
            showBack = false
        )

        val encoded = json.encodeToString(nav)
        val decoded = json.decodeFromString<NavigationConfig>(encoded)

        assertEquals(NavigationType.BOTTOM_NAV, decoded.type)
        assertEquals("Home", decoded.title)
        assertEquals(false, decoded.showBack)
    }

    @Test
    fun full_screen_definition_json_round_trip() {
        val screenJson = """
        {
            "screen_id": "scr-100",
            "screen_key": "dashboard",
            "screen_name": "Dashboard",
            "pattern": "dashboard",
            "version": 2,
            "template": {
                "navigation": {
                    "type": "top-bar",
                    "title": "Dashboard",
                    "showBack": false,
                    "actions": []
                },
                "zones": [
                    {
                        "id": "metrics",
                        "type": "metric-grid",
                        "distribution": "grid",
                        "slots": [
                            {
                                "id": "total-students",
                                "controlType": "metric-card",
                                "label": "Total Students",
                                "field": "totalStudents"
                            }
                        ]
                    }
                ]
            },
            "data_config": {
                "defaultParams": {},
                "refreshInterval": 30000
            },
            "updated_at": "2026-02-01T12:00:00Z"
        }
        """.trimIndent()

        val screen = json.decodeFromString<ScreenDefinition>(screenJson)

        assertEquals("scr-100", screen.screenId)
        assertEquals("dashboard", screen.screenKey)
        assertEquals(ScreenPattern.DASHBOARD, screen.pattern)
        assertEquals(2, screen.version)
        assertEquals("Dashboard", screen.template.navigation?.title)
        assertEquals(1, screen.template.zones.size)
        assertEquals(ZoneType.METRIC_GRID, screen.template.zones[0].type)
        assertEquals(Distribution.GRID, screen.template.zones[0].distribution)
    }

    @Test
    fun slot_with_eventId_serializes_correctly() {
        val slot = Slot(
            id = "btn-submit",
            controlType = ControlType.FILLED_BUTTON,
            value = "Submit",
            eventId = "submit-login"
        )

        val encoded = json.encodeToString(slot)
        val decoded = json.decodeFromString<Slot>(encoded)

        assertEquals("btn-submit", decoded.id)
        assertEquals("submit-login", decoded.eventId)
    }

    @Test
    fun slot_without_eventId_defaults_to_null() {
        val slot = Slot(
            id = "label-1",
            controlType = ControlType.LABEL,
            value = "Hello"
        )

        assertNull(slot.eventId)
    }
}
