package gg.rebelsrising.aom.voobly.stats.core.dal

import gg.rebelsrising.aom.voobly.stats.core.dal.columns.offsetdatetime
import gg.rebelsrising.aom.voobly.stats.core.model.Civ
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob.MatchScrapeStatus
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob.PlayerScrapeStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.time.OffsetDateTime

object MatchTable : Table("match") {

    val matchId = integer("match_id").index()
    val ladder = enumerationByName("ladder", 16, Ladder::class)
    val rating = short("rating").index()
    val map = varchar("map", 48).index()
    val mod = varchar("mod", 48).index().nullable()
    val duration = integer("game_duration").index() // Seconds.
    val recUrl = varchar("rec_url", 64).index()
    val hasObs = bool("has_obs") // Whether the game had at least one observer.
    val datePlayed = offsetdatetime("date_played")
    val dateIndexed = offsetdatetime("date_indexed").clientDefault { OffsetDateTime.now() }

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
    val teamUrl = varchar("team_name", 48)
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

object MatchJobTable : Table("match_job") {

    val matchId = integer("match_id").index()
    val ladder = enumerationByName("ladder", 16, Ladder::class)
    val status = enumerationByName("scrape_status", 16, MatchScrapeStatus::class).index()
    val lastProcessingAttempt = offsetdatetime("last_processing_attempt").clientDefault { OffsetDateTime.now() }
    val dateIndexed = offsetdatetime("date_indexed").clientDefault { OffsetDateTime.now() }

    override val primaryKey: PrimaryKey = PrimaryKey(matchId)

}

object PlayerJobTable : Table("player_job") {

    val playerId = integer("player_id").index()
    val ladder = enumerationByName("ladder", 16, Ladder::class)
    val status = enumerationByName("scrape_status", 16, PlayerScrapeStatus::class).index()
    val lastProcessingAttempt = offsetdatetime("last_processing_attempt").clientDefault { OffsetDateTime.now() }
    val dateIndexed = offsetdatetime("date_indexed").clientDefault { OffsetDateTime.now() }

    override val primaryKey: PrimaryKey = PrimaryKey(playerId)

}
