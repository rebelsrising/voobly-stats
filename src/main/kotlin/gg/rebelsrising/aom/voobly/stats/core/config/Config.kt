package gg.rebelsrising.aom.voobly.stats.core.config

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchIdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.PlayerIdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.config.scraper.RecentScraperConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

// TODO Make the config global so we don't always have to pass it around (it remains static during exec anyway).
@Serializable
data class Config(
    val database: DatabaseConfig,
    val voobly: VooblyConfig,
    val matchScraper: MatchScraperConfig,
    val matchIdScraper: MatchIdScraperConfig,
    val playerIdScraper: PlayerIdScraperConfig,
    val recentScraper: RecentScraperConfig
) {

    companion object {

        const val DEFAULT_CONFIG_FILE = "config.json"

        lateinit var globalConfig: Config

        fun load(config: String): Config {
            val jsonString = File(config).readText()

            return Json.decodeFromString(serializer(), jsonString)
        }

        fun loadConfigToGlobal(config: String): Config {
            globalConfig = load(config)

            return globalConfig
        }

    }

}
