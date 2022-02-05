package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

class PartHandler : Handler() {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            PART_CHANGE_BPM -> changeBPM(connectionData, message, intent)
            else -> unknown(PartHandler::class.java, connectionData, intent)
        }
    }

    private fun changeBPM(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val jamSession = connectionData.jamSessionController.findJamSession(connectionData.user) ?: throw WorkerException("User not in JamSession")
        val changeRequest: ChangeRequest = objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("ChangeRequest could not be deserialized")

        if (jamSession.parts.size <= changeRequest.partId) {
            throw WorkerException("Invalid PartId")
        }

        jamSession.parts[changeRequest.partId].beatsPerMinute = changeRequest.bpm
        connectionData.jamSessionInformer.informAllOtherUsers(jamSession, null, message)
    }

}

data class ChangeRequest(val partId: Int, val bpm: Int)