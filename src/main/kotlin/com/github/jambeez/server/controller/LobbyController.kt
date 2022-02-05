package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.User
import java.util.*

class LobbyController {

    // TODO REMOVE DEBUG LOBBY
    private val lobbys: MutableList<Lobby> = mutableListOf(Lobby("DEBUG"))

    fun createLobby(user: User): Lobby {
        val newSession = Lobby(UUID.randomUUID().toString())
        newSession.users.add(user)
        lobbys.add(newSession)
        return newSession
    }


    fun joinLobby(sessionId: String, user: User): Lobby {
        if (findLobby(user) != null) {
            throw IllegalArgumentException("User already in Lobby")
        }

        val lobbys = lobbys.find { s -> s.id == sessionId } ?: throw IllegalArgumentException("Your jam session does not exist :(")
        lobbys.users.add(user)
        return lobbys
    }

    fun findLobby(user: User) = lobbys.find { js -> js.users.contains(user) }
}