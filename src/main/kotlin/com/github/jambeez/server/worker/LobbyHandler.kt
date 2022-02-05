package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.intent.IntentWrapper
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

data class JoinRequest(val sessionId: String)

class LobbyHandler : Handler() {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            "lobby:create" -> createLobby(connectionData, message, intent)
            "lobby:join" -> joinLobby(connectionData, message, intent)
            else -> unknown(LobbyHandler::class.java, connectionData, intent)
        }
    }

    private fun createLobby(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val session = connectionData.jamSessionController.newSession(connectionData.user)
        IntentWrapper(intent, session).send(connectionData)
    }


    private fun joinLobby(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val joinRequest: JoinRequest = objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("JoinRequest object could not be deserialized")
        val session = connectionData.jamSessionController.joinSession(joinRequest.sessionId, connectionData.user)
        IntentWrapper(intent, session).send(connectionData)
    }
}