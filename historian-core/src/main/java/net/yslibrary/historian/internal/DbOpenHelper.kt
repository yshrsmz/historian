package net.yslibrary.historian.internal

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLiteOpenHelper for Historian
 */
class DbOpenHelper(
    context: Context,
    name: String
) : SQLiteOpenHelper(context, name, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(LogTable.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1) {
            db.execSQL(LogTable.DROP_TABLE)
            db.execSQL(LogTable.CREATE_TABLE)
        }
    }

    inline fun executeTransaction(block: (SQLiteDatabase) -> Unit) {
        writableDatabase.let { db ->
            db.beginTransaction()
            try {
                block(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    companion object {
        private const val DB_VERSION = 2
    }
}
