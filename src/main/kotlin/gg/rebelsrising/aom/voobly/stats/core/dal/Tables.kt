package gg.rebelsrising.aom.voobly.stats.core.dal

import gg.rebelsrising.aom.voobly.stats.core.dal.util.datetimezone
import gg.rebelsrising.aom.voobly.stats.core.model.Civ
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object MatchTable : Table("match") {

    val matchId = integer("match_id")
    val ladder = enumerationByName("ladder", 48, Ladder::class)
    val rating = short("rating").index()
    val map = varchar("map", 48).index()
    val mod = varchar("mod", 48).index()
    val duration = integer("game_duration").index() // Seconds.
    val recUrl = varchar("rec_url", 64).index()
    val hasObs = bool("has_obs") // Whether the game had at least one observer.
    val dateIndexed = datetimezone("date_indexed").clientDefault { DateTime() }
    val datePlayed = datetimezone("date_played")

    override val primaryKey: PrimaryKey = PrimaryKey(matchId)

}

object PlayerDataTable : Table("player_data") {

    val matchId = integer("match_id").references(
        MatchTable.matchId,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val playerId = integer("player_id").index()
    val name = varchar("name", 32).index()
    val teamName = varchar("team_name", 48)
    val teamTag = varchar("team_tag", 16)
    val civ = enumerationByName("civ", 16, Civ::class).index()
    val team = byte("team")
    val newRating = short("new_rating")
    val delta = byte("delta")

    override val primaryKey: PrimaryKey = PrimaryKey(matchId, playerId)

    init {
        index(true, matchId, playerId)
    }

}
