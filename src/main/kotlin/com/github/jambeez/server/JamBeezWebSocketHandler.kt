package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jambeez.server.controller.JamSessionController
import com.github.jambeez.server.controller.UserController
import com.github.jambeez.server.domain.JamSession
import com.github.jambeez.server.domain.User
import com.github.jambeez.server.domain.intent.Intent
import com.github.jambeez.server.worker.JamWorker
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

interface JamSessionInformer {
    fun informAllOtherUsers(jamSession: JamSession, user: User?, webSocketMessage: WebSocketMessage<*>)
}

data class WebsocketConnectionData(
    val websocketSession: WebSocketSession, val jamSessionInformer: JamSessionInformer, val userController: UserController, val jamSessionController: JamSessionController, val user: User
)

class JamBeezWebSocketHandler : AbstractWebSocketHandler(), JamSessionInformer {
    private val objectMapper: ObjectMapper = createObjectMapper()
    private val executors = Executors.newCachedThreadPool()

    private val connections: MutableMap<String, WebsocketConnectionData> = ConcurrentHashMap<String, WebsocketConnectionData>()

    private val jamSessionController = JamSessionController()
    private val userController = UserController()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        connections[session.id] = WebsocketConnectionData(
            ConcurrentWebSocketSessionDecorator(session, 2000, 4096), this, userController, jamSessionController, userController.createUser()
        )
        super.afterConnectionEstablished(session)
    }


    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        connections.remove(session.id)
        super.afterConnectionClosed(session, status)
    }


    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.debug("Got Message from $session : $message")
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
        executors.submit(JamWorker(connectionData, message, intent.intent))
    }

    override fun informAllOtherUsers(jamSession: JamSession, user: User?, webSocketMessage: WebSocketMessage<*>) {
        val connections = connections.values.filter { wssd -> wssd.user != user && jamSession.users.contains(wssd.user) }
        connections.forEach { wssd -> wssd.websocketSession.sendMessage(webSocketMessage) }
    }
}
