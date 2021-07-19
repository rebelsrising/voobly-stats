package gg.rebelsrising.aom.voobly.stats.core.config

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.LadderScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchIdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.PlayerIdScraperConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Config(
    val database: DatabaseConfig,
    val voobly: VooblyConfig,
    val matchScraper: MatchScraperConfig,
    val matchIdScraper: MatchIdScraperConfig,
    val playerIdScraper: PlayerIdScraperConfig,
    val ladderScraper: LadderScraperConfig
) {

    companion object {
        const val DEFAULT_CONFIG_FILE = "config.json"

        fun load(config: String): Config {
            val jsonString = File(config).readText()

            return Json.decodeFromString(serializer(), jsonString)
        }
    }

}
