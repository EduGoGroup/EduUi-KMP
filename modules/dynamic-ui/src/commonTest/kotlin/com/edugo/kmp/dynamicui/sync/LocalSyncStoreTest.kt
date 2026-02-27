package com.edugo.kmp.dynamicui.sync

import com.edugo.kmp.auth.model.MenuItem
import com.edugo.kmp.auth.model.MenuResponse
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.sync.model.UserDataBundle
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalSyncStoreTest {

    private lateinit var store: LocalSyncStore
    private lateinit var storage: SafeEduGoStorage

    @BeforeTest
    fun setup() {
        storage = SafeEduGoStorage(EduGoStorage.withSettings(MapSettings()), validateKeys = false)
        store = LocalSyncStore(storage)
    }

    private fun createTestBundle(): UserDataBundle {
        val menu = MenuResponse(
            items = listOf(
                MenuItem(key = "dashboard", displayName = "Dashboard", icon = "dashboard"),
                MenuItem(key = "schools", displayName = "Schools", icon = "school"),
            )
        )
        val screens = mapOf(
            "schools-list" to ScreenDefinition(
                screenKey = "schools-list",
                screenName = "Schools List",
                pattern = ScreenPattern.LIST,
                template = ScreenTemplate(zones = emptyList()),
            ),
            "schools-form" to ScreenDefinition(
                screenKey = "schools-form",
                screenName = "Schools Form",
                pattern = ScreenPattern.FORM,
                template = ScreenTemplate(zones = emptyList()),
            ),
        )
        val permissions = listOf("schools:read", "schools:write", "dashboard:read")
        val contexts = listOf(
            UserContext(roleId = "r1", roleName = "admin", schoolId = "s1", schoolName = "School A"),
        )
        val hashes = mapOf(
            "menu" to "hash_menu_1",
            "permissions" to "hash_perm_1",
            "screen:schools-list" to "hash_sl_1",
            "screen:schools-form" to "hash_sf_1",
            "available_contexts" to "hash_ctx_1",
        )

        return UserDataBundle(
            menu = menu,
            permissions = permissions,
            screens = screens,
            availableContexts = contexts,
            hashes = hashes,
            syncedAt = Clock.System.now(),
        )
    }

    @Test
    fun saveBundle_and_loadBundle_roundtrip() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        val loaded = store.loadBundle()
        assertNotNull(loaded)
        assertEquals(bundle.menu.items.size, loaded.menu.items.size)
        assertEquals(bundle.permissions, loaded.permissions)
        assertEquals(bundle.screens.keys, loaded.screens.keys)
        assertEquals(bundle.availableContexts.size, loaded.availableContexts.size)
        assertEquals(bundle.hashes, loaded.hashes)
    }

    @Test
    fun loadBundle_returns_null_when_empty() = runTest {
        val loaded = store.loadBundle()
        assertNull(loaded)
    }

    @Test
    fun getHashes_returns_saved_hashes() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        val hashes = store.getHashes()
        assertEquals(5, hashes.size)
        assertEquals("hash_menu_1", hashes["menu"])
        assertEquals("hash_perm_1", hashes["permissions"])
    }

    @Test
    fun updateMenu_updates_menu_and_hash() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        val newMenu = MenuResponse(
            items = listOf(MenuItem(key = "new-item", displayName = "New Item"))
        )
        store.updateMenu(newMenu, "hash_menu_2")

        val loaded = store.loadBundle()
        assertNotNull(loaded)
        assertEquals(1, loaded.menu.items.size)
        assertEquals("new-item", loaded.menu.items[0].key)
        assertEquals("hash_menu_2", loaded.hashes["menu"])
    }

    @Test
    fun updatePermissions_updates_permissions_and_hash() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        store.updatePermissions(listOf("new:perm"), "hash_perm_2")

        val loaded = store.loadBundle()
        assertNotNull(loaded)
        assertEquals(listOf("new:perm"), loaded.permissions)
        assertEquals("hash_perm_2", loaded.hashes["permissions"])
    }

    @Test
    fun updateScreen_adds_new_screen() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        val newScreen = ScreenDefinition(
            screenKey = "users-list",
            screenName = "Users List",
            pattern = ScreenPattern.LIST,
            template = ScreenTemplate(zones = emptyList()),
        )
        store.updateScreen("users-list", newScreen, "hash_ul_1")

        val loaded = store.loadBundle()
        assertNotNull(loaded)
        assertTrue(loaded.screens.containsKey("users-list"))
        assertEquals("hash_ul_1", loaded.hashes["screen:users-list"])
    }

    @Test
    fun removeScreen_removes_screen_and_hash() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        store.removeScreen("schools-form")

        val loaded = store.loadBundle()
        assertNotNull(loaded)
        assertTrue(!loaded.screens.containsKey("schools-form"))
        assertNull(loaded.hashes["screen:schools-form"])
        // Other screen should still exist
        assertTrue(loaded.screens.containsKey("schools-list"))
    }

    @Test
    fun clearAll_removes_everything() = runTest {
        val bundle = createTestBundle()
        store.saveBundle(bundle)

        store.clearAll()

        assertNull(store.loadBundle())
        assertTrue(store.getHashes().isEmpty())
    }
}
