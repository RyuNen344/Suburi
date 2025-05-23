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

package io.github.ryunen344.suburi.navigation

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavType
import io.github.ryunen344.suburi.util.deserialize
import io.github.ryunen344.suburi.util.serialize
import timber.log.Timber
import java.util.Base64
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("FunctionName")
inline fun <reified T : java.io.Serializable> SerializableNavTypeMap(): Map<KType, SerializableNavType<T>> =
    mapOf(typeOf<T>() to SerializableNavType<T>())

@Suppress("FunctionName")
inline fun <reified T : java.io.Serializable> SerializableNavTypeMap(
    noinline onParseValue: ((String) -> T?),
): Map<KType, SerializableNavType<T>> = mapOf(typeOf<T>() to SerializableNavType<T>(onParseValue))

inline fun <reified T : java.io.Serializable> SerializableNavType() = SerializableNavType(T::class.java)

inline fun <reified T : java.io.Serializable> SerializableNavType(noinline onParseValue: ((String) -> T?)) =
    SerializableNavType(T::class.java, onParseValue)

class SerializableNavType<T : java.io.Serializable>(
    val clazz: Class<T>,
    private val onParseValue: ((String) -> T?)? = null,
) : NavType<T>(false) {

    override val name: String = clazz.name

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): T? {
        return BundleCompat.getSerializable(bundle, key, clazz)
    }

    override fun parseValue(value: String): T {
        Timber.d("parseValue $value")
        return onParseValue?.invoke(value) ?: Base64.getUrlDecoder().decode(value).deserialize()
    }

    override fun serializeAsValue(value: T): String {
        Timber.d("serializeAsValue $value")
        return Base64.getUrlEncoder().encodeToString(value.serialize())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializableNavType<*>

        if (clazz != other.clazz) return false
        if (onParseValue != other.onParseValue) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + (onParseValue?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }
}

@Suppress("FunctionName")
inline fun <reified T : java.io.Serializable> NullableSerializableNavTypeMap(): Map<KType, NullableSerializableNavType<T>> =
    mapOf(typeOf<T>() to NullableSerializableNavType<T>())

@Suppress("FunctionName")
inline fun <reified T : java.io.Serializable> NullableSerializableNavTypeMap(
    noinline onParseValue: ((String) -> T?),
): Map<KType, NullableSerializableNavType<T>> = mapOf(typeOf<T>() to NullableSerializableNavType<T>(onParseValue))

inline fun <reified T : java.io.Serializable> NullableSerializableNavType() = NullableSerializableNavType(T::class.java)

inline fun <reified T : java.io.Serializable> NullableSerializableNavType(noinline onParseValue: ((String) -> T?)) =
    NullableSerializableNavType(T::class.java, onParseValue)

class NullableSerializableNavType<T : java.io.Serializable>(
    val clazz: Class<T>,
    private val onParseValue: ((String) -> T?)? = null,
) : NavType<T?>(true) {

    override val name: String = clazz.name

    override fun put(bundle: Bundle, key: String, value: T?) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): T? {
        return BundleCompat.getSerializable(bundle, key, clazz)
    }

    override fun parseValue(value: String): T? {
        Timber.d("parseValue $value")
        return if (value == "null") null else onParseValue?.invoke(value) ?: Base64.getUrlDecoder().decode(value).deserialize()
    }

    override fun serializeAsValue(value: T?): String {
        Timber.d("serializeAsValue $value")
        return if (value == null) {
            "null"
        } else {
            Base64.getUrlEncoder().encodeToString(value.serialize())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NullableSerializableNavType<*>

        if (clazz != other.clazz) return false
        if (onParseValue != other.onParseValue) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + (onParseValue?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }
}
