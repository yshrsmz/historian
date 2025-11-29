# Project Overview

## Purpose
**Historian** is an Android library that provides a custom `Timber.Tree` implementation to save logs to SQLite for debugging purposes. It's designed to help debug crashes in consumers' devices by persisting logs that can be retrieved later.

## Tech Stack
- **Language**: Java (Java 11)
- **Platform**: Android (minSdk 21, targetSdk/compileSdk 35)
- **Build System**: Gradle 8.14.3 with Android Gradle Plugin 8.13.1
- **Testing**: JUnit 4, Robolectric 4.14.1
- **Code Coverage**: JaCoCo 0.8.12
- **Publishing**: Maven Central via vanniktech/gradle-maven-publish-plugin 0.30.0

## Key Dependencies
- `androidx.annotation:annotation:1.9.1` - Android annotations
- `com.jakewharton.timber:timber:5.0.1` - Timber logging library (for historian-tree module)

## Module Structure
1. **historian-core**: Core library containing `Historian` class and SQLite persistence logic
2. **historian-tree**: Timber integration - provides `HistorianTree` which extends `Timber.Tree`
3. **sample**: Demo Android application showing usage

## Version
- Current version: `0.5.0-SNAPSHOT` (managed in `gradle.properties` via `VERSION_NAME`)
- Group ID: `net.yslibrary.historian`
