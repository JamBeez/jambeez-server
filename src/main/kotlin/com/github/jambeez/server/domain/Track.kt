package com.github.jambeez.server.domain

import java.util.*

data class Track(
    val id: String = UUID.randomUUID().toString(),
    var muted: Boolean = false,
    var sample: String,
    val beats: MutableList<Boolean> = mutableListOf()
)