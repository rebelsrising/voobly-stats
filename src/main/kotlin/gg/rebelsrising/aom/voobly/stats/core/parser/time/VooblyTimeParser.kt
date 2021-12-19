package gg.rebelsrising.aom.voobly.stats.core.parser.time

import gg.rebelsrising.aom.voobly.stats.core.TimeConst.MINUTES_PER_HOUR
import gg.rebelsrising.aom.voobly.stats.core.TimeConst.SECONDS_PER_HOUR
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

object VooblyTimeParser {

    // Parses the combined string (case-insensitive due to am/pm being lowercase).
    private val FULL_FORMAT = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMMM yyyy - h:mm a")
        .toFormatter()

    // Used to format the date for yesterday/today strings received.
    private val DAY_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun getDate(dirtyDate: String): OffsetDateTime {
        if (dirtyDate.contains("minutes ago")) {
            val minsAgo = dirtyDate.substringBefore(" minutes ago").toLong()
            return OffsetDateTime.now().minusMinutes(minsAgo)
        }

        var dateString = dirtyDate

        if (dirtyDate.contains("Today")) {
            // Prepare strings for matches from today.
            val todayDate = LocalDate.now()
            dateString = DAY_FORMAT.format(todayDate) + " - " + dirtyDate.substringAfter("Today, ")
        } else if (dirtyDate.contains("Yesterday")) {
            // Prepare strings for matches from yesterday.
            val yesterdayDate = LocalDate.now().minusDays(1)
            dateString = DAY_FORMAT.format(yesterdayDate) + " - " + dirtyDate.substringAfter("Yesterday, ")
        }

        val local = LocalDateTime.parse(dateString, FULL_FORMAT)

        return OffsetDateTime.of(local, ZoneOffset.UTC)
    }

    fun getDuration(dirtyDuration: String): Int {
        val split = dirtyDuration.split(":")
        return split[0].toInt() * SECONDS_PER_HOUR.toInt() + split[1].toInt() * MINUTES_PER_HOUR.toInt() + split[2].toInt()
    }

}
