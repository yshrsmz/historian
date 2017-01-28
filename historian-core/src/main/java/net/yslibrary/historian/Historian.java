package net.yslibrary.historian;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.CheckResult;
import android.util.Log;

import net.yslibrary.historian.internal.DbOpenHelper;
import net.yslibrary.historian.internal.LogQueue;
import net.yslibrary.historian.internal.LogWriter;
import net.yslibrary.historian.internal.LogWritingTask;
import net.yslibrary.historian.internal.MainThreadExecutor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yshrsmz on 17/01/20.
 */

public class Historian {

  static final String DB_NAME = "log.db";
  static final int SIZE = 500;
  static final int QUEUE_SIZE = 10;
  static final int LOG_LEVEL = Log.INFO;

  final Executor callbackExecutor;
  final ExecutorService executorService;
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

    createDirIfNeeded(directory);
    try {
      dbOpenHelper = new DbOpenHelper(context, directory.getCanonicalPath() + File.separator + name);
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }

    callbackExecutor = new MainThreadExecutor();
    executorService = Executors.newSingleThreadExecutor();
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

    executorService.execute(new LogWritingTask(callbackExecutor, logWriter, queue));
  }

  /**
   * Save cached logs to SQLite.
   * This operation is blocking.
   */
  public void flush() {
    checkInitialized();
    logWriter.log(queue);
  }

  /**
   * Terminate Historian
   * This method should only be called from {@link android.app.Application#onTerminate()}.
   * This method will perform;
   * - write all cached {@link net.yslibrary.historian.LogEntity} to SQLite(this is blocking operation)
   * - close underlying {@link net.yslibrary.historian.internal.DbOpenHelper}
   * <p>
   * After calling this method, all calls to this instance of {@link net.yslibrary.historian.Historian}
   * can produce exception or undefined behavior.
   */
  public void terminate() {
    checkInitialized();
    logWriter.log(queue);
    dbOpenHelper.close();
  }

  /**
   * delete cache
   */
  public void delete() {
    checkInitialized();
    logWriter.delete(queue);
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
  private void createDirIfNeeded(File file) {
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
  @SuppressWarnings("WeakerAccess")
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
     * @param directory directory to save SQLite database file.
     * @return Builder
     */
    public Builder directory(File directory) {
      this.directory = directory;
      return this;
    }

    /**
     * Specify a name of the Historian's Database file
     *
     * @param name file name of the backing SQLite database file
     * @return Builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Specify the max row number of the SQLite database
     *
     * @param size max row number
     * @return Builder
     */
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

    /**
     * Specify minimum log level to save. The value should be any one of {@link android.util.Log#VERBOSE},
     * {@link android.util.Log#DEBUG}, {@link android.util.Log#INFO}, {@link android.util.Log#WARN},
     * {@link android.util.Log#ERROR} or {@link android.util.Log#ASSERT}.
     *
     * @param logLevel log level
     * @return Builder
     */
    public Builder logLevel(int logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    /**
     * Build Historian. You need to call this method to use {@link Historian}
     *
     * @return {@link Historian}
     */
    @CheckResult
    public Historian build() {
      return new Historian(context, directory, name, size, queueSize, logLevel);
    }
  }
}
