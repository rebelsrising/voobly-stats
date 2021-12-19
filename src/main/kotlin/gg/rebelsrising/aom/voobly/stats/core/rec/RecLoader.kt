package gg.rebelsrising.aom.voobly.stats.core.rec

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Civ
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.model.Match
import gg.rebelsrising.aom.voobly.stats.core.model.Player
import gg.rebelsrising.aom.voobly.stats.core.Session
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

class RecLoader(
    val session: Session,
    val config: Config,
    val ladder: Ladder,
    val minRating: Int,
    val civ: Civ,
    val playerId: Int,
    val map: String,
    val patch: String
) : Runnable {

    companion object {

        const val REC_SUFFIX = "rcx"
        const val REC_URL = "https://www.voobly.com/files/view/"

        private fun getPlayerDetailString(p: Player): String {
            return "${p.name}_(${p.civ.toText()})"
        }

        private fun buildFileName(m: Match, pl: List<Player>): String {
            val map = m.map.replace("/", "-")

            if (pl.size > 2) {
                return "${m.matchId}_${map}_${m.rating}".replace(" ", "_")
            }

            val p0Details = getPlayerDetailString(pl[0])
            val p1Details = getPlayerDetailString(pl[1])

            return "${m.matchId}_${map}_${p0Details}_${p1Details}".replace(" ", "_")
        }

    }

    private val recPath = Path.of(config.download.storage)

    private fun getRecFromUrl(m: Match, players: List<Player>) {
        // Build filename.
        val fileName = buildFileName(m, players)

        val recZip = session.getRequest(REC_URL + m.recUrl).bodyAsBytes()
        val zis = ZipInputStream(ByteArrayInputStream(recZip))

        // We always have exactly 1 file (if we have none something went wrong).
        zis.nextEntry

        val bytes = zis.readAllBytes()
        if (bytes.isNotEmpty()) {
            val file = recPath.resolve("$fileName.$REC_SUFFIX").toFile()
            file.writeBytes(bytes)
        }
    }

    override fun run() {
        val matches = Db.getMatchDataByCondition(
            ladder = ladder,
            minRating = minRating,
            civ = civ,
            playerId = playerId,
            mapString = map,
            patch = patch
        )

        val playerMap = Db.getPlayerIdsForMatchIds(matches.map { it.matchId })

        Files.createDirectories(recPath)

        var succRecs = 0
        val totRecs = matches.size

        logger.info { "Attempting to download $totRecs recorded games..." }

        for ((i, m) in matches.withIndex()) {
            logger.info { "Downloading rec $i/${totRecs}..." }

            val id = m.matchId

            try {
                getRecFromUrl(m, playerMap[id]!!)

                logger.info { "Downloaded rec with ID $id." }

                succRecs++
            } catch (e: Exception) {
                logger.error { "Failed to get rec with ID $id." }
            }
        }

        logger.info { "Downloaded $succRecs/$totRecs recorded games." }
    }

}
