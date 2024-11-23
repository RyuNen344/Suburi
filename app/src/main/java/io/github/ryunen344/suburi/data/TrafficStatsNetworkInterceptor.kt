package io.github.ryunen344.suburi.data

import android.net.TrafficStats
import okhttp3.Interceptor
import okhttp3.Response

internal class TrafficStatsNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
        return try {
            chain.proceed(chain.request())
        } finally {
            TrafficStats.clearThreadStatsTag()
        }
    }
}
