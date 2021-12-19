package gg.rebelsrising.aom.voobly.stats.core

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.config.DatabaseConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

object Setup {

    private fun logExceptionAndExit(e: Exception) {
        logger.error { e }
        exitProcess(1)
    }

    fun loadConfig(path: String): Config {
        logger.info { "Loading configuration at ${path}..." }

        val config = Config.load(path)

        logger.info { "Successfully loaded configuration." }

        return config
    }

    fun vooblyLogin(config: Config): Session {
        logger.info { "Connecting to Voobly..." }

        val s = Session(config.voobly)

        try {
            s.login()
        } catch (e: Exception) {
            logger.info { "Failed to log in - exiting." }
            logExceptionAndExit(e)
        }

        logger.info { "Successfully logged into Voobly." }

        return s
    }

    fun dbConnect(databaseConfig: DatabaseConfig) {
        logger.info { "Establishing database connection..." }

        try {
            Db.connect(databaseConfig)
        } catch (e: Exception) {
            logger.info { "Failed to connect to database - exiting." }
            logExceptionAndExit(e)
        }

        // Only creates the tables if they don't already exist.
        Db.createTables()

        logger.info { "Successfully connected to the database." }
    }

}

