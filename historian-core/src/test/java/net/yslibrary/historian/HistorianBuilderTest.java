package net.yslibrary.historian;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by yshrsmz on 2017/01/22.
 */
@RunWith(ConfiguredRobolectricTestRunner.class)
public class HistorianBuilderTest {

  Context context;

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
    assertNotNull(historian.queue);

    String path = File.separator + "private" + context.getFilesDir() + File.separator + Historian.DB_NAME;
    assertEquals(historian.dbOpenHelper.getDatabaseName(), path);

    assertEquals(historian.logLevel, Historian.LOG_LEVEL);
    assertEquals(historian.directory, context.getFilesDir());
    assertEquals(historian.dbName, Historian.DB_NAME);
    assertEquals(historian.size, Historian.SIZE);
    assertEquals(historian.queueSize, Historian.QUEUE_SIZE);
  }

  @Test
  public void build_with_custom_params() {
    Historian historian = Historian.builder(context)
        .name("test.db")
        .directory(context.getExternalFilesDir(null))
        .logLevel(Log.DEBUG)
        .queueSize(50)
        .size(1000)
        .build();

    assertNotNull(historian.context);
    assertNotNull(historian.dbOpenHelper);
    assertNotNull(historian.logWriter);
    assertNotNull(historian.queue);

    String path = File.separator + "private" + context.getExternalFilesDir(null) + File.separator + "test.db";
    assertEquals(historian.dbOpenHelper.getDatabaseName(), path);

    assertEquals(historian.logLevel, Log.DEBUG);
    assertEquals(historian.directory, context.getExternalFilesDir(null));
    assertEquals(historian.dbName, "test.db");
    assertEquals(historian.size, 1000);
    assertEquals(historian.queueSize, 50);
  }
}
