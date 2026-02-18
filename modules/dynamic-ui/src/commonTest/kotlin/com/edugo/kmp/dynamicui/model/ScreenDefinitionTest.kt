package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

        assertNull(screen.dataEndpoint)
        assertNull(screen.dataConfig)
        assertEquals(emptyList(), screen.actions)
        assertNull(screen.userPreferences)
    }

    @Test
    fun actionDefinition_serializes_with_config() {
        val action = ActionDefinition(
            id = "act-1",
            trigger = ActionTrigger.BUTTON_CLICK,
            triggerSlotId = "btn-submit",
            type = ActionType.SUBMIT_FORM,
            config = JsonObject(mapOf("endpoint" to JsonPrimitive("/api/submit")))
        )

        val encoded = json.encodeToString(action)
        val decoded = json.decodeFromString<ActionDefinition>(encoded)

        assertEquals("act-1", decoded.id)
        assertEquals(ActionTrigger.BUTTON_CLICK, decoded.trigger)
        assertEquals(ActionType.SUBMIT_FORM, decoded.type)
        assertEquals("btn-submit", decoded.triggerSlotId)
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
            "screenId": "scr-100",
            "screenKey": "dashboard",
            "screenName": "Dashboard",
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
            "dataEndpoint": "/v1/dashboard/data",
            "dataConfig": {
                "defaultParams": {},
                "refreshInterval": 30000
            },
            "actions": [],
            "updatedAt": "2026-02-01T12:00:00Z"
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
        assertEquals("/v1/dashboard/data", screen.dataEndpoint)
    }
}
