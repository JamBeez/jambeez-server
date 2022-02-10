package com.github.jambeez.server.worker

import com.github.jambeez.server.domain.User
import org.springframework.web.socket.WebSocketSession

data class WebsocketConnectionData(val websocketSession: WebSocketSession, val user: User)
