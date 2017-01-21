package net.yslibrary.historian;

import net.yslibrary.historian.internal.Util;

/**
 * Created by yshrsmz on 2017/01/20.
 */

public class LogEntity {
  public final String priority;
  public final String message;
  public final long timestamp;

  public LogEntity(String priority, String message, long timestamp) {
    this.priority = priority;
    this.message = message;
    this.timestamp = timestamp;
  }

  public static LogEntity create(int priority, String message, long timestamp) {
    return new LogEntity(Util.priorityString(priority), message, timestamp);
  }
}
