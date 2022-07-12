package org.lynx.view.model

import java.time.LocalDateTime
import java.util.UUID

class FilesModel {
    lateinit var time: LocalDateTime
    lateinit var messageId: UUID
    lateinit var fileName: String
    lateinit var sender: String
}