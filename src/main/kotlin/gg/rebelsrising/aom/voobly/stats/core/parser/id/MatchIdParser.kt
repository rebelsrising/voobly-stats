package gg.rebelsrising.aom.voobly.stats.core.parser.id

import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.MATCH_VIEW
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY
import mu.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

// TODO Merge this with TopPlayerIdParser (arguments: filterString).
class MatchIdParser : Parser<ArrayList<String>> {

    companion object {
        private const val CSS_QUERY = "tr a[href]"
    }

    override fun parse(doc: Document): ArrayList<String> {
        // Do NOT use www.voobly here as matches don't have the prefix, but links to recorded games do!
        val urls = doc.select(CSS_QUERY)
            .filter { e -> e.attr("href").contains(VOOBLY + MATCH_VIEW) }
            .map { e -> e.attr("href") }
            .toCollection(ArrayList())

        val ids = urls.map { e -> e.substringAfterLast("/") }

        logger.debug { "Match IDs: " + ids.joinToString(", ") { it } }

        return ids.toCollection(ArrayList())
    }

}
