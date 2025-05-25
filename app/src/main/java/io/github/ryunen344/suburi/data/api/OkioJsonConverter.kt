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

package io.github.ryunen344.suburi.data.api

import io.ktor.http.ContentType
import io.ktor.http.content.ChannelWriterContent
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.guessSerializer
import io.ktor.serialization.kotlinx.serializerForTypeInfo
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.rethrowCloseCauseIfNeeded
import io.ktor.utils.io.streams.inputStream
import kotlinx.io.RawSource
import kotlinx.io.asOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.Buffer
import okio.BufferedSource

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, InternalAPI::class)
class OkioJsonConverter(private val format: Json) : ContentConverter {

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?,
    ): OutgoingContent {
        val serializer = try {
            format.serializersModule.serializerForTypeInfo(typeInfo)
        } catch (_: SerializationException) {
            guessSerializer(value, format.serializersModule)
        }

        val buffer = Buffer().apply {
            format.encodeToBufferedSink(
                serializer = serializer as KSerializer<Any?>,
                value = value,
                sink = this,
            )
        }

        // FIXME: can't see the content in logcat, because HttpLoggingInterceptor doesn't log the body of StreamRequestBody(ReadChannelContent, WriteChannelContent)
        return ChannelWriterContent(
            body = {
                writeBuffer.transferFrom(
                    object : RawSource {
                        override fun readAtMostTo(sink: kotlinx.io.Buffer, byteCount: Long): Long {
                            require(byteCount >= 0) { "byteCount ($byteCount) < 0" }
                            if (buffer.size == 0L) return -1L
                            val bytesWritten = if (byteCount > buffer.size) buffer.size else byteCount
                            buffer.writeTo(sink.asOutputStream(), bytesWritten)
                            return bytesWritten
                        }

                        override fun close() {
                            buffer.close()
                        }
                    },
                )
            },
            contentType = contentType,
            contentLength = buffer.size,
        )
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel,
    ): Any? {
        val serializer = format.serializersModule.serializerForTypeInfo(typeInfo)
        val contentPacket = content.readRemaining()
        return try {
            format.decodeFromBufferedSource(
                serializer,
                contentPacket,
            )
        } catch (@Suppress("TooGenericExceptionCaught") cause: Throwable) {
            throw JsonConvertException("Illegal input: ${cause.message}", cause)
        }
    }

    private suspend fun ByteReadChannel.readRemaining(): BufferedSource {
        val result = Buffer()
        val input = readBuffer.inputStream()
        while (!isClosedForRead) {
            result.readFrom(input)
            awaitContent()
        }
        rethrowCloseCauseIfNeeded()
        return result
    }
}
