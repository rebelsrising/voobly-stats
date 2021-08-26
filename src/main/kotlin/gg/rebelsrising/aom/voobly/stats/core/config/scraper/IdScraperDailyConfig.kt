package gg.rebelsrising.aom.voobly.stats.core.config.scraper

import kotlinx.serialization.Serializable

@Serializable
data class IdScraperDailyConfig(
    val busySleep: Long,
    val dailyInterval: Long
)
