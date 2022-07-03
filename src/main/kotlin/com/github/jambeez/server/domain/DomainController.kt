package com.github.jambeez.server.domain

import com.github.jambeez.server.logger
import com.github.jambeez.server.worker.WebsocketConnectionData
import java.time.Duration
import java.time.LocalDateTime
import java.util.Scanner
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedDeque
import org.springframework.core.io.ClassPathResource

class DomainController {

    private val users: MutableList<User> = mutableListOf()

    private val lobbies: MutableList<Lobby> = mutableListOf()

    private val lobbyIdParts: List<String>

    private val lobbiesForDeletion: ConcurrentLinkedDeque<Pair<Lobby, LocalDateTime>> =
        ConcurrentLinkedDeque()
    private var deletionThread: Thread?

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

        deletionThread = Thread { deleteLobbies() }
        deletionThread!!.name = "Lobby Cleanup"
        deletionThread!!.isDaemon = true
        deletionThread!!.start()
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
            lobbiesForDeletion.add(lobby to LocalDateTime.now())
        }
    }

    private fun deleteLobbies() {
        while (deletionThread != null) {
            Thread.sleep(90000)
            val currentLobbiesForDeletion = mutableListOf<Pair<Lobby, LocalDateTime>>()
            lobbiesForDeletion.iterator().forEach { currentLobbiesForDeletion.add(it) }

            val now = LocalDateTime.now()
            currentLobbiesForDeletion.removeIf { Duration.between(it.second, now).toMinutes() < 5 }

            if (currentLobbiesForDeletion.isEmpty() && logger.isDebugEnabled) {
                logger.debug("No lobbies for deletion")
            }

            for (lobby in currentLobbiesForDeletion) {
                lobbiesForDeletion.remove(lobby)
                if (lobby.first.users.isEmpty()) {
                    return
                }
                logger.debug("Remove Lobby ${lobby.first}")
                lobbies.remove(lobby.first)
            }
        }
    }
}
