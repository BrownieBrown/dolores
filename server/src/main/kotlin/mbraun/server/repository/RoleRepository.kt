package mbraun.server.repository

import mbraun.server.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    fun existsByName(name: String): Boolean
    fun findByName(name: String): Role?

}