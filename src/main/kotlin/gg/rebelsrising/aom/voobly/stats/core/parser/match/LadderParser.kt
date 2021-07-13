package gg.rebelsrising.aom.voobly.stats.core.parser.match

import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import mu.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

class LadderParser : Parser<Ladder> {

    companion object {
        private const val CSS_QUERY = "#content div table tr td a"
        private const val LADDER_VIEW = "ladder/view/"
    }

    override fun parse(doc: Document): Ladder {
        val a = doc.select(CSS_QUERY)

        if (a.size < 3) {
            return Ladder.UNKNOWN
        }

        val ladderUrl = a[2].attr("href").substringAfter(LADDER_VIEW)

        logger.debug { "Ladder URL: $ladderUrl" }

        return Ladder.byUrl(ladderUrl)
    }

}
