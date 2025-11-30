package net.yslibrary.historian.internal

import net.yslibrary.historian.Historian

/**
 * Runnable implementation writing logs and executing callbacks
 */
class LogWritingTask(
    private val callbacks: Historian.Callbacks,
    private val logWriter: LogWriter,
    private val log: LogEntity
) : Runnable {

    override fun run() {
        try {
            logWriter.log(log)
            callbacks.onSuccess()
        } catch (t: Throwable) {
            callbacks.onFailure(t)
        }
    }
}
