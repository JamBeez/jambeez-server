package com.github.jambeez.server.worker

import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Part
import com.github.jambeez.server.domain.intent.IntentWrapper
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

data class JoinRequest(val sessionId: String)
data class Parts(val parts: MutableList<Part>)

class LobbyHandler(domainController: DomainController, lobbyInformer: LobbyInformer) :
    Handler(domainController, lobbyInformer) {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            LOBBY_CREATE -> createLobby(connectionData, message, intent)
            LOBBY_JOIN -> joinLobby(connectionData, message, intent)
            LOBBY_UPDATE_PARTS -> updateParts(connectionData, message, intent)
            else -> unknown(LobbyHandler::class.java, connectionData, intent)
        }
    }


    private fun createLobby(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val session = domainController.createLobby(connectionData.user)
        IntentWrapper(intent, session).send(connectionData)
    }


    private fun joinLobby(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val joinRequest: JoinRequest = objectMapper.readValueOrNull(message.payload)
            ?: throw WorkerException("JoinRequest object could not be deserialized")
        val session = domainController.joinLobby(joinRequest.sessionId, connectionData.user)

        // Send lobby to me
        IntentWrapper(intent, session).send(connectionData)
        // Send to others
        lobbyInformer.informAllOtherUsers(
            session,
            connectionData.user,
            IntentWrapper(USER_JOINED, connectionData.user).payload()
        )
    }


    private fun updateParts(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val lobby =
            domainController.findLobby(connectionData.user) ?: throw WorkerException("User not in Lobby")
        val parts: Parts =
            objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("Parts could not be deserialized")
        lobby.parts.clear()
        lobby.parts.addAll(parts.parts)
        // Inform other about part update
        lobbyInformer.informAllOtherUsers(lobby, connectionData.user, message)
    }
}