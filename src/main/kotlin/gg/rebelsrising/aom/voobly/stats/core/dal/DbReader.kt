package gg.rebelsrising.aom.voobly.stats.core.dal

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object DbReader {

    // TODO Use default database for tx manager.
    // See https://github.com/JetBrains/Exposed/wiki/Transactions

    fun matchExists(matchId: Int): Boolean {
        return transaction(Database.connect(Db.db)) {
            MatchTable.select {
                MatchTable.matchId eq matchId
            }.count().toInt() >= 1
        }
    }

}
