package net.yslibrary.historian;

import android.database.Cursor;

/**
 * Utility methods for {@link android.database.Cursor}
 */

public class Cursors {

  private Cursors() {
    // no-op
  }

  public static String getString(Cursor cursor, String column) {
    return cursor.getString(cursor.getColumnIndex(column));
  }
}
