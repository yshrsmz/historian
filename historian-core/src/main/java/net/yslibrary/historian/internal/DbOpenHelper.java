package net.yslibrary.historian.internal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yshrsmz on 17/01/20.
 */

public class DbOpenHelper extends SQLiteOpenHelper {

  public static final int DB_VERSION = 1;


  public DbOpenHelper(Context context, String name) {
    super(context, name, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(LogTable.CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}
