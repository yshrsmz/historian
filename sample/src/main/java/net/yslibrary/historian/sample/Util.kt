package net.yslibrary.historian.sample

import java.io.Closeable

fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (t: Throwable) {
        // no-op
    }
}
