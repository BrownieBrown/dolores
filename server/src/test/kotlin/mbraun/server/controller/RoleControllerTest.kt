package mbraun.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import mbraun.server.model.Role
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class RoleControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    val baseUrl = "/api/v1/role"

    @Nested
    @DisplayName("GET /api/v1/role")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetRoles {

        @Test
        fun `should get all roles`() {
            // given when then
            mockMvc.get(baseUrl)
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { listOf<Role>() }
                    jsonPath("$[0].name") { value("USER") }
                }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/role/{name}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetRoleByName {

        @Test
        fun `should return role with given name`() {
            // given
            val name = "USER"

            // when
            val performGetRequest = mockMvc.get("$baseUrl/$name")

            // then
            performGetRequest
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id") { value(1) }
                }
        }

        @Test
        fun `should return NOT_FOUND if name does not exist`() {
            // given
            val invalidName = "INVALID_NAME"

            // when
            val performGetRequest = mockMvc.get("$baseUrl/$invalidName")

            // then
            performGetRequest
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/role")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateRole {

        @Test
        fun `should create role`() {
            // given
            val role = Role(
                null,
                "TEST_ROLE"
            )

            // when
            val performPostRequest = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(role)
            }

            // then
            performPostRequest
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.name") { value(role.name) }
                }
        }

        @Test
        fun `should return CONFLICT if role with given name already exists`() {
            // given
            val role = Role(11, "USER")

            // when
            val performPostRequest = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(role)
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
    @DisplayName("DELETE /api/v1/role/{name}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteRole {

        @Test
        fun `should delete role with name`() {
            // given
            val roleName = "USER"

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/$roleName")

            val performGetRequest = mockMvc.get("$baseUrl/$roleName")

            // then
            performDeleteRequest
                .andDo { print() }
                .andExpect { status { isNoContent() } }

            performGetRequest
                .andDo { print() }
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `should return NOT_FOUND if role with given name does not exist`() {
            // given
            val invalidRoleName = "INVALID_NAME"

            // when
            val performDeleteRequest = mockMvc.delete("$baseUrl/$invalidRoleName")

            // then
            performDeleteRequest
                .andDo { print() }
                .andExpect { status { isNotFound() } }
        }
    }
}
