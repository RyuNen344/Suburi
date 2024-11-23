package io.github.ryunen344.suburi.startup

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.reflect.typeOf

class StrictModeInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            // Thread
            val oldThreadPolicy = StrictMode.getThreadPolicy()
            runCatching {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .apply {
                            runBlocking(newFixedThreadPoolContext(1, "StrictModeInitializer")) {
                                typeOf<StrictModeInitializer>()
                            }
                            detectAll()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                penaltyListener(ContextCompat.getMainExecutor(context), Timber::w)
                            } else {
                                penaltyLog()
                            }
                        }
                        .build(),
                )
            }.onFailure {
                Timber.e(it)
                StrictMode.setThreadPolicy(oldThreadPolicy)
            }
            // VM
            val oldVmPolicy = StrictMode.getVmPolicy()
            runCatching {
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .apply {
                            detectAll()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                penaltyListener(ContextCompat.getMainExecutor(context), Timber::w)
                            } else {
                                penaltyLog()
                            }
                        }
                        .build(),
                )
            }.onFailure {
                Timber.e(it)
                StrictMode.setVmPolicy(oldVmPolicy)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(TimberInitializer::class.java)
    }
}
