package gg.rebelsrising.aom.voobly.stats.core.parser.match

import gg.rebelsrising.aom.voobly.stats.core.model.Civ
import gg.rebelsrising.aom.voobly.stats.core.model.Player
import gg.rebelsrising.aom.voobly.stats.core.parser.Parser
import mu.KotlinLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private val logger = KotlinLogging.logger {}

class PlayerParser : Parser<ArrayList<Player>> {

    companion object {
        private const val CSS_QUERY =
            ".content > table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr"

        private const val PROFILE_VIEW = "profile/view/"
        private const val RES_CIVS_PREFIX = "res/games/"
        private const val RES_CIVS_SUFFIX = "civs/"

        private const val CIV_URL = RES_CIVS_PREFIX + "AOM/" + RES_CIVS_SUFFIX
    }

    private fun parsePlayer(e: Element): Player {
        // Links: Player name and id, team name and url.
        val a = e.select("a")

        var name = ""
        var id = ""
        var teamName = ""
        var teamUrl = ""

        for (elem in a) {
            if (elem.attr("href").contains(PROFILE_VIEW)) {
                name = elem.text()
                id = elem.attr("href").substringAfterLast("/")
            } else {
                teamName = elem.text()
                teamUrl = elem.attr("href").substringAfter("://").substringBefore(".")
            }
        }

        logger.debug { "ID: $id Name: $name Team Name: $teamName Team URL: $teamUrl" }

        // Img: Civ.
        val img = e.select("img")

        val civId: String = img.first { i -> i.attr("src").contains(CIV_URL) }
            .attr("src").substringAfterLast("/").substringBefore(".jpg")

        // Team, rate, and delta.
        val span = e.select("span")

        val newRate = span.text().substringAfter("New Rating: ").substringBefore(" ")
        val delta = span.text().substringAfter("Points: ").substringBefore(" ")
        val team = span.text().substringAfter("Team: ").substringBefore(" ")

        logger.debug { "Civ ID $civId Team: $team New rate: $newRate Delta: $delta" }

        return Player(
            id.toIntOrNull() ?: -1,
            name,
            teamName,
            teamUrl,
            Civ.byId(civId.toIntOrNull() ?: -1),
            team.toByteOrNull() ?: -1,
            newRate.toShortOrNull() ?: 0,
            delta.toByteOrNull() ?: 0,
        )
    }

    override fun parse(doc: Document): ArrayList<Player> {
        val playerElements = doc.select(CSS_QUERY)
        val players = ArrayList<Player>()

        for (e in playerElements) {
            players.add(parsePlayer(e))
        }

        return players
    }

}
