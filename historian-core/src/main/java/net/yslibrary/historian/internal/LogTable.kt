package net.yslibrary.historian.internal

/**
 * Table definition of Log
 */
object LogTable {
    const val NAME = "log"

    val CREATE_TABLE = buildString {
        append("CREATE TABLE $NAME (")
        append("id INTEGER PRIMARY KEY AUTOINCREMENT,")
        append("priority TEXT NOT NULL,")
        append("tag TEXT NOT NULL,")
        append("message TEXT NOT NULL,")
        append("created_at INTEGER NOT NULL")
        append(");")
    }

    const val DROP_TABLE = "DROP TABLE $NAME;"

    const val INSERT = "INSERT INTO $NAME(priority, tag, message, created_at) VALUES(?, ?, ?, ?);"

    const val DELETE_OLDER = "DELETE FROM $NAME WHERE id NOT IN (SELECT id FROM $NAME ORDER BY created_at DESC LIMIT ?);"
}
