package gg.rebelsrising.aom.voobly.stats.core

import gg.rebelsrising.aom.voobly.stats.core.config.Config
import gg.rebelsrising.aom.voobly.stats.core.dal.Db
import gg.rebelsrising.aom.voobly.stats.core.model.Ladder
import gg.rebelsrising.aom.voobly.stats.core.scraper.Scraper
import gg.rebelsrising.aom.voobly.stats.core.scraper.Session

fun main() {
    val s = Session(Config.load("config.json").voobly)
    s.login()

    Db.createTables()

    Scraper(Ladder.AOT_1X, s).scrapePageBrowser()
}
