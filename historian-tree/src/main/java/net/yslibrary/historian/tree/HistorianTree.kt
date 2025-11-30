package net.yslibrary.historian.tree

import net.yslibrary.historian.Historian
import timber.log.Timber

class HistorianTree private constructor(
    private val historian: Historian
) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        historian.log(priority, tag, message)
    }

    companion object {
        @JvmStatic
        fun with(historian: Historian): HistorianTree = HistorianTree(historian)
    }
}

/**
 * Extension function to create a Timber.Tree from a Historian instance
 */
fun Historian.asTimberTree(): Timber.Tree = HistorianTree.with(this)
