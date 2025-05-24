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

package io.github.ryunen344.suburi.test.assertk.os

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import java.io.Serializable

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.string]
 */
fun Assert<Bundle>.string(key: String) = prop("string") { it.getString(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.integer]
 */
fun Assert<Bundle>.integer(key: String) = prop("integer") { it.getInt(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.longInt]
 */
fun Assert<Bundle>.longInt(key: String) = prop("longInt") { it.getLong(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.doubleFloat]
 */
fun Assert<Bundle>.doubleFloat(key: String) = prop("doubleFloat") { it.getDouble(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.bool]
 */
fun Assert<Bundle>.bool(key: String) = prop("bool") { it.getBoolean(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.byteArray]
 */
fun Assert<Bundle>.byteArray(key: String) = prop("byteArray") { it.getByteArray(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.parcelable]
 */
inline fun <reified T : Parcelable> Assert<Bundle>.parcelable(key: String) =
    prop("parcelable") { BundleCompat.getParcelable(it, key, T::class.java) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.stringArray]
 */
fun Assert<Bundle>.stringArray(key: String) = prop("stringArray") { it.getStringArray(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.stringArrayList]
 */
fun Assert<Bundle>.stringArrayList(key: String) = prop("stringArrayList") { it.getStringArrayList(key) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.parcelableArrayList]
 */
inline fun <reified T : Parcelable> Assert<Bundle>.parcelableArrayList(key: String) =
    prop("parcelableArrayList") { BundleCompat.getParcelableArrayList(it, key, T::class.java) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.serializable]
 */
inline fun <reified T : Serializable> Assert<Bundle>.serializable(key: String) =
    prop("serializable") { BundleCompat.getSerializable(it, key, T::class.java) }

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.hasSize]
 */
fun Assert<Bundle>.hasSize(size: Int) = prop("size") { actual -> actual.size() }.isEqualTo(size)

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.isEmpty]
 */
fun Assert<Bundle>.isEmpty() = prop("isEmpty") { actual -> actual.isEmpty }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.isNotEmpty]
 */
fun Assert<Bundle>.isNotEmpty() = prop("isEmpty") { actual -> actual.isEmpty }.isFalse()

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.containsKey]
 */
fun Assert<Bundle>.containsKey(key: String) = transform { actual -> actual.containsKey(key) }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.os.BundleSubject.doesNotContainKey]
 */
fun Assert<Bundle>.doesNotContainKey(key: String) = transform { actual -> actual.containsKey(key) }.isFalse()
