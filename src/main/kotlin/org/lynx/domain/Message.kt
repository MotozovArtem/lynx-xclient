package org.lynx.domain

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID
import org.lynx.http.client.MessageRequest

object Messages : UUIDTable("lynx_message") {
    val sender = reference("sender", Users)
    val senderName = varchar("sender_name", 255)
    val message = text("message").nullable()
    val messageType =
        customEnumeration(
            "message_type",
            "VARCHAR(255)",
            { value -> MessageDataType.valueOf(value as String) },
            { it.name })
    val fileData = text("file_data").nullable()
    val fileName = text("file_name").nullable()
    val chat = reference("chat", Users)
    val chatName = varchar("chat_name", 255)
    val iv = varchar("iv", 50)
    val time = datetime("creation_time").default(LocalDateTime.now())
    val unread = bool("unread").default(true)
    val owner = reference("owner", Users)
    val ownerName = varchar("owner_name", 255)
}

class Message(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Message>(Messages)

    var sender by User referencedOn Messages.sender
    var senderName by Messages.senderName
    var message by Messages.message
    var messageType by Messages.messageType
    var fileData by Messages.fileData
    var fileName by Messages.fileName
    var chat by User referencedOn Messages.chat
    var chatName by Messages.chatName
    var iv by Messages.iv
    var time by Messages.time
    var unread by Messages.unread
    var owner by User referencedOn Messages.owner
    var ownerName by Messages.ownerName

    override fun toString(): String {
        return "Message(sender=$sender, senderName='$senderName', message=$message, messageType=$messageType, fileData=$fileData, fileName=$fileName, chat=$chat, chatName='$chatName', iv='$iv', time=$time, unread=$unread, owner=$owner, ownerName='$ownerName')"
    }
}

fun Message.toDto(model: Message): MessageRequest {
    return MessageRequest(model.senderName, model.message!!, model.iv, model.chatName)
}

class MessageModel {
    lateinit var sender: User
    lateinit var senderName: String
    var message: String? = null
    lateinit var messageType: MessageDataType
    var fileData: String? = null
    var fileName: String? = null
    lateinit var chat: User
    lateinit var chatName: String
    lateinit var iv: ByteArray
    lateinit var time: LocalDateTime
    var unread: Boolean = false
    lateinit var owner: User
    lateinit var ownerName: String
}