package mbraun.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import mbraun.server.model.User
import mbraun.server.util.RoleToUserForm
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    val baseUrl = "/api/v1/user"

    @Nested
    @DisplayName("GET /api/v1/user")
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
                    jsonPath("$[0].email") { value("marco.braun2013@gmail.com") }
                }

        }
    }

    @Nested
    @DisplayName("GET /api/users/{email}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetUserByEmail {

        @Test
        fun `should return user with given email`() {
            // given
            val email = "marco.braun2013@gmail.com"

            // when
            val performGetRequest = mockMvc.get("$baseUrl/$email")

            // then
            performGetRequest.andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.fullName") { value("Marco Braun") }
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
    @DisplayName("POST /api/v1/user")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateUser {

        @Test
        @DirtiesContext
        fun `should create a new user`() {
            // given
            val user = User(
                email = "user@google.com",
                password = "1234",
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
                    jsonPath("$.email") { value(user.email) }
                }
        }

        @Test
        fun `should return CONFLICT if user with given email already exists`() {
            // given
            val user = User(email = "marco.braun2013@gmail.com", password = "1234", fullName = "Marco Braun")

            // when
            val performPostRequest = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(user)
            }

            // then
            performPostRequest
                .andDo { print() }
                .andExpect {
                    status { isConflict() }
                }
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/user")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class UpdateUser {

        @Test
        fun `should update an existing user by mail`() {
            // given
            val updatedUser =
                User(
                    email = "yannick.seppich@gmx.de",
                    password = "1234",
                    fullName = "Yannick Discopumper Seppich"
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


        @Test
        fun `should return NOT_FOUND if user with given email does not exist`() {
            // given
            val invalidUser = User(
                email = "invalidEmail@email.com",
                password = "1234",
                fullName = "invalid user"
            )

            // when
            val performPatchRequest = mockMvc.patch(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(invalidUser)
            }

            // then
            performPatchRequest
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/user/{email}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteUserByEmail {

        @Test
        @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
        fun `should delete the user with given email`() {
            // given
            val email = "yannick.seppich@gmx.de"

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/$email")

            val performGetRequest = mockMvc.get("$baseUrl/$email")

            // then
            performDeleteRequest
                .andDo { print() }
                .andExpect {
                    status { isNoContent() }
                }

            performGetRequest
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `should return NOT_FOUND if user with given email does not exist`() {
            // given
            val invalidEmail = "invalidEmail@email.com"

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/$invalidEmail")

            // then
            performDeleteRequest
                .andDo { print() }
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/role/add")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AddRoleToUser {

        @Test
        fun `should add role to user`() {
            // given
            val email = "yannick.seppich@gmx.de"
            val roleName = "ADMIN"
            val form = RoleToUserForm(email, roleName)

            // when
            val performPostRequest = mockMvc.post("$baseUrl/role/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performPostRequest
                .andDo { print() }.andExpect { status { isCreated() } }
        }

        @Test
        fun `should return NOT_FOUND if user with given email does not exist`() {
            // given
            val invalidEmail = "invalidEmail@economist.com"
            val roleName = "ADMIN"
            val form = RoleToUserForm(invalidEmail, roleName)

            // when
            val performPostRequest = mockMvc.post("$baseUrl/role/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performPostRequest
                .andDo { print() }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `should return NOT_FOUND if role does not exist`() {
            // given
            val email = "cclampe0@economist.com"
            val invalidRole = "INVALID"
            val form = RoleToUserForm(email, invalidRole)

            // when
            val performPostRequest = mockMvc.post("$baseUrl/role/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performPostRequest
                .andDo { print() }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `should return CONFLICT if user already has role`() {
            // given
            val email = "marco.braun2013@gmail.com"
            val roleName = "ADMIN"
            val form = RoleToUserForm(email, roleName)

            // when
            val performPostRequest = mockMvc.post("$baseUrl/role/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performPostRequest
                .andDo { print() }.andExpect { status { isConflict() } }
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/role/remove")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RemoveRoleFromUser {

        @Test
        fun `should remove role from user`() {
            // given
            val email = "marco.braun2013@gmail.com"
            val roleName = "USER"
            val form = RoleToUserForm(email, roleName)

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/role/remove") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performDeleteRequest
                .andDo { print() }.andExpect { status { isOk() } }
        }

        @Test
        fun `should return NOT_FOUND if user with given email does not exist`() {
            // given
            val invalidEmail = "invalidEmail@economist.com"
            val roleName = "ADMIN"
            val form = RoleToUserForm(invalidEmail, roleName)

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/role/remove") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performDeleteRequest
                .andDo { print() }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `should return NOT_FOUND if role does not exist`() {
            // given
            val email = "cclampe0@economist.com"
            val invalidRole = "INVALID"
            val form = RoleToUserForm(email, invalidRole)

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/role/remove") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performDeleteRequest
                .andDo { print() }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `should return CONFLICT if user does not have role`() {
            // given
            val email = "yannick.seppich@gmx.de"
            val roleName = "MANAGER"
            val form = RoleToUserForm(email, roleName)

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/role/remove") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(form)
            }

            // then
            performDeleteRequest
                .andDo { print() }.andExpect { status { isConflict() } }
        }
    }
}