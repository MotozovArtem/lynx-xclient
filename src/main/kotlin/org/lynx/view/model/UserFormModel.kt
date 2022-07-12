package org.lynx.view.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class UserFormModel(username: String? = null, password: String? = null, domain: String? = null) {
    val usernameProperty = SimpleStringProperty(this, "username", username)
    var username by usernameProperty

    val passwordProperty = SimpleStringProperty(this, "password", password)
    var password by passwordProperty

    val domainProperty = SimpleStringProperty(this, "domain", domain)
    var domain by domainProperty

    val validProperty = SimpleBooleanProperty(this, "valid", true)
    var valid by validProperty
}