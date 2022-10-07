package mbraun.server.controller

import mbraun.server.model.User
import mbraun.server.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
        val user = userService.getAllUser()

        return ResponseEntity(user, HttpStatus.OK)
    }

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        val user = userService.getUserByEmail(email)

        return ResponseEntity(user, HttpStatus.OK)

    }

    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        val newUser = userService.createUser(user)

        return ResponseEntity(newUser, HttpStatus.CREATED)
    }

    @PatchMapping
    fun updateUser(@RequestBody payload: User): ResponseEntity<User> {
        return ResponseEntity(userService.updateUser(payload), HttpStatus.OK)

    }

    @DeleteMapping("/{email}")
    fun deleteUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        userService.deleteUserByEmail(email)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping
    fun deleteAllUsers() {
        userService.deleteAllUsers()
    }
}