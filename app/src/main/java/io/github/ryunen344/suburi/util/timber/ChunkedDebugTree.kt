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

import timber.log.Timber

/**
 * A [Timber.DebugTree] that chunks log messages for Multibyte characters.
 */
class ChunkedDebugTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val bytes = message.toByteArray()
        if (bytes.size <= MAX_LOG_BYTES) {
            super.log(priority, tag, message, t)
        } else {
            bytes.chunked().forEach { chunk ->
                super.log(priority, tag, chunk, t)
            }
        }
    }

    private fun ByteArray.chunked(chunkSize: Int = MAX_LOG_BYTES): Sequence<String> = sequence {
        var offset = 0
        while (offset < size) {
            var end = (offset + chunkSize).coerceAtMost(size)
            val newline = indexOf(NEW_LINE_BYTE, offset, end)
            if (newline != -1) {
                end = newline + 1
                yield(decodeToString(offset, end))
            } else {
                while (end > offset) {
                    val decoded = decodeToString(offset, end)
                    if (decoded.lastOrNull() == REPLACEMENT_CHARACTER) {
                        end--
                    } else {
                        yield(decoded)
                        break
                    }
                }
            }
            offset = end
        }
    }

    private fun ByteArray.indexOf(
        byte: Byte,
        startIndex: Int,
        endIndex: Int,
    ): Int {
        for (i in startIndex until endIndex) {
            if (this[i] == byte) return i
        }
        return -1
    }

    private companion object {
        /**
         * [Timber.DebugTree.MAX_LOG_LENGTH] - 1
         */
        private const val MAX_LOG_BYTES = 3999
        private val NEW_LINE_BYTE = '\n'.code.toByte()
        private const val REPLACEMENT_CHARACTER = '\uFFFD'
    }
}
