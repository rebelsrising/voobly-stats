package gg.rebelsrising.aom.voobly.stats.core

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session
import gg.rebelsrising.aom.voobly.stats.core.scraper.match.MatchScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.PlayerIdScraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.player.PlayerScraper

fun main() {

    // TODO Add CLI.

    val config = Config.load(Config.DEFAULT_CONFIG_FILE)
    val s = Session(config.voobly).login()

    Db.connect(config.database)
    Db.createTables()

    // TODO Reset entries from PROCESSING to OPEN.

    Thread(PlayerIdScraper(s, Ladder.AOT_1X, config.playerIdScraper)).start()
    Thread(PlayerScraper(s, Ladder.AOT_1X, config.matchIdScraper)).start()
    Thread(MatchScraper(s, Ladder.AOT_1X, config.matchScraper)).start()

}
