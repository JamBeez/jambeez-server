package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.JamSession
import com.github.jambeez.server.domain.User
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

class JamSessionController {

    private val jamSessions: MutableList<JamSession> = mutableListOf()


    fun newSession(user: User): JamSession {
        val newSession = JamSession(UUID.randomUUID().toString(), user)
        jamSessions.add(newSession)
        return newSession
    }


    fun joinSession(sessionId: String, user: User): JamSession {
        val jamSession = jamSessions.find { s -> s.id == sessionId }
        if (jamSession == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Your jam session does not exist :(")
        }

        return jamSession
    }
}