package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketSessionData
import com.github.jambeez.server.domain.User
import com.github.jambeez.server.domain.intent.IntentWrapper
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

data class JoinRequest(val sessionId: String, val user: User)

class LobbyHandler : Handler() {
    override fun handle(sessionData: WebsocketSessionData, message: TextMessage, intent: String) {
        when (intent) {
            "lobby:create" -> createLobby(sessionData, message, intent)
            "lobby:join" -> joinLobby(sessionData, message, intent)
            else -> unknown(LobbyHandler::class.java, sessionData, intent)
        }
    }

    private fun createLobby(sessionData: WebsocketSessionData, message: TextMessage, intent: String) {
        val user: User = objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("User object could not be deserialized")
        validateUser(sessionData, user)
        val session = sessionData.jamSessionController.newSession(user)
        IntentWrapper(intent, session).send(sessionData)
    }


    private fun joinLobby(sessionData: WebsocketSessionData, message: TextMessage, intent: String) {
        val joinRequest: JoinRequest = objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("JoinRequest object could not be deserialized")
        val session = sessionData.jamSessionController.joinSession(joinRequest.sessionId, joinRequest.user)
        IntentWrapper(intent, session).send(sessionData)
    }
}