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

package io.github.ryunen344.suburi.util.timber

import okio.utf8Size
import timber.log.Timber

/**
 * A [Timber.DebugTree] that safely chunks UTF-8 log messages.
 */
class ChunkedDebugTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (message.requireChunking) {
            chunkingLog(priority, tag, message, t)
        } else {
            super.log(priority, tag, message, t)
        }
    }

    private fun chunkingLog(priority: Int, tag: String?, message: String, t: Throwable?) {
        var chunkStart = 0
        var index = 0
        var chunkByteCount = 0

        while (index < message.length) {
            val codeUnit = message[index]
            val isSurrogatePair = message.isSurrogatePairAt(index)
            val codeUnitCount = if (isSurrogatePair) 2 else 1
            val utf8ByteCount = codeUnit.utf8ByteCount(isSurrogatePair)

            if (chunkByteCount + utf8ByteCount > MAX_LOG_BYTES) {
                super.log(
                    priority,
                    tag,
                    message.substring(chunkStart, index),
                    t,
                )

                chunkStart = index
                chunkByteCount = 0
            } else {
                index += codeUnitCount
                chunkByteCount += utf8ByteCount

                if (codeUnit == NEW_LINE) {
                    super.log(
                        priority,
                        tag,
                        message.substring(chunkStart, index),
                        t,
                    )

                    chunkStart = index
                    chunkByteCount = 0
                }
            }
        }

        if (chunkStart < message.length) {
            super.log(
                priority,
                tag,
                message.substring(chunkStart),
                t,
            )
        }
    }

    private fun String.isSurrogatePairAt(index: Int): Boolean {
        return this[index] in HIGH_SURROGATE_START..HIGH_SURROGATE_END &&
            index + 1 < length &&
            this[index + 1] in LOW_SURROGATE_START..LOW_SURROGATE_END
    }

    private fun Char.utf8ByteCount(isSurrogatePair: Boolean): Int {
        return when {
            this < UTF8_TWO_BYTE_THRESHOLD -> 1
            this < UTF8_THREE_BYTE_THRESHOLD -> 2
            this !in HIGH_SURROGATE_START..LOW_SURROGATE_END -> 3
            isSurrogatePair -> 4
            else -> 1
        }
    }

    private val String.requireChunking: Boolean
        get() = length > GUARANTEED_FIT_CHAR_COUNT && utf8Size() > MAX_LOG_BYTES

    private companion object {
        /**
         * [Timber.DebugTree.MAX_LOG_LENGTH] - 1
         */
        private const val MAX_LOG_BYTES = 3999

        /**
         * A UTF-16 code unit requires at most 3 bytes in UTF-8.
         */
        private const val GUARANTEED_FIT_CHAR_COUNT = MAX_LOG_BYTES / 3

        private const val UTF8_TWO_BYTE_THRESHOLD = '\u0080'
        private const val UTF8_THREE_BYTE_THRESHOLD = '\u0800'

        private const val HIGH_SURROGATE_START = '\uD800'
        private const val HIGH_SURROGATE_END = '\uDBFF'
        private const val LOW_SURROGATE_START = '\uDC00'
        private const val LOW_SURROGATE_END = '\uDFFF'

        private const val NEW_LINE = '\n'
    }
}
