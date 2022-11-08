package mbraun.server.util

import mbraun.server.model.Role
import mbraun.server.model.User
import mbraun.server.repository.RoleRepository
import mbraun.server.repository.UserRepository
import mbraun.server.service.UserService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("test")
@Component
class DataInitializer(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val userService: UserService
) : ApplicationRunner {

    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {
        val roles = listOf(
            Role(
                null,
                "USER"
            ),

            Role(
                null,
                "MANAGER"
            ),
            Role(
                null,
                "ADMIN"
            ),
            Role(
                null,
                "SUPER_ADMIN"
            )
        )

        val users = listOf(
            User(
                email = "marco.braun2013@gmail.com",
                fullName = "Marco Braun",
                password = "1234"
            ),
            User(
                email = "yannick.seppich@gmx.de",
                fullName = "Yannick Seppich",
                password = "1234"
            ),
            User(
                email = "rainer.dirkmann@icloud.com",
                fullName = "Rainer Dirkmann",
                password = "1234"
            ),
            User(
                email = "miriam.hansel@yahoo.com",
                fullName = "Miriam Hansel",
                password = "1234"
            ),
            User(
                email = "josephin.wolf@icloud.com",
                fullName = "Josehphin Wolf",
                password = "1234"
            ),
            User(
                email = "manuel.engelmann@gmail.com",
                fullName = "Manuel Engelmann",
                password = "1234"
            ),
        )

        roles.forEach { role -> roleRepository.save(role) }

        users.forEach { user -> userRepository.save(user) }

        userService.addRoleToUser("marco.braun2013@gmail.com", "USER")
        userService.addRoleToUser("marco.braun2013@gmail.com", "MANAGER")
        userService.addRoleToUser("marco.braun2013@gmail.com", "ADMIN")
        userService.addRoleToUser("marco.braun2013@gmail.com", "SUPER_ADMIN")
        userService.addRoleToUser("yannick.seppich@gmx.de", "USER")
        userService.addRoleToUser("rainer.dirkmann@icloud.com", "USER")
        userService.addRoleToUser("miriam.hansel@yahoo.com", "USER")
        userService.addRoleToUser("josephin.wolf@icloud.com", "USER")
        userService.addRoleToUser("manuel.engelmann@gmail.com", "USER")
    }
}