package net.yslibrary.historian

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.annotation.CheckResult
import net.yslibrary.historian.internal.DbOpenHelper
import net.yslibrary.historian.internal.LogEntity
import net.yslibrary.historian.internal.LogWriter
import net.yslibrary.historian.internal.LogWritingTask
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Historian
 */
class Historian private constructor(
    val context: Context,
    val directory: File,
    val dbName: String,
    val size: Int,
    val logLevel: Int,
    val debug: Boolean,
    private val onSuccess: OnSuccessCallback?,
    private val onFailure: OnFailureCallback?,
    private val dbOpenHelper: DbOpenHelper,
    private val logWriter: LogWriter,
    private val executorService: ExecutorService
) {
    @Volatile
    private var initialized = false

    /**
     * initialize
     */
    fun initialize() {
        if (initialized) return
        dbOpenHelper.writableDatabase
        initialized = true
    }

    fun log(priority: Int, tag: String?, message: String?) {
        checkInitialized()
        if (priority < logLevel) return
        if (message.isNullOrEmpty()) return

        executorService.execute(
            LogWritingTask(
                onSuccess,
                onFailure,
                logWriter,
                LogEntity.create(priority, tag, message, System.currentTimeMillis())
            )
        )
    }

    /**
     * Terminate Historian immediately.
     * This method will perform;
     * - shutdown the background executor (pending writes may be lost)
     * - close underlying [DbOpenHelper]
     *
     * After calling this method, all calls to this instance of [Historian]
     * can produce exception or undefined behavior.
     *
     * @see terminateSafe for graceful shutdown that waits for pending writes
     */
    @Deprecated(
        message = "Use terminateSafe() for graceful shutdown that waits for pending writes",
        replaceWith = ReplaceWith("terminateSafe()")
    )
    fun terminate() {
        checkInitialized()
        executorService.shutdown()
        dbOpenHelper.close()
    }

    /**
     * Terminate Historian gracefully.
     * This method will block until pending log writes complete or timeout elapses.
     * - shutdown the background executor
     * - wait for pending log writes to complete (up to [timeoutSeconds])
     * - close underlying [DbOpenHelper]
     *
     * After calling this method, all calls to this instance of [Historian]
     * can produce exception or undefined behavior.
     *
     * Note: This method blocks the calling thread. Call from a background thread
     * if blocking is not acceptable. If timeout is reached before pending writes
     * complete, the executor is force-terminated via shutdownNow() and remaining
     * tasks are cancelled before the database is closed.
     *
     * @param timeoutSeconds maximum time to wait for pending writes (default: 5 seconds)
     * @return true if all pending writes completed, false if timeout elapsed
     */
    @JvmOverloads
    fun terminateSafe(timeoutSeconds: Long = 5): Boolean {
        checkInitialized()
        executorService.shutdown()
        val completed = try {
            executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
        if (!completed) {
            executorService.shutdownNow()
        }
        dbOpenHelper.close()
        return completed
    }

    /**
     * delete cache
     */
    fun delete() {
        checkInitialized()
        logWriter.delete()
    }

    /**
     * Get absolute path of database file
     *
     * @return absolute path of database file
     */
    fun dbPath(): String {
        checkInitialized()
        return try {
            "${directory.canonicalPath}${File.separator}$dbName"
        } catch (e: IOException) {
            throw HistorianFileException(
                "Could not resolve the canonical path to the Historian DB file: ${directory.absolutePath}",
                e
            )
        }
    }

    /**
     * Get database file name
     *
     * @return database file name
     */
    fun dbName(): String = dbName

    internal fun getDatabase(): SQLiteDatabase {
        checkInitialized()
        return dbOpenHelper.readableDatabase
    }

    /**
     * throw if [Historian.initialize] is not called.
     */
    private fun checkInitialized() {
        check(initialized) { "Historian#initialize is not called" }
    }

    /**
     * Callback for log write success.
     * Called on a background thread.
     */
    fun interface OnSuccessCallback {
        /**
         * Called when a log is successfully written to the database.
         */
        fun onSuccess()
    }

    /**
     * Callback for log write failure.
     * Called on a background thread.
     */
    fun interface OnFailureCallback {
        /**
         * Called when writing a log fails.
         * @param throwable the exception that caused the failure
         */
        fun onFailure(throwable: Throwable)
    }

    /**
     * Combined callbacks interface for log writing operations.
     * Both methods are called on a background thread.
     *
     * Consider using [OnSuccessCallback] and [OnFailureCallback] separately
     * if you only need one of the callbacks.
     */
    interface Callbacks : OnSuccessCallback, OnFailureCallback

    /**
     * DSL Builder for Kotlin
     */
    @HistorianDsl
    class DslBuilder internal constructor(private val context: Context) {
        var directory: File = context.filesDir
        var name: String = DB_NAME
        var size: Int = SIZE
            set(value) {
                require(value >= 0) { "size should be 0 or greater" }
                field = value
            }
        var logLevel: Int = LOG_LEVEL
        var debug: Boolean = false

        /**
         * Callback invoked when a log is successfully written to the database.
         * Called on a background thread. Optional - defaults to null (no callback).
         */
        var onSuccess: OnSuccessCallback? = null

        /**
         * Callback invoked when a log write fails.
         * Called on a background thread. Optional - if null and [debug] is true,
         * errors will be logged via [android.util.Log.e].
         */
        var onFailure: OnFailureCallback? = null

        @CheckResult
        internal fun build(): Historian {
            directory.mkdirs()

            val dbPath = try {
                "${directory.canonicalPath}${File.separator}$name"
            } catch (e: IOException) {
                throw HistorianFileException(
                    "Could not resolve the canonical path to the Historian DB file: ${directory.absolutePath}",
                    e
                )
            }

            val appContext = context.applicationContext
            val dbOpenHelper = DbOpenHelper(appContext, dbPath)

            if (debug) {
                Log.d(TAG, "backing database file will be created at: ${dbOpenHelper.databaseName}")
            }

            // Wrap onFailure to add debug logging if no failure callback provided
            val effectiveOnFailure = onFailure ?: if (debug) {
                OnFailureCallback { throwable ->
                    Log.e(TAG, "Failed to write log", throwable)
                }
            } else {
                null
            }

            return Historian(
                context = appContext,
                directory = directory,
                dbName = name,
                size = size,
                logLevel = logLevel,
                debug = debug,
                onSuccess = onSuccess,
                onFailure = effectiveOnFailure,
                dbOpenHelper = dbOpenHelper,
                logWriter = LogWriter(dbOpenHelper, size),
                executorService = Executors.newSingleThreadExecutor()
            )
        }
    }

    /**
     * Fluent Builder (Java-compatible)
     */
    @Deprecated(
        message = "Use Historian.invoke() DSL instead",
        replaceWith = ReplaceWith("Historian(context) { /* configure here */ }")
    )
    class Builder internal constructor(private val context: Context) {
        private val dslBuilder = DslBuilder(context)

        /**
         * Specify a directory where Historian's Database file is stored.
         *
         * @param directory directory to save SQLite database file.
         * @return Builder
         * @throws IllegalArgumentException if directory is null
         */
        @CheckResult
        fun directory(directory: File?): Builder = apply {
            requireNotNull(directory) { "directory must not be null" }
            dslBuilder.directory = directory
        }

        /**
         * Specify a name of the Historian's Database file
         *
         * Default is [Historian.DB_NAME]
         *
         * @param name file name of the backing SQLite database file
         * @return Builder
         * @throws IllegalArgumentException if name is null
         */
        @CheckResult
        fun name(name: String?): Builder = apply {
            requireNotNull(name) { "name must not be null" }
            dslBuilder.name = name
        }

        /**
         * Specify the max row number of the SQLite database
         *
         * Default is 500.
         *
         * @param size max row number
         * @return Builder
         */
        @CheckResult
        fun size(size: Int): Builder = apply { dslBuilder.size = size }

        /**
         * Specify minimum log level to save. The value should be any one of
         * [android.util.Log.VERBOSE],
         * [android.util.Log.DEBUG],
         * [android.util.Log.INFO],
         * [android.util.Log.WARN],
         * [android.util.Log.ERROR] or
         * [android.util.Log.ASSERT].
         *
         * Default is [android.util.Log.INFO]
         *
         * @param logLevel log level
         * @return Builder
         */
        @CheckResult
        fun logLevel(logLevel: Int): Builder = apply { dslBuilder.logLevel = logLevel }

        /**
         * Enable/disable Historian's debug logs(not saved to SQLite).
         *
         * Default is false.
         *
         * @param debug true: output logs. false: no debug logs
         * @return Builder
         */
        @CheckResult
        fun debug(debug: Boolean): Builder = apply { dslBuilder.debug = debug }

        /**
         * Specify callbacks. This callbacks are called each time Historian save a log.
         * This callbacks are called on background thread.
         *
         * @param callbacks callbacks to execute.
         * @return Builder
         * @throws IllegalArgumentException if callbacks is null
         */
        @CheckResult
        fun callbacks(callbacks: Callbacks?): Builder = apply {
            requireNotNull(callbacks) { "callbacks must not be null" }
            dslBuilder.onSuccess = callbacks
            dslBuilder.onFailure = callbacks
        }

        /**
         * Build Historian. You need to call this method to use [Historian]
         *
         * @return [Historian]
         */
        @CheckResult
        fun build(): Historian = dslBuilder.build()
    }

    companion object {
        const val DB_NAME = "log.db"
        const val SIZE = 500

        @JvmField
        val LOG_LEVEL = Log.INFO

        private const val TAG = "Historian"

        /**
         * Kotlin DSL entry point
         */
        operator fun invoke(context: Context, block: DslBuilder.() -> Unit = {}): Historian =
            DslBuilder(context.applicationContext).apply(block).build()

        /**
         * Get Builder
         *
         * @param context Context
         * @return [Builder]
         */
        @JvmStatic
        @CheckResult
        @Deprecated(
            message = "Use Historian.invoke() DSL instead",
            replaceWith = ReplaceWith("Historian(context) { /* configure here */ }")
        )
        fun builder(context: Context): Builder = Builder(context)
    }
}

@DslMarker
annotation class HistorianDsl
