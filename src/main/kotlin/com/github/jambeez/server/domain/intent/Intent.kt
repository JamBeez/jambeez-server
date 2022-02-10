package com.github.jambeez.server.domain.intent

import com.github.jambeez.server.createObjectMapper
import com.github.jambeez.server.worker.WebsocketConnectionData
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

open class Intent(val intent: String) {
    @Transient
    protected val objectMapper = createObjectMapper()

    open fun payload(): WebSocketMessage<*> {
        val dataString = objectMapper.writeValueAsString(this)
        return TextMessage(dataString)
    }

    fun send(connectionData: WebsocketConnectionData) = send(connectionData.websocketSession)
    fun send(webSocketSession: WebSocketSession) = webSocketSession.sendMessage(payload())

    override fun toString(): String = "Intent: $intent"
}