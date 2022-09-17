package mbraun.server.controller

import mbraun.server.model.User
import mbraun.server.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/user")
class UserController(@Autowired private val userService: UserService) {

    @GetMapping
    fun getAllUser(): ResponseEntity<List<User>> {
        return userService.getAllUser()
    }

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        return userService.getUserByEmail(email)
    }

    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        return userService.createUser(user)
    }

    @PatchMapping("/{email")
    fun updateUserPassword(@PathVariable email: String, @RequestBody user: User): ResponseEntity<User> {
        return userService.updateUser(email, user)
    }

    @DeleteMapping("/{email}")
    fun deleteUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        return userService.deleteUserByEmail(email)
    }

    @DeleteMapping
    fun deleteAllUsers() {
        userService.deleteAllUsers()
    }
}