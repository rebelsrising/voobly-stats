package gg.rebelsrising.aom.voobly.stats.core.config

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.IdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.IdScraperDailyConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchScraperConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Config(
    val database: DatabaseConfig,
    val voobly: VooblyConfig,
    val matchScraper: MatchScraperConfig,
    val playerScraper: IdScraperConfig,
    val ladderScraper: IdScraperDailyConfig,
    val recentScraper: IdScraperDailyConfig,
    val download: RecConfig
) {

    companion object {

        const val DEFAULT_CONFIG_FILE = "config.json"

        fun load(config: String): Config {
            val jsonString = File(config).readText()

            return Json.decodeFromString(serializer(), jsonString)
        }

    }

}
