package net.yslibrary.historian;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.yslibrary.historian.internal.DbOpenHelper;
import net.yslibrary.historian.internal.LogQueue;
import net.yslibrary.historian.internal.LogWriter;
import net.yslibrary.historian.internal.LogWritingTask;

import java.io.File;
import java.io.IOException;

/**
 * Created by yshrsmz on 17/01/20.
 */

public class Historian {

  static final String DB_NAME = "log.db";
  static final int SIZE = 500;
  static final int QUEUE_SIZE = 10;
  static final int LOG_LEVEL = Log.INFO;

  final DbOpenHelper dbOpenHelper;
  final LogWriter logWriter;
  final LogQueue queue;

  final Context context;
  final File directory;
  final String dbName;
  final int size;
  final int queueSize;
  final int logLevel;

  boolean initialized = false;

  private Historian(Context context,
                    File directory,
                    String name, int size, int queueSize, int logLevel) {
    this.context = context;
    this.directory = directory;
    this.dbName = name;
    this.size = size;
    this.queueSize = queueSize;
    this.logLevel = logLevel;

    checkAndCreateDir(directory);
    try {
      dbOpenHelper = new DbOpenHelper(context, directory.getCanonicalPath() + File.separator + name);
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }

    logWriter = new LogWriter(dbOpenHelper, size);
    queue = new LogQueue(queueSize);
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

    dbOpenHelper.getWritableDatabase();

    initialized = true;
  }

  public void log(int priority, String message) {
    checkInitialized();

    if (priority < logLevel) return;

    queue.queue(LogEntity.create(priority, message, System.currentTimeMillis()));

    if (!queue.isExceeded()) return;

    new LogWritingTask(logWriter).execute(queue);
  }

  public void flush() {
    checkInitialized();
    logWriter.log(queue);
  }

  public void terminate() {
    checkInitialized();
    logWriter.log(queue);
  }

  public void delete() {
    checkInitialized();
    logWriter.delete();
  }

  public String dbPath() {
    checkInitialized();
    try {
      return directory.getCanonicalPath() + File.separator + dbName;
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }
  }

  SQLiteDatabase getDatabase() {
    checkInitialized();
    return dbOpenHelper.getReadableDatabase();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void checkAndCreateDir(File file) {
    if (!file.exists()) file.mkdir();
  }

  /**
   * throw if {@link Historian#initialize()} is not called.
   */
  private void checkInitialized() {
    if (!initialized) throw new IllegalStateException("Historian#initialize is not called");
  }


  /**
   * Build class for {@link net.yslibrary.historian.Historian}
   */
  public static class Builder {

    private final Context context;
    private File directory;
    private String name = DB_NAME;
    private int size = SIZE;
    private int queueSize = QUEUE_SIZE;
    private int logLevel = LOG_LEVEL;

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
      if (queueSize < 0)
        throw new IllegalArgumentException("queueSize should be 0 or greater");
      this.queueSize = queueSize;
      return this;
    }

    public Builder logLevel(int logLevel) {
      this.logLevel = logLevel;
      return this;
    }


    public Historian build() {
      return new Historian(context, directory, name, size, queueSize, logLevel);
    }
  }
}
