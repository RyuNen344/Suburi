/*
 * Copyright (C) 2025 RyuNen344
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE.md
 */

package io.github.ryunen344.suburi.data

import android.net.TrafficStats
import android.os.Build
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
        val currentThread = Thread.currentThread()
        val tag = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            currentThread.threadId().toInt()
        } else {
            @Suppress("DEPRECATION")
            currentThread.id.toInt()
        }
        TrafficStats.setThreadStatsTag(tag)
    }

    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
        TrafficStats.clearThreadStatsTag()
    }
}
