package gg.rebelsrising.aom.voobly.stats.core.scraper.player

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.PlayerIdScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.PlayerScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.parser.id.TopPlayerIdParser
import gg.rebelsrising.aom.voobly.stats.core.scraper.IdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.LADDER_RANKING
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.Period
import kotlin.math.max

private val logger = KotlinLogging.logger {}

class PlayerIdScraper(
    session: Session,
    ladder: Ladder,
    val config: PlayerIdScraperConfig
) : IdScraper(session, ladder, config.busySleep) {

    companion object {
        private const val MILLIS_PER_DAY: Long = 86_400_000
    }

    override val urlPrefix = VOOBLY_WWW + LADDER_RANKING + ladder.idUrl
    override val idParser = TopPlayerIdParser()

    override fun processId(id: Int): ScrapeResult {
        return Db.insertPlayerJobIfNotDuplicate(PlayerScrapeJob(id, ladder))
    }

    override fun run() {
        while (true) {
            try {
                // TODO Consider using something like https://stackoverflow.com/questions/25296718/repeat-an-action-every-2-seconds-in-java here.

                val currTime = DateTime.now()

                scrapePageBrowser()

                val delta = Period(currTime, DateTime.now()).millis

                Thread.sleep((MILLIS_PER_DAY) / max(1, config.dailyInterval) - delta)
            } catch (e: Exception) {
                logger.error(e) { " Exception in PlayerIdScraper occurred! " }
            }
        }
    }

}
