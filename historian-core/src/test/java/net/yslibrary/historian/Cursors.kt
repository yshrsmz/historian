package net.yslibrary.historian

import android.database.Cursor

/**
 * Utility methods for [Cursor]
 */
object Cursors {
    @JvmStatic
    fun getString(cursor: Cursor, column: String): String =
        cursor.getString(cursor.getColumnIndexOrThrow(column))
}
