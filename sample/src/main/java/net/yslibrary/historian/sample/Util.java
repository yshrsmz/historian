package net.yslibrary.historian.sample;

import java.io.Closeable;

/**
 * Created by yshrsmz on 17/02/14.
 */

public class Util {

  private Util() {
    // no-op
  }

  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Throwable t) {
      // no-op
    }
  }
}
