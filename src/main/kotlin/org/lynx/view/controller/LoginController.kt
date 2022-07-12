package org.lynx.view.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.*
import org.lynx.service.NetworkService
import org.lynx.service.UserService
import org.lynx.view.model.UserFormModel

class LoginController : Controller() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(LoginController::class.java)
    }

    val userService: UserService by di()
    val userForm = UserFormModel()

    fun login() {
        userService.login(
            userForm.username,
            userForm.password,
            userForm.domain
        )
    }
}