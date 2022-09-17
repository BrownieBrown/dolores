package mbraun.server.service

import mbraun.server.model.User
import mbraun.server.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(@Autowired private val userRepository: UserRepository) {

    fun getAllUser(): ResponseEntity<List<User>> {
        val user = userRepository.findAll()

        if (user.isEmpty()) {
            return ResponseEntity<List<User>>(user, HttpStatus.NOT_FOUND)
        }

        return ResponseEntity<List<User>>(user, HttpStatus.OK)
    }

    fun getUserByEmail(email: String): ResponseEntity<User> {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No user with email: $email exists.")

        return ResponseEntity<User>(user, HttpStatus.OK)
    }

    fun createUser(user: User): ResponseEntity<User> {
        val emailExists = userRepository.existsByEmail(user.email)

        if (emailExists) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A user with email: ${user.email} already exists.")
        }

        userRepository.save(user)

        return ResponseEntity<User>(user, HttpStatus.CREATED)
    }

    fun updateUser(email: String, user: User): ResponseEntity<User> {
        val userToUpdate = userRepository.findByEmail(email) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No user with email: $email exists."
        )

        userToUpdate.hashed_password = user.hashed_password
        userToUpdate.fullName = user.fullName
        val emailToUpdateExists = userRepository.existsByEmail(user.email)
        if (emailToUpdateExists) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A user with email: ${user.email} already exists.")
        }

        return ResponseEntity<User>(user, HttpStatus.ACCEPTED)
    }

    fun deleteUserByEmail(email: String): ResponseEntity<User> {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No user with email: $email exists.")

        userRepository.delete(user)

        return ResponseEntity<User>(user, HttpStatus.OK)
    }

    fun deleteAllUsers(): ResponseEntity<User> {
        userRepository.deleteAll()

        return ResponseEntity<User>(HttpStatus.OK)
    }
}