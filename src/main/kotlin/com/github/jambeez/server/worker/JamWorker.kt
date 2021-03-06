package com.github.jambeez.server.worker

import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.intent.IntentMessage
import com.github.jambeez.server.worker.Handler.Companion.unknown
import org.springframework.web.socket.TextMessage

class JamWorker(
    private val domainController: DomainController,
    private val lobbyInformer: LobbyInformer,
    private val connectionData: WebsocketConnectionData,
    private val message: TextMessage,
    private val intent: String
) : Runnable {
    override fun run() {
        val rootIntent = intent.split(Regex(":"))[0]
        try {
            when (rootIntent) {
                LOBBY ->
                    LobbyHandler(domainController, lobbyInformer)
                        .handle(connectionData, message, intent)
                USER ->
                    UserHandler(domainController, lobbyInformer)
                        .handle(connectionData, message, intent)
                PART ->
                    PartHandler(domainController, lobbyInformer)
                        .handle(connectionData, message, intent)
                TRACK ->
                    TrackHandler(domainController, lobbyInformer)
                        .handle(connectionData, message, intent)
                else -> unknown<Handler>(null, connectionData, intent)
            }
        } catch (e: Exception) {
            connectionData.websocketSession.sendMessage(
                IntentMessage("error:$intent", e.message ?: "").payload())
        }
    }
}
