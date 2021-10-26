package gg.rebelsrising.aom.voobly.stats.core.rec

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Civ
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.model.Player
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

class RecLoader(
    val session: Session,
    val config: Config,
    val ladder: Ladder,
    val minRating: Int,
    val civ: Civ,
    val playerId: Int,
    val map: String
) : Runnable {

    companion object {

        const val REC_URL = "https://www.voobly.com/files/view/"

        private fun getPlayerDetailString(p: Player): String {
            return "${p.name}_(${p.civ.toText()})"
        }

        private fun buildFileName(m: Match, pl: List<Player>): String {
            val p0Details = getPlayerDetailString(pl[0])
            val p1Details = getPlayerDetailString(pl[1])
            val map = m.map.replace("/", "-")

            return "${m.matchId}_${p0Details}_${p1Details}_${map}".replace(" ", "_")
        }

    }

    fun getRecFromUrl(m: Match, players: List<Player>) {
        val fileName = buildFileName(m, players)
        val rec = m.recUrl

        val recZip = session.getRequest(REC_URL + rec).bodyAsBytes()
        val zis = ZipInputStream(ByteArrayInputStream(recZip))
        zis.nextEntry

        // TODO Take path from config (at start of function).
        File("recs/$fileName.rcx").writeBytes(zis.readAllBytes())
    }

    override fun run() {
        val matches = Db.getMatchDataByCondition(
            ladder = ladder,
            minRating = minRating,
            civ = civ,
            playerId = playerId,
            mapString = map
        )

        val playerMap = Db.getPlayerIdsForMatchIds(matches.map { it.matchId })

        for (m in matches) {
            try {
                getRecFromUrl(m, playerMap[m.matchId]!!)
            } catch (e: Exception) {
                logger.error { "Failed to get rec (either name or file) with ID ${m.matchId}." }
            }
        }

        logger.info { "Done." }
    }

}
