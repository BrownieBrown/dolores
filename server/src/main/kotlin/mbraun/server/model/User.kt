package mbraun.server.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToMany
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
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
    @ManyToMany(fetch = FetchType.EAGER)
    var roles: MutableList<Role> = arrayListOf()
)