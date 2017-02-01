package net.yslibrary.historian.internal;

/**
 * Entity class representing log
 */

public class LogEntity {
  public final String priority;
  public final String message;
  public final long timestamp;

  private LogEntity(String priority, String message, long timestamp) {
    this.priority = priority;
    this.message = message;
    this.timestamp = timestamp;
  }

  @SuppressWarnings("WeakerAccess")
  public static LogEntity create(int priority, String message, long timestamp) {
    return new LogEntity(Util.priorityString(priority), message, timestamp);
  }
}
