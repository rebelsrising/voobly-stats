package gg.rebelsrising.aom.voobly.stats.core.model

class MatchScrapeJob(
    val id: Int,
    val ladder: Ladder,
    var status: MatchScrapeStatus = MatchScrapeStatus.OPEN,
) {

    enum class MatchScrapeStatus {

        // Restrict the names to 16 characters at most.
        OPEN, FAILED, DELAYED, PROCESSING, DONE

    }

}
