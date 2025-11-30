package net.yslibrary.historian

import android.database.Cursor
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
class HistorianTest {

    companion object {
        const val TAG = "test_tag"
    }

    private lateinit var historian: Historian

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        historian = Historian.builder(context).build()
    }

    @Test(expected = IllegalStateException::class)
    fun `initialize not called`() {
        historian.log(Log.DEBUG, TAG, "this is debug1")
    }

    @Test
    fun `log queue under logLevel`() {
        historian.initialize()

        historian.log(Log.VERBOSE, TAG, "this is verbose")
        historian.log(Log.DEBUG, TAG, "this is debug1")
        historian.log(Log.DEBUG, TAG, "this is debug2")

        val result = getAllLogs(historian)
        assertEquals(0, result.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun `log queue over logLevel`() {
        historian.initialize()

        historian.log(Log.INFO, TAG, "this is info1")
        historian.log(Log.DEBUG, TAG, "this is debug1")
        historian.log(Log.INFO, TAG, "this is info2")
        historian.log(Log.WARN, TAG, "this is warn1")
        historian.log(Log.ERROR, TAG, "this is error1")

        Thread.sleep(500)

        val cursor = getAllLogs(historian)

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

        cursor.close()
    }

    @Test
    @Config(sdk = [23, 28, 33, 35])
    @Throws(Exception::class)
    fun `log background`() {
        historian.initialize()

        val es = Executors.newSingleThreadExecutor()
        val future = es.submit {
            for (i in 0 until 10) {
                historian.log(Log.INFO, TAG, "this log is from background thread - $i")
            }
        }

        future.get()

        Thread.sleep(200)

        val cursor = getAllLogs(historian)
        assertEquals(10, cursor.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun `multiple write in multiple threads`() {
        val nThreads = 10
        historian.initialize()

        for (i in 0 until nThreads) {
            val writer = Runnable {
                try {
                    Thread.sleep((Math.random() * 200.0).toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                historian.log(Log.INFO, TAG, "this is test: ${System.currentTimeMillis()}")
            }

            val thread = Thread(writer)
            thread.run()
        }

        Thread.sleep(1000)

        val cursor = getAllLogs(historian)
        assertEquals(10, cursor.count)
    }

    @Test
    @Throws(InterruptedException::class)
    fun `null tag`() {
        historian.initialize()

        historian.log(Log.INFO, null, "this tag should be null")

        Thread.sleep(1000)

        val cursor = getAllLogs(historian)

        cursor.moveToFirst()
        assertEquals("", Cursors.getString(cursor, "tag"))
    }

    private fun getAllLogs(historian: Historian): Cursor {
        val db = historian.dbOpenHelper.readableDatabase
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
