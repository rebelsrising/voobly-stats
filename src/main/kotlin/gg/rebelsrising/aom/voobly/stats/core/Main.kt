package gg.rebelsrising.aom.voobly.stats.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import gg.rebelsrising.aom.voobly.stats.core.rec.RecLauncher
import gg.rebelsrising.aom.voobly.stats.core.scraper.ScraperLauncher

class Main : CliktCommand() {

    override fun run() = Unit

}

fun main(args: Array<String>): Unit = Main().subcommands(ScraperLauncher(), RecLauncher()).main(args)

