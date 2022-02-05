package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.createObjectMapper
import org.springframework.web.socket.TextMessage

abstract class Handler {
    protected val objectMapper = createObjectMapper()

    @Throws(WorkerException::class)
    abstract fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String)
}