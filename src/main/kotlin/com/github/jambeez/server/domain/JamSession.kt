package com.github.jambeez.server.domain

data class JamSession(val id: String, val owner: User, val participants: MutableList<User> = mutableListOf())

