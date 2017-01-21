HistorianTree
===

HistorianTree is a custom [Timber](https://github.com/JakeWharton/timber).Tree implementation that saves logs to SQLite, so that you can see the SQLite file later for debugging.

This library is primarily made to help debugging crash in consumers' devices.


```java
class App extends Application {

    LogHistorian historian;

    @Override
    public void onCreate() {
        historian = LogHistorian.builder(context)
            // db name. defaults to "log.db"
            .name("log.db")
            // a directory where the db file will be saved. defaults to `context.getFiles()`.
            // The directory will be created if it does not exist.
            .directory(new File(Environment.getExternalStorageDirectory(), "somedir"))
            // max number of logs stored in db. defaults to 500
            .size(500)
            // queue logs until specified logs collected, so that db operation is minimized. defaults to 10
            .queueSize(10)
            // log level to save
            .logLevel(Log.INFO)
            .build();

        // initialize historian
        historian.initialize();

        Timber.plant(LogHistorianTree.with(historian));

        // immediately save queued logs
        historian.flush();

        // delete all saved logs
        historian.delete();

        // provide db path in Uri
        historian.dbPath();
    }

    @Override
    public void onTerminate() {
        // this is needed to save unsaved logs in the queue
        historian.terminate();
    }
}
```


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


DELETE FROM log WHERE id NOT IN
  (SELECT id FROM
     (SELECT id FROM log ORDER BY timestamp DESC LIMIT 5)
   )