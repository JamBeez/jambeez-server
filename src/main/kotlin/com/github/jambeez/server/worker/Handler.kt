package com.github.jambeez.server.worker

import com.github.jambeez.server.createObjectMapper
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.intent.IntentMessage
import com.github.jambeez.server.logger
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage

abstract class Handler(protected val domainController: DomainController, protected val lobbyInformer: LobbyInformer) {
    protected val objectMapper = createObjectMapper()


    @Throws(WorkerException::class)
    abstract fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String)

    protected fun findLobby(connectionData: WebsocketConnectionData): Lobby {
        return domainController.findLobby(connectionData.user) ?: throw WorkerException("User not in Lobby")
    }


    protected inline fun <reified SubjectOfChange, reified ChangeRequest> changeAttribute(
        connectionData: WebsocketConnectionData,
        message: TextMessage,
        changeRequestValidator: (ChangeRequest) -> Boolean,
        changeApplier: (SubjectOfChange, ChangeRequest) -> Unit,
        subjectFinder: (Lobby, ChangeRequest) -> SubjectOfChange,
        messageForBroadcast: (TextMessage, ChangeRequest) -> WebSocketMessage<*> = { m, _ -> m }
    ) {
        logger.debug("Try ChangeAttribute Payload [${message.payload}] from ${connectionData.user}")
        val lobby = findLobby(connectionData)
        val changeRequest = readChangeRequest(message, changeRequestValidator)
        val subjectOfChange = subjectFinder(lobby, changeRequest)
        changeApplier(subjectOfChange, changeRequest)
        logger.debug("Success ChangeAttribute [$changeRequest] from ${connectionData.user}")
        lobbyInformer.informAllOtherUsers(lobby, null, messageForBroadcast(message, changeRequest))
    }


    protected inline fun <reified R> readChangeRequest(message: TextMessage, changeRequestValidator: (R) -> Boolean): R {
        val changeRequest: R = objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("ChangeRequest could not be deserialized")

        if (!changeRequestValidator(changeRequest)) throw WorkerException("ChangeRequest incomplete or invalid")
        return changeRequest
    }


    companion object {
        fun <H : Handler> unknown(clazz: Class<H>?, connectionData: WebsocketConnectionData, intent: String) {
            logger.error("Got unknown intent: $intent")
            IntentMessage("error:$intent", "Unknown intent $intent. Selected Handler: ${clazz ?: "UNKNOWN"}").send(
                connectionData
            )
        }
    }

}
