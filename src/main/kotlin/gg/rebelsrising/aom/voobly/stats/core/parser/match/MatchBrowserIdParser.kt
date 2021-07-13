package gg.rebelsrising.aom.voobly.stats.core.parser.match

import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper
import mu.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

class MatchBrowserIdParser : Parser<List<String>> {

    companion object {
        private const val CSS_QUERY = "tr a[href]"
    }

    override fun parse(doc: Document): List<String> {
        val urls = doc.select(CSS_QUERY)
            .filter { e -> e.attr("href").contains(Scraper.VOOBLY + Scraper.MATCH_VIEW) }
            .map { e -> e.attr("href") }
            .toCollection(ArrayList())

        val ids = urls.map { e -> e.substringAfterLast("/") }

        logger.debug { "Match IDs: " + ids.joinToString(", ") { it } }

        return ids
    }

}
