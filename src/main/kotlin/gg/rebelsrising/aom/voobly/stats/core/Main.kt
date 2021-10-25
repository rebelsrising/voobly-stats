package gg.rebelsrising.aom.voobly.stats.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.config.DatabaseConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.rec.RecLoader
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import gg.rebelsrising.aom.voobly.stats.core.scraper.match.MatchScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.LadderScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.PlayerScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.recent.RecentScraper
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class VooblyScraper : CliktCommand() {

    enum class Mode {

        PLAYER_LADDER,
        RECENT_GAMES,
        HYBRID,
        DOWNLOAD

    }

    private val config: String by option(help = "The path of the configuration file to load.")
        .default(Config.DEFAULT_CONFIG_FILE)

    private val ladder: Ladder by option(help = "The ladder to scrape.")
        .enum<Ladder>()
        .default(Ladder.AOT_1X)

    private val mode: Mode by option(help = "The mode to run.")
        .enum<Mode>()
        .default(Mode.PLAYER_LADDER)

    private fun logExceptionAndExit(e: Exception) {
        logger.error { e }
        exitProcess(1)
    }

    private fun loadConfig(): Config {
        logger.info { "Loading configuration at ${config}..." }

        val config = Config.load(config)

        logger.info { "Successfully loaded configuration." }

        return config
    }

    private fun vooblyLogin(config: Config): Session {
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

    private fun dbConnect(databaseConfig: DatabaseConfig) {
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

    private fun startScrapers(session: Session, config: Config) {
        logger.info { "Starting the requested scrapers..." }

        if (mode == Mode.PLAYER_LADDER || mode == Mode.HYBRID) {
            Thread(LadderScraper(session, ladder, config.ladderScraper)).start()
            Thread(PlayerScraper(session, ladder, config.playerScraper)).start()
        }

        if (mode == Mode.RECENT_GAMES || mode == Mode.HYBRID) {
            Thread(RecentScraper(session, ladder, config.recentScraper)).start()
        }

        // TODO Add options to specify number of workers.
        Thread(MatchScraper(session, ladder, config.matchScraper)).start()

        logger.info { "Successfully started the requested scrapers." }
    }

    override fun run() {
        logger.info { "Starting." }

        val config = loadConfig()

        val session = vooblyLogin(config)

        dbConnect(config.database)

        if (mode != Mode.DOWNLOAD) {
            // TODO Reset entries from PROCESSING to OPEN.

            startScrapers(session, config)

            logger.info { "Setup complete." }
        } else {
            RecLoader(session, config).run()
        }
    }

}

fun main(args: Array<String>): Unit = VooblyScraper().main(args)
