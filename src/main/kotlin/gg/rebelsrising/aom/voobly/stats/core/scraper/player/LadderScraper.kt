package gg.rebelsrising.aom.voobly.stats.core.scraper.player

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.IdScraperDailyConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.parser.id.PlayerIdParser
import gg.rebelsrising.aom.voobly.stats.core.scraper.IdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.LADDER_RANKING
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import gg.rebelsrising.aom.voobly.stats.core.Session
import mu.KotlinLogging

class LadderScraper(
    session: Session,
    val ladder: Ladder,
    val config: IdScraperDailyConfig
) : IdScraper(session, config.busySleep) {

    override val logger = KotlinLogging.logger {}
    override val urlPrefix = VOOBLY_WWW + LADDER_RANKING + ladder.idUrl
    override val idParser = PlayerIdParser()

    override fun processId(id: Int): ScrapeResult {
        return Db.insertPlayerJobIfNotDuplicate(PlayerScrapeJob(id, ladder))
    }

    override fun run() {
        scrapePageBrowserDaily(config.dailyInterval)
    }

}
