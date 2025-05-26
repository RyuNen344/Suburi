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
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.support.fail
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

    private fun Assert<String>.isMatchTo(regex: String) = given { value ->
        if (!value.matches(Regex(regex))) {
            fail(regex, value)
        }
    }

    @Test
    fun testLevel_thenReturnNone() {
        assertThat(interceptor.level).isEqualTo(HttpLoggingInterceptor.Level.NONE)
    }

    @Test
    fun testLog_givenNone_whenRequestsGet_thenLogsNothing() {
        setLevel(HttpLoggingInterceptor.Level.NONE)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().build()).execute().closeQuietly()
        assertThat(record.logs).isEmpty()
    }

    @Test
    fun testLog_givenBasic_whenRequestsGet_thenLogsMethod() {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().get().build()).execute().closeQuietly()
        assertThat(record.logs).all {
            hasSize(2)
            index(0).prop(LogData::message).isEqualTo("--> GET $url http/1.1")
            index(1).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms, 0-byte body\)""")
        }
    }

    @Test
    fun testLog_givenBasic_whenRequestsPost_thenLogsRequestBodyLength() {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().post("Hi?".toRequestBody(PLAIN)).build()).execute().closeQuietly()
        assertThat(record.logs).all {
            hasSize(2)
            index(0).prop(LogData::message).isEqualTo("--> POST $url http/1.1 (3-byte body)")
            index(1).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms, 0-byte body\)""")
        }
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
        assertThat(record.logs).all {
            hasSize(2)
            index(0).prop(LogData::message).isEqualTo("--> GET $url http/1.1")
            index(1).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms, 6-byte body\)""")
        }
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
        assertThat(record.logs).all {
            hasSize(2)
            index(0).prop(LogData::message).isEqualTo("--> GET $url http/1.1")
            index(1).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms, unknown-length body\)""")
        }
    }

    @Test
    fun testLog_givenHeader_whenRequestsGet_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().get().build()).execute().closeQuietly()
        assertThat(record.logs).all {
            hasSize(9)
            index(0).prop(LogData::message).isEqualTo("--> GET $url http/1.1")
            index(1).prop(LogData::message).isEqualTo("Host: $host")
            index(2).prop(LogData::message).isEqualTo("Connection: Keep-Alive")
            index(3).prop(LogData::message).isEqualTo("Accept-Encoding: gzip")
            index(4).prop(LogData::message).isMatchTo("""User-Agent: okhttp/.+""")
            index(5).prop(LogData::message).isEqualTo("--> END GET")
            index(6).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms\)""")
            index(7).prop(LogData::message).isEqualTo("Content-Length: 0")
            index(8).prop(LogData::message).isEqualTo("<-- END HTTP")
        }
    }

    @Test
    fun testLog_givenHeader_whenRequestsPost_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().post("Hi?".toRequestBody(PLAIN)).build()).execute().closeQuietly()
        assertThat(record.logs).all {
            hasSize(11)
            index(0).prop(LogData::message).isEqualTo("--> POST $url http/1.1")
            index(1).prop(LogData::message).isEqualTo("Content-Type: text/plain; charset=utf-8")
            index(2).prop(LogData::message).isEqualTo("Content-Length: 3")
            index(3).prop(LogData::message).isEqualTo("Host: $host")
            index(4).prop(LogData::message).isEqualTo("Connection: Keep-Alive")
            index(5).prop(LogData::message).isEqualTo("Accept-Encoding: gzip")
            index(6).prop(LogData::message).isMatchTo("""User-Agent: okhttp/.+""")
            index(7).prop(LogData::message).isEqualTo("--> END POST")
            index(8).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms\)""")
            index(9).prop(LogData::message).isEqualTo("Content-Length: 0")
            index(10).prop(LogData::message).isEqualTo("<-- END HTTP")
        }
    }

    @Test
    fun testLog_givenHeader_whenRequestsPost_withNoContentType_thenLogsHeaders() {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().post("Hi?".toRequestBody(null)).build()).execute().closeQuietly()
        assertThat(record.logs).all {
            hasSize(10)
            index(0).prop(LogData::message).isEqualTo("--> POST $url http/1.1")
            index(1).prop(LogData::message).isEqualTo("Content-Length: 3")
            index(2).prop(LogData::message).isEqualTo("Host: $host")
            index(3).prop(LogData::message).isEqualTo("Connection: Keep-Alive")
            index(4).prop(LogData::message).isEqualTo("Accept-Encoding: gzip")
            index(5).prop(LogData::message).isMatchTo("""User-Agent: okhttp/.+""")
            index(6).prop(LogData::message).isEqualTo("--> END POST")
            index(7).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms\)""")
            index(8).prop(LogData::message).isEqualTo("Content-Length: 0")
            index(9).prop(LogData::message).isEqualTo("<-- END HTTP")
        }
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
        assertThat(record.logs).all {
            hasSize(11)
            index(0).prop(LogData::message).isEqualTo("--> POST $url http/1.1")
            index(1).prop(LogData::message).isEqualTo("Content-Type: text/plain; charset=utf-8")
            index(2).prop(LogData::message).isEqualTo("Transfer-Encoding: chunked")
            index(3).prop(LogData::message).isEqualTo("Host: $host")
            index(4).prop(LogData::message).isEqualTo("Connection: Keep-Alive")
            index(5).prop(LogData::message).isEqualTo("Accept-Encoding: gzip")
            index(6).prop(LogData::message).isMatchTo("""User-Agent: okhttp/.+""")
            index(7).prop(LogData::message).isEqualTo("--> END POST")
            index(8).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms\)""")
            index(9).prop(LogData::message).isEqualTo("Content-Length: 0")
            index(10).prop(LogData::message).isEqualTo("<-- END HTTP")
        }
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
        assertThat(record.logs).all {
            hasSize(10)
            index(0).prop(LogData::message).isEqualTo("--> GET $url http/1.1")
            index(1).prop(LogData::message).isEqualTo("Host: $host")
            index(2).prop(LogData::message).isEqualTo("Connection: Keep-Alive")
            index(3).prop(LogData::message).isEqualTo("Accept-Encoding: gzip")
            index(4).prop(LogData::message).isMatchTo("""User-Agent: okhttp/.+""")
            index(5).prop(LogData::message).isEqualTo("--> END GET")
            index(6).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms\)""")
            index(7).prop(LogData::message).isEqualTo("Content-Length: 6")
            index(8).prop(LogData::message).isEqualTo("Content-Type: text/plain; charset=utf-8")
            index(9).prop(LogData::message).isEqualTo("<-- END HTTP")
        }
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
        assertThat(record.logs).all {
            hasSize(10)
            index(0).prop(LogData::message).isEqualTo("--> GET $url http/1.1")
            index(1).prop(LogData::message).isEqualTo("Host: $host")
            index(2).prop(LogData::message).isEqualTo("Connection: Keep-Alive")
            index(3).prop(LogData::message).isEqualTo("Accept-Encoding: gzip")
            index(4).prop(LogData::message).isMatchTo("""User-Agent: okhttp/.+""")
            index(5).prop(LogData::message).isEqualTo("--> END GET")
            index(6).prop(LogData::message).isMatchTo("""<-- 200 OK $url \(\d+ms\)""")
            index(7).prop(LogData::message).isEqualTo("Transfer-encoding: chunked")
            index(8).prop(LogData::message).isEqualTo("Content-Type: text/plain; charset=utf-8")
            index(9).prop(LogData::message).isEqualTo("<-- END HTTP")
        }
    }

    private class RecordingTree : Timber.Tree() {
        private val _logs = mutableListOf<LogData>()
        val logs: List<LogData>
            get() = _logs.toList()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            _logs.add(LogData(priority, tag, message, t))
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
