package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.JamSession
import com.github.jambeez.server.domain.User
import java.util.*

class JamSessionController {

    private val jamSessions: MutableList<JamSession> = mutableListOf()

    fun newSession(user: User): JamSession {
        val newSession = JamSession(UUID.randomUUID().toString())
        newSession.users.add(user)
        jamSessions.add(newSession)
        return newSession
    }


    fun joinSession(sessionId: String, user: User): JamSession {
        if (findJamSession(user) != null) {
            throw IllegalArgumentException("User already in JamSession")
        }

        val jamSession = jamSessions.find { s -> s.id == sessionId } ?: throw IllegalArgumentException("Your jam session does not exist :(")
        jamSession.users.add(user)
        return jamSession
    }

    fun findJamSession(user: User) = jamSessions.find { js -> js.users.contains(user) }
}