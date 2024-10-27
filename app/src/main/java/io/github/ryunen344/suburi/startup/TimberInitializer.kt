package io.github.ryunen344.suburi.startup

import android.content.Context
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val tree = if (BuildConfig.DEBUG) Timber.DebugTree() else NoopTree()
        Timber.plant(tree)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Timber.tag("TimberAndroidRuntime").e(e)
            defaultHandler?.uncaughtException(t, e)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    private class NoopTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // noop
        }
    }
}
