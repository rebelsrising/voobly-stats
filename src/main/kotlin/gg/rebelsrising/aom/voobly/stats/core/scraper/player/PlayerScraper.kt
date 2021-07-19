package gg.rebelsrising.aom.voobly.stats.core.scraper.player

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchIdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session

class PlayerScraper(
    val session: Session,
    val ladder: Ladder,
    val config: MatchIdScraperConfig
) : Scraper {

    fun processPlayerJob(pendingJobsThreshold: Int = 100): Boolean {
        if (Db.estimateNumPendingMatchJobs() > pendingJobsThreshold) {
            // Don't do anything if we have too many pending match jobs.
            return false
        }

        // Not too many pending match jobs, get player job and scrape the player's match history.
        val playerJob = Db.getPlayerJobForProcessing(ladder) ?: return false

        MatchIdScraper(session, playerJob, config).scrapePageBrowser()

        playerJob.status = PlayerScrapeJob.PlayerScrapeStatus.OPEN

        Db.updatePlayerJob(playerJob)

        return true
    }

    override fun run() {
        while (true) {
            if (!processPlayerJob()) {
                // Happens if we have plenty of match IDs with status OPEN or no OPEN player jobs.
                Thread.sleep(config.idleSleep)
            }
        }
    }

}
