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
            db.compileStatement(LogTable.INSERT).apply {
                bindString(1, log.priority)
                bindString(2, log.tag)
                bindString(3, log.message)
                bindLong(4, log.timestamp)
                execute()
            }

            // delete if row count exceeds provided size
            db.compileStatement(LogTable.DELETE_OLDER).apply {
                bindLong(1, size.toLong())
                execute()
            }
        }
    }

    /**
     * Clear logs in SQLite.
     */
    fun delete() {
        dbOpenHelper.executeTransaction { db ->
            db.delete(LogTable.NAME, null, emptyArray())
        }
    }
}
