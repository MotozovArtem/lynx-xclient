package org.lynx.event

import java.util.EventListener

@FunctionalInterface
fun interface MessageListener : EventListener {
    fun onNewMessage(abonent: String, myMessage: Boolean)
}