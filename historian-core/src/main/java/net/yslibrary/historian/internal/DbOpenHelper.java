package net.yslibrary.historian.internal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper for Historian
 */

public class DbOpenHelper extends SQLiteOpenHelper {

  private static final int DB_VERSION = 1;

  public DbOpenHelper(Context context, String name) {
    super(context, name, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(LogTable.CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // no-op
  }

  void executeTransaction(Transaction transaction) {
    SQLiteDatabase db = null;
    try {
      db = getWritableDatabase();
      db.beginTransaction();

      transaction.call(db);

      db.setTransactionSuccessful();
    } finally {
      if (db != null) db.endTransaction();
    }
  }

  interface Transaction {
    void call(SQLiteDatabase db);
  }
}
