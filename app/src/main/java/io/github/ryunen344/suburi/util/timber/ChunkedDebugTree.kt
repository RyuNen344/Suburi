package io.github.ryunen344.suburi.util.timber

import timber.log.Timber

/**
 * A [Timber.DebugTree] that chunks log messages for Multibyte characters.
 */
class ChunkedDebugTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val bytes = message.toByteArray()
        if (bytes.size < MAX_LOG_BYTES) {
            super.log(priority, tag, message, t)
        } else {
            for (chunk in bytes.chunked()) {
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
            } else {
                while (end > offset && decodeToString(offset, end).lastOrNull() == REPLACEMENT_CHARACTER) {
                    end--
                }
            }
            yield(decodeToString(offset, end))
            offset = end
        }
    }

    private fun ByteArray.indexOf(
        byte: Byte,
        startIndex: Int = 0,
        endIndex: Int = size,
    ): Int {
        for (i in startIndex until endIndex) {
            if (this[i] == byte) return i
        }
        return -1
    }

    private companion object {
        private val NEW_LINE_BYTE = '\n'.code.toByte()
        private const val MAX_LOG_BYTES = 4000
        private const val REPLACEMENT_CHARACTER = '\uFFFD'
    }
}
