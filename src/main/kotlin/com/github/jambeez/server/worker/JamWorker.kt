package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketSessionData
import com.github.jambeez.server.domain.intent.IntentMessage
import org.springframework.web.socket.TextMessage

class JamWorker(private val sessionData: WebsocketSessionData, private val message: TextMessage, private val intent: String) : Runnable {
    override fun run() {
        val globalIntent = intent.split(Regex(":"))[0]
        try {
            when (globalIntent) {
                "lobby" -> LobbyHandler().handle(sessionData, message, intent)
                "user" -> UserHandler().handle(sessionData, message, intent)
                else -> unknown(null, sessionData, intent)
            }
        } catch (e: Exception) {
            sessionData.websocketSession.sendMessage(IntentMessage("error:$intent", e.message ?: "").payload())
        }
    }
}

fun unknown(clazz: Class<*>?, sessionData: WebsocketSessionData, intent: String) {
    IntentMessage(intent, "Unknown intent $intent. Selected Handler: ${clazz ?: "UNKNOWN"}").send(sessionData)
}