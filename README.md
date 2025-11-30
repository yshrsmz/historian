Historian
===

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Historian-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5329)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.yslibrary.historian/historian-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.yslibrary.historian/historian-core)

Historian is a custom [Timber](https://github.com/JakeWharton/timber).Tree implementation that saves logs to SQLite, so that you can see/download the SQLite file later for debugging.

This library is primarily made to help debugging crash in consumers' devices.

## Requirements

- minSdk 21+

## Installation

Historian is distributed via Maven Central. [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.yslibrary.historian/historian-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.yslibrary.historian/historian-core)

```kotlin
dependencies {
  implementation("net.yslibrary.historian:historian-core:0.6.0")
  implementation("net.yslibrary.historian:historian-tree:0.6.0")
  implementation("com.jakewharton.timber:timber:5.0.1")
}
```

<details>
<summary>Groovy DSL</summary>

```gradle
dependencies {
  implementation 'net.yslibrary.historian:historian-core:0.6.0'
  implementation 'net.yslibrary.historian:historian-tree:0.6.0'
  implementation 'com.jakewharton.timber:timber:5.0.1'
}
```
</details>

## Usage

### Kotlin

```kotlin
class App : Application() {

    lateinit var historian: Historian

    override fun onCreate() {
        super.onCreate()

        historian = Historian(this) {
            // db name. defaults to "log.db"
            name = "log.db"
            // a directory where the db file will be saved. defaults to `context.filesDir`.
            // The directory will be created if it does not exist.
            directory = File(getExternalFilesDir(null), "logs")
            // max number of logs stored in db. defaults to 500
            size = 500
            // log level to save. defaults to Log.INFO
            logLevel = Log.INFO
            // enable debug logs
            debug = true
            // optional callbacks
            onSuccess = Historian.OnSuccessCallback { /* log saved */ }
            onFailure = Historian.OnFailureCallback { throwable -> /* handle error */ }
        }

        // initialize historian
        historian.initialize()

        // plant as Timber tree - using extension function
        Timber.plant(historian.toTree())

        // delete all saved logs
        historian.delete()

        // get database path
        historian.dbPath()

        // graceful shutdown (call from background thread)
        // historian.terminateSafe()
    }
}
```

<details>
<summary>Java</summary>

```java
public class App extends Application {

    Historian historian;

    @Override
    public void onCreate() {
        super.onCreate();

        historian = Historian.builder(this)
            // db name. defaults to "log.db"
            .name("log.db")
            // a directory where the db file will be saved. defaults to `context.getFilesDir()`.
            // The directory will be created if it does not exist.
            .directory(new File(getExternalFilesDir(null), "logs"))
            // max number of logs stored in db. defaults to 500
            .size(500)
            // log level to save
            .logLevel(Log.INFO)
            .debug(true)
            .build();

        // initialize historian
        historian.initialize();

        Timber.plant(HistorianTree.with(historian));

        // delete all saved logs
        historian.delete();

        // provide db path
        historian.dbPath();
    }
}
```
</details>

## Table definition

```sql
CREATE TABLE log(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  priority TEXT NOT NULL,
  tag TEXT NOT NULL,
  message TEXT NOT NULL,
  created_at INTEGER NOT NULL);
```


## License

```
Copyright 2017-2025 Yasuhiro SHIMIZU (yshrsmz)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
