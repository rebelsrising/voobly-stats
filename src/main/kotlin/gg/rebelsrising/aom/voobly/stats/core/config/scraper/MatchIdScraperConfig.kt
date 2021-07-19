package gg.rebelsrising.aom.voobly.stats.core.config.scraper

import kotlinx.serialization.Serializable

@Serializable
data class MatchIdScraperConfig(
    val busySleep: Long,
    val idleSleep: Long,
)
