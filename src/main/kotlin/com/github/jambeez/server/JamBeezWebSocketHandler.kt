package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jambeez.server.controller.JamSessionController
import com.github.jambeez.server.controller.UserController
import com.github.jambeez.server.domain.intent.Intent
import com.github.jambeez.server.worker.JamWorker
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

data class WebsocketSessionData(
    val websocketSession: WebSocketSession, val userController: UserController, val jamSessionController: JamSessionController
)


class JamBeezWebSocketHandler : AbstractWebSocketHandler() {
    private val objectMapper: ObjectMapper = createObjectMapper()
    private val executors = Executors.newCachedThreadPool()

    private val sessions: MutableMap<String, WebsocketSessionData> = ConcurrentHashMap<String, WebsocketSessionData>()

    private val jamSessionController = JamSessionController()
    private val userController = UserController()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = WebsocketSessionData(
            ConcurrentWebSocketSessionDecorator(session, 2000, 4096), userController, jamSessionController
        )
        super.afterConnectionEstablished(session)
    }


    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        super.afterConnectionClosed(session, status)
    }


    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.debug("Got Message from $session : $message")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Got Text from $session : $message")
        val intent: Intent = objectMapper.readValueOrNull(message.asBytes()) ?: return
        logger.debug("JamRequest is $intent")
        val sessionData = sessions[session.id]
        if (sessionData == null) {
            logger.debug("Unknown session found $session")
            return
        }
        executors.submit(JamWorker(sessionData, message, intent.intent))
    }
}
