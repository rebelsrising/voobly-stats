package gg.rebelsrising.aom.voobly.stats.core.config.scraper

import kotlinx.serialization.Serializable

@Serializable
data class PlayerIdScraperConfig(
    val busySleep: Long,
    val dailyInterval: Long
)
