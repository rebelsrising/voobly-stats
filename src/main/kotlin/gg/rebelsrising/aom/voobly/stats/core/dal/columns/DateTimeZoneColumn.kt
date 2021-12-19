package gg.rebelsrising.aom.voobly.stats.core.dal.columns

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Only works for Postgres as of now!

fun Table.offsetdatetime(name: String): Column<OffsetDateTime> {
    if (currentDialect.name != PostgreSQLDialect.dialectName) {
        throw UnsupportedOperationException("OffsetDateTime column not allowed for any dialect other than Postgres!")
    }

    return registerColumn(name, OffsetDateTimeColumnType())
}

class OffsetDateTimeColumnType : ColumnType() {

    override fun sqlType(): String = "TIMESTAMP WITH TIME ZONE"

    override fun nonNullValueToString(value: Any): String {
        if (value is String) return value

        val dateTime = when (value) {
            is OffsetDateTime -> value
            else -> throw IllegalArgumentException("OffsetDateTime column expects an OffsetDateTime object!")
        }

        return dateTime.toString()
    }

    override fun valueFromDB(value: Any): Any = when (value) {
        is OffsetDateTime -> value
        is Timestamp -> {
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(value.time), ZoneOffset.UTC)
        }
        else -> valueFromDB(value.toString()) // Will take the String branch above.
    }

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is OffsetDateTime -> Timestamp(value.toEpochSecond() * 1_000)
        else -> value
    }

}
