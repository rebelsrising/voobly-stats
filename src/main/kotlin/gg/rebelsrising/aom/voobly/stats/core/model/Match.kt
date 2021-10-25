package gg.rebelsrising.aom.voobly.stats.core.model

import gg.rebelsrising.aom.voobly.stats.core.parser.match.MatchMetaData
import org.joda.time.DateTime

class Match(
    val matchId: Int,
    val date: DateTime,
    val duration: Int,
    val map: String,
    val mod: String,
    var ladder: Ladder,
    var recUrl: String,
    var rating: Short = 0,
    var hasObs: Boolean = false
) {

    companion object {

        fun fromData(meta: MatchMetaData, ladder: Ladder, playerList: List<Player>, url: String): Match {
            return Match(
                meta.matchId,
                meta.date,
                meta.duration,
                meta.map,
                meta.mod,
                ladder,
                url
            ).addPlayers(playerList)
        }

    }

    var players: List<Player> = ArrayList()
        private set

    fun addPlayers(playerList: List<Player>): Match {
        players = cleanPlayers(ArrayList(playerList))

        updateRating()

        return this
    }

    private fun cleanPlayers(playerList: ArrayList<Player>): List<Player> {
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

    private fun updateRating() {
        if (players.isEmpty()) {
            rating = 0
            return
        }

        var acc = 0

        for (i in players) {
            acc += i.newRating
        }

        rating = (acc / players.size).toShort()
    }

    fun isComplete(requireMap: Boolean = false): Boolean {
        val hasTwoPlayers = players.size >= 2

        if (!requireMap) {
            return hasTwoPlayers
        }

        return hasTwoPlayers && ((map != "n/a" && map != "") || recUrl != "")
    }

}
