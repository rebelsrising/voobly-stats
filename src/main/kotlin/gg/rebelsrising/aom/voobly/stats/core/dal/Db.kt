package gg.rebelsrising.aom.voobly.stats.core.dal

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gg.rebelsrising.aom.voobly.stats.core.config.DatabaseConfig
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob.MatchScrapeStatus
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob.PlayerScrapeStatus
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import javax.sql.DataSource

object Db {

    lateinit var db: DataSource

    fun connect(conf: DatabaseConfig) {
        // Creates a pool of connections rather than a single, specific connection.
        val hikariConf = HikariConfig()

        hikariConf.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        hikariConf.addDataSourceProperty("serverName", conf.url)
        hikariConf.addDataSourceProperty("portNumber", conf.port)
        hikariConf.addDataSourceProperty("user", conf.user)
        hikariConf.addDataSourceProperty("password", conf.pass)
        hikariConf.addDataSourceProperty("databaseName", conf.database)
        hikariConf.addDataSourceProperty("reWriteBatchedInserts", "true")

        db = HikariDataSource(hikariConf)
    }

    fun createTables() {
        transaction(Database.connect(db)) {
            SchemaUtils.create(MatchTable, PlayerDataTable, MatchJobTable, PlayerJobTable)
        }
    }

    // TODO Use default database for tx manager.
    // See https://github.com/JetBrains/Exposed/wiki/Transactions

    fun matchExistsQuery(id: Int): () -> Boolean {
        return {
            MatchTable.slice(intLiteral(1))
                .select { MatchTable.matchId eq id }
                .toList()
                .isNotEmpty()
        }
    }

    fun matchExistsQuery(j: MatchScrapeJob): () -> Boolean {
        return matchExistsQuery(j.id)
    }

    fun matchExists(id: Int): Boolean {
        return transaction(Database.connect(db)) {
            matchExistsQuery(id).invoke()
        }
    }

    fun matchJobExistsQuery(j: MatchScrapeJob): () -> Boolean {
        return {
            MatchJobTable.slice(intLiteral(1))
                .select { MatchJobTable.matchId eq j.id }
                .toList()
                .isNotEmpty()
        }
    }

    fun playerJobExistsQuery(j: PlayerScrapeJob): () -> Boolean {
        return {
            PlayerJobTable.slice(intLiteral(1))
                .select { PlayerJobTable.playerId eq j.id }
                .toList()
                .isNotEmpty()
        }
    }

    fun insertMatchJobQuery(j: MatchScrapeJob): () -> InsertStatement<Number> {
        return {
            MatchJobTable.insert {
                it[matchId] = j.id
                it[ladder] = j.ladder
                it[status] = j.status
                it[lastProcessingAttempt] = DateTime.now()
                it[dateIndexed] = DateTime.now()
            }
        }
    }

    fun insertPlayerJobQuery(j: PlayerScrapeJob): () -> InsertStatement<Number> {
        return {
            PlayerJobTable.insert {
                it[playerId] = j.id
                it[ladder] = j.ladder
                it[status] = j.status
                it[lastProcessingAttempt] = DateTime.now()
                it[dateIndexed] = DateTime.now()
            }
        }
    }

    // TODO Replace scrape result with bool.
    fun insertMatchJobIfNotScrapedOrDuplicate(matchJob: MatchScrapeJob): ScrapeResult {
        return transaction(Database.connect(db)) {
            if (matchExistsQuery(matchJob).invoke()) {
                return@transaction ScrapeResult.DUPLICATE
            }

            if (matchJobExistsQuery(matchJob).invoke()) {
                return@transaction ScrapeResult.DUPLICATE
            }

            // Insert (match was not found, insert can't fail).
            insertMatchJobQuery(matchJob).invoke()

            ScrapeResult.SUCCESS
        }
    }

    // TODO Replace scrape result with bool.
    fun insertPlayerJobIfNotDuplicate(j: PlayerScrapeJob): ScrapeResult {
        return transaction(Database.connect(db)) {
            if (playerJobExistsQuery(j).invoke()) {
                return@transaction ScrapeResult.DUPLICATE
            }

            // Insert (player was not found, insert can't fail).
            insertPlayerJobQuery(j).invoke()

            ScrapeResult.SUCCESS
        }
    }

    fun updatePlayerJob(job: PlayerScrapeJob) {
        return transaction(Database.connect(db)) {
            PlayerJobTable.update({ PlayerJobTable.playerId eq job.id }) {
                // Don't update ID, ladder, or indexing time.
                it[status] = job.status
                it[lastProcessingAttempt] = DateTime.now()
            }
        }
    }

