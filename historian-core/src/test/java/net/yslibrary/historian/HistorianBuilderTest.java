package net.yslibrary.historian;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by yshrsmz on 2017/01/22.
 */
@RunWith(RobolectricTestRunner.class)
public class HistorianBuilderTest {

  private Context context;

  @Before
  public void setup() {
    context = RuntimeEnvironment.application;
  }

  @Test
  public void build_with_defaults() {
    Historian historian = Historian.builder(context).build();

    assertNotNull(historian.context);
    assertNotNull(historian.dbOpenHelper);
    assertNotNull(historian.logWriter);

//    String path = context.getFilesDir() + File.separator + Historian.DB_NAME;
//    assertEquals(historian.dbOpenHelper.getDatabaseName(), path);

    assertEquals(Historian.LOG_LEVEL, historian.logLevel);
    assertEquals(context.getFilesDir(), historian.directory);
    assertEquals(Historian.DB_NAME, historian.dbName);
    assertEquals(Historian.SIZE, historian.size);
    assertFalse(historian.debug);
    assertThat(historian.callbacks, instanceOf(Historian.DefaultCallbacks.class));
  }

  @Test
  public void build_with_custom_params() {
    Historian historian = Historian.builder(context)
        .name("test.db")
        .directory(context.getExternalFilesDir(null))
        .logLevel(Log.DEBUG)
        .size(1000)
        .debug(true)
        .callbacks(new TestCallbacks())
        .build();

    assertNotNull(historian.context);
    assertNotNull(historian.dbOpenHelper);
    assertNotNull(historian.logWriter);

//    String path = context.getExternalFilesDir(null) + File.separator + "test.db";
//    assertEquals(path, historian.dbOpenHelper.getDatabaseName());

    assertEquals(Log.DEBUG, historian.logLevel);
    assertEquals(context.getExternalFilesDir(null), historian.directory);
    assertEquals("test.db", historian.dbName);
    assertEquals(1000, historian.size);
    assertTrue(historian.debug);
    assertThat(historian.callbacks, instanceOf(TestCallbacks.class));
  }

  static class TestCallbacks implements Historian.Callbacks {

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(Throwable throwable) {

    }
  }
}
