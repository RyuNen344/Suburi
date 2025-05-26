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

import android.util.Log
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.prop
import assertk.fail
import io.github.ryunen344.suburi.test.rules.MockWebServerRule
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.closeQuietly
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okio.BufferedSink
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class TimberHttpLoggingInterceptorTest {

    @get:Rule
    val serverRule = MockWebServerRule()

    private val interceptor = TimberHttpLoggingInterceptor()

    private lateinit var record: RecordingTree
    private lateinit var client: OkHttpClient
    private lateinit var host: String
    private lateinit var url: HttpUrl

    @Before
    fun setup() {
        record = RecordingTree()
        Timber.plant(record)

        client = OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .build()

        host = "${serverRule.server.hostName}:${serverRule.server.port}"
        url = serverRule.server.url("/")
    }

    @After
    fun cleanup() {
        Timber.uprootAll()
    }

    private fun setLevel(level: HttpLoggingInterceptor.Level) {
        interceptor.level = level
    }

    private fun request(): Request.Builder = Request.Builder().url(url)

    @Test
    fun testLevel_thenReturnNone() {
        assertThat(interceptor.level).isEqualTo(HttpLoggingInterceptor.Level.NONE)
    }

    @Test
    fun testLog_givenNone_whenRequestsGet_thenLogsNothing() {
        setLevel(HttpLoggingInterceptor.Level.NONE)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().build()).execute().closeQuietly()
        record.assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBasic_whenRequestsGet_thenLogsMethod() {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().get().build()).execute().closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms, 0-byte body\)""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBasic_whenRequestsPost_thenLogsRequestBodyLength() {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().post("Hi?".toRequestBody(PLAIN)).build()).execute().closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1 (3-byte body)")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms, 0-byte body\)""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBasic_whenResponseHasBody_thenLogsResponseBodyLength() {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
        serverRule.server.enqueue(
            MockResponse()
                .setBody("Hello!")
                .setHeader("Content-Type", PLAIN),
        )
        client.newCall(request().get().build()).execute().closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms, 6-byte body\)""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBasic_whenResponseHasChunkedBody_thenLogsResponseBodyLength() {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
        serverRule.server.enqueue(
            MockResponse()
                .setChunkedBody("Hello!", 2)
                .setHeader("Content-Type", PLAIN),
        )
        client.newCall(request().get().build()).execute().closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms, unknown-length body\)""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenHeader_whenRequestsGet_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().get().build()).execute().closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 0")
            .assertLogEqual("<-- END HTTP")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenHeader_whenRequestsPost_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().post("Hi?".toRequestBody(PLAIN)).build()).execute().closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogEqual("Content-Length: 3")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END POST")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 0")
            .assertLogEqual("<-- END HTTP")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenHeader_whenRequestsPost_withNoContentType_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().post("Hi?".toRequestBody(null)).build()).execute().closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1")
            .assertLogEqual("Content-Length: 3")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END POST")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 0")
            .assertLogEqual("<-- END HTTP")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenHeader_whenRequestsPost_withNoLength_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        val body = object : RequestBody() {
            override fun contentType() = PLAIN
            override fun writeTo(sink: BufferedSink) {
                sink.writeUtf8("Hi!")
            }
        }
        client.newCall(request().post(body).build()).execute().closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogEqual("Transfer-Encoding: chunked")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END POST")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 0")
            .assertLogEqual("<-- END HTTP")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenHeader_whenResponseHasBody_thenLogsResponseHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(
            MockResponse()
                .setBody("Hello!")
                .setHeader("Content-Type", PLAIN),
        )
        client.newCall(request().get().build()).execute().closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 6")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogEqual("<-- END HTTP")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenHeader_whenResponseHasChunkedBody_thenLogsResponseHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(
            MockResponse()
                .setChunkedBody("Hello!", 2)
                .setHeader("Content-Type", PLAIN),
        )
        client.newCall(request().get().build()).execute().closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Transfer-encoding: chunked")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogEqual("<-- END HTTP")
            .assertNoMoreLogs()
    }

    private class RecordingTree : Timber.Tree() {
        private val _logs = mutableListOf<LogData>()
        val logs: List<LogData>
            get() = _logs.toList()

        private var index = 0

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            _logs.add(LogData(priority, tag, message, t))
        }

        fun assertLogEqual(expected: String) = apply {
            assertThat(index).isLessThan(logs.size)
            assertThat(logs).index(index++)
                .prop(LogData::message)
                .isEqualTo(expected)
        }

        fun assertLogMatch(regex: String) = apply {
            assertThat(index).isLessThan(logs.size)
            assertThat(logs).index(index++)
                .prop(LogData::message)
                .given { value ->
                    if (!value.matches(Regex(regex))) {
                        fail(regex, value)
                    }
                }
        }

        fun assertNoMoreLogs() {
            assertThat(logs.size).isEqualTo(index)
        }
    }

    private data class LogData(
        val priority: Int = Log.DEBUG,
        val tag: String? = "OkHttp",
        val message: String,
        val t: Throwable? = null,
    )

    private companion object {
        private val PLAIN = "text/plain; charset=utf-8".toMediaType()
    }
}
