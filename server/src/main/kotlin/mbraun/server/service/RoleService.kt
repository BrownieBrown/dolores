package mbraun.server.service

import mbraun.server.model.Role
import mbraun.server.model.User
import mbraun.server.repository.RoleRepository
import mbraun.server.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class RoleService(private val roleRepository: RoleRepository, private val userRepository: UserRepository) {
    fun getRoles(): Collection<Role> {
        return roleRepository.findAll()
    }

    fun getRoleByName(name: String): Role {
        return roleRepository.findByName(name) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No role with name: $name exists."
        )
    }

    fun createRole(role: Role): Role {
        val roleExists = roleRepository.existsByName(role.name)

        if (roleExists) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Role with name: ${role.name} already exists.")
        }

        roleRepository.save(role)

        return role
    }

    fun deleteRoleByName(name: String) {
        val role = roleRepository.findByName(name) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No role with name: $name exists."
        )
        val users = userRepository.findAll()
        val usersWithRole = users.filter { user: User -> user.roles.contains(role) }

        usersWithRole.forEach { user: User -> user.roles.remove(role) }

        return roleRepository.delete(role)
    }
}