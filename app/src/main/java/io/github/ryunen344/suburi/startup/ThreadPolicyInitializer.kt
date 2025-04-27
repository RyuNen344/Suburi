package io.github.ryunen344.suburi.startup

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.reflect.typeOf

class ThreadPolicyInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            val oldThreadPolicy = StrictMode.getThreadPolicy()
            runCatching {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .apply {
                            val dispatcher = newFixedThreadPoolContext(1, "ThreadPolicyInitializer")
                            runBlocking(dispatcher) {
                                typeOf<ThreadPolicyInitializer>()
                                detectAll()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    penaltyListener(dispatcher.executor, Timber::w)
                                } else {
                                    penaltyLog()
                                }
                            }
                        }
                        .build(),
                )
            }.onFailure {
                Timber.e(it)
                StrictMode.setThreadPolicy(oldThreadPolicy)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(TimberInitializer::class.java)
    }
}
