package org.lynx.view

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.time.format.DateTimeFormatter
import org.lynx.Style
import org.lynx.domain.MessageDataType
import org.lynx.domain.MessageModel
import org.lynx.domain.UserModel
import org.lynx.view.controller.MainController

class MainView : View("Lynx") {
    private val mainController: MainController by inject()

    private val settingsImage = Image(javaClass.classLoader.getResource("img/Settings.png")!!.toExternalForm())
    private val logoutImage = Image(javaClass.classLoader.getResource("img/Logout.png")!!.toExternalForm())
    private val onlineImage = Image(javaClass.classLoader.getResource("img/Online.png")!!.toExternalForm())
    private val offlineImage = Image(javaClass.classLoader.getResource("img/Offline.png")!!.toExternalForm())
    private val fileWindowImage = Image(javaClass.classLoader.getResource("img/FileWindow.png")!!.toExternalForm())
    private val notificationBlueImage =
        Image(javaClass.classLoader.getResource("img/NotificationBlue.png")!!.toExternalForm())
    private val notificationBlackImage =
        Image(javaClass.classLoader.getResource("img/NotificationBlack.png")!!.toExternalForm())
    private val attachFileImage = Image(javaClass.classLoader.getResource("img/AttachFile.png")!!.toExternalForm())
    private val attachFileImageView = ImageView(attachFileImage)


