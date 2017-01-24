package net.yslibrary.historian.internal;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Created by yshrsmz on 17/01/24.
 */

public class MainThreadExecutor implements Executor {

  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void execute(Runnable command) {
    handler.post(command);
  }
}
