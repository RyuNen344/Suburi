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
import timber.log.Timber
import java.util.concurrent.Executors

class VmPolicyInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            val oldVmPolicy = StrictMode.getVmPolicy()
            runCatching {
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .apply {
                            detectAll()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                penaltyListener(Executors.newSingleThreadExecutor(), Timber::w)
                            } else {
                                penaltyLog()
                            }
                        }
                        .build(),
                )
            }.onFailure {
                Timber.e(it)
                StrictMode.setVmPolicy(oldVmPolicy)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(TimberInitializer::class.java)
    }
}
