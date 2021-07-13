package gg.rebelsrising.aom.voobly.stats.core.scraper

import gg.rebelsrising.aom.voobly.stats.core.dal.DbReader
import gg.rebelsrising.aom.voobly.stats.core.dal.DbWriter
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.parser.match.*
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper.ScrapeResult.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Scraper(val ladder: Ladder, val session: Session) {

    enum class ScrapeResult {
        SUCCESS, DUPLICATE, INSUFFICIENT_INFO, PARSE_ERROR, MALFORMED_ID, IO_ERROR
    }

    companion object {
        const val VOOBLY = "https://voobly.com/"
        const val VOOBLY_WWW = "https://www.voobly.com/"

        const val MATCHES = "ladder/matches/"
        const val MATCH_VIEW = "match/view/"
        const val PAGEBROWSER = "#pagebrowser"

        const val INIT_PAGE = 0
        const val NUM_PAGES = 100
    }

    private val matchBrowserIdParser = MatchBrowserIdParser()
    private val matchParser = MatchParser()
    private val ladderParser = LadderParser()
    private val playerParser = PlayerParser()
    private val recParser = RecIdParser()

    fun getRecIdsFromPageNum(pageNum: Int = INIT_PAGE): List<String> {
        val url = VOOBLY_WWW + MATCHES + ladder.idUrl + "/" + pageNum + PAGEBROWSER
        val doc = session.getRequest(url).parse()

        return matchBrowserIdParser.parse(doc)
    }

    fun getRecentRecIds(): ArrayList<String> {
        val ids = ArrayList<String>()

        for (i in INIT_PAGE until NUM_PAGES) {
            ids.addAll(getRecIdsFromPageNum(i))
        }

        return ids
    }

    fun idToMatchUrl(id: Int): String {
        return VOOBLY_WWW + MATCH_VIEW + id
    }

    fun getMatch(matchId: Int): Match {
        val url = idToMatchUrl(matchId)
        val doc = session.getRequest(url).parse()

        val match = matchParser.parse(doc)
        val ladder = ladderParser.parse(doc)
        val players = playerParser.parse(doc)
        val rec = recParser.parse(doc)

        match.process(ladder, players, rec)

        return match
    }

    fun scrapeMatch(matchId: String): ScrapeResult {
        val id = matchId.toIntOrNull()

        if (id == null) {
            logger.error { "Malformed match ID: $matchId" }
            return MALFORMED_ID
        }

        logger.debug { "Checking match ID $matchId." }

        // TODO Change this to upsert once it works for non-batch inserts.
        if (DbReader.matchExists(id)) {
            logger.debug("Duplicate match found in database, ignoring.")
            return DUPLICATE
        }

        logger.debug { "Processing new match." }

        val m: Match

        try {
            m = getMatch(id)
            logger.debug { "Successfully parsed match." }
        } catch (e: Exception) {
            logger.error { "Failed to parse match with ID $matchId!" }
            // TODO Better logging here.
            return PARSE_ERROR
        }

        if (!m.isComplete()) {
            logger.error { "Incomplete information for match!" }
            return INSUFFICIENT_INFO
        }

        // TODO Write rec.

        DbWriter.writeMatch(m)

        logger.debug { "Successfully wrote match to database." }

        return SUCCESS
    }

    fun scrapeLadderMatchBrowserPage(pageNum: Int): ScrapeStats {
        val ids = getRecIdsFromPageNum(pageNum)

        val stats = ScrapeStats()

        for (id in ids) {
            stats.total++

            when (scrapeMatch(id)) {
                SUCCESS -> stats.new++
                DUPLICATE -> stats.duplicates++
                else -> stats.failed++
            }
        }

        return stats
    }

    fun scrapePageBrowser(lastPage: Int = NUM_PAGES, maxPageDuplicates: Int = Int.MAX_VALUE) {
        var lastPageVisited = 0
        val stats = ScrapeStats()

        logger.info { "Scraping page browser for ladder ${ladder.name}." }

        for (i in INIT_PAGE until lastPage) {
            logger.info { "Scraping page with ID $i." }

            val currStats = scrapeLadderMatchBrowserPage(i)

            logger.info { "Obtained ${currStats.new} new matches and ${currStats.duplicates} duplicates (page ID: $i)." }

            stats += currStats
            lastPageVisited = i

            // Break early if we encountered a page with more duplicates than the specified value.
            if (currStats.duplicates > maxPageDuplicates) {
                break
            }
        }

        logger.info { "Stats from scraping up to match browser page ${lastPageVisited + 1} (page ID: $lastPageVisited):" }
        logger.info { "Checked: ${stats.total}" }
        logger.info { "Added: ${stats.new}" }
        logger.info { "Duplicates: ${stats.duplicates}" }
        logger.info { "Failed: ${stats.failed}" }
    }

}
