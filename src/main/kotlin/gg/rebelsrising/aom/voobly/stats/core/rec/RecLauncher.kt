package gg.rebelsrising.aom.voobly.stats.core.rec

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import gg.rebelsrising.aom.voobly.stats.core.Setup
import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.model.Civ
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class RecLauncher : CliktCommand(name = "download") {

    private val configPath: String by option(help = "The path of the configuration file to load.")
        .default(Config.DEFAULT_CONFIG_FILE)

    private val ladder: Ladder by option(help = "The ladder to filter recorded games for.")
        .enum<Ladder>()
        .default(Ladder.AOT_1X)

    private val minRating: Int by option(help = "The minimum rating to filter recorded games for.")
        .int()
        .default(10)

    private val civ: Civ by option(help = "The major god to filter recorded games for.")
        .enum<Civ>()
        .default(Civ.UNKNOWN)

    private val playerId: Int by option(help = "The player ID to filter recorded games for.")
        .int()
        .default(-1)

    private val map: String by option(help = "The map to filter recorded games for.")
        .default("")

    private fun startLoader(session: Session, config: Config) {
        logger.info { "Starting recorded game downloader..." }

        val loader = RecLoader(session, config, ladder, minRating, civ, playerId, map)

        Thread(loader).start()

        logger.info { "Successfully started recorded game downloader." }
    }

    override fun run() {
        logger.info { "Starting." }

        val config = Setup.loadConfig(configPath)
        val session = Setup.vooblyLogin(config)

        Setup.dbConnect(config.database)

        startLoader(session, config)

        logger.info { "Setup complete." }
    }

}
