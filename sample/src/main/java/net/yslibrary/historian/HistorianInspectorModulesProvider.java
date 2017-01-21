package net.yslibrary.historian;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.database.DatabaseConnectionProvider;
import com.facebook.stetho.inspector.database.DatabaseFilesProvider;
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yshrsmz on 2017/01/21.
 */
public class HistorianInspectorModulesProvider implements InspectorModulesProvider {

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
