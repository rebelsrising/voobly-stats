package gg.rebelsrising.aom.voobly.stats.core.model

import org.joda.time.DateTime

class Match(
    val matchId: Int,
    val date: DateTime,
    val duration: Int,
    val map: String,
    val mod: String
) {

    lateinit var ladder: Ladder
        private set

    lateinit var players: List<Player>
        private set

    lateinit var recUrl: String
        private set

    var winner: Byte = 0
        private set

    var rating: Short = 0
        private set

    var hasObs = false
        private set

    fun setMatchLadder(matchLadder: Ladder) {
        ladder = matchLadder
    }

    fun addPlayers(playerList: List<Player>) {
        players = cleanPlayers(ArrayList(playerList))

        updateRating()
        updateWinner()
    }

    fun setRecUrl(url: String) {
        recUrl = url
    }

    fun process(ladder: Ladder, playerList: List<Player>, url: String): Match {
        setMatchLadder(ladder)
        addPlayers(playerList)
        setRecUrl(url)

        return this
    }

    fun cleanPlayers(playerList: ArrayList<Player>): List<Player> {
        // Don't iterate over the list directly to avoid ConcurrentModificationException.
        val it = playerList.iterator()

        // Remove all players that could not be parsed or were observers.
        while (it.hasNext()) {
            if (it.next().playerId == -1) {
                it.remove()
                hasObs = true
            }
        }

        return playerList
    }

    fun updateRating(): Short {
        if (players.isEmpty()) {
            return 0
        }

        var acc = 0

        for (i in players) {
            acc += i.newRating
        }

        return (acc / players.size).toShort()
    }

    fun updateWinner(): Byte {
        if (players.isEmpty()) {
            return 0
        }

        return if (players[0].hasWon) 1 else 2
    }

    fun isComplete(): Boolean {
        return players.size >= 2 &&
                this::ladder.isInitialized &&
                ((map != "n/a" && map != "") || recUrl != "")
    }

}
