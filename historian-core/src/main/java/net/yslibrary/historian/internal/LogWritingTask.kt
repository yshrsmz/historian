package net.yslibrary.historian.internal

import net.yslibrary.historian.Historian

/**
 * Runnable implementation writing logs and executing callbacks
 */
class LogWritingTask(
    private val onSuccess: Historian.OnSuccessCallback?,
    private val onFailure: Historian.OnFailureCallback?,
    private val logWriter: LogWriter,
    private val log: LogEntity
) : Runnable {

    override fun run() {
        try {
            logWriter.log(log)
            onSuccess?.onSuccess()
        } catch (t: Throwable) {
            onFailure?.onFailure(t)
        }
    }
}
