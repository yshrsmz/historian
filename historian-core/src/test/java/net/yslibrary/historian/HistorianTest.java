package net.yslibrary.historian;

import android.content.Context;
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
    historian.log(Log.DEBUG, "this is debug");
    historian.log(Log.INFO, "this is info2");


    assertEquals(historian.queue.size(), 2);
    assertEquals(historian.queue.get(0).message, "this is info1");
    assertEquals(historian.queue.get(1).message, "this is info2");
  }
}
