package net.yslibrary.historian.sample;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.stetho.Stetho;

import net.yslibrary.historian.Historian;
import net.yslibrary.historian.HistorianInspectorModulesProvider;
import net.yslibrary.historian.tree.HistorianTree;

import timber.log.Timber;

/**
 * Created by yshrsmz on 17/01/20.
 */

public class App extends Application {

  Historian historian;

  public static App get(@NonNull Context context) {
    return (App) context.getApplicationContext();
  }

  public static Historian getHistorian(@NonNull Context context) {
    return get(context).getHistorian();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    historian = Historian.builder(this)
        .build();
    historian.initialize();

    Timber.plant(new Timber.DebugTree());
    Timber.plant(HistorianTree.with(historian));

    Timber.d(historian.dbPath());

    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(new HistorianInspectorModulesProvider(this, historian))
        .build());
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    historian.terminate();
  }

  public Historian getHistorian() {
    return historian;
  }
}
