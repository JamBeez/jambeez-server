package com.github.jambeez.server.worker

import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.Track
import com.github.jambeez.server.setAll
import org.springframework.web.socket.TextMessage

data class TrackChange(
    val partId: String,
    val trackId: String,
    val mute: Boolean? = null,
    val sample: String? = null,
    val beats: MutableList<Boolean>? = null
)


class TrackHandler(domainController: DomainController, lobbyInformer: LobbyInformer) :
    Handler(domainController, lobbyInformer) {
    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            TRACK_TOGGLE_MUTE -> toggleMute(connectionData, message, intent)
            TRACK_SET_SAMPLE -> setSample(connectionData, message, intent)
            TRACK_SET_BEATS -> setBeats(connectionData, message, intent)
            else -> unknown(TrackHandler::class.java, connectionData, intent)
        }
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

