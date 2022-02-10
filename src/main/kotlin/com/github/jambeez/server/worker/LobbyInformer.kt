package com.github.jambeez.server.worker

import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.User
import org.springframework.web.socket.WebSocketMessage

interface LobbyInformer {
    fun informAllOtherUsers(lobby: Lobby, user: User?, webSocketMessage: WebSocketMessage<*>)
}