@file:Suppress("DuplicatedCode")

package gg.rebelsrising.aom.voobly.stats.core.dal

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

// Batched upsert functionality, inspired by:
// https://gist.github.com/oshai/9b58cf3365257c9324fa4dcccb2481b2#file-upsert-kt
// https://github.com/LukasForst/exposed-upsert/blob/master/src/main/kotlin/pw/forst/exposed/InsertOrUpdate.kt

// If we ever switch away from Postgres, use https://github.com/dzikoysk/exposed-upsert instead.

class BatchUpsert(
    table: Table,
    private val onDupUpdate: List<Column<*>>,
    ignoreErrors: Boolean = false
) : BatchInsertStatement(table, ignoreErrors) {

    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {

            // Build the suffix of the statement.
            " ON CONFLICT (" + onDupUpdate.joinToString(", ") {
                transaction.identity(it) // The PK or unique columns to decide whether we update.
            } + ") DO UPDATE SET " + table.columns.joinToString {
                "${transaction.identity(it)} = EXCLUDED.${transaction.identity(it)}" // All the other columns.
            }

        } else "" // Empty list provided, could also throw an exception here.

        return super.prepareSQL(transaction) + onUpdateSQL
    }

}

fun <T : Table, E> T.batchUpsert(
    data: List<E>,
    onDupUpdateColumns: List<Column<*>>,
    body: T.(BatchUpsert, E) -> Unit
) {
    data.takeIf { it.isNotEmpty() }?.let {
        val upsert = BatchUpsert(this, onDupUpdateColumns)

        // Add batch and insert the data provided.
        data.forEach {
            upsert.addBatch()
            body(upsert, it)
        }

        TransactionManager.current().exec(upsert)
    }
}

class Upsert<Number : Any>(
    table: Table,
    private val onDupUpdate: List<Column<*>>,
    ignoreErrors: Boolean = false
) : InsertStatement<Number>(table, ignoreErrors) {

    // This is pretty much the same as for the batch version.
    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {

            // Build the suffix of the statement.
            " ON CONFLICT (" + onDupUpdate.joinToString(", ") {
                transaction.identity(it) // The PK or unique columns to decide whether we update.
            } + ") DO UPDATE SET " + table.columns.joinToString {
                "${transaction.identity(it)} = EXCLUDED.${transaction.identity(it)}" // All the other columns.
            }

        } else "" // Empty list provided, could also throw an exception here.

        return super.prepareSQL(transaction) + onUpdateSQL
    }

}

fun <T : Table> T.upsert(
    onDupUpdate: List<Column<*>>,
    ignoreErrors: Boolean = false,
    body: T.(InsertStatement<Number>) -> Unit
): Upsert<Number> = Upsert<Number>(this, onDupUpdate, ignoreErrors).apply {
    body(this)
    execute(TransactionManager.current())
}
