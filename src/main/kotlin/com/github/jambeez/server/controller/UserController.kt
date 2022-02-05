package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.User
import java.util.*

class UserController {

    private val users: MutableList<User> = mutableListOf()

    fun createUser(): User {
        val newUser = User(UUID.randomUUID().toString())
        users.add(newUser)
        return newUser
    }
}