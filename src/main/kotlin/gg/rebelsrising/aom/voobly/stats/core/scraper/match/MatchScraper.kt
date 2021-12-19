package gg.rebelsrising.aom.voobly.stats.core.scraper.match

import gg.rebelsrising.aom.voobly.stats.core.config.scraper.MatchScraperConfig
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob
import gg.rebelsrising.aom.voobly.stats.core.model.MatchScrapeJob.MatchScrapeStatus
import gg.rebelsrising.aom.voobly.stats.core.parser.match.LadderParser
import gg.rebelsrising.aom.voobly.stats.core.parser.match.MatchParser
import gg.rebelsrising.aom.voobly.stats.core.parser.match.PlayerParser
import gg.rebelsrising.aom.voobly.stats.core.parser.match.RecIdParser
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeResult.*
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScrapeStats
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.MATCH_VIEW
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.SCRAPER_EXCEPTION_MSG
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import gg.rebelsrising.aom.voobly.stats.core.Session
import mu.KotlinLogging
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

class MatchScraper(
    val session: Session,
    val ladder: Ladder,
    val config: MatchScraperConfig
) : Scraper {

    companion object {

        fun idToMatchUrl(id: Int): String {
            return VOOBLY_WWW + MATCH_VIEW + id
        }

    }

    private val matchParser = MatchParser()
    private val ladderParser = LadderParser()
    private val playerParser = PlayerParser()
    private val recParser = RecIdParser()

    fun getMatch(matchId: Int): Match {
        val url = idToMatchUrl(matchId)
        val doc = session.getRequest(url).parse()

        val meta = matchParser.parse(doc)
        val ladder = ladderParser.parse(doc)
        val players = playerParser.parse(doc)
        val rec = recParser.parse(doc)

        return Match.fromData(meta, ladder, players, rec)
    }

    fun processMatchJob(job: MatchScrapeJob): ScrapeResult {
        logger.info { "Processing match ID ${job.id}." }

        if (Db.matchExists(job.id)) {
            logger.warn { "Match ${job.id} has already been stored - this should not happen!" }
            logger.warn { "Marking entry in match job table as done." }

            job.status = MatchScrapeStatus.DONE

            Db.updateMatchJob(job)

            return DUPLICATE
        }

        val m: Match

        try {
            m = getMatch(job.id)
            logger.debug { "Successfully parsed match." }
        } catch (e: Exception) {
            logger.error { "Failed to parse match with ID ${job.id}!" }

            job.status = MatchScrapeStatus.FAILED

            Db.updateMatchJob(job)

            return PARSE_ERROR
        }

        if (!m.isComplete(config.requireMap)) {
            logger.info { "Incomplete information for match with ID ${job.id}!" }

            // The delay between reprocessing is defined via the lastProcessingThresholdMins config parameter.
            // TODO This may not be necessary (delaying makes no sense if lastProcessingThresholdMins is > ~5 minutes).
            if (job.status == MatchScrapeStatus.DELAYED) {
                job.status = MatchScrapeStatus.FAILED
            } else {
                job.status = MatchScrapeStatus.DELAYED
            }

            Db.updateMatchJob(job)

            return INSUFFICIENT_INFO
        }

        try {
            Db.writeMatchAndUpdateJob(m)
        } catch (e: Exception) {
            job.status = MatchScrapeStatus.FAILED

            Db.updateMatchJob(job)

            logger.error { "Failed to write match with ID ${job.id}!" }

            return WRITE_ERROR
        }

        logger.info { "Successfully wrote match with ID ${job.id} to database." }

        return SUCCESS
    }

    fun scrapeMatchJobBatch(): Boolean {
        val matchJobs = Db.getMatchJobBatch(
            config.batchSize,
            ladder,
            OffsetDateTime.now().minusMinutes(config.lastProcessingThresholdMins.toLong())
        )

        if (matchJobs.isEmpty()) {
            return false
        }

        // Process stats.
        val s = ScrapeStats()

        for (job in matchJobs) {
            when (processMatchJob(job)) {
                SUCCESS -> s.new++
                DUPLICATE -> s.duplicates++
                INSUFFICIENT_INFO -> s.failed++
                PARSE_ERROR -> s.failed++
                WRITE_ERROR -> s.failed++
            }

            s.total++

            Thread.sleep(config.busySleep)
        }

        logger.info {
            "Processed ${s.total} matches in batch (new: ${s.new}, duplicates: ${s.duplicates}," +
                    " failed: ${s.failed})."
        }

        return true
    }

    override fun run() {
        while (true) {
            try {
                if (!scrapeMatchJobBatch()) {
                    Thread.sleep(config.idleSleep)
                }
            } catch (e: Exception) {
                logger.error(e) { SCRAPER_EXCEPTION_MSG }
            }
        }
    }

}
