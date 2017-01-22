package net.yslibrary.historian;

import android.database.Cursor;

/**
 * Created by yshrsmz on 2017/01/23.
 */

public class Cursors {

  private Cursors() {
    // no-op
  }

  public static String getString(Cursor cursor, String column) {
    return cursor.getString(cursor.getColumnIndex(column));
  }
}
