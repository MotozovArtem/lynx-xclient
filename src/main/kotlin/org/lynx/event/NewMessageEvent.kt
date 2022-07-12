package org.lynx.event

import tornadofx.*

class NewMessageEvent(val chatName: String) : FXEvent()