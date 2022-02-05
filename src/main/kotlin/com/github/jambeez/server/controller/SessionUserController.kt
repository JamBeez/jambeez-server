package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.User
import org.springframework.stereotype.Controller
import org.springframework.web.context.annotation.SessionScope

@Controller
@SessionScope
class SessionUserController(private val userController: UserController) {

    private var user: User? = null

    @Synchronized
    fun getUser(): User {
        if (user == null) {
            user = userController.createUser()
        }
        return user!!
    }

    fun changeAlias(newAlias: String) {
        getUser().alias = newAlias
    }

}