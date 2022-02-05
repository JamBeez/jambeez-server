package com.github.jambeez.server.worker

import com.github.jambeez.server.WebsocketSessionData
import com.github.jambeez.server.createObjectMapper
import com.github.jambeez.server.domain.User
import org.springframework.web.socket.TextMessage

abstract class Handler {
    protected val objectMapper = createObjectMapper()

    @Throws(WorkerException::class)
    abstract fun handle(sessionData: WebsocketSessionData, message: TextMessage, intent: String)

    protected fun validateUser(sessionData: WebsocketSessionData, user: User) {
        if (!sessionData.userController.validate(user)) {
            throw WorkerException("Unknown user $user")
        }
    }
}