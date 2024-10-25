package io.github.ryunen344.suburi.startup

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import timber.log.Timber

class StrictModeInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            // Thread
            val oldThreadPolicy = StrictMode.getThreadPolicy()
            runCatching {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .apply {
                            detectAll()
                            penaltyLog()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                penaltyListener(ContextCompat.getMainExecutor(context), Timber::e)
                            }
                        }
                        .build()
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
                            penaltyLog()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                penaltyListener(ContextCompat.getMainExecutor(context), Timber::e)
                            }
                        }
                        .build()
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
