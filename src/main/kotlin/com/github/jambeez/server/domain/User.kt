package com.github.jambeez.server.domain

import java.awt.Color

data class User(val id: String, var alias: String? = null, @Transient val color: List<Float> = randomColor())

fun randomColor(): List<Float> {
    val color = Color.getHSBColor(Math.random().toFloat(), 1F, 1F)
    return listOf(color.red / 255.0F, color.green / 255.0F, color.blue / 255.0F)
}
