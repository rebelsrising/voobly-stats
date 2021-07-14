package gg.rebelsrising.aom.voobly.stats.core

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session

fun main() {
    val config = Config.load(Config.DEFAULT_CONFIG_FILE)

    val s = Session(config.voobly).login()

    Db.connect(config.database)

    Db.createTables()

    Scraper(Ladder.AOT_1X, s).scrapePageBrowser()
}
