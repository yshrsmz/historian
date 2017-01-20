package net.yslibrary.historian.sample;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.database.DatabaseConnectionProvider;
import com.facebook.stetho.inspector.database.DatabaseFilesProvider;
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

import net.yslibrary.historian.Historian;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by yshrsmz on 17/01/20.
 */

public class App extends Application {

  Historian historian;

  @Override
  public void onCreate() {
    super.onCreate();

    historian = Historian.builder(this)
        .build();
    historian.initialize();

    Timber.plant(new Timber.DebugTree());

    Timber.d(historian.dbPath());

    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(new HistorianInspectorModulesProvider(this, historian))
        .build());
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }

  public static class HistorianInspectorModulesProvider implements InspectorModulesProvider {

    private final Context context;
    private final Historian historian;

    public HistorianInspectorModulesProvider(Context context, Historian historian) {
      this.context = context;
      this.historian = historian;
    }

    @Override
    public Iterable<ChromeDevtoolsDomain> get() {
      return new Stetho.DefaultInspectorModulesBuilder(context)
          .provideDatabaseDriver(new SqliteDatabaseDriver(context,
              new DatabaseFilesProvider() {
                @Override
                public List<File> getDatabaseFiles() {
                  List<File> list = new ArrayList<>();
                  list.add(new File(historian.dbPath()));
                  return list;
                }
              }, new DatabaseConnectionProvider() {
            @Override
            public SQLiteDatabase openDatabase(File file) throws SQLiteException {
              return historian.getDatabase();
            }
          }))
          .finish();
    }
  }
}
