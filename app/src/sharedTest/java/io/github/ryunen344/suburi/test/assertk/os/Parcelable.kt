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

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.test.core.os.Parcelables
import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.fail

/**
 * assertk extension of [androidx.test.ext.truth.os.ParcelableSubject.recreatesEqual]
 */
fun <T : Parcelable> Assert<T>.recreatesEqual(creator: Creator<T>) = given { actual ->
    isEqualTo(Parcelables.forceParcel(actual, creator))
}

/**
 * assertk extension of [androidx.test.ext.truth.os.ParcelableSubject.marshallsEquallyTo]
 */
fun <T : Parcelable> Assert<T>.marshallsEquallyTo(other: Parcelable) = given { actual ->
    val parcel = Parcel.obtain()
    try {
        actual.writeToParcel(parcel, 0)
        val actualBytes = parcel.marshall()
        parcel.setDataPosition(0)
        other.writeToParcel(parcel, 0)
        val otherBytes = parcel.marshall()
        if (!actualBytes.contentEquals(otherBytes)) {
            fail(other, actual)
        }
    } finally {
        parcel.recycle()
    }
}
