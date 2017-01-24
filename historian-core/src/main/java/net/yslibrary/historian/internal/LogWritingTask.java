package net.yslibrary.historian.internal;

import java.util.concurrent.Executor;

/**
 * Created by yshrsmz on 2017/01/21.
 */

public class LogWritingTask implements Runnable {

  private final Executor callbackExecutor;
  private final LogWriter logWriter;
  private final LogQueue queue;

  public LogWritingTask(Executor callbackExecutor,
                        LogWriter logWriter,
                        LogQueue queue) {
    this.callbackExecutor = callbackExecutor;
    this.logWriter = logWriter;
    this.queue = queue;
  }

  @Override
  public void run() {

    try {
      logWriter.log(queue);
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
