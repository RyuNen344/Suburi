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

package io.github.ryunen344.suburi.test.assertk.util

import android.util.SparseBooleanArray
import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.support.expected
import assertk.assertions.support.show

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.hasTrueValueAt]
 */
fun Assert<SparseBooleanArray>.hasTrueValueAt(key: Int) = transform { actual -> actual.get(key) }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.hasFalseValueAt]
 */
fun Assert<SparseBooleanArray>.hasFalseValueAt(key: Int) = given { actual ->
    if (actual.indexOfKey(key) == -1) {
        expected("key :${show(key)} expected to be present but was not")
    }
    if (actual.get(key)) {
        expected("value for key :${show(key)} expected to be false but was not")
    }
}

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.containsKey]
 */
fun Assert<SparseBooleanArray>.containsKey(key: Int) = transform { actual -> actual.indexOfKey(key) }.isGreaterThan(-1)

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.doesNotContainKey]
 */
fun Assert<SparseBooleanArray>.doesNotContainKey(key: Int) = transform { actual -> actual.indexOfKey(key) }.isLessThan(0)

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.hasSize]
 */
fun Assert<SparseBooleanArray>.hasSize(size: Int) = prop("size") { actual -> actual.size() }.isEqualTo(size)

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.isEmpty]
 */
fun Assert<SparseBooleanArray>.isEmpty() = prop("size") { actual -> actual.size() }.isEqualTo(0)

/**
 * assertk extension of [androidx.test.ext.truth.util.SparseBooleanArraySubject.isNotEmpty]
 */
fun Assert<SparseBooleanArray>.isNotEmpty() = prop("size") { actual -> actual.size() }.isNotEqualTo(0)
