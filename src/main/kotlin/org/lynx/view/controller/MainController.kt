package org.lynx.view.controller

import com.google.crypto.tink.subtle.Base64
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.media.AudioClip
import kotlinx.coroutines.launch
import okio.IOException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import org.lynx.ApplicationScope
import org.lynx.domain.Message
import org.lynx.domain.MessageDataType
import org.lynx.domain.MessageModel
import org.lynx.domain.User
import org.lynx.domain.UserModel
import org.lynx.domain.Users
import org.lynx.service.CryptographyService
import org.lynx.service.MessageService
import org.lynx.service.UserService


class MainController : Controller() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(MainController::class.java)
    }

    private val messageService: MessageService by di()
    private val cryptographyService: CryptographyService by di()
    private val notificationSound =
        AudioClip(javaClass.classLoader.getResource("audio/message.wav")!!.toExternalForm())

    val userService: UserService by di()
    val userModelList: ObservableList<UserModel> = observableListOf()
    val messageModelList: ObservableList<MessageModel> = observableListOf()
    val selectedChat = SimpleObjectProperty<UserModel>()
    val chatSelected = SimpleBooleanProperty(false)
    val message: SimpleStringProperty = SimpleStringProperty("")


    init {
        updateUserList()
        userService.usersList.addListener(ListChangeListener {
            updateUserList()
        })
        messageService.addListener { chat, isMy ->
            ApplicationScope.launch {
                if (!isMy) {
                    notificationSound.play()
                }
                if (selectedChat.value?.username?.value.equals(chat)) {
                    getMessagesForChat(chat)
                } else {
                    userModelList.find {
                        it.username.value == selectedChat.value?.username?.value
                    }?.hasNewMessage?.value = true
                }
            }
        }
        selectedChat.addListener { _, _, newValue ->
            if (newValue != null) {
                getMessagesForChat(newValue.username.value)
                chatSelected.value = true
            } else {
                chatSelected.value = false
            }
        }
    }

    private fun updateUserList() {
        userModelList.clear()
        transaction {
            userModelList.addAll(
                User.all().filter {
                    it.username != userService.authenticatedUsername.value
                }.map {
                    UserModel().apply {
                        item = it
                    }
                }.asObservable()
            )
        }
    }

    fun getMessagesForChat(selectedChat: String) {
        MainController.log.debug("Receiving messages list for $selectedChat")
        messageModelList.clear()
        val ownerId = userService.getCurrentUser().id.value
        messageModelList.addAll(messageService.loadMessagesForChat(selectedChat, ownerId).map { message ->
            MessageModel().apply {
                this.message = if (message.message != null) {
                    cryptographyService.decrypt(
                        message.message!!,
                        cryptographyService.getSharedKeyForAbonent(selectedChat)!!,
                        Base64.decode(message.iv, Base64.NO_WRAP)
                    )
                } else {
                    message.fileName
                }
                fileName = message.fileName
                fileData = message.fileData
                messageType = message.messageType
                chatName = message.chatName
                senderName = message.senderName
                this.iv = Base64.decode(message.iv, Base64.NO_WRAP)
                unread = false
                time = message.time
                ownerName = userService.getCurrentUser().username
            }
        })
    }

    fun sendMessage(message: String, abonent: String) {
        ApplicationScope.launch {
            var sharedKeyForAbonent = cryptographyService.getSharedKeyForAbonent(abonent)
            if (sharedKeyForAbonent == null) {
                sharedKeyForAbonent = messageService.keyExchange(abonent)
            }
            val iv = cryptographyService.generateIv()
            val encryptedMessage =
                cryptographyService.encrypt(message, sharedKeyForAbonent, iv)
            lateinit var newMessage: Message
            transaction {
                val chat = User.find { Users.username eq abonent }.first()
                newMessage = Message.new {
                    this.message = Base64.encodeToString(encryptedMessage, Base64.NO_WRAP)
                    this.chat = chat
                    messageType = MessageDataType.MESSAGE
                    chatName = abonent
                    sender = userService.getCurrentUser()
                    senderName = userService.getCurrentUser().username
                    this.iv = Base64.encodeToString(iv, Base64.NO_WRAP)
                    unread = false
                    owner = userService.getCurrentUser()
                    ownerName = userService.getCurrentUser().username
                }
            }
            messageService.sendMessage(newMessage)
        }
    }

    fun sendFile(selectedFile: File, abonent: String) {
        ApplicationScope.launch {
            var sharedKeyForAbonent = cryptographyService.getSharedKeyForAbonent(abonent)
            if (sharedKeyForAbonent == null) {
                sharedKeyForAbonent = messageService.keyExchange(abonent)
            }
            lateinit var fileData: ByteArray
            try {
                val inputStream = selectedFile.inputStream()
                fileData = inputStream.use {
                    it.readBytes()
                }
            } catch (e: IOException) {
                MainController.log.error("Error while reading selected file", e)
                return@launch
            }
            val iv = cryptographyService.generateIv()
            val encryptedFileData =
                cryptographyService.encryptFile(
                    Base64.encodeToString(fileData, Base64.NO_WRAP),
                    sharedKeyForAbonent,
                    iv
                )
            lateinit var newMessage: Message
            transaction {
                val chat = User.find { Users.username eq abonent }.first()
                newMessage = Message.new {
                    // TODO: Maybe fix it, maybe remove
//                    this.message = Base64.encodeToString(
//                        """File: ${selectedFile.name}""".toByteArray(StandardCharsets.UTF_8),
//                        Base64.NO_WRAP
//                    )
                    this.chat = chat
                    chatName = abonent
                    sender = userService.getCurrentUser()
                    senderName = userService.getCurrentUser().username
                    this.iv = Base64.encodeToString(iv, Base64.NO_WRAP)
                    messageType = MessageDataType.FILE
                    this.fileData = Base64.encodeToString(encryptedFileData, Base64.NO_WRAP)
                    this.fileName = selectedFile.name
                    unread = false
                    owner = userService.getCurrentUser()
                    ownerName = userService.getCurrentUser().username
                }
            }
            messageService.sendFile(newMessage)
        }
    }

    fun downloadFile(message: MessageModel, selectedFile: File) {
        try {
            val outputStream = selectedFile.outputStream()
            val sharedKey = cryptographyService.getSharedKeyForAbonent(message.chatName)
            val decryptedFile = Base64.decode(
                cryptographyService.decryptFile(message.fileData!!, sharedKey!!, message.iv),
                Base64.NO_WRAP
            )
            outputStream.use {
                it.write(decryptedFile)
            }
        } catch (e: IOException) {
            MainController.log.error("Error while saving selected file", e)
        } catch (e: Exception) {
            MainController.log.error("Error while decrypting file", e)
        }
    }

    fun logout() {
        userService.logout();
    }
}