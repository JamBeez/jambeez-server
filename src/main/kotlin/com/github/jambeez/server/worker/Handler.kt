package com.github.jambeez.server.worker

import com.github.jambeez.server.*
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage

abstract class Handler(protected val domainController: DomainController, protected val lobbyInformer: LobbyInformer) {
    protected val objectMapper = createObjectMapper()


    @Throws(WorkerException::class)
    abstract fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String)

    protected fun findLobby(connectionData: WebsocketConnectionData): Lobby {
        return domainController.findLobby(connectionData.user) ?: throw WorkerException("User not in Lobby")
    }


    protected inline fun <reified D, reified R> changeAttribute(
        connectionData: WebsocketConnectionData,
        message: TextMessage,
        selector: (R) -> Any?,
        dataSetter: (D, R) -> Unit,
        dataGetter: (Lobby, R) -> D,
        messageToSend: (TextMessage, R) -> WebSocketMessage<*> = { m, _ -> m }
    ) {
        logger.debug("Try ChangeAttribute ${D::class.java.simpleName} ${R::class.java.simpleName} from User ${connectionData.user}")
        val lobby = findLobby(connectionData)
        val changeRequest = readChangeRequest(message, selector)
        val data = dataGetter(lobby, changeRequest)
        dataSetter(data, changeRequest)
        logger.debug("Success ChangeAttribute ${D::class.java.simpleName} ${R::class.java.simpleName} from User ${connectionData.user}")
        // lobbyInformer.informAllOtherUsers(lobby, null, message)
        lobbyInformer.informAllOtherUsers(lobby, null, messageToSend(message, changeRequest))
    }


    protected inline fun <reified R> readChangeRequest(message: TextMessage, selector: (R) -> Any?): R {
        val changeRequest: R = objectMapper.readValueOrNull(message.payload)
            ?: throw WorkerException("ChangeRequest could not be deserialized")

        if (selector(changeRequest) == null) throw WorkerException("ChangeRequest incomplete")
        return changeRequest
    }

}
