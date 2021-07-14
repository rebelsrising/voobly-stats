package gg.rebelsrising.aom.voobly.stats.core.dal

import gg.rebelsrising.aom.voobly.stats.core.dal.util.batchUpsert
import gg.rebelsrising.aom.voobly.stats.core.dal.util.upsert
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("DuplicatedCode")
object DbWriter {

    fun writeMatch(m: Match) {

        val players = m.players

        transaction(Database.connect(Db.db)) {

            val match = MatchTable.upsert(MatchTable.primaryKey.columns.toList()) {
                it[matchId] = m.matchId
                it[ladder] = m.ladder
                it[rating] = m.rating
                it[map] = m.map
                it[mod] = m.mod
                it[duration] = m.duration
                it[recUrl] = m.recUrl
                it[hasObs] = m.hasObs
                it[datePlayed] = m.date
            } get MatchTable.matchId

            PlayerDataTable.batchUpsert(
                players,
                PlayerDataTable.primaryKey.columns.toList()
            ) { batch, p ->
                batch[matchId] = match
                batch[playerId] = p.playerId
                batch[name] = p.name
                batch[teamName] = p.teamName
                batch[teamTag] = p.teamUrl
                batch[civ] = p.civ
                batch[team] = p.team
                batch[newRating] = p.newRating
                batch[delta] = p.delta
            }
        }

    }

}
