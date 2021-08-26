package gg.rebelsrising.aom.voobly.stats.core.dal.util

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.IDateColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

// Only works for Postgres as of now!

private val DEFAULT_DATE_TIME_ZONE_STRING_FORMATTER = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSSSSS Z")

fun Table.datetimezone(name: String): Column<DateTime> {
    if (currentDialect.name != PostgreSQLDialect.dialectName) {
        throw UnsupportedOperationException(
            "Date Time Zone column not allowed for any dialect other than Postgres!"
        )
    }

    return registerColumn(name, DateTimeZoneColumnType())
}

class DateTimeZoneColumnType : ColumnType(), IDateColumnType {

    override val hasTimePart: Boolean = true
    override fun sqlType(): String = "TIMESTAMP WITH TIME ZONE"

    override fun nonNullValueToString(value: Any): String {
        if (value is String) return value

        val dateTime = when (value) {
            is DateTime -> value
            else -> throw IllegalArgumentException("Date Time Zone column expects a DateTime object!")
        }

        return "'${DEFAULT_DATE_TIME_ZONE_STRING_FORMATTER.print(dateTime)}'"
    }

    override fun valueFromDB(value: Any): Any = when (value) {
        is DateTime -> value
        is String -> {
            DateTime.parse(value, formatterForDateTimeString(value))
        }
        else -> valueFromDB(value.toString()) // Will take the String branch above.
    }

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is DateTime -> java.sql.Timestamp(value.millis)
        else -> value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateTimeZoneColumnType) return false
        if (!super.equals(other)) return false

        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    private fun formatterForDateTimeString(date: String) =
        dateTimeWithFractionFormat(date.substringAfterLast('.', "").length)

    private fun dateTimeWithFractionFormat(fraction: Int): DateTimeFormatter {
        val baseFormat = "YYYY-MM-dd HH:mm:ss"
        val newFormat = if (fraction in 1..9) {
            (1..fraction).joinToString(prefix = "$baseFormat.", separator = "") { "S" }
        } else {
            baseFormat
        }

        return DateTimeFormat.forPattern(newFormat)
    }

}