    override val root = vbox {
        setPrefSize(1280.0, 720.0)
        hbox {
            addClass(Style.header)
            prefHeight = 50.0
            paddingAll = 5.0
            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            label(mainController.userService.getCurrentUserName()) {
                addClass(Style.boldLabel)
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
            imageview(fileWindowImage) {
                fitHeight = 30.0
                fitWidth = 30.0
                onMouseClicked = EventHandler {
                    replaceWith<FilesView>()
                }
            }
            imageview(settingsImage) {
                fitHeight = 30.0
                fitWidth = 30.0
                onMouseClicked = EventHandler {
                    replaceWith<SettingsView>()
                }
            }
            imageview(logoutImage) {
                fitHeight = 30.0
                fitWidth = 30.0
                onMouseClicked = EventHandler {
                    mainController.userService.logout()
                }
            }
        }
        vbox {
            paddingHorizontal = 10.0
            paddingBottom = 10.0
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            maxHeight = Double.MAX_VALUE
            hbox {
                spacing = 10.0
                vboxConstraints {
                    vGrow = Priority.ALWAYS
                }
                maxHeight = Double.MAX_VALUE
                vbox {
                    listview<UserModel> {
                        paddingAll = 0.0
                        items = mainController.userModelList
                        bindSelected(mainController.selectedChat)
                        cellFormat {
                            graphic = hbox {
                                spacing = 10.0
                                alignment = Pos.CENTER
                                label(it.username.value) {
                                    addClass(Style.boldLabel)
                                    alignment = Pos.CENTER_LEFT
                                    maxWidth = Double.MAX_VALUE
                                    hboxConstraints {
                                        hGrow = Priority.ALWAYS
                                    }
                                }
                                if (it.online.value) {
                                    imageview(onlineImage)
                                } else {
                                    imageview(offlineImage)
                                }
                                if (it.hasNewMessage.value) {
                                    imageview(notificationBlueImage)
                                } else {
                                    imageview(notificationBlackImage)
                                }
                            }
                        }
                        minWidth = 300.0
                        maxHeight = Double.MAX_VALUE
                        vboxConstraints {
                            vGrow = Priority.ALWAYS
                        }
                    }
                }
                vbox {
                    paddingTop = 5.0
                    listview(mainController.messageModelList) {
                        addClass(Style.messageList)
                        paddingAll = 0.0
                        cellFormat {
                            graphic = borderpane {
                                if (it.senderName == mainController.userService.getCurrentUserName()) {
                                    right = vbox {
                                        addClass(Style.ownerMessage)

                                        label(it.senderName) {
                                            addClass(Style.boldLabel)
                                        }
                                        if (it.messageType == MessageDataType.MESSAGE) {
                                            label(it.message!!) {
                                                addClass(Style.messageText)
                                            }
                                        } else {
                                            label(it.fileName!!) {
                                                addClass(Style.fileLink)
                                                addClass(Style.messageText)
                                                onMouseClicked = EventHandler { _ ->
                                                    downloadFile(it)
                                                }
                                            }
                                        }
                                        label(it.time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))) {
                                            addClass(Style.italicLabel)
                                            maxWidth = Double.MAX_VALUE
                                            alignment = Pos.BOTTOM_RIGHT
                                        }
                                        minWidth = 180.0
                                        maxWidth = 450.0
                                        paddingAll = 10.0
                                    }
                                } else {
                                    left = vbox {
                                        addClass(Style.otherMessage)

                                        label(it.senderName) {
                                            addClass(Style.boldLabel)
                                        }
                                        if (it.messageType == MessageDataType.MESSAGE) {
                                            label(it.message!!) {
                                                addClass(Style.messageText)
                                            }
                                        } else {
                                            label(it.fileName!!) {
                                                addClass(Style.fileLink)
                                                addClass(Style.messageText)
                                                onMouseClicked = EventHandler { _ ->
                                                    downloadFile(it)
                                                }
                                            }
                                        }
                                        label(it.time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))) {
                                            addClass(Style.italicLabel)
                                            maxWidth = Double.MAX_VALUE
                                            alignment = Pos.BOTTOM_RIGHT
                                        }
                                        minWidth = 180.0
                                        maxWidth = 450.0
                                        paddingAll = 10.0
                                    }
                                }
                            }
                        }
                        maxWidth = Double.MAX_VALUE
                        maxHeight = Double.MAX_VALUE
                        vboxConstraints {
                            vGrow = Priority.ALWAYS
                        }
                    }
                    hbox {
                        val hboxHeight = this.prefHeightProperty()
                        button(graphic = attachFileImageView) {
                            addClass(Style.roundedLeftButton)
                            enableWhen(mainController.chatSelected)
                            action {
                                sendFile()
                            }
                            maxHeight = Double.MAX_VALUE
                            this.prefHeightProperty().bind(hboxHeight)
                        }
                        textarea(mainController.message) {
                            addClass(Style.messageBox)
                            maxHeight = 70.0
                            maxWidth = Double.MAX_VALUE
                            hboxConstraints {
                                hGrow = Priority.ALWAYS
                            }
                        }
                        button("Send") {
                            enableWhen(mainController.chatSelected)
                            addClass(Style.roundedRightButton)
                            addClass(Style.boldLabel)
                            action {
                                mainController.sendMessage(
                                    mainController.message.value,
                                    mainController.selectedChat.value.username.value
                                )
                            }
                            maxHeight = Double.MAX_VALUE
                            this.prefHeightProperty().bind(hboxHeight)
                        }
                    }
                    maxWidth = Double.MAX_VALUE
                    maxHeight = Double.MAX_VALUE
                    hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
                    spacing = 10.0
                }

            }
        }
    }

    fun sendFile() {
        val selectedFileList = chooseFile(
            mode = FileChooserMode.Single,
            title = "Choose File",
            filters = arrayOf(FileChooser.ExtensionFilter("All files", "*.*"))
        )
        if (selectedFileList.isEmpty()) {
            return
        }
        val selectedFile = selectedFileList[0]
        mainController.sendFile(selectedFile, mainController.selectedChat.value.username.value)
    }

    fun downloadFile(message: MessageModel) {
        val home = System.getProperty("user.home")
        val downloadDirectory = File("$home/Downloads")

        val selectedFileList = chooseFile(
            mode = FileChooserMode.Save,
            title = "Choose File To Save",
            filters = arrayOf(FileChooser.ExtensionFilter("All files", "*.*")),
            initialDirectory = downloadDirectory
        )
        if (selectedFileList.isEmpty()) {
            return
        }
        val selectedFile = selectedFileList[0]
        mainController.downloadFile(message, selectedFile)
    }

    init {
        mainController.userService.isAuthenticated.addListener { _, _, newValue ->
            if (!newValue) {
                close()
                find<LoginView>().openWindow()
            }
        }
    }
}