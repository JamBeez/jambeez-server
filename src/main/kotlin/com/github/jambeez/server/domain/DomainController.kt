package com.github.jambeez.server.domain

import java.util.*

class DomainController {

    private val users: MutableList<User> = mutableListOf()

    // TODO REMOVE DEBUG LOBBY
    private val lobbys: MutableList<Lobby> = mutableListOf(Lobby("DEBUG"))

    fun createUser(): User {
        val newUser = User(UUID.randomUUID().toString())
        users.add(newUser)
        return newUser
    }

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

        val lobbys = lobbys.find { s -> s.id == sessionId }
            ?: throw IllegalArgumentException("Your jam session does not exist :(")
        lobbys.users.add(user)
        return lobbys
    }

    fun findLobby(user: User) = lobbys.find { js -> js.users.contains(user) }
}