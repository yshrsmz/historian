package net.yslibrary.historian;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * Created by yshrsmz on 2017/01/22.
 */
@RunWith(ConfiguredRobolectricTestRunner.class)
public class HistorianTest {

  Historian historian;

  Context context;

  @Before
  public void setup() {
    context = RuntimeEnvironment.application;
    historian = Historian.builder(context).build();
  }

  @Test(expected = IllegalStateException.class)
  public void initialize_not_called() {
    historian.log(Log.DEBUG, "this is debug1");
  }

  @Test
  public void log_queue_under_logLevel() {
    historian.initialize();

    historian.log(Log.VERBOSE, "this is verbose");
    historian.log(Log.DEBUG, "this is debug1");
    historian.log(Log.DEBUG, "this is debug2");

    Cursor result = getAllLogs(historian);

    assertEquals(0, result.getCount());
  }

  @Test
  public void log_queue_over_logLevel() throws InterruptedException {
    historian.initialize();

    historian.log(Log.INFO, "this is info1");
    historian.log(Log.DEBUG, "this is debug1");
    historian.log(Log.INFO, "this is info2");
    historian.log(Log.WARN, "this is warn1");
    historian.log(Log.ERROR, "this is error1");

    Thread.sleep(500);

    Cursor cursor = getAllLogs(historian);

    assertEquals(4, cursor.getCount());

    cursor.moveToFirst();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals("this is info1", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals("this is info2", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("WARN", Cursors.getString(cursor, "priority"));
    assertEquals("this is warn1", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("ERROR", Cursors.getString(cursor, "priority"));
    assertEquals("this is error1", Cursors.getString(cursor, "message"));

    cursor.close();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.JELLY_BEAN,
      Build.VERSION_CODES.KITKAT,
      Build.VERSION_CODES.LOLLIPOP,
      Build.VERSION_CODES.M,
      Build.VERSION_CODES.N})
  public void log_background() throws ExecutionException, InterruptedException {
    historian.initialize();

    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> future = es.submit(new Runnable() {
      @Override
      public void run() {
        for (int i = 0, len = 10; i < len; i++) {
          historian.log(Log.INFO, "this log is from background thread - " + i);
        }
      }
    });

    future.get();

    Thread.sleep(200);

    Cursor cursor = getAllLogs(historian);

    assertEquals(10, cursor.getCount());
  }

  @Test
  public void multipleWriteInMultipleThreads() throws InterruptedException {
    int nThreads = 10;
    historian.initialize();

    for (int i = 0; i < nThreads; i++) {
      Runnable writer = new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep((int) (Math.random() * 200.0));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          historian.log(Log.INFO, "this is test: " + System.currentTimeMillis());
        }
      };

      Thread thread = new Thread(writer);
      thread.run();
    }

    Thread.sleep(1000);

    Cursor cursor = getAllLogs(historian);

    assertEquals(cursor.getCount(), 10);
  }

  Cursor getAllLogs(Historian historian) {
    SQLiteDatabase db = historian.dbOpenHelper.getReadableDatabase();
    return db.query("log", new String[]{"id", "priority", "message", "timestamp"}, null, null, null, null, "timestamp ASC");
  }
}
