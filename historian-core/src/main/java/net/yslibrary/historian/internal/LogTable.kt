package net.yslibrary.historian.internal

/**
 * Table definition of Log
 */
object LogTable {
    const val NAME = "log"

    @JvmField
    val CREATE_TABLE = """
        CREATE TABLE $NAME (id INTEGER PRIMARY KEY AUTOINCREMENT,priority TEXT NOT NULL, tag TEXT NOT NULL, message TEXT NOT NULL, created_at INTEGER NOT NULL);
    """.trimIndent()

    @JvmField
    val DROP_TABLE = "DROP TABLE $NAME;"

    @JvmField
    val INSERT = """
        INSERT INTO $NAME(priority, tag, message, created_at) VALUES(?, ?, ?, ?);
    """.trimIndent()

    @JvmField
    val DELETE_OLDER = """
        DELETE FROM $NAME where id NOT IN (SELECT id FROM log ORDER BY created_at DESC LIMIT ?);
    """.trimIndent()
}
