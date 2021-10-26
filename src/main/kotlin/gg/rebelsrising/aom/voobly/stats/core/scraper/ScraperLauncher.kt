package gg.rebelsrising.aom.voobly.stats.core.scraper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import gg.rebelsrising.aom.voobly.stats.core.Setup
import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.scraper.match.MatchScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.LadderScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.PlayerScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.recent.RecentScraper
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ScraperLauncher : CliktCommand(name = "scrape") {

    enum class Mode {

        PLAYERS,
        RECENT,
        HYBRID
    }

    private val configPath: String by option(help = "The path of the configuration file to load.")
        .default(Config.DEFAULT_CONFIG_FILE)

    private val ladder: Ladder by option(help = "The ladder to scrape.")
        .enum<Ladder>()
        .default(Ladder.AOT_1X)

    private val mode: Mode by option(help = "The mode to run.")
        .enum<Mode>()
        .default(Mode.PLAYERS)

    private fun startScrapers(session: Session, config: Config) {
        logger.info { "Starting the requested scrapers..." }

        if (mode == Mode.PLAYERS || mode == Mode.HYBRID) {
            Thread(LadderScraper(session, ladder, config.ladderScraper)).start()
            Thread(PlayerScraper(session, ladder, config.playerScraper)).start()
        }

        if (mode == Mode.RECENT || mode == Mode.HYBRID) {
            Thread(RecentScraper(session, ladder, config.recentScraper)).start()
        }

        // TODO Add options to specify number of workers.
        Thread(MatchScraper(session, ladder, config.matchScraper)).start()

        logger.info { "Successfully started the requested scrapers." }
    }

    override fun run() {
        logger.info { "Starting." }

        val config = Setup.loadConfig(configPath)
        val session = Setup.vooblyLogin(config)

        Setup.dbConnect(config.database)

        // TODO Reset entries from PROCESSING to OPEN.
        startScrapers(session, config)

        logger.info { "Setup complete." }
    }

}
