package io.github.ryunen344.suburi.startup

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import timber.log.Timber
import java.util.concurrent.Executors

class VmPolicyInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            val oldVmPolicy = StrictMode.getVmPolicy()
            runCatching {
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .apply {
                            detectAll()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                penaltyListener(Executors.newSingleThreadExecutor(), Timber::w)
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
