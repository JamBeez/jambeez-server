package com.github.jambeez.server.domain

data class JamSession(
    val id: String,
    val users: MutableList<User> = mutableListOf(),
    val parts: MutableList<Part> = mutableListOf(Part())
)

