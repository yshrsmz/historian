package net.yslibrary.historian.internal;

/**
 * Table definition of Log
 */

class LogTable {

  public static final String NAME = "log";

  @SuppressWarnings("StringBufferReplaceableByString")
  public static final String CREATE_TABLE = new StringBuilder()
      .append("CREATE TABLE ").append(NAME)
      .append(" (")
      .append("id INTEGER PRIMARY KEY AUTOINCREMENT,")
      .append("priority TEXT NOT NULL, ")
      .append("tag TEXT NOT NULL, ")
      .append("message TEXT NOT NULL, ")
      .append("created_at INTEGER NOT NULL")
      .append(");")
      .toString();

  @SuppressWarnings("StringBufferReplaceableByString")
  public static final String INSERT = new StringBuilder()
      .append("INSERT INTO ").append(NAME)
      .append("(priority, tag, message, created_at) ")
      .append("VALUES(?, ?, ?, ?);")
      .toString();

  @SuppressWarnings("StringBufferReplaceableByString")
  public static final String DELETE_OLDER = new StringBuilder()
      .append("DELETE FROM ").append(NAME)
      .append(" where id NOT IN (")
      .append("SELECT id FROM log ORDER BY created_at DESC LIMIT ?")
      .append(");")
      .toString();

  private LogTable() {
    // no-op
  }
}
