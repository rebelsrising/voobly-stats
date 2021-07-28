package gg.rebelsrising.aom.voobly.stats.core.scraper.player

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchIdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.parser.id.MatchIdParser
import gg.rebelsrising.aom.voobly.stats.core.scraper.IdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.USER_MATCHES
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session

class MatchIdScraper(
    session: Session,
    val pJob: PlayerScrapeJob,
    val config: MatchIdScraperConfig
) : IdScraper(session, pJob.ladder, config.busySleep) {

    override val urlPrefix = VOOBLY_WWW + USER_MATCHES + pJob.id + "/" + ladder.idUrl
    override val idParser = MatchIdParser()

    override fun processId(id: Int): ScrapeResult {
        val ret = Db.insertMatchJobIfNotScrapedOrDuplicate(MatchScrapeJob(id, ladder))

        // Update player job processing time.
        Db.updatePlayerJob(pJob)

        return ret
    }

    // Use PlayerScraper, which essentially wraps around this class.
    override fun run() {
        throw NotImplementedError()
    }

}
