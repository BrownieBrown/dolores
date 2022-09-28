package mbraun.server.controller

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

@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

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

            // when then
            mockMvc.get("$baseUrl/$email")
                .andDo { print() }
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
            mockMvc.get("$baseUrl/$email")
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }
    
}