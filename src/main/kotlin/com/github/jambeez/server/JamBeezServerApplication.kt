package com.github.jambeez.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class JamBeezServerApplication

fun main(args: Array<String>) {
    runApplication<JamBeezServerApplication>(*args)
}
