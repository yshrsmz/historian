package net.yslibrary.historian.internal;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import net.yslibrary.historian.LogEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yshrsmz on 2017/01/20.
 */

public class LogWriter {

  private final DbOpenHelper dbOpenHelper;

  private final int size;

  public LogWriter(DbOpenHelper dbOpenHelper, int size) {
    this.dbOpenHelper = dbOpenHelper;
    this.size = size;
  }

  public List<LogEntity> log(final LogQueue queue) {
    final List<LogEntity> saved = new ArrayList<>();
    dbOpenHelper.executeTransaction(new DbOpenHelper.Transaction() {
      @Override
      public void call(SQLiteDatabase db) {

        // insert provided logs
        for (int i = 0, l = queue.size(); i < l; i++) {
          LogEntity log = queue.get(i);

          SQLiteStatement insertStatement = db.compileStatement(LogTable.INSERT);
          insertStatement.bindString(1, log.priority);
          insertStatement.bindString(2, log.message);
          insertStatement.bindLong(3, log.timestamp);
          insertStatement.execute();

          saved.add(log);
        }

        // delete if row count exceeds provided size
        SQLiteStatement deleteStatement = db.compileStatement(LogTable.DELETE_OLDER);
        deleteStatement.bindLong(1, (long) size);
        deleteStatement.execute();
      }
    });
    return saved;
  }

  public void delete() {
    dbOpenHelper.executeTransaction(new DbOpenHelper.Transaction() {
      @Override
      public void call(SQLiteDatabase db) {
        db.delete(LogTable.NAME, null, new String[]{});
      }
    });
  }
}
