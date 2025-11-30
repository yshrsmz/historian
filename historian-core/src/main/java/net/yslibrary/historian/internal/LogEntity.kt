package net.yslibrary.historian.internal

/**
 * Entity class representing log
 */
data class LogEntity(
    @JvmField val priority: String,
    @JvmField val tag: String,
    @JvmField val message: String,
    @JvmField val timestamp: Long
) {
    companion object {
        @JvmStatic
        fun create(priority: Int, tag: String?, message: String, timestamp: Long): LogEntity =
            LogEntity(
                priority = priority.toPriorityString(),
                tag = tag.orEmpty(),
                message = message,
                timestamp = timestamp
            )
    }
}
