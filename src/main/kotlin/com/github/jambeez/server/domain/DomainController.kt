package com.github.jambeez.server.domain

import com.github.jambeez.server.logger
import com.github.jambeez.server.worker.WebsocketConnectionData
import java.util.*
import org.springframework.core.io.ClassPathResource

class DomainController {

    private val users: MutableList<User> = mutableListOf()

    private val lobbies: MutableList<Lobby> = mutableListOf()

    private val lobbyIdParts: List<String>

    init {
        val musicalWordsResource = ClassPathResource("/musical-words.txt")
        val musicalWords = mutableListOf<String>()
        Scanner(musicalWordsResource.inputStream).use { scanner ->
            while (scanner.hasNextLine()) {
                val word = scanner.nextLine()?.trim()
                if (word == null || word.isBlank() || word.startsWith("#")) continue
                musicalWords.add(word)
            }
        }
        lobbyIdParts = musicalWords.toList()
    }

    fun amountOfLobbies() = lobbies.size

    fun createUser(): User {
        val newUser = User(UUID.randomUUID().toString())
        users.add(newUser)
        return newUser
    }

    @Synchronized
    fun createLobby(user: User): Lobby {
        val newLobby = Lobby(newLobbyId())
        newLobby.users.add(user)
        logger.debug("Create new Lobby $newLobby")
        lobbies.add(newLobby)
        return newLobby
    }

    private fun newLobbyId(): String {
        val key = "${lobbyIdParts.random()}_${lobbyIdParts.random()}_${lobbyIdParts.random()}"
        if (lobbies.any { l -> l.id == key }) return UUID.randomUUID().toString()
        return key
    }

    @Synchronized
    fun joinLobby(sessionId: String, user: User): Lobby {
        if (findLobby(user) != null) {
            throw IllegalArgumentException("User already in Lobby")
        }

        val lobby =
            lobbies.find { s -> s.id == sessionId }
                ?: throw IllegalArgumentException("Your jam session does not exist :(")
        lobby.users.add(user)
        logger.debug("User $user successfully joined Lobby: $lobby")
        return lobby
    }

    fun findLobby(user: User) = lobbies.find { js -> js.users.contains(user) }

    @Synchronized
    fun end(websocketConnectionData: WebsocketConnectionData?) {
        if (websocketConnectionData == null) return
        val lobby = findLobby(websocketConnectionData.user) ?: return
        lobby.users.remove(websocketConnectionData.user)
        if (lobby.users.isEmpty()) {
            logger.debug("Remove Lobby $lobby")
            lobbies.remove(lobby)
        }
    }
}
