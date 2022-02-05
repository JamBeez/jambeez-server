package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jambeez.server.controller.JamSessionController
import com.github.jambeez.server.controller.UserController
import com.github.jambeez.server.domain.JamRequest
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class JamBeezWebSocketHandler : TextWebSocketHandler() {

    private val jamSessionController: JamSessionController = JamSessionController()
    private val userController: UserController = UserController()
    private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        logger.debug("Got Message from $session : $message")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Got Text from $session : $message")
        val jamRequest: JamRequest = objectMapper.readValueOrNull(message.asBytes()) ?: return
        logger.debug("JamRequest is $jamRequest")
    }

}
