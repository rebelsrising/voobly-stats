package gg.rebelsrising.aom.voobly.stats.core.model

class PlayerScrapeJob(
    val id: Int,
    val ladder: Ladder,
    var status: PlayerScrapeStatus = PlayerScrapeStatus.OPEN
) {

    enum class PlayerScrapeStatus {

        // Restrict the names to 16 characters at most.
        OPEN, PROCESSING

    }

}
