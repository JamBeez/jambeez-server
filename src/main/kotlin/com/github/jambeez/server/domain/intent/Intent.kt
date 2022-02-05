package com.github.jambeez.server.domain.intent

import com.github.jambeez.server.WebsocketSessionData
import com.github.jambeez.server.createObjectMapper
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
    
    fun send(sessionData: WebsocketSessionData) = send(sessionData.websocketSession)
    fun send(webSocketSession: WebSocketSession) = webSocketSession.sendMessage(payload())
}