package net.yslibrary.historian.internal

import android.util.Log

fun Int.toPriorityString(): String = when (this) {
    Log.VERBOSE -> "VERBOSE"
    Log.DEBUG -> "DEBUG"
    Log.INFO -> "INFO"
    Log.WARN -> "WARN"
    Log.ERROR -> "ERROR"
    Log.ASSERT -> "ASSERT"
    else -> "UNKNOWN"
}
