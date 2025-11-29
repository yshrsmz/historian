package net.yslibrary.historian;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * Created by yshrsmz on 2017/01/22.
 */
@RunWith(RobolectricTestRunner.class)
public class HistorianTest {

  static final String TAG = "test_tag";

  private Historian historian;

  @Before
  public void setup() {
    Context context = ApplicationProvider.getApplicationContext();

    historian = Historian.builder(context).build();
  }

  @Test(expected = IllegalStateException.class)
  public void initialize_not_called() {
    historian.log(Log.DEBUG, TAG, "this is debug1");
  }

  @Test
  public void log_queue_under_logLevel() {
    historian.initialize();

    historian.log(Log.VERBOSE, TAG, "this is verbose");
    historian.log(Log.DEBUG, TAG, "this is debug1");
    historian.log(Log.DEBUG, TAG, "this is debug2");

    Cursor result = getAllLogs(historian);

    assertEquals(0, result.getCount());
  }

  @Test
  public void log_queue_over_logLevel() throws InterruptedException {
    historian.initialize();

    historian.log(Log.INFO, TAG, "this is info1");
    historian.log(Log.DEBUG, TAG, "this is debug1");
    historian.log(Log.INFO, TAG, "this is info2");
    historian.log(Log.WARN, TAG, "this is warn1");
    historian.log(Log.ERROR, TAG, "this is error1");

    Thread.sleep(500);

    Cursor cursor = getAllLogs(historian);

    assertEquals(4, cursor.getCount());

    cursor.moveToFirst();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals(TAG, Cursors.getString(cursor, "tag"));
    assertEquals("this is info1", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals(TAG, Cursors.getString(cursor, "tag"));
    assertEquals("this is info2", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("WARN", Cursors.getString(cursor, "priority"));
    assertEquals(TAG, Cursors.getString(cursor, "tag"));
    assertEquals("this is warn1", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("ERROR", Cursors.getString(cursor, "priority"));
    assertEquals(TAG, Cursors.getString(cursor, "tag"));
    assertEquals("this is error1", Cursors.getString(cursor, "message"));

    cursor.close();
  }

  @Test
  @Config(sdk = {21, 23, 24, 28, 33})
  public void log_background() throws ExecutionException, InterruptedException {
    historian.initialize();

    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> future = es.submit(new Runnable() {
      @Override
      public void run() {
        for (int i = 0, len = 10; i < len; i++) {
          historian.log(Log.INFO, TAG, "this log is from background thread - " + i);
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
          historian.log(Log.INFO, TAG, "this is test: " + System.currentTimeMillis());
        }
      };

      Thread thread = new Thread(writer);
      thread.run();
    }

    Thread.sleep(1000);

    Cursor cursor = getAllLogs(historian);

    assertEquals(cursor.getCount(), 10);
  }

  @Test
  public void nullTag() throws InterruptedException {
    historian.initialize();

    historian.log(Log.INFO, null, "this tag should be null");

    Thread.sleep(1000);

    Cursor cursor = getAllLogs(historian);

    cursor.moveToFirst();
    assertEquals("", Cursors.getString(cursor, "tag"));
  }

  private Cursor getAllLogs(Historian historian) {
    SQLiteDatabase db = historian.dbOpenHelper.getReadableDatabase();
    return db.query("log", new String[]{"id", "tag", "priority", "message", "created_at"}, null, null, null, null, "created_at ASC");
  }
}
