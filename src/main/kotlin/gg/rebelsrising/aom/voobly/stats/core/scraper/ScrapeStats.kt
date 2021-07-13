package gg.rebelsrising.aom.voobly.stats.core.scraper

class ScrapeStats(var total: Int = 0, var new: Int = 0, var duplicates: Int = 0, var failed: Int = 0) {

    operator fun plusAssign(other: ScrapeStats) {
        this.total += other.total
        this.new += other.new
        this.duplicates += other.duplicates
        this.failed += other.failed
    }

}
