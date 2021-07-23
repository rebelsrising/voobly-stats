package gg.rebelsrising.aom.voobly.stats.core.scraper

import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.PAGEBROWSER
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

abstract class IdScraper(
    val session: Session,
    val ladder: Ladder,
    val sleepMillis: Long
) : Scraper {

    companion object {
        const val INIT_PAGE_ID = 0
        const val INVALID_ID = -1

        fun parseId(idString: String): Int {
            return idString.toIntOrNull() ?: -1
        }
    }

    // TODO Add scraper name.
    abstract val urlPrefix: String
    abstract val idParser: Parser<ArrayList<String>>

    abstract fun processId(id: Int): ScrapeResult

    fun getMatchIdsFromPage(pageNum: Int): ArrayList<String> {
        val url = "$urlPrefix/$pageNum$PAGEBROWSER"
        val doc = session.getRequest(url).parse()

        return idParser.parse(doc)
    }

    fun scrapePage(pageId: Int): ScrapeStats {
        logger.info { "Scraping page with ID $pageId for IDs." }

        val currStats = ScrapeStats()

        val currIds = getMatchIdsFromPage(pageId)

        for (stringId in currIds) {
            currStats.total++

            val id = parseId(stringId)

            if (id == INVALID_ID) {
                currStats.failed++
                continue
            }

            when (processId(id)) {
                ScrapeResult.SUCCESS -> currStats.new++
                ScrapeResult.DUPLICATE -> currStats.duplicates
                else -> currStats.failed++
            }
        }

        return currStats
    }

    fun scrapePageBrowser() {
        val stats = ScrapeStats()
        var currPage = INIT_PAGE_ID
        var currStats: ScrapeStats

        do {
            currStats = scrapePage(currPage)

            logger.info { "Obtained ${currStats.new} new IDs and ${currStats.duplicates} duplicates (page ID: $currPage)." }

            stats += currStats

            Thread.sleep(sleepMillis)

            currPage++
        } while (currStats.total > 0)

        logger.info { "Stats from scraping up to browser page $currPage (page ID: ${currPage - 1}):" }
        logger.info { "Checked: ${stats.total}" }
        logger.info { "Added: ${stats.new}" }
        logger.info { "Duplicates: ${stats.duplicates}" }
        logger.info { "Failed: ${stats.failed}" }
    }

}
