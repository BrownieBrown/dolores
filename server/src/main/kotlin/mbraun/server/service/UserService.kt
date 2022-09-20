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
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "A user with email: ${user.email} already exists.")
        }

        return userRepository.save(user)
    }

    fun updateUser(email: String, user: User): User {

        val userExists = userRepository.existsByEmail(email)

        return if (userExists) {
            userRepository.save(
                User(
                    id = user.id,
                    email = user.email,
                    fullName = user.fullName,
                    hashed_password = user.hashed_password,
                    isActive = user.isActive,
                    isSuperUser = user.isSuperUser
                )
            )
        } else {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No user with email: $email exists."
            )
        }
    }

    fun deleteUserByEmail(email: String) {
        val user = userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: $email exists."
        )

        return userRepository.deleteByEmail(user.email)
    }

    fun deleteAllUsers() {
        return userRepository.deleteAll()
    }
}