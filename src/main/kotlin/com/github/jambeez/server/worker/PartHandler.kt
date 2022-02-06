package com.github.jambeez.server.worker

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jambeez.server.LobbyInformer
import com.github.jambeez.server.WebsocketConnectionData
import com.github.jambeez.server.domain.DomainController
import com.github.jambeez.server.domain.Lobby
import com.github.jambeez.server.domain.Part
import com.github.jambeez.server.domain.Track
import com.github.jambeez.server.domain.intent.IntentWrapper
import org.springframework.web.socket.TextMessage


data class PartChange(
    @JsonProperty("part_id") val partId: String,
    val bpm: Int? = null,
    val bars: Int? = null,
    @JsonProperty("sig_lower") val sigLower: Int? = null,
    @JsonProperty("sig_upper") val sigUpper: Int? = null,
    @JsonProperty("track_to_remove") val trackToRemove: String? = null,
    @JsonProperty("track_to_add") val trackToAdd: Track? = null
) {
    fun validate(): Boolean {
        if (bpm != null) return bpm > 0
        if (bars != null) return bars > 0
        if (sigLower != null) return sigLower > 0
        if (sigUpper != null) return sigUpper > 0
        if (trackToAdd != null) return trackToAdd.validate()
        return true
    }
}

data class NewTrackResponse(
    @JsonProperty("part_id") val partId: String, @JsonProperty("track_to_add") val trackToAdd: Track
)

class PartHandler(domainController: DomainController, lobbyInformer: LobbyInformer) :
    Handler(domainController, lobbyInformer) {


    override fun handle(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        when (intent) {
            PART_CHANGE_BPM -> changeBPM(connectionData, message, intent)
            PART_CHANGE_BARS -> changeBars(connectionData, message, intent)
            PART_CHANGE_SIG_LOWER -> changeSigLower(connectionData, message, intent)
            PART_CHANGE_SIG_UPPER -> changeSigUpper(connectionData, message, intent)
            PART_REMOVE_TRACK -> removeTrack(connectionData, message, intent)
            PART_ADD_TRACK -> addTrack(connectionData, message, intent)
            else -> unknown(PartHandler::class.java, connectionData, intent)
        }
    }

    private fun addTrack(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Part, PartChange>(connectionData,
            message,
            validator = { it.trackToAdd != null && it.validate() },
            dataSetter = { p, c -> setTrack(p, c) },
            dataGetter = { l, c -> findPart(l, c) },
            messageToSend = { _, c -> IntentWrapper(intent, NewTrackResponse(c.partId, c.trackToAdd!!)).payload() })
    }

    private fun setTrack(p: Part, c: PartChange) {
        val track = c.trackToAdd!!
        track.colorPerBeat.clear()
        track.beats.forEach { _ -> track.colorPerBeat.add(listOf()) }
        p.tracks.add(track)
    }

    private fun removeTrack(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Part, PartChange>(connectionData,
            message,
            validator = { it.trackToRemove != null && it.validate() },
            dataSetter = { p, c -> p.tracks.removeIf { it.id == c.trackToRemove!! } },
            dataGetter = { l, c -> findPart(l, c) })
    }

    private fun changeBPM(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Part, PartChange>(
            connectionData,
            message,
            validator = { it.bpm != null && it.validate() },
            dataSetter = { p, c -> p.bpm = c.bpm!! },
            dataGetter = { l, c -> findPart(l, c) })
    }

    private fun changeBars(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Part, PartChange>(
            connectionData,
            message,
            validator = { it.bars != null && it.validate() },
            dataSetter = { p, c -> p.bars = c.bars!! },
            dataGetter = { l, c -> findPart(l, c) })
    }

    private fun changeSigUpper(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Part, PartChange>(
            connectionData,
            message,
            validator = { it.sigUpper != null && it.validate() },
            dataSetter = { p, c -> p.sigUpper = c.sigUpper!! },
            dataGetter = { l, c -> findPart(l, c) })
    }

    private fun changeSigLower(connectionData: WebsocketConnectionData, message: TextMessage, intent: String) {
        changeAttribute<Part, PartChange>(
            connectionData,
            message,
            validator = { it.sigLower != null && it.validate() },
            dataSetter = { p, c -> p.sigLower = c.sigLower!! },
            dataGetter = { l, c -> findPart(l, c) })
    }


    private fun findPart(lobby: Lobby, partChange: PartChange): Part {
        return lobby.parts.find { p -> p.id == partChange.partId } ?: throw WorkerException("Invalid PartId")
    }

}
