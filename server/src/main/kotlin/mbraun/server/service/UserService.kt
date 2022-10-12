package mbraun.server.service

import mbraun.server.model.User
import mbraun.server.repository.RoleRepository
import mbraun.server.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(private val userRepository: UserRepository, private val roleRepository: RoleRepository) {

    fun getAllUser(): Collection<User> {
        return userRepository.findAll()
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: $email exists."
        )
    }

    fun createUser(user: User): User {
        val emailExists = userRepository.existsByEmail(user.email)

        if (emailExists) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A user with email: ${user.email} already exists.")
        }

        userRepository.save(user)

        return user
    }

    fun updateUser(user: User): User {
        val currentUser = userRepository.findByEmail(user.email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: ${user.email} exists."
        )
        userRepository.delete(currentUser)
        userRepository.save(user)

        return user
    }

    fun deleteUserByEmail(email: String) {
        val user = userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: $email exists."
        )

        return userRepository.delete(user)
    }

    fun deleteAllUsers() {
        return userRepository.deleteAll()
    }

    fun addRoleToUser(email: String, roleName: String): User {
        val user = userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: $email exists."
        )

        val role = roleRepository.findByName(roleName) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No role with name: $roleName exists."
        )

        if (user.roles.contains(role)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "User with email: $email already has role."
            )
        }

        user.roles.add(role)
        return userRepository.save(user)
    }

    fun removeRoleFromUser(email: String, roleName: String): User {
        val user = userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: $email exists."
        )

        val role = roleRepository.findByName(roleName) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No role with name: $roleName exists."
        )

        if (!user.roles.contains(role)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "The user with email: $email does not posses this role."
            )
        }

        user.roles.remove(role)
        return userRepository.save(user)
    }
}