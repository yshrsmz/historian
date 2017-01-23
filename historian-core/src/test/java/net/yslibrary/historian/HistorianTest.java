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

    assertEquals(historian.queue.size(), 0);
  }

  @Test
  public void log_queue_over_logLevel() {
    historian.initialize();

    historian.log(Log.INFO, "this is info1");
    historian.log(Log.DEBUG, "this is debug2");
    historian.log(Log.INFO, "this is info2");
    historian.log(Log.INFO, "this is info3");
    historian.log(Log.WARN, "this is warn1");
    historian.log(Log.WARN, "this is warn2");
    historian.log(Log.WARN, "this is warn3");
    historian.log(Log.ERROR, "this is error1");
    historian.log(Log.ERROR, "this is error2");
    historian.log(Log.ERROR, "this is error3");
    historian.log(Log.DEBUG, "this is debug2");


    assertEquals(9, historian.queue.size());
    assertEquals("this is info1", historian.queue.get(0).message);
    assertEquals("this is info2", historian.queue.get(1).message);
    assertEquals("this is info3", historian.queue.get(2).message);
    assertEquals("this is warn1", historian.queue.get(3).message);
    assertEquals("this is warn2", historian.queue.get(4).message);
    assertEquals("this is warn3", historian.queue.get(5).message);
    assertEquals("this is error1", historian.queue.get(6).message);
    assertEquals("this is error2", historian.queue.get(7).message);
    assertEquals("this is error3", historian.queue.get(8).message);
  }

  @Test
  public void log_saved() throws InterruptedException {
    historian.initialize();

    historian.log(Log.INFO, "this is info1");
    historian.log(Log.DEBUG, "this is debug2");
    historian.log(Log.INFO, "this is info2");
    historian.log(Log.INFO, "this is info3");
    historian.log(Log.WARN, "this is warn1");
    historian.log(Log.WARN, "this is warn2");
    historian.log(Log.WARN, "this is warn3");
    historian.log(Log.ERROR, "this is error1");
    historian.log(Log.ERROR, "this is error2");
    historian.log(Log.ERROR, "this is error3");
    historian.log(Log.DEBUG, "this is debug2");
    historian.log(Log.ERROR, "this is error4");

    // wait for sqlite operation in background
    Thread.sleep(500);

    historian.log(Log.ERROR, "this is error5");

    // queue size is 10, so 1 log should remain in the queue
    assertEquals(1, historian.queue.size());
    assertEquals("ERROR", historian.queue.get(0).priority);
    assertEquals("this is error5", historian.queue.get(0).message);

    SQLiteDatabase db = historian.dbOpenHelper.getReadableDatabase();
    Cursor cursor = db.query("log", new String[]{"id", "priority", "message", "timestamp"}, null, null, null, null, "timestamp ASC");

    assertEquals(10, cursor.getCount());

    cursor.moveToFirst();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals(Cursors.getString(cursor, "message"), "this is info1");

    cursor.moveToNext();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals("this is info2", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("INFO", Cursors.getString(cursor, "priority"));
    assertEquals("this is info3", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("WARN", Cursors.getString(cursor, "priority"));
    assertEquals("this is warn1", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("WARN", Cursors.getString(cursor, "priority"));
    assertEquals("this is warn2", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("WARN", Cursors.getString(cursor, "priority"));
    assertEquals("this is warn3", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("ERROR", Cursors.getString(cursor, "priority"));
    assertEquals("this is error1", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("ERROR", Cursors.getString(cursor, "priority"));
    assertEquals("this is error2", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("ERROR", Cursors.getString(cursor, "priority"));
    assertEquals("this is error3", Cursors.getString(cursor, "message"));

    cursor.moveToNext();
    assertEquals("ERROR", Cursors.getString(cursor, "priority"));
    assertEquals("this is error4", Cursors.getString(cursor, "message"));

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
          historian.log(Log.INFO, "this log in from background thread - " + i);
        }
      }
    });

    future.get();
  }
}
