package net.yslibrary.historian.internal;

import net.yslibrary.historian.LogEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yshrsmz on 2017/01/20.
 */

public class LogQueue {

  private final int size;
  private final List<LogEntity> queue;

  public LogQueue(int size) {
    this.size = size;
    this.queue = Collections.synchronizedList(new ArrayList<LogEntity>());
  }

  public boolean isExceeded() {
    return queue.size() > size;
  }

  public List<LogEntity> dequeue() {
    List<LogEntity> logs = new ArrayList<>();
    logs.addAll(queue);
    queue.removeAll(logs);

    return logs;
  }

  public List<LogEntity> dequeueIfNeeded() {
    if (isExceeded()) {
      return dequeue();
    }
    return Collections.emptyList();
  }
}
