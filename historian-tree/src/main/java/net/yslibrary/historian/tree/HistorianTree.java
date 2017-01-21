package net.yslibrary.historian.tree;

import net.yslibrary.historian.Historian;

import timber.log.Timber;

/**
 * Created by yshrsmz on 2017/01/21.
 */

public class HistorianTree extends Timber.Tree {

  private final Historian historian;

  private HistorianTree(Historian historian) {
    this.historian = historian;
  }

  public static HistorianTree with(Historian historian) {
    return new HistorianTree(historian);
  }

  @Override
  protected void log(int priority, String tag, String message, Throwable t) {
    historian.log(priority, message);
  }
}
