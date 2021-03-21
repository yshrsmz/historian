package net.yslibrary.historian;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.CheckResult;

import net.yslibrary.historian.internal.DbOpenHelper;
import net.yslibrary.historian.internal.LogEntity;
import net.yslibrary.historian.internal.LogWriter;
import net.yslibrary.historian.internal.LogWritingTask;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Historian
 */

public class Historian {

  static final String DB_NAME = "log.db";
  static final int SIZE = 500;
  static final int LOG_LEVEL = Log.INFO;

  private static final String TAG = "Historian";

  final DbOpenHelper dbOpenHelper;
  final LogWriter logWriter;
  final Context context;
  final File directory;
  final String dbName;
  final int size;
  final int logLevel;
  final boolean debug;
  final Callbacks callbacks;

  private final ExecutorService executorService;
  private boolean initialized = false;

  private Historian(Context context,
                    File directory,
                    String name,
                    int size,
                    int logLevel,
                    boolean debug,
                    Callbacks callbacks) {
    this.context = context;
    this.directory = directory;
    this.dbName = name;
    this.size = size;
    this.logLevel = logLevel;
    this.debug = debug;
    this.callbacks = (callbacks == null) ? new DefaultCallbacks(debug) : callbacks;

    createDirIfNeeded(directory);
    try {
      dbOpenHelper = new DbOpenHelper(context, directory.getCanonicalPath() + File.separator + name);
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }

    if (debug)
      Log.d(TAG, String.format(Locale.ENGLISH, "backing database file will be created at: %s", dbOpenHelper.getDatabaseName()));

    executorService = Executors.newSingleThreadExecutor();
    logWriter = new LogWriter(dbOpenHelper, size);
  }

  /**
   * Get Builder
   *
   * @param context Context
   * @return {@link Builder}
   */
  @CheckResult
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

  public void log(int priority, String tag, String message) {
    checkInitialized();

    if (priority < logLevel) return;
    if (message == null || message.length() == 0) return;

    executorService.execute(
        new LogWritingTask(
            callbacks,
            logWriter,
            LogEntity.create(priority, tag, message, System.currentTimeMillis())
        )
    );
  }

  /**
   * Terminate Historian
   * This method will perform;
   * - close underlying {@link net.yslibrary.historian.internal.DbOpenHelper}
   * <p>
   * After calling this method, all calls to this instance of {@link net.yslibrary.historian.Historian}
   * can produce exception or undefined behavior.
   */
  public void terminate() {
    checkInitialized();
    dbOpenHelper.close();
  }

  /**
   * delete cache
   */
  public void delete() {
    checkInitialized();
    logWriter.delete();
  }

  /**
   * Get absolute path of database file
   *
   * @return absolute path of database file
   */
  public String dbPath() {
    checkInitialized();
    try {
      return directory.getCanonicalPath() + File.separator + dbName;
    } catch (IOException e) {
      throw new HistorianFileException("Could not resolve the canonical path to the Historian DB file: " + directory.getAbsolutePath(), e);
    }
  }

  /**
   * Get database file name
   *
   * @return database file name
   */
  public String dbName() {
    return dbName;
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


  public interface Callbacks {
    void onSuccess();

    void onFailure(Throwable throwable);
  }

  /**
   * Builder class for {@link net.yslibrary.historian.Historian}
   */
  @SuppressWarnings("WeakerAccess")
  public static class Builder {

    private final Context context;
    private File directory;
    private String name = DB_NAME;
    private int size = SIZE;
    private int logLevel = LOG_LEVEL;
    private boolean debug = false;
    private Callbacks callbacks = null;

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
    @CheckResult
    public Builder directory(File directory) {
      this.directory = directory;
      return this;
    }

    /**
     * Specify a name of the Historian's Database file
     * <p>
     * Default is {@link Historian#DB_NAME}
     *
     * @param name file name of the backing SQLite database file
     * @return Builder
     */
    @CheckResult
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Specify the max row number of the SQLite database
     * <p>
     * Default is 500.
     *
     * @param size max row number
     * @return Builder
     */
    @CheckResult
    public Builder size(int size) {
      if (size < 0) throw new IllegalArgumentException("size should be 0 or greater");
      this.size = size;
      return this;
    }

    /**
     * Specify minimum log level to save. The value should be any one of
     * {@link android.util.Log#VERBOSE},
     * {@link android.util.Log#DEBUG},
     * {@link android.util.Log#INFO},
     * {@link android.util.Log#WARN},
     * {@link android.util.Log#ERROR} or
     * {@link android.util.Log#ASSERT}.
     * <p>
     * Default is {@link android.util.Log#INFO}
     *
     * @param logLevel log level
     * @return Builder
     */
    @CheckResult
    public Builder logLevel(int logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    /**
     * Enable/disable Historian's debug logs(not saved to SQLite).
     * <p>
     * Default is false.
     *
     * @param debug true: output logs. false: no debug logs
     * @return Builder
     */
    @CheckResult
    public Builder debug(boolean debug) {
      this.debug = debug;
      return this;
    }

    /**
     * Specify callbacks. This callbacks are called each time Historian save a log.
     * This callbacks are called on background thread.
     * <p>
     * Default is {@link Historian.DefaultCallbacks}
     *
     * @param callbacks callbacks to execute.
     * @return Builder
     */
    @CheckResult
    public Builder callbacks(Callbacks callbacks) {
      this.callbacks = callbacks;
      return this;
    }

    /**
     * Build Historian. You need to call this method to use {@link Historian}
     *
     * @return {@link Historian}
     */
    @CheckResult
    public Historian build() {
      return new Historian(context, directory, name, size, logLevel, debug, callbacks);
    }
  }

  static class DefaultCallbacks implements Callbacks {
    private final boolean debug;

    DefaultCallbacks(boolean debug) {
      this.debug = debug;
    }

    @Override
    public void onSuccess() {
      // no-op
    }

    @Override
    public void onFailure(Throwable throwable) {
      if (debug) Log.e(TAG, "Something happened while trying to save a log", throwable);
    }
  }
}
