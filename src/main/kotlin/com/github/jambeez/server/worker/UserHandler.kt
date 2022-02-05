package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketSessionData
import com.github.jambeez.server.domain.intent.IntentWrapper
import org.springframework.web.socket.TextMessage

class UserHandler : Handler() {
    override fun handle(sessionData: WebsocketSessionData, message: TextMessage, intent: String) {
        when (intent) {
            "user:create" -> createUser(sessionData, message, intent)
            else -> unknown(LobbyHandler::class.java, sessionData, intent)
        }
    }

    private fun createUser(sessionData: WebsocketSessionData, message: TextMessage, intent: String) {
        val user = sessionData.userController.createUser()
        IntentWrapper(intent, user).send(sessionData)
    }
}
