package gg.rebelsrising.aom.voobly.stats.core.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object TimeUtil {

    val FULL_FORMAT: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy - hh:mm a").withZoneUTC()
    val DAY_FORMAT: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy").withZoneUTC()

    fun getDate(dirtyDate: String): DateTime {
        if (dirtyDate.contains("minutes ago")) {
            val minsAgo = dirtyDate.substringBefore(" minutes ago").toInt()
            return DateTime(UTC)
                .minusMinutes(minsAgo)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0)
        }

        var dateString = dirtyDate

        if (dirtyDate.contains("Today")) {
            val todayDate = DateTime(UTC)
            dateString = DAY_FORMAT.print(todayDate) + " - " + dirtyDate.substringAfter("Today, ")
        } else if (dirtyDate.contains("Yesterday")) {
            val yesterdayDate = DateTime(UTC).minusDays(1)
            dateString = DAY_FORMAT.print(yesterdayDate) + " - " + dirtyDate.substringAfter("Yesterday, ")
        }

        return FULL_FORMAT.parseDateTime(dateString)
    }

    fun getDuration(dirtyDuration: String): Int {
        val split = dirtyDuration.split(":")
        return split[0].toInt() * 3600 + split[1].toInt() * 60 + split[2].toInt()
    }

}
