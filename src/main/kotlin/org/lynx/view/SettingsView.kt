package org.lynx.view

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*
import org.lynx.Style
import org.lynx.view.controller.SettingsController

class SettingsView : View("Lynx Settings") {
    private val settingsController: SettingsController by inject()

    override val root = vbox {
        hbox {
            addClass(Style.header)
            label("Settings") {
                addClass(Style.boldLabel)
            }
            prefHeight = 50.0
            alignment = Pos.CENTER_LEFT
            paddingAll = 10.0
        }
        vbox {
            paddingAll = 10.0
            spacing = 5.0
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            vbox {
                label("Language") {
                    addClass(Style.boldLabel)
                }
                choicebox(settingsController.selectedLanguage, settingsController.languageList) {
                    addClass(Style.settingsChoiceBox)
                    minWidth = 220.0
                }
                spacing = 10.0
            }
            vbox {
                label("Registration") {
                    addClass(Style.boldLabel)
                }
                button("New Account") {
                    addClass(Style.boldLabel)
                    action {
                        //TODO: Open dialog with registration data
                    }
                    minWidth = 220.0
                }
                spacing = 10.0
            }
            vbox {
                label("Accounts & Devices") {
                    addClass(Style.boldLabel)
                }
                button("Synchronize Devices") {
                    addClass(Style.boldLabel)
                    action {
                        // TODO: Open wizard with synchronization
                    }
                    minWidth = 220.0
                }
                button("Add New Device") {
                    addClass(Style.boldLabel)
                    action {
                        // TODO: Open dialog with new domain form
                    }
                    minWidth = 220.0
                }
                spacing = 10.0
            }
            hbox {
                button("Cancel") {
                    addClass(Style.redButton)
                    addClass(Style.boldLabel)
                    action {
                        replaceWith<MainView>()
                    }
                }
                button("Save") {
                    addClass(Style.boldLabel)
                    action {
                        // TODO: Save settings
                        replaceWith<MainView>()
                    }
                }
                spacing = 10.0
                alignment = Pos.BOTTOM_RIGHT
                vboxConstraints {
                    vGrow = Priority.ALWAYS
                }
            }

        }
    }
}
