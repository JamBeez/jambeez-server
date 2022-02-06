package com.github.jambeez.server.worker

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.Track
import com.github.jambeez.server.domain.intent.IntentWrapper
import com.github.jambeez.server.setAll
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage

data class TrackChange(
    @JsonProperty("part_id") val partId: String,
    @JsonProperty("track_id") val trackId: String,
    val mute: Boolean? = null,
    val sample: String? = null,
    val beats: MutableList<Boolean>? = null,
    @JsonProperty("color_per_beat") var colorPerBeat: MutableList<List<Float>>? = null,
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
        changeAttribute<Track, TrackChange>(connectionData,
            message,
            selector = { it.beats },
            dataSetter = { p, c -> processBeatsChange(connectionData, p, c) },
            dataGetter = { l, c -> findTrack(l, c) },
            messageToSend = { _, c -> beatWithColorData(connectionData, c, intent) }
        )
    }

    private fun beatWithColorData(
        connectionData: WebsocketConnectionData,
        c: TrackChange,
        intent: String
    ): WebSocketMessage<*> {
        c.colorPerBeat = findTrack(findLobby(connectionData), c).colorPerBeat
        return IntentWrapper(intent, c).payload()
    }

    private fun processBeatsChange(connectionData: WebsocketConnectionData, p: Track, c: TrackChange) {
        val copy = p.beats.toList()
        val newBeats = c.beats!!

        val colors: MutableList<List<Float>> = IntRange(0, newBeats.size - 1).map { idx ->
            if (newBeats[idx] && !copy.getOrElse(idx) { false }) {
                connectionData.user.color
            } else if (copy.getOrElse(idx) { false }) {
                p.colorPerBeat[idx]
            } else {
                listOf()
            }
        }.toMutableList()

        p.beats.setAll(c.beats)
        p.colorPerBeat.setAll(colors)
    }


    private fun findTrack(lobby: Lobby, trackChange: TrackChange): Track {
        return lobby.parts.find { p -> p.id == trackChange.partId }?.tracks?.find { t -> t.id == trackChange.trackId }
            ?: throw WorkerException("Track not found")
    }

}

