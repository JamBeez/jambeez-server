package com.github.jambeez.server.domain

data class Lobby(
    val id: String,
    val users: MutableList<User> = mutableListOf(),
    val parts: MutableList<Part> = mutableListOf()
) {
    fun validate(): Boolean {
        return parts.all { it.validate() }
    }
}