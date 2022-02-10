package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.User
import com.github.jambeez.server.domain.intent.Intent
import com.github.jambeez.server.domain.intent.IntentMessage
import com.github.jambeez.server.worker.JamWorker
import com.github.jambeez.server.worker.LobbyInformer
import com.github.jambeez.server.worker.WebsocketConnectionData
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class JamBeezWebSocketHandler : AbstractWebSocketHandler(), LobbyInformer {
    private val objectMapper: ObjectMapper = createObjectMapper()
    private val executors = Executors.newCachedThreadPool()

    private val connections: MutableMap<String, WebsocketConnectionData> =
        ConcurrentHashMap<String, WebsocketConnectionData>()

    private val domainController = DomainController()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("Connection Established to $session")
        connections[session.id] = WebsocketConnectionData(
            ConcurrentWebSocketSessionDecorator(session, 2000, 4096),
            domainController.createUser()
        )
        logger.info("New connected client. #Lobbies: ${domainController.amountOfLobbies()} #Clients: ${connections.size}")
        super.afterConnectionEstablished(session)
    }


    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.debug("Connection closed to $session")
        domainController.end(connections[session.id])
        connections.remove(session.id)
        logger.info("Lost a client. #Lobbies: ${domainController.amountOfLobbies()} #Clients: ${connections.size}")
        super.afterConnectionClosed(session, status)
    }


    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.debug("Got Binary Message from $session : $message")
        // Try to convert to string
        try {
            val messageAsString = StandardCharsets.UTF_8.decode(message.payload).toString()
            handleTextMessage(session, TextMessage(messageAsString))
        } catch (e: Exception) {
            logger.error(e.message, e)
            IntentMessage("error:binary_not_string", "Binary content is no string").send(session)
        }

    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Got Text from $session : $message")
        val intent: Intent = objectMapper.readValueOrNull(message.asBytes()) ?: return
        logger.debug("JamRequest is $intent")
        val connectionData = connections[session.id]
        if (connectionData == null) {
            logger.debug("Unknown session found $session")
            return
        }
        executors.submit(JamWorker(domainController, this, connectionData, message, intent.intent))
    }

    override fun informAllOtherUsers(lobby: Lobby, user: User?, webSocketMessage: WebSocketMessage<*>) {
        val connections = connections.values.filter { wssd -> wssd.user != user && lobby.users.contains(wssd.user) }
        connections.forEach { wssd -> wssd.websocketSession.sendMessage(webSocketMessage) }
    }
}
