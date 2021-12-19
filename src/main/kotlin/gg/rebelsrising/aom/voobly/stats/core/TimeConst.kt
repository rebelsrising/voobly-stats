package gg.rebelsrising.aom.voobly.stats.core

object TimeConst {

    const val MILLIS_PER_SECOND: Long = 1000
    const val SECONDS_PER_MINUTE: Long = 3600
    const val MINUTES_PER_HOUR: Long = 60
    const val HOURS_PER_DAY: Long = 24
    const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
    const val SECONDS_PER_DAY = HOURS_PER_DAY * SECONDS_PER_HOUR
    const val MILLIS_PER_DAY = MILLIS_PER_SECOND * SECONDS_PER_DAY

}
