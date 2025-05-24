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

package io.github.ryunen344.suburi.coil

import android.util.Log
import coil3.util.Logger
import timber.log.Timber

internal class TimberCoilLogger @JvmOverloads constructor(
    override var minLevel: Logger.Level = Logger.Level.Debug,
) : Logger {

    override fun log(tag: String, level: Logger.Level, message: String?, throwable: Throwable?) {
        Timber.tag(tag).log(level.priority, throwable, message)
    }

    private companion object {
        val Logger.Level.priority: Int
            get() = when (this) {
                Logger.Level.Verbose -> Log.VERBOSE
                Logger.Level.Debug -> Log.DEBUG
                Logger.Level.Info -> Log.INFO
                Logger.Level.Warn -> Log.WARN
                Logger.Level.Error -> Log.ERROR
            }
    }
}
