package com.github.jambeez.server.worker

import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

class PartHandler(domainController: DomainController, lobbyInformer: LobbyInformer) :
    Handler(domainController, lobbyInformer) {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            PART_CHANGE_BPM -> changeBPM(connectionData, message, intent)
            else -> unknown(PartHandler::class.java, connectionData, intent)
        }
    }

    private fun changeBPM(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val lobby = domainController.findLobby(connectionData.user) ?: throw WorkerException("User not in Lobby")
        val changeRequest: ChangeRequest = objectMapper.readValueOrNull(message.payload)
            ?: throw WorkerException("ChangeRequest could not be deserialized")

        if (lobby.parts.size <= changeRequest.partId) {
            throw WorkerException("Invalid PartId")
        }

        lobby.parts[changeRequest.partId].beatsPerMinute = changeRequest.bpm
        lobbyInformer.informAllOtherUsers(lobby, null, message)
    }

}

data class ChangeRequest(val partId: Int, val bpm: Int)