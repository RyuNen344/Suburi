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

package io.github.ryunen344.suburi.slf4j

import android.os.Build
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern

class TimberSLF4JLoggerFactory : ILoggerFactory {

    private val loggers: ConcurrentMap<String, Logger> = ConcurrentHashMap()

    override fun getLogger(name: String?): Logger {
        val tag = name?.let(::createTag) ?: TAG_ANONYMOUS
        return loggers.computeIfAbsent(tag, ::TimberSLF4JLogger)
    }

    private fun createTag(name: String): String {
        var tag = name
        val matcher = ANONYMOUS_CLASS.matcher(tag)
        if (matcher.find()) {
            tag = matcher.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tag
        } else {
            tag.substring(0, MAX_TAG_LENGTH)
        }
    }

    private companion object {
        const val MAX_TAG_LENGTH = 23
        val ANONYMOUS_CLASS: Pattern = Pattern.compile("(\\$\\d+)+$")
        const val TAG_ANONYMOUS: String = "anonymous"
    }
}
