package gg.rebelsrising.aom.voobly.stats.core.scraper

import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.PAGEBROWSER
import mu.KLogger
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Period
import org.joda.time.PeriodType

abstract class IdScraper(
    val session: Session,
    val sleepMillis: Long
) : Scraper {

    companion object {

        const val INIT_PAGE_ID = 0
        const val INVALID_ID = -1

        fun parseId(idString: String): Int {
            return idString.toIntOrNull() ?: -1
        }

    }

    abstract val logger: KLogger
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
                ScrapeResult.DUPLICATE -> currStats.duplicates++
                else -> currStats.failed++
            }
        }

        return currStats
    }

    fun scrapePageBrowser() {
        val s = ScrapeStats()
        var currPage = INIT_PAGE_ID
        var currStats: ScrapeStats

        do {
            currStats = scrapePage(currPage)

            logger.info { "Obtained ${currStats.new} new IDs and ${currStats.duplicates} duplicates (page ID: $currPage)." }

            s += currStats

            Thread.sleep(sleepMillis)

            currPage++
        } while (currStats.total > 0)

        logger.info {
            "Scraped up to browser page ${currPage - 1} (checked: ${s.total}, new: ${s.new}," +
                    " duplicates: ${s.duplicates}, failed: ${s.failed})."
        }
    }

    fun scrapePageBrowserDaily(numRunsPerDay: Long) {
        if (numRunsPerDay < 1) {
            return
        }

        while (true) {
            try {
                // TODO Info logging here.
                val startTime = DateTime.now()

                scrapePageBrowser()

                val endTime = DateTime.now()
                val delta = Period(startTime, endTime, PeriodType.millis())

                Thread.sleep(DateTimeConstants.MILLIS_PER_DAY / numRunsPerDay - delta.millis)
            } catch (e: Exception) {
                logger.error(e) { ScraperConst.SCRAPER_EXCEPTION_MSG }
            }
        }
    }

}
