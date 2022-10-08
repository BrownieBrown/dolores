package mbraun.server.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mbraun.server.model.User
import mbraun.server.repository.UserRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

internal class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val userService: UserService = UserService(userRepository)

    @Test
    fun `function getAllUser() return list of User`() {
        // given
        val userList = listOf(
            User(
                UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
                "cclampe0@economist.com",
                "Claybourne Clampe",
                "DPmySioRuUT",
                true,
                false
            ),
            User(
                UUID.fromString("47516273-07ea-4307-9413-ae7df6e3e21e"),
                "marco.braun2013@icloud.com",
                "Marco Braun",
                "DPmySioRuUT",
                true,
                false
            )
        )
        every { userRepository.findAll() } returns userList

        // when
        val result = userService.getAllUser()

        //then
        verify(exactly = 1) { userRepository.findAll() }
        assertEquals(userList, result)
    }

    @Test
    fun `getUserByMail() returns user`() {
        // user
        val user = User(
            UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
            "cclampe0@economist.com",
            "Claybourne Clampe",
            "DPmySioRuUT",
            true,
            false
        )
        every { userRepository.findByEmail(user.email) } returns user

        // when
        val result = userService.getUserByEmail(user.email)

        // then
        verify(exactly = 1) { userRepository.findByEmail(user.email) }
        assertEquals(user, result)

    }

    @Test
    fun `getUserByEmail() throws NOT_FOUND when null`() {
        // given
        val userEmail = "test@example.com"
        every { userRepository.findByEmail(userEmail) } returns null

        //when

        //then
        verify(exactly = 0) { userRepository.findByEmail(userEmail) }
        assertThrows<ResponseStatusException> { userService.getUserByEmail(userEmail) }
    }

    @Test
    fun `createUser() successfully creates user`() {
        //given
        val user = User(
            UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
            "cclampe0@economist.com",
            "Claybourne Clampe",
            "DPmySioRuUT",
            true,
            false
        )
        every { userRepository.existsByEmail(user.email) } returns false
        every { userRepository.save(user) } returns user
        every { userRepository.findByEmail(user.email) } returns user

        //when
        userService.createUser(user)

        //then
        verify(exactly = 1) { userRepository.existsByEmail(user.email) }
        verify(exactly = 1) { userRepository.save(user) }
        Assertions.assertThat(userRepository.findByEmail(user.email)).isNotNull

    }

    @Test
    fun updateUser() {
        // given
        val user = User(
            UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
            "cclampe0@economist.com",
            "Claybourne Clampe",
            "DPmySioRuUT",
            true,
            false
        )
        every { userRepository.findByEmail(user.email) } returns user
        every { userRepository.delete(user) } returns Unit
        every { userRepository.save(user) } returns user

        // when
        val result = userService.updateUser(user)

        // then
        verify(exactly = 1) { userRepository.findByEmail(user.email) }
        verify(exactly = 1) { userRepository.delete(user) }
        verify(exactly = 1) { userRepository.save(user) }
        assertEquals(user, result)
    }

    @Test
    fun deleteUserByEmail() {
        // given
        val user = User(
            UUID.fromString("fc2dff64-4ccb-4c71-9ef5-4bd9fb628f14"),
            "cclampe0@economist.com",
            "Claybourne Clampe",
            "DPmySioRuUT",
            true,
            false
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
    fun deleteAllUsers() {
        // given
        every { userRepository.deleteAll() } returns Unit

        // when
        userService.deleteAllUsers()

        //then
        verify(exactly = 1) { userRepository.deleteAll() }
    }
}