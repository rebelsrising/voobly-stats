package gg.rebelsrising.aom.voobly.stats.core.parser.util

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.MINUTES_PER_HOUR
import org.joda.time.DateTimeConstants.SECONDS_PER_HOUR
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object TimeUtil {

    val FULL_FORMAT: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy - hh:mm a")
    val DAY_FORMAT: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy")

    fun getDate(dirtyDate: String): DateTime {
        if (dirtyDate.contains("minutes ago")) {
            val minsAgo = dirtyDate.substringBefore(" minutes ago").toInt()
            return DateTime()
                .minusMinutes(minsAgo)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0)
        }

        var dateString = dirtyDate

        if (dirtyDate.contains("Today")) {
            val todayDate = DateTime()
            dateString = DAY_FORMAT.print(todayDate) + " - " + dirtyDate.substringAfter("Today, ")
        } else if (dirtyDate.contains("Yesterday")) {
            val yesterdayDate = DateTime().minusDays(1)
            dateString = DAY_FORMAT.print(yesterdayDate) + " - " + dirtyDate.substringAfter("Yesterday, ")
        }

        return FULL_FORMAT.parseDateTime(dateString)
    }

    fun getDuration(dirtyDuration: String): Int {
        val split = dirtyDuration.split(":")
        return split[0].toInt() * SECONDS_PER_HOUR + split[1].toInt() * MINUTES_PER_HOUR + split[2].toInt()
    }

}
