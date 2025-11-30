package net.yslibrary.historian.sample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.yslibrary.historian.Historian
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

class MainActivity : AppCompatActivity() {

    private val counter = AtomicLong()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            repeat(100) {
                Timber.i("test: %d", counter.getAndIncrement())
            }
        }

        val exportButton = findViewById<android.view.View>(R.id.export)
        exportButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                export(this, App.getHistorian(this))
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE == permissions[0] &&
                PackageManager.PERMISSION_GRANTED == grantResults[0]
            ) {
                export(this, App.getHistorian(this))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun export(context: Context, historian: Historian) {
        val dir = File(Environment.getExternalStorageDirectory(), "HistorianSample")
        if (!dir.exists()) {
            dir.mkdir()
        }
        val dbPath = historian.dbPath()
        val exportPath = "${dir.path}${File.separator}${historian.dbName()}"

        val dbFile = File(dbPath)
        val file = File(exportPath)

        // delete if exists
        file.delete()

        var fis: FileInputStream? = null
        var output: FileOutputStream? = null

        try {
            fis = FileInputStream(dbFile)
            output = FileOutputStream(exportPath)
            val buffer = ByteArray(1024)
            var length: Int
            while (fis.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()

            Toast.makeText(context, "File exported to: $exportPath", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export", Toast.LENGTH_SHORT).show()
        } finally {
            output.closeQuietly()
            fis.closeQuietly()
        }
    }
}
