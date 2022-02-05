package com.github.jambeez.server.domain.intent

import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage

class IntentWrapper(intent: String, private val data: Any) : Intent(intent) {
    override fun payload(): WebSocketMessage<*> {
        // TODO Optimize code
        val dataAsJson = objectMapper.writeValueAsString(data)
        val dataAsMap: MutableMap<String, Any?>? = objectMapper.readValueOrNull(dataAsJson)
        if (dataAsMap == null || dataAsMap.containsKey("intent")) {
            // Internal Error while mapping to mutable map
            return IntentMessage(intent, "Internal Error while mapping to mutable map. Maybe \"intent\" is part of internal class").payload()
        }
        dataAsMap["intent"] = intent
        val completeData = objectMapper.writeValueAsString(dataAsMap)
        return TextMessage(completeData)
    }
}