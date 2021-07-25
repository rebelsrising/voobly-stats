package gg.rebelsrising.aom.voobly.stats.core.config.scraper

import kotlinx.serialization.Serializable

@Serializable
data class MatchScraperConfig(
    val busySleep: Long,
    val idleSleep: Long,
    val lastProcessingThresholdMins: Int,
    val requireMap: Boolean,
    val batchSize: Int
)
