package net.yslibrary.historian.internal;

import android.util.Log;

/**
 * Created by yshrsmz on 2017/01/20.
 */

public class Util {

    private Util() {
        // no-op
    }

    public static String priorityString(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "VERBOSE";
            case Log.DEBUG:
                return "DEBUG";
            case Log.INFO:
                return "INFO";
            case Log.WARN:
                return "WARN";
            case Log.ERROR:
                return "ERROR";
            case Log.ASSERT:
                return "ASSERT";
            default:
                return "UNKNOWN";
        }
    }
}
