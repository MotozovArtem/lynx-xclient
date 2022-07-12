package org.lynx.view.controller

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.*
import org.lynx.domain.Message
import org.lynx.domain.MessageDataType
import org.lynx.domain.Messages
import org.lynx.service.CryptographyService
import org.lynx.service.UserService
import org.lynx.view.model.FilesModel

class FilesController : Controller() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(FilesController::class.java)
    }

    private val userService: UserService by di()
    private val cryptographyService: CryptographyService by di()

    val filesList = observableListOf<FilesModel>()

    init {
        transaction {
            filesList.clear()
            filesList.addAll(Message.find {
                (Messages.owner eq userService.getCurrentUserId()) and (Messages.messageType eq MessageDataType.FILE)
            }
                .orderBy(Messages.time to SortOrder.ASC).map {
                    FilesModel().apply {
                        time = it.time
                        messageId = it.id.value
                        fileName = it.fileName!!
                        sender = it.senderName
                    }
                })
        }
    }
}