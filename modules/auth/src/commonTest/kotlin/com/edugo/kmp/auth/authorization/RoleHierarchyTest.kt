package com.edugo.kmp.auth.authorization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoleHierarchyTest {

    // Test implementation of Role
    private data class TestRole(
        override val name: String,
        override val displayName: String,
        override val level: Int
    ) : Role

    private val VIEWER = TestRole("VIEWER", "Viewer", 10)
    private val EDITOR = TestRole("EDITOR", "Editor", 20)
    private val ADMIN = TestRole("ADMIN", "Admin", 30)
    private val SUPER_ADMIN = TestRole("SUPER_ADMIN", "Super Admin", 40)

    private val hierarchy = RoleHierarchy(listOf(VIEWER, EDITOR, ADMIN, SUPER_ADMIN))

    // ==================== allRoles ====================

    @Test
    fun `allRoles returns roles sorted by level ascending`() {
        val roles = hierarchy.allRoles()

        assertEquals(4, roles.size)
        assertEquals(VIEWER, roles[0])
        assertEquals(EDITOR, roles[1])
        assertEquals(ADMIN, roles[2])
        assertEquals(SUPER_ADMIN, roles[3])
    }

    // ==================== getRolesAtLeast ====================

    @Test
    fun `getRolesAtLeast returns roles with level greater or equal`() {
        val roles = hierarchy.getRolesAtLeast(EDITOR)

        assertEquals(3, roles.size)
        assertTrue(roles.contains(EDITOR))
        assertTrue(roles.contains(ADMIN))
        assertTrue(roles.contains(SUPER_ADMIN))
        assertFalse(roles.contains(VIEWER))
    }

    @Test
    fun `getRolesAtLeast with highest role returns only that role`() {
        val roles = hierarchy.getRolesAtLeast(SUPER_ADMIN)

        assertEquals(1, roles.size)
        assertEquals(SUPER_ADMIN, roles[0])
    }

    @Test
    fun `getRolesAtLeast with lowest role returns all roles`() {
        val roles = hierarchy.getRolesAtLeast(VIEWER)

        assertEquals(4, roles.size)
    }

    // ==================== getRolesAtMost ====================

    @Test
    fun `getRolesAtMost returns roles with level less or equal`() {
        val roles = hierarchy.getRolesAtMost(EDITOR)

        assertEquals(2, roles.size)
        assertTrue(roles.contains(VIEWER))
        assertTrue(roles.contains(EDITOR))
        assertFalse(roles.contains(ADMIN))
    }

    @Test
    fun `getRolesAtMost with lowest role returns only that role`() {
        val roles = hierarchy.getRolesAtMost(VIEWER)

        assertEquals(1, roles.size)
        assertEquals(VIEWER, roles[0])
    }

    // ==================== getHighestRole / getLowestRole ====================

    @Test
    fun `getHighestRole returns role with highest level`() {
        val highest = hierarchy.getHighestRole(
            listOf(VIEWER, ADMIN, EDITOR)
        )

        assertEquals(ADMIN, highest)
    }

    @Test
    fun `getHighestRole with empty list returns null`() {
        val highest = hierarchy.getHighestRole(emptyList())

        assertNull(highest)
    }

    @Test
    fun `getLowestRole returns role with lowest level`() {
        val lowest = hierarchy.getLowestRole(
            listOf(ADMIN, VIEWER, EDITOR)
        )

        assertEquals(VIEWER, lowest)
    }

    @Test
    fun `getLowestRole with empty list returns null`() {
        val lowest = hierarchy.getLowestRole(emptyList())

        assertNull(lowest)
    }

    @Test
    fun `getHighestRole with single role returns that role`() {
        val highest = hierarchy.getHighestRole(listOf(EDITOR))

        assertEquals(EDITOR, highest)
    }

    // ==================== isHigherThan / isAtLeast ====================

    @Test
    fun `isHigherThan returns true when roleA has higher level`() {
        assertTrue(hierarchy.isHigherThan(ADMIN, EDITOR))
    }

    @Test
    fun `isHigherThan returns false when levels are equal`() {
        assertFalse(hierarchy.isHigherThan(ADMIN, ADMIN))
    }

    @Test
    fun `isHigherThan returns false when roleA has lower level`() {
        assertFalse(hierarchy.isHigherThan(VIEWER, ADMIN))
    }

    @Test
    fun `isAtLeast returns true when roleA has higher level`() {
        assertTrue(hierarchy.isAtLeast(ADMIN, EDITOR))
    }

    @Test
    fun `isAtLeast returns true when levels are equal`() {
        assertTrue(hierarchy.isAtLeast(ADMIN, ADMIN))
    }

    @Test
    fun `isAtLeast returns false when roleA has lower level`() {
        assertFalse(hierarchy.isAtLeast(VIEWER, ADMIN))
    }

    // ==================== findByName ====================

    @Test
    fun `findByName returns role when exists`() {
        val role = hierarchy.findByName("ADMIN")

        assertEquals(ADMIN, role)
    }

    @Test
    fun `findByName returns null when not exists`() {
        val role = hierarchy.findByName("NONEXISTENT")

        assertNull(role)
    }

    @Test
    fun `findByName is case sensitive`() {
        val role = hierarchy.findByName("admin")

        assertNull(role)
    }
}