    fun updateMatchJob(job: MatchScrapeJob) {
        return transaction(Database.connect(db)) {
            MatchJobTable.update({ MatchJobTable.matchId eq job.id }) {
                // Don't update ID, ladder, or indexing time.
                it[status] = job.status
                it[lastProcessingAttempt] = DateTime.now()
            }
        }
    }

    fun estimateNumPendingMatchJobs(): Int {
        // Requires Postgres!
        return transaction(Database.connect(db)) {
            return@transaction exec("SELECT reltuples AS estimate FROM pg_class WHERE relname = '${MatchJobTable.tableName}';") {
                it.next()
                return@exec it.getInt(1)
            }
        } ?: 0 // If this fails, something went very wrong.
    }

    fun writeMatchAndUpdateJob(m: Match) {
        transaction(Database.connect(db)) {
            val match = MatchTable.insert {
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

            PlayerDataTable.batchInsert(
                m.players,
            ) { p ->
                this[PlayerDataTable.matchId] = match
                this[PlayerDataTable.playerId] = p.playerId
                this[PlayerDataTable.name] = p.name
                this[PlayerDataTable.teamName] = p.teamName
                this[PlayerDataTable.teamTag] = p.teamUrl
                this[PlayerDataTable.civ] = p.civ
                this[PlayerDataTable.team] = p.team
                this[PlayerDataTable.newRating] = p.newRating
                this[PlayerDataTable.delta] = p.delta
            }

            MatchJobTable.update({ MatchJobTable.matchId eq m.matchId }) {
                it[status] = MatchScrapeStatus.DONE
                it[lastProcessingAttempt] = DateTime.now()
            }
        }
    }

    // TODO Consider removing the threshold as, upon a crash/restart we should just reset all elements in PROCESSING state.
    fun getPlayerJobForProcessing(
        targetLadder: Ladder,
        lastProcessingThreshold: DateTime = DateTime.now().minusMinutes(10)
    ): PlayerScrapeJob? {
        return transaction(Database.connect(db)) {
            val job = PlayerJobTable.select {
                // Correct ladder && (status == open || (status == processing && not recently processed)).
                (PlayerJobTable.ladder eq targetLadder) and
                        ((PlayerJobTable.status eq PlayerScrapeStatus.OPEN) or
                                ((PlayerJobTable.status eq PlayerScrapeStatus.PROCESSING) and
                                        (PlayerJobTable.lastProcessingAttempt less lastProcessingThreshold)))
            }.orderBy(PlayerJobTable.lastProcessingAttempt, SortOrder.ASC).limit(1).map {
                PlayerScrapeJob(
                    id = it[PlayerJobTable.playerId],
                    ladder = it[PlayerJobTable.ladder],
                    status = PlayerScrapeStatus.PROCESSING,
                )
            }.singleOrNull() ?: return@transaction null

            PlayerJobTable.update({ PlayerJobTable.playerId eq job.id }) {
                it[status] = PlayerScrapeStatus.PROCESSING
                it[lastProcessingAttempt] = DateTime.now()
            }

            job
        }
    }

    fun getMatchJobBatch(
        size: Int,
        targetLadder: Ladder,
        lastProcessingThreshold: DateTime = DateTime.now().minusMinutes(10)
    ): List<MatchScrapeJob> {
        return transaction(Database.connect(db)) {
            val jobs = MatchJobTable.select {
                // Correct ladder && (status == open || (status == processing|delayed && not recently processed)).
                (MatchJobTable.ladder eq targetLadder) and
                        ((MatchJobTable.status eq MatchScrapeStatus.OPEN) or
                                (((MatchJobTable.status eq MatchScrapeStatus.PROCESSING) or
                                        (MatchJobTable.status eq MatchScrapeStatus.DELAYED)) and
                                        (MatchJobTable.lastProcessingAttempt less lastProcessingThreshold)))
            }.orderBy(MatchJobTable.matchId, SortOrder.DESC).limit(size) // Always get most recent games first.
                .map {
                    MatchScrapeJob(
                        id = it[MatchJobTable.matchId],
                        ladder = it[MatchJobTable.ladder],
                        status = MatchScrapeStatus.PROCESSING,
                    )
                }.toCollection(ArrayList())


            for (job in jobs) {
                // TODO Batch update or inside the above map {} block.
                MatchJobTable.update({ MatchJobTable.matchId eq job.id }) {
                    it[status] = MatchScrapeStatus.PROCESSING
                    it[lastProcessingAttempt] = DateTime.now()
                }
            }

            jobs
        }
    }

}
