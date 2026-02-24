package com.edugo.kmp.dynamicui.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationItemUtilsTest {

    private val testItems = listOf(
        NavigationItem(
            key = "dashboard",
            label = "Dashboard",
            icon = "dashboard",
            screenKey = "dashboard-screen",
        ),
        NavigationItem(
            key = "admin",
            label = "Admin",
            icon = "shield",
            children = listOf(
                NavigationItem(key = "users", label = "Users", icon = "group", screenKey = "users-screen"),
                NavigationItem(key = "schools", label = "Schools", icon = "school", screenKey = "schools-screen"),
            ),
        ),
        NavigationItem(
            key = "content",
            label = "Content",
            icon = "folder",
            children = listOf(
                NavigationItem(key = "materials", label = "Materials", icon = "book", screenKey = "materials-screen"),
            ),
        ),
    )

    @Test
    fun findByKey_returns_top_level_item() {
        val result = testItems.findByKey("dashboard")
        assertEquals("dashboard", result?.key)
        assertEquals("Dashboard", result?.label)
    }

    @Test
    fun findByKey_returns_nested_child() {
        val result = testItems.findByKey("users")
        assertEquals("users", result?.key)
        assertEquals("users-screen", result?.screenKey)
    }

    @Test
    fun findByKey_returns_null_for_unknown_key() {
        assertNull(testItems.findByKey("nonexistent"))
    }

    @Test
    fun findParentKey_returns_parent_of_child() {
        assertEquals("admin", testItems.findParentKey("users"))
        assertEquals("admin", testItems.findParentKey("schools"))
        assertEquals("content", testItems.findParentKey("materials"))
    }

    @Test
    fun findParentKey_returns_null_for_top_level_item() {
        assertNull(testItems.findParentKey("dashboard"))
    }

    @Test
    fun findParentKey_returns_null_for_unknown_key() {
        assertNull(testItems.findParentKey("nonexistent"))
    }

    @Test
    fun firstLeaf_returns_first_item_without_children() {
        val result = testItems.firstLeaf()
        assertEquals("dashboard", result?.key)
    }

    @Test
    fun firstLeaf_skips_parents_and_finds_first_child() {
        val itemsWithoutDashboard = listOf(
            NavigationItem(
                key = "admin",
                label = "Admin",
                icon = "shield",
                children = listOf(
                    NavigationItem(key = "users", label = "Users", screenKey = "users-screen"),
                    NavigationItem(key = "schools", label = "Schools", screenKey = "schools-screen"),
                ),
            ),
        )
        val result = itemsWithoutDashboard.firstLeaf()
        assertEquals("users", result?.key)
    }

    @Test
    fun firstLeaf_returns_null_for_empty_list() {
        assertNull(emptyList<NavigationItem>().firstLeaf())
    }
}
