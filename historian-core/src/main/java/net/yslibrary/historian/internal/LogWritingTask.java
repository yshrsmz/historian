package net.yslibrary.historian.internal;

import net.yslibrary.historian.LogEntity;

import java.util.concurrent.Executor;

/**
 * Created by yshrsmz on 2017/01/21.
 */

public class LogWritingTask implements Runnable {

  private final Executor callbackExecutor;
  private final LogWriter logWriter;
  private final LogEntity log;

  public LogWritingTask(Executor callbackExecutor,
                        LogWriter logWriter,
                        LogEntity log) {
    this.callbackExecutor = callbackExecutor;
    this.logWriter = logWriter;
    this.log = log;
  }

  @Override
  public void run() {

    try {
      logWriter.log(log);
    } catch (final Exception e) {
      // rethrow to main thread
      callbackExecutor.execute(new Runnable() {
        @Override
        public void run() {
          throw e;
        }
      });
    }
  }
}
