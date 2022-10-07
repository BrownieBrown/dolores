package mbraun.server.service

import mbraun.server.model.User
import mbraun.server.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(@Autowired private val userRepository: UserRepository) {

    fun getAllUser(): MutableList<User> {
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
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with email: ${user.email} already exists.")
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
}