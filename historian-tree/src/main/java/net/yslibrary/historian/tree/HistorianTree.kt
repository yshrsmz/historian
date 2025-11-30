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
 * Extension function to create a Timber.Tree from a Historian instance.
 *
 * Usage in Kotlin:
 * ```
 * Timber.plant(historian.toTree())
 * ```
 *
 * Usage in Java:
 * ```
 * Timber.plant(HistorianTreeKt.toTimberTree(historian));
 * ```
 */
@JvmName("toTimberTree")
fun Historian.toTree(): Timber.Tree = HistorianTree.with(this)
