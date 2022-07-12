package org.lynx.domain

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import tornadofx.*
import java.util.UUID


object Users : UUIDTable("lynx_user") {
    var username = varchar("username", 255).uniqueIndex()
    var domains = text("domains")
    var online = bool("online")
    var hasNewMessage = bool("hasNewMessage").default(false)
}

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var username by Users.username
    var domains by Users.domains
    var online by Users.online
    var hasNewMessage by Users.hasNewMessage

    override fun toString(): String {
        return "User(username='$username', domains='$domains', online=$online, hasNewMessage=$hasNewMessage)"
    }
}

class UserModel : ItemViewModel<User>() {
    val username = bind(User::username)
    val domains = bind(User::domains)
    val online = bind(User::online)
    val hasNewMessage = bind(User::hasNewMessage)
}