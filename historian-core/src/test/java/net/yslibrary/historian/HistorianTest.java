package net.yslibrary.historian;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

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


    assertEquals(historian.queue.size(), 9);
    assertEquals(historian.queue.get(0).message, "this is info1");
    assertEquals(historian.queue.get(1).message, "this is info2");
    assertEquals(historian.queue.get(2).message, "this is info3");
    assertEquals(historian.queue.get(3).message, "this is warn1");
    assertEquals(historian.queue.get(4).message, "this is warn2");
    assertEquals(historian.queue.get(5).message, "this is warn3");
    assertEquals(historian.queue.get(6).message, "this is error1");
    assertEquals(historian.queue.get(7).message, "this is error2");
    assertEquals(historian.queue.get(8).message, "this is error3");
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

    // wait for sqlite operation in AsyncTask
    Thread.sleep(100);

    assertEquals(historian.queue.size(), 0);

    SQLiteDatabase db = historian.dbOpenHelper.getReadableDatabase();
    Cursor cursor = db.query("log", new String[]{"id", "priority", "message", "timestamp"}, null, null, null, null, "timestamp ASC");

    assertEquals(cursor.getCount(), 10);

    cursor.moveToFirst();
    assertEquals(Cursors.getString(cursor, "priority"), "INFO");
    assertEquals(Cursors.getString(cursor, "message"), "this is info1");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "INFO");
    assertEquals(Cursors.getString(cursor, "message"), "this is info2");


    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "INFO");
    assertEquals(Cursors.getString(cursor, "message"), "this is info3");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "WARN");
    assertEquals(Cursors.getString(cursor, "message"), "this is warn1");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "WARN");
    assertEquals(Cursors.getString(cursor, "message"), "this is warn2");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "WARN");
    assertEquals(Cursors.getString(cursor, "message"), "this is warn3");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "ERROR");
    assertEquals(Cursors.getString(cursor, "message"), "this is error1");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "ERROR");
    assertEquals(Cursors.getString(cursor, "message"), "this is error2");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "ERROR");
    assertEquals(Cursors.getString(cursor, "message"), "this is error3");

    cursor.moveToNext();
    assertEquals(Cursors.getString(cursor, "priority"), "ERROR");
    assertEquals(Cursors.getString(cursor, "message"), "this is error4");

    cursor.close();
  }
}
