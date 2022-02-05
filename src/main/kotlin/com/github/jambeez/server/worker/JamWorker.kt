package com.github.jambeez.server.worker

import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.intent.IntentMessage
import org.springframework.web.socket.TextMessage

class JamWorker(
    private val domainController: DomainController,
    private val lobbyInformer: LobbyInformer,
    private val connectionData: WebsocketConnectionData,
    private val message: TextMessage,
    private val intent: String
) : Runnable {
    override fun run() {
        val globalIntent = intent.split(Regex(":"))[0]
        try {
            when (globalIntent) {
                LOBBY -> LobbyHandler(domainController, lobbyInformer).handle(connectionData, message, intent)
                USER -> UserHandler(domainController, lobbyInformer).handle(connectionData, message, intent)
                PART -> PartHandler(domainController, lobbyInformer).handle(connectionData, message, intent)
                else -> unknown(null, connectionData, intent)
            }
        } catch (e: Exception) {
            connectionData.websocketSession.sendMessage(IntentMessage("error:$intent", e.message ?: "").payload())
        }
    }
}

fun unknown(clazz: Class<*>?, connectionData: WebsocketConnectionData, intent: String) {
    IntentMessage("error:$intent", "Unknown intent $intent. Selected Handler: ${clazz ?: "UNKNOWN"}").send(
        connectionData
    )
}