package io.github.ryunen344.suburi.startup

import android.content.Context
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import io.github.ryunen344.suburi.util.timber.ChunkedDebugTree
import io.github.ryunen344.suburi.util.timber.NoopTree
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val tree = if (BuildConfig.DEBUG) ChunkedDebugTree() else NoopTree()
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
}
