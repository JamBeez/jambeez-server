# jambeez-server

## How to start

Start your own server with:

```
docker pull ghcr.io/jambeez/jambeez-server:latest
docker run -it --rm -p 8080:8080 ghcr.io/jambeez/jambeez-server:latest
```

Define your endpoint with: `ws://localhost:8080/jambeez`

## Websocket Communication

### Intents

<!-- 
| lobby:send_state | Sends current lobby | [Lobby](src/main/kotlin/com/github/jambeez/server/domain/Lobby.kt) | | 
| part:add_track | Adds a new track to a part | [Track](src/main/kotlin/com/github/jambeez/server/domain/Track.kt) |  |
-->

| Intent                | Description                                   | Payload to Server                                                               | Response From Server                                                                                                                                                                                 | 
|-----------------------|-----------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| lobby:create          | Creates a new lobby                           | -                                                                               | [Lobby](src/main/kotlin/com/github/jambeez/server/domain/Lobby.kt)                                                                                                                                   |
| lobby:join            | User joins to an existing lobby               | [JoinRequest](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt) | To joined user: [Lobby](src/main/kotlin/com/github/jambeez/server/domain/Lobby.kt) and to other members: [User](src/main/kotlin/com/github/jambeez/server/domain/User.kt) with intent: `user:joined` |
| lobby:update_parts    | Updates the parts of the lobby                | [Parts](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt)       | Overwrites all parts of the lobby with updated parts. To others: [Parts](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt)                                                           |
| lobby:remove_part     | Removes a part from a lobby                   | [PartID](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt)      | Deletes the part from in all sessions. To others: [PartId](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt)                                                                         |
| part:change_bpm       | Sets the bpm of a part                        | [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)   | Overwrites the bpm of the part and sends the update to all: [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)                                                            |
| part:change_bars      | Sets the bars of a part                       | [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)   | Overwrites the bars of a part and sends the update to all: [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)                                                             |
| part:remove_track     | Removes a track from a part                   | [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)   | Removes the track from the part in all sessions to all: [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)                                                                |
| part:change_sig_lower | Sets the sig_lower of a part                  | [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)   | Overwrites the sig_lower of the part and sends the update to all: [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)                                                      |
| part:change_sig_upper | Sets the sig_upper of a part                  | [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)   | Overwrites the sig_upper of the part and sends the update to all: [PartChange](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)                                                      |
| track:toggle_mute     | Mutes/ Unmutes the track                      | [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt) | Sends the toggled mute to all: [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt)                                                                                       |
| track:set_sample      | Sets the sample to the track                  | [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt) | Overwrites the sample of the track and sends the update to all: [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt)                                                      |
| track:change_volume   | Sets the volume of the track                  | [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt) | Overwrites the volume of the track and sends the update to all: [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt)                                                      |
| track:set_beats       | Sets the beats to the track                   | [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt) | Overwrites the beats of the track and sends the update to all: [TrackChange](src/main/kotlin/com/github/jambeez/server/worker/TrackHandler.kt)                                                       |
| user:joined           | Message that another user joined to the lobby | -                                                                               | [User](src/main/kotlin/com/github/jambeez/server/domain/User.kt)                                                                                                                                     |
| user:change_alias     | Message that another user                     | changed its alias                                                               | -                                                                                                                                                                                                    | To all: [User](src/main/kotlin/com/github/jambeez/server/domain/User.kt)          |
