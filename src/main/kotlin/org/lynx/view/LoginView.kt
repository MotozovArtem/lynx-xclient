package org.lynx.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import tornadofx.*
import org.lynx.Style
import org.lynx.view.controller.LoginController

class LoginView : View("Lynx Login") {
    val loginController: LoginController by inject()

    override val root = vbox {
        setPrefSize(450.0, 600.0)
        setMinSize(450.0, 600.0)
        alignment = Pos.CENTER
        paddingAll = 50.0
        vbox {
            addClass(Style.lynxForm)
            alignment = Pos.CENTER
            padding = Insets(5.0, 10.0, 5.0, 10.0)
            spacing = 10.0
            imageview("img/LynxChatWhite.png") {
                fitHeight = 180.0
                fitWidth = 250.0
            }
            label("Login") {
                addClass(Style.boldLabel)
            }
            textfield(loginController.userForm.usernameProperty) {
                addClass(Style.formInput)
            }
            label("Password") {
                addClass(Style.boldLabel)
            }
            passwordfield(loginController.userForm.passwordProperty) {
                addClass(Style.formInput)
            }
            label("Server") {
                addClass(Style.boldLabel)
            }
            textfield(loginController.userForm.domainProperty) {
                addClass(Style.formInput)
            }

            label(loginController.userService.loginResult) {
                addClass(Style.italicLabel)
            }

            button("Login") {
                addClass(Style.boldLabel)
                isDefaultButton = true
                action {
                    enableWhen(loginController.userService.loginEnded)
                    loginController.login()
                }
            }
        }
    }


    init {
        loginController.userService.isAuthenticated.addListener { _, _, newValue ->
            if (newValue) {
                loginController.userService.loginResult.value = ""
                close()
                find<MainView>().openWindow()
            }
        }

    }
}
