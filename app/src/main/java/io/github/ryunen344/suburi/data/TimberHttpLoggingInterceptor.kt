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
import io.github.ryunen344.suburi.util.isProbablyUtf8
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import okio.Buffer
import okio.GzipSource
import timber.log.Timber
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.TreeSet
import java.util.concurrent.TimeUnit

/**
 * An [Interceptor] that logs request and response information using [Timber] for logging.
 *
 * Differences from the default [HttpLoggingInterceptor]:
 * - Uses Timber for logging instead of Android's Log class.
 * - Allows customization of the log tag and priority.
 * - Encodes request bodies that are one-shot using [okhttp3.RequestBody.isOneShot].
 *
 * This interceptor has overhead for encoding request bodies, so it should only be used with [Level.NONE] or [Level.BASIC] or [Level.HEADERS] on production builds.
 *
 * @param tag The tag to use for logging. Default is "OkHttp".
 * @param priority The log priority. Default is [Log.DEBUG].
 */
class TimberHttpLoggingInterceptor(
    val tag: String = "OkHttp",
    val priority: Int = Log.DEBUG,
) : Interceptor {

    /**
     * see [HttpLoggingInterceptor.headersToRedact]
     */
    @Volatile
    private var headersToRedact = emptySet<String>()

    /**
     * see [HttpLoggingInterceptor.level]
     */
    @set:JvmName("level")
    @Volatile
    var level = Level.NONE

    /**
     * see [HttpLoggingInterceptor.redactHeader]
     */
    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact += headersToRedact
        newHeadersToRedact += name
        headersToRedact = newHeadersToRedact
    }

    /**
     * see [HttpLoggingInterceptor.intercept]
     */
    @Suppress("LongMethod", "NestedBlockDepth", "CyclomaticComplexMethod")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level

        val original = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(original)
        }

        val logBody = level == Level.BODY
        val logHeaders = level == Level.BODY || level == Level.HEADERS

        val requestBody = original.body
        var peekBody: RequestBody? = null

        val connection = chain.connection()

        val requestStartMessage = buildString {
            append("--> ${original.method} ${original.url}")
            if (connection != null) append(" ${connection.protocol()}")
            if (!logHeaders && requestBody != null) {
                append(" (${requestBody.contentLength()}-byte body)")
            }
        }
        Timber.tag(tag).log(priority, requestStartMessage)

        if (logHeaders) {
            val headers = original.headers

            if (requestBody != null) {
                requestBody.contentType()?.let {
                    if (headers["Content-Type"] == null) {
                        Timber.tag(tag).log(priority, "Content-Type: $it")
                    }
                }
                if (requestBody.contentLength() != -1L && headers["Content-Length"] == null) {
                    Timber.tag(tag).log(priority, "Content-Length: ${requestBody.contentLength()}")
                }
            }

            for (i in 0 until headers.size) {
                logHeader(headers, i)
            }

            val message = when {
                !logBody || requestBody == null -> "--> END ${original.method}"

                bodyHasUnknownEncoding(original.headers) -> "--> END ${original.method} (encoded body omitted)"

                requestBody.isDuplex() -> "--> END ${original.method} (duplex request body omitted)"

                else -> buildString {
                    var buffer = Buffer()
                    appendLine("")
                    requestBody.writeTo(buffer)

                    var gzippedLength: Long? = null
                    if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                        gzippedLength = buffer.size
                        GzipSource(buffer).use { gzippedResponseBody ->
                            buffer = Buffer()
                            buffer.writeAll(gzippedResponseBody)
                        }
                    }
                    val contentType = requestBody.contentType()
                    if (!buffer.isProbablyUtf8()) {
                        appendLine("--> END ${original.method} (binary ${requestBody.contentLength()}-byte body omitted)")
                    } else {
                        if (gzippedLength != null) {
                            appendLine("--> END ${original.method} (${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
                        } else {
                            val charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                            appendLine(buffer.clone().readString(charset))
                            appendLine("--> END ${original.method} (${buffer.size}-byte body)")
                        }
                    }
                    peekBody = buffer.readByteArray().toRequestBody(contentType)
                    buffer.clear()
                }
            }
            Timber.tag(tag).log(priority, message)
        }

        val startNs = System.nanoTime()
        val response = try {
            chain.proceed(
                if (peekBody == null) {
                    original
                } else {
                    original.newBuilder().method(original.method, peekBody).build()
                },
            )
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.tag(tag).log(priority, "<-- HTTP FAILED: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = requireNotNull(response.body) { "Response body is null for ${response.request.url}" }
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        Timber.tag(tag).log(
            priority,
            buildString {
                append("<-- ${response.code}")
                if (response.message.isNotEmpty()) {
                    append(" ${response.message}")
                }
                append(" ${response.request.url} (${tookMs}ms")
                if (!logHeaders) {
                    append(", $bodySize body")
                }
                append(")")
            },
        )

        if (logHeaders) {
            val headers = response.headers
            for (i in 0 until headers.size) {
                logHeader(headers, i)
            }

            val message = when {
                !logBody || !response.promisesBody() -> "<-- END HTTP"

                bodyHasUnknownEncoding(response.headers) -> "<-- END HTTP (encoded body omitted)"

                bodyIsStreaming(response) -> "<-- END HTTP (streaming)"

                else -> {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE) // Buffer the entire body.

                    val totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

                    var buffer = source.buffer

                    var gzippedLength: Long? = null
                    if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                        gzippedLength = buffer.size
                        GzipSource(buffer.clone()).use { gzippedResponseBody ->
                            buffer = Buffer()
                            buffer.writeAll(gzippedResponseBody)
                        }
                    }

                    val contentType = responseBody.contentType()
                    val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

                    buildString {
                        if (!buffer.isProbablyUtf8()) {
                            appendLine("")
                            appendLine("<-- END HTTP (${totalMs}ms, binary ${buffer.size}-byte body omitted)")
                        } else {
                            if (contentLength != 0L) {
                                appendLine("")
                                appendLine(buffer.clone().readString(charset))
                            }

                            if (gzippedLength != null) {
                                appendLine("<-- END HTTP (${totalMs}ms, ${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
                            } else {
                                appendLine("<-- END HTTP (${totalMs}ms, ${buffer.size}-byte body)")
                            }
                        }
                    }
                }
            }
            Timber.tag(tag).log(priority, message)
        }

        return response
    }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
        Timber.tag(tag).log(priority, headers.name(i) + ": " + value)
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
            !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun bodyIsStreaming(response: Response): Boolean {
        val contentType = response.body?.contentType()
        return contentType != null && contentType.type == "text" && contentType.subtype == "event-stream"
    }
}
