package gg.rebelsrising.aom.voobly.stats.core.parser.id

import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.MATCH_VIEW
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperConst.VOOBLY_WWW
import mu.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

class MatchIdParser : Parser<ArrayList<String>> {

    companion object {
        private const val CSS_QUERY = "tr a[href]"
    }

    override fun parse(doc: Document): ArrayList<String> {
        // TODO Consider adding as a setting whether we only want games with a rec or all games.
        // VOOBLY_WWW points to recorded games - this is something we normally want because otherwise we wouldn't know the map.
        val urls = doc.select(CSS_QUERY)
            .filter { e -> e.attr("href").startsWith(VOOBLY_WWW + MATCH_VIEW) }
            .map { e -> e.attr("href") }
            .toCollection(ArrayList())

        val ids = urls.map { e -> e.substringAfterLast("/") }

        logger.debug { "Match IDs: " + ids.joinToString(", ") { it } }

        return ids.toCollection(ArrayList())
    }

}


