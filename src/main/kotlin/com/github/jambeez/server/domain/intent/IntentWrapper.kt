package com.github.jambeez.server.domain.intent

import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage

class IntentWrapper(intent: String, private val data: Any) : Intent(intent) {
    override fun payload(): WebSocketMessage<*> {
        val completeDataMap = mapOf(
            "intent" to intent,
            data::class.java.simpleName.lowercase() to data
        )
        val completeData = objectMapper.writeValueAsString(completeDataMap)
        return TextMessage(completeData)
    }
}