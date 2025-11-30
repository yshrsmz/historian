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

/**
 * Historian
 */
class Historian private constructor(
    @JvmField val context: Context,
    @JvmField val directory: File,
    @JvmField val dbName: String,
    @JvmField val size: Int,
    @JvmField val logLevel: Int,
    @JvmField val debug: Boolean,
    @JvmField val callbacks: Callbacks,
    @JvmField internal val dbOpenHelper: DbOpenHelper,
    @JvmField internal val logWriter: LogWriter,
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
                callbacks,
                logWriter,
                LogEntity.create(priority, tag, message, System.currentTimeMillis())
            )
        )
    }

    /**
     * Terminate Historian
     * This method will perform;
     * - shutdown the background executor
     * - close underlying [DbOpenHelper]
     *
     * After calling this method, all calls to this instance of [Historian]
     * can produce exception or undefined behavior.
     */
    fun terminate() {
        checkInitialized()
        executorService.shutdown()
        dbOpenHelper.close()
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
     * Callbacks interface for log writing operations.
     * Both methods are called on a background thread.
     */
    interface Callbacks {
        /**
         * Called when a log is successfully written to the database.
         */
        fun onSuccess()

        /**
         * Called when writing a log fails.
         * @param throwable the exception that caused the failure
         */
        fun onFailure(throwable: Throwable)
    }

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
        var callbacks: Callbacks? = null

        @CheckResult
        fun build(): Historian {
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

            return Historian(
                context = appContext,
                directory = directory,
                dbName = name,
                size = size,
                logLevel = logLevel,
                debug = debug,
                callbacks = callbacks ?: DefaultCallbacks(debug),
                dbOpenHelper = dbOpenHelper,
                logWriter = LogWriter(dbOpenHelper, size),
                executorService = Executors.newSingleThreadExecutor()
            )
        }
    }

    /**
     * Fluent Builder (Java-compatible)
     */
    class Builder internal constructor(private val context: Context) {
        private val dslBuilder = DslBuilder(context)

        /**
         * Specify a directory where Historian's Database file is stored.
         *
         * @param directory directory to save SQLite database file.
         * @return Builder
         */
        @CheckResult
        fun directory(directory: File): Builder = apply { dslBuilder.directory = directory }

        /**
         * Specify a name of the Historian's Database file
         *
         * Default is [Historian.DB_NAME]
         *
         * @param name file name of the backing SQLite database file
         * @return Builder
         */
        @CheckResult
        fun name(name: String): Builder = apply { dslBuilder.name = name }

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
         * Default is [Historian.DefaultCallbacks]
         *
         * @param callbacks callbacks to execute.
         * @return Builder
         */
        @CheckResult
        fun callbacks(callbacks: Callbacks): Builder = apply { dslBuilder.callbacks = callbacks }

        /**
         * Build Historian. You need to call this method to use [Historian]
         *
         * @return [Historian]
         */
        @CheckResult
        fun build(): Historian = dslBuilder.build()
    }

    internal class DefaultCallbacks(private val debug: Boolean) : Callbacks {
        override fun onSuccess() {
            // no-op
        }

        override fun onFailure(throwable: Throwable) {
            if (debug) {
                Log.e(TAG, "Failed to write log", throwable)
            }
        }
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
            DslBuilder(context).apply(block).build()

        /**
         * Get Builder
         *
         * @param context Context
         * @return [Builder]
         */
        @JvmStatic
        @CheckResult
        fun builder(context: Context): Builder = Builder(context)
    }
}

@DslMarker
annotation class HistorianDsl
