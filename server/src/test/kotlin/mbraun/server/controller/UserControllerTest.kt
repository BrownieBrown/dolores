package mbraun.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import mbraun.server.model.User
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    val baseUrl = "/api/v1/user"

    @Nested
    @DisplayName("getAllUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetBanks {
        @Test
        fun `should return all user`() {
            // given when then
            mockMvc.get(baseUrl)
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { listOf<User>() }
                    jsonPath("$[0].email") { value("cclampe0@economist.com") }
                }

        }
    }

    @Nested
    @DisplayName("getUserByEmail()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetUserByEmail {

        @Test
        fun `should return user with given email`() {
            // given
            val email = "pniessen4@archive.org"

            // when
            val performGetRequest = mockMvc.get("$baseUrl/$email")

            // then
            performGetRequest.andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.fullName") { value("Penny Niessen") }
                }
        }

        @Test
        fun `should return NOT_FOUND if the email does not exist`() {
            // given
            val email = "wrong.email@google.com"

            // when
            val performGetRequest = mockMvc.get("$baseUrl/$email")

            // then
            performGetRequest.andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    @DisplayName("createUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {

        @Test
        fun `should create a new user`() {
            // given
            val user = User(
                email = "user@google.com",
                hashed_password = "1234",
                fullName = "test user"
            )

            // when
            val performPostRequest = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(user)
            }

            // then
            performPostRequest
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                    }
                    jsonPath("$.email") { value("user@google.com") }
                }
        }

        @Test
        fun `should return BAD_REQUEST if user with given email already exists`() {
            // given
            val user = User(email = "cclampe0@economist.com", hashed_password = "1234", fullName = "test user")

            // when
            val performPostRequest = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(user)
            }

            // then
            performPostRequest
                .andDo { print() }
                .andExpect {
                    status { isBadRequest() }
                }
        }
    }

    @Nested
    @DisplayName("updateUser()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class UpdateUser {

        @Test
        fun `should update an existing user by mail`() {
            // given
            val updatedUser =
                User(
                    email = "jde1@constantcontact.com",
                    hashed_password = "1234",
                    fullName = "test user"
                )

            // when
            val performPatchRequest = mockMvc.patch(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(updatedUser)
            }

            // then
            performPatchRequest
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content {
                        MediaType.APPLICATION_JSON
                        json(objectMapper.writeValueAsString(updatedUser))
                    }
                }
        }
    }


}