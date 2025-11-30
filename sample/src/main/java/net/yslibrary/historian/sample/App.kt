package net.yslibrary.historian.sample

import android.app.Application
import android.content.Context
import net.yslibrary.historian.Historian
import net.yslibrary.historian.tree.HistorianTree
import timber.log.Timber

class App : Application() {

    lateinit var historian: Historian
        private set

    override fun onCreate() {
        super.onCreate()

        // Using Kotlin DSL
        historian = Historian(this) {
            debug = true
        }
        historian.initialize()

        Timber.plant(Timber.DebugTree())
        Timber.plant(HistorianTree.with(historian))

        Timber.d(historian.dbPath())
    }

    override fun onTerminate() {
        super.onTerminate()
        historian.terminate()
    }

    companion object {
        @JvmStatic
        fun get(context: Context): App = context.applicationContext as App

        @JvmStatic
        fun getHistorian(context: Context): Historian = get(context).historian
    }
}
