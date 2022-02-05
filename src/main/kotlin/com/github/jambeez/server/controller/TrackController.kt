package com.github.jambeez.server.controller

import com.github.jambeez.server.domain.Track
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackController {
    private val tracks: MutableList<Track>

    constructor() {
        tracks = mutableListOf()
    }

    @GetMapping("/api/new-track/{name}")
    fun newTrack(@PathVariable name: String): Track {
        val track = Track(name)
        tracks.add(track)
        return track
    }

    @GetMapping("/api/tracks")
    fun tracks(): List<Track> {
        return tracks
    }
}
