package net.yslibrary.historian.internal;

/**
 * Created by yshrsmz on 17/01/20.
 */

class LogTable {

  public static final String NAME = "log";

  @SuppressWarnings("StringBufferReplaceableByString")
  public static final String CREATE_TABLE = new StringBuilder()
      .append("CREATE TABLE ").append(NAME)
      .append(" (")
      .append("id INTEGER PRIMARY KEY AUTOINCREMENT,")
      .append("priority TEXT, ")
      .append("message TEXT, ")
      .append("timestamp INTEGER")
      .append(");")
      .toString();

  @SuppressWarnings("StringBufferReplaceableByString")
  public static final String INSERT = new StringBuilder()
      .append("INSERT INTO ").append(NAME)
      .append("(priority, message, timestamp) ")
      .append("VALUES(?, ?, ?);")
      .toString();

  @SuppressWarnings("StringBufferReplaceableByString")
  public static final String DELETE_OLDER = new StringBuilder()
      .append("DELETE FROM ").append(NAME)
      .append(" where id NOT IN (")
      .append("SELECT id FROM log ORDER BY timestamp DESC LIMIT ?")
      .append(");")
      .toString();

  private LogTable() {
    // no-op
  }
}
