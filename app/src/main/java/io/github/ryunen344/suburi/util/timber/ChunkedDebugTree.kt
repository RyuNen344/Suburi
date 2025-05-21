package io.github.ryunen344.suburi.util.timber

import okio.Buffer
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
            bytes.chunk().forEach {
                super.log(priority, tag, it.decodeToString(), t)
            }
        }
    }

    private fun ByteArray.chunk(chunkSize: Long = MAX_LOG_BYTES.toLong()): Sequence<ByteArray> {
        val buffer = Buffer().write(this)
        return sequence {
            while (!buffer.exhausted()) {
                // consume new line of the start of the byte
                if (buffer.indexOf(NEW_LINE_BYTE) == 0L) {
                    buffer.skip(1L)
                    // ignore new line of the end of the byte
                    if (buffer.exhausted()) break
                }

                val bytesToRead = when (val newline = buffer.indexOf(NEW_LINE_BYTE)) {
                    -1L -> buffer.size.coerceAtMost(chunkSize)
                    else -> newline.coerceAtMost(chunkSize)
                }

                val segment = Buffer()
                buffer.peek().read(segment, bytesToRead)

                var readCount = bytesToRead
                var valid = false
                do {
                    val bytes = segment.peek().readByteArray(readCount)
                    valid = bytes.decodeToString().lastOrNull() != REPLACEMENT_CHARACTER
                    if (valid) {
                        yield(bytes)
                    } else {
                        readCount--
                    }
                } while (!valid && readCount > 0L)

                buffer.readByteArray(readCount)
            }
        }
    }

    private companion object {
        private val NEW_LINE_BYTE = '\n'.code.toByte()
        private const val MAX_LOG_BYTES = 4000
        private const val REPLACEMENT_CHARACTER = '\uFFFD'
    }
}
