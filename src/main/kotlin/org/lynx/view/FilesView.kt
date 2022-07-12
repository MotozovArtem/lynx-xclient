package org.lynx.view

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*
import java.time.format.DateTimeFormatter
import org.lynx.Style
import org.lynx.view.controller.FilesController

class FilesView : View("Lynx Files") {
    val filesController: FilesController by inject()


    override val root = vbox {
        hbox {
            addClass(Style.header)
            prefHeight = 50.0
            alignment = Pos.CENTER_LEFT
            paddingAll = 10.0
            label("Files") {
                addClass(Style.boldLabel)
            }
        }
        flowpane {
            paddingAll = 10.0
            vgap = 10.0
            hgap = 10.0
            for (file in filesController.filesList) {
                vbox {
                    addClass(Style.darkBackground)
                    addClass(Style.lightRounded)
                    spacing = 10.0
                    paddingAll = 10.0
                    label(file.sender) {
                        addClass(Style.boldLabel)
                    }
                    label(file.fileName)
                    label(file.time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))) {
                        addClass(Style.italicLabel)
                    }
                    button("Download") {
                        addClass(Style.boldLabel)
                        addClass(Style.lightRounded)
                        action {
                            // TODO: Save action
                        }
                    }
                    minWidth = 250.0
                    minHeight = 150.0
                    alignment = Pos.CENTER
                }
            }
        }
        hbox {
            paddingAll = 10.0
            button("Back") {
                addClass(Style.boldLabel)
                action {
                    replaceWith<MainView>()
                }
            }
            alignment = Pos.BOTTOM_RIGHT
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
        }
    }
}
