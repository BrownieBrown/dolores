package mbraun.server.model

import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "user_data")
data class User(
    @Id
    @GeneratedValue
    val id: UUID = UUID.randomUUID(),
    var email: String = "",
    var fullName: String = "",
    var password: String = "",
    var isActive: Boolean = false,
    var isSuperUser: Boolean = false
)