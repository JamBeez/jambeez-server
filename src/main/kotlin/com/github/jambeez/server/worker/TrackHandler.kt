package com.github.jambeez.server.worker

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.Track
import com.github.jambeez.server.setAll
import org.springframework.web.socket.TextMessage

data class TrackChange(
    @JsonProperty("part_id")
    val partId: String,
    @JsonProperty("track_id")
    val trackId: String,
    val mute: Boolean? = null,
    val sample: String? = null,
    val beats: MutableList<Boolean>? = null,
    val volume: Int? = null
)


class TrackHandler(domainController: DomainController, lobbyInformer: LobbyInformer) :
    Handler(domainController, lobbyInformer) {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            TRACK_TOGGLE_MUTE -> toggleMute(connectionData, message, intent)
            TRACK_SET_SAMPLE -> setSample(connectionData, message, intent)
            TRACK_SET_BEATS -> setBeats(connectionData, message, intent)
            TRACK_CHANGE_VOLUME -> changeVolume(connectionData, message, intent)
            else -> unknown(TrackHandler::class.java, connectionData, intent)
        }
    }

    private fun changeVolume(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Track, TrackChange>(
            connectionData,
            message,
            selector = { it.volume },
            dataSetter = { p, c -> p.volume = c.volume!! },
            dataGetter = { l, c -> findTrack(l, c) })
    }

    private fun toggleMute(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Track, TrackChange>(
            connectionData,
            message,
            selector = { it.mute },
            dataSetter = { p, c -> p.muted = c.mute!! },
            dataGetter = { l, c -> findTrack(l, c) })
    }

    private fun setSample(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Track, TrackChange>(
            connectionData,
            message,
            selector = { it.sample },
            dataSetter = { p, c -> p.sample = c.sample!! },
            dataGetter = { l, c -> findTrack(l, c) })
    }

    private fun setBeats(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Track, TrackChange>(
            connectionData,
            message,
            selector = { it.beats },
            dataSetter = { p, c -> p.beats.setAll(c.beats!!) },
            dataGetter = { l, c -> findTrack(l, c) })
    }


    private fun findTrack(lobby: Lobby, trackChange: TrackChange): Track {
        return lobby.parts.find { p -> p.id == trackChange.partId }?.tracks?.find { t -> t.id == trackChange.trackId }
            ?: throw WorkerException("Track not found")
    }

}

