package io.github.ryunen344.suburi.data

import android.net.TrafficStats
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Protocol
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * @see <a href="https://github.com/square/okhttp/issues/3537">https://github.com/square/okhttp/issues/3537</a>
 */
internal class TrafficStatsEventListener : EventListener() {
    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
    }

    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
        TrafficStats.clearThreadStatsTag()
    }
}
