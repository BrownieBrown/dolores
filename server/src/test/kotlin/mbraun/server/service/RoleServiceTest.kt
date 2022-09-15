package mbraun.server.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mbraun.server.model.Role
import mbraun.server.model.User
import mbraun.server.repository.RoleRepository
import mbraun.server.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.server.ResponseStatusException

internal class RoleServiceTest {
    private val roleRepository: RoleRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val roleService: RoleService = RoleService(roleRepository, userRepository)

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

        @Test
        fun `returns NOT_FOUND if role not found`() {
            // given
            val invalidRole = Role(1, "invalid")
            every { roleRepository.findByName(invalidRole.name) } returns null

            // when
            val exception = assertThrows<ResponseStatusException> { roleService.getRoleByName(invalidRole.name) }

            // then
            verify(exactly = 1) { roleRepository.findByName(invalidRole.name) }
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No role with name: ${invalidRole.name} exists.", exception.reason)
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

        @Test
        fun `throws CONFLICT if role already exists`() {
            // given
            val role = Role(1, "admin")
            every { roleRepository.existsByName(role.name) } returns true

            // when
            val exception = assertThrows<ResponseStatusException> { roleService.createRole(role) }

            // then
            verify { roleRepository.existsByName(role.name) }
            assertEquals(HttpStatus.CONFLICT, exception.status)
            assertEquals("Role with name: ${role.name} already exists.", exception.reason)
        }
    }

    @Nested
    @DisplayName("deleteRoleByName()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteRole {

        @Test
        @DirtiesContext
        fun `deletes an existing role`() {
            // given
            val role = Role(3, "ADMIN")
            val users = listOf(
                User(
                    email = "admin@example.com",
                    fullName = "admin",
                    password = "admin",
                    roles = mutableListOf(Role(1, "ADMIN"), Role(2, "USER"))
                ),
                User(
                    email = "user@example.com",
                    fullName = "user",
                    password = "user",
                    roles = mutableListOf(Role(2, "USER"))
                )
            )
            every { roleRepository.findByName(role.name) } returns role
            every { roleRepository.delete(role) } returns Unit
            every { userRepository.findAll() } returns users

            // when
            val result = roleService.deleteRoleByName(role.name)

            // then
            verify { roleRepository.findByName(role.name) }
            verify { userRepository.findAll() }
            verify(exactly = 1) { roleRepository.delete(role) }
        }

        @Test
        fun `throws NOT_FOUND if role not found`() {
            // given
            val role = Role(1, "admin")
            every { roleRepository.findByName(role.name) } returns null

            // when
            val exception = assertThrows<ResponseStatusException> { roleService.deleteRoleByName(role.name) }

            // then
            verify(exactly = 1) { roleRepository.findByName(role.name) }
            verify(exactly = 0) { roleRepository.delete(role) }
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No role with name: ${role.name} exists.", exception.reason)
        }
    }
}