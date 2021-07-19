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

    fun processPlayerJob(): Boolean {
        // TODO Check how many unprocessed matches we have, sleep if there are too many.

        val playerJob = Db.getPlayerJobForProcessing(ladder) ?: return false

        MatchIdScraper(session, playerJob, config).scrapePageBrowser()

        playerJob.status = PlayerScrapeJob.PlayerScrapeStatus.OPEN

        Db.updatePlayerJob(playerJob)

        return true
    }

    override fun run() {
        while (true) {
            if (!processPlayerJob()) {
                Thread.sleep(config.idleSleep)
            }
        }
    }

}
