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

package io.github.ryunen344.suburi.startup

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.startup.Initializer
import io.github.ryunen344.suburi.BuildConfig
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.reflect.typeOf

class ThreadPolicyInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            val oldThreadPolicy = StrictMode.getThreadPolicy()
            runCatching {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .apply {
                            val dispatcher = newFixedThreadPoolContext(1, "ThreadPolicyInitializer")
                            runBlocking(dispatcher) {
                                typeOf<ThreadPolicyInitializer>()
                                detectAll()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    penaltyListener(dispatcher.executor, Timber::w)
                                } else {
                                    penaltyLog()
                                }
                            }
                        }
                        .build(),
                )
            }.onFailure {
                Timber.e(it)
                StrictMode.setThreadPolicy(oldThreadPolicy)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(TimberInitializer::class.java)
    }
}
