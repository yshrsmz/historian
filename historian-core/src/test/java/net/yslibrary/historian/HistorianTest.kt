package net.yslibrary.historian

import android.database.Cursor
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class HistorianTest {

    companion object {
        const val TAG = "test_tag"
        const val TIMEOUT_SECONDS = 5L
    }

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Suppress("DEPRECATION")
    @Test(expected = IllegalStateException::class)
    fun `initialize not called`() {
        val historian = Historian.builder(context).build()
        try {
            historian.log(Log.DEBUG, TAG, "this is debug1")
        } finally {
            historian.terminate()
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `log queue under logLevel`() {
        val historian = Historian.builder(context).build()
        historian.initialize()

        try {
            historian.log(Log.VERBOSE, TAG, "this is verbose")
            historian.log(Log.DEBUG, TAG, "this is debug1")
            historian.log(Log.DEBUG, TAG, "this is debug2")

            getAllLogs(historian).use { cursor ->
                assertEquals(0, cursor.count)
            }
        } finally {
            historian.terminate()
        }
    }

    @Test
    fun `log queue over logLevel`() {
        val expectedLogs = 4
        val latch = CountDownLatch(expectedLogs)

        val historian = Historian(context) {
            onSuccess = Historian.OnSuccessCallback { latch.countDown() }
        }
        historian.initialize()

        try {
            historian.log(Log.INFO, TAG, "this is info1")
            historian.log(Log.DEBUG, TAG, "this is debug1")  // Below logLevel, not counted
            historian.log(Log.INFO, TAG, "this is info2")
            historian.log(Log.WARN, TAG, "this is warn1")
            historian.log(Log.ERROR, TAG, "this is error1")

            assertTrue("Timed out waiting for logs", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))

            getAllLogs(historian).use { cursor ->
                assertEquals(4, cursor.count)

                cursor.moveToFirst()
                assertEquals("INFO", Cursors.getString(cursor, "priority"))
                assertEquals(TAG, Cursors.getString(cursor, "tag"))
                assertEquals("this is info1", Cursors.getString(cursor, "message"))

                cursor.moveToNext()
                assertEquals("INFO", Cursors.getString(cursor, "priority"))
                assertEquals(TAG, Cursors.getString(cursor, "tag"))
                assertEquals("this is info2", Cursors.getString(cursor, "message"))

                cursor.moveToNext()
                assertEquals("WARN", Cursors.getString(cursor, "priority"))
                assertEquals(TAG, Cursors.getString(cursor, "tag"))
                assertEquals("this is warn1", Cursors.getString(cursor, "message"))

                cursor.moveToNext()
                assertEquals("ERROR", Cursors.getString(cursor, "priority"))
                assertEquals(TAG, Cursors.getString(cursor, "tag"))
                assertEquals("this is error1", Cursors.getString(cursor, "message"))
            }
        } finally {
            historian.terminate()
        }
    }

    @Test
    @Config(sdk = [23, 28, 33, 35])
    fun `log background`() {
        val expectedLogs = 10
        val latch = CountDownLatch(expectedLogs)

        val historian = Historian(context) {
            onSuccess = Historian.OnSuccessCallback { latch.countDown() }
        }
        historian.initialize()

        try {
            val es = Executors.newSingleThreadExecutor()
            val future = es.submit {
                for (i in 0 until expectedLogs) {
                    historian.log(Log.INFO, TAG, "this log is from background thread - $i")
                }
            }

            future.get()
            assertTrue("Timed out waiting for logs", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))

            getAllLogs(historian).use { cursor ->
                assertEquals(expectedLogs, cursor.count)
            }
        } finally {
            historian.terminate()
        }
    }

    @Test
    fun `multiple write in multiple threads`() {
        val nThreads = 10
        val latch = CountDownLatch(nThreads)

        val historian = Historian(context) {
            onSuccess = Historian.OnSuccessCallback { latch.countDown() }
        }
        historian.initialize()

        try {
            val threads = List(nThreads) {
                Thread {
                    try {
                        Thread.sleep(Random.nextLong(0, 200))
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    historian.log(Log.INFO, TAG, "this is test: ${System.currentTimeMillis()}")
                }.apply { start() }
            }

            // Wait for all threads to complete
            threads.forEach { it.join() }

            assertTrue("Timed out waiting for logs", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))

            getAllLogs(historian).use { cursor ->
                assertEquals(nThreads, cursor.count)
            }
        } finally {
            historian.terminate()
        }
    }

    @Test
    fun `null tag`() {
        val latch = CountDownLatch(1)

        val historian = Historian(context) {
            onSuccess = Historian.OnSuccessCallback { latch.countDown() }
        }
        historian.initialize()

        try {
            historian.log(Log.INFO, null, "this tag should be null")

            assertTrue("Timed out waiting for log", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))

            getAllLogs(historian).use { cursor ->
                cursor.moveToFirst()
                assertEquals("", Cursors.getString(cursor, "tag"))
            }
        } finally {
            historian.terminate()
        }
    }

    @Test
    fun `onSuccess callback is invoked for each successful write`() {
        val successCount = AtomicInteger(0)
        val expectedLogs = 5
        val latch = CountDownLatch(expectedLogs)

        val historian = Historian(context) {
            onSuccess = Historian.OnSuccessCallback {
                successCount.incrementAndGet()
                latch.countDown()
            }
        }
        historian.initialize()

        try {
            repeat(expectedLogs) { i ->
                historian.log(Log.INFO, TAG, "message $i")
            }

            assertTrue("Timed out waiting for callbacks", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(expectedLogs, successCount.get())
        } finally {
            historian.terminate()
        }
    }

    @Test
    fun `separate onSuccess and onFailure callbacks work independently`() {
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        val latch = CountDownLatch(3)

        val historian = Historian(context) {
            onSuccess = Historian.OnSuccessCallback {
                successCount.incrementAndGet()
                latch.countDown()
            }
            onFailure = Historian.OnFailureCallback {
                failureCount.incrementAndGet()
                latch.countDown()
            }
        }
        historian.initialize()

        try {
            // Log 3 successful messages
            repeat(3) { i ->
                historian.log(Log.INFO, TAG, "message $i")
            }

            assertTrue("Timed out waiting for callbacks", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(3, successCount.get())
            assertEquals(0, failureCount.get())
        } finally {
            historian.terminate()
        }
    }

    private fun getAllLogs(historian: Historian): Cursor {
        val db = historian.getDatabase()
        return db.query(
            "log",
            arrayOf("id", "tag", "priority", "message", "created_at"),
            null,
            null,
            null,
            null,
            "created_at ASC"
        )
    }
}
