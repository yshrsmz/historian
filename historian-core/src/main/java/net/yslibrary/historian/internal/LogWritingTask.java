package net.yslibrary.historian.internal;

/**
 * Created by yshrsmz on 2017/01/21.
 */

public class LogWritingTask implements Runnable {

  private final LogWriter logWriter;
  private final LogQueue queue;

  public LogWritingTask(LogWriter logWriter,
                        LogQueue queue) {
    this.logWriter = logWriter;
    this.queue = queue;
  }

  @Override
  public void run() {
//    throw new RuntimeException("test");
    logWriter.log(queue);
  }
}
