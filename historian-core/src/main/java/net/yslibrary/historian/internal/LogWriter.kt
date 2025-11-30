package net.yslibrary.historian.internal

/**
 * Class for log writing operation
 */
class LogWriter(
    private val dbOpenHelper: DbOpenHelper,
    private val size: Int
) {
    fun log(log: LogEntity) {
        dbOpenHelper.executeTransaction { db ->
            // insert provided log
            db.compileStatement(LogTable.INSERT).use { stmt ->
                stmt.bindString(1, log.priority)
                stmt.bindString(2, log.tag)
                stmt.bindString(3, log.message)
                stmt.bindLong(4, log.timestamp)
                stmt.execute()
            }

            // delete if row count exceeds provided size
            db.compileStatement(LogTable.DELETE_OLDER).use { stmt ->
                stmt.bindLong(1, size.toLong())
                stmt.execute()
            }
        }
    }

    /**
     * Clear logs in SQLite.
     */
    fun delete() {
        dbOpenHelper.executeTransaction { db ->
            db.delete(LogTable.NAME, null, null)
        }
    }
}
