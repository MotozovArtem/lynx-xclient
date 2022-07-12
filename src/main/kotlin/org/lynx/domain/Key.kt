package org.lynx.domain

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Keys : LongIdTable("lynx_my_key") {
    val privateKey = varchar("private_key", 255).uniqueIndex()
    val publicKey = varchar("public_key", 255).uniqueIndex()
    val creationTime = datetime("creation_time").default(LocalDateTime.now())
}

class Key(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Key>(Keys)

    var privateKey by Keys.privateKey
    var publicKey by Keys.publicKey
    var creationTime by Keys.creationTime

    override fun toString(): String {
        return "Key(privateKey='$privateKey', publicKey='$publicKey', creationTime=$creationTime)"
    }
}