package gg.rebelsrising.aom.voobly.stats.core.config.scraper

import kotlinx.serialization.Serializable

@Serializable
data class LadderScraperConfig(
    val busySleep: Long,
    val hourlyInterval: Long
)
