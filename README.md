Historian Tree
===

HistorianTree is a custom [Timber](https://github.com/JakeWharton/timber).Tree implementation that saves logs to SQLite, so that you can see the SQLite file later for debugging.

This library is primarily made to help debugging crash in consumers' devices.

## Installation

TBD

## Usage

```java
class App extends Application {

    Historian historian;

    @Override
    public void onCreate() {
        historian = Historian.builder(context)
            // db name. defaults to "log.db"
            .name("log.db")
            // a directory where the db file will be saved. defaults to `context.getFiles()`.
            // The directory will be created if it does not exist.
            .directory(new File(Environment.getExternalStorageDirectory(), "somedir"))
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

        // provide db path in Uri
        historian.dbPath();
    }
}
```


## License

```
Copyright 2017 Shimizu Yasuhiro (yshrsmz)

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


```
CREATE TABLE log(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  priority TEXT,
  message TEXT,
  timestamp INTEGER
);

CREATE TABLE log(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  priority TEXT,
  message TEXT,
  timestamp TIMESTAMP DEFAULT (DATETIME('now', 'localtime'))
);

INSERT INTO log(priority, message) VALUES("DEBUG", "message2");
INSERT INTO log(priority, message) VALUES("DEBUG", "message3");
INSERT INTO log(priority, message) VALUES("DEBUG", "message4");
INSERT INTO log(priority, message) VALUES("DEBUG", "message5");
INSERT INTO log(priority, message) VALUES("DEBUG", "message6");
INSERT INTO log(priority, message) VALUES("DEBUG", "message7");
INSERT INTO log(priority, message) VALUES("DEBUG", "message8");
INSERT INTO log(priority, message) VALUES("DEBUG", "message9");
INSERT INTO log(priority, message) VALUES("DEBUG", "message10");
INSERT INTO log(priority, message) VALUES("DEBUG", "message11");

INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message2", 12346);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message3", 12347);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message4", 12348);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message5", 12349);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message6", 12350);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message7", 12351);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message8", 12352);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message9", 12353);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message10", 12354);
INSERT INTO log(priority, message, timestamp) VALUES("DEBUG", "message11", 12355);


DELETE FROM log WHERE id NOT IN (SELECT id FROM log ORDER BY timestamp DESC LIMIT 5)
```
