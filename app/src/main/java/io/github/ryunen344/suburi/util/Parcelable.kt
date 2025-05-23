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

package io.github.ryunen344.suburi.util

import android.os.Parcel
import android.os.Parcelable
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("TooGenericExceptionCaught")
inline fun <T : Parcel?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.recycleFinally(exception)
    }
}

@Suppress("TooGenericExceptionCaught")
fun Parcel?.recycleFinally(cause: Throwable?): Unit = when {
    this == null -> {}
    cause == null -> recycle()
    else ->
        try {
            recycle()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

fun Parcelable.parcel(): ByteArray {
    return Parcel.obtain().use { parcel ->
        writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        parcel.marshall()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Parcelable> ByteArray.unparcel(clazz: Class<T>): T {
    val creator = clazz.getDeclaredField("CREATOR").get(null) as? Parcelable.Creator<T>
        ?: throw IllegalArgumentException("Could not access CREATOR field in class ${clazz.simpleName}")
    return Parcel.obtain().use { parcel ->
        parcel.unmarshall(this, 0, this.size)
        parcel.setDataPosition(0)
        creator.createFromParcel(parcel)
    }
}
