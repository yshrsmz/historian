package net.yslibrary.historian;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.yslibrary.historian.internal.DbOpenHelper;
import net.yslibrary.historian.internal.LogTable;

import java.io.File;
import java.io.IOException;

/**
 * Created by yshrsmz on 17/01/20.
 */

public class Historian {

  private static final String DB_NAME = "log.db";
  private static final int SIZE = 500;
  private static final int QUEUE_SIZE = 10;

  private final Context context;
  private final DbOpenHelper dbOpenHelper;
  private final File directory;
  private final String dbName;

  private boolean initialized = false;

  private Historian(Context context,
                    File directory,
                    String name) {
    this.context = context;
    this.directory = directory;
    this.dbName = name;

    checkAndCreateDir(directory);
    try {
      dbOpenHelper = new DbOpenHelper(context, directory.getCanonicalPath() + File.pathSeparator + name);
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }
  }

  /**
   * Get Builder
   *
   * @param context Context
   * @return
   */
  public static Builder builder(Context context) {
    return new Builder(context);
  }

  /**
   * initialize
   */
  public void initialize() {
    if (initialized) return;

    SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
    db.execSQL(LogTable.INSERT, new Object[]{"DEBUG", "TEST", System.currentTimeMillis()});
    initialized = true;
  }

  public void flush() {

  }

  public void terminate() {

  }

  public void delete() {

  }

  public String dbPath() {
    try {
      return directory.getCanonicalPath() + File.separator + dbName;
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }
  }

  public SQLiteDatabase getDatabase() {
    return dbOpenHelper.getReadableDatabase();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void checkAndCreateDir(File file) {
    if (!file.exists()) file.mkdir();
  }

  public static class Builder {

    private final Context context;
    private File directory;
    private String name = DB_NAME;
    private int size = SIZE;
    private int queueSize = QUEUE_SIZE;

    Builder(Context context) {
      this.context = context.getApplicationContext();
      directory = context.getFilesDir();
    }

    /**
     * Specify a directory where Historian's Database file is stored.
     *
     * @param directory
     * @return
     */
    public Builder directory(File directory) {
      this.directory = directory;
      return this;
    }

    /**
     * Specify a name of the Historian's Database file
     *
     * @param name
     * @return
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder size(int size) {
      if (size < 0) throw new IllegalArgumentException("size should be 0 or greater");
      this.size = size;
      return this;
    }

    public Builder queueSize(int queueSize) {
      if (queueSize < 0) throw new IllegalArgumentException("queueSize should be 0 or greater");
      this.queueSize = queueSize;
      return this;
    }


    public Historian build() {
      return new Historian(context, directory, name);
    }
  }
}
