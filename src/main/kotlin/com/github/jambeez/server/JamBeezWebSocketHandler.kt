package com.github.jambeez.server

import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class JamBeezWebSocketHandler : TextWebSocketHandler() {
    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        println(message)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println(message)
    }

}