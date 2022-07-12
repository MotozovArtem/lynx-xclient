package org.lynx.domain

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object AbonentKeys : LongIdTable("lynx_abonent_key") {
    val abonentId = reference("abonent_id", Users)
    val abonentName = varchar("abonent_name", 255)
    val abonentPublicKey = varchar("abonent_public_key", 255)
    val sharedKey = varchar("shared_key", 255)
    val creationTime = datetime("creation_time").default(LocalDateTime.now())
    val lastModifiedDate = datetime("last_modified_date").default(LocalDateTime.now())
}

class AbonentKey(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AbonentKey>(AbonentKeys)

    var abonentId by User referencedOn AbonentKeys.abonentId
    var abonentName by AbonentKeys.abonentName
    var abonentPublicKey by AbonentKeys.abonentPublicKey
    var sharedKey by AbonentKeys.sharedKey
    var creationTime by AbonentKeys.creationTime
    var lastModifiedDate by AbonentKeys.lastModifiedDate

    override fun toString(): String {
        return "AbonentKey(abonentId=$abonentId, abonentName='$abonentName', abonentPublicKey='$abonentPublicKey', sharedKey='$sharedKey', creationTime=$creationTime, lastModifiedDate=$lastModifiedDate)"
    }

}