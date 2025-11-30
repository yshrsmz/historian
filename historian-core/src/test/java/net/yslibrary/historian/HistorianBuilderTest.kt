package net.yslibrary.historian

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HistorianBuilderTest {

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `build with defaults`() {
        val historian = Historian.builder(context).build()

        assertNotNull(historian.context)
        assertNotNull(historian.dbOpenHelper)
        assertNotNull(historian.logWriter)

        assertEquals(Historian.LOG_LEVEL, historian.logLevel)
        assertEquals(context.filesDir, historian.directory)
        assertEquals(Historian.DB_NAME, historian.dbName)
        assertEquals(Historian.SIZE, historian.size)
        assertFalse(historian.debug)
    }

    @Test
    fun `build with custom params`() {
        val historian = Historian.builder(context)
            .name("test.db")
            .directory(context.getExternalFilesDir(null)!!)
            .logLevel(Log.DEBUG)
            .size(1000)
            .debug(true)
            .callbacks(TestCallbacks())
            .build()

        assertNotNull(historian.context)
        assertNotNull(historian.dbOpenHelper)
        assertNotNull(historian.logWriter)

        assertEquals(Log.DEBUG, historian.logLevel)
        assertEquals(context.getExternalFilesDir(null), historian.directory)
        assertEquals("test.db", historian.dbName)
        assertEquals(1000, historian.size)
        assertTrue(historian.debug)
    }

    @Test
    fun `build with Kotlin DSL`() {
        val historian = Historian(context) {
            name = "kotlin.db"
            size = 2000
            logLevel = Log.WARN
            debug = true
        }

        assertNotNull(historian.context)
        assertEquals("kotlin.db", historian.dbName)
        assertEquals(2000, historian.size)
        assertEquals(Log.WARN, historian.logLevel)
        assertTrue(historian.debug)
    }

    class TestCallbacks : Historian.Callbacks {
        override fun onSuccess() {}
        override fun onFailure(throwable: Throwable) {}
    }
}
