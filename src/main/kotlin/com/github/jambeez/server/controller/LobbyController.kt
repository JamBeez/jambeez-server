package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.User
import java.util.*

class LobbyController {

    // TODO REMOVE DEBUG LOBBY
    private val jamSessions: MutableList<Lobby> = mutableListOf(Lobby("DEBUG"))

    fun createLobby(user: User): Lobby {
        val newSession = Lobby(UUID.randomUUID().toString())
        newSession.users.add(user)
        jamSessions.add(newSession)
        return newSession
    }


    fun joinLobby(sessionId: String, user: User): Lobby {
        if (findJamSession(user) != null) {
            throw IllegalArgumentException("User already in JamSession")
        }

        val jamSession = jamSessions.find { s -> s.id == sessionId } ?: throw IllegalArgumentException("Your jam session does not exist :(")
        jamSession.users.add(user)
        return jamSession
    }

    fun findJamSession(user: User) = jamSessions.find { js -> js.users.contains(user) }
}