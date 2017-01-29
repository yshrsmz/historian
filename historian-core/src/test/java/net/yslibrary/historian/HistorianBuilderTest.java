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

    String path = context.getFilesDir() + File.separator + Historian.DB_NAME;
    assertEquals(historian.dbOpenHelper.getDatabaseName(), path);

    assertEquals(Historian.LOG_LEVEL, historian.logLevel);
    assertEquals(context.getFilesDir(), historian.directory);
    assertEquals(Historian.DB_NAME, historian.dbName);
    assertEquals(Historian.SIZE, historian.size);
  }

  @Test
  public void build_with_custom_params() {
    Historian historian = Historian.builder(context)
        .name("test.db")
        .directory(context.getExternalFilesDir(null))
        .logLevel(Log.DEBUG)
        .size(1000)
        .build();

    assertNotNull(historian.context);
    assertNotNull(historian.dbOpenHelper);
    assertNotNull(historian.logWriter);

    String path = context.getExternalFilesDir(null) + File.separator + "test.db";
    assertEquals(path, historian.dbOpenHelper.getDatabaseName());

    assertEquals(Log.DEBUG, historian.logLevel);
    assertEquals(context.getExternalFilesDir(null), historian.directory);
    assertEquals("test.db", historian.dbName);
    assertEquals(1000, historian.size);
  }
}
