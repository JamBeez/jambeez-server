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

| Intent  | Description | Payload to Server | Response From Server | 
|---|---|---|---|
| lobby:create | Creates a new lobby | - | [Lobby](src/main/kotlin/com/github/jambeez/server/domain/Lobby.kt) |
| lobby:join | User joins to an existing lobby | [JoinRequest](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt) | To joined user: [Lobby](src/main/kotlin/com/github/jambeez/server/domain/Lobby.kt) and to other members: [User](src/main/kotlin/com/github/jambeez/server/domain/User.kt) with intent: `user:joined`|
| lobby:update_parts | Updates the parts of the lobby | [Parts](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt) | Overwrites all parts of the lobby with updated parts. To others: [Parts](src/main/kotlin/com/github/jambeez/server/worker/LobbyHandler.kt) |
| part:change_bpm | Sets the bpm of a part | [ChangeRequest](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt)| Overwrites the bpm of the part and sends the update to all: [ChangeRequest](src/main/kotlin/com/github/jambeez/server/worker/PartHandler.kt) |
| user:joined | Message that another user joined to the lobby | - | [User](src/main/kotlin/com/github/jambeez/server/domain/User.kt) |
| user:change_alias | Message that another user changed its alias | - | To all: [User](src/main/kotlin/com/github/jambeez/server/domain/User.kt) |
