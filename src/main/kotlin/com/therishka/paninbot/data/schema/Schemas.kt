package com.therishka.paninbot.data.schema

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

object Chats : Table("chats") {
    val id = long("id")
    val name = varchar("name", length = 50)
    val active = bool("active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object Users : Table("users") {
    val id = long("id")
    val name = varchar("name", length = 100)
    override val primaryKey = PrimaryKey(id)
}

object Users2Chats : Table("users2chats") {
    val chat_id = long("chat_id").references(Chats.id)
    val user_id = long("user_id").references(Users.id)
    val active = bool("active").default(false)
    val rating = integer("rating").default(0)

    override val primaryKey = PrimaryKey(chat_id, user_id)
}

fun <T : Table> T.insertOrUpdate(vararg keys: Column<*>, body: T.(InsertStatement<Number>) -> Unit) =
        InsertOrUpdate<Number>(this, keys = *keys).apply {
            body(this)
            execute(TransactionManager.current())
        }

class InsertOrUpdate<Key : Any>(
        table: Table,
        isIgnore: Boolean = false,
        private vararg val keys: Column<*>
) : InsertStatement<Key>(table, isIgnore) {
    override fun prepareSQL(transaction: Transaction): String {
        val tm = TransactionManager.current()
        val updateSetter = table.columns.joinToString { "${tm.identity(it)} = EXCLUDED.${tm.identity(it)}" }
        val onConflict = "ON CONFLICT (${keys.joinToString { tm.identity(it) }}) DO UPDATE SET $updateSetter"
        return "${super.prepareSQL(transaction)} $onConflict"
    }
}