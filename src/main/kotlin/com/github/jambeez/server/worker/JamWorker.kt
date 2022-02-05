package com.github.jambeez.server.worker

import com.github.jambeez.server.SessionData
import org.springframework.web.socket.TextMessage

class JamWorker(private val sessionData: SessionData, private val message: TextMessage) : Runnable {
    override fun run() {

    }
}