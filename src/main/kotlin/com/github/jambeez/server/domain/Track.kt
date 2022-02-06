package com.github.jambeez.server.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class Track(
    val id: String = UUID.randomUUID().toString(),
    var muted: Boolean = false,
    var sample: String,
    val beats: MutableList<Boolean> = mutableListOf(),
    @JsonProperty("color_per_beat") val colorPerBeat: MutableList<List<Float>> = mutableListOf(),
    var volume: Int = 50
) {
    fun validate(): Boolean {
        return sample.isNotBlank() && (volume >= 0) && (volume <= 100)
    }
}