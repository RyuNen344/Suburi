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
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.isNotNull
import assertk.assertions.prop
import assertk.assertions.support.fail
import io.github.ryunen344.suburi.test.rules.MockWebServerRule
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okio.Buffer
import okio.BufferedSink
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import okio.GzipSink
import okio.IOException
import okio.buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import java.net.InetAddress
import java.net.UnknownHostException

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

    // region: level none
    @Test
    fun testLevel_thenReturnNone() {
        assertThat(interceptor.level).isEqualTo(HttpLoggingInterceptor.Level.NONE)
    }

    @Test
    fun testLog_givenNone_whenRequestsGet_thenLogsNothing() {
        setLevel(HttpLoggingInterceptor.Level.NONE)
        // get with empty response
        serverRule.server.enqueue(MockResponse())
        client.newCall(request().build()).execute().closeQuietly()

        // post with plain response
        serverRule.server.enqueue(
            MockResponse()
                .setBody("Hello!")
                .setHeader("Content-Type", PLAIN),
        )
        client.newCall(request().post("Hi?".toRequestBody(PLAIN)).build()).execute().closeQuietly()
        record.assertNoMoreLogs()
    }
    // endregion

    // region: level basic
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
    // endregion

    // region: level headers
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
    // endregion

    // region: level body
    @Test
    fun testLog_givenBody_whenRequestsPost_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setBody("Hello!")
                .setHeader("Content-Type", PLAIN),
        )
        val response = client.newCall(request().post("Hi?".toRequestBody(PLAIN)).build()).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::string)
            .isEqualTo("Hello!")
        response.closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogEqual("Content-Length: 3")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("\nHi?\n--> END POST (3-byte body)\n")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 6")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogMatch("""\nHello!\n<-- END HTTP \(\d+ms, 6-byte body\)\n""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenRequestHasBinary_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setBody("Hello!")
                .setHeader("Content-Type", PLAIN),
        )

        val buffer = Buffer()
        buffer.writeUtf8CodePoint(0x89)
        buffer.writeUtf8CodePoint(0x50)
        buffer.writeUtf8CodePoint(0x4e)
        buffer.writeUtf8CodePoint(0x47)
        buffer.writeUtf8CodePoint(0x0d)
        buffer.writeUtf8CodePoint(0x0a)
        buffer.writeUtf8CodePoint(0x1a)
        buffer.writeUtf8CodePoint(0x0a)

        val response = client.newCall(
            request()
                .post(
                    buffer
                        .readByteString()
                        .toRequestBody("image/png; charset=utf-8".toMediaType()),
                )
                .build(),
        ).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::string)
            .isEqualTo("Hello!")
        response.closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1")
            .assertLogEqual("Content-Type: image/png; charset=utf-8")
            .assertLogEqual("Content-Length: 9")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("\n--> END POST (binary 9-byte body omitted)\n")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 6")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogMatch("""\nHello!\n<-- END HTTP \(\d+ms, 6-byte body\)\n""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenResponseHasChunkedBody_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setChunkedBody("Hello!", 2)
                .setHeader("Content-Type", PLAIN),
        )
        val response = client.newCall(request().build()).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::string)
            .isEqualTo("Hello!")
        response.closeQuietly()
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
            .assertLogMatch("""\nHello!\n<-- END HTTP \(\d+ms, 6-byte body\)\n""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenRequestsGzipPost_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(Buffer().writeUtf8("Uncompressed"))
                .setHeader("Content-Type", PLAIN),
        )
        val request = request()
            .addHeader("Content-Encoding", "gzip")
            .post("GZIP Uncompressed".toRequestBody().gzip())
            .build()
        val response = client.newCall(request).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::string)
            .isEqualTo("Uncompressed")
        response.closeQuietly()
        record
            .assertLogEqual("--> POST $url http/1.1")
            .assertLogEqual("Content-Encoding: gzip")
            .assertLogEqual("Transfer-Encoding: chunked")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("\n--> END POST (17-byte, 37-gzipped-byte body)\n")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 12")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogMatch("""\nUncompressed\n<-- END HTTP \(\d+ms, 12-byte body\)\n""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenResponseHasGzip_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(Buffer().write(requireNotNull("H4sIAAAAAAAAAPNIzcnJ11HwQKIAdyO+9hMAAAA=".decodeBase64())))
                .setHeader("Content-Encoding", "gzip")
                .setHeader("Content-Type", PLAIN),
        )
        val response = client.newCall(request().build()).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::string)
            .isEqualTo("Hello, Hello, Hello")
        response.closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 29")
            .assertLogEqual("Content-Encoding: gzip")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogMatch("""\nHello, Hello, Hello\n<-- END HTTP \(\d+ms, 19-byte, 29-gzipped-byte body\)\n""")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenResponseHasUnknownEncoded_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(Buffer().write(requireNotNull("iwmASGVsbG8sIEhlbGxvLCBIZWxsbwoD".decodeBase64())))
                .setHeader("Content-Encoding", "br")
                .setHeader("Content-Type", PLAIN),
        )
        val response = client.newCall(request().build()).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::byteString)
            .isEqualTo("8b098048656c6c6f2c2048656c6c6f2c2048656c6c6f0a03".decodeHex())
        response.closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 24")
            .assertLogEqual("Content-Encoding: br")
            .assertLogEqual("Content-Type: text/plain; charset=utf-8")
            .assertLogEqual("<-- END HTTP (encoded body omitted)")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenResponseIsStreaming_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        serverRule.server.enqueue(
            MockResponse()
                .setChunkedBody(
                    """
                    |event: add
                    |data: 73857293
                    |
                    |event: remove
                    |data: 2153
                    |
                    |event: add
                    |data: 113411
                    |
                    |
                    """.trimMargin(),
                    8,
                )
                .setHeader("Content-Type", "text/event-stream"),
        )
        val response = client.newCall(request().build()).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::string)
            .isEqualTo("event: add\ndata: 73857293\n\nevent: remove\ndata: 2153\n\nevent: add\ndata: 113411\n\n")
        response.closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Transfer-encoding: chunked")
            .assertLogEqual("Content-Type: text/event-stream")
            .assertLogEqual("<-- END HTTP (streaming)")
            .assertNoMoreLogs()
    }

    @Test
    fun testLog_givenBody_whenResponseIsBinary_thenLogsBody() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        val buffer = Buffer()
        buffer.writeUtf8CodePoint(0x89)
        buffer.writeUtf8CodePoint(0x50)
        buffer.writeUtf8CodePoint(0x4e)
        buffer.writeUtf8CodePoint(0x47)
        buffer.writeUtf8CodePoint(0x0d)
        buffer.writeUtf8CodePoint(0x0a)
        buffer.writeUtf8CodePoint(0x1a)
        buffer.writeUtf8CodePoint(0x0a)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(buffer)
                .setHeader("Content-Type", "image/png; charset=utf-8"),
        )
        val response = client.newCall(request().build()).execute()
        assertThat(response.body)
            .isNotNull()
            .transform(name = "string", transform = ResponseBody::byteString)
            .isEqualTo("c289504e470d0a1a0a".decodeHex())
        response.closeQuietly()
        record
            .assertLogEqual("--> GET $url http/1.1")
            .assertLogEqual("Host: $host")
            .assertLogEqual("Connection: Keep-Alive")
            .assertLogEqual("Accept-Encoding: gzip")
            .assertLogMatch("""User-Agent: okhttp/.+""")
            .assertLogEqual("--> END GET")
            .assertLogMatch("""<-- 200 OK $url \(\d+ms\)""")
            .assertLogEqual("Content-Length: 9")
            .assertLogEqual("Content-Type: image/png; charset=utf-8")
            .assertLogMatch("""\n<-- END HTTP \(\d+ms, binary 9-byte body omitted\)\n""")
            .assertNoMoreLogs()
    }
    // endregion

    @Test
    fun testLog_whenThrowsException_thenLogsFailure() {
        setLevel(HttpLoggingInterceptor.Level.BODY)
        val exception = UnknownHostException("emulate DNS failure $host")
        assertFailure {
            client
                .newBuilder().apply {
                    networkInterceptors().clear()
                    addInterceptor(interceptor)
                }
                .dns(
                    object : Dns {
                        override fun lookup(hostname: String): List<InetAddress> {
                            throw exception
                        }
                    },
                )
                .build()
                .newCall(request().build())
                .execute()
                .closeQuietly()
        }.isEqualTo(exception)
        record
            .assertLogEqual("--> GET $url")
            .assertLogEqual("--> END GET")
            .assertLogEqual("<-- HTTP FAILED: java.net.UnknownHostException: emulate DNS failure $host")
            .assertNoMoreLogs()
    }

    private fun RequestBody.gzip(): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? = this@gzip.contentType()

            override fun contentLength(): Long {
                return -1 // We don't know the compressed length in advance!
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                GzipSink(sink).buffer().use(this@gzip::writeTo)
            }

            override fun isOneShot(): Boolean = this@gzip.isOneShot()
        }
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
            assertThat(logs)
                .index(index++)
                .prop(LogData::message)
                .run {
                    given { value ->
                        if (!value.matches(Regex(regex))) {
                            fail(regex, value)
                        }
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
