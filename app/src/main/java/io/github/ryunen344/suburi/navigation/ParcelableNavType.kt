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
import android.os.Parcelable
import androidx.core.os.BundleCompat
import androidx.navigation.NavType
import io.github.ryunen344.suburi.util.parcel
import io.github.ryunen344.suburi.util.unparcel
import timber.log.Timber
import java.util.Base64
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("FunctionName")
inline fun <reified T : Parcelable> ParcelableNavTypeMap(): Map<KType, ParcelableNavType<T>> =
    mapOf(typeOf<T>() to ParcelableNavType<T>())

@Suppress("FunctionName")
inline fun <reified T : Parcelable> ParcelableNavTypeMap(
    noinline onParseValue: ((String) -> T?),
): Map<KType, ParcelableNavType<T>> = mapOf(typeOf<T>() to ParcelableNavType<T>(onParseValue))

inline fun <reified T : Parcelable> ParcelableNavType() = ParcelableNavType(T::class.java)

inline fun <reified T : Parcelable> ParcelableNavType(noinline onParseValue: ((String) -> T?)) =
    ParcelableNavType(T::class.java, onParseValue)

class ParcelableNavType<T : Parcelable>(
    val clazz: Class<T>,
    private val onParseValue: ((String) -> T?)? = null,
) : NavType<T>(false) {

    override val name: String = clazz.name

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): T? {
        return BundleCompat.getParcelable(bundle, key, clazz)
    }

    override fun parseValue(value: String): T {
        Timber.d("parseValue $value")
        return onParseValue?.invoke(value) ?: Base64.getUrlDecoder().decode(value).unparcel(clazz)
    }

    override fun serializeAsValue(value: T): String {
        Timber.d("serializeAsValue $value")
        return Base64.getUrlEncoder().encodeToString(value.parcel())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableNavType<*>

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
inline fun <reified T : Parcelable> NullableParcelableNavTypeMap(): Map<KType, NullableParcelableNavType<T>> =
    mapOf(typeOf<T>() to NullableParcelableNavType<T>())

@Suppress("FunctionName")
inline fun <reified T : Parcelable> NullableParcelableNavTypeMap(
    noinline onParseValue: ((String) -> T?),
): Map<KType, NullableParcelableNavType<T>> = mapOf(typeOf<T>() to NullableParcelableNavType<T>(onParseValue))

inline fun <reified T : Parcelable> NullableParcelableNavType() = NullableParcelableNavType(T::class.java)

inline fun <reified T : Parcelable> NullableParcelableNavType(noinline onParseValue: ((String) -> T?)) =
    NullableParcelableNavType(T::class.java, onParseValue)

class NullableParcelableNavType<T : Parcelable>(
    val clazz: Class<T>,
    private val onParseValue: ((String) -> T?)? = null,
) : NavType<T?>(true) {

    override val name: String = clazz.name

    override fun put(bundle: Bundle, key: String, value: T?) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): T? {
        return BundleCompat.getParcelable(bundle, key, clazz)
    }

    override fun parseValue(value: String): T? {
        Timber.d("parseValue $value")
        return if (value == "null") null else onParseValue?.invoke(value) ?: Base64.getUrlDecoder().decode(value).unparcel(clazz)
    }

    override fun serializeAsValue(value: T?): String {
        Timber.d("serializeAsValue $value")
        return if (value == null) {
            "null"
        } else {
            Base64.getUrlEncoder().encodeToString(value.parcel())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NullableParcelableNavType<*>

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
