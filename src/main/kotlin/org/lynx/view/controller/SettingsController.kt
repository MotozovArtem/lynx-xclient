package org.lynx.view.controller

import javafx.beans.property.SimpleStringProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.*

class SettingsController: Controller() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(SettingsController::class.java)
    }


    val languageList = observableListOf("Русский", "English")
    val selectedLanguage= SimpleStringProperty("English")


}