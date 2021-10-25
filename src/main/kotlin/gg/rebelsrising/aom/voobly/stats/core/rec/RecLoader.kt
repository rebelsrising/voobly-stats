package gg.rebelsrising.aom.voobly.stats.core.rec

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.model.Player
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

// TODO Add CLI command directly for this.
class RecLoader(val session: Session, val config: Config) {

    companion object {

        const val REC_URL = "https://www.voobly.com/files/view/"

        private fun getPlayerDetailString(p: Player): String {
            return "${p.name}_(${p.civ.toText()})"
        }

        private fun buildFileName(m: Match, pl: List<Player>): String {
            val p0Details = getPlayerDetailString(pl[0])
            val p1Details = getPlayerDetailString(pl[1])

            return "${m.matchId}_${p0Details}_${p1Details}_${m.map}".replace(" ", "_")
        }

    }

    fun run() {
        // TODO Add logging.

        val matches = Db.getMatchDataByCondition(
            ladder = Ladder.AOT_1X,
            minRating = 1750
        )

        val playerMap = Db.getPlayerIdsForMatchIds(matches.map { it.matchId })

        for (m in matches) {
            val fileName = buildFileName(m, playerMap[m.matchId]!!)
            val rec = m.recUrl

            val recZip = session.getRequest(REC_URL + rec).bodyAsBytes()
            val zis = ZipInputStream(ByteArrayInputStream(recZip))
            zis.nextEntry;

            // TODO Take path from config (at start of function).
            File("recs/$fileName.rcx").writeBytes(zis.readAllBytes())
        }

        logger.info { "Done." }
    }

}
