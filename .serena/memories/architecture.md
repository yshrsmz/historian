# Architecture

## Module Dependency Graph
```
sample --> historian-tree --> historian-core
                         \--> timber
```

## Key Classes

### historian-core

#### `Historian` (Public API)
- Location: `historian-core/src/main/java/net/yslibrary/historian/Historian.java`
- Main entry point with builder pattern for configuration
- Manages SQLite database, log writing via ExecutorService, and lifecycle
- Inner classes:
  - `Builder`: Configuration builder
  - `Callbacks`: Interface for lifecycle events
  - `DefaultCallbacks`: No-op implementation

#### Internal Classes (in `internal` package)
- `LogWriter`: Handles actual SQLite write operations
- `LogWritingTask`: Runnable task submitted to executor for async persistence
- `DbOpenHelper`: SQLite database helper for schema management
- `LogTable`: SQLite table schema definitions
- `LogEntity`: Data class for log entries
- `Util`: Utility methods

### historian-tree

#### `HistorianTree`
- Location: `historian-tree/src/main/java/net/yslibrary/historian/tree/HistorianTree.java`
- Thin adapter implementing `Timber.Tree`
- Delegates all logging to `Historian.log()`

## Data Flow

1. App plants `HistorianTree` in Timber
2. Timber calls `HistorianTree.log()` → delegates to `Historian.log()`
3. `Historian` filters by log level, then submits `LogWritingTask` to single-thread executor
4. `LogWriter` persists to SQLite, pruning old entries when exceeding configured size

## Configuration Options (via Builder)

- `directory`: Custom directory for database file
- `name`: Database filename (default: defined by `DB_NAME` constant)
- `size`: Maximum number of log entries (default: defined by `SIZE` constant)
- `logLevel`: Minimum log level to persist (default: defined by `LOG_LEVEL` constant)
- `debug`: Enable debug mode
- `callbacks`: Lifecycle callbacks implementation
