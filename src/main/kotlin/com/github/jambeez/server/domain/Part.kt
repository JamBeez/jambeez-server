package com.github.jambeez.server.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class Part(
    val id: String = UUID.randomUUID().toString(),
    var bars: Int = 4,
    var bpm: Int = 120,
    @JsonProperty("sig_lower")
    var sigLower: Int = 4,
    @JsonProperty("sig_upper")
    var sigUpper: Int = 4,
    val tracks: MutableList<Track> = mutableListOf()
) {
    fun validate(): Boolean {
        return bars > 0 && bpm > 0 && sigLower > 0 && sigUpper > 0 && tracks.all { it.validate() }
    }

}