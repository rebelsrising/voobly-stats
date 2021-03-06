package gg.rebelsrising.aom.voobly.stats.core.parser.match

import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import gg.rebelsrising.aom.voobly.stats.core.parser.time.VooblyTimeParser
import mu.KotlinLogging
import org.jsoup.nodes.Document
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

data class MatchMetaData(
    val matchId: Int,
    val date: OffsetDateTime,
    val duration: Int,
    val map: String,
    val mod: String?
)

class MatchParser : Parser<MatchMetaData> {

    companion object {

        private const val TABLE_CSS_QUERY = ".content table tbody tr td table tbody tr td"

    }

    // TODO Better exception handling/escalation.
    override fun parse(doc: Document): MatchMetaData {
        val table = doc.select(TABLE_CSS_QUERY)

        // This should work for AoM and AoE.
        // Use filters/lambdas if this turns out to be unreliable.

        val matchId = table[1].text()
        val date = table[4].text()
        // val rating = table[6].text() // Calculated from player details.
        val map = table[8].text().lowercase() // Fix casing.
        val duration = table[10].text()
        // val players = table[12].text() // Unused.

        val mod = if (table.size >= 15) {
            table[14].text()
        } else {
            null
        }

        logger.debug { "Match ID: $matchId Date: $date Map: $map Duration: $duration Mod: $mod" }

        return MatchMetaData(
            matchId.substringAfter("#").toInt(),
            VooblyTimeParser.getDate(date),
            VooblyTimeParser.getDuration(duration),
            map,
            mod,
        )
    }

}
