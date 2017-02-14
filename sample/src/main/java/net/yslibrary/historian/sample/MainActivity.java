package net.yslibrary.historian.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.yslibrary.historian.Historian;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  AtomicLong counter = new AtomicLong();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        for (int i = 0, l = 100; i < 100; i++) {
          Timber.i("test: %d", counter.getAndIncrement());
        }
      }
    });

    View exportButton = findViewById(R.id.export);
    exportButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
          export(MainActivity.this, App.getHistorian(MainActivity.this));
        } else {
          ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 0) {
      if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0]) && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
        export(this, App.getHistorian(this));
      }
    }
  }

  public void export(Context context, Historian historian) {
    File dir = new File(Environment.getExternalStorageDirectory(), "HistorianSample");
    if (!dir.exists()) {
      dir.mkdir();
    }
    String dbPath = historian.dbPath();
    String exportPath = dir.getPath() + File.separator + historian.dbName();

    FileInputStream fis = null;
    OutputStream output = null;

    File dbFile = new File(dbPath);
    File file = new File(exportPath);

    // delete if exists
    file.delete();
    try {
      fis = new FileInputStream(dbFile);
      output = new FileOutputStream(exportPath);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = fis.read(buffer)) > 0) {
        output.write(buffer, 0, length);
      }
      //Close the streams
      output.flush();

      Toast.makeText(context, "File exported to: " + exportPath, Toast.LENGTH_SHORT).show();
    } catch (IOException e) {
      e.printStackTrace();
      Toast.makeText(context, "Failed to export", Toast.LENGTH_SHORT).show();
    } finally {
      Util.closeQuietly(output);
      Util.closeQuietly(fis);
    }
  }
}
