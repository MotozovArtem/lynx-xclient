package org.lynx.service

import com.google.inject.Inject
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import org.lynx.domain.Message
import org.lynx.domain.MessageDataType
import org.lynx.domain.Messages
import org.lynx.domain.User
import org.lynx.domain.Users
import org.lynx.event.MessageListener
import org.lynx.http.client.Certificate
import org.lynx.http.client.FileRequest
import org.lynx.http.client.MessageRequest


interface MessageService {

    suspend fun sendMessage(message: Message)

    // TODO: Generalize message/file sending
    suspend fun sendFile(message: Message)

    suspend fun keyExchange(abonent: String): ByteArray

    fun receiveMessage(message: MessageRequest)

    fun receiveFile(file: FileRequest)

    fun loadMessagesForChat(chat: String, ownerId: UUID): List<Message>

    fun setChatIsRead(chat: String, ownerId: UUID)

    fun getChats(): List<User>

    fun addListener(listener: MessageListener)
}

@Singleton
class MessageServiceImpl : MessageService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(MessageServiceImpl::class.java)
    }

    val listeners: MutableList<MessageListener> = mutableListOf()

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var networkService: NetworkService

    @Inject
    private lateinit var cryptographyService: CryptographyService

    override suspend fun keyExchange(abonent: String): ByteArray {
        log.info("Started key exchange with $abonent")
        val abonentLynxChatHttpClientList = networkService.getLynxChatHttpClientByAbonent(abonent)
        lateinit var sharedKey: ByteArray
        for (httpClient in abonentLynxChatHttpClientList) {
            val sendCert = httpClient.sendCertificate(
                Certificate(
                    userService.getCurrentUserName(),
                    cryptographyService.getPublicKeyAsBase64()
                )
            )
            when (sendCert.body()) {
                is Certificate -> {
                    log.info("Abonent public key received")
                    val abonentCertificate = sendCert.body()!!
                    sharedKey = cryptographyService.generateSharedKeyForAbonent(
                        abonentCertificate.username,
                        abonentCertificate.publicKey
                    )
                }
                else -> {
                    log.error("Key exchange failed")
                    throw Exception("Key exchange failed")
                }
            }
        }
        return sharedKey
    }

    override suspend fun sendMessage(message: Message) {
        val chatHttpClients = networkService.getLynxChatHttpClientByAbonent(message.chatName)
        var oneSuccessful = false
        for (httpClient in chatHttpClients) {
            val response = httpClient.sendMessage(
                MessageRequest(
                    message.senderName,
                    message.message!!,
                    message.iv,
                    message.chatName
                )
            )
            when (response.body()) {
                is String -> {
                    try {
                        val result = response.body()!!
                        if (result == "OK") {
                            log.info("Message sent successfully")
                            oneSuccessful = true
                        } else {
                            log.info("Message sending failed")
                        }
                    } catch (e: Exception) {
                        log.error("Error while processing response \n${response.raw()}")
                    }
                }
                else -> {
                    log.error("Error while sending message \n${response.raw()}")
                }
            }
        }
        if (!oneSuccessful) {
            message.delete()
        } else {
            fireNewMessageEvent(message.chatName, true)
        }
    }

    override suspend fun sendFile(message: Message) {
        val chatHttpClients = networkService.getLynxChatHttpClientByAbonent(message.chatName)
        var oneSuccessful = false
        for (httpClient in chatHttpClients) {
            val response = httpClient.sendFile(
                FileRequest(
                    message.senderName,
                    message.fileData!!,
                    message.fileName!!,
                    message.iv,
                    message.chatName
                )
            )
            when (response.body()) {
                is String -> {
                    try {
                        val result = response.body()!!
                        if (result == "OK") {
                            log.info("File sent successfully")
                            oneSuccessful = true
                        } else {
                            log.info("File sending failed")
                        }
                    } catch (e: Exception) {
                        log.error("Error while processing response \n${response.raw()}")
                    }
                }
                else -> {
                    log.error("Error while sending message \n${response.raw()}")
                }
            }
        }
        if (!oneSuccessful) {
            transaction {
                message.delete()
            }
        } else {
            fireNewMessageEvent(message.chatName, true)
        }
    }

    override fun receiveMessage(message: MessageRequest) {
        transaction {
            val chat = User.find { Users.username eq message.username }.first()
            Message.new {
                this.message = message.message
                unread = true
                iv = message.iv
                messageType = MessageDataType.MESSAGE
                senderName = message.username
                this.sender = chat
                chatName = message.username
                this.chat = chat
                owner = userService.getCurrentUser()
                ownerName = userService.authenticatedUsername.value
            }
            log.info("New message received and inserted into database")
        }
        fireNewMessageEvent(message.username, false)
    }

    override fun receiveFile(file: FileRequest) {
        transaction {
            val chat = User.find { Users.username eq file.username }.first()
            Message.new {
                this.fileName = file.fileName
                this.fileData = file.fileData
                unread = true
                iv = file.iv
                messageType = MessageDataType.FILE
                senderName = file.username
                this.sender = chat
                chatName = file.username
                this.chat = chat
                owner = userService.getCurrentUser()
                ownerName = userService.authenticatedUsername.value
            }
            log.info("New message received and inserted into database")
        }
        fireNewMessageEvent(file.username, false)
    }


    override fun loadMessagesForChat(chat: String, ownerId: UUID): List<Message> {
        lateinit var messagesForChat: List<Message>
        transaction {
            messagesForChat =
                Message.find { (Messages.chatName eq chat) and (Messages.owner eq ownerId) }
                    .orderBy(Messages.time to SortOrder.ASC).toMutableList()
        }
        return messagesForChat
    }

    override fun setChatIsRead(chat: String, ownerId: UUID) {
        transaction {
            Messages.update({ (Messages.chatName eq chat) and Messages.unread and (Messages.owner eq ownerId) }) {
                it[unread] = false
            }
        }
    }

    override fun getChats(): List<User> {
        var chatList: List<User> = listOf()
        transaction {
            chatList = User.all().toList()
        }
        return chatList
    }

    override fun addListener(listener: MessageListener) {
        listeners.add(listener)
    }

    private fun fireNewMessageEvent(abonent: String, isMyMessage: Boolean) {
        for (listener in listeners) {
            listener.onNewMessage(abonent, isMyMessage)
        }
    }
}