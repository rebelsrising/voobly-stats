package gg.rebelsrising.aom.voobly.stats.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import gg.rebelsrising.aom.voobly.stats.core.scraper.ladder.RecentScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.match.MatchScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.PlayerIdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.PlayerScraper

class VooblyScraper : CliktCommand() {

    enum class Mode {

        PLAYER_GAMES,
        RECENT_GAMES,
        HYBRID

    }

    private val config: String by option(help = "The path of the configuration file to load.")
        .default(Config.DEFAULT_CONFIG_FILE)

    private val ladder: Ladder by option(help = "The ladder to scrape.")
        .enum<Ladder>()
        .default(Ladder.AOT_1X)

    private val mode: Mode by option(help = "The mode to run.")
        .enum<Mode>()
        .default(Mode.PLAYER_GAMES)

    override fun run() {
        val config = Config.load(config)
        val s = Session(config.voobly).login()

        Db.connect(config.database)
        Db.createTables()

        // TODO Reset entries from PROCESSING to OPEN.

        if (mode == Mode.PLAYER_GAMES || mode == Mode.HYBRID) {
            Thread(PlayerIdScraper(s, ladder, config.playerIdScraper)).start()
            Thread(PlayerScraper(s, ladder, config.matchIdScraper)).start()
        }

        if (mode == Mode.RECENT_GAMES || mode == Mode.HYBRID) {
            Thread(RecentScraper(s, ladder, config.recentScraper)).start()
        }

        // TODO Add options to specify number of workers.
        Thread(MatchScraper(s, ladder, config.matchScraper)).start()
    }

}

fun main(args: Array<String>): Unit = VooblyScraper().main(args)
