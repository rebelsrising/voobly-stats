package gg.rebelsrising.aom.voobly.stats.core.scraper.recent

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.IdScraperDailyConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.parser.id.MatchIdParser
import gg.rebelsrising.aom.voobly.stats.core.scraper.IdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.LADDER_MATCHES
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import gg.rebelsrising.aom.voobly.stats.core.Session
import mu.KotlinLogging

class RecentScraper(
    session: Session,
    val ladder: Ladder,
    val config: IdScraperDailyConfig
) : IdScraper(session, config.busySleep) {

    override val logger = KotlinLogging.logger {}
    override val urlPrefix = VOOBLY_WWW + LADDER_MATCHES + ladder.idUrl
    override val idParser = MatchIdParser()

    override fun processId(id: Int): ScrapeResult {
        return Db.insertMatchJobIfNotScrapedOrDuplicate(MatchScrapeJob(id, ladder))
    }

    override fun run() {
        scrapePageBrowserDaily(config.dailyInterval)
    }

}
