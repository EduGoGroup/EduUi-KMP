package com.edugo.kmp.dynamicui.contract

import com.edugo.kmp.dynamicui.model.DataConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScreenContractRegistryTest {

    private fun createContract(key: String, res: String = "test"): ScreenContract {
        return object : ScreenContract {
            override val screenKey = key
            override val resource = res
            override fun endpointFor(event: ScreenEvent, context: EventContext): String? = "/api/v1/$res"
        }
    }

    @Test
    fun find_returns_contract_for_registered_key() {
        val contract = createContract("test-screen")
        val registry = ScreenContractRegistry(listOf(contract))

        val found = registry.find("test-screen")
        assertNotNull(found)
        assertEquals("test-screen", found.screenKey)
    }

    @Test
    fun find_returns_null_for_unregistered_key() {
        val registry = ScreenContractRegistry(listOf(createContract("existing")))

        assertNull(registry.find("missing"))
    }

    @Test
    fun has_returns_true_for_registered_key() {
        val registry = ScreenContractRegistry(listOf(createContract("my-screen")))

        assertTrue(registry.has("my-screen"))
    }

    @Test
    fun has_returns_false_for_missing_key() {
        val registry = ScreenContractRegistry(emptyList())

        assertFalse(registry.has("any-key"))
    }

    @Test
    fun allKeys_returns_all_registered_keys() {
        val registry = ScreenContractRegistry(listOf(
            createContract("screen-a"),
            createContract("screen-b"),
            createContract("screen-c")
        ))

        val keys = registry.allKeys()
        assertEquals(3, keys.size)
        assertTrue(keys.contains("screen-a"))
        assertTrue(keys.contains("screen-b"))
        assertTrue(keys.contains("screen-c"))
    }

    @Test
    fun empty_registry_returns_empty_keys() {
        val registry = ScreenContractRegistry()

        assertEquals(0, registry.allKeys().size)
    }

    @Test
    fun last_contract_wins_for_duplicate_keys() {
        val first = createContract("dup-key", "first")
        val second = createContract("dup-key", "second")
        val registry = ScreenContractRegistry(listOf(first, second))

        val found = registry.find("dup-key")
        assertNotNull(found)
        assertEquals("second", found.resource)
    }
}
