package net.yslibrary.historian.internal;

import net.yslibrary.historian.LogEntity;

import java.util.ArrayList;
import java.util.Collection;
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

  public int size() {
    return queue.size();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public boolean isExceeded() {
    return queue.size() >= size;
  }

  public void queue(LogEntity logEntity) {
    queue.add(logEntity);
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

  public LogEntity get(int index) {
    return queue.get(index);
  }

  public void clear() {
    queue.clear();
  }

  public boolean removeAll(Collection<LogEntity> list) {
    return queue.removeAll(list);
  }
}
