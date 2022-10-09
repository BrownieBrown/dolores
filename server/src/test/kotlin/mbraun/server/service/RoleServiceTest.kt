package mbraun.server.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mbraun.server.model.Role
import mbraun.server.repository.RoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

internal class RoleServiceTest {
    private val roleRepository: RoleRepository = mockk()
    private val roleService: RoleService = RoleService(roleRepository)

    @Nested
    @DisplayName("getRoles()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetRoles {

        @Test
        fun `returns collection of all roles`() {
            // given
            val roleList = listOf(Role(1, "admin"), Role(2, "user"))
            every { roleRepository.findAll() } returns roleList

            // when
            val result = roleService.getRoles()

            // then
            verify(exactly = 1) { roleRepository.findAll() }
            assertEquals(roleList, result)
        }
    }

    @Nested
    @DisplayName("getRoleByName()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetRoleByName {

        @Test
        fun `returns a role by name`() {
            // given
            val role = Role(1, "admin")
            every { roleRepository.findByName(role.name) } returns role

            // when
            val result = roleService.getRoleByName(role.name)

            // then
            verify(exactly = 1) { roleRepository.findByName(role.name) }
            assertEquals(role, result)
        }
    }

    @Nested
    @DisplayName("createRole()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateRole {

        @Test
        fun `creates a new role`() {
            // given
            val role = Role(1, "admin")
            every { roleRepository.existsByName(role.name) } returns false
            every { roleRepository.save(role) } returns role

            // when
            val result = roleService.createRole(role)

            // then
            verify(exactly = 1) { roleRepository.existsByName(role.name) }
            verify(exactly = 1) { roleRepository.save(role) }
            assertEquals(role, result)
        }
    }

    @Nested
    @DisplayName("updateRole()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class UpdateRole {

        @Test
        fun `updates an existing role`() {
            // given
            val role = Role(1, "admin")
            every { roleRepository.findByName(role.name) } returns role
            every { roleRepository.delete(role) } returns Unit
            every { roleRepository.save(role) } returns role

            // when
            val result = roleService.updateRole(role)

            // then
            verify(exactly = 1) { roleRepository.findByName(role.name) }
            verify(exactly = 1) { roleRepository.delete(role) }
            verify(exactly = 1) { roleRepository.save(role) }
            assertEquals(role, result)
        }
    }

    @Nested
    @DisplayName("deleteRoleByName()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteRole {

        @Test
        fun `deletes an existing role`() {
            // given
            val role = Role(1, "admin")
            every { roleRepository.findByName(role.name) } returns role
            every { roleRepository.delete(role) } returns Unit

            // when
            roleService.deleteRoleByName(role.name)

            // then
            verify(exactly = 1) { roleRepository.findByName(role.name) }
            verify(exactly = 1) { roleRepository.delete(role) }
        }
    }

    @Nested
    @DisplayName("deleteAllRoles()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteAllRoles {

        @Test
        fun `deletes all roles`() {
            // given
            every { roleRepository.deleteAll() } returns Unit

            // when
            roleService.deleteAllRoles()

            // then
            verify(exactly = 1) { roleRepository.deleteAll() }
        }
    }
}