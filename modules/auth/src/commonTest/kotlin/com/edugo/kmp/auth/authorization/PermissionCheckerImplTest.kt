package com.edugo.kmp.auth.authorization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionCheckerImplTest {

    // Test implementations using data classes (enums can't override 'name')
    private data class TestRole(
        override val name: String,
        override val displayName: String,
        override val level: Int
    ) : Role

    private data class TestPermission(
        override val name: String,
        override val resource: String,
        override val action: String
    ) : Permission

    // Roles
    private val VIEWER = TestRole("VIEWER", "Viewer", 10)
    private val EDITOR = TestRole("EDITOR", "Editor", 20)
    private val ADMIN = TestRole("ADMIN", "Admin", 30)

    // Permissions
    private val VIEW_CONTENT = TestPermission("VIEW_CONTENT", "content", "view")
    private val EDIT_CONTENT = TestPermission("EDIT_CONTENT", "content", "edit")
    private val DELETE_CONTENT = TestPermission("DELETE_CONTENT", "content", "delete")
    private val MANAGE_USERS = TestPermission("MANAGE_USERS", "users", "manage")
    private val VIEW_REPORTS = TestPermission("VIEW_REPORTS", "reports", "view")

    private val allPermissions = setOf(VIEW_CONTENT, EDIT_CONTENT, DELETE_CONTENT, MANAGE_USERS, VIEW_REPORTS)

    private val checker = PermissionCheckerImpl<TestRole, TestPermission> { role ->
        when (role) {
            VIEWER -> setOf(VIEW_CONTENT)
            EDITOR -> setOf(VIEW_CONTENT, EDIT_CONTENT, VIEW_REPORTS)
            ADMIN -> allPermissions
            else -> emptySet()
        }
    }

    // ==================== hasPermission ====================

    @Test
    fun `hasPermission returns true when role has permission`() {
        assertTrue(checker.hasPermission(VIEWER, VIEW_CONTENT))
    }

    @Test
    fun `hasPermission returns false when role lacks permission`() {
        assertFalse(checker.hasPermission(VIEWER, EDIT_CONTENT))
    }

    @Test
    fun `hasPermission admin has all permissions`() {
        allPermissions.forEach { permission ->
            assertTrue(
                checker.hasPermission(ADMIN, permission),
                "Admin should have permission: ${permission.name}"
            )
        }
    }

    @Test
    fun `hasPermission editor has view and edit but not manage`() {
        assertTrue(checker.hasPermission(EDITOR, VIEW_CONTENT))
        assertTrue(checker.hasPermission(EDITOR, EDIT_CONTENT))
        assertFalse(checker.hasPermission(EDITOR, MANAGE_USERS))
        assertFalse(checker.hasPermission(EDITOR, DELETE_CONTENT))
    }

    // ==================== hasAnyPermission ====================

    @Test
    fun `hasAnyPermission returns true when role has at least one`() {
        assertTrue(
            checker.hasAnyPermission(VIEWER, setOf(VIEW_CONTENT, EDIT_CONTENT))
        )
    }

    @Test
    fun `hasAnyPermission returns false when role has none`() {
        assertFalse(
            checker.hasAnyPermission(VIEWER, setOf(EDIT_CONTENT, DELETE_CONTENT))
        )
    }

    @Test
    fun `hasAnyPermission with empty set returns false`() {
        assertFalse(
            checker.hasAnyPermission(ADMIN, emptySet())
        )
    }

    // ==================== hasAllPermissions ====================

    @Test
    fun `hasAllPermissions returns true when role has all`() {
        assertTrue(
            checker.hasAllPermissions(EDITOR, setOf(VIEW_CONTENT, EDIT_CONTENT))
        )
    }

    @Test
    fun `hasAllPermissions returns false when role lacks one`() {
        assertFalse(
            checker.hasAllPermissions(EDITOR, setOf(VIEW_CONTENT, DELETE_CONTENT))
        )
    }

    @Test
    fun `hasAllPermissions with empty set returns true`() {
        assertTrue(
            checker.hasAllPermissions(VIEWER, emptySet())
        )
    }

    @Test
    fun `hasAllPermissions admin has all permissions`() {
        assertTrue(
            checker.hasAllPermissions(ADMIN, allPermissions)
        )
    }

    // ==================== getEffectivePermissions ====================

    @Test
    fun `getEffectivePermissions returns correct permissions for viewer`() {
        val permissions = checker.getEffectivePermissions(VIEWER)

        assertEquals(1, permissions.size)
        assertTrue(permissions.contains(VIEW_CONTENT))
    }

    @Test
    fun `getEffectivePermissions returns correct permissions for editor`() {
        val permissions = checker.getEffectivePermissions(EDITOR)

        assertEquals(3, permissions.size)
        assertTrue(permissions.contains(VIEW_CONTENT))
        assertTrue(permissions.contains(EDIT_CONTENT))
        assertTrue(permissions.contains(VIEW_REPORTS))
    }

    @Test
    fun `getEffectivePermissions returns all permissions for admin`() {
        val permissions = checker.getEffectivePermissions(ADMIN)

        assertEquals(allPermissions.size, permissions.size)
    }

    // ==================== Permission interface properties ====================

    @Test
    fun `permission has correct resource and action`() {
        assertEquals("content", EDIT_CONTENT.resource)
        assertEquals("edit", EDIT_CONTENT.action)
        assertEquals("EDIT_CONTENT", EDIT_CONTENT.name)
    }

    // ==================== Role interface properties ====================

    @Test
    fun `role has correct properties`() {
        assertEquals("EDITOR", EDITOR.name)
        assertEquals("Editor", EDITOR.displayName)
        assertEquals(20, EDITOR.level)
    }
}
