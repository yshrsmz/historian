# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Historian is an Android library that provides a custom Timber.Tree implementation to save logs to SQLite for debugging purposes. It's designed to help debug crashes in consumers' devices by persisting logs that can be retrieved later.

## Build Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run tests for a specific module
./gradlew :historian-core:test
./gradlew :historian-tree:test

# Generate coverage report (historian-core only)
./gradlew :historian-core:jacocoTestReportDebug

# Check for dependency updates
./gradlew dependencyUpdates

# Publish to Maven Central (requires credentials)
./gradlew clean publish --no-daemon
```

## Architecture

### Module Structure

- **historian-core**: Core library containing the main `Historian` class and SQLite persistence logic
- **historian-tree**: Timber integration - provides `HistorianTree` which extends `Timber.Tree`
- **sample**: Demo Android application showing usage

### Key Classes

- `Historian` (historian-core): Main entry point with builder pattern for configuration. Manages SQLite database, log writing via ExecutorService, and lifecycle.
- `HistorianTree` (historian-tree): Thin adapter that implements `Timber.Tree` and delegates to `Historian.log()`
- `LogWriter` / `LogWritingTask` (internal): Handle async log persistence with configurable max row limits
- `DbOpenHelper` / `LogTable` (internal): SQLite schema and database management

### Data Flow

1. App plants `HistorianTree` in Timber
2. Timber calls `HistorianTree.log()` → delegates to `Historian.log()`
3. `Historian` filters by log level, then submits `LogWritingTask` to single-thread executor
4. `LogWriter` persists to SQLite, pruning old entries when exceeding configured size

## Configuration

Version is managed in `gradle.properties` (`VERSION_NAME`). Dependencies are centralized in `gradle/dependencies.gradle`.
