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

package io.github.ryunen344.suburi.test.rules

import okhttp3.internal.closeQuietly
import okhttp3.internal.concurrent.TaskRunner
import okhttp3.internal.http2.Http2
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.ExternalResource
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.logging.ConsoleHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.reflect.KClass

class MockWebServerRule : ExternalResource() {

    val server: MockWebServer = MockWebServer()

    private val closeables = mutableListOf<Closeable>()

    override fun before() {
        try {
            closeables.add(OkHttpDebugLogging.enable(MockWebServer::class))
            closeables.add(OkHttpDebugLogging.enableHttp2())
            closeables.add(OkHttpDebugLogging.enableTaskRunner())
            server.start()
        } catch (e: IOException) {
            @Suppress("TooGenericExceptionThrown")
            throw RuntimeException(e)
        }
    }

    override fun after() {
        try {
            server.shutdown()
            closeables.forEach(Closeable::closeQuietly)
        } catch (e: IOException) {
            logger.log(Level.WARNING, "MockWebServer shutdown failed", e)
        }
    }

    /**
     * https://github.com/square/okhttp/blob/master/okhttp-testing-support/src/main/kotlin/okhttp3/OkHttpDebugLogging.kt
     */
    object OkHttpDebugLogging {
        // Keep references to loggers to prevent their configuration from being GC'd.
        private val configuredLoggers = CopyOnWriteArraySet<Logger>()

        fun enableHttp2() = enable(Http2::class)

        fun enableTaskRunner() = enable(TaskRunner::class)

        fun logHandler() =
            ConsoleHandler().apply {
                level = Level.FINE
                formatter =
                    object : SimpleFormatter() {
                        override fun format(record: LogRecord) = "[%1\$tF %1\$tT] %2\$s %n".format(record.millis, record.message)
                    }
            }

        fun enable(
            loggerClass: String,
            handler: Handler = logHandler(),
        ): Closeable {
            val logger = Logger.getLogger(loggerClass)
            if (configuredLoggers.add(logger)) {
                logger.addHandler(handler)
                logger.level = Level.FINEST
            }
            return Closeable {
                logger.removeHandler(handler)
            }
        }

        fun enable(loggerClass: KClass<*>) = enable(loggerClass.java.name)
    }

    private companion object {
        private val logger = Logger.getLogger(MockWebServerRule::class.java.name)
    }
}
