package gg.rebelsrising.aom.voobly.stats.core.scraper.player

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.IdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.parser.id.MatchIdParser
import gg.rebelsrising.aom.voobly.stats.core.scraper.IdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.USER_MATCHES
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PlayerHistoryScraper(
    session: Session,
    val pJob: PlayerScrapeJob,
    val config: IdScraperConfig
) : IdScraper(session, config.busySleep) {

    override val logger = KotlinLogging.logger {}
    override val urlPrefix = VOOBLY_WWW + USER_MATCHES + pJob.id + "/" + pJob.ladder.idUrl
    override val idParser = MatchIdParser()

    override fun processId(id: Int): ScrapeResult {
        val ret = Db.insertMatchJobIfNotScrapedOrDuplicate(MatchScrapeJob(id, pJob.ladder))

        // Update player job processing time.
        Db.updatePlayerJob(pJob)

        return ret
    }

    // Use PlayerScraper, which essentially wraps around this class.
    override fun run() {
        throw NotImplementedError()
    }

}

class PlayerScraper(
    val session: Session,
    val ladder: Ladder,
    val config: IdScraperConfig
) : Scraper {

    fun processPlayerJob(): Boolean {
        // Not too many pending match jobs, get player job and scrape the player's match history.
        val playerJob = Db.getPlayerJobForProcessing(ladder) ?: return false

        PlayerHistoryScraper(session, playerJob, config).scrapePageBrowser()

        playerJob.status = PlayerScrapeJob.PlayerScrapeStatus.OPEN

        Db.updatePlayerJob(playerJob)

        return true
    }

    override fun run() {
        while (true) {
            try {
                if (!processPlayerJob()) {
                    // Happens if we have plenty of match IDs with status OPEN or no OPEN player jobs.
                    Thread.sleep(config.idleSleep)
                }
            } catch (e: Exception) {
                logger.error(e) { "Caught exception while running!" }
            }
        }
    }

}
