package mbraun.server.controller

import mbraun.server.model.Role
import mbraun.server.service.RoleService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/role")
class RoleController(private val roleService: RoleService) {

    @GetMapping
    fun getRoles(): Collection<Role> = roleService.getRoles()

    @GetMapping("/{name}")
    fun getRoleByName(@PathVariable name: String): Role = roleService.getRoleByName(name)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRole(@RequestBody role: Role): Role = roleService.createRole(role)

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRoleByName(@PathVariable name: String): Unit = roleService.deleteRoleByName(name)
}