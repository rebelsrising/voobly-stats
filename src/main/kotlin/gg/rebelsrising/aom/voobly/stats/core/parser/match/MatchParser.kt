package gg.rebelsrising.aom.voobly.stats.core.parser.match

import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.util.TimeUtil
import mu.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

class MatchParser : Parser<Match> {

    companion object {
        private const val TABLE_CSS_QUERY = ".content table tbody tr td table tbody tr td"
    }

    override fun parse(doc: Document): Match {
        val table = doc.select(TABLE_CSS_QUERY)

        // This should work for AoM and AoE.
        // Use filters/lambdas if it turns out to be too unreliable.

        val matchId = table[1].text()
        val date = table[4].text()
        // val rating = table[6].text() // Calculated from player details.
        val map = table[8].text()
        val duration = table[10].text()
        // val players = table[12].text() // Unused.

        var mod = ""

        if (table.size >= 15) {
            mod = table[14].text()
        }

        logger.debug { "Match ID: $matchId Date: $date Map: $map Duration: $duration Mod: $mod" }

        return Match(
            matchId.substringAfter("#").toInt(),
            TimeUtil.getDate(date),
            TimeUtil.getDuration(duration),
            map,
            mod,
        )
    }

}
