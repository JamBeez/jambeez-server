package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jambeez.server.domain.JamRequest
import com.github.jambeez.server.worker.JamWorker
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

data class SessionData(
    @Volatile var websocketSession: WebSocketSession
)

class JamBeezWebSocketHandler : AbstractWebSocketHandler() {
    private val objectMapper: ObjectMapper = createObjectMapper()
    private val executors = Executors.newCachedThreadPool()

    private val sessions: MutableMap<String, SessionData> = ConcurrentHashMap<String, SessionData>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = SessionData(ConcurrentWebSocketSessionDecorator(session, 2000, 4096))
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
        val jamRequest: JamRequest = objectMapper.readValueOrNull(message.asBytes()) ?: return
        logger.debug("JamRequest is $jamRequest")
        val sessionData = sessions[session.id]
        if (sessionData == null) {
            logger.debug("Unknown session found $session")
            return
        }
        executors.submit(JamWorker(sessionData, message))
    }
}
