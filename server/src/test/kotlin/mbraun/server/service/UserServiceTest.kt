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
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

internal class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val roleRepository: RoleRepository = mockk()
    private val userService: UserService = UserService(userRepository, roleRepository)

    @Nested
    @DisplayName("getAllUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetAllUser {

        @Test
        fun `returns collection of all user`() {
            // given
            val userList = listOf(
                User(
                    UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                    "cclampe0@economist.com",
                    "Claybourne Clampe",
                    "DPmySioRuUT",
                ),
                User(
                    UUID.fromString("47516273-07ea-4307-9413-ae7df6e3e21e"),
                    "marco.braun2013@icloud.com",
                    "Marco Braun",
                    "DPmySioRuUT",
                )
            )
            every { userRepository.findAll() } returns userList

            // when
            val result = userService.getAllUser()

            // then
            verify(exactly = 1) { userRepository.findAll() }
            assertEquals(userList, result)
        }

    }

    @Nested
    @DisplayName("getUserByEmail()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetUserByEmail {

        @Test
        fun `returns existing user by email `() {
            // given
            val user = User(
                id = UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                email = "cclampe0@economist.com",
                fullName = "Claybourne Clampe",
                password = "DPmySioRuUT",
            )
            every { userRepository.findByEmail(user.email) } returns user

            // when
            val result = userService.getUserByEmail(user.email)

            // then
            verify(exactly = 1) { userRepository.findByEmail(user.email) }
            assertEquals(user, result)
        }

        @Test
        fun `throws NOT_FOUND when no user is found`() {
            // given
            val userEmail = "test@example.com"
            every { userRepository.findByEmail(userEmail) } returns null

            //when
            val exception = assertThrows<ResponseStatusException> { userService.getUserByEmail(userEmail) }

            //then
            verify(exactly = 1) { userRepository.findByEmail(userEmail) }
            assertEquals(
                "No user with email: $userEmail exists.",
                exception.reason
            )
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
        }
    }

    @Nested
    @DisplayName("createUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {

        @Test
        fun `successfully creates user`() {
            //given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
            )
            every { userRepository.existsByEmail(user.email) } returns false
            every { userRepository.save(user) } returns user

            //when
            userService.createUser(user)

            //then
            // TODO Why are there 2 calls for existsByEmail()?
            verify { userRepository.existsByEmail(user.email) }
            verify(exactly = 1) { userRepository.save(user) }
        }

        @Test
        fun `throws CONFLICT when email already exists`() {
            //given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
            )
            every { userRepository.existsByEmail(user.email) } returns true

            //when
            val exception = assertThrows<ResponseStatusException> { userService.createUser(user) }

            //then
            verify(exactly = 1) { userRepository.existsByEmail(user.email) }
            verify(exactly = 0) { userRepository.save(user) }
            assertEquals("A user with email: ${user.email} already exists.", exception.reason)
            assertEquals(HttpStatus.CONFLICT, exception.status)
        }
    }

    @Nested
    @DisplayName("updateUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class UpdateUser {

        @Test
        fun `successfully updates user`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
            )
            every { userRepository.findByEmail(user.email) } returns user
            every { userRepository.delete(user) } returns Unit
            every { userRepository.save(user) } returns user

            // when
            val result = userService.updateUser(user)

            // then
            // TODO Why are there 2 calls?
            verify(exactly = 2) { userRepository.findByEmail(user.email) }
            verify(exactly = 1) { userRepository.delete(user) }
            verify(exactly = 1) { userRepository.save(user) }
            assertEquals(user, result)
        }

        @Test
        fun `throws NOT_FOUND when no user is found`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
            )
            every { userRepository.findByEmail(user.email) } returns null

            // when
            val exception = assertThrows<ResponseStatusException> { userService.updateUser(user) }

            // then
            verify(exactly = 1) { userRepository.findByEmail(user.email) }
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No user with email: ${user.email} exists.", exception.reason)

        }
    }

    @Nested
    @DisplayName("deleteUserByEmail()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteUserByEmail {

        @Test
        fun deleteUserByEmail() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
            )
            every { userRepository.findByEmail(user.email) } returns user
            every { userRepository.delete(user) } returns Unit

            // when
            userService.deleteUserByEmail(user.email)

            // then
            verify(exactly = 1) { userRepository.findByEmail(user.email) }
            verify(exactly = 1) { userRepository.delete(user) }
        }

        @Test
        fun `throws NOT_FOUND when no user is found`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
            )
            every { userRepository.findByEmail(user.email) } returns null

            // when
            val exception = assertThrows<ResponseStatusException> { userService.deleteUserByEmail(user.email) }

            // then
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No user with email: ${user.email} exists.", exception.reason)

        }
    }

    @Nested
    @DisplayName("deleteAllUsers()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteAllUsers {

        @Test
        fun deleteAllUsers() {
            // given
            every { userRepository.deleteAll() } returns Unit

            // when
            userService.deleteAllUsers()

            //then
            verify(exactly = 1) { userRepository.deleteAll() }
        }
    }

    @Nested
    @DisplayName("addRoleToUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AddRoleToUser {

        @Test
        fun `adds role to user`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )
            val role = Role(1, "admin")
            every { userRepository.findByEmail(user.email) } returns user
            every { roleRepository.findByName(role.name) } returns role
            every { userRepository.save(user) } returns user

            // when
            userService.addRoleToUser(user.email, role.name)
            val result = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf(Role(1, "admin"))
            )

            // then
            assertEquals(result, user)
        }

        @Test
        fun `throws NOT_FOUND when no user is found`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )
            val role = Role(1, "admin")
            every { userRepository.findByEmail(user.email) } returns null

            // when
            val exception = assertThrows<ResponseStatusException> { userService.addRoleToUser(user.email, role.name) }

            // then
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No user with email: ${user.email} exists.", exception.reason)
        }

        @Test
        fun `throws NOT_FOUND when no role is found`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )
            val role = Role(1, "admin")
            every { userRepository.findByEmail(user.email) } returns user
            every { roleRepository.findByName(role.name) } returns null

            // when
            val exception = assertThrows<ResponseStatusException> { userService.addRoleToUser(user.email, role.name) }


            // then
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No role with name: ${role.name} exists.", exception.reason)
        }

        @Test
        fun `throws CONFLICT when user already has role`() {
            // given
            val role = Role(1, "admin")
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf(role)
            )
            every { userRepository.findByEmail(user.email) } returns user
            every { roleRepository.findByName(role.name) } returns role

            // when
            val exception = assertThrows<ResponseStatusException> { userService.addRoleToUser(user.email, role.name) }


            // then
            assertEquals(HttpStatus.CONFLICT, exception.status)
            assertEquals("User with email: ${user.email} already has role.", exception.reason)
        }
    }

    @Nested
    @DisplayName("removeRoleFromUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RemoveRoleFromUser {

        @Test
        fun `removes role from user`() {
            // given
            val role = Role(1, "admin")
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf(role)
            )
            every { userRepository.findByEmail(user.email) } returns user
            every { roleRepository.findByName(role.name) } returns role
            every { userRepository.save(user) } returns user

            // when
            userService.removeRoleFromUser(user.email, role.name)
            val result = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )

            // then
            assertEquals(result, user)
        }

        @Test
        fun `throws NOT_FOUND when no user is found`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )
            val role = Role(1, "admin")
            every { userRepository.findByEmail(user.email) } returns null

            // when
            val exception =
                assertThrows<ResponseStatusException> { userService.removeRoleFromUser(user.email, role.name) }

            // then
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No user with email: ${user.email} exists.", exception.reason)
        }

        @Test
        fun `throws NOT_FOUND when no role is found`() {
            // given
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )
            val role = Role(1, "admin")
            every { userRepository.findByEmail(user.email) } returns user
            every { roleRepository.findByName(role.name) } returns null

            // when
            val exception =
                assertThrows<ResponseStatusException> { userService.removeRoleFromUser(user.email, role.name) }


            // then
            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            assertEquals("No role with name: ${role.name} exists.", exception.reason)
        }

        @Test
        fun `throws CONFLICT when user does not have role`() {
            // given
            val role = Role(1, "admin")
            val user = User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                roles = arrayListOf()
            )
            every { userRepository.findByEmail(user.email) } returns user
            every { roleRepository.findByName(role.name) } returns role

            // when
            val exception =
                assertThrows<ResponseStatusException> { userService.removeRoleFromUser(user.email, role.name) }


            // then
            assertEquals(HttpStatus.CONFLICT, exception.status)
            assertEquals("The user with email: ${user.email} does not posses this role.", exception.reason)
        }
    }
}