package gg.rebelsrising.aom.voobly.stats.core.parser.match

import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import mu.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

class RecIdParser : Parser<String> {

    companion object {
        private const val CSS_QUERY = ".content table tbody tr td table tbody tr td a"
        private const val FILES_VIEW = "files/view/"
    }

    override fun parse(doc: Document): String {
        val recData = doc.select(CSS_QUERY).filter { e -> e.text().contains("Download Rec.") }

        // Take first rec.
        val recs = recData.filter { e -> e.attr("href").contains(FILES_VIEW) }

        if (recs.isEmpty()) {
            return ""
        }

        val recUrl = recs[0].attr("href").substringAfter(FILES_VIEW)

        logger.debug { "Rec ID: $recUrl" }

        // Clean prefix and return.
        return recUrl
    }

}
