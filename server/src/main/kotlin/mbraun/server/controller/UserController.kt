package mbraun.server.controller

import mbraun.server.model.User
import mbraun.server.service.UserService
import mbraun.server.util.RoleToUserForm
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/user")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUser(): Collection<User> = userService.getAllUser()

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): User = userService.getUserByEmail(email)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody user: User): User = userService.createUser(user)

    @PatchMapping
    fun updateUser(@RequestBody user: User): User = userService.updateUser(user)

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserByEmail(@PathVariable email: String): Unit = userService.deleteUserByEmail(email)

    @PostMapping("/role/add")
    @ResponseStatus(HttpStatus.CREATED)
    fun addRoleToUser(@RequestBody form: RoleToUserForm): User =
        userService.addRoleToUser(form.email, form.roleName)

    @DeleteMapping("/role/remove")
    fun removeRoleFromUser(@RequestBody form: RoleToUserForm): User =
        userService.removeRoleFromUser(form.email, form.roleName)
}