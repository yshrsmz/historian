package net.yslibrary.historian.internal;

import android.os.AsyncTask;

import net.yslibrary.historian.LogEntity;

import java.util.List;

/**
 * Created by yshrsmz on 2017/01/21.
 */

public class LogWritingTask extends AsyncTask<LogQueue, Void, Void> {

  private final LogWriter logWriter;

  public LogWritingTask(LogWriter logWriter) {
    this.logWriter = logWriter;
  }

  @Override
  protected final Void doInBackground(LogQueue... params) {
    try {
      List<LogEntity> saved = logWriter.log(params[0]);

      if (!saved.isEmpty()) {
        params[0].removeAll(saved);
      }
    } catch (Exception e) {

    }

    return null;
  }
}
