package com.github.jambeez.server.domain

import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.logger
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
        val newLobby = Lobby(UUID.randomUUID().toString())
        newLobby.users.add(user)
        logger.debug("Create new Lobby $newLobby")
        lobbies.add(newLobby)
        return newLobby
    }

    @Synchronized
    fun joinLobby(sessionId: String, user: User): Lobby {
        if (findLobby(user) != null) {
            throw IllegalArgumentException("User already in Lobby")
        }

        val lobby = lobbies.find { s -> s.id == sessionId }
            ?: throw IllegalArgumentException("Your jam session does not exist :(")
        lobby.users.add(user)
        logger.debug("User $user successfully joined Lobby: $lobby")
        return lobby
    }

    fun findLobby(user: User) = lobbies.find { js -> js.users.contains(user) }

    @Synchronized
    fun end(websocketConnectionData: WebsocketConnectionData?) {
        if (websocketConnectionData == null) return
        val lobby = findLobby(websocketConnectionData.user) ?: return
        lobby.users.remove(websocketConnectionData.user)
        if (lobby.users.isEmpty()) {
            logger.debug("Remove Lobby $lobby")
            lobbies.remove(lobby)
        }


    }
}