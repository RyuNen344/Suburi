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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.ktor.http.ContentType
import io.ktor.http.content.ChannelWriterContent
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.JsonConvertException
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.io.readByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.modules.SerializersModule
import org.junit.Before
import org.junit.Test

class OkioJsonConverterTest {

    private lateinit var serializer: OkioJsonConverter

    @Before
    fun setup() {
        serializer = OkioJsonConverter(Json.Default)
    }

    @Test
    fun testMaps_givenFully_thenSuccess() = runTest {
        val data = mapOf(
            "a" to "1",
            "b" to "2",
        )
        val string = """{"a":"1","b":"2"}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<Map<String, String>>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testMaps_givenNullValue_thenSuccess() = runTest {
        val data = mapOf(
            "a" to "1",
            "b" to null,
        )
        val string = """{"a":"1","b":null}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<Map<String, String?>>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testMaps_givenNullKey_thenSuccess() = runTest {
        val data = mapOf(
            "a" to "1",
            null to "2",
        )
        val string = """{"a":"1",null:"2"}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<Map<String?, String>>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testMaps_givenUnquoteNumber_thenFails() = runTest {
        val data = mapOf(
            "a" to "1",
            "b" to 2,
        )
        val string = """{"a":"1","b":2}"""

        @Suppress("MaxLineLength")
        assertFailure { serializer.testSerialize<Map<String, Any>>(data) }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage(
                "Serializing collections of different element types is not yet supported. Selected serializers: [kotlin.String, kotlin.Int]",
            )

        @Suppress("MaxLineLength")
        assertFailure { serializer.testDeserialize<Map<String, Any>>(ByteReadChannel(string)) }
            .isInstanceOf(SerializationException::class)
            .hasMessage(
                "Serializer for class 'Map' is not found.\nPlease ensure that class is marked as '@Serializable' and that the serialization compiler plugin is applied.\n",
            )
    }

    @Test
    fun testStruct_thenSuccess() = runTest {
        val data = User(
            id = 1,
            login = "test_user",
        )
        val string = """{"id":1,"login":"test_user"}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<User>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testList_givenUsers_thenSuccess() = runTest {
        val data = listOf(
            User(id = 1, login = "test_user_1"),
            User(id = 2, login = "test_user_2"),
        )
        val string = """[{"id":1,"login":"test_user_1"},{"id":2,"login":"test_user_2"}]"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<List<User>>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testList_givenPhotos_thenSuccess() = runTest {
        val data = listOf(
            Photo(id = 1, path = "https://example.com/photo1.jpg"),
            Photo(id = 2, path = "https://example.com/photo2.jpg"),
        )
        val string = """[{"id":1,"path":"https://example.com/photo1.jpg"},{"id":2,"path":"https://example.com/photo2.jpg"}]"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<List<Photo>>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testJsonElement_givenFully_thenSuccess() = runTest {
        val data = buildJsonObject {
            put("a", "1")
            put(
                "b",
                buildJsonArray {
                    add("c")
                    add(JsonPrimitive(2))
                },
            )
        }
        val string = """{"a":"1","b":["c",2]}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<JsonObject>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testJsonElement_givenNullValue_thenSuccess() = runTest {
        val data = buildJsonObject {
            put("a", "1")
            put(
                "b",
                buildJsonObject {
                    put("c", 3)
                },
            )
            put("x", JsonNull)
        }
        val string = """{"a":"1","b":{"c":3},"x":null}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        val deserialized = serializer.testDeserialize<JsonObject>(ByteReadChannel(string))
        assertThat(deserialized)
            .isNotNull()
            .isEqualTo(data)
    }

    @Test
    fun testContextual_thenSuccess() = runTest {
        serializer = OkioJsonConverter(
            Json {
                prettyPrint = true
                encodeDefaults = true
                serializersModule =
                    SerializersModule {
                        contextual(Either::class) { serializers: List<KSerializer<*>> ->
                            EitherSerializer(serializers[0], serializers[1])
                        }
                    }
            },
        )

        val eitherDogData = Either.Right(DogDTO(8, "Auri"))
        // language=json
        val eitherDogString = """
            {
                "age": 8,
                "name": "Auri"
            }
        """.trimIndent()

        val eitherDogSerialized = serializer.testSerialize<Either<ErrorDTO, DogDTO>>(eitherDogData)
        assertThat(eitherDogSerialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(eitherDogString)

        val eitherDogDeserialized = serializer.testDeserialize<Either<ErrorDTO, DogDTO>>(ByteReadChannel(eitherDogString))
        assertThat(eitherDogDeserialized)
            .isNotNull()
            .isEqualTo(eitherDogData)

        val eitherErrorData = Either.Left(ErrorDTO("Some error"))
        // language=json
        val eitherErrorString = """
            {
                "message": "Some error"
            }
        """.trimIndent()

        val eitherErrorSerialized = serializer.testSerialize<Either<ErrorDTO, DogDTO>>(eitherErrorData)
        assertThat(eitherErrorSerialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(eitherErrorString)

        val eitherErrorDeserialized = serializer.testDeserialize<Either<ErrorDTO, DogDTO>>(ByteReadChannel(eitherErrorString))
        assertThat(eitherErrorDeserialized)
            .isNotNull()
            .isEqualTo(eitherErrorData)

        val eitherEmptyErrorData = Either.Left(ErrorDTO())
        // language=json
        val eitherEmptyErrorString = """{}""".trimIndent()

        val eitherEmptyErrorSerialized = serializer.testSerialize<Either<ErrorDTO, DogDTO>>(eitherEmptyErrorData)
        assertThat(eitherEmptyErrorSerialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(
                """
                {
                    "message": "Some default error"
                }
                """.trimIndent(),
            )

        val eitherEmptyErrorDeserialized = serializer.testDeserialize<Either<ErrorDTO, DogDTO>>(ByteReadChannel(eitherEmptyErrorString))
        assertThat(eitherEmptyErrorDeserialized)
            .isNotNull()
            .isEqualTo(eitherEmptyErrorData)
    }

    @Test
    fun testExtraFields_thenIgnoreExtraField() = runTest {
        val data = DogDTO(8, "Auri")
        val string = """{"age":8,"name":"Auri"}"""
        val extraString = """{"age":8,"name":"Auri","color":"Black"}"""

        val serialized = serializer.testSerialize(data)
        assertThat(serialized)
            .transform("json", ByteArray::decodeToString)
            .isEqualTo(string)

        @Suppress("MaxLineLength")
        assertFailure { serializer.testDeserialize<DogDTO>(ByteReadChannel(extraString)) }
            .isInstanceOf(JsonConvertException::class)
            .hasMessage(
                "Illegal input: Encountered an unknown key 'color' at offset 24 at path: $\nUse 'ignoreUnknownKeys = true' in 'Json {}' builder or '@JsonIgnoreUnknownKeys' annotation to ignore unknown keys.\nJSON input: {\"age\":8,\"name\":\"Auri\",\"color\":\"Black\"}",
            )
    }

    @Test
    fun testSequence_thenFails() = runTest {
        val data = sequenceOf(DogDTO(8, "Auri"))
        val string = """[{"age":8,"name":"Auri"}]"""

        assertFailure { serializer.testSerialize<Sequence<DogDTO>>(data) }
            .isInstanceOf(SerializationException::class)

        assertFailure { serializer.testDeserialize<Sequence<DogDTO>>(ByteReadChannel(string)) }
            .isInstanceOf(JsonConvertException::class)
    }

    private suspend inline fun <reified T : Any> ContentConverter.testSerialize(data: T): ByteArray {
        return when (val content = serialize(ContentType.Application.Json, Charsets.UTF_8, typeInfo<T>(), data)) {
            is OutgoingContent.ByteArrayContent -> content.bytes()
            is ChannelWriterContent -> {
                val channel = ByteChannel()
                GlobalScope.launch {
                    content.writeTo(channel)
                    channel.close()
                }
                channel.readRemaining().readByteArray()
            }

            else -> error("Failed to get serialized $data")
        }
    }

    private suspend inline fun <reified T : Any> ContentConverter.testDeserialize(content: ByteReadChannel): T? {
        return deserialize(Charsets.UTF_8, typeInfo<T>(), content) as? T
    }

    @Serializable
    data class User(val id: Long, val login: String)

    @Serializable
    data class Photo(val id: Long, val path: String)

    @Serializable
    data class DogDTO(val age: Int, val name: String)

    @Serializable
    data class ErrorDTO(val message: String = "Some default error")

    sealed class Either<out L, out R> {

        data class Left<out L>(val left: L) : Either<L, Nothing>()

        data class Right<out R>(val right: R) : Either<Nothing, R>()
    }

    class EitherSerializer<L, R>(
        private val leftSerializer: KSerializer<L>,
        private val rightSerializer: KSerializer<R>,
    ) : KSerializer<Either<L, R>> {

        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("NetworkEitherSerializer") {
                element("left", leftSerializer.descriptor)
                element("right", rightSerializer.descriptor)
            }

        override fun deserialize(decoder: Decoder): Either<L, R> {
            require(decoder is JsonDecoder) { "only works in JSON format" }
            val element: JsonElement = decoder.decodeJsonElement()

            return try {
                Either.Right(decoder.json.decodeFromJsonElement(rightSerializer, element))
            } catch (_: Throwable) {
                Either.Left(decoder.json.decodeFromJsonElement(leftSerializer, element))
            }
        }

        override fun serialize(encoder: Encoder, value: Either<L, R>) {
            when (value) {
                is Either.Left -> encoder.encodeSerializableValue(leftSerializer, value.left)
                is Either.Right -> encoder.encodeSerializableValue(rightSerializer, value.right)
            }
        }
    }
}
