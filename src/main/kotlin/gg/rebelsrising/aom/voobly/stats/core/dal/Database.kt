package gg.rebelsrising.aom.voobly.stats.core.dal

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.config.DatabaseConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object Db {

    init {
        // TODO Remove this for release.
        connectHardcoded()
    }

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

    private fun connectHardcoded() {
        connect(Config.load(Config.DEFAULT_CONFIG_FILE).database)
    }

    fun createTables() {
        transaction(Database.connect(db)) {
            SchemaUtils.create(MatchTable, PlayerDataTable)
        }
    }

}
