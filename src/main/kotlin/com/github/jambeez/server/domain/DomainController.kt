package com.github.jambeez.server.domain

import com.github.jambeez.server.WebsocketConnectionData
import java.util.*

class DomainController {

    private val users: MutableList<User> = mutableListOf()

    // TODO REMOVE DEBUG LOBBY
    private val lobbies: MutableList<Lobby> = mutableListOf(Lobby("DEBUG"))

    fun createUser(): User {
        val newUser = User(UUID.randomUUID().toString())
        users.add(newUser)
        return newUser
    }

    @Synchronized
    fun createLobby(user: User): Lobby {
        val newSession = Lobby(UUID.randomUUID().toString())
        newSession.users.add(user)
        lobbies.add(newSession)
        return newSession
    }

    @Synchronized
    fun joinLobby(sessionId: String, user: User): Lobby {
        if (findLobby(user) != null) {
            throw IllegalArgumentException("User already in Lobby")
        }

        val lobbys = lobbies.find { s -> s.id == sessionId }
            ?: throw IllegalArgumentException("Your jam session does not exist :(")
        lobbys.users.add(user)
        return lobbys
    }

    fun findLobby(user: User) = lobbies.find { js -> js.users.contains(user) }

    @Synchronized
    fun end(websocketConnectionData: WebsocketConnectionData?) {
        if (websocketConnectionData == null) return
        val lobby = findLobby(websocketConnectionData.user) ?: return
        lobby.users.remove(websocketConnectionData.user)
        if (lobby.users.isEmpty()) {
            lobbies.remove(lobby)
        }


    }
}