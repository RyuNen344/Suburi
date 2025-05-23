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

import android.util.Log
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test

class ChunkedDebugTreeTest {

    private lateinit var tree: ChunkedDebugTree
    private lateinit var prioritySlot: CapturingSlot<Int>
    private lateinit var msgSlot: MutableList<String>

    @Before
    fun setup() {
        tree = ChunkedDebugTree()
        prioritySlot = slot<Int>()
        msgSlot = mutableListOf()
        mockkStatic(Log::class)
        every { Log.println(capture(prioritySlot), TAG, capture(msgSlot)) } answers { prioritySlot.captured }
    }

    @After
    fun cleanup() {
        // enable after 1.14.3 or newer
        // https://github.com/mockk/mockk/issues/242
        // confirmVerified(Log::class)
    }

    @Test
    fun testLog_givenMessage_whenLessThanMaxLogBytes_thenLogsDirectly() {
        val message = "a".repeat(3998)

        tree.log(Log.INFO, message)

        verify(exactly = 1) { Log.println(Log.INFO, TAG, message) }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3998)
    }

    @Test
    fun testLog_givenMessage_whenExactlyMaxLogBytes_thenLogsDirectly() {
        val message = "a".repeat(3999)

        tree.log(Log.INFO, message)

        verify(exactly = 1) { Log.println(Log.INFO, TAG, message) }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3999)
    }

    @Test
    fun testLog_givenMessage_whenJustOverMaxLogBytes_thenLogsInTwoChunks() {
        val message = "a".repeat(4000)

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "a")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(1)
    }

    @Test
    fun testLog_givenMessage_withNewlineAtChunkBoundary_thenSplitsAtNewline() {
        val message = "a".repeat(3999) + "\n" + "b"

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "b")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[2].toByteArray().size).isEqualTo(1)
    }

    @Test
    fun testLog_givenMessage_withNewlineAtMidChunk_thenSplitsAtNewline() {
        val message =
            "a".repeat(1000) + "\n" + "b".repeat(1000) + "\n" + "c".repeat(1000) + "\n" + "d".repeat(1000) + "\n" + "e".repeat(1000)

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(1000) + "\n")
            Log.println(Log.INFO, TAG, "b".repeat(1000) + "\n")
            Log.println(Log.INFO, TAG, "c".repeat(1000) + "\n")
            Log.println(Log.INFO, TAG, "d".repeat(1000) + "\n")
            Log.println(Log.INFO, TAG, "e".repeat(1000))
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(1001)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(1001)
        assertThat(msgSlot[2].toByteArray().size).isEqualTo(1001)
        assertThat(msgSlot[3].toByteArray().size).isEqualTo(1001)
        assertThat(msgSlot[4].toByteArray().size).isEqualTo(1000)
    }

    @Test
    fun testLog_givenMessage_withMultibyteAtBoundary_thenNoCorruption() {
        val message = "a".repeat(3998) + "マルチバイト文字列"

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3998))
            Log.println(Log.INFO, TAG, "マルチバイト文字列")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3998)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(27)
    }

    @Test
    fun testLog_givenMessage_withMultibyteAndNewline_thenNoCorruption() {
        val message = "a".repeat(3998) + "マルチ\nバイト\n文字列"

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3998))
            Log.println(Log.INFO, TAG, "マルチ\n")
            Log.println(Log.INFO, TAG, "バイト\n")
            Log.println(Log.INFO, TAG, "文字列")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3998)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(10)
        assertThat(msgSlot[2].toByteArray().size).isEqualTo(10)
        assertThat(msgSlot[3].toByteArray().size).isEqualTo(9)
    }

    @Test
    fun testLog_givenMessage_withEmojiAtBoundary_thenNoCorruption() {
        val message = "a".repeat(3996) + "😎😎😎😎😎😎😎"

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3996))
            Log.println(Log.INFO, TAG, "😎😎😎😎😎😎😎")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3996)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(28)
    }

    @Test
    fun testLog_givenLongMessageWithoutNewline_thenSplitsByBytes() {
        val message = "a".repeat(3999 * 3 + 1)

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "a")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[2].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[3].toByteArray().size).isEqualTo(1)
    }

    @Test
    fun testLog_givenAllNewlines_thenEachIsSeparateChunk() {
        val message = "a".repeat(3999 * 2 + 1) + "\n".repeat(10)

        tree.log(Log.INFO, message)

        verifySequence {
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "a".repeat(3999))
            Log.println(Log.INFO, TAG, "a\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
            Log.println(Log.INFO, TAG, "\n")
        }
        assertThat(msgSlot[0].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[1].toByteArray().size).isEqualTo(3999)
        assertThat(msgSlot[2].toByteArray().size).isEqualTo(2)
        assertThat(msgSlot[3].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[4].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[5].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[6].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[7].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[8].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[9].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[10].toByteArray().size).isEqualTo(1)
        assertThat(msgSlot[11].toByteArray().size).isEqualTo(1)
    }

    private companion object {
        val TAG: String = ChunkedDebugTreeTest::class.java.simpleName
    }
}
