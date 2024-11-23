package io.github.ryunen344.suburi.data

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

internal class TimberHttpLoggingInterceptorLogger(
    val tag: String = "OkHttp",
    val priority: Int = Log.DEBUG,
) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Timber.tag(tag).log(priority, message)
    }
}
