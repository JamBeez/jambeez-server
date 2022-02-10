package com.github.jambeez.server.worker

import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.User
import com.github.jambeez.server.domain.intent.IntentWrapper
import com.github.jambeez.server.logger
import com.github.jambeez.server.readValueOrNull
import org.springframework.web.socket.TextMessage

class UserHandler(domainController: DomainController, lobbyInformer: LobbyInformer) : Handler(domainController, lobbyInformer) {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            USER_CHANGE_ALIAS -> changeAlias(connectionData, message, intent)
            else -> unknown(UserHandler::class.java, connectionData, intent)
        }
    }

    private fun changeAlias(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        val user: User = objectMapper.readValueOrNull(message.payload) ?: throw WorkerException("No User Provided")
        if (user.id != connectionData.user.id) {
            throw WorkerException("User Id can't be changed")
        }
        connectionData.user.alias = user.alias
        val result = IntentWrapper(intent, connectionData.user)
        result.send(connectionData)

        val lobby = domainController.findLobby(connectionData.user) ?: return
        lobbyInformer.informAllOtherUsers(lobby, null, result.payload())
        logger.debug("Changed Alias of User: $user")
    }
}
