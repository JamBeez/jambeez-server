package com.github.jambeez.server.worker

import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.createObjectMapper
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

abstract class Handler(protected val domainController: DomainController, protected val lobbyInformer: LobbyInformer) {
    protected val objectMapper = createObjectMapper()


    @Throws(WorkerException::class)
    abstract fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String)

    protected fun findLobby(connectionData: WebsocketConnectionData): Lobby {
        return domainController.findLobby(connectionData.user) ?: throw WorkerException("User not in Lobby")
    }


    protected inline fun <D, reified R> changeAttribute(
        connectionData: WebsocketConnectionData,
        message: TextMessage,
        selector: (R) -> Any?,
        dataSetter: (D, R) -> Unit,
        dataGetter: (Lobby, R) -> D
    ) {
        val lobby = findLobby(connectionData)
        val changeRequest = readChangeRequest(message, selector)
        val data = dataGetter(lobby, changeRequest)
        dataSetter(data, changeRequest)
        lobbyInformer.informAllOtherUsers(lobby, null, message)
    }


    protected inline fun <reified R> readChangeRequest(message: TextMessage, selector: (R) -> Any?): R {
        val changeRequest: R = objectMapper.readValueOrNull(message.payload)
            ?: throw WorkerException("ChangeRequest could not be deserialized")

        if (selector(changeRequest) == null) throw WorkerException("ChangeRequest incomplete")
        return changeRequest
    }

}
