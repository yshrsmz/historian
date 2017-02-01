package net.yslibrary.historian.internal;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/**
 * Class for log writing operation
 */

public class LogWriter {

  private final DbOpenHelper dbOpenHelper;

  private final int size;

  public LogWriter(DbOpenHelper dbOpenHelper, int size) {
    this.dbOpenHelper = dbOpenHelper;
    this.size = size;
  }

  public void log(final LogEntity log) {
    dbOpenHelper.executeTransaction(new DbOpenHelper.Transaction() {
      @Override
      public void call(SQLiteDatabase db) {

        // insert provided log
        SQLiteStatement insertStatement = db.compileStatement(LogTable.INSERT);
        insertStatement.bindString(1, log.priority);
        insertStatement.bindString(2, log.message);
        insertStatement.bindLong(3, log.timestamp);
        insertStatement.execute();

        // delete if row count exceeds provided size
        SQLiteStatement deleteStatement = db.compileStatement(LogTable.DELETE_OLDER);
        deleteStatement.bindLong(1, (long) size);
        deleteStatement.execute();
      }
    });
  }

  /**
   * Clear logs in SQLite.
   */
  public void delete() {
    dbOpenHelper.executeTransaction(new DbOpenHelper.Transaction() {
      @Override
      public void call(SQLiteDatabase db) {
        db.delete(LogTable.NAME, null, new String[]{});
      }
    });
  }
}
